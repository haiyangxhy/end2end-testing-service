package com.testplatform.repository;

import com.testplatform.model.TestReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestReportRepository extends MongoRepository<TestReport, String> {
    List<TestReport> findBySuiteId(String suiteId);
    List<TestReport> findByExecutionId(String executionId);
}