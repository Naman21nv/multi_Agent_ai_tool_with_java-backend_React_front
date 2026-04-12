import { useState } from 'react';

/**
 * Visual representation of the agent's ReAct execution trace.
 * Shows Think → Act → Observe steps with timing info.
 */
export default function AgentTrace({ traces }) {
  const [isExpanded, setIsExpanded] = useState(false);

  if (!traces || traces.length === 0) return null;

  function getTraceIcon(type) {
    switch (type) {
      case 'THINKING': return '🧠';
      case 'TOOL_CALL': return '⚙️';
      case 'TOOL_RESULT': return '✅';
      case 'FINAL_ANSWER': return '🎯';
      case 'ERROR': return '❌';
      default: return '📌';
    }
  }

  function getTraceClass(type) {
    switch (type) {
      case 'THINKING': return 'thinking';
      case 'TOOL_CALL': return 'tool-call';
      case 'TOOL_RESULT': return 'tool-result';
      case 'ERROR': return 'error';
      default: return '';
    }
  }

  function formatDuration(ms) {
    if (!ms) return '';
    if (ms < 1000) return `${ms}ms`;
    return `${(ms / 1000).toFixed(1)}s`;
  }

  return (
    <div className="agent-trace">
      <div className="trace-header" onClick={() => setIsExpanded(!isExpanded)}>
        <span>{isExpanded ? '▼' : '▶'}</span>
        <span>Agent Trace — {traces.length} steps</span>
        <span style={{ marginLeft: 'auto', fontFamily: 'var(--font-mono)' }}>
          {formatDuration(traces.reduce((sum, t) => sum + (t.durationMs || 0), 0))} total
        </span>
      </div>
      
      {isExpanded && (
        <div className="trace-steps">
          {traces.map((trace, index) => (
            <div key={index} className={`trace-step ${getTraceClass(trace.type)}`}>
              <span className="trace-icon">{getTraceIcon(trace.type)}</span>
              <div className="trace-detail">
                <div className="trace-label">
                  {trace.type === 'TOOL_CALL' && trace.toolName
                    ? `Calling: ${trace.toolName}`
                    : trace.type === 'TOOL_RESULT' && trace.toolName
                    ? `Result: ${trace.toolName}`
                    : trace.type === 'THINKING'
                    ? 'Thinking'
                    : trace.content || trace.type}
                </div>
                {trace.toolInput && (
                  <div className="trace-content">Input: {trace.toolInput}</div>
                )}
                {trace.toolOutput && (
                  <div className="trace-content">Output: {trace.toolOutput}</div>
                )}
                {trace.content && trace.type === 'THINKING' && (
                  <div className="trace-content">{trace.content}</div>
                )}
              </div>
              <span className="trace-duration">{formatDuration(trace.durationMs)}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
