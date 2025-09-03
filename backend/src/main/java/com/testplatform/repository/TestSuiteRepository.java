package com.testplatform.repository;

import com.testplatform.model.TestSuite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestSuiteRepository extends JpaRepository<TestSuite, String> {
    List<TestSuite> findByType(TestSuite.TestSuiteType type);
}