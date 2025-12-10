package com.fa25se225.capstone.configuration.dataseed;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.TokenTransactionType;
import com.fa25se225.capstone.entity.TransactionStatus;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.*;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.TokenTransactionTypeRepository;
import com.fa25se225.capstone.repository.TransactionStatusRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.repository.v2.*;
import com.fa25se225.capstone.entity.MaterialType;
import com.fa25se225.capstone.repository.MaterialTypeRepository;
import com.fa25se225.capstone.entity.PaymentStatus;
import com.fa25se225.capstone.repository.PaymentStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeederV2 {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionDifficultyV2Repository difficultyRepository;
    private final QuestionTopicV2Repository topicRepository;
    private final QuestionV2Repository questionRepository;
    private final ExamTemplateV2Repository templateRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final TransactionStatusRepository transactionStatusRepository;
    private final TokenTransactionTypeRepository tokenTransactionTypeRepository;


    @Transactional
    public void createExamDataSeed() {
        log.info("Starting V2 data seeding...");
        try {
            seedPaymentStatuses();
            createTransactionStatusSeed();
            seedTokenTransactionTypes();
//            createAPQuestionTopicsV2();

            if (questionRepository.findAll().isEmpty()) {
                User teacher = userRepository.findByEmail("admin123@gmail.com")
                        .orElseThrow(() -> new RuntimeException("User 'admin123@gmail.com' not found. Please ensure this user exists."));

                Subject mathSubject = subjectRepository.findByNameIgnoreCase("Mathematics")
                        .orElseGet(() -> subjectRepository.save(
                                Subject.builder().name("Mathematics").code("MATH").build()
                        ));

                QuestionDifficultyV2 diffEasy = createDifficulty("Easy", "Easy level questions");
                QuestionDifficultyV2 diffMedium = createDifficulty("Medium", "Medium level questions");
                QuestionDifficultyV2 diffHard = createDifficulty("Hard", "Hard level questions");

                QuestionTopicV2 topicAlgebra = createTopic("Algebra", mathSubject, teacher);
                QuestionTopicV2 topicGeometry = createTopic("Geometry", mathSubject, teacher);

                log.info("Creating 30 questions...");
                List<QuestionV2> allQuestions = new ArrayList<>();

                // 10 MCQ Algebra (5 Easy, 5 Medium)
                allQuestions.addAll(createMcqQuestions(teacher, mathSubject, topicAlgebra, diffEasy, 5, "Algebra Easy"));
                allQuestions.addAll(createMcqQuestions(teacher, mathSubject, topicAlgebra, diffMedium, 5, "Algebra Medium"));

                // 10 MCQ Geometry (5 Easy, 5 Medium)
                allQuestions.addAll(createMcqQuestions(teacher, mathSubject, topicGeometry, diffEasy, 5, "Geometry Easy"));
                allQuestions.addAll(createMcqQuestions(teacher, mathSubject, topicGeometry, diffMedium, 5, "Geometry Medium"));

                // 10 FRQ (5 Algebra Hard, 5 Geometry Hard)
                allQuestions.addAll(createFrqQuestions(teacher, mathSubject, topicAlgebra, diffHard, 5, "Algebra Hard"));
                allQuestions.addAll(createFrqQuestions(teacher, mathSubject, topicGeometry, diffHard, 5, "Geometry Hard"));

                questionRepository.saveAll(allQuestions);
                log.info("Created {} questions.", allQuestions.size());

                // 6. Create Exam Template
                createExamTemplate(teacher, mathSubject, topicAlgebra, topicGeometry, diffEasy, diffMedium, diffHard);


                //7.create learning material type
                createMaterialTypeSeed();

                log.info("V2 data seeding complete.");
            } else {
                log.info("V2 data already seeded. Skipping.");
            }

        } catch (Exception e) {
            log.error("Error during V2 data seeding: {}", e.getMessage(), e);
        }
    }

    private QuestionDifficultyV2 createDifficulty(String name, String desc) {
        return difficultyRepository.findById(name)
                .orElseGet(() -> difficultyRepository.save(
                        QuestionDifficultyV2.builder().name(name).description(desc).build()
                ));
    }

    private QuestionTopicV2 createTopic(String name, Subject subject, User teacher) {
        return topicRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> topicRepository.save(
                        QuestionTopicV2.builder()
                                .name(name)
                                .subject(subject)
                                .createdBy(teacher)
                                .build()
                ));
    }

    private QuestionTopicV2 createTopicWithDescription(String name, String description, Subject subject, User teacher) {
        return topicRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> topicRepository.save(
                        QuestionTopicV2.builder()
                                .name(name)
                                .description(description)
                                .subject(subject)
                                .createdBy(teacher)
                                .build()
                ));
    }

    private List<QuestionV2> createMcqQuestions(User teacher, Subject subject, QuestionTopicV2 topic, QuestionDifficultyV2 diff, int count, String prefix) {
        List<QuestionV2> questions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            QuestionV2 q = QuestionV2.builder()
                    .content("Content for " + prefix + " question " + i + "?")
                    .type(QuestionType.MCQ)
                    .subject(subject)
                    .difficulty(diff)
                    .topic(topic)
                    .createdBy(teacher)
                    .build();

            AnswerV2 a1 = AnswerV2.builder().question(q).content("Choice A (Incorrect)").isCorrect(false).build();
            AnswerV2 a2 = AnswerV2.builder().question(q).content("Choice B (Correct)").isCorrect(true).build();
            AnswerV2 a3 = AnswerV2.builder().question(q).content("Choice C (Incorrect)").isCorrect(false).build();
            AnswerV2 a4 = AnswerV2.builder().question(q).content("Choice D (Incorrect)").isCorrect(false).build();

            q.setAnswers(Arrays.asList(a1, a2, a3, a4));
            questions.add(q);
        }
        return questions;
    }

    /**
     * Creates realistic FRQ questions based on the prefix.
     */
    private List<QuestionV2> createFrqQuestions(User teacher, Subject subject, QuestionTopicV2 topic, QuestionDifficultyV2 diff, int count, String prefix) {
        if ("Algebra Hard".equals(prefix)) {
            return createAlgebraFrqQuestions(teacher, subject, topic, diff);
        } else if ("Geometry Hard".equals(prefix)) {
            return createGeometryFrqQuestions(teacher, subject, topic, diff);
        }

        // Fallback for any other prefix
        log.warn("Unknown FRQ prefix '{}', creating generic questions.", prefix);
        List<QuestionV2> questions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            questions.add(createSingleFrq(
                    "Generic " + prefix + " FRQ " + i + ". Please explain.",
                    "This is a generic model answer for " + prefix + " " + i + ".",
                    "Generic explanation.",
                    teacher, subject, topic, diff
            ));
        }
        return questions;
    }

    /**
     * Helper method to create a single FRQ question.
     */
    private QuestionV2 createSingleFrq(String content, String modelAnswer, String explanation,
                                       User teacher, Subject subject, QuestionTopicV2 topic, QuestionDifficultyV2 diff) {
        QuestionV2 q = QuestionV2.builder()
                .content(content)
                .type(QuestionType.FRQ)
                .subject(subject)
                .difficulty(diff)
                .topic(topic)
                .createdBy(teacher)
                .build();

        AnswerV2 answer = AnswerV2.builder()
                .question(q)
                .content(modelAnswer)
                .isCorrect(true)
                .explanation(explanation)
                .build();

        q.setAnswers(List.of(answer));
        return q;
    }

    /**
     * Creates 5 specific Algebra FRQ questions.
     */
    private List<QuestionV2> createAlgebraFrqQuestions(User teacher, Subject subject, QuestionTopicV2 topic, QuestionDifficultyV2 diff) {
        List<QuestionV2> questions = new ArrayList<>();

        questions.add(createSingleFrq(
                "Solve the quadratic equation $x^2 - 5x + 6 = 0$. Explain your steps.",
                "To solve $x^2 - 5x + 6 = 0$, we look for two numbers that multiply to 6 and add to -5. These numbers are -2 and -3. Therefore, the equation can be factored as $(x - 2)(x - 3) = 0$. By the zero-product property, the solutions are $x = 2$ and $x = 3$.",
                "Check: $(2)^2 - 5(2) + 6 = 4 - 10 + 6 = 0$. $(3)^2 - 5(3) + 6 = 9 - 15 + 6 = 0$. Both solutions are correct.",
                teacher, subject, topic, diff
        ));

        questions.add(createSingleFrq(
                "Simplify the expression $(x^2 - 4) / (x - 2)$. Explain any domain restrictions.",
                "Step 1: Factor the numerator. $x^2 - 4$ is a difference of squares, which factors to $(x - 2)(x + 2)`. \nStep 2: Rewrite the expression: `(x - 2)(x + 2) / (x - 2)`. \nStep 3: Identify domain restrictions. The original denominator `(x - 2)` cannot be zero, so $x \neq 2$. \nStep 4: Cancel the common factor: The `(x - 2)` terms cancel out. \nThe simplified expression is $x + 2$, with the restriction that $x \neq 2$.",
                "The function has a hole (removable discontinuity) at $x = 2$.",
                teacher, subject, topic, diff
        ));

        questions.add(createSingleFrq(
                "Solve for x: $\\log_3(x) + \\log_3(x - 2) = 1$. Justify each step.",
                "Step 1: Apply the log product rule: $\\log_3(x(x - 2)) = 1$. \nStep 2: Convert to exponential form: $x(x - 2) = 3^1$. \nStep 3: Simplify and solve the quadratic equation: $x^2 - 2x = 3$, which gives $x^2 - 2x - 3 = 0$. \nStep 4: Factor the quadratic: $(x - 3)(x + 1) = 0$. Possible solutions are $x = 3$ and $x = -1$. \nStep 5: Check domain. The original $\\log_3(x)$ and $\\log_3(x - 2)$ must have positive arguments. $x = -1$ is invalid because $\\log_3(-1)$ is undefined. $x = 3$ is valid. \nThe final solution is $x = 3$.",
                "Domain checking is a critical step for logarithmic equations.",
                teacher, subject, topic, diff
        ));

        questions.add(createSingleFrq(
                "Find the inverse function $f^{-1}(x)$ for $f(x) = 2x + 3$. Show your work.",
                "Step 1: Replace $f(x)$ with $y$: $y = 2x + 3$. \nStep 2: Swap $x$ and $y$: $x = 2y + 3$. \nStep 3: Solve for $y$. Subtract 3 from both sides: $x - 3 = 2y$. \nStep 4: Divide by 2: $y = (x - 3) / 2$. \nStep 5: Replace $y$ with $f^{-1}(x)$. \nThe inverse function is $f^{-1}(x) = (x - 3) / 2$.",
                "The inverse function swaps the domain and range of the original function.",
                teacher, subject, topic, diff
        ));

        questions.add(createSingleFrq(
                "Solve the inequality $|x - 3| > 5$ and express the solution in interval notation.",
                "An absolute value inequality $|A| > B$ breaks into two cases: $A > B$ or $A < -B$. \nCase 1: $x - 3 > 5$. Add 3 to both sides: $x > 8$. \nCase 2: $x - 3 < -5$. Add 3 to both sides: $x < -2$. \nThe solution is all numbers $x$ such that $x < -2$ OR $x > 8$. \nIn interval notation, this is $(-\\infty, -2) \\cup (8, \\infty)$.",
                "The solution represents all numbers that are more than 5 units away from 3 on the number line.",
                teacher, subject, topic, diff
        ));

        return questions;
    }

    /**
     * Creates 5 specific Geometry FRQ questions.
     */
    private List<QuestionV2> createGeometryFrqQuestions(User teacher, Subject subject, QuestionTopicV2 topic, QuestionDifficultyV2 diff) {
        List<QuestionV2> questions = new ArrayList<>();

        questions.add(createSingleFrq(
                "A right triangle has a hypotenuse of length 13 and one leg of length 5. Find the length of the other leg and explain the theorem used.",
                "We use the Pythagorean theorem, which states $a^2 + b^2 = c^2$ for a right triangle, where $a$ and $b$ are the legs and $c$ is the hypotenuse. \nGiven $a = 5$ and $c = 13$. We need to find $b$. \nThe equation is $5^2 + b^2 = 13^2$. \nThis simplifies to $25 + b^2 = 169$. \nSubtract 25 from both sides: $b^2 = 144$. \nTake the square root of both sides: $b = 12$ (length must be positive). \nThe length of the other leg is 12.",
                "This is a classic 5-12-13 Pythagorean triple.",
                teacher, subject, topic, diff
        ));

        questions.add(createSingleFrq(
                "A circle has a circumference of $18\\pi$ cm. What is the area of the circle? Explain the formulas used.",
                "Step 1: Find the radius using the circumference formula $C = 2\\pi r$. \nGiven $C = 18\\pi$. So, $18\\pi = 2\\pi r$. \nDivide by $2\\pi$: $r = 9$ cm. \nStep 2: Find the area using the area formula $A = \\pi r^2$. \nSubstitute the radius $r = 9$: $A = \\pi(9^2)`. \nThe area is $81\\pi$ cm$^2$.",
                "The key is to use the circumference to find the radius first, then use the radius to find the area.",
                teacher, subject, topic, diff
        ));

        questions.add(createSingleFrq(
                "Triangle ABC is similar to triangle DEF. If $AB = 4$, $DE = 6$, and the area of triangle ABC is 20, what is the area of triangle DEF? Explain the relationship.",
                "Step 1: Find the ratio of corresponding sides (scale factor), $k$. $k = DE / AB = 6 / 4 = 3/2$. \nStep 2: Use the relationship between the areas of similar triangles. The ratio of the areas is the square of the scale factor, $k^2$. \nArea(DEF) / Area(ABC) = $k^2 = (3/2)^2 = 9/4$. \nStep 3: Set up the equation: Area(DEF) / 20 = 9/4. \nStep 4: Solve for Area(DEF): Area(DEF) = 20 * (9/4) = 5 * 9 = 45. \nThe area of triangle DEF is 45.",
                "Area scales with the square of the side length (a 2D measurement).",
                teacher, subject, topic, diff
        ));

        questions.add(createSingleFrq(
                "Find the volume of a cone with a radius of 3 units and a height of 7 units. What is the formula?",
                "The formula for the volume of a cone is $V = (1/3)\\pi r^2 h$, where $r$ is the radius and $h$ is the height. \nGiven $r = 3$ and $h = 7$. \nSubstitute the values: $V = (1/3)\\pi(3^2)(7)`. \nSimplify $3^2$: $V = (1/3)\\pi(9)(7)`. \nMultiply: $V = 3 \\cdot \\pi \\cdot 7`. \nThe final volume is $21\\pi$ cubic units.",
                "The volume of a cone is exactly one-third the volume of a cylinder with the same radius and height.",
                teacher, subject, topic, diff
        ));

        questions.add(createSingleFrq(
                "The sum of the interior angles of a polygon is $1080^\\circ$. How many sides does this polygon have? Explain the formula.",
                "The formula for the sum of the interior angles of a polygon with $n$ sides is $S = (n - 2) \\cdot 180^\\circ$. \nWe are given $S = 1080^\\circ$. \nSet up the equation: $1080 = (n - 2) \\cdot 180$. \nStep 1: Divide both sides by 180. $1080 / 180 = 6$. \nStep 2: We now have $6 = n - 2$. \nStep 3: Add 2 to both sides: $n = 8$. \nThe polygon has 8 sides (it is an octagon).",
                "This formula works because any polygon can be divided into $n-2$ triangles, each having $180^\\circ$.",
                teacher, subject, topic, diff
        ));

        return questions;
    }


    private void createExamTemplate(User teacher, Subject subject, QuestionTopicV2 topicAlgebra, QuestionTopicV2 topicGeometry,
                                    QuestionDifficultyV2 diffEasy, QuestionDifficultyV2 diffMedium, QuestionDifficultyV2 diffHard) {

        if (templateRepository.findByTitle("Comprehensive Math Exam V2").isPresent()) {
            log.warn("ExamTemplate 'Comprehensive Math Exam V2' already exists, skipping creation.");
            return;
        }

        ExamTemplateV2 template = ExamTemplateV2.builder()
                .title("Comprehensive Math Exam V2")
                .description("A 15-question comprehensive exam (10 MCQ, 5 FRQ) generated from V2 bank.")
                .subject(subject)
                .createdBy(teacher)
                .duration(90)
                .passingScore(15)
                .isActive(true)
                .tokenCost(BigDecimal.ZERO)
                .build();

        List<ExamRuleV2> rules = Arrays.asList(
                //total 25 points
                // 5 MCQ Algebra
                ExamRuleV2.builder().template(template).topic(topicAlgebra).difficulty(diffEasy).questionType(QuestionType.MCQ).numberOfQuestions(2).points(2.0).build(),
                ExamRuleV2.builder().template(template).topic(topicAlgebra).difficulty(diffMedium).questionType(QuestionType.MCQ).numberOfQuestions(3).points(2.0).build(),
                // 5 MCQ Geometry
                ExamRuleV2.builder().template(template).topic(topicGeometry).difficulty(diffEasy).questionType(QuestionType.MCQ).numberOfQuestions(2).points(2.0).build(),
                ExamRuleV2.builder().template(template).topic(topicGeometry).difficulty(diffMedium).questionType(QuestionType.MCQ).numberOfQuestions(3).points(2.0).build(),
                // 10 FRQ (Hard)
                ExamRuleV2.builder().template(template).topic(topicAlgebra).difficulty(diffHard).questionType(QuestionType.FRQ).numberOfQuestions(2).points(3.0).build(),
                ExamRuleV2.builder().template(template).topic(topicGeometry).difficulty(diffHard).questionType(QuestionType.FRQ).numberOfQuestions(3).points(3.0).build()
        );

        template.setRules(rules);
        templateRepository.save(template);
        log.info("Successfully created ExamTemplate: {}", template.getTitle());
    }

    @Transactional
    public void createMaterialTypeSeed() {
        log.info("Starting MaterialType data seeding...");
        List<MaterialType> materialTypes = Arrays.asList(
                MaterialType.builder()
                        .name("FREE")
                        .description("Public learning materials that users can access for free")
                        .deleted(false)
                        .build(),

                MaterialType.builder()
                        .name("TOKEN")
                        .description("Premium learning materials that require tokens to unlock")
                        .deleted(false)
                        .build(),

                MaterialType.builder()
                        .name("PRIVATE")
                        .description("Private learning materials visible only to the author or permitted users")
                        .deleted(false)
                        .build()
        );

        for (MaterialType mt : materialTypes) {
            materialTypeRepository.save(mt);
            log.info("Successfully created MaterialType: {}", mt.getName());
        }
    }
    @Transactional
    public void createTransactionStatusSeed() {
        if (transactionStatusRepository.count() == 0) {
            createTransactionStatusIfNotExists("success", "Success", "Transaction completed successfully.");
            createTransactionStatusIfNotExists("fail", "Fail", "Transaction failed.");
            createTransactionStatusIfNotExists("pending", "Pending", "Transaction is pending.");
            log.info("Seeded TransactionStatus data.");
        } else {
            log.info("TransactionStatus table is not empty. Skipping seeding.");
        }
    }

    private void createTransactionStatusIfNotExists(String code, String name, String description) {
        if (transactionStatusRepository.findByCode(code).isEmpty() && transactionStatusRepository.findByName(name).isEmpty()) {
            TransactionStatus status = TransactionStatus.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .build();
            transactionStatusRepository.save(status);
            log.info("Seeded TransactionStatus: {}", name);
        }
    }


    private void seedPaymentStatuses() {
        if (paymentStatusRepository.count() == 0) {
            createPaymentStatusIfNotExists("active", "Active", "Payment is Active.");
            createPaymentStatusIfNotExists("inactive", "Inactive", "Payment is Inactive.");
        } else {
            log.info("PaymentStatus table is not empty. Skipping seeding.");
        }
    }

    private void createPaymentStatusIfNotExists(String code, String name, String description) {
        if (paymentStatusRepository.findByCode(code).isEmpty() && paymentStatusRepository.findByName(name).isEmpty()) {
            PaymentStatus status = PaymentStatus.builder()
                .code(code)
                .name(name)
                .description(description)
                .build();
            paymentStatusRepository.save(status);
            log.info("Seeded PaymentStatus: {}", name);
        }
    }

    private void seedTokenTransactionTypes() {
        if (tokenTransactionTypeRepository.findByName("WITHDRAWAL").isEmpty()) {
            TokenTransactionType withdrawalType = TokenTransactionType.builder()
                    .name("WITHDRAWAL")
                    .description("Withdrawal transaction type")
                    .affectsBalance(true)
                    .multiplier(1)
                    .createdAt(java.time.LocalDate.now())
                    .updatedAt(java.time.LocalDate.now())
                    .deleted(false)
                    .build();
            tokenTransactionTypeRepository.save(withdrawalType);
            log.info("Seeded TokenTransactionType: WITHDRAWAL");

            if (tokenTransactionTypeRepository.findByName("EXAM_PAYMENT").isEmpty()) {
                TokenTransactionType examPayment = TokenTransactionType.builder()
                        .name("EXAM_PAYMENT")
                        .description("Describe the action of students having their tokens deducted when taking the exam")
                        .affectsBalance(true)
                        .multiplier(1)
                        .createdAt(java.time.LocalDate.now())
                        .updatedAt(java.time.LocalDate.now())
                        .deleted(false)
                        .build();
                tokenTransactionTypeRepository.save(examPayment);
                log.info("Seeded TokenTransactionType: EXAM_PAYMENT");
            }
        }

        if (tokenTransactionTypeRepository.findByName("INCOME_SHARE").isEmpty()) {
            TokenTransactionType incomeShare = TokenTransactionType.builder()
                    .name("INCOME_SHARE")
                    .description("Describe the action of the teacher receiving the token when the student takes the exam.")
                    .affectsBalance(true)
                    .multiplier(1)
                    .createdAt(java.time.LocalDate.now())
                    .updatedAt(java.time.LocalDate.now())
                    .deleted(false)
                    .build();
            tokenTransactionTypeRepository.save(incomeShare);
            log.info("Seeded TokenTransactionType: INCOME_SHARE");
        }
    }

    /**
     * Seeds sample QuestionTopicV2 data for demonstration or testing.
     */

    @Transactional
    public void createAPQuestionTopicsV2() {
        User teacher = userRepository.findByEmail("teacher@gmail.com")
                .orElseThrow(() -> new RuntimeException("User 'admin123@gmail.com' not found. Please ensure this user exists."));

        // Create AP subjects as subjects, not topics
        Subject apCalcAbSubject = subjectRepository.findByNameIgnoreCase("AP Calculus AB")
                .orElseGet(() -> subjectRepository.save(
                        Subject.builder().name("AP Calculus AB").code("APCALCAB").build()
                ));
        Subject apCalcBcSubject = subjectRepository.findByNameIgnoreCase("AP Calculus BC")
                .orElseGet(() -> subjectRepository.save(
                        Subject.builder().name("AP Calculus BC").code("APCALCBC").build()
                ));
        Subject apStatsSubject = subjectRepository.findByNameIgnoreCase("AP Statistics")
                .orElseGet(() -> subjectRepository.save(
                        Subject.builder().name("AP Statistics").code("APSTATS").build()
                ));
        Subject apEngLangSubject = subjectRepository.findByNameIgnoreCase("AP English Language")
                .orElseGet(() -> subjectRepository.save(
                        Subject.builder().name("AP English Language").code("APENGLANG").build()
                ));
        // Add topics for these AP subjects
        // AP Calculus AB
        createTopicWithDescription("Limits and Continuity", "Limits, continuity, and introduction to calculus", apCalcAbSubject, teacher);
        createTopicWithDescription("Differentiation: Definition and Basic Rules", "Basic differentiation rules and concepts", apCalcAbSubject, teacher);
        createTopicWithDescription("Differentiation: Composite, Implicit, and Inverse Functions", "Advanced differentiation techniques", apCalcAbSubject, teacher);
        createTopicWithDescription("Applications of Derivatives", "Motion, related rates, optimization, graph analysis", apCalcAbSubject, teacher);
        createTopicWithDescription("Integration and Accumulation of Change", "Antiderivatives, definite integrals", apCalcAbSubject, teacher);
        createTopicWithDescription("Differential Equations", "Slope fields, separable DEs, exponential models", apCalcAbSubject, teacher);
        createTopicWithDescription("Applications of Integration", "Areas, volumes, average value", apCalcAbSubject, teacher);
        // AP Calculus BC
        createTopicWithDescription("Parametric, Polar, and Vector Functions", "Parametric equations, polar coordinates, vectors", apCalcBcSubject, teacher);
        createTopicWithDescription("Infinite Sequences and Series", "Convergence tests, Taylor series", apCalcBcSubject, teacher);
        // AP Statistics
        createTopicWithDescription("Exploring One-Variable Data", "Graphs, center, spread, shape", apStatsSubject, teacher);
        createTopicWithDescription("Exploring Two-Variable Data", "Scatterplots, correlation, regression", apStatsSubject, teacher);
        createTopicWithDescription("Collecting Data", "Surveys, experiments, sampling, bias", apStatsSubject, teacher);
        createTopicWithDescription("Probability, Random Variables, and Probability Distributions", "Probability theory and distributions", apStatsSubject, teacher);
        createTopicWithDescription("Sampling Distributions", "Sampling and the Central Limit Theorem", apStatsSubject, teacher);
        createTopicWithDescription("Inference for Categorical Data: Proportions", "Proportion inference", apStatsSubject, teacher);
        createTopicWithDescription("Inference for Quantitative Data: Means", "Mean inference", apStatsSubject, teacher);
        createTopicWithDescription("Inference for Categorical Data: Chi-Square", "Chi-square tests", apStatsSubject, teacher);
        createTopicWithDescription("Inference for Quantitative Data: Slopes", "Linear regression inference", apStatsSubject, teacher);
        // AP English Language
        createTopicWithDescription("Rhetorical Situation", "Audience, purpose, context", apEngLangSubject, teacher);
        createTopicWithDescription("Claims and Evidence", "Finding and supporting arguments", apEngLangSubject, teacher);
        createTopicWithDescription("Reasoning and Organization", "Line of reasoning, structure of an argument", apEngLangSubject, teacher);
        createTopicWithDescription("Style and Tone", "Diction, syntax, rhetorical devices", apEngLangSubject, teacher);
        createTopicWithDescription("Visual and Quantitative Texts", "Charts, images, graphics as arguments", apEngLangSubject, teacher);
        createTopicWithDescription("Synthesis and Research", "Using multiple sources in one argument", apEngLangSubject, teacher);
        createTopicWithDescription("Argumentation", "Writing your own argumentative essays", apEngLangSubject, teacher);
        createTopicWithDescription("Rhetorical Analysis", "Analyzing how writers use language", apEngLangSubject, teacher);
        createTopicWithDescription("Exam Review and Practice", "Timed writing, MC practice, FRQ practice", apEngLangSubject, teacher);
        log.info("Seeded AP QuestionTopicV2 data.");
    }

}
