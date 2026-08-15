package com.mayank.code_review_agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GithubService {

    private static final Logger log = LoggerFactory.getLogger(GithubService.class);

    private final RestClient restClient;

    public GithubService(@Value("${github.api.token}") String githubToken) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public String getPullRequestDiff(String repoFullName, int prNumber) {
        String targetUri = String.format("/repos/%s/pulls/%d", repoFullName, prNumber);
        log.info("Fetching pull request diff from GitHub API: uri={}", targetUri);

        return restClient.get()
                .uri(targetUri)
                .accept(MediaType.valueOf("application/vnd.github.v3.diff"))
                .retrieve()
                .body(String.class);
    }

    public void postReviewComment(String repoFullName, int prNumber, String commentBody) {
        String commentUri = String.format("/repos/%s/issues/%d/comments", repoFullName, prNumber);
        log.info("Posting review comment to GitHub issue timeline: uri={}", commentUri);

        restClient.post()
                .uri(commentUri)
                .body(Map.of("body", commentBody))
                .retrieve()
                .toBodilessEntity();
    }
}