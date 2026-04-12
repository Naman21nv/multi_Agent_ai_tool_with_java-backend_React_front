import { createContext, useContext, useReducer, useCallback } from 'react';

const AgentContext = createContext(null);

const initialState = {
  // Chat state
  conversations: [],
  currentConversationId: null,
  messages: [],
  isProcessing: false,
  traces: [],
  
  // System state
  ollamaConnected: false,
  tools: [],
  stats: null,
  
  // UI state
  error: null,
};

function agentReducer(state, action) {
  switch (action.type) {
    case 'SET_CONVERSATIONS':
      return { ...state, conversations: action.payload };
    
    case 'SET_CURRENT_CONVERSATION':
      return { ...state, currentConversationId: action.payload };
    
    case 'SET_MESSAGES':
      return { ...state, messages: action.payload };
    
    case 'ADD_MESSAGE':
      return { ...state, messages: [...state.messages, action.payload] };
    
    case 'SET_PROCESSING':
      return { ...state, isProcessing: action.payload };
    
    case 'SET_TRACES':
      return { ...state, traces: action.payload };
    
    case 'ADD_TRACE':
      return { ...state, traces: [...state.traces, action.payload] };
    
    case 'CLEAR_TRACES':
      return { ...state, traces: [] };
    
    case 'SET_OLLAMA_STATUS':
      return { ...state, ollamaConnected: action.payload };
    
    case 'SET_TOOLS':
      return { ...state, tools: action.payload };
    
    case 'SET_STATS':
      return { ...state, stats: action.payload };
    
    case 'SET_ERROR':
      return { ...state, error: action.payload };
    
    case 'CLEAR_ERROR':
      return { ...state, error: null };
    
    case 'NEW_CONVERSATION':
      return {
        ...state,
        currentConversationId: null,
        messages: [],
        traces: [],
        error: null,
      };
    
    default:
      return state;
  }
}

export function AgentProvider({ children }) {
  const [state, dispatch] = useReducer(agentReducer, initialState);

  const clearError = useCallback(() => dispatch({ type: 'CLEAR_ERROR' }), []);
  const setError = useCallback((error) => dispatch({ type: 'SET_ERROR', payload: error }), []);
  const newConversation = useCallback(() => dispatch({ type: 'NEW_CONVERSATION' }), []);

  return (
    <AgentContext.Provider value={{ state, dispatch, clearError, setError, newConversation }}>
      {children}
    </AgentContext.Provider>
  );
}

export function useAgent() {
  const context = useContext(AgentContext);
  if (!context) {
    throw new Error('useAgent must be used within an AgentProvider');
  }
  return context;
}
