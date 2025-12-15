package com.example.appointment.dto;

import lombok.Data;

@Data
public class DoctorTimeSlotDTO {
    private Long scheduleId;
    private String period; // MORNING, AFTERNOON
    private String timeSlot; // 具体时间段，如：08:00-09:00, 09:30-10:30等
    private Integer maxAppointments;
    private Integer availableAppointments;
    private Boolean isAvailable; // 是否有剩余号源
    private String congestionLevel; // 拥挤度级别：LOW, MEDIUM, HIGH, FULL
    private Double congestionRate; // 拥挤度比例 (0.0-1.0)
    private Integer bookedAppointments; // 已预约数量
}