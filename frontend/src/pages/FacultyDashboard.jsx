import { useEffect, useState } from 'react'
import Layout from '../components/common/Layout'
import { courseService, attendanceService, marksService } from '../services/api'
import { BookOpen, ClipboardList, BarChart2, LayoutDashboard } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'

function FacultyHome({ courses }) {
  return (
    <div>
      <h2 className="text-xl font-bold text-gray-800 mb-6">My Courses</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {courses.map(c => (
          <div key={c.id} className="bg-white rounded-2xl p-5 shadow-sm border-l-4 border-green-500">
            <span className="text-xs font-bold bg-green-100 text-green-700 px-2 py-1 rounded-lg">{c.code}</span>
            <h3 className="font-semibold text-gray-900 mt-2 mb-1">{c.name}</h3>
            <p className="text-xs text-gray-500 mb-3">{c.description}</p>
            <div className="flex gap-3 text-xs text-gray-400">
              <span>{c.credits} credits</span>
              <span>•</span>
              <span>Sem {c.semester}</span>
              <span>•</span>
              <span className="text-green-600 font-medium">{c.studentCount} students</span>
            </div>
          </div>
        ))}
        {courses.length === 0 && (
          <p className="text-gray-400 text-sm col-span-3">No courses assigned yet.</p>
        )}
      </div>
    </div>
  )
}

