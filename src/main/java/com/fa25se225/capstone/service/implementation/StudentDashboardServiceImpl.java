package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.response.PurchasedMaterialInfo;
import com.fa25se225.capstone.dto.response.StudentExamDashboardResponse;
import com.fa25se225.capstone.dto.response.StudentFinancialDashboardResponse;
import com.fa25se225.capstone.dto.response.StudentOverallDashboardResponse;
import com.fa25se225.capstone.service.StudentDashboardService;

import com.fa25se225.capstone.entity.*;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.ExamAttemptV2Mapper;
import com.fa25se225.capstone.repository.*;
import com.fa25se225.capstone.repository.v2.*;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardServiceImpl implements StudentDashboardService {

    private final AccountUtil accountUtil;
    private final ExamAttemptV2Repository attemptRepository;
    private final StudentAnswerV2Repository answerRepository;
    private final ExamAttemptV2Mapper attemptMapper;
    private final StudentProfileRepository studentProfileRepository;

    // Repositories for financial dashboard
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final LearningMaterialRepository learningMaterialRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Cacheable(value = "student_exam_dashboard", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public StudentExamDashboardResponse getStudentExamDashboard() {
        User student = accountUtil.getCurrentUser();
        String studentId = student.getId();
        return getStudentExamDashboardById(studentId);
    }

    @Override
    @Cacheable(value = "children_exam_dashboard", key = "#childrenId", unless = "#result == null")
    public StudentExamDashboardResponse getChildrenExamDashboard(String childrenId) {
        return getStudentExamDashboardById(childrenId);
    }

    private StudentExamDashboardResponse getStudentExamDashboardById(String studentId) {

        long total = attemptRepository.countByUserIdAndStatus(studentId, AttemptStatusV2.COMPLETED);
        Double avgScore = attemptRepository.getAverageScoreByUserId(studentId);
        String recommend = "";
        Optional<StudentProfile> studentProfileOpt = studentProfileRepository.findByUserId(studentId);
        if(studentProfileOpt.isPresent() && Strings.isNotBlank(studentProfileOpt.get().getRecommend())){
            recommend = studentProfileOpt.get().getRecommend();
        }

        long inProgress = 0;


        List<Object[]> topicStats = answerRepository.analyzeTopicPerformance(studentId);
        Map<String, Double> topicPerformance = new HashMap<>();
        String weakestTopic = null;
        double minAcc = 100.0;

        for (Object[] row : topicStats) {
            String topicName = (String) row[0];
            long totalQ = (Long) row[1];
            long correctQ = (Long) row[2];

            double accuracy = totalQ > 0 ? (double) correctQ / totalQ * 100 : 0;
            topicPerformance.put(topicName, Math.round(accuracy * 10.0) / 10.0);

            if (accuracy < minAcc) {
                minAcc = accuracy;
                weakestTopic = topicName;
            }
        }

        var recentPage = attemptRepository.findByUserId(studentId, PageRequest.of(0, 10));
        var recentList = recentPage.getContent().stream().map(attemptMapper::toResponse).toList();


        return StudentExamDashboardResponse.builder()
                .totalExamsTaken(total)
                .averageScore(avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0)
                .examsInProgress(inProgress)
                .topicPerformance(topicPerformance)
                .recommendedTopic(weakestTopic)
                .recommend(recommend)
                .recentAttempts(recentList)
                .build();
    }

    @Override
    @Cacheable(value = "student_financial_dashboard", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public StudentFinancialDashboardResponse getStudentFinancialDashboard() {
        User student = accountUtil.getCurrentUser();
        return getStudentFinancialDashboardById(student.getId());
    }

    @Override
    @Cacheable(value = "children_financial_dashboard", key = "#childrenId", unless = "#result == null")
    public StudentFinancialDashboardResponse getChildrenFinancialDashboard(String childrenId) {
        return getStudentFinancialDashboardById(childrenId);
    }

    @Override
    @Cacheable(value = "student_overall_dashboard", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public StudentOverallDashboardResponse getStudentOverallDashboard() {
        User student = accountUtil.getCurrentUser();
        
        StudentExamDashboardResponse examStats = getStudentExamDashboardById(student.getId());
        StudentFinancialDashboardResponse financialStats = getStudentFinancialDashboardById(student.getId());
        
        return StudentOverallDashboardResponse.builder()
                .examStats(examStats)
                .financialStats(financialStats)
                .build();
    }

    private StudentFinancialDashboardResponse getStudentFinancialDashboardById(String studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy tất cả permissions của student có dạng "LEARNING_xxx"
        Set<Permission> grantedPermissions = student.getGrantedPermissions();
        List<String> learningMaterialIds = grantedPermissions.stream()
                .map(Permission::getName)
                .filter(name -> name.startsWith("LEARNING_"))
                .map(name -> name.substring("LEARNING_".length()))
                .collect(Collectors.toList());

        if (learningMaterialIds.isEmpty()) {
            // Nếu không có learning material nào, trả về response rỗng
            return StudentFinancialDashboardResponse.builder()
                    .totalSpent(BigDecimal.ZERO)
                    .totalRegisteredMaterials(0L)
                    .currentBalance(getCurrentBalance(student))
                    .spendingBySubject(new HashMap<>())
                    .spendingByType(new HashMap<>())
                    .monthlySpending(new HashMap<>())
                    .recentPurchases(new ArrayList<>())
                    .build();
        }

        // Lấy danh sách learning materials đã mua
        List<LearningMaterial> purchasedMaterials = learningMaterialRepository.findAllById(learningMaterialIds);
        
        // Tính tổng số tiền đã chi
        BigDecimal totalSpent = purchasedMaterials.stream()
                .map(LearningMaterial::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy số dư hiện tại
        BigDecimal currentBalance = getCurrentBalance(student);

        // Chi tiêu theo subject
        Map<String, BigDecimal> spendingBySubject = purchasedMaterials.stream()
                .filter(material -> material.getSubject() != null)
                .collect(Collectors.groupingBy(
                        material -> material.getSubject().getName(),
                        Collectors.reducing(BigDecimal.ZERO, LearningMaterial::getPrice, BigDecimal::add)
                ));

        // Chi tiêu theo type
        Map<String, BigDecimal> spendingByType = purchasedMaterials.stream()
                .filter(material -> material.getType() != null)
                .collect(Collectors.groupingBy(
                        material -> material.getType().getName(),
                        Collectors.reducing(BigDecimal.ZERO, LearningMaterial::getPrice, BigDecimal::add)
                ));

        // Chi tiêu theo tháng (lấy từ transactions)
        Map<String, BigDecimal> monthlySpending = getMonthlySpending(student, learningMaterialIds);

        // Lấy danh sách purchases gần nhất (top 10)
        List<PurchasedMaterialInfo> recentPurchases = getRecentPurchases(student, learningMaterialIds);

        return StudentFinancialDashboardResponse.builder()
                .totalSpent(totalSpent)
                .totalRegisteredMaterials((long) purchasedMaterials.size())
                .currentBalance(currentBalance)
                .spendingBySubject(spendingBySubject)
                .spendingByType(spendingByType)
                .monthlySpending(monthlySpending)
                .recentPurchases(recentPurchases)
                .build();
    }

    private BigDecimal getCurrentBalance(User user) {
        Optional<Payment> paymentOpt = paymentRepository.findByUser(user);
        return paymentOpt.map(Payment::getAmount).orElse(BigDecimal.ZERO);
    }

    private Map<String, BigDecimal> getMonthlySpending(User user, List<String> learningMaterialIds) {
        // Lấy các transactions liên quan đến learning materials
        List<Transaction> transactions = transactionRepository.findByPayment_User(user);
        
        // Filter transactions có reference đến learning materials
        Map<String, BigDecimal> monthlySpending = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        // Khởi tạo 6 tháng gần nhất với giá trị 0
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            monthlySpending.put(month.format(formatter), BigDecimal.ZERO);
        }

        // Tính toán chi tiêu từ transactions
        transactions.stream()
                .filter(transaction -> transaction.getExternalReference() != null)
                .filter(transaction -> transaction.getExternalReference().startsWith("PAYMENT LEARNING_"))
                .filter(transaction -> {
                    String materialId = transaction.getExternalReference().substring("PAYMENT LEARNING_".length());
                    return learningMaterialIds.contains(materialId);
                })
                .forEach(transaction -> {
                    LocalDate createdAt = transaction.getCreatedAt();
                    if (createdAt != null) {
                        String monthKey = createdAt.format(formatter);
                        if (monthlySpending.containsKey(monthKey)) {
                            BigDecimal current = monthlySpending.get(monthKey);
                            monthlySpending.put(monthKey, current.add(transaction.getAmount()));
                        }
                    }
                });

        return monthlySpending;
    }

    private List<PurchasedMaterialInfo> getRecentPurchases(User user, List<String> learningMaterialIds) {
        // Lấy transactions và map với learning materials
        List<Transaction> transactions = transactionRepository.findByPayment_User(user);
        
        Map<String, LocalDate> materialPurchaseDate = new HashMap<>();
        
        transactions.stream()
                .filter(transaction -> transaction.getExternalReference() != null)
                .filter(transaction -> transaction.getExternalReference().startsWith("PAYMENT LEARNING_"))
                .forEach(transaction -> {
                    String materialId = transaction.getExternalReference().substring("PAYMENT LEARNING_".length());
                    if (learningMaterialIds.contains(materialId)) {
                        materialPurchaseDate.putIfAbsent(materialId, transaction.getCreatedAt());
                    }
                });

        // Lấy thông tin chi tiết của các materials
        List<LearningMaterial> materials = learningMaterialRepository.findAllById(learningMaterialIds);
        
        return materials.stream()
                .map(material -> PurchasedMaterialInfo.builder()
                        .materialId(material.getId())
                        .title(material.getTitle())
                        .subjectName(material.getSubject() != null ? material.getSubject().getName() : "N/A")
                        .typeName(material.getType() != null ? material.getType().getName() : "N/A")
                        .price(material.getPrice())
                        .purchasedDate(materialPurchaseDate.getOrDefault(material.getId(), material.getCreatedAt()))
                        .authorName(material.getAuthor() != null ? 
                                material.getAuthor().getFirstName() + " " + material.getAuthor().getLastName() : "Unknown")
                        .fileImage(material.getFileImage())
                        .build())
                .sorted((a, b) -> b.getPurchasedDate().compareTo(a.getPurchasedDate()))
                .limit(10)
                .collect(Collectors.toList());
    }

}
