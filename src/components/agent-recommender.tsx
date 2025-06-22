'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Bot, Loader2, Sparkles } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { recommendAgent, type RecommendAgentOutput } from '@/ai/flows/recommend-agent';
import { availableAgents } from '@/lib/agents';
import { useToast } from '@/hooks/use-toast';
import { Alert, AlertDescription, AlertTitle } from './ui/alert';

const FormSchema = z.object({
  userInput: z.string().min(10, { message: '请输入至少10个字来描述您的问题。' }),
});

export default function AgentRecommender() {
  const [recommendation, setRecommendation] = useState<RecommendAgentOutput | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();

  const form = useForm<z.infer<typeof FormSchema>>({
    resolver: zodResolver(FormSchema),
    defaultValues: {
      userInput: '',
    },
  });

  async function onSubmit(data: z.infer<typeof FormSchema>) {
    setIsLoading(true);
    setRecommendation(null);
    try {
      const result = await recommendAgent({
        userInput: data.userInput,
        availableAgents: availableAgents.map(({ name, description }) => ({ name, description })),
      });
      setRecommendation(result);
    } catch (error) {
      console.error('Error recommending agent:', error);
      toast({
        variant: 'destructive',
        title: '推荐失败',
        description: '无法获取智能体推荐，请稍后再试。',
      });
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <Card className="shadow-lg border-2 border-primary/20 bg-card/60 backdrop-blur-lg">
      <CardHeader>
        <div className="flex items-center gap-3">
          <Sparkles className="w-8 h-8 text-accent" />
          <CardTitle className="font-headline text-2xl">智能体推荐</CardTitle>
        </div>
        <CardDescription>不知道该用哪个智能体？告诉我们您的需求，AI为您推荐最合适的！</CardDescription>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="userInput"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="sr-only">Your Input</FormLabel>
                  <FormControl>
                    <Textarea placeholder="例如：请帮我解释一下什么是光合作用，并给出一个相关的化学方程式。" {...field} rows={4} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" disabled={isLoading} className="w-full">
              {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Bot className="mr-2 h-4 w-4" />}
              {isLoading ? '分析中...' : '获取推荐'}
            </Button>
          </form>
        </Form>
        {recommendation && (
          <Alert className="mt-6 bg-accent/5 border-accent/50">
            <Sparkles className="h-5 w-5 text-accent" />
            <AlertTitle className="text-lg font-headline text-accent">推荐智能体: {recommendation.recommendedAgent}</AlertTitle>
            <AlertDescription className="mt-2 text-foreground">
              <strong>推荐理由:</strong> {recommendation.reason}
            </AlertDescription>
          </Alert>
        )}
      </CardContent>
    </Card>
  );
}
