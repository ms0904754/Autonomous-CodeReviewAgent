package com.mayank.code_review_agent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mayank.code_review_agent.model.ReviewLog;
import com.mayank.code_review_agent.repository.ReviewLogRepository;
import com.mayank.code_review_agent.service.AiReviewAgent;
import com.mayank.code_review_agent.service.GithubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final GithubService githubService;
    private final AiReviewAgent aiReviewAgent;
    private final ReviewLogRepository reviewLogRepository;

    public WebhookController(GithubService githubService, 
                             AiReviewAgent aiReviewAgent, 
                             ReviewLogRepository reviewLogRepository) {
        this.githubService = githubService;
        this.aiReviewAgent = aiReviewAgent;
        this.reviewLogRepository = reviewLogRepository;
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGithubWebhook(
            @RequestBody JsonNode payload, 
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "UNKNOWN") String event) {
        
        log.info("Received GitHub event: type={}", event);

        if (!isPullRequestEvent(event)) {
            log.debug("Ignored non-pull_request event");
            return ResponseEntity.ok("Ignored non-PR event");
        }

        String action = payload.path("action").asText();
        if (!isActionSupported(action)) {
            log.debug("Ignored unsupported PR action: action={}", action);
            return ResponseEntity.ok("Action ignored");
        }

        processPullRequest(payload, action);
        return ResponseEntity.ok("Review completed and posted.");
    }

    private boolean isPullRequestEvent(String event) {
        return "pull_request".equals(event);
    }

    private boolean isActionSupported(String action) {
        return "opened".equals(action) || "synchronize".equals(action);
    }

    private void processPullRequest(JsonNode payload, String action) {
        String repoFullName = payload.path("repository").path("full_name").asText();
        int prNumber = payload.path("pull_request").path("number").asInt();

        log.info("Processing pull request: repo={}, prNumber={}, action={}", repoFullName, prNumber, action);

        try {
            // 1. Fetch code changes
            String diff = githubService.getPullRequestDiff(repoFullName, prNumber);

            // 2. Generate AI review
            String reviewComment = aiReviewAgent.analyzeDiff(diff);
            
            // 3. Post review back to GitHub
            String formattedComment = "🤖 **Autonomous Review:**\n\n" + reviewComment;
            githubService.postReviewComment(repoFullName, prNumber, formattedComment);

            // 4. Persist log to database
            saveReviewLog(repoFullName, prNumber, reviewComment);

            log.info("Successfully completed review pipeline for repo={}, pr={}", repoFullName, prNumber);

        } catch (Exception e) {
            log.error("Failed to process code review pipeline for repo={}, pr={}", repoFullName, prNumber, e);
            throw new RuntimeException("Error processing code review", e);
        }
    }

    private void saveReviewLog(String repoFullName, int prNumber, String reviewSummary) {
        ReviewLog reviewLog = new ReviewLog();
        reviewLog.setRepositoryName(repoFullName);
        reviewLog.setPullRequestNumber(prNumber);
        reviewLog.setReviewSummary(reviewSummary);
        reviewLogRepository.save(reviewLog);
    }
}