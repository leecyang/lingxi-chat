import type { LucideIcon } from 'lucide-react';
import { Calculator, BookOpen, MessageSquareQuote, Languages, BrainCircuit } from 'lucide-react';
import { agentService } from '@/services/api-service';

export interface Agent {
  id: number;
  name: string;
  displayName: string;
  description: string;
  modelId: string;
  endpoint: string;
  avatar: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED';
  type: 'JIUTIAN' | 'LOCAL' | 'CUSTOM';
  category?: 'MORAL_EDUCATION' | 'INTELLECTUAL_EDUCATION' | 'PHYSICAL_EDUCATION' | 'AESTHETIC_EDUCATION' | 'LABOR_EDUCATION' | 'LIFE_SUPPORT';
  creator: {
    id: number;
    username: string;
    nickname: string;
  };
  approver?: {
    id: number;
    username: string;
    nickname: string;
  };
  approvalTime?: string;
  approvalNotes?: string;
  config?: Record<string, string>;
  totalCalls: number;
  successCalls: number;
  lastCallTime?: string;
  averageResponseTime?: number;
  enabled: boolean;
  priority: number;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  // 前端扩展字段
  icon?: LucideIcon;
  dataAiHint?: string;
}

// 智能体数据将从后端API获取
export let availableAgents: Agent[] = [];

// 从后端获取智能体数据的函数
export async function fetchAgents(): Promise<Agent[]> {
  try {
    const response = await agentService.getAgents();
    if (response.success && response.data) {
      // 后端返回格式: {agents: [...], total: number}
      const agents = response.data.agents || response.data || [];
      availableAgents = agents;
      return agents;
    }
    return [];
  } catch (error) {
    // 获取智能体失败
    return [];
  }
}

// 根据ID获取智能体
export function getAgentById(id: number | string): Agent | undefined {
  const numId = typeof id === 'string' ? parseInt(id, 10) : id;
  return availableAgents.find(agent => agent.id === numId);
}

// 根据名称获取智能体
export function getAgentByName(name: string): Agent | undefined {
  return availableAgents.find(agent => agent.name === name);
}

// 刷新智能体缓存
export async function refreshAgents(): Promise<void> {
  await fetchAgents();
}

// 清空智能体缓存
export function clearAgentsCache(): void {
  availableAgents = [];
}
