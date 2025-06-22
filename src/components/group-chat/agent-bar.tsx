'use client';

import { availableAgents } from '@/lib/agents';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { Users } from 'lucide-react';

export default function AgentBar() {
  return (
    <div className="w-full px-4 py-2 border-t bg-background/80 backdrop-blur-sm">
      <div className="max-w-4xl mx-auto flex items-center gap-4">
        <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
          <Users className="w-5 h-5" />
          <span>在线智能体</span>
        </div>
        <TooltipProvider delayDuration={0}>
          <div className="flex items-center gap-3">
            {availableAgents.map((agent) => (
              <Tooltip key={agent.name}>
                <TooltipTrigger asChild>
                  <div className="relative cursor-pointer">
                    <Avatar className="w-9 h-9 border-2 border-transparent hover:border-primary transition-colors">
                      <AvatarImage src={agent.avatar} alt={agent.name} data-ai-hint={agent.dataAiHint} />
                      <AvatarFallback>{agent.name.charAt(0)}</AvatarFallback>
                    </Avatar>
                    <span className="absolute bottom-0 right-0 block h-2.5 w-2.5 rounded-full bg-green-500 ring-2 ring-background" />
                  </div>
                </TooltipTrigger>
                <TooltipContent>
                  <p className="font-bold">{agent.name}</p>
                  <p className="text-sm text-muted-foreground">{agent.description}</p>
                </TooltipContent>
              </Tooltip>
            ))}
          </div>
        </TooltipProvider>
      </div>
    </div>
  );
}
