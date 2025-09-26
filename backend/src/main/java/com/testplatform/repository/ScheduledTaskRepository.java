package com.testplatform.repository;

import com.testplatform.model.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, String> {
    
    /**
     * 根据套件ID查找定时任务
     */
    List<ScheduledTask> findBySuiteId(String suiteId);
    
    /**
     * 根据环境ID查找定时任务
     */
    List<ScheduledTask> findByEnvironmentId(String environmentId);
    
    /**
     * 查找活跃的定时任务
     */
    List<ScheduledTask> findByIsActiveTrue();
    
    /**
     * 查找需要执行的定时任务（下次执行时间已到且为活跃状态）
     */
    @Query("SELECT st FROM ScheduledTask st WHERE st.isActive = true AND st.nextRun <= :currentTime")
    List<ScheduledTask> findTasksToExecute(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 根据套件ID删除定时任务
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduledTask st WHERE st.suiteId = :suiteId")
    void deleteBySuiteId(@Param("suiteId") String suiteId);
    
    /**
     * 根据环境ID删除定时任务
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduledTask st WHERE st.environmentId = :environmentId")
    void deleteByEnvironmentId(@Param("environmentId") String environmentId);
    
    /**
     * 更新任务的下次执行时间
     */
    @Modifying
    @Transactional
    @Query("UPDATE ScheduledTask st SET st.nextRun = :nextRun WHERE st.id = :id")
    void updateNextRun(@Param("id") String id, @Param("nextRun") LocalDateTime nextRun);
    
    /**
     * 更新任务的最后执行时间
     */
    @Modifying
    @Transactional
    @Query("UPDATE ScheduledTask st SET st.lastRun = :lastRun WHERE st.id = :id")
    void updateLastRun(@Param("id") String id, @Param("lastRun") LocalDateTime lastRun);
}
