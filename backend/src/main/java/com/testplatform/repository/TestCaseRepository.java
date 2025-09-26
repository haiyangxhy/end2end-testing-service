package com.testplatform.repository;

import com.testplatform.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, String> {
    // 移除按类型查询方法，测试用例不再有类型字段
    List<TestCase> findByPriority(TestCase.Priority priority);
    List<TestCase> findByStatus(TestCase.Status status);
    List<TestCase> findByIsActive(Boolean isActive);
    // 移除按类型和激活状态查询方法，测试用例不再有类型字段
    List<TestCase> findByPriorityAndIsActive(TestCase.Priority priority, Boolean isActive);
}