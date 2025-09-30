package com.testplatform.repository;

import com.testplatform.model.TestExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestExecutionLogRepository extends JpaRepository<TestExecutionLog, String> {
    
    /**
     * 根据执行ID查找所有日志
     */
    List<TestExecutionLog> findByExecutionIdOrderByTimestampAsc(String executionId);
    
    /**
     * 根据执行ID和测试用例ID查找日志
     */
    List<TestExecutionLog> findByExecutionIdAndTestCaseIdOrderByTimestampAsc(String executionId, String testCaseId);
    
    /**
     * 根据执行ID和日志级别查找日志
     */
    List<TestExecutionLog> findByExecutionIdAndLevelOrderByTimestampAsc(String executionId, String level);
    
    /**
     * 根据执行ID删除所有日志
     */
    void deleteByExecutionId(String executionId);
    
    /**
     * 根据执行ID查找最新的日志
     */
    @Query("SELECT l FROM TestExecutionLog l WHERE l.executionId = :executionId ORDER BY l.timestamp DESC")
    List<TestExecutionLog> findLatestByExecutionId(@Param("executionId") String executionId);
}
