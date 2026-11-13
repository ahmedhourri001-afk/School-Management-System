import { useEffect, useState } from 'react'
import Layout from '../components/common/Layout'
import { courseService, attendanceService, marksService } from '../services/api'
import { BookOpen, ClipboardCheck, BarChart2, LayoutDashboard } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useWebSocket } from '../hooks/useWebSocket'
import { RadarChart, Radar, PolarGrid, PolarAngleAxis, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts'

function StudentHome({ courses, marks, attendance }) {
  // Compute per-course stats
  const courseStats = courses.map(c => {
    const courseMarks = marks.filter(m => m.courseName === c.name)
    const avgPct = courseMarks.length
      ? Math.round(courseMarks.reduce((a, m) => a + m.percentage, 0) / courseMarks.length)
      : 0
    return { name: c.code, avg: avgPct }
  })

  const overallAvg = marks.length
    ? Math.round(marks.reduce((a, m) => a + m.percentage, 0) / marks.length)
    : 0

  const attendancePct = attendance.length
    ? Math.round(attendance.filter(a => a.status === 'PRESENT').length / attendance.length * 100)
    : 0

  return (
    <div className="space-y-6">
      {/* Summary cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-2xl p-5 shadow-sm text-center">
          <p className="text-4xl font-bold text-blue-600">{courses.length}</p>
          <p className="text-sm text-gray-500 mt-1">Enrolled Courses</p>
        </div>
        <div className="bg-white rounded-2xl p-5 shadow-sm text-center">
          <p className={`text-4xl font-bold ${overallAvg >= 60 ? 'text-green-600' : 'text-red-500'}`}>{overallAvg}%</p>
          <p className="text-sm text-gray-500 mt-1">Overall Performance</p>
        </div>
        <div className="bg-white rounded-2xl p-5 shadow-sm text-center">
          <p className={`text-4xl font-bold ${attendancePct >= 75 ? 'text-green-600' : 'text-red-500'}`}>{attendancePct}%</p>
          <p className="text-sm text-gray-500 mt-1">Attendance</p>
          {attendancePct < 75 && <p className="text-xs text-red-400 mt-1">⚠ Below 75% threshold</p>}
        </div>
      </div>

      {/* Performance chart */}
      {courseStats.length > 0 && (
        <div className="bg-white rounded-2xl p-6 shadow-sm">
          <h3 className="font-semibold text-gray-800 mb-4">Performance by Course</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={courseStats}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
              <Tooltip formatter={(v) => `${v}%`} />
              <Bar dataKey="avg" fill="#3b82f6" radius={[6, 6, 0, 0]} name="Average %" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Enrolled courses */}
      <div>
        <h3 className="font-semibold text-gray-800 mb-3">My Courses</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {courses.map(c => (
            <div key={c.id} className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-4">
              <div className="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center">
                <BookOpen size={18} className="text-blue-600" />
              </div>
              <div>
                <p className="font-medium text-gray-900 text-sm">{c.name}</p>
                <p className="text-xs text-gray-400">{c.code} • {c.credits} credits • {c.facultyName || 'TBA'}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

function AttendanceView({ studentId }) {
  const [attendance, setAttendance] = useState([])

  useEffect(() => {
    if (studentId) attendanceService.getByStudent(studentId).then(r => setAttendance(r.data))
  }, [studentId])

  // Group by course
  const byCourse = attendance.reduce((acc, a) => {
    if (!acc[a.courseName]) acc[a.courseName] = []
    acc[a.courseName].push(a)
    return acc
  }, {})

  return (
    <div>
      <h2 className="text-xl font-bold text-gray-800 mb-6">My Attendance</h2>
      {Object.keys(byCourse).length === 0 && (
        <p className="text-gray-400 text-sm bg-white rounded-2xl p-8 text-center shadow-sm">No attendance records yet.</p>
      )}
      {Object.entries(byCourse).map(([course, records]) => {
        const present = records.filter(r => r.status === 'PRESENT').length
        const pct = Math.round(present / records.length * 100)
        return (
          <div key={course} className="bg-white rounded-2xl p-6 shadow-sm mb-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-gray-900">{course}</h3>
              <div className="text-right">
                <span className={`text-2xl font-bold ${pct >= 75 ? 'text-green-600' : 'text-red-500'}`}>{pct}%</span>
                <p className="text-xs text-gray-400">{present}/{records.length} classes</p>
              </div>
            </div>
            {/* Progress bar */}
            <div className="w-full bg-gray-100 rounded-full h-2 mb-4">
              <div className={`h-2 rounded-full ${pct >= 75 ? 'bg-green-500' : 'bg-red-400'}`} style={{ width: `${pct}%` }} />
            </div>
            <div className="flex flex-wrap gap-2">
              {records.map(r => (
                <span key={r.id} title={r.date}
                  className={`w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold
                    ${r.status === 'PRESENT' ? 'bg-green-100 text-green-700' :
                      r.status === 'LATE' ? 'bg-yellow-100 text-yellow-700' : 'bg-red-100 text-red-500'}`}>
                  {r.status === 'PRESENT' ? 'P' : r.status === 'LATE' ? 'L' : 'A'}
                </span>
              ))}
            </div>
          </div>
        )
      })}
    </div>
  )
}

function MarksView({ studentId }) {
  const [marks, setMarks] = useState([])

  useEffect(() => {
    if (studentId) marksService.getByStudent(studentId).then(r => setMarks(r.data))
  }, [studentId])

  const byCourse = marks.reduce((acc, m) => {
    if (!acc[m.courseName]) acc[m.courseName] = []
    acc[m.courseName].push(m)
    return acc
  }, {})

  const gradeColor = { 'A+': 'bg-emerald-100 text-emerald-700', A: 'bg-green-100 text-green-700', B: 'bg-blue-100 text-blue-700', C: 'bg-yellow-100 text-yellow-700', D: 'bg-orange-100 text-orange-600', F: 'bg-red-100 text-red-600' }

  return (
    <div>
      <h2 className="text-xl font-bold text-gray-800 mb-6">My Marks & Grades</h2>
      {Object.keys(byCourse).length === 0 && (
        <p className="text-gray-400 text-sm bg-white rounded-2xl p-8 text-center shadow-sm">No marks uploaded yet.</p>
      )}
      {Object.entries(byCourse).map(([course, courseMarks]) => {
        const avg = Math.round(courseMarks.reduce((a, m) => a + m.percentage, 0) / courseMarks.length)
        return (
          <div key={course} className="bg-white rounded-2xl p-6 shadow-sm mb-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-gray-900">{course}</h3>
              <span className="text-sm font-medium text-gray-500">Average: <span className={`font-bold ${avg >= 60 ? 'text-green-600' : 'text-red-500'}`}>{avg}%</span></span>
            </div>
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
                <tr>{['Exam','Marks','Total','%','Grade'].map(h => <th key={h} className="px-4 py-2 text-left">{h}</th>)}</tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {courseMarks.map(m => (
                  <tr key={m.id}>
                    <td className="px-4 py-3"><span className="bg-gray-100 text-gray-700 px-2 py-1 rounded text-xs">{m.examType}</span></td>
                    <td className="px-4 py-3 font-medium">{m.marksObtained}</td>
                    <td className="px-4 py-3 text-gray-500">{m.totalMarks}</td>
                    <td className="px-4 py-3">{m.percentage}%</td>
                    <td className="px-4 py-3"><span className={`px-2 py-1 rounded-lg text-xs font-bold ${gradeColor[m.grade] || ''}`}>{m.grade}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      })}
    </div>
  )
}

export default function StudentDashboard() {
  const { user } = useAuth()
  const { notifications, clearNotifications } = useWebSocket(user?.email)
  const [courses, setCourses] = useState([])
  const [marks, setMarks] = useState([])
  const [attendance, setAttendance] = useState([])

  useEffect(() => {
    if (!user?.userId) return
    courseService.getStudentCourses(user.userId).then(r => setCourses(r.data))
    marksService.getByStudent(user.userId).then(r => setMarks(r.data))
    attendanceService.getByStudent(user.userId).then(r => setAttendance(r.data))
  }, [user])

  // Refresh marks when notification arrives
  useEffect(() => {
    if (notifications.length > 0 && user?.userId) {
      marksService.getByStudent(user.userId).then(r => setMarks(r.data))
    }
  }, [notifications])

  const navItems = [
    { id: 'home', label: 'Dashboard', icon: LayoutDashboard, component: <StudentHome courses={courses} marks={marks} attendance={attendance} /> },
    { id: 'attendance', label: 'Attendance', icon: ClipboardCheck, component: <AttendanceView studentId={user?.userId} /> },
    { id: 'marks', label: 'Marks & Grades', icon: BarChart2, component: <MarksView studentId={user?.userId} /> },
  ]

  return <Layout navItems={navItems} role="STUDENT" notifications={notifications} />
}
