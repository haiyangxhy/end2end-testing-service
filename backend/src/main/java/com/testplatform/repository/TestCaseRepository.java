package com.testplatform.repository;

import com.testplatform.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, String> {
    List<TestCase> findBySuiteId(String suiteId);
    List<TestCase> findByType(TestCase.TestCaseType type);
}