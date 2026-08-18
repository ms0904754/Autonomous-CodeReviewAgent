package com.mayank.code_review_agent.service;

import com.mayank.code_review_agent.model.ReviewLog;
import com.mayank.code_review_agent.repository.ReviewLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CodeReviewProcessorService {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewProcessorService.class);

    private final GithubService githubService;
    private final AiReviewAgent aiReviewAgent;
    private final ReviewLogRepository reviewLogRepository;

    public CodeReviewProcessorService(GithubService githubService, 
                                      AiReviewAgent aiReviewAgent, 
                                      ReviewLogRepository reviewLogRepository) {
        this.githubService = githubService;
        this.aiReviewAgent = aiReviewAgent;
        this.reviewLogRepository = reviewLogRepository;
    }

    @Async
    public void processPullRequestAsync(String repoFullName, int prNumber, String action) {
        log.info("Background thread started processing PR: repo={}, prNumber={}", repoFullName, prNumber);

        try {
            // Fetch code changes from GitHub
            String diff = githubService.getPullRequestDiff(repoFullName, prNumber);

            // Pass diff to Gemini AI for analysis
            String reviewComment = aiReviewAgent.analyzeDiff(diff);
            
            // Post formatted review back to GitHub PR timeline
            String formattedComment = "🤖 **Autonomous Review:**\n\n" + reviewComment;
            githubService.postReviewComment(repoFullName, prNumber, formattedComment);

            // Save review transaction to Neon PostgreSQL
            ReviewLog reviewLog = new ReviewLog();
            reviewLog.setRepositoryName(repoFullName);
            reviewLog.setPullRequestNumber(prNumber);
            reviewLog.setReviewSummary(reviewComment);
            reviewLogRepository.save(reviewLog);

            log.info("Background thread successfully completed review pipeline for repo={}, pr={}", repoFullName, prNumber);

        } catch (Exception e) {
            log.error("Background task failed for repo={}, pr={}", repoFullName, prNumber, e);
        }
    }
}