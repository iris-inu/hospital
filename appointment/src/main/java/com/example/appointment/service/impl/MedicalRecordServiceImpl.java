package com.example.appointment.service.impl;

import com.example.appointment.dto.MedicalRecordDTO;
import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.Doctor;
import com.example.appointment.entity.MedicalRecord;
import com.example.appointment.entity.User;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.repository.DoctorRepository;
import com.example.appointment.repository.MedicalRecordRepository;
import com.example.appointment.repository.UserRepository;
import com.example.appointment.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {
    
    private static final Logger log = LoggerFactory.getLogger(MedicalRecordServiceImpl.class);
    
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public MedicalRecordDTO createMedicalRecord(MedicalRecordDTO medicalRecordDTO) {
        log.info("创建病历请求: {}", medicalRecordDTO);
        
        // 验证预约是否存在
        Appointment appointment = appointmentRepository.findById(medicalRecordDTO.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("预约不存在，ID: " + medicalRecordDTO.getAppointmentId()));
        
        // 验证医生是否存在
        Doctor doctor = doctorRepository.findById(medicalRecordDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("医生不存在，ID: " + medicalRecordDTO.getDoctorId()));
        
        // 验证患者是否存在
        User patient = userRepository.findById(medicalRecordDTO.getPatientId())
                .orElseThrow(() -> new RuntimeException("患者不存在，ID: " + medicalRecordDTO.getPatientId()));
        
        // 检查是否已经存在该预约的病历
        if (medicalRecordRepository.findByAppointmentId(medicalRecordDTO.getAppointmentId()).isPresent()) {
            throw new RuntimeException("该预约已存在病历");
        }
        
        // 创建病历实体
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setAppointment(appointment);
        medicalRecord.setDoctor(doctor);
        medicalRecord.setPatient(patient);
        medicalRecord.setDiagnosis(medicalRecordDTO.getDiagnosis());
        medicalRecord.setTreatment(medicalRecordDTO.getTreatment());
        medicalRecord.setMedication(medicalRecordDTO.getMedication());
        medicalRecord.setNotes(medicalRecordDTO.getNotes());
        medicalRecord.setStatus(medicalRecordDTO.getStatus() != null ? medicalRecordDTO.getStatus() : 1);
        
        // 保存病历
        MedicalRecord savedMedicalRecord = medicalRecordRepository.save(medicalRecord);
        log.info("病历创建成功，ID: {}", savedMedicalRecord.getId());
        
        return convertToDTO(savedMedicalRecord);
    }
    
    @Override
    public Optional<MedicalRecordDTO> getMedicalRecordById(Long id) {
        log.info("根据ID查询病历，ID: {}", id);
        return medicalRecordRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Override
    public Optional<MedicalRecordDTO> getMedicalRecordByAppointmentId(Long appointmentId) {
        log.info("根据预约ID查询病历，预约ID: {}", appointmentId);
        return medicalRecordRepository.findByAppointmentId(appointmentId)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional
    public MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO medicalRecordDTO) {
        log.info("更新病历请求，ID: {}, 数据: {}", id, medicalRecordDTO);
        
        // 检查病历是否存在
        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("病历不存在，ID: " + id));
        
        // 更新病历信息
        medicalRecord.setDiagnosis(medicalRecordDTO.getDiagnosis());
        medicalRecord.setTreatment(medicalRecordDTO.getTreatment());
        medicalRecord.setMedication(medicalRecordDTO.getMedication());
        medicalRecord.setNotes(medicalRecordDTO.getNotes());
        medicalRecord.setStatus(medicalRecordDTO.getStatus());
        
        // 保存更新后的病历
        MedicalRecord updatedMedicalRecord = medicalRecordRepository.save(medicalRecord);
        log.info("病历更新成功，ID: {}", updatedMedicalRecord.getId());
        
        return convertToDTO(updatedMedicalRecord);
    }
    
    @Override
    @Transactional
    public void deleteMedicalRecord(Long id) {
        log.info("删除病历请求，ID: {}", id);
        
        // 检查病历是否存在
        if (!medicalRecordRepository.existsById(id)) {
            throw new RuntimeException("病历不存在，ID: " + id);
        }
        
        // 删除病历
        medicalRecordRepository.deleteById(id);
        log.info("病历删除成功，ID: {}", id);
    }
    
    @Override
    public List<MedicalRecordDTO> getMedicalRecordsByDoctorId(Long doctorId) {
        log.info("根据医生ID查询病历列表，医生ID: {}", doctorId);
        return medicalRecordRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MedicalRecordDTO> getMedicalRecordsByPatientId(Long patientId) {
        log.info("根据患者ID查询病历列表，患者ID: {}", patientId);
        return medicalRecordRepository.findByPatientId(patientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MedicalRecordDTO> getMedicalRecordsByDoctorIdAndPatientId(Long doctorId, Long patientId) {
        log.info("根据医生ID和患者ID查询病历列表，医生ID: {}, 患者ID: {}", doctorId, patientId);
        return medicalRecordRepository.findByDoctorIdAndPatientId(doctorId, patientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MedicalRecordDTO> getMedicalRecordsByStatus(Integer status) {
        log.info("根据状态查询病历列表，状态: {}", status);
        return medicalRecordRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换实体到DTO
     */
    private MedicalRecordDTO convertToDTO(MedicalRecord medicalRecord) {
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setId(medicalRecord.getId());
        dto.setAppointmentId(medicalRecord.getAppointment().getId());
        dto.setDoctorId(medicalRecord.getDoctor().getId());
        dto.setPatientId(medicalRecord.getPatient().getId());
        dto.setDiagnosis(medicalRecord.getDiagnosis());
        dto.setTreatment(medicalRecord.getTreatment());
        dto.setMedication(medicalRecord.getMedication());
        dto.setNotes(medicalRecord.getNotes());
        dto.setStatus(medicalRecord.getStatus());
        dto.setCreatedAt(medicalRecord.getCreatedAt());
        dto.setUpdatedAt(medicalRecord.getUpdatedAt());
        
        // 设置扩展信息
        dto.setPatientName(medicalRecord.getPatient().getName());
        dto.setDoctorName(medicalRecord.getDoctor().getName());
        dto.setAppointmentTime(medicalRecord.getAppointment().getAppointmentTime().toString());
        
        return dto;
    }
}
