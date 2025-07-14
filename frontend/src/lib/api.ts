/**
 * API配置和请求工具
 */

// API基础配置
export const API_CONFIG = {
  BASE_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
  WEBSOCKET_URL: process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws/chat',
  TIMEOUT: 30000,
};

// API端点
export const API_ENDPOINTS = {
  // 认证相关
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    REFRESH: '/auth/refresh',
    LOGOUT: '/auth/logout',
    ME: '/auth/me',
    CHECK_USERNAME: '/auth/check-username',
    VALIDATE: '/auth/validate',
  },
  // 聊天相关
  CHAT: {
    SEND: '/chat/send',
    HISTORY: '/chat/history',
    SESSIONS: '/chat/sessions',
    LAST_MESSAGE: (sessionId: string) => `/chat/sessions/${sessionId}/last`,
    RATE: '/chat/rate',
  },
  // 智能体相关
  AGENTS: {
    LIST: '/agents',
    CREATE: '/agents',
    DETAIL: (id: string) => `/agents/${id}`,
    UPDATE: (id: string) => `/agents/${id}`,
    DELETE: (id: string) => `/agents/${id}`,
    CHAT: (id: string) => `/agents/${id}/chat`,
    PENDING: '/agents/pending',
    APPROVED: '/agents/approved',
  },
  // 智能体提交申请相关
  AGENT_SUBMISSIONS: {
    SUBMIT: '/agent-submissions/submit',
    LIST: '/agent-submissions',
    PENDING: '/agent-submissions/pending',
    USER: (userId: string) => `/agent-submissions/user/${userId}`,
    REVIEW: (id: string) => `/agent-submissions/review/${id}`,
    DELETE: (id: string) => `/agent-submissions/${id}`,
  },
  // 开发者申请相关
  DEVELOPER_REQUESTS: {
    CREATE: '/developer-requests',
    PENDING: '/developer-requests/pending',
    APPROVED: '/developer-requests/approved',
    REJECTED: '/developer-requests/rejected',
    USER: (userId: string) => `/developer-requests/user/${userId}`,
    REVIEW: (id: string) => `/developer-requests/${id}/review`,
    CANCEL: (id: string) => `/developer-requests/${id}/cancel`,
  },
  // 对话日志相关
  CONVERSATION_LOGS: {
    CREATE: '/conversation-logs',
    LIST: '/conversation-logs',
    REVIEW: '/conversation-logs/review',
    FLAGGED: '/conversation-logs/flagged',
    USER: (userId: string) => `/conversation-logs/user/${userId}`,
    AGENT: (agentId: string) => `/conversation-logs/agent/${agentId}`,
    SESSION: (sessionId: string) => `/conversation-logs/session/${sessionId}`,
    FLAG: (id: string) => `/conversation-logs/${id}/flag`,
    UNFLAG: (id: string) => `/conversation-logs/${id}/unflag`,
  },
  // 成绩相关
  GRADES: {
    LIST: '/grades',
    CREATE: '/grades',
    BATCH_IMPORT: '/grades/batch',
    STATISTICS: '/grades/statistics',
    TRENDS: '/grades/trends',
    PREDICTION: '/grades/prediction',
  },
};

// 请求类型定义
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  code?: number;
}

export interface ApiError {
  message: string;
  code?: number;
  details?: any;
}

// 请求配置
export interface RequestConfig {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  headers?: Record<string, string>;
  body?: any;
  timeout?: number;
}

