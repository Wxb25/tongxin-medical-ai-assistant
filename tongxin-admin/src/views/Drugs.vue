<template>
  <div class="drugs">
    <div class="tx-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索药品名称/通用名" clearable style="width:220px" @keyup.enter="loadData" />
        <el-input v-model="query.category" placeholder="分类" clearable style="width:140px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
        <el-button type="success" @click="openDialog()"><el-icon><Plus /></el-icon>新增药品</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="药品名称" width="140" />
        <el-table-column prop="genericName" label="通用名" width="140" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column prop="specification" label="规格" width="110" />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="manufacturer" label="生产厂家" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="90" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="removeDrug(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑药品' : '新增药品'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="药品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="通用名">
          <el-input v-model="form.genericName" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如：抗生素、感冒药" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.specification" placeholder="如：0.25g*24片" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如：盒、瓶" />
        </el-form-item>
        <el-form-item label="生产厂家">
          <el-input v-model="form.manufacturer" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="用药说明">
          <el-input v-model="form.instruction" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="副作用">
          <el-input v-model="form.sideEffects" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
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
import { drugApi } from '@/api'
import type { Drug } from '@/types'

const query = reactive({ keyword: '', category: '', pageNum: 1, pageSize: 10 })
const list = ref<Drug[]>([])
const total = ref(0)
const loading = ref(false)

const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const form = reactive<Drug>({
  id: 0, name: '', genericName: '', category: '', specification: '', unit: '',
  manufacturer: '', price: 0, stock: 0, instruction: '', sideEffects: '', status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await drugApi.list(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const onPageChange = (p: number) => { query.pageNum = p; loadData() }

const openDialog = (row?: Drug) => {
  editId.value = row?.id ?? null
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: 0, name: '', genericName: '', category: '', specification: '', unit: '', manufacturer: '', price: 0, stock: 0, instruction: '', sideEffects: '', status: 1 })
  }
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.name) {
    ElMessage.warning('药品名称必填')
    return
  }
  submitting.value = true
  try {
    if (editId.value) {
      await drugApi.update(editId.value, form)
      ElMessage.success('编辑成功')
    } else {
      await drugApi.add(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: Drug) => {
  await drugApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  loadData()
}

const removeDrug = (row: Drug) => {
  ElMessageBox.confirm(`确定删除药品「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await drugApi.remove(row.id)
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
