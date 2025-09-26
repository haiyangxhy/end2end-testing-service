package com.testplatform.service;

import com.testplatform.model.TestCase;

import java.util.List;
import java.util.Optional;

public interface TestCaseService {
    List<TestCase> getAllTestCases();
    Optional<TestCase> getTestCaseById(String id);
    TestCase createTestCase(TestCase testCase);
    TestCase updateTestCase(String id, TestCase testCase);
    void deleteTestCase(String id);
    // 移除按类型查询方法，测试用例不再有类型字段
    List<TestCase> getTestCasesByPriority(TestCase.Priority priority);
    List<TestCase> getTestCasesByStatus(TestCase.Status status);
    List<TestCase> getActiveTestCases();
    // 移除按类型和激活状态查询方法，测试用例不再有类型字段
}