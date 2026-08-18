package com.mayank.code_review_agent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mayank.code_review_agent.service.CodeReviewProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final CodeReviewProcessorService reviewProcessorService;

    public WebhookController(CodeReviewProcessorService reviewProcessorService) {
        this.reviewProcessorService = reviewProcessorService;
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGithubWebhook(
            @RequestBody JsonNode payload, 
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "UNKNOWN") String event) {
        
        log.info("Received GitHub event: type={}", event);

        if (!"pull_request".equals(event)) {
            return ResponseEntity.ok("Ignored non-PR event");
        }

        String action = payload.path("action").asText();
        if (!"opened".equals(action) && !"synchronize".equals(action)) {
            return ResponseEntity.ok("Action ignored");
        }

        String repoFullName = payload.path("repository").path("full_name").asText();
        int prNumber = payload.path("pull_request").path("number").asInt();

        log.info("Triggering asynchronous review pipeline for repo={}, prNumber={}", repoFullName, prNumber);

        // Instantly triggers background thread and frees up controller thread
        reviewProcessorService.processPullRequestAsync(repoFullName, prNumber, action);

        // GitHub gets its 200 OK response instantly (in ~5ms instead of waiting 10 seconds for AI)
        return ResponseEntity.ok("Webhook received and processing asynchronously.");
    }
}