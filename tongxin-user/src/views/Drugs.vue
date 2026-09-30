<template>
  <div class="drugs">
    <div class="tx-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="药品名称" clearable style="width:240px" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="query.category" placeholder="如：感冒、消炎" clearable style="width:160px" />
        </el-form-item>
        <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>搜索</el-button>
      </el-form>
    </div>

    <div class="tx-card" style="margin-top:16px">
      <el-row :gutter="16">
        <el-col :span="8" v-for="drug in list" :key="drug.id">
          <el-card class="drug-card" shadow="hover">
            <div class="drug-icon"><el-icon :size="36" color="#f0a020"><Medal /></el-icon></div>
            <div class="drug-name">{{ drug.name }}</div>
            <div class="drug-cat">{{ drug.category }}</div>
            <div class="drug-desc">{{ drug.description || drug.specification }}</div>
            <div class="drug-footer">
              <span class="price">¥{{ drug.price }}</span>
              <el-button size="small" type="primary" link @click="showDetail(drug)">详情</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && list.length === 0" description="请输入关键词搜索药品" />
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

    <el-dialog v-model="detailVisible" :title="current?.name" width="500px">
      <div class="detail" v-if="current">
        <div class="row"><span>分类</span>{{ current.category }}</div>
        <div class="row" v-if="current.manufacturer"><span>生产厂家</span>{{ current.manufacturer }}</div>
        <div class="row" v-if="current.specification"><span>规格</span>{{ current.specification }}</div>
        <div class="row"><span>价格</span><span class="price">¥{{ current.price }}</span></div>
        <div class="row" v-if="current.description"><span>说明</span><span class="desc">{{ current.description }}</span></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { drugApi } from '@/api'
import type { Drug } from '@/types'
import { Medal, Search } from '@element-plus/icons-vue'

const query = reactive({ keyword: '', category: '', pageNum: 1, pageSize: 9 })
const list = ref<Drug[]>([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const current = ref<Drug | null>(null)

const loadData = async () => {
  if (!query.keyword.trim()) {
    list.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const res = await drugApi.search(query)
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

const showDetail = async (drug: Drug) => {
  current.value = await drugApi.detail(drug.id)
  detailVisible.value = true
}
</script>

<style lang="scss" scoped>
.drug-card { margin-bottom: 16px; border-radius: 10px; border: 1px solid var(--tx-border); }
.drug-icon { text-align: center; margin-bottom: 10px; }
.drug-name { font-size: 16px; font-weight: 600; text-align: center; }
.drug-cat { text-align: center; color: var(--tx-text-light); font-size: 12px; margin: 4px 0 8px; }
.drug-desc { color: var(--tx-text-light); font-size: 13px; min-height: 36px; margin-bottom: 10px; }
.drug-footer { display: flex; justify-content: space-between; align-items: center; }
.price { color: var(--tx-danger); font-weight: 600; font-size: 16px; }
.detail .row { display: flex; padding: 8px 0; border-bottom: 1px dashed var(--tx-border); }
.detail .row span:first-child { width: 90px; color: var(--tx-text-light); }
.detail .desc { flex: 1; color: var(--tx-text); white-space: pre-wrap; }
</style>
