import request from '@/utils/request'
import type { Doctor, User, KnowledgeDoc, Drug, Appointment, PageResult } from '@/types'

// 医生管理
export const doctorApi = {
  list: (params: any) => request.get<any, PageResult<Doctor>>('/admin/doctors', { params }),
  all: () => request.get<any, PageResult<Doctor>>('/admin/doctors', { params: { pageNum: 1, pageSize: 999 } }),
  add: (data: Doctor) => request.post('/admin/doctors', data),
  update: (id: number, data: Doctor) => request.put(`/admin/doctors/${id}`, data),
  remove: (id: number) => request.delete(`/admin/doctors/${id}`),
  updateStatus: (id: number, status: number) => request.put(`/admin/doctors/${id}/status`, { status })
}

// 用户管理
export const userApi = {
  list: (params: any) => request.get<any, PageResult<User>>('/admin/users', { params }),
  update: (id: number, data: User) => request.put(`/admin/users/${id}`, data),
  updateStatus: (id: number, status: number) => request.put(`/admin/users/${id}/status`, { status })
}

// 挂号管理
export const appointmentApi = {
  list: (params: any) => request.get<any, PageResult<Appointment>>('/admin/appointments', { params }),
  update: (id: number, data: any) => request.put(`/admin/appointments/${id}`, data)
}

// 药品管理
export const drugApi = {
  list: (params: any) => request.get<any, PageResult<Drug>>('/admin/drugs', { params }),
  add: (data: Drug) => request.post('/admin/drugs', data),
  update: (id: number, data: Drug) => request.put(`/admin/drugs/${id}`, data),
  remove: (id: number) => request.delete(`/admin/drugs/${id}`),
  updateStatus: (id: number, status: number) => request.put(`/admin/drugs/${id}/status`, { status })
}

// 知识库管理
export const knowledgeApi = {
  list: (params: any) => request.get<any, PageResult<KnowledgeDoc>>('/knowledge/docs', { params }),
  upload: (formData: FormData) => request.post('/knowledge/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  remove: (docId: string) => request.delete(`/knowledge/docs/${docId}`)
}
