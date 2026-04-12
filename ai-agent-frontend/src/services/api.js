const API_BASE = 'http://localhost:8080/api';

/**
 * Send a chat message to the agent and receive the full response with traces.
 */
export async function sendMessage(message, conversationId = null) {
  const body = { message };
  if (conversationId) {
    body.conversationId = conversationId;
  }

  const response = await fetch(`${API_BASE}/agent/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error || `Request failed with status ${response.status}`);
  }

  return response.json();
}

/**
 * Get all conversations sorted by most recent.
 */
export async function getConversations() {
  const response = await fetch(`${API_BASE}/conversations`);
  if (!response.ok) throw new Error('Failed to fetch conversations');
  return response.json();
}

/**
 * Get messages for a specific conversation.
 */
export async function getMessages(conversationId) {
  const response = await fetch(`${API_BASE}/conversations/${conversationId}/messages`);
  if (!response.ok) throw new Error('Failed to fetch messages');
  return response.json();
}

/**
 * Create a new conversation.
 */
export async function createConversation(title = 'New Conversation') {
  const response = await fetch(`${API_BASE}/conversations`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title }),
  });
  if (!response.ok) throw new Error('Failed to create conversation');
  return response.json();
}

/**
 * Delete a conversation.
 */
export async function deleteConversation(conversationId) {
  const response = await fetch(`${API_BASE}/conversations/${conversationId}`, {
    method: 'DELETE',
  });
  if (!response.ok) throw new Error('Failed to delete conversation');
}

/**
 * Get dashboard statistics.
 */
export async function getStats() {
  const response = await fetch(`${API_BASE}/conversations/stats`);
  if (!response.ok) throw new Error('Failed to fetch stats');
  return response.json();
}

/**
 * Get registered tools list.
 */
export async function getTools() {
  const response = await fetch(`${API_BASE}/tools`);
  if (!response.ok) throw new Error('Failed to fetch tools');
  return response.json();
}

/**
 * Check system health (Ollama connectivity, etc.).
 */
export async function checkHealth() {
  const response = await fetch(`${API_BASE}/health`);
  if (!response.ok) throw new Error('Health check failed');
  return response.json();
}
