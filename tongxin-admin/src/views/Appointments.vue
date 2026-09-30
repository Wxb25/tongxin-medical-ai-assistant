<template>
  <div class="appointments">
    <div class="tx-card">
      <!-- 筛选区：选科室 → 选医师 → 选日期 → 选时段 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="科室">
          <el-select v-model="filter.department" placeholder="选择科室" clearable style="width:160px" @change="onDeptChange">
            <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="医师">
          <el-select v-model="filter.doctorId" placeholder="选择医师" clearable style="width:160px" :disabled="!filter.department">
            <el-option v-for="d in doctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="filter.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:160px" />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="filter.timeSlot" placeholder="选择时段" clearable style="width:160px">
            <el-option label="上午" value="AM">
              <el-option v-for="s in amSlots" :key="s" :label="s" :value="s" />
            </el-option>
            <el-option label="下午" value="PM">
              <el-option v-for="s in pmSlots" :key="s" :label="s" :value="s" />
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="状态" clearable style="width:120px">
            <el-option label="已预约" :value="1" />
            <el-option label="已取消" :value="2" />
            <el-option label="已完成" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
          <el-button @click="resetFilter"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe style="margin-top:8px">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="department" label="科室" width="90" />
        <el-table-column prop="doctorName" label="医师" width="90" />
        <el-table-column prop="patientName" label="患者" width="90" />
        <el-table-column prop="patientPhone" label="手机号" width="120" />
        <el-table-column prop="appointmentDate" label="日期" width="110" />
        <el-table-column prop="appointmentTime" label="时段" width="110" />
        <el-table-column prop="symptom" label="症状" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="editVisible" title="编辑挂号记录" width="500px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="日期">
          <el-date-picker v-model="editForm.appointmentDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="时段">
          <el-input v-model="editForm.appointmentTime" placeholder="如：09:00-09:30" />
        </el-form-item>
        <el-form-item label="症状">
          <el-input v-model="editForm.symptom" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">已预约</el-radio>
            <el-radio :value="2">已取消</el-radio>
            <el-radio :value="3">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="editForm.status === 2" label="取消原因">
          <el-input v-model="editForm.cancelReason" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { appointmentApi, doctorApi } from '@/api'
import type { Appointment, Doctor } from '@/types'

const amSlots = ['08:00-08:30','08:30-09:00','09:00-09:30','09:30-10:00','10:00-10:30','10:30-11:00','11:00-11:30','11:30-12:00']
const pmSlots = ['14:00-14:30','14:30-15:00','15:00-15:30','15:30-16:00','16:00-16:30','16:30-17:00','17:00-17:30']

const filter = reactive({
  department: '',
  doctorId: null as number | null,
  date: '',
  timeSlot: '',
  status: null as number | null
})
const query = reactive({ pageNum: 1, pageSize: 10 })
const list = ref<Appointment[]>([])
const total = ref(0)
const loading = ref(false)

const departments = ref<string[]>([])
const doctors = ref<Doctor[]>([])
const allDoctors = ref<Doctor[]>([])

const editVisible = ref(false)
const submitting = ref(false)
const editForm = reactive<any>({
  id: 0, appointmentDate: '', appointmentTime: '', symptom: '', status: 1, cancelReason: ''
})

const statusText = (s: number) => s === 1 ? '已预约' : s === 2 ? '已取消' : s === 3 ? '已完成' : '已预约'
const statusType = (s: number) => s === 1 ? 'success' : s === 2 ? 'info' : s === 3 ? 'warning' : 'success'

// 加载所有科室（去重）
const loadDepartments = async () => {
  const res = await doctorApi.all()
  const map = new Map<string, Doctor[]>()
  res.records.forEach(d => {
    if (!map.has(d.department)) map.set(d.department, [])
    map.get(d.department)!.push(d)
  })
  departments.value = Array.from(map.keys()).sort()
  allDoctors.value = res.records
}

const onDeptChange = () => {
  filter.doctorId = null
  doctors.value = allDoctors.value.filter(d => d.department === filter.department)
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { ...query }
    if (filter.department) params.department = filter.department
    if (filter.doctorId) params.doctorId = filter.doctorId
    if (filter.date) params.date = filter.date
    if (filter.timeSlot) params.timeSlot = filter.timeSlot
    if (filter.status !== null) params.status = filter.status
    const res = await appointmentApi.list(params)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const onPageChange = (p: number) => { query.pageNum = p; loadData() }

const resetFilter = () => {
  Object.assign(filter, { department: '', doctorId: null, date: '', timeSlot: '', status: null })
  doctors.value = []
  loadData()
}

const openEdit = (row: Appointment) => {
  Object.assign(editForm, row)
  editVisible.value = true
}

const submitEdit = async () => {
  submitting.value = true
  try {
    await appointmentApi.update(editForm.id, editForm)
    ElMessage.success('编辑成功')
    editVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadDepartments()
  loadData()
})
</script>

<style scoped>
.filter-form { margin-bottom: 8px; }
</style>
