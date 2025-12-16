<template>
  <div class="app-container">
    <div class="filter-container">
      <el-date-picker
        v-model="queryParams.date"
        type="date"
        placeholder="选择日期"
        @change="handleDateChange"
      />
      <el-select v-model="queryParams.status" placeholder="预约状态" clearable @change="handleFilter">
        <el-option label="待确认" value="PENDING" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="handleFilter">查询</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="appointmentList"
      border
      style="width: 100%"
      row-key="id"
    >
      <el-table-column label="预约时间" prop="appointmentTime" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.appointmentTime) }}
        </template>
      </el-table-column>
      <el-table-column label="患者姓名" prop="patientName" />
      <el-table-column label="科室" prop="departmentName" />
      <el-table-column label="症状描述" prop="description" show-overflow-tooltip />
      <el-table-column label="状态" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 'PENDING'"
            type="success"
            size="small"
            @click="handleConfirm(scope.row)"
          >
            确认
          </el-button>
          <el-button
            v-if="scope.row.status === 'CONFIRMED'"
            type="primary"
            size="small"
            @click="handleComplete(scope.row)"
          >
            完成就诊
          </el-button>
          <el-button
            v-if="scope.row.status === 'COMPLETED'"
            type="primary"
            size="small"
            @click="handleViewMedicalRecord(scope.row)"
          >
            查看病历
          </el-button>
          <el-button
            type="primary"
            size="small"
            @click="handleViewDetails(scope.row)"
          >
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-show="total > 0"
      :total="total"
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
      layout="total, sizes, prev, pager, next"
    />

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="detailsVisible"
      title="预约详情"
      width="500px"
    >
      <div v-if="currentAppointment">
        <p><strong>预约时间：</strong>{{ formatDateTime(currentAppointment.appointmentTime) }}</p>
        <p><strong>患者姓名：</strong>{{ currentAppointment.patientName }}</p>
        <p><strong>科室：</strong>{{ currentAppointment.departmentName }}</p>
        <p><strong>症状描述：</strong>{{ currentAppointment.description }}</p>
        <p><strong>状态：</strong>{{ getStatusText(currentAppointment.status) }}</p>
        <p><strong>创建时间：</strong>{{ formatDateTime(currentAppointment.createTime) }}</p>
      </div>
    </el-dialog>

    <!-- 病历创建对话框 -->
    <el-dialog
      v-model="medicalRecordVisible"
      title="创建病历"
      width="600px"
      :before-close="handleMedicalRecordClose"
    >
      <el-form :model="medicalRecordForm" label-width="80px">
        <el-form-item label="患者姓名" :label-width="'80px'">
          <el-input v-model="medicalRecordForm.patientName" disabled />
        </el-form-item>
        <el-form-item label="预约时间" :label-width="'80px'">
          <el-input v-model="medicalRecordForm.appointmentTime" disabled />
        </el-form-item>
        <el-form-item label="诊断结果" :label-width="'80px'" required>
          <el-input
            v-model="medicalRecordForm.diagnosis"
            type="textarea"
            :rows="3"
            placeholder="请输入诊断结果"
          />
        </el-form-item>
        <el-form-item label="治疗方案" :label-width="'80px'" required>
          <el-input
            v-model="medicalRecordForm.treatmentPlan"
            type="textarea"
            :rows="4"
            placeholder="请输入治疗方案"
          />
        </el-form-item>
        <el-form-item label="用药建议" :label-width="'80px'">
          <el-input
            v-model="medicalRecordForm.medicationAdvice"
            type="textarea"
            :rows="3"
            placeholder="请输入用药建议"
          />
        </el-form-item>
        <el-form-item label="注意事项" :label-width="'80px'">
          <el-input
            v-model="medicalRecordForm.notes"
            type="textarea"
            :rows="2"
            placeholder="请输入注意事项"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="medicalRecordVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateMedicalRecord">保存病历</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 病历查看对话框 -->
    <el-dialog
      v-model="viewMedicalRecordVisible"
      title="查看病历"
      width="600px"
    >
      <div v-if="currentMedicalRecord" class="medical-record-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <p><strong>患者姓名：</strong>{{ currentMedicalRecord.patientName }}</p>
          <p><strong>预约时间：</strong>{{ formatDateTime(currentMedicalRecord.appointmentTime) }}</p>
          <p><strong>医生姓名：</strong>{{ currentMedicalRecord.doctorName || '无' }}</p>
          <p><strong>创建时间：</strong>{{ formatDateTime(currentMedicalRecord.createdAt) }}</p>
        </div>
        <div class="detail-section">
          <h4>诊断信息</h4>
          <p><strong>诊断结果：</strong>{{ currentMedicalRecord.diagnosis || '无' }}</p>
          <p><strong>治疗方案：</strong>{{ currentMedicalRecord.treatmentPlan || '无' }}</p>
          <p><strong>用药建议：</strong>{{ currentMedicalRecord.medicationAdvice || '无' }}</p>
          <p><strong>注意事项：</strong>{{ currentMedicalRecord.notes || '无' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctorAppointments, updateAppointmentStatus } from '@/api/appointment'
import { getDoctorByUserId } from '@/api/doctor'
import { createMedicalRecord, getMedicalRecordByAppointmentId } from '@/api/medicalRecord'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

const store = useStore()
const router = useRouter()
const loading = ref(false)
const total = ref(0)
const appointmentList = ref([])
const detailsVisible = ref(false)
const currentAppointment = ref(null)
const doctorInfo = ref(null)

// 病历相关变量
const medicalRecordVisible = ref(false)
const viewMedicalRecordVisible = ref(false)
const medicalRecordForm = ref({
  appointmentId: null,
  patientName: '',
  appointmentTime: '',
  diagnosis: '',
  treatmentPlan: '',
  medicationAdvice: '',
  notes: ''
})
const currentMedicalRecord = ref(null)
const currentAppointmentForMedicalRecord = ref(null)

const userInfo = computed(() => store.state.user.userInfo)
const userRole = computed(() => userInfo.value?.role)
const userId = computed(() => userInfo.value?.id)

// 获取医生信息
const getDoctorInfo = async () => {
  try {
    const response = await getDoctorByUserId(userId.value)
    console.log('Doctor info response:', response)
    
    if (!response) {
      throw new Error('返回数据为空')
    }

    // 后端返回的数据直接就是医生信息对象，不需要检查data字段
    if (!response.id) {
      throw new Error('医生ID不存在')
    }
    
    // 确保医生记录存在且userId匹配
    if (response.userId !== userId.value) {
      console.warn('医生记录的userId与当前用户不匹配:', { 
        doctorUserId: response.userId, 
        currentUserId: userId.value 
      })
      throw new Error('医生信息与当前用户不匹配')
    }
    
    doctorInfo.value = response
    console.log('Doctor info set:', doctorInfo.value)
    return response
  } catch (error) {
    console.error('获取医生信息失败：', error)
    ElMessage.error(error.message || '获取医生信息失败')
    return null
  }
}

// 检查用户权限
onMounted(async () => {
  console.log('Current user info:', userInfo.value)
  console.log('User role:', userRole.value)
  console.log('User ID:', userId.value)
  
  if (userRole.value !== 'DOCTOR') {
    ElMessage.error('无权访问此页面')
    router.push('/home')
    return
  }
  if (!userId.value) {
    ElMessage.error('获取用户信息失败')
    router.push('/home')
    return
  }

  try {
    // 获取医生信息
    const doctor = await getDoctorInfo()
    if (!doctor) {
      ElMessage.error('获取医生信息失败')
      router.push('/home')
      return
    }
    await getList()
  } catch (error) {
    console.error('初始化失败：', error)
    ElMessage.error(error.message || '初始化失败')
    router.push('/home')
  }
})

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  date: null,
  status: ''
})

