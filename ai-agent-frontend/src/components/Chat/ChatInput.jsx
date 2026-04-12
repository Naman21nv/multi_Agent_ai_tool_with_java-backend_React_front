import { useState, useRef, useEffect } from 'react';

export default function ChatInput({ onSend, disabled }) {
  const [message, setMessage] = useState('');
  const textareaRef = useRef(null);

  // Auto-resize textarea
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = Math.min(textareaRef.current.scrollHeight, 120) + 'px';
    }
  }, [message]);

  function handleSubmit(e) {
    e?.preventDefault();
    const trimmed = message.trim();
    if (!trimmed || disabled) return;
    onSend(trimmed);
    setMessage('');
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  }

  return (
    <div className="chat-input-container">
      <div className="chat-input-wrapper">
        <form onSubmit={handleSubmit} className="chat-input-box">
          <textarea
            ref={textareaRef}
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask the AI Agent anything..."
            disabled={disabled}
            rows={1}
            id="chat-input"
          />
          <button
            type="submit"
            className="send-btn"
            disabled={disabled || !message.trim()}
            id="send-button"
            aria-label="Send message"
          >
            ➤
          </button>
        </form>
        <div className="chat-input-hint">
          Press Enter to send • Shift+Enter for new line • Powered by Ollama + Llama 3.1
        </div>
      </div>
    </div>
  );
}
