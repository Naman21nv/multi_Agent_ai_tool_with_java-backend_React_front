import { useEffect, useState } from 'react';
import { useAgent } from '../context/AgentContext';
import { getStats, checkHealth } from '../services/api';

export default function DashboardPage() {
  const { state, dispatch } = useAgent();
  const [stats, setStats] = useState(null);
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  async function loadDashboardData() {
    setLoading(true);
    try {
      const [statsData, healthData] = await Promise.allSettled([
        getStats(),
        checkHealth(),
      ]);
      
      if (statsData.status === 'fulfilled') setStats(statsData.value);
      if (healthData.status === 'fulfilled') setHealth(healthData.value);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    } finally {
      setLoading(false);
    }
  }

  function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  if (loading) {
    return (
      <div className="dashboard-page">
        <div className="loading-spinner">
          <div className="spinner" />
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      <h1 className="page-title">Dashboard</h1>
      <p className="page-subtitle">Monitor your AI agent's activity and system health</p>

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">💬</div>
          <div className="stat-value">{stats?.totalConversations ?? 0}</div>
          <div className="stat-label">Total Conversations</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">📨</div>
          <div className="stat-value">{stats?.totalMessages ?? 0}</div>
          <div className="stat-label">Total Messages</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">🤖</div>
          <div className="stat-value">{stats?.totalAgentResponses ?? 0}</div>
          <div className="stat-label">Agent Responses</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">🔧</div>
          <div className="stat-value">{health?.registeredTools?.length ?? 0}</div>
          <div className="stat-label">Active Tools</div>
        </div>
      </div>

      {/* System Health */}
      <div className="health-section">
        <h3>🩺 System Health</h3>
        <div className="health-items">
          <div className="health-item">
            <span className="health-label">Backend Server</span>
            <span className={`health-value ${health ? 'healthy' : 'unhealthy'}`}>
              <div className={`status-dot ${health ? '' : 'offline'}`} style={{ width: 6, height: 6 }} />
              {health ? 'Running' : 'Offline'}
            </span>
          </div>
          <div className="health-item">
            <span className="health-label">Ollama LLM Server</span>
            <span className={`health-value ${health?.ollamaConnected ? 'healthy' : 'unhealthy'}`}>
              <div className={`status-dot ${health?.ollamaConnected ? '' : 'offline'}`} style={{ width: 6, height: 6 }} />
              {health?.ollamaConnected ? 'Connected' : 'Disconnected'}
            </span>
          </div>
          <div className="health-item">
            <span className="health-label">Model</span>
            <span className="health-value healthy">llama3.1</span>
          </div>
          <div className="health-item">
            <span className="health-label">Database</span>
            <span className={`health-value ${health ? 'healthy' : 'unhealthy'}`}>
              {health ? 'H2 (Active)' : 'Unknown'}
            </span>
          </div>
        </div>
      </div>

      {/* Recent Activity */}
      <div className="activity-section">
        <h3>📋 Recent Conversations</h3>
        <div className="activity-list">
          {stats?.recentConversations && stats.recentConversations.length > 0 ? (
            stats.recentConversations.map((conv) => (
              <div key={conv.id} className="activity-item">
                <span className="activity-icon">💬</span>
                <span className="activity-text">
                  <strong>{conv.title}</strong> — {conv.messageCount} messages
                </span>
                <span className="activity-time">{formatDate(conv.updatedAt)}</span>
              </div>
            ))
          ) : (
            <div className="empty-state">
              <div className="empty-icon">📭</div>
              <p>No conversations yet. Start chatting with the agent!</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
