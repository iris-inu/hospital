<template>
  <div class="medical-record-form">
    <el-card shadow="hover" class="content-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEditMode ? '编辑病历' : '创建病历' }}</span>
          <el-button
            type="primary"
            size="small"
            @click="goBack"
          >
            返回列表
          </el-button>
        </div>
      </template>
      
      <div class="form-container">
        <el-form
          v-loading="loading"
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          class="record-form"
        >
          <!-- 基本信息 -->
          <el-card shadow="hover" class="section-card" style="margin-bottom: 20px;">
            <template #header>
              <div class="section-header">
                <span>基本信息</span>
              </div>
            </template>
            
            <div class="form-section">
              <el-form-item label="患者ID" prop="patientId">
                <el-input
                  v-model="formData.patientId"
                  placeholder="请输入患者ID"
                  :disabled="isEditMode"
                />
              </el-form-item>
              
              <el-form-item label="患者姓名" prop="patientName">
                <el-input
                  v-model="formData.patientName"
                  placeholder="请输入患者姓名"
                  :disabled="isEditMode"
                />
              </el-form-item>
              
              <el-form-item label="状态" prop="status">
                <el-select
                  v-model="formData.status"
                  placeholder="请选择状态"
                >
                  <el-option label="有效" value="ACTIVE" />
                  <el-option label="已归档" value="ARCHIVED" />
                </el-select>
              </el-form-item>
            </div>
          </el-card>
          
          <!-- 诊断信息 -->
          <el-card shadow="hover" class="section-card" style="margin-bottom: 20px;">
            <template #header>
              <div class="section-header">
                <span>诊断信息</span>
              </div>
            </template>
            
            <div class="form-section">
              <el-form-item label="诊断内容" prop="diagnosis">
                <el-input
                  v-model="formData.diagnosis"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入诊断内容"
                />
              </el-form-item>
            </div>
          </el-card>
          
          <!-- 治疗方案 -->
          <el-card shadow="hover" class="section-card" style="margin-bottom: 20px;">
            <template #header>
              <div class="section-header">
                <span>治疗方案</span>
              </div>
            </template>
            
            <div class="form-section">
              <el-form-item label="治疗内容" prop="treatment">
                <el-input
                  v-model="formData.treatment"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入治疗方案"
                />
              </el-form-item>
            </div>
          </el-card>
          
          <!-- 用药信息 -->
          <el-card shadow="hover" class="section-card" style="margin-bottom: 20px;">
            <template #header>
              <div class="section-header">
                <span>用药信息</span>
              </div>
            </template>
            
            <div class="form-section">
              <el-form-item label="用药内容" prop="medication">
                <el-input
                  v-model="formData.medication"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入用药信息，包括药物名称、剂量、用法等"
                />
              </el-form-item>
            </div>
          </el-card>
          
          <!-- 检查结果 -->
          <el-card shadow="hover" class="section-card" style="margin-bottom: 20px;">
            <template #header>
              <div class="section-header">
                <span>检查结果</span>
              </div>
            </template>
            
            <div class="form-section">
              <el-form-item label="检查内容" prop="examinationResult">
                <el-input
                  v-model="formData.examinationResult"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入检查结果"
                />
              </el-form-item>
            </div>
          </el-card>
          
          <!-- 备注信息 -->
          <el-card shadow="hover" class="section-card" style="margin-bottom: 20px;">
            <template #header>
              <div class="section-header">
                <span>备注信息</span>
              </div>
            </template>
            
            <div class="form-section">
              <el-form-item label="备注" prop="remark">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入备注信息"
                />
              </el-form-item>
            </div>
          </el-card>
          
          <!-- 表单操作 -->
          <div class="form-actions">
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              {{ isEditMode ? '更新' : '创建' }}
            </el-button>
            <el-button @click="goBack">取消</el-button>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createMedicalRecord, updateMedicalRecord, getMedicalRecordById } from '@/api/medicalRecord'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

// 判断是否为编辑模式
const isEditMode = computed(() => {
  return route.name === 'EditMedicalRecord' || (route.name === 'CreateMedicalRecordByAppointment' && route.params.appointmentId)
})

// 表单数据
const formData = reactive({
  patientId: '',
  patientName: '',
  diagnosis: '',
  treatment: '',
  medication: '',
  examinationResult: '',
  remark: '',
  status: 'ACTIVE'
})

// 表单验证规则
const formRules = reactive({
  patientId: [
    { required: true, message: '请输入患者ID', trigger: 'blur' }
  ],
  patientName: [
    { required: true, message: '请输入患者姓名', trigger: 'blur' }
  ],
  diagnosis: [
    { required: true, message: '请输入诊断内容', trigger: 'blur' }
  ],
  treatment: [
    { required: true, message: '请输入治疗方案', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

// 获取病历详情（编辑模式）
const fetchMedicalRecordDetail = async () => {
  const id = route.params.id
  if (!id) return
  
  loading.value = true
  try {
    const response = await getMedicalRecordById(id)
    const record = response.data
    
    // 填充表单数据
    formData.patientId = record.patientId
    formData.patientName = record.patientName
    formData.diagnosis = record.diagnosis
    formData.treatment = record.treatment
    formData.medication = record.medication
    formData.examinationResult = record.examinationResult
    formData.remark = record.remark
    formData.status = record.status
  } catch (error) {
    ElMessage.error('获取病历详情失败：' + (error.response?.data?.message || error.message))
    goBack()
  } finally {
    loading.value = false
  }
}

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const submitData = {
      patientId: formData.patientId,
      patientName: formData.patientName,
      diagnosis: formData.diagnosis,
      treatment: formData.treatment,
      medication: formData.medication,
      examinationResult: formData.examinationResult,
      remark: formData.remark,
      status: formData.status
    }
    
    if (isEditMode.value && route.params.id) {
      // 编辑模式
      await updateMedicalRecord(route.params.id, submitData)
      ElMessage.success('更新病历成功')
    } else if (route.params.appointmentId) {
      // 通过预约创建病历
      submitData.appointmentId = route.params.appointmentId
      await createMedicalRecord(submitData)
      ElMessage.success('创建病历成功')
    } else {
      // 创建新病历
      await createMedicalRecord(submitData)
      ElMessage.success('创建病历成功')
    }
    
    // 返回病历列表
    router.push({ name: 'DoctorMedicalRecordManagement' })
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message !== 'Cancel') {
      ElMessage.error(isEditMode.value ? '更新失败' : '创建失败')
    }
  } finally {
    submitting.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push({ name: 'DoctorMedicalRecordManagement' })
}

// 组件挂载时获取数据（编辑模式）
onMounted(() => {
  if (isEditMode.value && route.params.id) {
    fetchMedicalRecordDetail()
  }
})
</script>

<style scoped>
.medical-record-form {
  padding: 20px;
}

.content-card {
  margin: 0 auto;
  max-width: 1200px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-container {
  margin-top: 20px;
}

.record-form {
  padding: 0 10px;
}

.section-card {
  border-radius: 8px;
}

.section-header {
  font-weight: bold;
}

.form-section {
  padding: 10px 0;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.form-actions button {
  margin: 0 10px;
}
</style>