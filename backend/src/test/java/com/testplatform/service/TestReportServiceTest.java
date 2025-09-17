package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestReport;
import com.testplatform.repository.TestExecutionRepository;
import com.testplatform.repository.TestReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestReportServiceTest {

    @Mock
    private TestReportRepository testReportRepository;

    @Mock
    private TestExecutionRepository testExecutionRepository;

    @InjectMocks
    private TestReportGenerationService testReportGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateReportWithValidExecution() {
        // 准备测试数据
        String executionId = "test-execution-id";
        TestExecution execution = new TestExecution();
        execution.setId(executionId);
        execution.setSuiteId("test-suite-id");
        execution.setStatus(TestExecution.ExecutionStatus.COMPLETED);
        execution.setStartTime(LocalDateTime.now().minusMinutes(5));
        execution.setEndTime(LocalDateTime.now());
        execution.setResult("测试用例 '测试1': 通过 (耗时: 1000 ms)\n测试用例 '测试2': 失败 (耗时: 2000 ms)\n");

        // 设置mock行为
        when(testExecutionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(testReportRepository.save(any(TestReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        TestReport report = testReportGenerationService.generateReport(executionId);

        // 验证结果
        assertNotNull(report);
        assertEquals(executionId, report.getExecutionId());
        assertEquals("test-suite-id", report.getSuiteId());
        assertNotNull(report.getSummary());
        assertEquals(2, report.getSummary().getTotalTests());
        assertEquals(1, report.getSummary().getPassedTests());
        assertEquals(1, report.getSummary().getFailedTests());
        assertEquals(0, report.getSummary().getSkippedTests());
        assertEquals(50.0, report.getSummary().getPassRate());
        assertNotNull(report.getDetails());
        assertEquals(2, report.getDetails().size());

        // 验证mock调用
        verify(testExecutionRepository, times(1)).findById(executionId);
        verify(testReportRepository, times(1)).save(any(TestReport.class));
    }

    @Test
    void testGenerateReportWithInvalidExecution() {
        // 准备测试数据
        String executionId = "invalid-execution-id";

        // 设置mock行为
        when(testExecutionRepository.findById(executionId)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            testReportGenerationService.generateReport(executionId);
        });

        // 验证mock调用
        verify(testExecutionRepository, times(1)).findById(executionId);
        verify(testReportRepository, never()).save(any(TestReport.class));
    }
}