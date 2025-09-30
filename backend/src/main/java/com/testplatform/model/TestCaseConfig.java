package com.testplatform.model;

import java.util.List;
import java.util.Map;

public class TestCaseConfig {
    private String method;
    private String endpoint;
    private Map<String, String> headers;
    private Map<String, Object> params;
    private Object body;
    private List<TestAssertion> assertions;
    private Map<String, String> extract;
    private Integer timeout;
    private Integer retries;
    
    // Constructors
    public TestCaseConfig() {}
    
    // Getters and Setters
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    public Object getBody() {
        return body;
    }
    
    public void setBody(Object body) {
        this.body = body;
    }
    
    public List<TestAssertion> getAssertions() {
        return assertions;
    }
    
    public void setAssertions(List<TestAssertion> assertions) {
        this.assertions = assertions;
    }
    
    public Map<String, String> getExtract() {
        return extract;
    }
    
    public void setExtract(Map<String, String> extract) {
        this.extract = extract;
    }
    
    public Integer getTimeout() {
        return timeout;
    }
    
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
    
    public Integer getRetries() {
        return retries;
    }
    
    public void setRetries(Integer retries) {
        this.retries = retries;
    }
    
    // 内部类：测试断言
    public static class TestAssertion {
        private String type;
        private String path;
        private Object expected;
        private String operator;
        
        public TestAssertion() {}
        
        public TestAssertion(String type, Object expected) {
            this.type = type;
            this.expected = expected;
        }
        
        public TestAssertion(String type, String path, Object expected) {
            this.type = type;
            this.path = path;
            this.expected = expected;
        }
        
        // Getters and Setters
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        public Object getExpected() {
            return expected;
        }
        
        public void setExpected(Object expected) {
            this.expected = expected;
        }
        
        public String getOperator() {
            return operator;
        }
        
        public void setOperator(String operator) {
            this.operator = operator;
        }
    }
}
