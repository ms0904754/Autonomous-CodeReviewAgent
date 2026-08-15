package com.mayank.code_review_agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiReviewAgent {

    private final ChatClient chatClient;

    public AiReviewAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a strict, senior backend engineer. Review the provided Git diff. Focus on time/space complexity, security vulnerabilities, and code smells. Provide your review in clear Markdown.")
                .build();
    }

    public String analyzeDiff(String codeDiff) {
        if (codeDiff == null || codeDiff.trim().isEmpty()) {
            return "No changes found to review.";
        }
        return chatClient.prompt()
                .user("Please review the following code diff:\n\n" + codeDiff)
                .call()
                .content();
    }
}