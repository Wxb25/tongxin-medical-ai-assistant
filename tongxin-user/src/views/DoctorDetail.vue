<template>
  <div class="doctor-detail" v-loading="loading">
    <template v-if="doctor">
      <el-page-header @back="$router.back()" content="返回" style="margin-bottom:16px" />
      <div class="tx-card detail-card">
        <div class="doc-header">
          <el-avatar :size="90" :icon="UserFilled" />
          <div class="doc-main">
            <h2>{{ doctor.name }}
              <el-tag type="success">{{ doctor.title }}</el-tag>
            </h2>
            <div class="dept"><el-icon><OfficeBuilding /></el-icon> {{ doctor.department }}</div>
          </div>
          <div class="fee">
            <div class="fee-num">¥{{ doctor.consultationFee }}</div>
            <div class="fee-label">挂号费</div>
          </div>
        </div>
        <el-divider />
        <div class="section">
          <div class="label">专长</div>
          <div class="value">{{ doctor.specialty || '暂无' }}</div>
        </div>
        <div class="section" v-if="doctor.introduction">
          <div class="label">简介</div>
          <div class="value intro-text">{{ doctor.introduction }}</div>
        </div>
        <div class="action-bar">
          <el-button type="primary" size="large" @click="goBooking">
            <el-icon><Clock /></el-icon>预约挂号
          </el-button>
        </div>
      </div>

      <div class="tx-card schedule-card" style="margin-top:16px">
        <div class="section-title">排班信息（未来 7 天号源）</div>
        <el-table :data="scheduleMatrix" border stripe v-loading="scheduleLoading">
          <el-table-column prop="timeSlot" label="时段" width="140" fixed="left" />
          <el-table-column
            v-for="d in dateColumns"
            :key="d.value"
            :label="d.label"
            width="120"
            align="center"
          >
            <template #default="{ row }">
              <span v-if="row.cells[d.value]" :class="cellClass(row.cells[d.value])">
                余 {{ row.cells[d.value].remaining }}
              </span>
              <span v-else class="no-data">—</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="legend">
          <span class="dot green"></span>可约
          <span class="dot orange"></span>紧张（≤3）
          <span class="dot red"></span>已满
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi } from '@/api'
import type { Doctor, ScheduleItem } from '@/types'
import { UserFilled, Star, OfficeBuilding, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const doctor = ref<Doctor | null>(null)
const schedule = ref<ScheduleItem[]>([])
const loading = ref(false)
const scheduleLoading = ref(false)

// 排班矩阵：行=时段，列=日期
interface MatrixRow {
  timeSlot: string
  cells: Record<string, ScheduleItem | undefined>
}

const dateColumns = computed(() => {
  // 取排班数据里出现过的日期，按时间排序
  const set = new Set<string>()
  schedule.value.forEach(s => set.add(s.date))
  return Array.from(set).sort().map(v => {
    const d = new Date(v)
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    return {
      value: v,
      label: `${mm}-${dd}\n${weekdays[d.getDay()]}`
    }
  })
})

const scheduleMatrix = computed<MatrixRow[]>(() => {
  // 收集所有时段
  const slots = new Set<string>()
  schedule.value.forEach(s => slots.add(s.timeSlot))
  return Array.from(slots).sort().map(slot => {
    const cells: Record<string, ScheduleItem | undefined> = {}
    dateColumns.value.forEach(d => {
      cells[d.value] = schedule.value.find(s => s.date === d.value && s.timeSlot === slot)
    })
    return { timeSlot: slot, cells }
  })
})

const cellClass = (s: ScheduleItem) => ({
  'cell-green': s.remaining > 3,
  'cell-orange': s.remaining > 0 && s.remaining <= 3,
  'cell-red': s.remaining === 0
})

const loadData = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    doctor.value = await doctorApi.detail(id)
    loading.value = false
    scheduleLoading.value = true
    try {
      // 不传 date，拿未来 7 天排班
      schedule.value = await doctorApi.schedule(id)
    } catch {
      schedule.value = []
    } finally {
      scheduleLoading.value = false
    }
  } finally {
    loading.value = false
  }
}

const goBooking = () => {
  if (!doctor.value) return
  router.push({
    path: '/booking',
    query: { doctorId: doctor.value.id }
  })
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.doc-header { display: flex; align-items: center; gap: 20px; }
.doc-main { flex: 1; }
.doc-main h2 { font-size: 24px; margin-bottom: 8px; }
.dept { color: var(--tx-text-light); margin-top: 4px; font-size: 14px; display: flex; align-items: center; gap: 4px; }
.fee { text-align: center; padding: 0 20px; border-left: 1px solid var(--tx-border); }
.fee-num { font-size: 26px; color: var(--tx-primary); font-weight: 700; }
.fee-label { color: var(--tx-text-light); font-size: 13px; }
.section { margin-bottom: 16px; }
.label { color: var(--tx-text-light); font-size: 13px; margin-bottom: 4px; }
.value { color: var(--tx-text); }
.intro-text { white-space: pre-wrap; line-height: 1.8; }
.action-bar { margin-top: 16px; text-align: center; }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.no-data { color: #ccc; }
.cell-green { color: var(--tx-success); font-weight: 600; }
.cell-orange { color: var(--tx-warning); font-weight: 600; }
.cell-red { color: var(--tx-danger); font-weight: 600; }
.legend { margin-top: 12px; font-size: 12px; color: var(--tx-text-light); display: flex; gap: 16px; align-items: center; }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 4px; }
.dot.green { background: var(--tx-success); }
.dot.orange { background: var(--tx-warning); }
.dot.red { background: var(--tx-danger); }
:deep(.el-table__cell) { white-space: pre-line; }
</style>
