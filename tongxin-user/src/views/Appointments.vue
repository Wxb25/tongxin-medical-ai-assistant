<template>
  <div class="appointments">
    <div class="tx-card">
      <el-tabs v-model="activeStatus" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="已预约" name="1" />
        <el-tab-pane label="已取消" name="2" />
        <el-tab-pane label="已完成" name="3" />
      </el-tabs>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column label="医生">
          <template #default="{ row }">
            <div>{{ row.doctorName }}</div>
            <div style="font-size:12px;color:var(--tx-text-light)">{{ row.department }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="appointmentDate" label="日期" width="120" />
        <el-table-column prop="appointmentTime" label="时段" width="100" />
        <el-table-column prop="symptom" label="症状" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" :disabled="row.status === 2 || row.status === 3" @click="cancelAppoint(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top:20px;justify-content:flex-end"
        layout="prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="onPageChange"
      />
    </div>

    <el-dialog v-model="cancelDialog" title="取消预约" width="400px">
      <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因" />
      <template #footer>
        <el-button @click="cancelDialog = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="doCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appointmentApi } from '@/api'
import type { AppointmentDTO } from '@/types'

const query = reactive({ status: undefined as number | undefined, pageNum: 1, pageSize: 10 })
const list = ref<AppointmentDTO[]>([])
const total = ref(0)
const loading = ref(false)
const activeStatus = ref('')

// 状态映射：0（历史待确认）和 1（已确认）统一显示为"已预约"
const statusMap: Record<number, { text: string; type: string }> = {
  0: { text: '已预约', type: 'success' },
  1: { text: '已预约', type: 'success' },
  2: { text: '已取消', type: 'info' },
  3: { text: '已完成', type: '' }
}

const cancelDialog = ref(false)
const cancelReason = ref('')
const currentId = ref(0)
const submitting = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await appointmentApi.myList(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const onTabChange = (name: string) => {
  query.status = name === '' ? undefined : Number(name)
  query.pageNum = 1
  loadData()
}

const onPageChange = (p: number) => {
  query.pageNum = p
  loadData()
}

const cancelAppoint = (row: AppointmentDTO) => {
  currentId.value = row.id
  cancelReason.value = ''
  cancelDialog.value = true
}

const doCancel = async () => {
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  submitting.value = true
  try {
    await appointmentApi.cancel(currentId.value, cancelReason.value)
    ElMessage.success('已取消预约')
    cancelDialog.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
