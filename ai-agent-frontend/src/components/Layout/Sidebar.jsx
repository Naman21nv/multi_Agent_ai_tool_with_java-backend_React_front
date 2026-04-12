import { useEffect } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useAgent } from '../../context/AgentContext';
import { getConversations } from '../../services/api';

export default function Sidebar() {
  const { state, dispatch, newConversation } = useAgent();
  const location = useLocation();

  useEffect(() => {
    loadConversations();
  }, []);

  async function loadConversations() {
    try {
      const convos = await getConversations();
      dispatch({ type: 'SET_CONVERSATIONS', payload: convos });
    } catch (err) {
      // Silently fail - backend might not be running yet
      console.warn('Could not load conversations:', err.message);
    }
  }

  function handleSelectConversation(id) {
    dispatch({ type: 'SET_CURRENT_CONVERSATION', payload: id });
  }

  function handleNewChat() {
    newConversation();
  }

  function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const now = new Date();
    const diffMs = now - d;
    const diffMins = Math.floor(diffMs / 60000);
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    return d.toLocaleDateString();
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="sidebar-logo">
          <div className="logo-icon">⚡</div>
          <div>
            <h1>NeuronAgent</h1>
            <div className="subtitle">ReAct AI Agent</div>
          </div>
        </div>
      </div>

      <nav className="sidebar-nav">
        <NavLink
          to="/"
          className={({ isActive }) => `nav-item ${isActive && location.pathname === '/' ? 'active' : ''}`}
          end
        >
          <span className="nav-icon">💬</span>
          Chat
        </NavLink>
        <NavLink
          to="/dashboard"
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <span className="nav-icon">📊</span>
          Dashboard
        </NavLink>
        <NavLink
          to="/tools"
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <span className="nav-icon">🔧</span>
          Tools
        </NavLink>
      </nav>

      <div className="nav-section-title">Recent Conversations</div>
      
      <div className="sidebar-conversations">
        {state.conversations.length === 0 ? (
          <div style={{ padding: '8px 16px', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
            No conversations yet
          </div>
        ) : (
          state.conversations.slice(0, 15).map((conv) => (
            <div
              key={conv.id}
              className={`conversation-item ${state.currentConversationId === conv.id ? 'active' : ''}`}
              onClick={() => handleSelectConversation(conv.id)}
              title={conv.title}
            >
              <span>💬</span>
              <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {conv.title}
              </span>
            </div>
          ))
        )}
      </div>

      <button className="new-chat-btn" onClick={handleNewChat} id="new-chat-button">
        ✨ New Chat
      </button>
    </aside>
  );
}
