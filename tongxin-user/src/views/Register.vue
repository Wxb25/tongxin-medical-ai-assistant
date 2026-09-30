<template>
  <div class="auth-page">
    <div class="auth-bg"></div>
    <div class="auth-card">
      <div class="brand">
        <el-icon :size="40" color="#fff"><FirstAidKit /></el-icon>
        <h1>用户注册</h1>
        <p>加入同心医院智能助手</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="4-20 位字符" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="6-20 位字符" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width:100%" @click="handleRegister">注 册</el-button>
      </el-form>
      <div class="footer-tip">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import { userApi } from '@/api'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '', password: '', realName: '', phone: ''
})
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '4-20 位字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '6-20 位字符', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userApi.register(form)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.auth-page {
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.auth-bg {
  position: absolute; inset: 0;
  background:
    linear-gradient(135deg, rgba(28,122,217,0.85), rgba(0,179,164,0.8)),
    url('https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=1600') center/cover;
}
.auth-card {
  position: relative;
  width: 440px;
  background: #fff;
  border-radius: 16px;
  padding: 36px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.25);
  z-index: 1;
}
.brand {
  text-align: center;
  margin-bottom: 24px;
  h1 {
    font-size: 24px;
    margin: 8px 0 4px;
    background: linear-gradient(135deg, #1c7ad9, #00b3a4);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  p { color: #6b7a90; font-size: 13px; }
}
.footer-tip {
  text-align: center;
  margin-top: 18px;
  font-size: 13px;
  color: #6b7a90;
  a { color: var(--tx-primary); }
}
</style>
