import { useState } from 'react';
import AgentTrace from './AgentTrace';

export default function MessageBubble({ message }) {
  const isUser = message.role === 'USER';
  const isSystem = message.role === 'SYSTEM';
  const isAssistant = message.role === 'ASSISTANT';

  function formatTime(timestamp) {
    if (!timestamp) return '';
    const d = new Date(timestamp);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  function formatDuration(ms) {
    if (!ms) return '';
    if (ms < 1000) return `${ms}ms`;
    return `${(ms / 1000).toFixed(1)}s`;
  }

  if (isSystem) {
    return (
      <div className="message-row" style={{ justifyContent: 'center' }}>
        <div className="message-bubble" style={{
          background: 'rgba(239, 68, 68, 0.08)',
          border: '1px solid rgba(239, 68, 68, 0.2)',
          borderRadius: 'var(--radius-md)',
          color: 'var(--accent-error)',
          fontSize: '0.82rem',
          maxWidth: '600px',
          textAlign: 'center',
        }}>
          {message.content}
        </div>
      </div>
    );
  }

  return (
    <div className={`message-row ${isUser ? 'user' : ''}`}>
      <div className={`message-avatar ${isUser ? 'user' : 'ai'}`}>
        {isUser ? '👤' : '⚡'}
      </div>
      <div className="message-content">
        <div className={`message-bubble ${isUser ? 'user' : 'ai'}`}>
          {message.content.split('\n').map((line, i) => (
            <p key={i}>{line || '\u00A0'}</p>
          ))}
        </div>
        <div className="message-meta">
          <span>{formatTime(message.timestamp)}</span>
          {isAssistant && message.processingTimeMs && (
            <span>• {formatDuration(message.processingTimeMs)}</span>
          )}
          {isAssistant && message.traces && message.traces.length > 0 && (
            <span>• {message.traces.length} steps</span>
          )}
        </div>
        
        {/* Agent execution trace */}
        {isAssistant && message.traces && message.traces.length > 0 && (
          <AgentTrace traces={message.traces} />
        )}
      </div>
    </div>
  );
}
