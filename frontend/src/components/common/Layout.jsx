import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { BookOpen, LogOut, Menu, X, Bell } from 'lucide-react'

export default function Layout({ children, navItems, role, notifications = [] }) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [activeTab, setActiveTab] = useState(navItems[0]?.id)
  const [showNotifs, setShowNotifs] = useState(false)
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const roleColors = { ADMIN: 'bg-purple-600', FACULTY: 'bg-green-600', STUDENT: 'bg-blue-600' }
  const bgColor = roleColors[role] || 'bg-blue-600'

  const activeNav = navItems.find(n => n.id === activeTab)

  return (
    <div className="flex h-screen bg-gray-100 overflow-hidden">
      {/* Sidebar */}
      <aside className={`${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} md:translate-x-0 fixed md:static inset-y-0 left-0 z-50 w-64 ${bgColor} text-white flex flex-col transition-transform duration-200`}>
        {/* Brand */}
        <div className="flex items-center gap-3 p-6 border-b border-white/20">
          <BookOpen size={24} />
          <div>
            <p className="font-bold text-sm leading-tight">SMS Portal</p>
            <p className="text-xs text-white/70">{role} Dashboard</p>
          </div>
        </div>

        {/* User info */}
        <div className="p-4 border-b border-white/20">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full bg-white/20 flex items-center justify-center font-bold text-sm">
              {user?.name?.charAt(0)}
            </div>
            <div className="min-w-0">
              <p className="text-sm font-medium truncate">{user?.name}</p>
              <p className="text-xs text-white/60 truncate">{user?.email}</p>
            </div>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
          {navItems.map(item => (
            <button key={item.id} onClick={() => { setActiveTab(item.id); setSidebarOpen(false) }}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors text-left
                ${activeTab === item.id ? 'bg-white/25' : 'hover:bg-white/10'}`}>
              <item.icon size={18} />
              {item.label}
            </button>
          ))}
        </nav>

        {/* Logout */}
        <div className="p-4 border-t border-white/20">
          <button onClick={handleLogout} className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm hover:bg-white/10 transition-colors">
            <LogOut size={18} />
            Sign out
          </button>
        </div>
      </aside>

      {/* Overlay */}
      {sidebarOpen && <div className="fixed inset-0 z-40 bg-black/50 md:hidden" onClick={() => setSidebarOpen(false)} />}

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Topbar */}
        <header className="bg-white shadow-sm px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button onClick={() => setSidebarOpen(true)} className="md:hidden p-2 rounded-lg hover:bg-gray-100">
              <Menu size={20} />
            </button>
            <h1 className="font-semibold text-gray-800">{activeNav?.label}</h1>
          </div>
          <div className="relative">
            <button onClick={() => setShowNotifs(!showNotifs)} className="relative p-2 rounded-lg hover:bg-gray-100">
              <Bell size={20} className="text-gray-600" />
              {notifications.length > 0 && (
                <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                  {notifications.length}
                </span>
              )}
            </button>
            {showNotifs && notifications.length > 0 && (
              <div className="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-xl border z-50 max-h-80 overflow-y-auto">
                <p className="text-xs font-semibold text-gray-500 px-4 py-3 border-b">NOTIFICATIONS</p>
                {notifications.map((n, i) => (
                  <div key={i} className="px-4 py-3 border-b last:border-0 hover:bg-gray-50">
                    <p className="text-sm font-medium text-gray-800">{n.message}</p>
                    <p className="text-xs text-gray-500 mt-1">{n.courseName} — Grade: <span className="font-bold text-green-600">{n.grade}</span></p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-6">
          {activeNav?.component}
        </main>
      </div>
    </div>
  )
}
