import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
      { path: 'doctors', name: 'Doctors', component: () => import('@/views/Doctors.vue'), meta: { title: '医生列表' } },
      { path: 'doctors/:id', name: 'DoctorDetail', component: () => import('@/views/DoctorDetail.vue'), meta: { title: '医生详情' } },
      { path: 'booking', name: 'AppointmentBooking', component: () => import('@/views/AppointmentBooking.vue'), meta: { title: '预约挂号' } },
      { path: 'appointments', name: 'Appointments', component: () => import('@/views/Appointments.vue'), meta: { title: '我的预约' } },
      { path: 'drugs', name: 'Drugs', component: () => import('@/views/Drugs.vue'), meta: { title: '药品查询' } },
      { path: 'chat', name: 'Chat', component: () => import('@/views/Chat.vue'), meta: { title: 'AI 助手' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.public) {
    next()
  } else if (!userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