// 获取预约列表
const getList = async () => {
  if (!doctorInfo.value?.id) {
    console.error('医生信息未找到')
    ElMessage.error('获取医生信息失败')
    return
  }

  loading.value = true
  try {
    // 使用医生ID
    const doctorId = doctorInfo.value.id
    console.log('Fetching appointments for doctor:', doctorId)
    
    const params = {
      page: queryParams.value.pageNum ? queryParams.value.pageNum - 1 : 0,
      size: queryParams.value.pageSize || 10,
      sort: 'appointment_time,desc'
    }
    if (queryParams.value.date) {
      params.date = queryParams.value.date
    }
    if (queryParams.value.status) {
      params.status = queryParams.value.status
    }
    console.log('Request params:', params)
    
    const response = await getDoctorAppointments(doctorId, params)
    console.log('Appointments response:', response)
    
    if (!response) {
      throw new Error('返回数据为空')
    }

    // 直接使用response作为响应数据，不再检查data属性
    const content = response.content || []
    const totalElements = response.totalElements || 0
    console.log('Appointments content:', content)
    console.log('Appointments total:', totalElements)
    appointmentList.value = content
    total.value = totalElements
  } catch (error) {
    console.error('获取预约列表失败：', error)
    ElMessage.error(error.message || '获取预约列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态显示文本
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

// 确认预约
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认这个预约吗？')
    await updateAppointmentStatus(row.id, 'CONFIRMED')
    ElMessage.success('预约已确认')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认预约失败：', error)
      ElMessage.error('确认预约失败')
    }
  }
}

