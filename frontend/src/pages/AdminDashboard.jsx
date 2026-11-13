import { useEffect, useState } from 'react'
import Layout from '../components/common/Layout'
import { adminService, courseService, authService } from '../services/api'
import { Users, BookOpen, GraduationCap, Plus, Trash2, UserPlus, LayoutDashboard } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'

function StatCard({ icon: Icon, label, value, color }) {
  return (
    <div className="bg-white rounded-2xl p-6 shadow-sm flex items-center gap-4">
      <div className={`w-14 h-14 rounded-xl flex items-center justify-center ${color}`}>
        <Icon className="text-white" size={26} />
      </div>
      <div>
        <p className="text-3xl font-bold text-gray-900">{value}</p>
        <p className="text-sm text-gray-500">{label}</p>
      </div>
    </div>
  )
}

function DashboardHome() {
  const [stats, setStats] = useState({})
  useEffect(() => { adminService.dashboard().then(r => setStats(r.data)) }, [])
  return (
    <div>
      <h2 className="text-xl font-bold text-gray-800 mb-6">Overview</h2>
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard icon={GraduationCap} label="Total Students" value={stats.totalStudents ?? '-'} color="bg-blue-500" />
        <StatCard icon={Users} label="Total Faculty" value={stats.totalFaculty ?? '-'} color="bg-green-500" />
        <StatCard icon={BookOpen} label="Total Courses" value={stats.totalCourses ?? '-'} color="bg-purple-500" />
      </div>
    </div>
  )
}

