package com.example.appointment.service;

import com.example.appointment.dto.MedicalRecordDTO;
import com.example.appointment.entity.MedicalRecord;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordService {
    
    /**
     * 创建病历
     */
    MedicalRecordDTO createMedicalRecord(MedicalRecordDTO medicalRecordDTO);
    
    /**
     * 根据ID查询病历
     */
    Optional<MedicalRecordDTO> getMedicalRecordById(Long id);
    
    /**
     * 根据预约ID查询病历
     */
    Optional<MedicalRecordDTO> getMedicalRecordByAppointmentId(Long appointmentId);
    
    /**
     * 更新病历
     */
    MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO medicalRecordDTO);
    
    /**
     * 删除病历
     */
    void deleteMedicalRecord(Long id);
    
    /**
     * 根据医生ID查询病历列表
     */
    List<MedicalRecordDTO> getMedicalRecordsByDoctorId(Long doctorId);
    
    /**
     * 根据患者ID查询病历列表
     */
    List<MedicalRecordDTO> getMedicalRecordsByPatientId(Long patientId);
    
    /**
     * 根据医生ID和患者ID查询病历列表
     */
    List<MedicalRecordDTO> getMedicalRecordsByDoctorIdAndPatientId(Long doctorId, Long patientId);
    
    /**
     * 查询特定状态的病历
     */
    List<MedicalRecordDTO> getMedicalRecordsByStatus(Integer status);
}
