package com.testplatform.service;

import com.testplatform.model.TestSuite;

import java.util.List;
import java.util.Optional;

public interface TestSuiteService {
    List<TestSuite> getAllTestSuites();
    Optional<TestSuite> getTestSuiteById(String id);
    TestSuite createTestSuite(TestSuite testSuite);
    TestSuite updateTestSuite(String id, TestSuite testSuite);
    void deleteTestSuite(String id);
    List<TestSuite> getTestSuitesByType(TestSuite.TestSuiteType type);
}