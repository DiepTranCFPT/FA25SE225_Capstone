package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.TeacherAiJson;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.entity.ConversationAI;
import com.fa25se225.capstone.entity.LearningMaterial;
import com.fa25se225.capstone.entity.Lesson;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.ConversationAIRepository;
import com.fa25se225.capstone.repository.LearningMaterialRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Service
public class GeminiService {
    Logger log = LoggerFactory.getLogger(GeminiService.class);

    String promt = "Bạn là một gia sư AI có tên là \"AP Certificate\", một người bạn đồng hành học tập cực kỳ thân thiện, kiên nhẫn và thấu hiểu. Vai trò của bạn là giúp người dùng hiểu sâu các khái niệm chứ không chỉ đơn thuần là cung cấp câu trả lời.\n" +
            "\n" +
            "**Nhiệm vụ chính của bạn:**\n" +
            "1.  **Giải thích sâu:** Khi người dùng hỏi về một chủ đề, hãy giải thích nó một cách đơn giản, rõ ràng, sử dụng các ví dụ và phép loại suy từ đời thực để họ dễ hình dung.\n" +
            "2.  **Hướng dẫn từng bước:** Đối với các bài toán hoặc câu hỏi phức tạp, đừng đưa ra đáp án cuối cùng ngay lập tức. Thay vào đó, hãy chia nhỏ vấn đề và hướng dẫn người dùng giải quyết từng bước một.\n" +
            "3.  **Đặt câu hỏi gợi mở:** Luôn khuyến khích tư duy phản biện bằng cách đặt các câu hỏi như \"Tại sao bạn nghĩ vậy?\", \"Bạn có thể giải thích cách bạn đi đến kết luận đó không?\", \"Còn cách nào khác để giải quyết vấn đề này không?\".\n" +
            "4.  **Kiểm tra sự thấu hiểu:** Sau khi giải thích một khái niệm, hãy chủ động hỏi lại \"Bạn đã hiểu phần này chưa?\" hoặc đưa ra một câu hỏi nhỏ để kiểm tra xem người dùng đã thực sự nắm bắt được kiến thức hay chưa.\n" +
            "5.  **Tạo câu hỏi luyện tập:** Dựa trên chủ đề đang thảo luận, hãy tạo ra các câu hỏi trắc nghiệm hoặc bài tập nhỏ để người dùng củng cố kiến thức.\n" +
            "\n" +
            "**Quy tắc ứng xử:**\n" +
            "* **Luôn tích cực và khuyến khích:** Sử dụng ngôn ngữ động viên, ví dụ: \"Làm tốt lắm!\", \"Đó là một câu hỏi rất hay!\", \"Đừng lo, chúng ta sẽ cùng nhau tìm ra câu trả lời.\"\n" +
            "* **Không bao giờ chê bai:** Nếu người dùng trả lời sai, hãy nhẹ nhàng chỉ ra lỗi và giải thích lại khái niệm một cách kiên nhẫn.\n" +
            "* **Thừa nhận khi không biết:** Nếu bạn không chắc chắn về một câu trả lời, hãy nói thật thay vì bịa đặt thông tin.\n" +
            "* **Ngắn gọn, đi vào trọng tâm:** Cố gắng trình bày các ý tưởng một cách súc tích. Sử dụng gạch đầu dòng, in đậm để làm nổi bật các ý chính.\n" +
            "\n" +
            "**Giọng văn:** Thân thiện, gần gũi, kiên nhẫn và đầy động lực.";

    private final ConversationAIRepository aiRepository;
    private final AccountUtil accountUtil;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final LearningMaterialRepository learningMaterialRepository;

    public GeminiService(ConversationAIRepository aiRepository, AccountUtil accountUtil, ChatClient.Builder chatClient, UserRepository userRepository, LearningMaterialRepository learningMaterialRepository) {
        this.aiRepository = aiRepository;
        this.accountUtil = accountUtil;
        this.chatClient = chatClient.build();
        this.userRepository = userRepository;
        this.learningMaterialRepository = learningMaterialRepository;
    }

//    public Question ask(String question) throws Exception {
//        var hits = vectorStore.similaritySearch(
//                SearchRequest.builder()
//                        .query(question)
//                        .topK(5)
//                        .similarityThreshold(0.6)
//                        .build()
//        );
//        String context = hits.stream()
//                .map(Document::getText)
//                .collect(Collectors.joining("\n---\n"));
//
//        String jsonResponse = chatClient.prompt()
//                .system("Bạn là AI, trả lời bằng JSON hợp lệ theo schema: {\"answer\": string, \"confidence\": number}")
//                .user("Question: " + question + "\nContext:\n" + context)
//                .call()
//                .content();
//
//        return objectMapper.readValue(jsonResponse, Question.class);
//    }

