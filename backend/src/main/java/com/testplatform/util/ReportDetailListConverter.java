package com.testplatform.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.model.TestReport.ReportDetail;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

@Converter
public class ReportDetailListConverter implements AttributeConverter<List<ReportDetail>, String> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(List<ReportDetail> reportDetails) {
        try {
            return objectMapper.writeValueAsString(reportDetails);
        } catch (Exception e) {
            throw new RuntimeException("Error converting ReportDetail list to JSON", e);
        }
    }
    
    @Override
    public List<ReportDetail> convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ReportDetail>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to ReportDetail list", e);
        }
    }
}
