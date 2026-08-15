package com.mayank.code_review_agent.model;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Data;

@Entity
@Data
public class ReviewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String RepositoryName;
    private Integer PullRequestNumber;
    
    @Column(columnDefinition = "TEXT")
    private String ReviewSummary;
    
    private LocalDateTime ReviewedAt = LocalDateTime.now();

}
