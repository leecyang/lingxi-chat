/**
 * 统一的API服务层
 * 封装所有API调用，确保认证头正确添加
 */

import { api, API_ENDPOINTS } from '@/lib/api';

// 认证相关API
export const authService = {
  login: (credentials: { username: string; password: string }) =>
    api.post(API_ENDPOINTS.AUTH.LOGIN, credentials),
    
  register: (userData: { username: string; password: string; email: string; name: string; studentId: string; college: string; className: string }) =>
    api.post(API_ENDPOINTS.AUTH.REGISTER, userData),
    
  validateToken: () =>
    api.get(API_ENDPOINTS.AUTH.VALIDATE),
    
  refreshToken: () =>
    api.post(API_ENDPOINTS.AUTH.REFRESH),
    
  logout: () =>
    api.post(API_ENDPOINTS.AUTH.LOGOUT),
};

// 智能体相关API
export const agentService = {
  getAgents: () =>
    api.get(API_ENDPOINTS.AGENTS.LIST),
    
  getAgentById: (id: string) =>
    api.get(API_ENDPOINTS.AGENTS.DETAIL(id)),
    
  getPendingAgents: () =>
    api.get(API_ENDPOINTS.AGENTS.PENDING),
    
  submitAgent: (agentData: any) =>
    api.post(API_ENDPOINTS.AGENT_SUBMISSIONS.SUBMIT, agentData),
    
  getPendingSubmissions: () =>
    api.get(API_ENDPOINTS.AGENT_SUBMISSIONS.PENDING),
    
  getUserSubmissions: (userId: string) =>
    api.get(API_ENDPOINTS.AGENT_SUBMISSIONS.USER(userId)),
    
  reviewSubmission: (id: string, reviewData: any) =>
    api.post(API_ENDPOINTS.AGENT_SUBMISSIONS.REVIEW(id), reviewData),

  reviewAgent: (id: string, status: string, comment: string, reviewerId?: string) =>
    api.post(`/agents/${id}/review`, { status, comment, reviewerId }),

  getActiveAgents: () =>
    api.get('/agents/active'),
    
  updateAgent: (id: string, agentData: any) =>
    api.put(API_ENDPOINTS.AGENTS.UPDATE(id), agentData),
    
  deleteAgent: (id: string) =>
    api.delete(API_ENDPOINTS.AGENTS.DELETE(id)),
    
  activateAgent: (id: string) =>
    api.post(`/agents/${id}/toggle`, { enabled: true }),
    
  deactivateAgent: (id: string) =>
    api.post(`/agents/${id}/toggle`, { enabled: false }),
};

// 聊天相关API
export const chatService = {
  getChatHistory: (sessionId?: string) => {
    const params = sessionId ? { sessionId } : {};
    return api.get('/chat/history', params);
  },
    
  sendMessage: (content: string) =>
    api.post('/chat/send', { content }),
    
  sendMessageWithAgent: (content: string, agentId?: number | null, sessionId?: string) => {
    const requestBody: any = { content };
    if (agentId !== null && agentId !== undefined) {
      requestBody.agentId = agentId;
    }
    if (sessionId) {
      requestBody.sessionId = sessionId;
    }
    return api.post('/chat/send', requestBody);
  },

  sendMessageStream: (sessionId: string, content: string, agentId: number | null, onChunk: (chunk: string) => void, onComplete: (response: any) => void, onError: (error: string) => void) => {
    const token = localStorage.getItem('access_token');
    if (!token) {
      onError('未登录');
      return;
    }

    // Use fetch for POST with SSE
    // 开始发送SSE请求
     fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'}/chat/send/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        'Accept': 'text/event-stream',
        'Cache-Control': 'no-cache'
      },
      body: JSON.stringify({ sessionId, content, agentId })
    }).then(async response => {
      // 收到SSE响应
      if (!response.ok) {
        if (response.status === 401) {
          try {
            const errorData = await response.json();
            onError(errorData.error || '未登录');
          } catch {
            onError('未登录');
          }
          return;
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('无法获取响应流');
      }
      // 成功获取SSE流读取器

      const decoder = new TextDecoder();
      let buffer = '';
      let currentEvent = '';

      function readStream(): Promise<void> {
        return reader.read().then(({ done, value }) => {
          // 读取流数据
      if (done) {
        // 流读取完成
            return;
          }

          buffer += decoder.decode(value, { stream: true });
          // 当前buffer内容
          const lines = buffer.split('\n');
          buffer = lines.pop() || ''; // 保留不完整的行
          // 分割后的行数

          for (const line of lines) {
            const trimmedLine = line.trim();
            if (!trimmedLine) {
              // 空行表示一个SSE事件的结束
              currentEvent = '';
              continue;
            }
            
            if (trimmedLine.startsWith('event:')) {
              currentEvent = trimmedLine.slice(6).trim();
              // SSE事件类型
            } else if (trimmedLine.startsWith('data:')) {
              const data = trimmedLine.slice(5).trim();
              if (data === '[DONE]') {
                onComplete(null);
                return;
              }

              try {
                const parsed = JSON.parse(data);
                // 解析的SSE数据
                
                // 根据事件类型处理数据
                if (currentEvent === 'chunk' || parsed.chunk) {
                  // 处理流式文本块
                  const chunkText = parsed.chunk || data;
                  if (chunkText) {
                    // 接收到文本块
                    onChunk(chunkText);
                  } else {
                    // 文本块为空
                  }
                } else if (currentEvent === 'agent_complete' || parsed.chatHistory) {
                  // 处理完成事件
                  // 智能体回复完成
                  onComplete(parsed.chatHistory || parsed);
                } else if (currentEvent === 'error' || parsed.error) {
                  // 处理错误事件
                  // 收到错误
                  onError(parsed.error || parsed.message || '未知错误');
                } else if (currentEvent === 'user_message' || currentEvent === 'agent_start') {
                  // 处理状态消息
                  // 收到状态消息
                } else {
                  // 处理其他类型的数据
                  if (parsed.chunk) {
                    onChunk(parsed.chunk);
                  } else if (parsed.error) {
                    onError(parsed.error);
                  } else if (parsed.message) {
                    // 收到消息
                  } else if (parsed.chatHistory) {
                    // 只记录chatHistory，不调用onComplete，避免重复处理
                    // 收到聊天历史数据
                  } else {
                    // 未处理的SSE数据格式
                  }
                }
              } catch (e) {
                // 解析SSE数据失败
                // 如果不是JSON格式，可能是纯文本chunk
                if (data && data !== '[DONE]') {
                  // 作为纯文本处理
                  onChunk(data);
                }
              }
            }
          }

          return readStream();
        });
      }

      return readStream();
    }).catch(error => {
      // SSE连接错误
      onError(error.message || '连接失败');
    });
  },
    
  getSessionMessages: (sessionId: string) =>
    api.get(API_ENDPOINTS.CHAT.HISTORY, { sessionId }),
};