// 获取认证头
function getAuthHeaders(): Record<string, string> {
  const token = localStorage.getItem('access_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

// Token刷新标志，防止并发刷新
let isRefreshingToken = false;
let refreshTokenPromise: Promise<boolean> | null = null;

// 检查是否为认证错误
function isAuthenticationError(response: Response, data: any): boolean {
  // HTTP状态码检查
  if (response.status === 401 || response.status === 403) {
    return true;
  }
  
  // 检查响应体中的错误信息
  if (data) {
    const message = data.message || data.error || '';
    const lowerMessage = message.toLowerCase();
    
    // 检查常见的认证错误关键字
    if (lowerMessage.includes('authentication') || 
        lowerMessage.includes('token') || 
        lowerMessage.includes('unauthorized') || 
        lowerMessage.includes('身份验证') ||
        lowerMessage.includes('已过期') ||
        lowerMessage.includes('invalid token') ||
        lowerMessage.includes('token expired') ||
        lowerMessage.includes('无效的认证信息') ||
        message.includes('Authentication Token已过期，请重新生成')) {
      return true;
    }
    
    // 检查九天平台特定的错误码
    if (data.code === 1001 || data.code === 1002) {
      return true;
    }
  }
  
  return false;
}

// 刷新Token
async function refreshAuthToken(): Promise<boolean> {
  if (isRefreshingToken && refreshTokenPromise) {
    return refreshTokenPromise;
  }
  
  isRefreshingToken = true;
  refreshTokenPromise = (async () => {
    try {
      const refreshToken = localStorage.getItem('refresh_token');
      if (!refreshToken) {
        // 没有找到refresh token
        return false;
      }
      
      const response = await fetch(`${API_CONFIG.BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          refreshToken: refreshToken
        })
      });
      
      if (response.ok) {
        const data = await response.json();
        if (data.accessToken) {
          // 更新本地存储的token
          localStorage.setItem('access_token', data.accessToken);
          if (data.refreshToken) {
            localStorage.setItem('refresh_token', data.refreshToken);
          }
          // Token刷新成功
          return true;
        }
      }
      
      // Token刷新失败
      // 清除无效的token
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      return false;
    } catch (error) {
      // Token刷新异常
      return false;
    } finally {
      isRefreshingToken = false;
      refreshTokenPromise = null;
    }
  })();
  
  return refreshTokenPromise;
}

// 基础请求函数
export async function apiRequest<T = any>(
  endpoint: string,
  config: RequestConfig = {},
  retryCount = 0
): Promise<ApiResponse<T>> {
  const {
    method = 'GET',
    headers = {},
    body,
    timeout = API_CONFIG.TIMEOUT,
  } = config;

  const url = `${API_CONFIG.BASE_URL}${endpoint}`;
  
  const requestHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    ...getAuthHeaders(),
    ...headers,
  };

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  try {
    const response = await fetch(url, {
      method,
      headers: requestHeaders,
      body: body ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });

    clearTimeout(timeoutId);

    const data = await response.json();
    
    if (!response.ok) {
      // 检查是否为认证错误且未重试过
      if (isAuthenticationError(response, data) && retryCount === 0) {
        // 检测到认证错误，尝试刷新Token
        const refreshSuccess = await refreshAuthToken();
        
        if (refreshSuccess) {
          // Token刷新成功，重试请求
          // 递归调用，重试请求
          return apiRequest(endpoint, config, retryCount + 1);
        } else {
          // Token刷新失败，跳转到登录页面
          // Token刷新失败，跳转到登录页面
          if (typeof window !== 'undefined') {
            window.location.href = '/login';
          }
        }
      }
      
      // 返回错误格式的ApiResponse
      return {
        success: false,
        message: data.message || data.error || `HTTP ${response.status}: ${response.statusText}`,
        code: response.status
      };
    }

    // 检查后端返回的数据是否已经是ApiResponse格式
    if (typeof data.success === 'boolean') {
      return data;
    }

    // 如果不是，则包装成ApiResponse格式
    return {
      success: true,
      data: data,
    };
  } catch (error) {
    clearTimeout(timeoutId);
    
    if (error instanceof Error) {
      if (error.name === 'AbortError') {
        return {
          success: false,
          message: '请求超时'
        };
      }
      return {
        success: false,
        message: error.message
      };
    }
    
    return {
      success: false,
      message: '网络请求失败'
    };
  }
}

// 便捷方法
export const api = {
  get: <T = any>(endpoint: string, config?: Omit<RequestConfig, 'method'>) =>
    apiRequest<T>(endpoint, { ...config, method: 'GET' }),
    
  post: <T = any>(endpoint: string, body?: any, config?: Omit<RequestConfig, 'method' | 'body'>) =>
    apiRequest<T>(endpoint, { ...config, method: 'POST', body }),
    
  put: <T = any>(endpoint: string, body?: any, config?: Omit<RequestConfig, 'method' | 'body'>) =>
    apiRequest<T>(endpoint, { ...config, method: 'PUT', body }),
    
  delete: <T = any>(endpoint: string, config?: Omit<RequestConfig, 'method'>) =>
    apiRequest<T>(endpoint, { ...config, method: 'DELETE' }),
    
  patch: <T = any>(endpoint: string, body?: any, config?: Omit<RequestConfig, 'method' | 'body'>) =>
    apiRequest<T>(endpoint, { ...config, method: 'PATCH', body }),
};

// WebSocket连接管理
export class WebSocketManager {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private listeners: Map<string, Function[]> = new Map();

  connect(sessionId?: string): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        const url = sessionId 
          ? `${API_CONFIG.WEBSOCKET_URL}?sessionId=${sessionId}`
          : API_CONFIG.WEBSOCKET_URL;
          
        this.ws = new WebSocket(url);
        
        this.ws.onopen = () => {
          // WebSocket连接已建立
          this.reconnectAttempts = 0;
          resolve();
        };
        
        this.ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            this.emit('message', data);
          } catch (error) {
            // 解析WebSocket消息失败
          }
        };
        
        this.ws.onclose = () => {
          // WebSocket连接已关闭
          this.emit('close');
          this.attemptReconnect();
        };
        
        this.ws.onerror = (error) => {
          // WebSocket错误
          this.emit('error', error);
          reject(error);
        };
      } catch (error) {
        reject(error);
      }
    });
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  send(data: any): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    } else {
      // WebSocket未连接
    }
  }

  on(event: string, callback: Function): void {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event)!.push(callback);
  }

  off(event: string, callback: Function): void {
    const callbacks = this.listeners.get(event);
    if (callbacks) {
      const index = callbacks.indexOf(callback);
      if (index > -1) {
        callbacks.splice(index, 1);
      }
    }
  }

  private emit(event: string, data?: any): void {
    const callbacks = this.listeners.get(event);
    if (callbacks) {
      callbacks.forEach(callback => callback(data));
    }
  }

  private attemptReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      // 尝试重连WebSocket
      
      setTimeout(() => {
        this.connect();
      }, this.reconnectDelay * this.reconnectAttempts);
    } else {
      // WebSocket重连失败，已达到最大重试次数
    }
  }
}

// 导出WebSocket实例
export const wsManager = new WebSocketManager();