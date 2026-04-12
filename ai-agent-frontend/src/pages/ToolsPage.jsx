import { useEffect, useState } from 'react';
import { getTools, checkHealth } from '../services/api';

export default function ToolsPage() {
  const [tools, setTools] = useState([]);
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTools();
  }, []);

  async function loadTools() {
    setLoading(true);
    try {
      const [toolsData, healthData] = await Promise.allSettled([
        getTools(),
        checkHealth(),
      ]);

      if (toolsData.status === 'fulfilled') {
        setTools(toolsData.value);
      }
      if (healthData.status === 'fulfilled') {
        setHealth(healthData.value);
      }
    } catch (err) {
      console.error('Failed to load tools:', err);
    } finally {
      setLoading(false);
    }
  }

  function getToolIcon(name) {
    switch (name) {
      case 'web_search': return '🔍';
      case 'write_to_file': return '📝';
      case 'summarise': return '📋';
      default: return '🔧';
    }
  }

  function getToolDetails(name) {
    switch (name) {
      case 'web_search':
        return {
          tech: 'DuckDuckGo HTML Scraping',
          category: 'Information Retrieval',
          color: '#06b6d4',
        };
      case 'write_to_file':
        return {
          tech: 'Java NIO File System',
          category: 'File Operations',
          color: '#10b981',
        };
      case 'summarise':
        return {
          tech: 'Ollama / Llama 3.1 (temp=0.3)',
          category: 'Text Processing',
          color: '#8b5cf6',
        };
      default:
        return {
          tech: 'Custom',
          category: 'General',
          color: '#6366f1',
        };
    }
  }

  if (loading) {
    return (
      <div className="tools-page">
        <div className="loading-spinner">
          <div className="spinner" />
        </div>
      </div>
    );
  }

  return (
    <div className="tools-page">
      <h1 className="page-title">Agent Tools</h1>
      <p className="page-subtitle">
        These tools are registered with the ReAct agent. The LLM reads each tool's description 
        to decide when and how to use them during reasoning.
      </p>

      <div className="tools-grid">
        {tools.length > 0 ? (
          tools.map((tool) => {
            const details = getToolDetails(tool.name);
            return (
              <div key={tool.name} className="tool-card">
                <div className="tool-icon" style={{
                  background: `linear-gradient(135deg, ${details.color}88 0%, ${details.color}44 100%)`,
                }}>
                  {getToolIcon(tool.name)}
                </div>
                <h3>{tool.name}</h3>
                <p>{tool.description}</p>
                <div style={{ 
                  marginTop: '12px', 
                  fontSize: '0.75rem', 
                  color: 'var(--text-muted)',
                  fontFamily: 'var(--font-mono)',
                }}>
                  <div style={{ marginBottom: '4px' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Category: </span>
                    {details.category}
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-secondary)' }}>Tech: </span>
                    {details.tech}
                  </div>
                </div>
                <div className="tool-status">
                  <div className="status-dot" style={{ width: 6, height: 6 }} />
                  Active
                </div>
              </div>
            );
          })
        ) : (
          /* Fallback cards when backend isn't connected */
          <>
            <div className="tool-card">
              <div className="tool-icon">🔍</div>
              <h3>web_search</h3>
              <p>Search the web for real-time info. Best for news or facts.</p>
              <div style={{ marginTop: '12px', fontSize: '0.75rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                <div style={{ marginBottom: '4px' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>Category: </span>Information Retrieval
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Tech: </span>DuckDuckGo HTML Scraping
                </div>
              </div>
              <div className="tool-status">
                <div className="status-dot offline" style={{ width: 6, height: 6 }} />
                Backend Offline
              </div>
            </div>
            <div className="tool-card">
              <div className="tool-icon">📝</div>
              <h3>write_to_file</h3>
              <p>Write content to a file. The file will be saved in the agent-output directory.</p>
              <div style={{ marginTop: '12px', fontSize: '0.75rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                <div style={{ marginBottom: '4px' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>Category: </span>File Operations
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Tech: </span>Java NIO File System
                </div>
              </div>
              <div className="tool-status">
                <div className="status-dot offline" style={{ width: 6, height: 6 }} />
                Backend Offline
              </div>
            </div>
            <div className="tool-card">
              <div className="tool-icon">📋</div>
              <h3>summarise</h3>
              <p>Summarize long text, such as data from a web search, into a concise summary.</p>
              <div style={{ marginTop: '12px', fontSize: '0.75rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                <div style={{ marginBottom: '4px' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>Category: </span>Text Processing
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Tech: </span>Ollama / Llama 3.1 (temp=0.3)
                </div>
              </div>
              <div className="tool-status">
                <div className="status-dot offline" style={{ width: 6, height: 6 }} />
                Backend Offline
              </div>
            </div>
          </>
        )}
      </div>

      {/* Architecture Explanation */}
      <div className="health-section" style={{ marginTop: '32px' }}>
        <h3>⚡ How ReAct Tools Work</h3>
        <div className="health-items">
          <div className="health-item">
            <span className="health-label">1. Registration</span>
            <span className="health-value healthy">
              Tools are auto-discovered via Spring Component Scanning
            </span>
          </div>
          <div className="health-item">
            <span className="health-label">2. Schema Generation</span>
            <span className="health-value healthy">
              Each tool provides a JSON schema for Ollama function-calling
            </span>
          </div>
          <div className="health-item">
            <span className="health-label">3. LLM Decision</span>
            <span className="health-value healthy">
              The LLM reads tool descriptions and decides which to invoke
            </span>
          </div>
          <div className="health-item">
            <span className="health-label">4. Execution</span>
            <span className="health-value healthy">
              ToolExecutorService runs the tool and feeds results back to the LLM
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
