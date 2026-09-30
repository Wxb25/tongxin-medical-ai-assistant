<template>
  <div class="doctors">
    <div class="tx-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索医生姓名/专长" clearable style="width:220px" @keyup.enter="loadData" />
        <el-input v-model="query.department" placeholder="科室" clearable style="width:140px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
        <el-button type="success" @click="openDialog()"><el-icon><Plus /></el-icon>新增医生</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="title" label="职称" width="100" />
        <el-table-column prop="specialty" label="专长" show-overflow-tooltip />
        <el-table-column prop="consultationFee" label="挂号费" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="removeDoctor(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑医生' : '新增医生'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="科室">
          <el-input v-model="form.department" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="专长">
          <el-input v-model="form.specialty" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.introduction" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="挂号费">
          <el-input-number v-model="form.consultationFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { doctorApi } from '@/api'
import type { Doctor } from '@/types'

const query = reactive({ keyword: '', department: '', pageNum: 1, pageSize: 10 })
const list = ref<Doctor[]>([])
const total = ref(0)
const loading = ref(false)

const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const form = reactive<Doctor>({
  id: 0, name: '', department: '', title: '', specialty: '',
  introduction: '', consultationFee: 0, status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await doctorApi.list(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const onPageChange = (p: number) => { query.pageNum = p; loadData() }

const openDialog = (row?: Doctor) => {
  editId.value = row?.id ?? null
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: 0, name: '', department: '', title: '', specialty: '', introduction: '', consultationFee: 0, status: 1 })
  }
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.name || !form.department) {
    ElMessage.warning('姓名和科室必填')
    return
  }
  submitting.value = true
  try {
    if (editId.value) {
      await doctorApi.update(editId.value, form)
      ElMessage.success('编辑成功')
    } else {
      await doctorApi.add(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: Doctor) => {
  await doctorApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  loadData()
}

const removeDoctor = (row: Doctor) => {
  ElMessageBox.confirm(`确定删除医生「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await doctorApi.remove(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; align-items: center; }
</style>
