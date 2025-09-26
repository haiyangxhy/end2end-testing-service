package com.testplatform.repository;

import com.testplatform.model.TestSuiteCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TestSuiteCaseRepository extends JpaRepository<TestSuiteCase, String> {
    
    /**
     * 根据套件ID查找所有关联的测试用例
     */
    List<TestSuiteCase> findBySuiteIdOrderByExecutionOrder(String suiteId);
    
    /**
     * 根据套件ID删除所有关联的测试用例
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TestSuiteCase tsc WHERE tsc.suiteId = :suiteId")
    void deleteBySuiteId(@Param("suiteId") String suiteId);
    
    /**
     * 根据套件ID和测试用例ID查找关联记录
     */
    TestSuiteCase findBySuiteIdAndTestCaseId(String suiteId, String testCaseId);
    
    /**
     * 根据测试用例ID删除所有关联记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TestSuiteCase tsc WHERE tsc.testCaseId = :testCaseId")
    void deleteByTestCaseId(@Param("testCaseId") String testCaseId);
}
