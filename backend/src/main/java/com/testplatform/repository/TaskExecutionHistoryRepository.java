package com.testplatform.repository;

import com.testplatform.model.TaskExecutionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskExecutionHistoryRepository extends JpaRepository<TaskExecutionHistory, String> {
    
    /**
     * 根据执行ID检查是否存在历史记录
     */
    @Query("SELECT COUNT(t) > 0 FROM TaskExecutionHistory t WHERE t.executionId = :executionId")
    boolean existsByExecutionId(@Param("executionId") String executionId);
    
    /**
     * 根据执行ID列表检查是否存在历史记录
     */
    @Query("SELECT COUNT(t) > 0 FROM TaskExecutionHistory t WHERE t.executionId IN :executionIds")
    boolean existsByExecutionIdIn(@Param("executionIds") List<String> executionIds);
}
