package com.testplatform.repository;

import com.testplatform.model.TargetSystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TargetSystemConfigRepository extends JpaRepository<TargetSystemConfig, String> {
    List<TargetSystemConfig> findByIsActiveTrue();
}