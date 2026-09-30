<template>
  <div class="booking-page">
    <!-- 步骤条 -->
    <el-steps :active="step" finish-status="success" class="tx-card" style="margin-bottom:16px">
      <el-step title="选择科室" />
      <el-step title="选择日期" />
      <el-step title="选择医生" />
      <el-step title="确认时段" />
    </el-steps>

    <!-- 步骤 1：选科室 -->
    <div class="tx-card" v-show="step >= 0">
      <div class="section-title">
        <el-icon><OfficeBuilding /></el-icon> 选择科室
        <span class="selected" v-if="selectedDept">已选：{{ selectedDept }}</span>
      </div>
      <div class="dept-grid">
        <div
          v-for="d in departments"
          :key="d"
          class="dept-item"
          :class="{ active: selectedDept === d }"
          @click="selectDept(d)"
        >
          <el-icon><FirstAidKit /></el-icon>
          <span>{{ d }}</span>
        </div>
        <div v-if="departments.length === 0 && !loadingDept" class="empty-tip">
          暂无科室数据，请先确保数据库中有医生记录
        </div>
      </div>
    </div>

    <!-- 步骤 2：选日期 -->
    <div class="tx-card" v-show="step >= 1 && selectedDept" style="margin-top:16px">
      <div class="section-title">
        <el-icon><Calendar /></el-icon> 选择日期
        <span class="hint">（最多可提前 7 天预约）</span>
      </div>
      <div class="date-grid">
        <div
          v-for="d in dateOptions"
          :key="d.value"
          class="date-item"
          :class="{ active: selectedDate === d.value }"
          @click="selectDate(d.value)"
        >
          <div class="weekday">{{ d.weekday }}</div>
          <div class="date">{{ d.label }}</div>
          <div class="tag" v-if="d.value === today">今天</div>
        </div>
      </div>
    </div>

    <!-- 步骤 3：医生列表 -->
    <div class="tx-card" v-show="step >= 2 && selectedDept && selectedDate" style="margin-top:16px">
      <div class="section-title">
        <el-icon><UserFilled /></el-icon> 可预约医生
        <span class="hint">（{{ selectedDept }} · {{ selectedDate }}）</span>
        <el-button link type="primary" @click="refreshDoctors" style="margin-left:auto">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
      <div v-loading="loadingDoctors">
        <el-row :gutter="16" v-if="doctors.length > 0">
          <el-col :span="8" v-for="doc in doctors" :key="doc.id">
            <el-card class="doc-card" shadow="hover">
              <div class="doc-top">
                <el-avatar :size="56" :icon="UserFilled" />
                <div class="doc-info">
                  <div class="doc-name">{{ doc.name }}
                    <el-tag size="small" type="success">{{ doc.title }}</el-tag>
                  </div>
                  <div class="doc-specialty">{{ doc.specialty }}</div>
                  <div class="doc-meta">
                    <span>¥{{ doc.consultationFee }}</span>
                  </div>
                </div>
              </div>
              <el-button type="primary" style="width:100%" @click="openDoctor(doc)">
                <el-icon><Clock /></el-icon>预约
              </el-button>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="该科室当日无可预约医生" />
      </div>
    </div>

    <!-- 步骤 4：时段预约弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`预约挂号 - ${currentDoctor?.name || ''}`"
      width="640px"
      @close="onDialogClose"
    >
      <div v-loading="loadingSchedule">
        <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="医生">{{ currentDoctor?.name }}（{{ currentDoctor?.title }}）</el-descriptions-item>
          <el-descriptions-item label="科室">{{ currentDoctor?.department }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ selectedDate }}</el-descriptions-item>
          <el-descriptions-item label="挂号费">¥{{ currentDoctor?.consultationFee }}</el-descriptions-item>
        </el-descriptions>

        <div class="slots-section">
          <div class="slot-header">
            <el-icon><Sunny /></el-icon> 上午（08:00 - 12:00）
          </div>
          <div class="slot-grid">
            <div
              v-for="s in morningSlots"
              :key="s.time"
              class="slot-item"
              :class="slotClass(s)"
              @click="selectSlot(s)"
            >
              <div class="time">{{ s.time }}</div>
              <div class="remain">
                <span v-if="s.available">余 {{ s.remaining }}</span>
                <span v-else>无号</span>
              </div>
            </div>
          </div>
        </div>

        <div class="slots-section">
          <div class="slot-header">
            <el-icon><Moon /></el-icon> 下午（14:00 - 17:30）
          </div>
          <div class="slot-grid">
            <div
              v-for="s in afternoonSlots"
              :key="s.time"
              class="slot-item"
              :class="slotClass(s)"
              @click="selectSlot(s)"
            >
              <div class="time">{{ s.time }}</div>
              <div class="remain">
                <span v-if="s.available">余 {{ s.remaining }}</span>
                <span v-else>无号</span>
              </div>
            </div>
          </div>
        </div>

        <el-form :model="appointForm" label-width="80px" style="margin-top:16px">
          <el-form-item label="已选时段">
            <el-tag type="success" v-if="appointForm.appointmentTime">
              {{ appointForm.appointmentTime }}
            </el-tag>
            <span v-else class="hint">请点击上方时段</span>
          </el-form-item>
          <el-form-item label="症状描述">
            <el-input
              v-model="appointForm.symptom"
              type="textarea"
              :rows="3"
              placeholder="请描述您的症状（可选）"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="!appointForm.appointmentTime"
          @click="submitAppoint"
        >
          确认预约
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi, appointmentApi } from '@/api'
import type { Doctor, ScheduleItem } from '@/types'
import {
  UserFilled, Star, Calendar, OfficeBuilding, FirstAidKit,
  Refresh, Clock, Sunny, Moon
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// ============ 步骤状态 ============
const step = ref(0)
const selectedDept = ref('')
const selectedDate = ref('')
const today = new Date().toISOString().slice(0, 10)

// ============ 科室数据 ============
const departments = ref<string[]>([])
const loadingDept = ref(false)
const loadDepartments = async () => {
  loadingDept.value = true
  try {
    // 拉一次大列表，按 department 去重
    const res = await doctorApi.list({ pageNum: 1, pageSize: 200 })
    const set = new Set<string>()
    res.records.forEach(d => { if (d.department) set.add(d.department) })
    departments.value = Array.from(set).sort()
  } finally {
    loadingDept.value = false
  }
}
const selectDept = (d: string) => {
  selectedDept.value = d
  step.value = 1
  // 切换科室时重置后续选择
  selectedDate.value = today
  step.value = 2
  loadDoctors()
}

// ============ 日期选项 ============
const dateOptions = computed(() => {
  const list: { value: string; label: string; weekday: string }[] = []
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const now = new Date()
  for (let i = 0; i < 7; i++) {
    const d = new Date(now)
    d.setDate(now.getDate() + i)
    const v = d.toISOString().slice(0, 10)
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    list.push({
      value: v,
      label: `${mm}-${dd}`,
      weekday: weekdays[d.getDay()]
    })
  }
  return list
})
const selectDate = (v: string) => {
  selectedDate.value = v
  step.value = 2
  loadDoctors()
}

// ============ 医生列表 ============
const doctors = ref<Doctor[]>([])
const loadingDoctors = ref(false)
const loadDoctors = async () => {
  if (!selectedDept.value) return
  loadingDoctors.value = true
  try {
    const res = await doctorApi.list({
      department: selectedDept.value,
      pageNum: 1,
      pageSize: 50
    })
    // 按 id 去重，防止数据库中同一医生有多条记录导致重复显示
    const map = new Map<number, Doctor>()
    res.records.forEach(d => { if (!map.has(d.id)) map.set(d.id, d) })
    doctors.value = Array.from(map.values())
  } finally {
    loadingDoctors.value = false
  }
}
const refreshDoctors = () => loadDoctors()

// ============ 时段弹窗 ============
const dialogVisible = ref(false)
const currentDoctor = ref<Doctor | null>(null)
const loadingSchedule = ref(false)
const scheduleData = ref<ScheduleItem[]>([])
const submitting = ref(false)

// 规则生成所有时段
interface SlotInfo {
  time: string       // 格式 HH:mm-HH:mm
  start: string
  end: string
  available: boolean
  remaining: number
  total: number
}

const buildSlots = (startH: number, startM: number, endH: number, endM: number): SlotInfo[] => {
  const result: SlotInfo[] = []
  let cur = new Date()
  cur.setHours(startH, startM, 0, 0)
  const end = new Date()
  end.setHours(endH, endM, 0, 0)
  while (true) {
    const s = new Date(cur)
    const e = new Date(cur)
    e.setMinutes(e.getMinutes() + 30)
    if (e > end) break
    const fmt = (d: Date) =>
      `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    const startStr = fmt(s)
    const endStr = fmt(e)
    result.push({
      time: `${startStr}-${endStr}`,
      start: startStr,
      end: endStr,
      available: false,
      remaining: 0,
      total: 0
    })
    cur = e
  }
  return result
}

const morningSlots = ref<SlotInfo[]>([])
const afternoonSlots = ref<SlotInfo[]>([])

const appointForm = reactive({
  doctorId: 0,
  appointmentDate: '',
  appointmentTime: '',
  symptom: ''
})

const openDoctor = async (doc: Doctor) => {
  currentDoctor.value = doc
  appointForm.doctorId = doc.id
  appointForm.appointmentDate = selectedDate.value
  appointForm.appointmentTime = ''
  appointForm.symptom = ''
  dialogVisible.value = true
  loadingSchedule.value = true

  // 生成规则时段
  morningSlots.value = buildSlots(8, 0, 12, 0)
  afternoonSlots.value = buildSlots(14, 0, 17, 30)

  try {
    // 拉该医生该日期的排班，将号源映射到时段
    const res = await doctorApi.schedule(doc.id, selectedDate.value)
    scheduleData.value = res || []
    const map = new Map<string, ScheduleItem>()
    scheduleData.value.forEach(s => map.set(s.timeSlot, s))
    const apply = (arr: SlotInfo[]) => {
      arr.forEach(slot => {
        const s = map.get(slot.time)
        if (s) {
          slot.available = s.remaining > 0
          slot.remaining = s.remaining
          slot.total = s.total
        } else {
          // 数据库无此时段，按"无号"处理
          slot.available = false
          slot.remaining = 0
          slot.total = 0
        }
      })
    }
    apply(morningSlots.value)
    apply(afternoonSlots.value)
  } catch (e: any) {
    ElMessage.warning('排班查询失败：' + (e?.message || ''))
  } finally {
    loadingSchedule.value = false
  }
}

const slotClass = (s: SlotInfo) => ({
  active: appointForm.appointmentTime === s.time,
  disabled: !s.available
})

const selectSlot = (s: SlotInfo) => {
  if (!s.available) {
    ElMessage.warning('该时段已无号源')
    return
  }
  appointForm.appointmentTime = s.time
}

const onDialogClose = () => {
  appointForm.appointmentTime = ''
  currentDoctor.value = null
}

const submitAppoint = async () => {
  if (!appointForm.appointmentTime) {
    ElMessage.warning('请先选择时段')
    return
  }
  submitting.value = true
  try {
    await appointmentApi.create(appointForm)
    ElMessage.success('预约成功！请到「我的预约」查看')
    dialogVisible.value = false
    step.value = 3
    // 跳转到我的预约
    setTimeout(() => router.push('/appointments'), 800)
  } catch (e: any) {
    ElMessage.error(e?.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  selectedDate.value = today
  await loadDepartments()
  // 支持从医生详情页跳转过来：?doctorId=xxx
  // 只预选该医生所在科室并展示医生卡片，不自动弹窗；
  // 用户需要自己选日期，再点医生的「预约」按钮才会弹时段
  const qDoctorId = route.query.doctorId
  if (qDoctorId) {
    try {
      const doc = await doctorApi.detail(Number(qDoctorId))
      if (doc) {
        selectedDept.value = doc.department || ''
        step.value = 2
        // 加载该科室的医生列表（其中包含跳过来的这个医生）
        await loadDoctors()
        // 若该医生未在列表（例如状态非1），单独补进去
        if (!doctors.value.find(d => d.id === doc.id)) {
          doctors.value = [doc, ...doctors.value]
        }
      }
    } catch (e: any) {
      ElMessage.error('医生信息加载失败')
    }
  }
})
</script>

<style lang="scss" scoped>
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  .hint { font-size: 12px; color: var(--tx-text-light); font-weight: normal; }
  .selected { font-size: 13px; color: var(--tx-primary); font-weight: normal; }
}
.dept-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}
.dept-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border: 1px solid var(--tx-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  color: var(--tx-text-secondary);
  &:hover { border-color: var(--tx-primary); color: var(--tx-primary); background: var(--tx-primary-bg); }
  &.active {
    border-color: var(--tx-primary);
    background: var(--tx-primary-bg);
    color: var(--tx-primary);
    font-weight: 500;
  }
}
.empty-tip { color: var(--tx-text-light); padding: 20px; text-align: center; }
.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
}
.date-item {
  padding: 12px 8px;
  border: 1px solid var(--tx-border);
  border-radius: 10px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  position: relative;
  &:hover { border-color: var(--tx-primary); }
  &.active {
    border-color: var(--tx-primary);
    background: var(--tx-primary-bg);
    color: var(--tx-primary);
    font-weight: 500;
  }
  .weekday { font-size: 12px; color: var(--tx-text-light); }
  &.active .weekday { color: var(--tx-primary); }
  .date { font-size: 16px; font-weight: 600; margin-top: 2px; }
  .tag {
    position: absolute;
    top: -6px; right: -6px;
    background: var(--tx-danger);
    color: #fff;
    font-size: 10px;
    padding: 1px 6px;
    border-radius: 8px;
  }
}
.doc-card {
  border-radius: 10px;
  border: 1px solid var(--tx-border);
  margin-bottom: 16px;
}
.doc-top { display: flex; gap: 12px; margin-bottom: 12px; }
.doc-info { flex: 1; }
.doc-name { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.doc-specialty { color: var(--tx-text-light); font-size: 13px; min-height: 36px; }
.doc-meta { display: flex; gap: 10px; align-items: center; font-size: 13px; color: var(--tx-text-light); }
.slots-section { margin-bottom: 16px; }
.slot-header {
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--tx-text);
  display: flex;
  align-items: center;
  gap: 6px;
}
.slot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 8px;
}
.slot-item {
  padding: 10px 8px;
  border: 1px solid var(--tx-border);
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;
  background: #fff;
  .time { font-size: 13px; font-weight: 500; color: var(--tx-text); }
  .remain { font-size: 11px; color: var(--tx-success); margin-top: 3px; }
  &:hover:not(.disabled) { border-color: var(--tx-primary); background: var(--tx-primary-bg); }
  &.active {
    border-color: var(--tx-primary);
    background: var(--tx-primary-bg);
    .time { color: var(--tx-primary); font-weight: 600; }
    .remain { color: var(--tx-primary); }
  }
  &.disabled {
    background: var(--tx-border-light);
    cursor: not-allowed;
    .time { color: var(--tx-text-light); }
    .remain { color: var(--tx-text-light); }
  }
}
</style>
