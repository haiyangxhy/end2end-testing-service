package com.testplatform.repository;

import com.testplatform.model.GlobalVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariableRepository extends JpaRepository<GlobalVariable, String> {
    
    /**
     * 根据环境ID查找所有变量
     */
    List<GlobalVariable> findByEnvironmentId(String environmentId);
    
    /**
     * 根据环境ID和变量名查找变量
     */
    GlobalVariable findByEnvironmentIdAndName(String environmentId, String name);
    
    /**
     * 根据环境ID删除所有变量
     */
    void deleteByEnvironmentId(String environmentId);
    
    /**
     * 根据变量类型查找变量
     */
    List<GlobalVariable> findByVariableType(String variableType);
    
    /**
     * 根据环境ID和变量类型查找变量
     */
    List<GlobalVariable> findByEnvironmentIdAndVariableType(String environmentId, String variableType);
    
    /**
     * 检查变量名是否已存在
     */
    boolean existsByEnvironmentIdAndName(String environmentId, String name);
}
