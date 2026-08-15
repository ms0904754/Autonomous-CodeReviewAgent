package com.mayank.code_review_agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mayank.code_review_agent.model.ReviewLog;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
}