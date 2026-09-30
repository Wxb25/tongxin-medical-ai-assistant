<template>
  <div class="knowledge">
    <div class="tx-card">
      <div class="toolbar">
        <el-select v-model="query.category" placeholder="分类" clearable style="width:140px">
          <el-option label="疾病" value="disease" />
          <el-option label="药品" value="drug" />
          <el-option label="健康" value="health" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="搜索标题" clearable style="width:220px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
        <el-button type="success" @click="uploadDialog = true"><el-icon><Upload /></el-icon>上传文档</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column label="分类" width="90">
          <template #default="{ row }">{{ categoryText(row.category) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已处理' : '处理中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="removeDoc(row)">删除</el-button>
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

    <el-dialog v-model="uploadDialog" title="上传知识文档" width="460px">
      <el-form label-width="80px">
        <el-form-item label="文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".txt,.md,.pdf"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip><div style="color:#999;font-size:12px">支持 txt / md / pdf</div></template>
          </el-upload>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="uploadForm.category" style="width:100%">
            <el-option label="疾病" value="disease" />
            <el-option label="药品" value="drug" />
            <el-option label="健康" value="health" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="uploadForm.title" placeholder="可选，默认使用文件名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="doUpload">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadInstance } from 'element-plus'
import { knowledgeApi } from '@/api'
import type { KnowledgeDoc } from '@/types'

const query = reactive({ category: '', keyword: '', pageNum: 1, pageSize: 10 })
const list = ref<KnowledgeDoc[]>([])
const total = ref(0)
const loading = ref(false)

const uploadDialog = ref(false)
const uploading = ref(false)
const uploadRef = ref<UploadInstance>()
const uploadForm = reactive({ category: 'disease', title: '', file: null as File | null })

const categoryText = (c: string) => c === 'disease' ? '疾病' : c === 'drug' ? '药品' : '健康'

const loadData = async () => {
  loading.value = true
  try {
    const res = await knowledgeApi.list(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const onPageChange = (p: number) => { query.pageNum = p; loadData() }

const handleFileChange = (file: any) => {
  uploadForm.file = file.raw
}

const doUpload = async () => {
  if (!uploadForm.file) {
    ElMessage.warning('请选择文件')
    return
  }
  if (!uploadForm.category) {
    ElMessage.warning('请选择分类')
    return
  }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadForm.file)
    fd.append('category', uploadForm.category)
    if (uploadForm.title) fd.append('title', uploadForm.title)
    await knowledgeApi.upload(fd)
    ElMessage.success('上传成功')
    uploadDialog.value = false
    uploadRef.value?.clearFiles()
    uploadForm.file = null
    loadData()
  } finally {
    uploading.value = false
  }
}

const removeDoc = (row: KnowledgeDoc) => {
  ElMessageBox.confirm(`确定删除文档「${row.title}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await knowledgeApi.remove(row.docId)
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