function StudentsPanel() {
  const [students, setStudents] = useState([])
  const [showAdd, setShowAdd] = useState(false)
  const [form, setForm] = useState({ name: '', email: '', password: '', rollNumber: '', branch: '', semester: '' })
  const [msg, setMsg] = useState('')

  const load = () => adminService.getStudents().then(r => setStudents(r.data))
  useEffect(() => { load() }, [])

  const handleAdd = async (e) => {
    e.preventDefault()
    try {
      await api.post('/auth/register', { ...form, role: 'STUDENT', semester: Number(form.semester) })
      setMsg('Student added!'); load(); setShowAdd(false)
      setForm({ name: '', email: '', password: '', rollNumber: '', branch: '', semester: '' })
    } catch { setMsg('Error adding student') }
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this student?')) return
    await adminService.deleteStudent(id); load()
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800">Students ({students.length})</h2>
        <button onClick={() => setShowAdd(!showAdd)} className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-xl text-sm font-medium hover:bg-blue-700 transition-colors">
          <Plus size={16} /> Add Student
        </button>
      </div>

      {msg && <p className="mb-4 text-sm text-green-600 bg-green-50 p-3 rounded-lg">{msg}</p>}

      {showAdd && (
        <form onSubmit={handleAdd} className="bg-white p-6 rounded-2xl shadow-sm mb-6 grid grid-cols-2 gap-4">
          {[['name','Full Name'],['email','Email'],['password','Password'],['rollNumber','Roll Number'],['branch','Branch'],['semester','Semester']].map(([k,l]) => (
            <div key={k}>
              <label className="text-xs font-medium text-gray-600">{l}</label>
              <input value={form[k]} onChange={e => setForm({...form, [k]: e.target.value})} required
                className="mt-1 w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400" />
            </div>
          ))}
          <div className="col-span-2 flex gap-3">
            <button type="submit" className="bg-blue-600 text-white px-6 py-2 rounded-xl text-sm font-medium hover:bg-blue-700">Save</button>
            <button type="button" onClick={() => setShowAdd(false)} className="border px-6 py-2 rounded-xl text-sm font-medium text-gray-600 hover:bg-gray-50">Cancel</button>
          </div>
        </form>
      )}

      <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
            <tr>{['Name','Email','Roll No.','Branch','Sem','Actions'].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {students.map(s => (
              <tr key={s.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-900">{s.name}</td>
                <td className="px-4 py-3 text-gray-500">{s.email}</td>
                <td className="px-4 py-3">{s.rollNumber || '—'}</td>
                <td className="px-4 py-3">{s.branch || '—'}</td>
                <td className="px-4 py-3">{s.semester || '—'}</td>
                <td className="px-4 py-3">
                  <button onClick={() => handleDelete(s.id)} className="text-red-500 hover:text-red-700 p-1 rounded">
                    <Trash2 size={16} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function CoursesPanel() {
  const [courses, setCourses] = useState([])
  const [faculty, setFaculty] = useState([])
  const [showAdd, setShowAdd] = useState(false)
  const [form, setForm] = useState({ code: '', name: '', description: '', credits: '', semester: '', facultyId: '' })

  const load = () => courseService.getAll().then(r => setCourses(r.data))
  useEffect(() => {
    load()
    adminService.getFaculty().then(r => setFaculty(r.data))
  }, [])

  const handleAdd = async (e) => {
    e.preventDefault()
    await api.post('/courses', { ...form, credits: Number(form.credits), semester: Number(form.semester), facultyId: Number(form.facultyId) })
    load(); setShowAdd(false)
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete course?')) return
    await courseService.delete(id); load()
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800">Courses ({courses.length})</h2>
        <button onClick={() => setShowAdd(!showAdd)} className="flex items-center gap-2 bg-purple-600 text-white px-4 py-2 rounded-xl text-sm font-medium hover:bg-purple-700">
          <Plus size={16} /> Add Course
        </button>
      </div>

      {showAdd && (
        <form onSubmit={handleAdd} className="bg-white p-6 rounded-2xl shadow-sm mb-6 grid grid-cols-2 gap-4">
          {[['code','Course Code'],['name','Course Name'],['description','Description'],['credits','Credits'],['semester','Semester']].map(([k,l]) => (
            <div key={k}>
              <label className="text-xs font-medium text-gray-600">{l}</label>
              <input value={form[k]} onChange={e => setForm({...form,[k]:e.target.value})} required
                className="mt-1 w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-purple-400" />
            </div>
          ))}
          <div>
            <label className="text-xs font-medium text-gray-600">Assign Faculty</label>
            <select value={form.facultyId} onChange={e => setForm({...form, facultyId: e.target.value})}
              className="mt-1 w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-purple-400">
              <option value="">Select Faculty</option>
              {faculty.map(f => <option key={f.id} value={f.id}>{f.name}</option>)}
            </select>
          </div>
          <div className="col-span-2 flex gap-3">
            <button type="submit" className="bg-purple-600 text-white px-6 py-2 rounded-xl text-sm font-medium hover:bg-purple-700">Save</button>
            <button type="button" onClick={() => setShowAdd(false)} className="border px-6 py-2 rounded-xl text-sm font-medium text-gray-600">Cancel</button>
          </div>
        </form>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {courses.map(c => (
          <div key={c.id} className="bg-white rounded-2xl p-5 shadow-sm">
            <div className="flex items-start justify-between mb-3">
              <div>
                <span className="text-xs font-bold bg-purple-100 text-purple-700 px-2 py-1 rounded-lg">{c.code}</span>
                <h3 className="font-semibold text-gray-900 mt-2">{c.name}</h3>
              </div>
              <button onClick={() => handleDelete(c.id)} className="text-red-400 hover:text-red-600 p-1"><Trash2 size={15} /></button>
            </div>
            <p className="text-xs text-gray-500 mb-3">{c.description}</p>
            <div className="flex gap-3 text-xs text-gray-500">
              <span>{c.credits} credits</span>
              <span>•</span>
              <span>Sem {c.semester}</span>
              <span>•</span>
              <span>{c.studentCount} students</span>
            </div>
            {c.facultyName && <p className="text-xs text-blue-600 mt-2 font-medium">By {c.facultyName}</p>}
          </div>
        ))}
      </div>
    </div>
  )
}

export default function AdminDashboard() {
  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard, component: <DashboardHome /> },
    { id: 'students', label: 'Students', icon: GraduationCap, component: <StudentsPanel /> },
    { id: 'courses', label: 'Courses', icon: BookOpen, component: <CoursesPanel /> },
    { id: 'faculty', label: 'Faculty', icon: Users, component: <FacultyList /> },
  ]
  return <Layout navItems={navItems} role="ADMIN" />
}

function FacultyList() {
  const [faculty, setFaculty] = useState([])
  useEffect(() => { adminService.getFaculty().then(r => setFaculty(r.data)) }, [])
  return (
    <div>
      <h2 className="text-xl font-bold text-gray-800 mb-6">Faculty ({faculty.length})</h2>
      <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
            <tr>{['Name','Email','Department','Designation'].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {faculty.map(f => (
              <tr key={f.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{f.name}</td>
                <td className="px-4 py-3 text-gray-500">{f.email}</td>
                <td className="px-4 py-3">{f.department || '—'}</td>
                <td className="px-4 py-3">{f.designation || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
