import type { LucideIcon } from 'lucide-react';
import { Calculator, BookOpen, MessageSquareQuote, Languages, BrainCircuit } from 'lucide-react';

export interface Agent {
  name: string;
  description: string;
  icon: LucideIcon;
}

export const availableAgents: Agent[] = [
  {
    name: '数学解题大师',
    description: '能够解决从小学到大学的各种数学问题，并提供详细的解题步骤。',
    icon: Calculator,
  },
  {
    name: '历史故事讲解员',
    description: '生动地讲述各个时期的历史事件和人物故事，让历史学习不再枯燥。',
    icon: BookOpen,
  },
  {
    name: '英语语法纠错官',
    description: '精确识别并纠正英语写作中的语法错误，提供修改建议，提升写作水平。',
    icon: Languages,
  },
  {
    name: '古诗词鉴赏家',
    description: '深入解析古诗词的意境、背景和修辞手法，帮助学生更好地理解和背诵。',
    icon: MessageSquareQuote,
  },
  {
      name: '通用知识问答',
      description: '一个全能型助手，可以回答各种领域的常识性问题，是你的随身智囊。',
      icon: BrainCircuit,
  }
];
