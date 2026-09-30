<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand">
        <div class="brand-icon"><el-icon :size="24"><FirstAidKit /></el-icon></div>
        <h1>同心医院</h1>
        <p>智能医疗服务平台</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-button type="primary" :loading="loading" size="large" style="width:100%;margin-top:8px" @click="handleLogin">登 录</el-button>
      </el-form>
      <div class="footer-tip">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { userApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await userApi.login(form)
      userStore.setToken(res.token)
      userStore.setUserInfo(res.userInfo)
      ElMessage.success('登录成功')
      router.push('/home')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.auth-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--tx-bg);
}
.auth-card {
  width: 400px;
  background: #fff;
  border: 1px solid var(--tx-border-light);
  border-radius: 14px;
  padding: 40px 36px;
  box-shadow: var(--tx-shadow-hover);
}
.brand {
  text-align: center;
  margin-bottom: 28px;
}
.brand-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--tx-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
}
.brand h1 {
  font-size: 22px;
  font-weight: 600;
  color: var(--tx-text);
  margin-bottom: 4px;
}
.brand p {
  color: var(--tx-text-light);
  font-size: 13px;
}
.footer-tip {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: var(--tx-text-light);
  a { color: var(--tx-primary); text-decoration: none; }
}
</style>
