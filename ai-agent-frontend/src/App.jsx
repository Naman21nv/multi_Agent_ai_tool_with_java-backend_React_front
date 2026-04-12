import { Routes, Route } from 'react-router-dom'
import { AgentProvider } from './context/AgentContext'
import Layout from './components/Layout/Layout'
import ChatPage from './pages/ChatPage'
import DashboardPage from './pages/DashboardPage'
import ToolsPage from './pages/ToolsPage'

function App() {
  return (
    <AgentProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<ChatPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/tools" element={<ToolsPage />} />
        </Routes>
      </Layout>
    </AgentProvider>
  )
}

export default App
