import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useAgent } from '../../context/AgentContext';
import { checkHealth } from '../../services/api';

export default function Header() {
  const { state } = useAgent();
  const location = useLocation();
  const [ollamaStatus, setOllamaStatus] = useState(false);

  useEffect(() => {
    checkOllamaHealth();
    const interval = setInterval(checkOllamaHealth, 30000);
    return () => clearInterval(interval);
  }, []);

  async function checkOllamaHealth() {
    try {
      const health = await checkHealth();
      setOllamaStatus(health.ollamaConnected);
    } catch {
      setOllamaStatus(false);
    }
  }

  function getPageTitle() {
    switch (location.pathname) {
      case '/': return 'Chat with Agent';
      case '/dashboard': return 'Dashboard';
      case '/tools': return 'Agent Tools';
      default: return 'NeuronAgent';
    }
  }

  return (
    <header className="header">
      <h2 className="header-title">{getPageTitle()}</h2>
      <div className="header-actions">
        <div className="header-btn">
          <div className={`status-dot ${ollamaStatus ? '' : 'offline'}`} />
          <span>{ollamaStatus ? 'Ollama Connected' : 'Ollama Offline'}</span>
        </div>
        <div className="header-btn">
          <span>🧠</span>
          <span>llama3.1</span>
        </div>
      </div>
    </header>
  );
}
