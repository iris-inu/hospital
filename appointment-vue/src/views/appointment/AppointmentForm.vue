<template>
  <div class="appointment-container">
    <el-card class="filter-card">
      <div class="filter-section">
        <el-form :model="queryForm" inline>
          <el-form-item label="科室">
            <el-select
              v-model="queryForm.departmentId"
              placeholder="请选择科室"
              @change="handleDepartmentChange"
            >
              <el-option
                v-for="dept in departmentOptions"
                :key="dept.value"
                :label="dept.label"
                :value="dept.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="医生">
            <el-select
              v-model="queryForm.doctorId"
              placeholder="请选择医生"
              :disabled="!queryForm.departmentId"
              @change="handleDoctorChange"
            >
              <el-option
                v-for="doctor in doctorOptions"
                :key="doctor.value"
                :label="doctor.label"
                :value="doctor.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <div class="content-section">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="schedule-card">
            <template #header>
              <div class="card-header">
                <span>排班信息</span>
                <el-date-picker
                  v-model="selectedDate"
                  type="date"
                  placeholder="选择日期"
                  :disabled="!queryForm.doctorId"
                  :disabled-date="disabledDate"
                  @change="handleDateChange"
                />
              </div>
            </template>
            <div class="schedule-list">
              <el-empty v-if="!scheduleList.length" description="暂无排班信息" />
              <el-table v-else :data="scheduleList" style="width: 100%">
                <el-table-column prop="period" label="时段" width="180" />
                <el-table-column prop="remainingQuota" label="剩余名额" width="180" />
                <el-table-column label="操作">
                  <template #default="{ row }">
                    <el-button
                      type="primary"
                      :disabled="row.remainingQuota <= 0"
                      @click="handleAppointment(row)"
                    >
                      预约
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <span>医生信息</span>
              </div>
            </template>
            <div v-if="selectedDoctor" class="doctor-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="姓名">{{ selectedDoctor.name }}</el-descriptions-item>
                <el-descriptions-item label="科室">{{ selectedDoctor.departmentName }}</el-descriptions-item>
                <el-descriptions-item label="职称">{{ selectedDoctor.title }}</el-descriptions-item>
                <el-descriptions-item label="专长">{{ selectedDoctor.specialty }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="请选择医生" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDoctorSchedules, createAppointment, checkTimeSlotAvailability } from '@/api/appointment'
import { getDoctorList } from '@/api/doctor'
import { getDepartmentList } from '@/api/department'
import { formatDateToYYYYMMDD } from '@/utils/format'

const queryForm = ref({
  departmentId: '',
  doctorId: ''
})

const selectedDate = ref('')
const departmentOptions = ref([])
const doctorOptions = ref([])
const scheduleList = ref([])
const selectedDoctor = ref(null)

