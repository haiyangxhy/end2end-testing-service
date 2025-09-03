package com.testplatform.service;

import com.testplatform.model.TestReport;

import java.util.List;
import java.util.Optional;

public interface TestReportService {
    List<TestReport> getAllReports();
    Optional<TestReport> getReportById(String id);
    List<TestReport> getReportsBySuiteId(String suiteId);
    List<TestReport> getReportsByExecutionId(String executionId);
    TestReport createReport(TestReport report);
    TestReport updateReport(String id, TestReport report);
    void deleteReport(String id);
}