<template>
  <div class="home">
    <!-- 欢迎区 -->
    <div class="banner">
      <div class="banner-left">
        <div class="banner-greeting">你好，{{ userName }}</div>
        <h2 class="banner-title">今天有什么可以帮您？</h2>
        <p class="banner-desc">同心医院智能助手，为您提供在线问诊、预约挂号、医生查询服务</p>
        <div class="banner-actions">
          <el-button type="primary" size="large" @click="$router.push('/booking')">
            <el-icon><Calendar /></el-icon>立即挂号
          </el-button>
          <el-button size="large" @click="$router.push('/chat')">
            <el-icon><ChatDotRound /></el-icon>AI 问诊
          </el-button>
        </div>
      </div>
      <div class="banner-right">
        <div class="banner-deco">
          <el-icon :size="96" color="#2563eb"><FirstAidKit /></el-icon>
        </div>
      </div>
    </div>

    <!-- 数据概览 -->
    <el-row :gutter="16" class="stats">
      <el-col :span="6" v-for="item in stats" :key="item.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: item.bg, color: item.color }">
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <div class="section">
      <div class="section-header">
        <div class="section-title">快捷服务</div>
      </div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="entry in entries" :key="entry.name">
          <div class="entry" @click="$router.push(entry.path)">
            <div class="entry-icon" :style="{ background: entry.bg, color: entry.color }">
              <el-icon :size="22"><component :is="entry.icon" /></el-icon>
            </div>
            <div class="entry-name">{{ entry.name }}</div>
            <div class="entry-desc">{{ entry.desc }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 推荐医生 -->
    <div class="section">
      <div class="section-header">
        <div class="section-title">推荐医生</div>
        <el-link type="primary" :underline="false" @click="$router.push('/doctors')">查看全部</el-link>
      </div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="doc in doctors" :key="doc.id" @click="$router.push(`/doctors/${doc.id}`)">
          <div class="doc-card">
            <div class="doc-top">
              <el-avatar :size="52" :icon="UserFilled" class="doc-avatar" />
              <div class="doc-info">
                <div class="doc-name">
                  {{ doc.name }}
                  <el-tag size="small" effect="plain">{{ doc.title }}</el-tag>
                </div>
                <div class="doc-dept">{{ doc.department }}</div>
              </div>
            </div>
            <div class="doc-specialty">{{ doc.specialty }}</div>
            <div class="doc-footer">
              <span class="doc-fee">挂号费 ¥{{ doc.consultationFee }}</span>
              <el-button type="primary" link size="small">查看详情</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { doctorApi } from '@/api'
import type { Doctor } from '@/types'
import { UserFilled, Calendar, Medal, Files, ChatDotRound, FirstAidKit, Clock, Search } from '@element-plus/icons-vue'

const userStore = useUserStore()
const doctors = ref<Doctor[]>([])

const userName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '用户')

const stats = [
  { label: '今日出诊医生', value: 12, icon: UserFilled, bg: '#eff6ff', color: '#2563eb' },
  { label: '我的预约', value: 0, icon: Calendar, bg: '#f0fdfa', color: '#0d9488' },
  { label: '药品种类', value: 86, icon: Medal, bg: '#fffbeb', color: '#d97706' },
  { label: '知识文档', value: 24, icon: Files, bg: '#fef2f2', color: '#dc2626' }
]

const entries = [
  { name: '预约挂号', desc: '在线选择医生时段', icon: Calendar, bg: '#eff6ff', color: '#2563eb', path: '/booking' },
  { name: '我的预约', desc: '查看与管理预约', icon: Clock, bg: '#f0fdfa', color: '#0d9488', path: '/appointments' },
  { name: '药品查询', desc: '药品信息检索', icon: Search, bg: '#fffbeb', color: '#d97706', path: '/drugs' },
  { name: 'AI 助手', desc: '智能健康咨询', icon: ChatDotRound, bg: '#f5f3ff', color: '#7c3aed', path: '/chat' }
]

onMounted(async () => {
  try {
    const res = await doctorApi.list({ pageNum: 1, pageSize: 6 })
    doctors.value = res.records.slice(0, 6)
  } catch { /* ignore */ }
})
</script>

<style lang="scss" scoped>
.home {
  width: 100%;
}

/* 欢迎区 */
.banner {
  background: #fff;
  border: 1px solid var(--tx-border-light);
  border-radius: var(--tx-radius);
  padding: 32px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  box-shadow: var(--tx-shadow);
}
.banner-greeting {
  font-size: 14px;
  color: var(--tx-text-secondary);
  margin-bottom: 6px;
}
.banner-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--tx-text);
  margin-bottom: 8px;
}
.banner-desc {
  font-size: 14px;
  color: var(--tx-text-light);
  margin-bottom: 20px;
}
.banner-actions {
  display: flex;
  gap: 12px;
}
.banner-right {
  flex-shrink: 0;
}
.banner-deco {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 数据概览 */
.stats {
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border: 1px solid var(--tx-border-light);
  border-radius: var(--tx-radius);
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--tx-shadow);
  transition: box-shadow 0.2s;
  &:hover {
    box-shadow: var(--tx-shadow-hover);
  }
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-num {
  font-size: 22px;
  font-weight: 600;
  color: var(--tx-text);
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: var(--tx-text-light);
  margin-top: 2px;
}

/* 通用区块 */
.section {
  background: #fff;
  border: 1px solid var(--tx-border-light);
  border-radius: var(--tx-radius);
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: var(--tx-shadow);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--tx-text);
}

/* 快捷入口 */
.entry {
  background: #fff;
  border: 1px solid var(--tx-border-light);
  border-radius: var(--tx-radius-sm);
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
  &:hover {
    border-color: var(--tx-primary);
    box-shadow: var(--tx-shadow-hover);
    transform: translateY(-2px);
  }
}
.entry-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
}
.entry-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--tx-text);
  margin-bottom: 4px;
}
.entry-desc {
  font-size: 12px;
  color: var(--tx-text-light);
}

/* 医生卡片 */
.doc-card {
  background: #fff;
  border: 1px solid var(--tx-border-light);
  border-radius: var(--tx-radius-sm);
  padding: 18px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 16px;
  &:hover {
    border-color: var(--tx-primary);
    box-shadow: var(--tx-shadow-hover);
  }
}
.doc-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.doc-avatar {
  background: var(--tx-primary-bg);
  color: var(--tx-primary);
}
.doc-info {
  flex: 1;
  min-width: 0;
}
.doc-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--tx-text);
  display: flex;
  align-items: center;
  gap: 8px;
}
.doc-dept {
  font-size: 13px;
  color: var(--tx-primary);
  margin-top: 3px;
}
.doc-specialty {
  font-size: 13px;
  color: var(--tx-text-secondary);
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.doc-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--tx-border-light);
}
.doc-fee {
  font-size: 14px;
  font-weight: 600;
  color: var(--tx-text);
}
</style>
