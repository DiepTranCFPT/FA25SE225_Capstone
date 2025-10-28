package com.fa25se225.capstone.service;

import com.fa25se225.capstone.entity.ConversationAI;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.ConversationAIRepository;
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
    @Autowired(required = false)
    private VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;

    public GeminiService(ConversationAIRepository aiRepository, AccountUtil accountUtil, ChatClient.Builder chatClient, UserRepository userRepository) {
        this.aiRepository = aiRepository;
        this.accountUtil = accountUtil;
        this.chatClient = chatClient.build();
        this.userRepository = userRepository;
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

    public String chat(String message){
        User user = accountUtil.getCurrentUser();
        String system = String.format("%sKhi đầu là Chào %s", promt, user.getLastName());

        SystemMessage systemMessage = new SystemMessage(system);
        UserMessage userMessage = new UserMessage(message);

        Prompt prom = new Prompt(systemMessage, userMessage);
        String response = chatClient.prompt(prom)
                .call()
                .content();
        ConversationAI conversationAI = new ConversationAI(message,user,response);
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

    private void upsert(String text) {
        if (vectorStore == null) return;
        Document doc = new Document(text);
        vectorStore.add(List.of(doc));
    }

    private void upsertMany(List<String> texts) {
        if (vectorStore == null) return;
        List<Document> docs = texts.stream()
                .map(Document::new)
                .toList();
        vectorStore.add(docs);
    }


}
