import type { Agent } from './agents';

export interface Message {
  id: string;
  sender: 'user' | 'system' | Agent | string;
  content: string;
  timestamp: string;
  isTyping?: boolean;
}
