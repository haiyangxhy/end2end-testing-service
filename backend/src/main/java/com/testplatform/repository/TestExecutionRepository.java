package com.testplatform.repository;

import com.testplatform.model.TestExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, String> {
    List<TestExecution> findBySuiteId(String suiteId);
    List<TestExecution> findByStatus(TestExecution.ExecutionStatus status);
    
    /**
     * 根据套件ID删除所有测试执行记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TestExecution te WHERE te.suiteId = :suiteId")
    void deleteBySuiteId(@Param("suiteId") String suiteId);
}