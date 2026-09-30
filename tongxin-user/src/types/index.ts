// 后端统一响应结构
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 分页结果
export interface PageResult<T = any> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  realName: string
  phone: string
  role: number
  avatar?: string
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

// 医生
export interface Doctor {
  id: number
  name: string
  department: string
  title: string
  specialty: string
  consultationFee: number
  avatar?: string
  introduction?: string
}

export interface ScheduleItem {
  date: string
  timeSlot: string
  total: number
  remaining: number
}

// 预约
export interface AppointmentDTO {
  id: number
  userId: number
  doctorId: number
  doctorName: string
  department: string
  appointmentDate: string
  appointmentTime: string
  symptom?: string
  status: number
  createdAt: string
}

// 药品
export interface Drug {
  id: number
  name: string
  category: string
  manufacturer?: string
  price: number
  specification?: string
  description?: string
}

// 知识库
export interface KnowledgeDocDTO {
  id: number
  docId: string
  title: string
  summary: string
  category: string
  source: string
  status: number
}

// 对话
export interface ChatSession {
  id: number
  sessionId: string
  userId: number
  title: string
  messageCount: number
  createdAt: string
  updatedAt: string
}

export interface ChatMessage {
  id: number
  sessionId: number
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt: string
}