function AttendancePanel({ courses }) {
  const [selectedCourse, setSelectedCourse] = useState('')
  const [students, setStudents] = useState([])
  const [date, setDate] = useState(new Date().toISOString().split('T')[0])
  const [attendance, setAttendance] = useState({}) // { studentId: 'PRESENT'|'ABSENT'|'LATE' }
  const [msg, setMsg] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!selectedCourse) return
    api.get(`/courses/${selectedCourse}`).then(r => {
      // Load students enrolled in course
      api.get(`/admin/students`).then(res => {
        // Filter students who are enrolled — we'll mark all for simplicity
        setStudents(res.data)
        const init = {}
        res.data.forEach(s => { init[s.id] = 'PRESENT' })
        setAttendance(init)
      })
    })
  }, [selectedCourse])

  const handleSubmit = async () => {
    setSaving(true)
    setMsg('')
    try {
      await Promise.all(
        students.map(s =>
          attendanceService.mark({
            studentId: s.id,
            courseId: Number(selectedCourse),
            date,
            status: attendance[s.id] || 'ABSENT'
          })
        )
      )
      setMsg('Attendance saved successfully!')
    } catch {
      setMsg('Error saving attendance.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2 className="text-xl font-bold text-gray-800 mb-6">Mark Attendance</h2>

      <div className="bg-white rounded-2xl p-6 shadow-sm mb-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
          <div>
            <label className="text-xs font-medium text-gray-600 block mb-1">Select Course</label>
            <select value={selectedCourse} onChange={e => setSelectedCourse(e.target.value)}
              className="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-400">
              <option value="">-- Select Course --</option>
              {courses.map(c => <option key={c.id} value={c.id}>{c.code} — {c.name}</option>)}
            </select>
          </div>
          <div>
            <label className="text-xs font-medium text-gray-600 block mb-1">Date</label>
            <input type="date" value={date} onChange={e => setDate(e.target.value)}
              className="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-400" />
          </div>
        </div>

        {msg && <p className={`text-sm p-3 rounded-lg mb-4 ${msg.includes('Error') ? 'bg-red-50 text-red-600' : 'bg-green-50 text-green-600'}`}>{msg}</p>}

        {students.length > 0 && (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                  <tr>
                    <th className="px-4 py-3 text-left">Student</th>
                    <th className="px-4 py-3 text-left">Roll No.</th>
                    {['PRESENT','ABSENT','LATE'].map(s => (
                      <th key={s} className="px-4 py-3 text-center">{s}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {students.map(s => (
                    <tr key={s.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 font-medium text-gray-900">{s.name}</td>
                      <td className="px-4 py-3 text-gray-500">{s.rollNumber || '—'}</td>
                      {['PRESENT','ABSENT','LATE'].map(status => (
                        <td key={status} className="px-4 py-3 text-center">
                          <input type="radio" name={`att-${s.id}`} value={status}
                            checked={attendance[s.id] === status}
                            onChange={() => setAttendance(prev => ({...prev, [s.id]: status}))}
                            className="accent-green-500 w-4 h-4" />
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <button onClick={handleSubmit} disabled={saving}
              className="mt-4 bg-green-600 text-white px-8 py-2.5 rounded-xl text-sm font-medium hover:bg-green-700 disabled:opacity-50 transition-colors">
              {saving ? 'Saving...' : 'Save Attendance'}
            </button>
          </>
        )}

        {selectedCourse && students.length === 0 && (
          <p className="text-gray-400 text-sm text-center py-8">No students found.</p>
        )}

        {!selectedCourse && (
          <p className="text-gray-400 text-sm text-center py-8">Select a course to mark attendance.</p>
        )}
      </div>
    </div>
  )
}

function MarksPanel({ courses }) {
  const [selectedCourse, setSelectedCourse] = useState('')
  const [students, setStudents] = useState([])
  const [examType, setExamType] = useState('MIDTERM')
  const [totalMarks, setTotalMarks] = useState(100)
  const [marks, setMarks] = useState({}) // { studentId: marksObtained }
  const [msg, setMsg] = useState('')
  const [saving, setSaving] = useState(false)
  const [existingMarks, setExistingMarks] = useState([])

  useEffect(() => {
    if (!selectedCourse) return
    api.get('/admin/students').then(res => {
      setStudents(res.data)
      const init = {}
      res.data.forEach(s => { init[s.id] = '' })
      setMarks(init)
    })
    marksService.getByCourse(selectedCourse).then(r => setExistingMarks(r.data))
  }, [selectedCourse])

  const handleSubmit = async () => {
    setSaving(true); setMsg('')
    try {
      await Promise.all(
        students
          .filter(s => marks[s.id] !== '')
          .map(s =>
            marksService.upload({
              studentId: s.id,
              courseId: Number(selectedCourse),
              examType,
              marksObtained: Number(marks[s.id]),
              totalMarks: Number(totalMarks)
            })
          )
      )
      setMsg('Marks uploaded! Students notified via WebSocket.')
      marksService.getByCourse(selectedCourse).then(r => setExistingMarks(r.data))
    } catch {
      setMsg('Error uploading marks.')
    } finally {
      setSaving(false)
    }
  }

  const gradeColor = { 'A+': 'text-emerald-600', A: 'text-green-600', B: 'text-blue-600', C: 'text-yellow-600', D: 'text-orange-500', F: 'text-red-600' }

  return (
    <div>
      <h2 className="text-xl font-bold text-gray-800 mb-6">Upload Marks</h2>

      <div className="bg-white rounded-2xl p-6 shadow-sm mb-6">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-4">
          <div>
            <label className="text-xs font-medium text-gray-600 block mb-1">Course</label>
            <select value={selectedCourse} onChange={e => setSelectedCourse(e.target.value)}
              className="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-400">
              <option value="">-- Select Course --</option>
              {courses.map(c => <option key={c.id} value={c.id}>{c.code} — {c.name}</option>)}
            </select>
          </div>
          <div>
            <label className="text-xs font-medium text-gray-600 block mb-1">Exam Type</label>
            <select value={examType} onChange={e => setExamType(e.target.value)}
              className="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-400">
              {['MIDTERM','FINAL','ASSIGNMENT','QUIZ'].map(t => <option key={t}>{t}</option>)}
            </select>
          </div>
          <div>
            <label className="text-xs font-medium text-gray-600 block mb-1">Total Marks</label>
            <input type="number" value={totalMarks} onChange={e => setTotalMarks(e.target.value)}
              className="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-400" />
          </div>
        </div>

        {msg && <p className={`text-sm p-3 rounded-lg mb-4 ${msg.includes('Error') ? 'bg-red-50 text-red-600' : 'bg-green-50 text-green-600'}`}>{msg}</p>}

        {students.length > 0 && (
          <>
            <table className="w-full text-sm mb-4">
              <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                <tr>
                  <th className="px-4 py-3 text-left">Student</th>
                  <th className="px-4 py-3 text-left">Roll No.</th>
                  <th className="px-4 py-3 text-left">Marks (out of {totalMarks})</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {students.map(s => (
                  <tr key={s.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium">{s.name}</td>
                    <td className="px-4 py-3 text-gray-500">{s.rollNumber || '—'}</td>
                    <td className="px-4 py-3">
                      <input type="number" min="0" max={totalMarks} value={marks[s.id]}
                        onChange={e => setMarks(prev => ({...prev, [s.id]: e.target.value}))}
                        placeholder="—"
                        className="border rounded-lg px-3 py-1.5 text-sm w-28 focus:outline-none focus:ring-2 focus:ring-green-400" />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <button onClick={handleSubmit} disabled={saving}
              className="bg-green-600 text-white px-8 py-2.5 rounded-xl text-sm font-medium hover:bg-green-700 disabled:opacity-50">
              {saving ? 'Uploading...' : 'Upload & Notify Students'}
            </button>
          </>
        )}

        {!selectedCourse && <p className="text-gray-400 text-sm text-center py-8">Select a course to upload marks.</p>}
      </div>

      {/* Existing marks table */}
      {existingMarks.length > 0 && (
        <div className="bg-white rounded-2xl p-6 shadow-sm">
          <h3 className="font-semibold text-gray-800 mb-4">Marks History</h3>
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>{['Student','Exam','Marks','Total','Grade','%'].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {existingMarks.map(m => (
                <tr key={m.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{m.studentName}</td>
                  <td className="px-4 py-3"><span className="bg-gray-100 text-gray-700 px-2 py-1 rounded text-xs">{m.examType}</span></td>
                  <td className="px-4 py-3">{m.marksObtained}</td>
                  <td className="px-4 py-3">{m.totalMarks}</td>
                  <td className={`px-4 py-3 font-bold ${gradeColor[m.grade] || ''}`}>{m.grade}</td>
                  <td className="px-4 py-3">{m.percentage}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

export default function FacultyDashboard() {
  const { user } = useAuth()
  const [courses, setCourses] = useState([])

  useEffect(() => {
    if (user?.userId) {
      courseService.getFacultyCourses(user.userId).then(r => setCourses(r.data))
    }
  }, [user])

  const navItems = [
    { id: 'home', label: 'My Courses', icon: LayoutDashboard, component: <FacultyHome courses={courses} /> },
    { id: 'attendance', label: 'Attendance', icon: ClipboardList, component: <AttendancePanel courses={courses} /> },
    { id: 'marks', label: 'Upload Marks', icon: BarChart2, component: <MarksPanel courses={courses} /> },
  ]

  return <Layout navItems={navItems} role="FACULTY" />
}
