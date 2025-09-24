package com.testplatform.repository;

import com.testplatform.model.TestEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestEnvironmentRepository extends JpaRepository<TestEnvironment, String> {
    Optional<TestEnvironment> findByName(String name);
    Optional<TestEnvironment> findByIsActiveTrue();
    List<TestEnvironment> findByIsActiveTrueOrderByCreatedAtDesc();
}