// 完成就诊 - 打开病历创建对话框
const handleComplete = (row) => {
  currentAppointmentForMedicalRecord.value = row
  medicalRecordForm.value = {
    appointmentId: row.id,
    patientName: row.patientName,
    appointmentTime: formatDateTime(row.appointmentTime),
    diagnosis: '',
    treatmentPlan: '',
    medicationAdvice: '',
    notes: ''
  }
  medicalRecordVisible.value = true
}

// 关闭病历对话框前的确认
const handleMedicalRecordClose = () => {
  return ElMessageBox.confirm('是否放弃创建病历？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return true
  }).catch(() => {
    return false
  })
}

// 创建病历
const handleCreateMedicalRecord = async () => {
  try {
    if (!medicalRecordForm.value.diagnosis || !medicalRecordForm.value.treatmentPlan) {
      ElMessage.error('诊断结果和治疗方案不能为空')
      return
    }

    const medicalRecordData = {
      appointmentId: medicalRecordForm.value.appointmentId,
      diagnosis: medicalRecordForm.value.diagnosis,
      treatmentPlan: medicalRecordForm.value.treatmentPlan,
      medicationAdvice: medicalRecordForm.value.medicationAdvice,
      notes: medicalRecordForm.value.notes
    }

    loading.value = true
    // 创建病历
    await createMedicalRecord(medicalRecordData)
    // 更新预约状态为已完成
    await updateAppointmentStatus(medicalRecordForm.value.appointmentId, 'COMPLETED')
    
    medicalRecordVisible.value = false
    ElMessage.success('病历创建成功，就诊已完成')
    getList()
  } catch (error) {
    console.error('创建病历失败：', error)
    ElMessage.error('创建病历失败')
  } finally {
    loading.value = false
  }
}

// 查看病历
const handleViewMedicalRecord = async (row) => {
  try {
    loading.value = true
    const response = await getMedicalRecordByAppointmentId(row.id)
    currentMedicalRecord.value = response
    viewMedicalRecordVisible.value = true
  } catch (error) {
    console.error('获取病历失败：', error)
    ElMessage.error('获取病历失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleViewDetails = (row) => {
  currentAppointment.value = row
  detailsVisible.value = true
}

// 处理日期变化
const handleDateChange = () => {
  handleFilter()
}

// 处理筛选
const handleFilter = () => {
  queryParams.value.pageNum = 1
  getList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  getList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  queryParams.value.pageNum = 1
  getList()
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}

/* 病历创建表单样式优化 */
.el-dialog__body {
  padding: 20px;
}

.el-form {
  max-width: 100%;
}

.el-form-item {
  margin-bottom: 20px;
}

/* 病历详情样式优化 */
.medical-record-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
}

.detail-section:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.detail-section h4 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #1890ff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid #e6f7ff;
  padding-bottom: 8px;
  display: flex;
  align-items: center;
}

.detail-section h4::before {
  content: "";
  display: inline-block;
  width: 4px;
  height: 16px;
  background-color: #1890ff;
  margin-right: 8px;
  border-radius: 2px;
}

.detail-section p {
  margin: 12px 0;
  line-height: 1.8;
  color: #333;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
}

.detail-section p strong {
  color: #606266;
  font-weight: 500;
  min-width: 120px;
}

/* 长文本内容优化 */
.detail-section p:nth-child(n+2):nth-child(-n+5) {
  flex-direction: column;
}

.detail-section p:nth-child(n+2):nth-child(-n+5) strong {
  margin-bottom: 6px;
  min-width: auto;
}
</style>