package com.testplatform.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.model.TestReport.ReportSummary;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ReportSummaryConverter implements AttributeConverter<ReportSummary, String> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(ReportSummary reportSummary) {
        try {
            return objectMapper.writeValueAsString(reportSummary);
        } catch (Exception e) {
            throw new RuntimeException("Error converting ReportSummary to JSON", e);
        }
    }
    
    @Override
    public ReportSummary convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, ReportSummary.class);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to ReportSummary", e);
        }
    }
}
