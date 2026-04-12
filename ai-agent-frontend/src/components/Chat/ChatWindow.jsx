import { useState, useRef, useEffect } from 'react';
import { useAgent } from '../../context/AgentContext';
import { sendMessage, getMessages, getConversations } from '../../services/api';
import MessageBubble from './MessageBubble';
import ChatInput from './ChatInput';

export default function ChatWindow() {
  const { state, dispatch } = useAgent();
  const messagesEndRef = useRef(null);
  const [isThinking, setIsThinking] = useState(false);

  // Load messages when conversation changes
  useEffect(() => {
    if (state.currentConversationId) {
      loadMessages(state.currentConversationId);
    }
  }, [state.currentConversationId]);

  // Auto-scroll to bottom
  useEffect(() => {
    scrollToBottom();
  }, [state.messages, isThinking]);

  async function loadMessages(conversationId) {
    try {
      const msgs = await getMessages(conversationId);
      dispatch({ type: 'SET_MESSAGES', payload: msgs });
    } catch (err) {
      console.error('Failed to load messages:', err);
    }
  }

  function scrollToBottom() {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }

  async function handleSend(message) {
    // Add user message to UI immediately
    const userMsg = {
      id: 'temp-' + Date.now(),
      content: message,
      role: 'USER',
      timestamp: new Date().toISOString(),
    };
    dispatch({ type: 'ADD_MESSAGE', payload: userMsg });
    dispatch({ type: 'SET_PROCESSING', payload: true });
    dispatch({ type: 'CLEAR_TRACES' });
    setIsThinking(true);

    try {
      const response = await sendMessage(message, state.currentConversationId);

      if (response.success) {
        // Set conversation ID if this is a new conversation
        if (!state.currentConversationId && response.conversationId) {
          dispatch({ type: 'SET_CURRENT_CONVERSATION', payload: response.conversationId });
        }

        // Add AI response
        const aiMsg = {
          id: 'ai-' + Date.now(),
          content: response.content,
          role: 'ASSISTANT',
          timestamp: new Date().toISOString(),
          processingTimeMs: response.processingTimeMs,
          traces: response.traces || [],
        };
        dispatch({ type: 'ADD_MESSAGE', payload: aiMsg });
        dispatch({ type: 'SET_TRACES', payload: response.traces || [] });

        // Refresh conversation list
        try {
          const convos = await getConversations();
          dispatch({ type: 'SET_CONVERSATIONS', payload: convos });
        } catch {}
      } else {
        dispatch({
          type: 'ADD_MESSAGE',
          payload: {
            id: 'error-' + Date.now(),
            content: `Error: ${response.error || 'Unknown error occurred'}`,
            role: 'SYSTEM',
            timestamp: new Date().toISOString(),
          },
        });
      }
    } catch (err) {
      dispatch({
        type: 'ADD_MESSAGE',
        payload: {
          id: 'error-' + Date.now(),
          content: `Connection error: ${err.message}. Make sure the Spring Boot backend is running on port 8080.`,
          role: 'SYSTEM',
          timestamp: new Date().toISOString(),
        },
      });
    } finally {
      dispatch({ type: 'SET_PROCESSING', payload: false });
      setIsThinking(false);
    }
  }

  // If no messages, show welcome screen
  if (state.messages.length === 0 && !state.currentConversationId) {
    return (
      <div className="chat-page">
        <div className="welcome-screen">
          <div className="welcome-icon">⚡</div>
          <h2>How can I help you today?</h2>
          <p>
            I'm a ReAct AI Agent powered by Llama 3.1. I can search the web, 
            summarize information, and write files — all running locally on your machine.
          </p>
          <div className="welcome-cards">
            <div className="welcome-card" onClick={() => handleSend('Search for the latest news about AI agents')}>
              <div className="card-icon">🔍</div>
              <h4>Web Search</h4>
              <p>Search the web for real-time information and news</p>
            </div>
            <div className="welcome-card" onClick={() => handleSend('Write a short poem about artificial intelligence and save it to a file')}>
              <div className="card-icon">📝</div>
              <h4>Write Files</h4>
              <p>Generate content and save it to files</p>
            </div>
            <div className="welcome-card" onClick={() => handleSend('Search for React best practices and summarize the key points')}>
              <div className="card-icon">📋</div>
              <h4>Summarize</h4>
              <p>Condense long content into concise summaries</p>
            </div>
          </div>
        </div>
        <ChatInput onSend={handleSend} disabled={state.isProcessing} />
      </div>
    );
  }

  return (
    <div className="chat-page">
      <div className="chat-messages">
        {state.messages.map((msg) => (
          <MessageBubble key={msg.id} message={msg} />
        ))}
        
        {isThinking && (
          <div className="message-row">
            <div className="message-avatar ai">⚡</div>
            <div className="message-content">
              <div className="thinking-indicator">
                <div className="thinking-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
                <span>Agent is thinking...</span>
              </div>
            </div>
          </div>
        )}
        
        <div ref={messagesEndRef} />
      </div>
      <ChatInput onSend={handleSend} disabled={state.isProcessing} />
    </div>
  );
}
