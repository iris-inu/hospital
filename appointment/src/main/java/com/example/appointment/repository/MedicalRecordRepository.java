package com.example.appointment.repository;

import com.example.appointment.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    
    /**
     * 根据预约ID查询病历
     */
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
    
    /**
     * 根据医生ID查询病历列表
     */
    List<MedicalRecord> findByDoctorId(Long doctorId);
    
    /**
     * 根据患者ID查询病历列表
     */
    List<MedicalRecord> findByPatientId(Long patientId);
    
    /**
     * 根据医生ID和患者ID查询病历列表
     */
    List<MedicalRecord> findByDoctorIdAndPatientId(Long doctorId, Long patientId);
    
    /**
     * 根据预约ID、医生ID和患者ID查询病历
     */
    Optional<MedicalRecord> findByAppointmentIdAndDoctorIdAndPatientId(Long appointmentId, Long doctorId, Long patientId);
    
    /**
     * 查询特定状态的病历
     */
    List<MedicalRecord> findByStatus(Integer status);
}
