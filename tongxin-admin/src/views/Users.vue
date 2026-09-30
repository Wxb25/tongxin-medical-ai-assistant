<template>
  <div class="users">
    <div class="tx-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索用户名/姓名/手机号" clearable style="width:260px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="row.role === 2 ? 'danger' : row.role === 1 ? 'warning' : ''">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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

    <el-dialog v-model="dialogVisible" title="编辑用户" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input :model-value="form.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
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
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import type { User } from '@/types'

const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const list = ref<User[]>([])
const total = ref(0)
const loading = ref(false)

const dialogVisible = ref(false)
const submitting = ref(false)
const form = reactive<User>({
  id: 0, username: '', realName: '', phone: '', idCard: '', gender: 0, role: 0, status: 1
})

const genderText = (g?: number) => g === 1 ? '男' : g === 2 ? '女' : '未知'
const roleText = (r: number) => r === 2 ? '管理员' : r === 1 ? '医生' : '患者'

const loadData = async () => {
  loading.value = true
  try {
    const res = await userApi.list(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const onPageChange = (p: number) => { query.pageNum = p; loadData() }

const openDialog = (row: User) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const submit = async () => {
  submitting.value = true
  try {
    await userApi.update(form.id, form)
    ElMessage.success('编辑成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: User) => {
  await userApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; align-items: center; }
</style>
