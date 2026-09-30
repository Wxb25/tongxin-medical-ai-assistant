<template>
  <div class="main-layout">
    <el-container class="layout-container">
      <el-aside width="220px" class="aside">
        <div class="logo">
          <div class="logo-icon">
            <el-icon :size="18"><FirstAidKit /></el-icon>
          </div>
          <span class="logo-text">同心医院后台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="transparent"
          text-color="#475569"
          active-text-color="#2563eb"
        >
          <el-menu-item index="/doctors">
            <el-icon><UserFilled /></el-icon><span>医生管理</span>
          </el-menu-item>
          <el-menu-item index="/appointments">
            <el-icon><Calendar /></el-icon><span>挂号管理</span>
          </el-menu-item>
          <el-menu-item index="/drugs">
            <el-icon><Medal /></el-icon><span>药品管理</span>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><Avatar /></el-icon><span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/knowledge">
            <el-icon><Files /></el-icon><span>知识库管理</span>
          </el-menu-item>
        </el-menu>
        <div class="aside-footer">
          <div class="footer-version">管理后台 v1.0</div>
        </div>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-title">{{ currentTitle }}</div>
          <el-dropdown @command="handleCommand">
            <div class="admin-info">
              <el-avatar :size="32" :icon="UserFilled" class="admin-avatar" />
              <span class="admin-name">{{ adminStore.adminInfo?.realName || adminStore.adminInfo?.username }}</span>
              <el-icon :size="14" color="#94a3b8"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '同心医院管理后台')

const handleCommand = (cmd: string) => {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
      .then(() => {
        adminStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
.main-layout { height: 100%; }
.layout-container { height: 100%; }

/* 侧边栏 - 白色简约 */
.aside {
  background: #fff;
  border-right: 1px solid var(--tx-border-light);
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  border-bottom: 1px solid var(--tx-border-light);
}
.logo-icon {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--tx-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--tx-text);
  letter-spacing: 0.5px;
}
.el-menu {
  border-right: none !important;
  flex: 1;
  padding: 12px 10px;
}
:deep(.el-menu-item) {
  margin: 2px 0;
  border-radius: 8px;
  height: 42px;
  line-height: 42px;
  font-size: 14px;
  position: relative;
  &.is-active {
    background: var(--tx-primary-bg) !important;
    color: var(--tx-primary) !important;
    font-weight: 500;
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 18px;
      background: var(--tx-primary);
      border-radius: 0 2px 2px 0;
    }
  }
  &:hover {
    background: var(--tx-border-light) !important;
  }
}
.aside-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--tx-border-light);
}
.footer-version {
  font-size: 12px;
  color: var(--tx-text-light);
}

/* 顶部栏 */
.header {
  background: #fff;
  border-bottom: 1px solid var(--tx-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  height: 60px;
}
.header-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--tx-text);
}
.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.15s;
  &:hover { background: var(--tx-border-light); }
}
.admin-avatar {
  background: var(--tx-primary-bg);
  color: var(--tx-primary);
}
.admin-name {
  font-size: 14px;
  color: var(--tx-text-secondary);
}

/* 主内容区 */
.main {
  background: var(--tx-bg);
  padding: 24px 28px;
}
</style>
