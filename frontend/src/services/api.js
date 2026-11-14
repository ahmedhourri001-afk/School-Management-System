import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// Request interceptor — attach JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor — redirect to login on 401
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api

// ---- Auth ----
export const authService = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (data) => api.post('/auth/register', data),
  me: () => api.get('/auth/me'),
}

// ---- Admin ----
export const adminService = {
  dashboard: () => api.get('/admin/dashboard'),
  getStudents: () => api.get('/admin/students'),
  getFaculty: () => api.get('/admin/faculty'),
  deleteStudent: (id) => api.delete(`/admin/students/${id}`),
  enroll: (studentId, courseId) => api.post('/admin/enroll', { studentId, courseId }),
}

// ---- Courses ----
export const courseService = {
  getAll: () => api.get('/courses'),
  create: (data) => api.post('/courses', data),
  delete: (id) => api.delete(`/courses/${id}`),
  getFacultyCourses: (facultyId) => api.get(`/faculty/courses?facultyId=${facultyId}`),
  getStudentCourses: (studentId) => api.get(`/student/courses?studentId=${studentId}`),
  getStudents: (courseId) => api.get(`/faculty/courses/${courseId}/students`),
}

// ---- Attendance ----
export const attendanceService = {
  mark: (data) => api.post('/attendance', data),
  getByStudent: (studentId) => api.get(`/attendance/student/${studentId}`),
  getByCourse: (courseId) => api.get(`/attendance/course/${courseId}`),
  update: (id, data) => api.put(`/attendance/${id}`, data),
  getSummary: (studentId) => api.get(`/attendance/student/${studentId}/summary`),
}

// ---- Marks ----
export const marksService = {
  upload: (data) => api.post('/marks', data),
  getByStudent: (studentId) => api.get(`/marks/student/${studentId}`),
  getByCourse: (courseId) => api.get(`/marks/course/${courseId}`),
  update: (id, data) => api.put(`/marks/${id}`, data),
}

// ---- Config (seuil d'absences, fourni par la ConfigMap côté serveur) ----
export const configService = {
  getAbsenceThreshold: () => api.get('/config/absence-threshold'),
}

// ---- Export CSV (administration) ----
// Le JWT est dans un en-tête HTTP : on télécharge via axios (blob), pas avec un simple lien <a href>.
export const exportService = {
  download: async (kind, filename) => {
    const res = await api.get(`/admin/export/${kind}`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(res.data)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  },
}
