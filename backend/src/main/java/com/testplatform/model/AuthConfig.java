package com.testplatform.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthConfig {
    private String type;
    private String loginUrl;
    private Credentials credentials;
    private String tokenField;
    private String refreshTokenField;
    private String tokenVersionField;
    private String headerName;
    private String headerFormat;
    private String refreshUrl;
    private String refreshMethod;
    private java.util.Map<String, String> refreshParams;
    private Integer expiresIn;
    private Boolean autoRefresh;
    private String apiKey;
    private String apiKeyHeader;
    private String apiKeyValue;
    
    // Constructors
    public AuthConfig() {}
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getLoginUrl() {
        return loginUrl;
    }
    
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }
    
    public Credentials getCredentials() {
        return credentials;
    }
    
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
    
    public String getTokenField() {
        return tokenField;
    }
    
    public void setTokenField(String tokenField) {
        this.tokenField = tokenField;
    }
    
    public String getRefreshTokenField() {
        return refreshTokenField;
    }
    
    public void setRefreshTokenField(String refreshTokenField) {
        this.refreshTokenField = refreshTokenField;
    }
    
    public String getTokenVersionField() {
        return tokenVersionField;
    }
    
    public void setTokenVersionField(String tokenVersionField) {
        this.tokenVersionField = tokenVersionField;
    }
    
    public String getHeaderName() {
        return headerName;
    }
    
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
    
    public String getHeaderFormat() {
        return headerFormat;
    }
    
    public void setHeaderFormat(String headerFormat) {
        this.headerFormat = headerFormat;
    }
    
    public String getRefreshUrl() {
        return refreshUrl;
    }
    
    public void setRefreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
    }
    
    public String getRefreshMethod() {
        return refreshMethod;
    }
    
    public void setRefreshMethod(String refreshMethod) {
        this.refreshMethod = refreshMethod;
    }
    
    public java.util.Map<String, String> getRefreshParams() {
        return refreshParams;
    }
    
    public void setRefreshParams(java.util.Map<String, String> refreshParams) {
        this.refreshParams = refreshParams;
    }
    
    public Integer getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public Boolean getAutoRefresh() {
        return autoRefresh;
    }
    
    public void setAutoRefresh(Boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiKeyHeader() {
        return apiKeyHeader;
    }
    
    public void setApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
    }
    
    public String getApiKeyValue() {
        return apiKeyValue;
    }
    
    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }
    
    // 内部类：认证凭据
    public static class Credentials {
        private String username;
        private String password;
        
        public Credentials() {}
        
        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
