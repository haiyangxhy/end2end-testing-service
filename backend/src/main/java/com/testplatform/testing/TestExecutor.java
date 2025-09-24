package com.testplatform.testing;

import com.testplatform.model.TestCase;
import com.testplatform.model.TestEnvironment;

public interface TestExecutor {
    TestExecutionResult execute(TestCase testCase, TestEnvironment environment);
}