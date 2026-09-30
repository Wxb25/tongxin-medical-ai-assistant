<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="login-icon"><el-icon :size="22"><FirstAidKit /></el-icon></div>
        <h2>同心医院管理后台</h2>
        <p>管理员登录</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="doLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock" show-password @keyup.enter="doLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%;margin-top:8px" :loading="loading" @click="doLogin">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, FirstAidKit } from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const doLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await adminStore.login(form.username, form.password)
      ElMessage.success('登录成功')
      router.push('/')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--tx-bg);
}
.login-card {
  width: 380px;
  background: #fff;
  border: 1px solid var(--tx-border-light);
  border-radius: 14px;
  padding: 40px 32px;
  box-shadow: var(--tx-shadow-hover);
}
.login-header {
  text-align: center;
  margin-bottom: 28px;
}
.login-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--tx-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
}
.login-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--tx-text);
  margin-bottom: 4px;
}
.login-header p {
  color: var(--tx-text-light);
  font-size: 13px;
}
</style>
