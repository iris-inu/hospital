package com.example.appointment.service.impl;

import com.example.appointment.dto.DoctorDTO;
import com.example.appointment.dto.UserDTO;
import com.example.appointment.entity.Department;
import com.example.appointment.entity.Doctor;
import com.example.appointment.repository.DepartmentRepository;
import com.example.appointment.repository.DoctorRepository;
import com.example.appointment.service.DoctorService;
import com.example.appointment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    
    private static final Logger log = LoggerFactory.getLogger(DoctorServiceImpl.class);

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    @Override
    @Transactional
    public DoctorDTO create(DoctorDTO doctorDTO) {
        // 检查科室是否存在并获取
        Set<Department> departments = new HashSet<>();
        for (Long deptId : doctorDTO.getDepartmentIds()) {
            Department department = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new RuntimeException("科室不存在，ID: " + deptId));
            departments.add(department);
        }

        // 确保userId字段有值
        if (doctorDTO.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        Doctor doctor = new Doctor();
        doctor.setUserId(doctorDTO.getUserId());  // 显式设置userId
        updateDoctorFromDTO(doctor, doctorDTO, departments);
        
        doctor = doctorRepository.save(doctor);
        return convertToDTO(doctor);
    }

    @Override
    @Transactional
    public DoctorDTO update(Long id, DoctorDTO doctorDTO) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));

        // 检查科室是否存在并获取
        Set<Department> departments = new HashSet<>();
        for (Long deptId : doctorDTO.getDepartmentIds()) {
            Department department = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new RuntimeException("科室不存在，ID: " + deptId));
            departments.add(department);
        }

        updateDoctorFromDTO(doctor, doctorDTO, departments);
        
        doctor = doctorRepository.save(doctor);
        
        // 同步更新用户信息
        updateUserInfo(doctor.getUserId(), doctorDTO);
        
        return convertToDTO(doctor);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!doctorRepository.existsByIdCustom(id)) {
            throw new RuntimeException("医生不存在");
        }
        
        // 获取医生信息以删除对应的用户
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        
        // 先删除医生记录
        doctorRepository.deleteById(id);
        
        // 同步删除对应的用户信息
        if (doctor.getUserId() != null) {
            try {
                userService.deleteUser(doctor.getUserId());
                log.info("成功同步删除用户信息，用户ID: {}", doctor.getUserId());
            } catch (Exception e) {
                log.error("同步删除用户信息失败，用户ID: {}，错误: {}", doctor.getUserId(), e.getMessage());
                // 不抛出异常，避免影响医生删除主流程
            }
        }
    }

    @Override
    public DoctorDTO getById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        return convertToDTO(doctor);
    }

    @Override
    public Page<DoctorDTO> search(String name, Long departmentId, List<Long> departmentIds, Integer status, Pageable pageable) {
        Page<Doctor> doctors;
        
        // 优先使用新的departmentIds参数（多科室筛选）
        if (departmentIds != null && !departmentIds.isEmpty()) {
            // 有departmentIds参数，使用多科室筛选
            if (StringUtils.hasText(name) && status != null) {
                doctors = doctorRepository.findDoctorsByNameAndDepartmentIdsAndStatus(name, departmentIds, status, pageable);
            } else if (StringUtils.hasText(name)) {
                doctors = doctorRepository.findDoctorsByNameAndDepartmentIds(name, departmentIds, pageable);
            } else if (status != null) {
                doctors = doctorRepository.findDoctorsByDepartmentIdsAndStatus(departmentIds, status, pageable);
            } else {
                doctors = doctorRepository.findDoctorsByDepartmentIds(departmentIds, pageable);
            }
        } else {
            // 没有departmentIds参数，使用旧的单departmentId参数
            if (StringUtils.hasText(name) && departmentId != null && status != null) {
                doctors = doctorRepository.findDoctorsByNameAndDepartmentAndStatus(name, departmentId, status, pageable);
            } else if (StringUtils.hasText(name) && status != null) {
                doctors = doctorRepository.findDoctorsByNameAndStatus(name, status, pageable);
            } else if (departmentId != null && status != null) {
                doctors = doctorRepository.findByDepartmentIdAndStatus(departmentId, status, pageable);
            } else {
                doctors = doctorRepository.findAll(pageable);
            }
        }
        
        return doctors.map(this::convertToDTO);
    }

    @Override
    public List<DoctorDTO> getByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorDTO getDoctorByUserId(Long userId) {
        log.info("Fetching doctor info for userId: {}", userId);
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Doctor not found for userId: {}", userId);
                    return new RuntimeException("医生信息不存在");
                });
        log.info("Found doctor: {}", doctor);
        DoctorDTO dto = convertToDTO(doctor);
        log.info("Converted to DTO: {}", dto);
        return dto;
    }

    private void updateDoctorFromDTO(Doctor doctor, DoctorDTO dto, Set<Department> departments) {
        doctor.setName(dto.getName());
        doctor.setTitle(dto.getTitle());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setIntroduction(dto.getIntroduction());
        doctor.setPhotoUrl(dto.getPhotoUrl());
        doctor.setDepartments(departments);
        doctor.setStatus(dto.getStatus());
        
        // 设置userId，确保与User表关联
        if (dto.getUserId() != null) {
            doctor.setUserId(dto.getUserId());
        }
    }

    private void updateUserInfo(Long userId, DoctorDTO doctorDTO) {
        if (userId == null) {
            log.warn("用户ID为空，无法同步更新用户信息");
            return;
        }
        
        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setName(doctorDTO.getName());
            userDTO.setPhone(doctorDTO.getPhone());
            userDTO.setEmail(doctorDTO.getEmail());
            userDTO.setUsername(doctorDTO.getUsername());
            
            userService.updateUser(userId, userDTO);
            log.info("成功同步更新用户信息，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("同步更新用户信息失败，用户ID: {}，错误: {}", userId, e.getMessage());
            // 不抛出异常，避免影响医生信息更新
        }
    }

    private DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setUserId(doctor.getUserId());
        dto.setName(doctor.getName());
        dto.setTitle(doctor.getTitle());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setIntroduction(doctor.getIntroduction());
        dto.setPhotoUrl(doctor.getPhotoUrl());
        dto.setStatus(doctor.getStatus());
        
        // 处理多科室信息
        if (doctor.getDepartments() != null && !doctor.getDepartments().isEmpty()) {
            // 设置departmentIds列表
            List<Long> departmentIds = doctor.getDepartments().stream()
                    .map(Department::getId)
                    .collect(Collectors.toList());
            dto.setDepartmentIds(departmentIds);
            
            // 设置departmentNames集合
            Set<String> departmentNames = doctor.getDepartments().stream()
                    .map(Department::getName)
                    .collect(Collectors.toSet());
            dto.setDepartmentNames(departmentNames);
            
            // 兼容旧接口，设置第一个科室ID
            dto.setDepartmentId(departmentIds.get(0));
            dto.setDepartmentName(departmentNames.iterator().next());
        }
        
        // 获取用户信息
        if (doctor.getUserId() != null) {
            try {
                UserDTO userDTO = userService.getUserById(doctor.getUserId());
                if (userDTO != null) {
                    dto.setUsername(userDTO.getUsername());
                    dto.setEmail(userDTO.getEmail());
                    dto.setPhone(userDTO.getPhone());
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败，用户ID: {}，错误: {}", doctor.getUserId(), e.getMessage());
                // 不抛出异常，继续返回医生基本信息
            }
        }
        
        return dto;
    }
}