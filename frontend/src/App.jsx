import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Login from './pages/Login'
import AdminDashboard from './pages/AdminDashboard'
import FacultyDashboard from './pages/FacultyDashboard'
import StudentDashboard from './pages/StudentDashboard'

function ProtectedRoute({ children, roles }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div></div>
  if (!user) return <Navigate to="/login" />
  if (roles && !roles.includes(user.role)) return <Navigate to="/login" />
  return children
}

function RoleRouter() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" />
  if (user.role === 'ADMIN') return <Navigate to="/admin" />
  if (user.role === 'FACULTY') return <Navigate to="/faculty" />
  return <Navigate to="/student" />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<ProtectedRoute><RoleRouter /></ProtectedRoute>} />
          <Route path="/admin/*" element={<ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>} />
          <Route path="/faculty/*" element={<ProtectedRoute roles={['ADMIN','FACULTY']}><FacultyDashboard /></ProtectedRoute>} />
          <Route path="/student/*" element={<ProtectedRoute roles={['ADMIN','FACULTY','STUDENT']}><StudentDashboard /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
