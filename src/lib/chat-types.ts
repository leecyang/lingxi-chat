import type { Agent } from './agents';

export interface Message {
  id: string;
  sender: 'user' | Agent;
  content: string;
  timestamp: string;
  isTyping?: boolean;
}