    public String chat(String message) {
        User user = accountUtil.getCurrentUser();

        String system = buildSystemPrompt(user);

        SystemMessage systemMessage = new SystemMessage(system);
        UserMessage userMessage = new UserMessage(message);

        Prompt prom = new Prompt(systemMessage, userMessage);

        String response = chatClient.prompt(prom)
                .call()
                .content();

        ConversationAI conversationAI = new ConversationAI(message, user, response);
        try {
            if (aiRepository != null) {
                aiRepository.save(conversationAI);
            }
            userRepository.save(user);
        } catch (Exception ex) {
            log.warn("Failed to persist conversation: {}", ex.getMessage());
        }
        log.debug("Conversation AI : {}", conversationAI);
        return response;
    }

    private String buildSystemPrompt(User user) {
        StringBuilder sb = new StringBuilder();

        sb.append("At the beginning of the conversation, you MUST greet the user with: ")
                .append("\"Hello ").append(user.getLastName()).append("\".\n\n");

        // Your base prompt (existing variable)
        sb.append(promt).append("\n\n");

        sb.append(buildLearningMaterialsContext());

        sb.append("\n\n")
                .append("If the user asks about a course that is not in the list above, ")
                .append("reply that the system currently does not offer that course.\n")
                .append("Only use information about the courses listed above when answering ")
                .append("questions related to courses.\n");

        return sb.toString();
    }

    private String buildLearningMaterialsContext() {
        List<LearningMaterial> materials = getLearningMaterials();

        if (materials == null || materials.isEmpty()) {
            return "At the moment, the system does not have any courses.\n";
        }

        StringBuilder sb = new StringBuilder("My system has the following courses:\n");

        for (LearningMaterial m : materials) {
            sb.append("- Course name: ").append(m.getTitle()).append("\n");

            if (m.getSubject() != null) {
                sb.append("  | Subject: ").append(m.getSubject());
            }
            if (m.getDescription() != null) {
                sb.append("  | Description: ").append(m.getDescription());
            }
            sb.append("\n");

            // Add lesson info
            if (m.getLessons() != null && !m.getLessons().isEmpty()) {
                sb.append("  | Lessons:\n");
                for (Lesson lesson : m.getLessons()) {
                    sb.append("    + Lesson: ").append(lesson.getName());
                    if (lesson.getDescription() != null && !lesson.getDescription().isEmpty()) {
                        sb.append(" | Description: ").append(lesson.getDescription());
                    }
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    public TeacherAiJson reviewTeacherProfileForVerification(TeacherProfileResponse profile) {
        String system = """
                You are an AP teacher quality reviewer.
                Output ONLY valid JSON. No extra text.
                Language: English.
                """;

        String user = """
                Evaluate the following teacher profile using 4 criteria scored from 1 to 5:
                - syllabusAlignment: fit for teaching AP based on qualification/specialization
                - conceptAccuracy: knowledge credibility based on qualification + certificates
                - difficultyFit: ability to design appropriate AP-level practice based on experience
                - explanationQuality: ability to explain clearly based on biography + experience
                
                The field "recommendation" must be exactly one of:
                "Qualified" | "Partially qualified" | "Not qualified"
                
                Return valid JSON exactly in this schema:
                {
                  "syllabusAlignment": 1,
                  "conceptAccuracy": 1,
                  "difficultyFit": 1,
                  "explanationQuality": 1,
                  "recommendation": "Qualified|Partially qualified|Not qualified",
                  "feedback": "short bullet-like feedback"
                }
                
                TeacherProfile JSON:
                %s
                """.formatted(safeJson(profile));

        Prompt prompt = new Prompt(new SystemMessage(system), new UserMessage(user));
        String raw = chatClient.prompt(prompt).call().content();

        return parseTeacherAiJson(raw);
    }


    private String safeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private TeacherAiJson parseTeacherAiJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalArgumentException("AI response missing JSON object");
        }
        String json = raw.substring(start, end + 1);
        try {
            return objectMapper.readValue(json, TeacherAiJson.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse AI JSON: " + e.getMessage(), e);
        }
    }

    private List<LearningMaterial> getLearningMaterials() {
        return learningMaterialRepository.findAll();
    }


}
