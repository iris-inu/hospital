<template>
  <div class="quick-appointment-container">
    <el-card class="quick-appointment-card" v-if="true">
      <template #header>
        <div class="card-header">
          <span>快速预约</span>
          <el-tag type="info">三步完成预约</el-tag>
        </div>
      </template>
      
      <!-- 步骤条 -->
      <el-steps :active="currentStep" finish-status="success" class="appointment-steps">
        <el-step title="选择科室" />
        <el-step title="选择医生" />
        <el-step title="确认预约" />
      </el-steps>

      <!-- 第一步：选择科室 -->
      <div v-if="currentStep === 0" class="step-content">
        <div class="department-selection-header">
          <h3>请选择科室</h3>
        </div>
        
        <div class="department-grid" v-if="departments.length > 0">
          <el-card 
            v-for="dept in departments" 
            :key="dept.id"
            class="department-card"
            :class="{ 'selected': selectedDepartment?.id === dept.id }"
            @click="selectDepartment(dept)"
            shadow="hover"
          >
            <div class="department-item">
              <el-icon class="department-icon"><OfficeBuilding /></el-icon>
              <div class="department-info">
                <h4>{{ dept.name }}</h4>
                <p>{{ dept.description }}</p>
              </div>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="暂无科室数据，请稍后重试" />
      </div>

      <!-- 第二步：选择医生 -->
      <div v-if="currentStep === 1" class="step-content">
        <h3>选择医生</h3>
        <div class="doctor-list">
          <el-card 
            v-for="doctor in doctors" 
            :key="doctor.id"
            class="doctor-card"
            :class="{ 'selected': selectedDoctor?.id === doctor.id }"
            @click="selectDoctor(doctor)"
            shadow="hover"
          >
            <div class="doctor-item">
              <el-avatar :size="60" :src="doctor.photoUrl || defaultAvatar" />
              <div class="doctor-info">
                <h4>{{ doctor.name }}</h4>
                <p class="doctor-title">{{ doctor.title }}</p>
                <p class="doctor-specialty">专长：{{ doctor.specialty }}</p>
                <el-tag size="small" type="success">可预约</el-tag>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 第三步：确认预约 -->
      <div v-if="currentStep === 2" class="step-content">
        <h3>确认预约信息</h3>
        <el-card class="appointment-summary">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="科室">{{ selectedDepartment.name }}</el-descriptions-item>
            <el-descriptions-item label="医生">{{ selectedDoctor.name }} - {{ selectedDoctor.title }}</el-descriptions-item>
            <el-descriptions-item label="预约日期">
              <el-date-picker
                  v-model="appointmentDate"
                  type="date"
                  placeholder="选择预约日期"
                  :disabled-date="disabledDate"
                  style="width: 100%"
                />
            </el-descriptions-item>
            <el-descriptions-item label="预约时段">
              <el-radio-group v-model="appointmentPeriod">
                <el-radio label="MORNING">上午 (9:00-12:00)</el-radio>
                <el-radio label="AFTERNOON">下午 (14:00-17:00)</el-radio>
              </el-radio-group>
            </el-descriptions-item>
            <el-descriptions-item label="症状描述">
              <el-input
                v-model="symptomDescription"
                type="textarea"
                placeholder="请简要描述您的症状（可选）"
                :rows="3"
              />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>


      </div>

      <!-- 操作按钮 -->
      <div class="step-actions">
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button 
          v-if="currentStep < 2" 
          type="primary" 
          @click="nextStep"
          :disabled="!canProceed"
        >
          下一步
        </el-button>
        <el-button 
          v-if="currentStep === 2" 
          type="success" 
          @click="submitAppointment"
          :loading="submitting"
          :disabled="!canSubmit"
        >
          确认预约
        </el-button>
      </div>
    </el-card>

    <!-- 预约成功对话框 -->
    <el-dialog
      v-model="successDialogVisible"
      title="预约成功"
      width="400px"
      center
    >
      <div class="success-content">
        <el-icon class="success-icon" color="#67C23A" :size="48"><CircleCheck /></el-icon>
        <h3>预约成功！</h3>
        <p>您的预约信息如下：</p>
        <el-descriptions :column="1" border>
            <el-descriptions-item label="预约号">{{ appointmentResult.appointmentNumber }}</el-descriptions-item>
            <el-descriptions-item label="科室">{{ appointmentResult.departmentName }}</el-descriptions-item>
            <el-descriptions-item label="医生">{{ appointmentResult.doctorName }} {{ appointmentResult.doctorTitle }}</el-descriptions-item>
            <el-descriptions-item label="预约日期">{{ appointmentResult.appointmentDate }}</el-descriptions-item>
            <el-descriptions-item label="预约时间">{{ appointmentResult.formattedAppointmentTime }}</el-descriptions-item>
            <el-descriptions-item label="就诊地点">{{ appointmentResult.location }}</el-descriptions-item>
            <el-descriptions-item label="温馨提示">{{ appointmentResult.notes }}</el-descriptions-item>
          </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="goToMyAppointments">查看我的预约</el-button>
        <el-button type="primary" @click="continueAppointment">继续预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onErrorCaptured } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { 
  getDepartments, 
  getDoctorSchedules,
  createAppointment,
  checkTimeSlotAvailability 
} from '@/api/appointment'
import { getDoctorList } from '@/api/doctor'
import { formatDateTime } from '@/utils/format'
import { OfficeBuilding, CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()

// 错误捕获
onErrorCaptured(() => {
  ElMessage.error('页面加载出错，请刷新重试')
  return false
})

// 步骤控制
const currentStep = ref(0)
const submitting = ref(false)
const successDialogVisible = ref(false)

// 数据
const departments = ref([])
const doctors = ref([])
const selectedDepartment = ref(null)
const selectedDoctor = ref(null)
const appointmentDate = ref('')
const appointmentPeriod = ref('')
const symptomDescription = ref('')
const appointmentResult = ref({})

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 是否可以继续
const canProceed = computed(() => {
  if (currentStep.value === 0) return selectedDepartment.value !== null
  if (currentStep.value === 1) return selectedDoctor.value !== null
  return false
})

// 是否可以提交
const canSubmit = computed(() => {
  return appointmentDate.value && appointmentPeriod.value
})

// 获取科室列表
const fetchDepartments = async () => {
  try {
    const response = await getDepartments()
    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    departments.value = response.content || response || []
  } catch (error) {
    ElMessage.error('获取科室列表失败: ' + (error?.message || '未知错误'))
    // 即使失败也设置空数组，确保页面能显示
    departments.value = []
  }
}

// 获取医生列表
const fetchDoctors = async (departmentId) => {
  try {
    const response = await getDoctorList({ departmentId })
    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    doctors.value = response.content || response
  } catch (error) {
    ElMessage.error('获取医生列表失败')
  }
}

// 选择科室
const selectDepartment = (department) => {
  selectedDepartment.value = department
}

// 选择医生
const selectDoctor = (doctor) => {
  selectedDoctor.value = doctor
}



// 上一步
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 下一步
const nextStep = async () => {
  if (currentStep.value === 0 && selectedDepartment.value) {
    await fetchDoctors(selectedDepartment.value.id)
    currentStep.value++
  } else if (currentStep.value === 1 && selectedDoctor.value) {
    currentStep.value++
  }
}

// 提交预约
const submitAppointment = async () => {
  if (!canSubmit.value) return
  
  submitting.value = true
  try {
    // 确保date是YYYY-MM-DD格式字符串
    let dateParam;
    if (appointmentDate.value instanceof Date) {
      dateParam = `${appointmentDate.value.getFullYear()}-${String(appointmentDate.value.getMonth() + 1).padStart(2, '0')}-${String(appointmentDate.value.getDate()).padStart(2, '0')}`;
    } else if (typeof appointmentDate.value === 'string') {
      dateParam = appointmentDate.value.split('T')[0];
    } else {
      dateParam = appointmentDate.value;
    }
    
    // 获取医生在该日期和时段的预约数量
    const availabilityResponse = await checkTimeSlotAvailability(selectedDoctor.value.id, dateParam, appointmentPeriod.value)
    
    // 检查医生是否有排班
    if (!availabilityResponse.hasSchedule) {
      ElMessage.error('该医生在所选日期和时段没有排班，无法预约')
      return
    }
    
    // 获取预约数量
    const appointmentCount = availabilityResponse.appointmentCount || 0
    
    // 根据预约数量和时段生成具体时间
    let year, month, day;
    if (appointmentDate.value instanceof Date) {
      year = appointmentDate.value.getFullYear();
      month = appointmentDate.value.getMonth();
      day = appointmentDate.value.getDate();
    } else {
      // 字符串格式，假设是YYYY-MM-DD或YYYY-MM-DDTHH:mm:ss格式
      const dateParts = appointmentDate.value.split('T')[0].split('-');
      year = parseInt(dateParts[0]);
      month = parseInt(dateParts[1]) - 1; // 月份从0开始
      day = parseInt(dateParts[2]);
    }
    
    // 设置基础时间：上午9点，下午14点
    const baseHour = appointmentPeriod.value === 'MORNING' ? 9 : 14;
    const baseMinute = 0;
    const timeInterval = 20; // 20分钟为一个时间段
    
    // 计算具体时间 (预约数量 * 时间间隔)
    const totalMinutes = baseMinute + (appointmentCount * timeInterval);
    const hours = baseHour + Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    
    // 使用本地时间创建日期对象，确保与后端时区一致
    const appointmentDateTime = new Date(year, month, day, hours, minutes, 0, 0);

    // 创建包含本地时区信息的时间字符串，避免toISOString()导致的时区偏移
    const formatDateTimeForBackend = (date) => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}:00`;
    };

    const response = await createAppointment({
      doctorId: selectedDoctor.value.id,
      departmentId: selectedDepartment.value.id,
      appointmentTime: formatDateTimeForBackend(appointmentDateTime),
      description: symptomDescription.value || '快速预约'
    })

    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    appointmentResult.value = response
    
    // 如果后端没有返回formattedAppointmentTime，我们在前端处理一下
    if (!appointmentResult.value.formattedAppointmentTime) {
      const hoursStr = String(appointmentDateTime.getHours()).padStart(2, '0')
      const minutesStr = String(appointmentDateTime.getMinutes()).padStart(2, '0')
      appointmentResult.value.formattedAppointmentTime = `${hoursStr}:${minutesStr}`
    }
    
    successDialogVisible.value = true
    // 显示成功消息
    ElMessage({
      message: response.successMessage || '预约成功！',
      type: 'success',
      duration: 5000,
      showClose: true
    })
    // 重置表单
    resetForm()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  currentStep.value = 0
  selectedDepartment.value = null
  selectedDoctor.value = null
  appointmentDate.value = ''
  appointmentPeriod.value = ''
  symptomDescription.value = ''
}



// 禁用日期
const disabledDate = (time) => {
  const date = new Date(time)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today || date.getDay() === 0 || date.getDay() === 6
}

// 查看我的预约
const goToMyAppointments = () => {
  successDialogVisible.value = false
  router.push('/appointment/my')
}

// 继续预约
const continueAppointment = () => {
  successDialogVisible.value = false
  resetForm()
}

onMounted(() => {
  fetchDepartments()
})
</script>

<style lang="scss" scoped>
.quick-appointment-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.quick-appointment-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.appointment-steps {
  margin: 30px 0;
}

.step-content {
  min-height: 400px;
  padding: 20px 0;
}

.department-selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

// 科室网格
.department-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.department-card {
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &.selected {
    border-color: #409EFF;
    background-color: #f0f9ff;
  }
}

.department-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.department-icon {
  font-size: 32px;
  color: #409EFF;
}

.department-info {
  flex: 1;
  
  h4 {
    margin: 0 0 5px 0;
    color: #333;
  }
  
  p {
    margin: 0;
    color: #666;
    font-size: 14px;
  }
}

// 医生列表
.doctor-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.doctor-card {
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &.selected {
    border-color: #67C23A;
    background-color: #f0f9f0;
  }
}

.doctor-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.doctor-info {
  flex: 1;
  
  h4 {
    margin: 0 0 5px 0;
    color: #333;
  }
  
  .doctor-title {
    margin: 0 0 5px 0;
    color: #666;
    font-size: 14px;
  }
  
  .doctor-specialty {
    margin: 0 0 10px 0;
    color: #999;
    font-size: 13px;
  }
}

// 预约摘要
.appointment-summary {
  margin: 20px 0;
}



// 成功内容
.success-content {
  text-align: center;
  padding: 20px;
  
  .success-icon {
    margin-bottom: 20px;
  }
  
  h3 {
    margin: 10px 0;
    color: #67C23A;
  }
  
  p {
    margin: 15px 0;
    color: #666;
  }
}
</style>