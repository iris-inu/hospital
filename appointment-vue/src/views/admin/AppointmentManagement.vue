<template>
  <div class="admin-appointment-management">
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.patientName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="医生姓名">
          <el-input v-model="searchForm.doctorName" placeholder="请输入医生姓名" clearable />
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="searchForm.departmentId" placeholder="请选择科室" clearable>
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            placeholder="选择日期"
            clearable
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="appointmentList"
        border
        style="width: 100%"
        row-key="id"
      >
        <el-table-column label="预约ID" prop="id" width="100" />
        <el-table-column label="预约时间" prop="appointmentTime" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column label="患者姓名" prop="patientName" />
        <el-table-column label="科室" prop="departmentName" />
        <el-table-column label="医生" prop="doctorName" />
        <el-table-column label="症状描述" prop="description" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetails(scope.row)"
              :disabled="!scope.row"
            >
              查看详情
            </el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              type="success"
              size="small"
              @click="handleUpdateStatus(scope.row.id, 'CONFIRMED')"
            >
              确认
            </el-button>
            <el-button
              v-if="scope.row.status === 'CONFIRMED'"
              type="warning"
              size="small"
              @click="handleUpdateStatus(scope.row.id, 'COMPLETED')"
            >
              完成
            </el-button>
            <el-button
              v-if="scope.row.status !== 'CANCELLED' && scope.row.status !== 'COMPLETED'"
              type="danger"
              size="small"
              @click="handleUpdateStatus(scope.row.id, 'CANCELLED')"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-show="total > 0"
        :total="total"
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
        layout="total, sizes, prev, pager, next"
      />
    </el-card>

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="detailsVisible"
      title="预约详情"
      width="500px"
    >
      <div v-if="currentAppointment" class="appointment-details">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="预约ID">{{ currentAppointment.id }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ formatDateTime(currentAppointment.appointmentTime) }}</el-descriptions-item>
          <el-descriptions-item label="患者姓名">{{ currentAppointment.patientName }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ currentAppointment.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ currentAppointment.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="症状描述">{{ currentAppointment.description }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentAppointment.remark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentAppointment.status)">
              {{ getStatusText(currentAppointment.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentAppointment.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(currentAppointment.updateTime) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 病历信息 -->
        <div v-if="currentMedicalRecord" class="medical-record-section">
          <h3>病历信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="诊断结果">{{ currentMedicalRecord.diagnosis || '无' }}</el-descriptions-item>
            <el-descriptions-item label="治疗方案">{{ currentMedicalRecord.treatmentPlan || '无' }}</el-descriptions-item>
            <el-descriptions-item label="用药建议">{{ currentMedicalRecord.medicationAdvice || '无' }}</el-descriptions-item>
            <el-descriptions-item label="医生备注">{{ currentMedicalRecord.notes || '无' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(currentMedicalRecord.createdAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else-if="currentAppointment.status === 'COMPLETED'" class="no-medical-record">
          <el-alert
            title="未找到病历信息"
            type="info"
            description="该预约已完成，但尚未创建病历"
            show-icon
            :closable="false"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAppointmentList, updateAppointmentStatus, exportAppointments } from '@/api/appointment'
import { getDepartmentList } from '@/api/department'
import { getMedicalRecordByAppointmentId } from '@/api/medicalRecord'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const appointmentList = ref([])
const departmentList = ref([])
const total = ref(0)
const detailsVisible = ref(false)
const currentAppointment = ref(null)
const currentMedicalRecord = ref(null)

const searchForm = ref({
  patientName: '',
  doctorName: '',
  departmentId: '',
  status: null,
  date: ''
})

const pagination = ref({
  currentPage: 1,
  pageSize: 10
})

// 获取科室列表
const fetchDepartments = async () => {
  try {
    const response = await getDepartmentList({ page: 0, size: 100 })
    if (response && response.content) {
      departmentList.value = response.content
    } else {
      departmentList.value = response || []
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    ElMessage.error('获取科室列表失败')
  }
}

// 获取预约列表
const fetchAppointments = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.currentPage - 1,
      size: pagination.value.pageSize,
      ...searchForm.value
    }

    const response = await getAppointmentList(params)
    console.log('getAppointmentList响应:', response)

    if (response && response.content) {
      appointmentList.value = response.content
      total.value = response.totalElements || 0
    } else {
      appointmentList.value = response || []
      total.value = 0
    }
  } catch (error) {
    console.error('获取预约列表失败:', error)
    ElMessage.error('获取预约列表失败')
    appointmentList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  fetchAppointments()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    patientName: '',
    doctorName: '',
    departmentId: '',
    status: null,
    date: ''
  }
  handleSearch()
}

// 导出预约列表
const handleExport = async () => {
  try {
    const params = {
      ...searchForm.value
    }
    
    const response = await exportAppointments(params)
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    
    // 设置文件名
    const filename = `预约列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.href = url
    link.setAttribute('download', filename)
    
    // 模拟点击下载
    document.body.appendChild(link)
    link.click()
    
    // 清理
    window.URL.revokeObjectURL(url)
    document.body.removeChild(link)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出预约列表失败:', error)
    ElMessage.error('导出失败')
  }
}

// 页码变化
const handleCurrentChange = (val) => {
  pagination.value.currentPage = val
  fetchAppointments()
}

// 每页条数变化
const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  pagination.value.currentPage = 1
  fetchAppointments()
}

// 查看详情
const handleViewDetails = async (appointment) => {
  currentAppointment.value = appointment
  currentMedicalRecord.value = null
  detailsVisible.value = true
  
  // 如果预约已完成，尝试获取病历
  if (appointment.status === 'COMPLETED') {
    try {
      loading.value = true
      const response = await getMedicalRecordByAppointmentId(appointment.id)
      currentMedicalRecord.value = response
    } catch (error) {
      console.error('获取病历失败:', error)
      // 如果获取病历失败，不显示错误，只记录日志
    } finally {
      loading.value = false
    }
  }
}

// 更新状态
const handleUpdateStatus = async (id, status) => {
  try {
    await updateAppointmentStatus(id, status)
    ElMessage.success(`预约已${getStatusText(status)}`)
    fetchAppointments()
  } catch (error) {
    console.error('更新预约状态失败:', error)
    ElMessage.error('更新预约状态失败')
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return typeMap[status] || ''
}

// 页面加载时获取数据
onMounted(() => {
  fetchDepartments()
  fetchAppointments()
})
</script>

<style scoped>
.admin-appointment-management {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.appointment-details {
  max-height: 60vh;
  overflow-y: auto;
}

/* 病历信息区域样式优化 */
.medical-record-section {
  margin-top: 24px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
}

.medical-record-section:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.medical-record-section h3 {
  margin-bottom: 16px;
  color: #1890ff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid #e6f7ff;
  padding-bottom: 8px;
  display: flex;
  align-items: center;
}

.medical-record-section h3::before {
  content: "";
  display: inline-block;
  width: 4px;
  height: 16px;
  background-color: #1890ff;
  margin-right: 8px;
  border-radius: 2px;
}

/* el-descriptions组件样式优化 */
.medical-record-section :deep(.el-descriptions) {
  margin-top: 10px;
}

.medical-record-section :deep(.el-descriptions__label) {
  color: #606266;
  font-weight: 500;
  background-color: #f0f9ff;
}

.medical-record-section :deep(.el-descriptions__content) {
  color: #333;
  line-height: 1.6;
}

.medical-record-section :deep(.el-descriptions-item) {
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
}

.medical-record-section :deep(.el-descriptions-item:last-child) {
  border-bottom: none;
}

.no-medical-record {
  margin-top: 20px;
}
</style>