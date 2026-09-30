export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export interface AdminInfo {
  id: number
  username: string
  realName: string
}

export interface Doctor {
  id: number
  userId?: number
  name: string
  department: string
  title: string
  specialty?: string
  introduction?: string
  consultationFee?: number
  status: number
}

export interface User {
  id: number
  username: string
  realName?: string
  phone?: string
  idCard?: string
  gender?: number
  birthDate?: string
  role: number
  avatar?: string
  status: number
  createdAt?: string
}

export interface KnowledgeDoc {
  id: number
  docId: string
  title: string
  summary?: string
  category: string
  source?: string
  status: number
  createdAt: string
}

export interface Drug {
  id: number
  name: string
  genericName?: string
  category?: string
  specification?: string
  unit?: string
  manufacturer?: string
  price?: number
  stock?: number
  instruction?: string
  sideEffects?: string
  status: number
}

export interface Appointment {
  id: number
  patientId: number
  patientName?: string
  patientPhone?: string
  doctorId: number
  doctorName?: string
  department?: string
  appointmentDate: string
  appointmentTime: string
  symptom?: string
  status: number
  cancelReason?: string
  createdAt?: string
}
