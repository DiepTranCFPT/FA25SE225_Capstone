package com.fa25se225.capstone.configuration;

import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.mapper.UserMapper;
import com.fa25se225.capstone.service.implementation.SseService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AITool {

    public final SseService sseService;
    public final UserMapper userMapper;
    public final AccountUtil accountUtil;



    @Tool(
            description = """
    Sends the confirmed user information to the client via Server-Sent Events (SSE).
    
    This tool MUST ONLY be called after the user has explicitly confirmed
    that the parsed user data should be finalized and sent as a UserResponse.
    This tool should be called ONLY when the user explicitly says:
    - "i confirm to parse user to user response"
    
    Do NOT call this tool if:
    - The user is still reviewing the parsed data
    - The user has not clearly confirmed (e.g. "yes", "confirm", "ok")
    
    This tool produces a side effect and does not return any value.
    """
    )
    void userResponseParse() {

        User user = accountUtil.getCurrentUser();
        System.out.println("///////////////////////////////////////////////////");
        sseService.sendSSe(
                user.getId(),
                userMapper.toResponse(user),
                "Map"
        );
    }
}
