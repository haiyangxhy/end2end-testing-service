package com.testplatform.controller;

import com.testplatform.model.TestExecution;
import com.testplatform.service.TestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/executions")
@CrossOrigin(origins = "*")
public class TestExecutionController {
    
    @Autowired
    private TestExecutionService testExecutionService;
    
    @GetMapping
    public ResponseEntity<List<TestExecution>> getAllExecutions() {
        List<TestExecution> executions = testExecutionService.getAllExecutions();
        return new ResponseEntity<>(executions, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestExecution> getExecutionById(@PathVariable String id) {
        Optional<TestExecution> execution = testExecutionService.getExecutionById(id);
        if (execution.isPresent()) {
            return new ResponseEntity<>(execution.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/suite/{suiteId}")
    public ResponseEntity<List<TestExecution>> getExecutionsBySuiteId(@PathVariable String suiteId) {
        List<TestExecution> executions = testExecutionService.getExecutionsBySuiteId(suiteId);
        return new ResponseEntity<>(executions, HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<TestExecution> createExecution(@RequestBody TestExecution execution) {
        TestExecution createdExecution = testExecutionService.createExecution(execution);
        return new ResponseEntity<>(createdExecution, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TestExecution> updateExecution(@PathVariable String id, @RequestBody TestExecution execution) {
        TestExecution updatedExecution = testExecutionService.updateExecution(id, execution);
        return new ResponseEntity<>(updatedExecution, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteExecution(@PathVariable String id) {
        testExecutionService.deleteExecution(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TestExecution>> getExecutionsByStatus(@PathVariable TestExecution.ExecutionStatus status) {
        List<TestExecution> executions = testExecutionService.getExecutionsByStatus(status);
        return new ResponseEntity<>(executions, HttpStatus.OK);
    }
}