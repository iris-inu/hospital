<template>
  <div class="doctor-medical-record-management">
    <el-card shadow="hover" class="content-card">
      <template #header>
        <div class="card-header">
          <span>病历管理</span>
          <el-button
            type="primary"
            size="small"
            @click="handleCreate"
          >
            <el-icon><Plus /></el-icon>
            创建病历
          </el-button>
        </div>
      </template>
      
      <!-- 搜索和筛选 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="患者姓名">
            <el-input
              v-model="searchForm.patientName"
              placeholder="请输入患者姓名"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="诊断">
            <el-input
              v-model="searchForm.diagnosis"
              placeholder="请输入诊断内容"
              clearable
              style="width: 300px"
            />
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 300px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
            >
              <el-option label="有效" value="ACTIVE" />
              <el-option label="已归档" value="ARCHIVED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 病历列表 -->
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="medicalRecordList"
          style="width: 100%"
          stripe
        >
          <el-table-column prop="id" label="病历ID" width="100" align="center" />
          <el-table-column prop="patientName" label="患者" width="120" align="center" />
          <el-table-column label="日期" width="180" align="center">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="diagnosis" label="诊断" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'">
                {{ scope.row.status === 'ACTIVE' ? '有效' : '已归档' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="handleViewDetail(scope.row.id)"
                style="margin-right: 5px"
              >
                查看
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click="handleEdit(scope.row.id)"
                style="margin-right: 5px"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleArchive(scope.row)"
              >
                {{ scope.row.status === 'ACTIVE' ? '归档' : '激活' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMedicalRecords, updateMedicalRecordStatus } from '@/api/medicalRecord'

const router = useRouter()
const loading = ref(false)
const medicalRecordList = ref([])
const total = ref(0)

// 搜索表单
const searchForm = ref({
  patientName: '',
  diagnosis: '',
  dateRange: null,
  status: ''
})

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 10
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 获取病历列表
const fetchMedicalRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.currentPage,
      size: pagination.value.pageSize,
      patientName: searchForm.value.patientName,
      diagnosis: searchForm.value.diagnosis,
      status: searchForm.value.status
    }
    
    if (searchForm.value.dateRange) {
      params.startDate = searchForm.value.dateRange[0]
      params.endDate = searchForm.value.dateRange[1]
    }
    
    const response = await getMedicalRecords(params)
    medicalRecordList.value = response.data || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('获取病历列表失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  fetchMedicalRecords()
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    patientName: '',
    diagnosis: '',
    dateRange: null,
    status: ''
  }
  pagination.value.currentPage = 1
  fetchMedicalRecords()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
  fetchMedicalRecords()
}

const handleCurrentChange = (current) => {
  pagination.value.currentPage = current
  fetchMedicalRecords()
}

// 创建病历
const handleCreate = () => {
  router.push({ name: 'CreateMedicalRecord' })
}

// 查看病历详情
const handleViewDetail = (id) => {
  router.push({ name: 'MedicalRecordDetail', params: { id } })
}

// 编辑病历
const handleEdit = (id) => {
  router.push({ name: 'EditMedicalRecord', params: { id } })
}

// 归档/激活病历
const handleArchive = (record) => {
  const newStatus = record.status === 'ACTIVE' ? 'ARCHIVED' : 'ACTIVE'
  const action = record.status === 'ACTIVE' ? '归档' : '激活'
  
  ElMessageBox.confirm(`确定要${action}该病历吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateMedicalRecordStatus(record.id, newStatus)
      ElMessage.success(`${action}成功`)
      fetchMedicalRecords()
    } catch (error) {
      ElMessage.error(`${action}失败：` + (error.response?.data?.message || error.message))
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchMedicalRecords()
})
</script>

<style scoped>
.doctor-medical-record-management {
  padding: 20px;
}

.content-card {
  margin: 0 auto;
  max-width: 1400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-container {
  margin-top: 20px;
}

.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.table-container {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.empty-state {
  margin-top: 50px;
  text-align: center;
}
</style>