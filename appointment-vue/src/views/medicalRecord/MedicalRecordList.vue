<template>
  <div class="medical-record-list">
    <el-card shadow="hover" class="content-card">
      <template #header>
        <div class="card-header">
          <span>我的病历</span>
        </div>
      </template>
      
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="medicalRecordList"
          style="width: 100%"
          stripe
        >
          <el-table-column prop="id" label="病历ID" width="100" align="center" />
          <el-table-column label="日期" width="180" align="center">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="doctorName" label="医生" width="120" align="center" />
          <el-table-column prop="diagnosis" label="诊断" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'">
                {{ scope.row.status === 'ACTIVE' ? '有效' : '已归档' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="handleViewDetail(scope.row.id)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-if="medicalRecordList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无病历记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMedicalRecordsByPatientId } from '@/api/medicalRecord'

const router = useRouter()
const loading = ref(false)
const medicalRecordList = ref([])

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取病历列表
const fetchMedicalRecords = async () => {
  loading.value = true
  try {
    // 假设当前用户ID存储在localStorage或Vuex中
    const patientId = localStorage.getItem('userId') || '1'
    const response = await getMedicalRecordsByPatientId(patientId)
    medicalRecordList.value = response.data || []
  } catch (error) {
    ElMessage.error('获取病历列表失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 查看病历详情
const handleViewDetail = (id) => {
  router.push({ name: 'MedicalRecordDetail', params: { id } })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchMedicalRecords()
})
</script>

<style scoped>
.medical-record-list {
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

.table-container {
  margin-top: 20px;
}

.empty-state {
  margin-top: 50px;
  text-align: center;
}
</style>