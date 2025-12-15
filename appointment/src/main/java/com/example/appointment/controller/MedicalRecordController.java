package com.example.appointment.controller;

import com.example.appointment.annotation.RequireRole;
import com.example.appointment.common.Result;
import com.example.appointment.dto.MedicalRecordDTO;
import com.example.appointment.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {
    
    private final MedicalRecordService medicalRecordService;
    
    /**
     * 创建病历
     */
    @PostMapping
    @RequireRole({"DOCTOR", "ADMIN"})
    public Result<MedicalRecordDTO> create(@RequestBody MedicalRecordDTO medicalRecordDTO) {
        log.info("创建病历请求: {}", medicalRecordDTO);
        return Result.success(medicalRecordService.createMedicalRecord(medicalRecordDTO));
    }
    
    /**
     * 根据ID查询病历
     */
    @GetMapping("/{id}")
    public Result<MedicalRecordDTO> getById(@PathVariable Long id) {
        log.info("查询病历，ID: {}", id);
        return medicalRecordService.getMedicalRecordById(id)
                .map(Result::success)
                .orElse(Result.error("病历不存在"));
    }
    
    /**
     * 根据预约ID查询病历
     */
    @GetMapping("/appointment/{appointmentId}")
    public Result<MedicalRecordDTO> getByAppointmentId(@PathVariable Long appointmentId) {
        log.info("根据预约ID查询病历，预约ID: {}", appointmentId);
        return medicalRecordService.getMedicalRecordByAppointmentId(appointmentId)
                .map(Result::success)
                .orElse(Result.error("病历不存在"));
    }
    
    /**
     * 更新病历
     */
    @PutMapping("/{id}")
    @RequireRole({"DOCTOR", "ADMIN"})
    public Result<MedicalRecordDTO> update(@PathVariable Long id, @RequestBody MedicalRecordDTO medicalRecordDTO) {
        log.info("更新病历，ID: {}, 数据: {}", id, medicalRecordDTO);
        return Result.success(medicalRecordService.updateMedicalRecord(id, medicalRecordDTO));
    }
    
    /**
     * 删除病历
     */
    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除病历，ID: {}", id);
        medicalRecordService.deleteMedicalRecord(id);
        return Result.success();
    }
    
    /**
     * 根据医生ID查询病历列表
     */
    @GetMapping("/doctor/{doctorId}")
    @RequireRole({"DOCTOR", "ADMIN"})
    public Result<List<MedicalRecordDTO>> getByDoctorId(@PathVariable Long doctorId) {
        log.info("根据医生ID查询病历列表，医生ID: {}", doctorId);
        return Result.success(medicalRecordService.getMedicalRecordsByDoctorId(doctorId));
    }
    
    /**
     * 根据患者ID查询病历列表
     */
    @GetMapping("/patient/{patientId}")
    @RequireRole({"PATIENT", "ADMIN"})
    public Result<List<MedicalRecordDTO>> getByPatientId(@PathVariable Long patientId) {
        log.info("根据患者ID查询病历列表，患者ID: {}", patientId);
        return Result.success(medicalRecordService.getMedicalRecordsByPatientId(patientId));
    }
    
    /**
     * 根据医生ID和患者ID查询病历列表
     */
    @GetMapping("/doctor/{doctorId}/patient/{patientId}")
    @RequireRole({"DOCTOR", "ADMIN"})
    public Result<List<MedicalRecordDTO>> getByDoctorIdAndPatientId(
            @PathVariable Long doctorId, 
            @PathVariable Long patientId) {
        log.info("根据医生ID和患者ID查询病历列表，医生ID: {}, 患者ID: {}", doctorId, patientId);
        return Result.success(medicalRecordService.getMedicalRecordsByDoctorIdAndPatientId(doctorId, patientId));
    }
    
    /**
     * 查询特定状态的病历
     */
    @GetMapping("/status/{status}")
    @RequireRole({"ADMIN"})
    public Result<List<MedicalRecordDTO>> getByStatus(@PathVariable Integer status) {
        log.info("根据状态查询病历列表，状态: {}", status);
        return Result.success(medicalRecordService.getMedicalRecordsByStatus(status));
    }
}
