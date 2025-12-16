<template>
  <div class="medical-record-detail">
    <el-card shadow="hover" class="content-card">
      <template #header>
        <div class="card-header">
          <span>病历详情</span>
          <el-button
            type="primary"
            size="small"
            @click="goBack"
          >
            返回列表
          </el-button>
        </div>
      </template>
      
      <div v-loading="loading" class="detail-container">
        <div v-if="medicalRecord" class="detail-content">
          <!-- 基本信息 -->
          <el-descriptions :column="1" border>
            <el-descriptions-item label="病历ID">{{ medicalRecord.id }}</el-descriptions-item>
            <el-descriptions-item label="创建日期">{{ formatDate(medicalRecord.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新日期">{{ formatDate(medicalRecord.updateTime) }}</el-descriptions-item>
            <el-descriptions-item label="医生">{{ medicalRecord.doctorName }}</el-descriptions-item>
            <el-descriptions-item label="患者">{{ medicalRecord.patientName }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="medicalRecord.status === 'ACTIVE' ? 'success' : 'info'">
                {{ medicalRecord.status === 'ACTIVE' ? '有效' : '已归档' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 诊断信息 -->
          <el-card shadow="hover" class="section-card" style="margin-top: 20px;">
            <template #header>
              <div class="section-header">
                <span>诊断信息</span>
              </div>
            </template>
            <div class="diagnosis-content">
              <p>{{ medicalRecord.diagnosis }}</p>
            </div>
          </el-card>
          
          <!-- 治疗方案 -->
          <el-card shadow="hover" class="section-card" style="margin-top: 20px;">
            <template #header>
              <div class="section-header">
                <span>治疗方案</span>
              </div>
            </template>
            <div class="treatment-content">
              <p>{{ medicalRecord.treatment }}</p>
            </div>
          </el-card>
          
          <!-- 用药信息 -->
          <el-card shadow="hover" class="section-card" style="margin-top: 20px;">
            <template #header>
              <div class="section-header">
                <span>用药信息</span>
              </div>
            </template>
            <div class="medication-content">
              <p>{{ medicalRecord.medication }}</p>
            </div>
          </el-card>
          
          <!-- 检查结果 -->
          <el-card shadow="hover" class="section-card" style="margin-top: 20px;">
            <template #header>
              <div class="section-header">
                <span>检查结果</span>
              </div>
            </template>
            <div class="examination-content">
              <p>{{ medicalRecord.examinationResult || '无检查结果记录' }}</p>
            </div>
          </el-card>
          
          <!-- 备注信息 -->
          <el-card shadow="hover" class="section-card" style="margin-top: 20px;">
            <template #header>
              <div class="section-header">
                <span>备注</span>
              </div>
            </template>
            <div class="remark-content">
              <p>{{ medicalRecord.remark || '无备注信息' }}</p>
            </div>
          </el-card>
        </div>
        
        <div v-else-if="!loading" class="empty-state">
          <el-empty description="未找到病历信息" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMedicalRecordById } from '@/api/medicalRecord'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const medicalRecord = ref(null)

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取病历详情
const fetchMedicalRecordDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('病历ID不存在')
    goBack()
    return
  }
  
  loading.value = true
  try {
    const response = await getMedicalRecordById(id)
    medicalRecord.value = response.data
  } catch (error) {
    ElMessage.error('获取病历详情失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchMedicalRecordDetail()
})
</script>

<style scoped>
.medical-record-detail {
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

.detail-container {
  margin-top: 20px;
}

.detail-content {
  padding: 10px 0;
}

.section-card {
  border-radius: 8px;
}

.section-header {
  font-weight: bold;
}

.diagnosis-content,
.treatment-content,
.medication-content,
.examination-content,
.remark-content {
  padding: 10px 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.empty-state {
  margin-top: 50px;
  text-align: center;
}
</style>