package com.testplatform.testing;

import com.testplatform.model.TestCase;
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
    
    public TestExecutor getExecutor(TestCase testCase) {
        switch (testCase.getType()) {
            case API:
                return apiTestExecutor;
            case UI:
                return uiTestExecutor;
            case BUSINESS:
                return businessTestExecutor;
            default:
                throw new IllegalArgumentException("不支持的测试类型: " + testCase.getType());
        }
    }
}