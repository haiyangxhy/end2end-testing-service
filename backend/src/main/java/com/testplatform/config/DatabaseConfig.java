package com.testplatform.config;

import com.testplatform.util.DatabaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig implements CommandLineRunner {

    @Autowired
    private DatabaseValidator databaseValidator;

    @Override
    public void run(String... args) throws Exception {
        // 应用启动时验证数据库结构
        databaseValidator.validateSchema();
    }
}