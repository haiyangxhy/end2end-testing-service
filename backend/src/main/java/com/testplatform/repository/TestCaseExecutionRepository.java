package com.testplatform.repository;

import com.testplatform.model.TestCaseExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TestCaseExecutionRepository extends JpaRepository<TestCaseExecution, String> {
    
    /**
     * 根据测试用例ID查找所有执行记录
     */
    List<TestCaseExecution> findByTestCaseId(String testCaseId);
    
    /**
     * 根据执行ID查找所有测试用例执行记录
     */
    List<TestCaseExecution> findByExecutionId(String executionId);
    
    /**
     * 根据测试用例ID删除所有执行记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TestCaseExecution tce WHERE tce.testCaseId = :testCaseId")
    void deleteByTestCaseId(@Param("testCaseId") String testCaseId);
    
    /**
     * 根据执行ID删除所有测试用例执行记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TestCaseExecution tce WHERE tce.executionId = :executionId")
    void deleteByExecutionId(@Param("executionId") String executionId);
    
    /**
     * 根据状态查找测试用例执行记录
     */
    List<TestCaseExecution> findByStatus(TestCaseExecution.ExecutionStatus status);
}
