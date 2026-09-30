import { get, post, put, del } from '@/utils/request'
import type {
  UserInfo, LoginResponse, PageResult,
  Doctor, ScheduleItem, AppointmentDTO, Drug,
  KnowledgeDocDTO, ChatSession, ChatMessage
} from '@/types'

// ============ 用户 ============
export const userApi = {
  register: (data: { username: string; password: string; realName: string; phone: string; idCard?: string; gender?: number; birthDate?: string }) =>
    post<void>('/users/register', data),
  login: (data: { username: string; password: string }) =>
    post<LoginResponse>('/users/login', data),
  getCurrentUser: () => get<UserInfo>('/users/me')
}

// ============ 医生 ============
export const doctorApi = {
  list: (params: { department?: string; keyword?: string; pageNum?: number; pageSize?: number }) =>
    get<PageResult<Doctor>>('/doctors', params),
  detail: (id: number) => get<Doctor>(`/doctors/${id}`),
  schedule: (id: number, date?: string) =>
    get<ScheduleItem[]>(`/doctors/${id}/schedule`, date ? { date } : {})
}

// ============ 预约 ============
export const appointmentApi = {
  create: (data: { doctorId: number; appointmentDate: string; appointmentTime: string; symptom?: string }) =>
    post<{ appointmentId: number }>('/appointments', data),
  myList: (params: { status?: number; pageNum?: number; pageSize?: number }) =>
    get<PageResult<AppointmentDTO>>('/appointments/my', params),
  cancel: (id: number, cancelReason: string) =>
    put<void>(`/appointments/${id}/cancel`, { cancelReason })
}

// ============ 药品 ============
export const drugApi = {
  search: (params: { keyword: string; category?: string; pageNum?: number; pageSize?: number }) =>
    get<PageResult<Drug>>('/drugs/search', params),
  detail: (id: number) => get<Drug>(`/drugs/${id}`)
}

// ============ 对话 ============
export const chatApi = {
  createSession: (data: { title?: string }) =>
    post<{ sessionId: string }>('/chat/sessions', data),
  mySessions: (params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<ChatSession>>('/chat/sessions', params),
  deleteSession: (sessionId: string) => del<void>(`/chat/sessions/${sessionId}`),
  messages: (sessionId: string, params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<ChatMessage>>(`/chat/sessions/${sessionId}/messages`, params)
}

// ============ 知识库 ============
export const knowledgeApi = {
  upload: (formData: FormData) => post<{ docId: string }>('/knowledge/upload', formData),
  list: (params: { category?: string; keyword?: string; pageNum?: number; pageSize?: number }) =>
    get<PageResult<KnowledgeDocDTO>>('/knowledge/docs', params),
  delete: (docId: string) => del<void>(`/knowledge/docs/${docId}`)
}
