import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/doctors',
    children: [
      { path: 'doctors', name: 'Doctors', component: () => import('@/views/Doctors.vue'), meta: { title: '医生管理' } },
      { path: 'appointments', name: 'Appointments', component: () => import('@/views/Appointments.vue'), meta: { title: '挂号管理' } },
      { path: 'drugs', name: 'Drugs', component: () => import('@/views/Drugs.vue'), meta: { title: '药品管理' } },
      { path: 'users', name: 'Users', component: () => import('@/views/Users.vue'), meta: { title: '用户管理' } },
      { path: 'knowledge', name: 'Knowledge', component: () => import('@/views/Knowledge.vue'), meta: { title: '知识库管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const adminStore = useAdminStore()
  if (to.meta.public) {
    next()
  } else if (!adminStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