// 管理员相关API
export const adminService = {
  getPendingAgents: () =>
    api.get(API_ENDPOINTS.AGENTS.PENDING),
    
  getPendingSubmissions: () =>
    api.get(API_ENDPOINTS.AGENT_SUBMISSIONS.PENDING),
    
  getDeveloperRequests: () =>
    api.get(API_ENDPOINTS.DEVELOPER_REQUESTS.PENDING),
    
  getConversationLogs: () =>
    api.get(API_ENDPOINTS.CONVERSATION_LOGS.REVIEW),
    
  getFlaggedLogs: () =>
    api.get(API_ENDPOINTS.CONVERSATION_LOGS.FLAGGED),

  getApprovedAgents: () =>
    api.get(API_ENDPOINTS.AGENTS.APPROVED),
};

// 开发者相关API
export const developerService = {
  applyForDeveloper: (applicationData: any) =>
    api.post(API_ENDPOINTS.DEVELOPER_REQUESTS.CREATE, applicationData),
    
  getPendingRequests: () =>
    api.get(API_ENDPOINTS.DEVELOPER_REQUESTS.PENDING),
    
  getUserRequests: (userId: string) =>
    api.get(API_ENDPOINTS.DEVELOPER_REQUESTS.USER(userId)),
    
  reviewRequest: (id: string, reviewData: any) =>
    api.post(API_ENDPOINTS.DEVELOPER_REQUESTS.REVIEW(id), reviewData),
    
  cancelRequest: (id: string, userId: string) =>
    api.post(API_ENDPOINTS.DEVELOPER_REQUESTS.CANCEL(id), { userId }),
};

// 对话日志相关API
export const conversationLogService = {
  getLogsNeedingReview: () =>
    api.get(API_ENDPOINTS.CONVERSATION_LOGS.REVIEW),
    
  getFlaggedLogs: () =>
    api.get(API_ENDPOINTS.CONVERSATION_LOGS.FLAGGED),
    
  getUserLogs: (userId: string) =>
    api.get(API_ENDPOINTS.CONVERSATION_LOGS.USER(userId)),
    
  getAgentLogs: (agentId: string) =>
    api.get(API_ENDPOINTS.CONVERSATION_LOGS.AGENT(agentId)),
    
  flagLog: (id: string, flagData: any) =>
    api.post(API_ENDPOINTS.CONVERSATION_LOGS.FLAG(id), flagData),
    
  unflagLog: (id: string, userId: string) =>
    api.post(API_ENDPOINTS.CONVERSATION_LOGS.UNFLAG(id), { userId }),
};

// 用户相关API
export const userService = {
  getProfile: () =>
    api.get(API_ENDPOINTS.AUTH.ME),
    
  // TODO: 后端需要添加用户资料更新端点
  // updateProfile: (userData: any) =>
  //   api.put('/users/profile', userData),
};

// 认证辅助API
export const authHelperService = {
  checkUsername: (username: string) =>
    api.get(API_ENDPOINTS.AUTH.CHECK_USERNAME, { username }),
};