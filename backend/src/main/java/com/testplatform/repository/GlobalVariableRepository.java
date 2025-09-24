package com.testplatform.repository;

import com.testplatform.model.GlobalVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalVariableRepository extends JpaRepository<GlobalVariable, String> {
    Optional<GlobalVariable> findByNameAndEnvironmentId(String name, String environmentId);
    List<GlobalVariable> findByEnvironmentId(String environmentId);
    List<GlobalVariable> findByNameContainingIgnoreCase(String name);
}
