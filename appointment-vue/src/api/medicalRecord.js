import request from '@/utils/request'

/**
 * 病历相关API调用封装
 */

// 创建病历
export function createMedicalRecord(data) {
  return request({
    url: '/medical-records',
    method: 'post',
    data
  })
}

// 根据ID查询病历
export function getMedicalRecordById(id) {
  return request({
    url: `/medical-records/${id}`,
    method: 'get'
  })
}

// 根据预约ID查询病历
export function getMedicalRecordByAppointmentId(appointmentId) {
  return request({
    url: `/medical-records/appointment/${appointmentId}`,
    method: 'get'
  })
}

// 更新病历
export function updateMedicalRecord(id, data) {
  return request({
    url: `/medical-records/${id}`,
    method: 'put',
    data
  })
}

// 删除病历
export function deleteMedicalRecord(id) {
  return request({
    url: `/medical-records/${id}`,
    method: 'delete'
  })
}

// 根据医生ID查询病历列表
export function getMedicalRecordsByDoctorId(doctorId) {
  return request({
    url: `/medical-records/doctor/${doctorId}`,
    method: 'get'
  })
}

// 根据患者ID查询病历列表
export function getMedicalRecordsByPatientId(patientId) {
  return request({
    url: `/medical-records/patient/${patientId}`,
    method: 'get'
  })
}

// 根据医生ID和患者ID查询病历列表
export function getMedicalRecordsByDoctorIdAndPatientId(doctorId, patientId) {
  return request({
    url: `/medical-records/doctor/${doctorId}/patient/${patientId}`,
    method: 'get'
  })
}

// 根据状态查询病历
export function getMedicalRecordsByStatus(status) {
  return request({
    url: `/medical-records/status/${status}`,
    method: 'get'
  })
}

// 获取当前患者的病历列表
export function getCurrentPatientMedicalRecords() {
  return request({
    url: '/medical-records/patient/current',
    method: 'get'
  })
}

// 获取当前医生的病历列表
export function getCurrentDoctorMedicalRecords() {
  return request({
    url: '/medical-records/doctor/current',
    method: 'get'
  })
}

// 获取病历列表（支持分页、搜索和筛选）
export function getMedicalRecords(params) {
  return request({
    url: '/medical-records',
    method: 'get',
    params
  })
}

// 更新病历状态
export function updateMedicalRecordStatus(id, status) {
  return request({
    url: `/medical-records/${id}/status`,
    method: 'patch',
    data: { status }
  })
}