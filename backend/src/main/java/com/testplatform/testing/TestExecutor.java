package com.testplatform.testing;

import com.testplatform.model.TestCase;
import com.testplatform.model.TargetSystemConfig;

public interface TestExecutor {
    TestExecutionResult execute(TestCase testCase, TargetSystemConfig config);
}