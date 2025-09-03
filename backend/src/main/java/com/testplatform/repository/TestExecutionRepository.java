package com.testplatform.repository;

import com.testplatform.model.TestExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, String> {
    List<TestExecution> findBySuiteId(String suiteId);
    List<TestExecution> findByStatus(TestExecution.ExecutionStatus status);
}