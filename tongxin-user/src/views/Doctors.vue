<template>
  <div class="doctors">
    <div class="tx-card filter-card">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="科室">
          <el-input v-model="query.department" placeholder="输入科室" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="医生姓名/专长" clearable style="width:220px" @keyup.enter="loadData" />
        </el-form-item>
        <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>搜索</el-button>
      </el-form>
    </div>

    <div class="tx-card" style="margin-top:16px">
      <el-row :gutter="16">
        <el-col :span="8" v-for="doc in list" :key="doc.id">
          <el-card class="doc-card" shadow="hover" @click="$router.push(`/doctors/${doc.id}`)">
            <div class="doc-top">
              <el-avatar :size="60" :icon="UserFilled" class="doc-avatar" />
              <div class="doc-info">
                <div class="doc-name">{{ doc.name }}</div>
                <el-tag size="small" type="success">{{ doc.title }}</el-tag>
                <div class="doc-dept">{{ doc.department }}</div>
              </div>
            </div>
            <div class="doc-specialty">{{ doc.specialty }}</div>
            <div class="doc-meta">
              <span>挂号费 ¥{{ doc.consultationFee }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && list.length === 0" description="暂无医生数据" />
      <el-pagination
        v-if="total > 0"
        style="margin-top:20px;justify-content:flex-end"
        layout="prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { doctorApi } from '@/api'
import type { Doctor } from '@/types'
import { UserFilled, Star, Search } from '@element-plus/icons-vue'

const query = reactive({
  department: '', keyword: '', pageNum: 1, pageSize: 9
})
const list = ref<Doctor[]>([])
const total = ref(0)
const loading = ref(false)

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

const onPageChange = (p: number) => {
  query.pageNum = p
  loadData()
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.filter-card {
  margin-bottom: 0;
  padding: 18px 24px;
}
.doc-card {
  cursor: pointer;
  border-radius: var(--tx-radius-sm);
  border: 1px solid var(--tx-border-light);
  margin-bottom: 16px;
  height: 100%;
  transition: all 0.2s;
  &:hover {
    border-color: var(--tx-primary);
    box-shadow: var(--tx-shadow-hover);
    transform: translateY(-2px);
  }
  :deep(.el-card__body) {
    padding: 18px;
  }
}
.doc-top { display: flex; gap: 14px; align-items: center; margin-bottom: 12px; }
.doc-avatar {
  background: var(--tx-primary-bg);
  color: var(--tx-primary);
}
.doc-info { flex: 1; min-width: 0; }
.doc-name { font-size: 16px; font-weight: 600; margin-bottom: 4px; color: var(--tx-text); }
.doc-dept { color: var(--tx-primary); font-size: 13px; margin-top: 4px; }
.doc-specialty { color: var(--tx-text-secondary); font-size: 13px; margin-bottom: 12px; min-height: 38px; line-height: 1.5; }
.doc-meta { display: flex; justify-content: space-between; font-size: 13px; color: var(--tx-text-light); align-items: center; }
</style>
