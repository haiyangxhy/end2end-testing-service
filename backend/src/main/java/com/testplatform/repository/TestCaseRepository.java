package com.testplatform.repository;

import com.testplatform.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, String> {
    List<TestCase> findByType(TestCase.TestCaseType type);
    List<TestCase> findByPriority(TestCase.Priority priority);
    List<TestCase> findByStatus(TestCase.Status status);
    List<TestCase> findByIsActive(Boolean isActive);
    List<TestCase> findByTypeAndIsActive(TestCase.TestCaseType type, Boolean isActive);
    List<TestCase> findByPriorityAndIsActive(TestCase.Priority priority, Boolean isActive);
}