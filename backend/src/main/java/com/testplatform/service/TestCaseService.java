package com.testplatform.service;

import com.testplatform.model.TestCase;

import java.util.List;
import java.util.Optional;

public interface TestCaseService {
    List<TestCase> getAllTestCases();
    Optional<TestCase> getTestCaseById(String id);
    List<TestCase> getTestCasesBySuiteId(String suiteId);
    TestCase createTestCase(TestCase testCase);
    TestCase updateTestCase(String id, TestCase testCase);
    void deleteTestCase(String id);
    List<TestCase> getTestCasesByType(TestCase.TestCaseType type);
}