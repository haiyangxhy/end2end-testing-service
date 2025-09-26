package com.testplatform.testing;

import com.testplatform.model.TestSuite;
import com.testplatform.testing.api.ApiTestExecutor;
import com.testplatform.testing.ui.UiTestExecutor;
import com.testplatform.testing.business.BusinessTestExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestExecutorFactory {
    
    @Autowired
    private ApiTestExecutor apiTestExecutor;
    
    @Autowired
    private UiTestExecutor uiTestExecutor;
    
    @Autowired
    private BusinessTestExecutor businessTestExecutor;
    
    /**
     * 根据测试套件类型选择执行器
     * 测试用例不再有类型字段，类型由所属的测试套件决定
     */
    public TestExecutor getExecutor(TestSuite testSuite) {
        switch (testSuite.getType()) {
            case API:
                return apiTestExecutor;
            case UI:
                return uiTestExecutor;
            case BUSINESS:
                return businessTestExecutor;
            default:
                throw new IllegalArgumentException("不支持的测试套件类型: " + testSuite.getType());
        }
    }
    
    /**
     * 根据测试套件类型选择执行器（重载方法，支持直接传入类型）
     */
    public TestExecutor getExecutor(TestSuite.TestSuiteType suiteType) {
        switch (suiteType) {
            case API:
                return apiTestExecutor;
            case UI:
                return uiTestExecutor;
            case BUSINESS:
                return businessTestExecutor;
            default:
                throw new IllegalArgumentException("不支持的测试套件类型: " + suiteType);
        }
    }
}