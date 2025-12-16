<template>
  <div class="my-appointment">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的预约记录</span>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="appointments"
        style="width: 100%"
      >
        <el-table-column prop="departmentName" label="科室" />
        <el-table-column prop="doctorName" label="医生" />
        <el-table-column prop="appointmentTime" label="预约时间">
          <template #default="scope">
            {{ formatDateTime(scope.row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="handleCancel(scope.row)"
            >
              取消预约
            </el-button>
            <el-button
            v-if="scope.row.status === 'COMPLETED'"
            type="primary"
            size="small"
            @click="handleViewMedicalRecord(scope.row)"
          >
            查看病历
          </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 病历查看对话框 -->
    <el-dialog
      v-model="medicalRecordVisible"
      title="我的病历"
      width="600px"
    >
      <div v-if="currentMedicalRecord" class="medical-record-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <p><strong>医生姓名：</strong>{{ currentMedicalRecord.doctorName || '无' }}</p>
          <p><strong>预约时间：</strong>{{ formatDateTime(currentMedicalRecord.appointmentTime) }}</p>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import { getPatientAppointments, cancelAppointment } from '@/api/appointment'
import { getMedicalRecordByAppointmentId } from '@/api/medicalRecord'

const store = useStore()
const loading = ref(false)
const appointments = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 病历相关变量
const medicalRecordVisible = ref(false)
const currentMedicalRecord = ref(null)

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'CANCELLED': 'info',
    'COMPLETED': 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CANCELLED': '已取消',
    'COMPLETED': '已完成'
  }
  return texts[status] || status
}

const fetchAppointments = async () => {
  loading.value = true
  try {
    const response = await getPatientAppointments({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    console.log('预约记录响应:', response);
    
    // 处理不同的数据结构
    if (response && response.content) {
      // 标准分页结构
      appointments.value = response.content;
      total.value = response.totalElements;
    } else if (response && Array.isArray(response)) {
      // 直接返回数组
      appointments.value = response;
      total.value = response.length;
    } else if (response && response.data) {
      // 嵌套结构
      if (response.data.content) {
        appointments.value = response.data.content;
        total.value = response.data.totalElements;
      } else if (Array.isArray(response.data)) {
        appointments.value = response.data;
        total.value = response.data.length;
      } else {
        appointments.value = [];
        total.value = 0;
      }
    } else {
      appointments.value = [];
      total.value = 0;
      ElMessage.warning('没有找到预约记录');
    }
  } catch (error) {
    console.error('获取预约记录失败:', error);
    ElMessage.error('获取预约记录失败：' + (error.message || '未知错误'));
    appointments.value = [];
    total.value = 0;
  } finally {
    loading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelAppointment(row.id)
    ElMessage.success('预约已取消')
    fetchAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消预约失败：' + error.message)
    }
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAppointments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAppointments()
}

// 查看病历
const handleViewMedicalRecord = async (row) => {
  try {
    console.log('查看病历按钮被点击，行数据：', row)
    console.log('预约ID：', row.id)
    console.log('预约状态：', row.status)
    
    loading.value = true
    console.log('调用API：getMedicalRecordByAppointmentId，参数：', row.id)
    const response = await getMedicalRecordByAppointmentId(row.id)
    console.log('API响应：', response)
    
    currentMedicalRecord.value = response
    console.log('设置当前病历：', currentMedicalRecord.value)
    
    medicalRecordVisible.value = true
    console.log('设置病历对话框可见：', medicalRecordVisible.value)
  } catch (error) {
    console.error('获取病历失败：', error)
    console.error('错误详情：', JSON.stringify(error, null, 2))
    ElMessage.error('获取病历失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
    console.log('加载状态设置为false')
  }
}

onMounted(() => {
  fetchAppointments()
})
</script>

<style lang="scss" scoped>
.my-appointment {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

/* 病历详情样式优化 */
.el-dialog__body {
  padding: 20px;
}

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