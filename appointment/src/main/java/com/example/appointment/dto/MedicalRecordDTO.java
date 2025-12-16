package com.example.appointment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicalRecordDTO {
    
    private Long id;
    
    private Long appointmentId;
    
    private Long doctorId;
    
    private Long patientId;
    
    private String diagnosis;
    
    private String treatment;
    
    private String medication;
    
    private String notes;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // 前端使用的字段名，与treatment同义
    private String treatmentPlan;
    
    // 前端使用的字段名，与medication同义
    private String medicationAdvice;
    
    // 扩展信息，用于前端显示
    private String patientName;
    
    private String doctorName;
    
    private String appointmentTime;
}
