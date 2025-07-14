/**
 * 通用类型定义
 */

// 用户类型
export interface User {
  id?: string | number;
  name: string;
  username?: string;
  email?: string;
  studentId?: string;
  college?: string;
  className?: string;
  role: 'student' | 'teacher' | 'developer' | 'admin';
  avatar?: string;
  createdAt?: string;
  updatedAt?: string;
}

// API响应类型
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  code?: number;
}

// 错误类型
export interface ApiError {
  message: string;
  code?: number;
  details?: any;
}

// 分页类型
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// 表单状态类型
export interface FormState {
  isLoading: boolean;
  errors: Record<string, string>;
  touched: Record<string, boolean>;
}

// 智能体提交类型
export interface AgentSubmission {
  id?: string;
  name: string;
  description: string;
  apiUrl: string;
  appId: string;
  apiKey: string;
  token: string;
  category: string;
  submitterId?: string | number;
  submitterRole?: string;
  status: 'pending' | 'approved' | 'rejected';
  createdAt?: string;
  updatedAt?: string;
}

// 开发者申请类型
export interface DeveloperApplication {
  id?: string;
  reason: string;
  skills?: string;  // 对应后端的 skills 字段
  experience?: string;  // 对应后端的 experience 字段
  contactInfo?: string;  // 对应后端的 contactInfo 字段
  portfolio?: string;  // 前端额外字段，可选
  github?: string;  // 前端额外字段，可选
  contact?: string;  // 前端额外字段，保持兼容性
  user?: {
    id: number;
    username: string;
    nickname?: string;
    name?: string;
  };
  reviewer?: {
    id: number;
    username: string;
    nickname?: string;
    name?: string;
  };
  reviewTime?: string;
  reviewNotes?: string;
  applicantId?: string | number;
  applicantName?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'pending' | 'approved' | 'rejected';
  createdAt?: string;
  updatedAt?: string;
}

// 对话日志类型
export interface ConversationLog {
  id: number;
  sessionId: string;
  user: {
    id: number;
    name: string;
    username: string;
  };
  agent: {
    id: number;
    name: string;
    description: string;
  };
  messageCount: number;
  durationMinutes?: number;
  totalTokens?: number;
  costAmount?: number;
  status: 'ACTIVE' | 'COMPLETED' | 'TERMINATED' | 'ARCHIVED' | 'DELETED';
  isFlagged: boolean;
  flagReason?: string;
  flaggedBy?: {
    id: number;
    name: string;
  };
  flaggedAt?: string;
  contentRating?: 'SAFE' | 'QUESTIONABLE' | 'INAPPROPRIATE' | 'HARMFUL';
  summary?: string;
  lastActivityAt?: string;
  createdAt: string;
  updatedAt: string;
}