// 获取科室列表
const fetchDepartments = async () => {
  try {
    console.log('正在获取科室列表...')
    const response = await getDepartmentList()
    console.log('科室列表响应:', response)
    
    if (response.code === 200 && response.data) {
      // 检查 response.data 是否包含 content 属性（分页数据）
      if (response.data.content) {
        departmentOptions.value = response.data.content.map(dept => ({
          value: dept.id,
          label: dept.name
        }))
        console.log('科室列表处理成功:', departmentOptions.value)
      } else if (Array.isArray(response.data)) {
        // 如果不是分页数据，直接使用数组
        departmentOptions.value = response.data.map(dept => ({
          value: dept.id,
          label: dept.name
        }))
      } else {
        console.error('科室数据格式不正确:', response.data)
        ElMessage.error('获取科室列表失败：数据格式不正确')
      }
    } else {
      console.error('获取科室列表失败:', response)
      ElMessage.error(response.message || '获取科室列表失败')
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    ElMessage.error('获取科室列表失败')
  }
}

// 获取医生列表
const fetchDoctors = async (departmentId) => {
  if (!departmentId) {
    doctorOptions.value = []
    return
  }

  try {
    console.log('正在获取医生列表，科室ID:', departmentId)
    const response = await getDoctorList({ departmentId })
    console.log('医生列表响应:', response)
    
    if (response.code === 200 && response.data) {
      // 检查 response.data 是否包含 content 属性（分页数据）
      if (response.data.content) {
        doctorOptions.value = response.data.content.map(doctor => ({
          value: doctor.id,
          label: doctor.name,
          ...doctor // 保留其他医生信息
        }))
        console.log('医生列表处理成功:', doctorOptions.value)
      } else if (Array.isArray(response.data)) {
        // 如果不是分页数据，直接使用数组
        doctorOptions.value = response.data.map(doctor => ({
          value: doctor.id,
          label: doctor.name,
          ...doctor // 保留其他医生信息
        }))
      } else {
        console.error('医生数据格式不正确:', response.data)
        ElMessage.error('获取医生列表失败：数据格式不正确')
      }
    } else {
      console.error('获取医生列表失败:', response)
      ElMessage.error(response.message || '获取医生列表失败')
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    ElMessage.error('获取医生列表失败')
  }
}

// 获取排班列表
const fetchSchedules = async () => {
  if (!queryForm.value.doctorId || !selectedDate.value) {
    scheduleList.value = []
    return
  }
  
  try {
    console.log('正在获取排班列表, 医生ID:', queryForm.value.doctorId, '日期:', selectedDate.value)
    const formattedDate = formatDateToYYYYMMDD(selectedDate.value)
    const response = await getDoctorSchedules(queryForm.value.doctorId, formattedDate)
    console.log('排班列表响应:', response)
    
    if (response.code === 200 && response.data) {
      // 时间段映射，将时间段代码转换为具体显示文本
      const timeSlotMap = {
        'MORNING_FIRST': '08:00-09:00',
        'MORNING_SECOND': '09:30-10:30',
        'MORNING_THIRD': '11:00-12:00',
        'AFTERNOON_FIRST': '14:00-15:00',
        'AFTERNOON_SECOND': '15:30-16:30',
        'AFTERNOON_THIRD': '17:00-18:00'
      }
      
      scheduleList.value = response.data.map(schedule => ({
        ...schedule,
        period: timeSlotMap[schedule.period] || schedule.period,
        remainingQuota: schedule.availableAppointments
      }))
      console.log('排班列表处理成功:', scheduleList.value)
    } else {
      console.error('排班列表数据格式不正确:', response)
      ElMessage.error('获取排班列表失败')
    }
  } catch (error) {
    console.error('获取排班列表失败:', error)
    ElMessage.error(error.response?.data?.message || '获取排班列表失败')
  }
}

// 处理科室变化
const handleDepartmentChange = async (value) => {
  queryForm.value.doctorId = ''
  selectedDoctor.value = null
  scheduleList.value = []
  if (value) {
    await fetchDoctors(value)
  } else {
    doctorOptions.value = []
  }
}

// 处理医生选择
const handleDoctorChange = () => {
  const doctor = doctorOptions.value.find(d => d.value === queryForm.value.doctorId)
  selectedDoctor.value = doctor
  scheduleList.value = []
  selectedDate.value = ''
}

// 处理日期变化
const handleDateChange = () => {
  if (selectedDate.value) {
    // 检查选择的日期是否有效（不能选择过去的日期）
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const selected = new Date(selectedDate.value)
    
    if (selected < today) {
      ElMessage.warning('不能选择过去的日期')
      selectedDate.value = ''
      scheduleList.value = []
      return
    }
    
    fetchSchedules()
  } else {
    scheduleList.value = []
  }
}

// 处理查询
const handleSearch = () => {
  if (!queryForm.value.doctorId) {
    ElMessage.warning('请选择医生')
    return
  }
  if (!selectedDate.value) {
    ElMessage.warning('请选择日期')
    return
  }
  fetchSchedules()
}

// 处理预约
const handleAppointment = async (schedule) => {
  try {
    // 检查时间段是否可用
    const formattedDate = formatDateToYYYYMMDD(selectedDate.value)
    const isAvailable = await checkTimeSlotAvailability(
      queryForm.value.doctorId,
      formattedDate,
      schedule.period
    )
    
    if (!isAvailable) {
      ElMessage.warning('该时间段已被预约，请选择其他时间段')
      return
    }

    // 根据具体时间段设置预约时间
    const appointmentTime = new Date(selectedDate.value)
    
    // 根据时间段文本解析具体的小时和分钟
    let hours, minutes
    if (schedule.period.includes('08:00-09:00')) {
      hours = 8
      minutes = 0
    } else if (schedule.period.includes('09:30-10:30')) {
      hours = 9
      minutes = 30
    } else if (schedule.period.includes('11:00-12:00')) {
      hours = 11
      minutes = 0
    } else if (schedule.period.includes('14:00-15:00')) {
      hours = 14
      minutes = 0
    } else if (schedule.period.includes('15:30-16:30')) {
      hours = 15
      minutes = 30
    } else if (schedule.period.includes('17:00-18:00')) {
      hours = 17
      minutes = 0
    } else {
      // 默认值，不应该到达这里
      hours = 9
      minutes = 0
    }
    
    appointmentTime.setHours(hours, minutes, 0, 0)

    // 创建预约
    await createAppointment({
      doctorId: queryForm.value.doctorId,
      departmentId: queryForm.value.departmentId,
      appointmentTime: appointmentTime.toISOString(),
      description: '请准时就诊'
    })
    
    ElMessage.success('预约成功')
    fetchSchedules() // 刷新排班列表
  } catch (error) {
    console.error('预约失败:', error)
    ElMessage.error(error.response?.data?.message || '预约失败')
  }
}

// 禁用过去的日期和周末
const disabledDate = (time) => {
  const date = new Date(time)
  return time.getTime() < Date.now() - 8.64e7 || // 过去的日期
         date.getDay() === 0 || // 周日
         date.getDay() === 6    // 周六
}

onMounted(() => {
  fetchDepartments()
})
</script>

<style lang="scss" scoped>
.appointment-container {
  height: 100%;
  
  .filter-card {
    margin-bottom: 20px;
    
    .filter-section {
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }
  
  .content-section {
    height: calc(100% - 100px);
    
    .schedule-card, .info-card {
      height: 100%;
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .schedule-list {
        height: calc(100% - 60px);
        overflow-y: auto;
      }
      
      .doctor-info {
        padding: 20px;
      }
    }
  }
}

:deep(.el-card__body) {
  height: calc(100% - 60px);
  padding: 20px;
}

:deep(.el-table) {
  height: 100%;
}
</style>