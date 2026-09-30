<template>
  <div class="profile">
    <div class="tx-card">
      <div class="profile-header">
        <el-avatar :size="80" :icon="UserFilled" />
        <div>
          <h2>{{ userInfo?.realName || '用户' }}</h2>
          <p>{{ userInfo?.username }}</p>
        </div>
      </div>
      <el-descriptions :column="2" border style="margin-top:20px">
        <el-descriptions-item label="ID">{{ userInfo?.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo?.phone }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          {{ userInfo?.role === 0 ? '普通用户' : '管理员' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { UserFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
</script>

<style lang="scss" scoped>
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  h2 { font-size: 22px; margin-bottom: 4px; }
  p { color: var(--tx-text-light); }
}
</style>
