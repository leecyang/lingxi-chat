import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Mic, Video, Volume2 } from 'lucide-react';
import Image from 'next/image';

export default function ARDialoguePage() {
  return (
    <div className="container mx-auto px-4 py-12">
      <section className="text-center mb-12">
        <h1 className="text-4xl md:text-5xl font-bold font-headline">AR 语音对话</h1>
        <p className="mt-4 max-w-2xl mx-auto text-lg text-muted-foreground">
          与您的专属虚拟教师进行实时语音互动，让学习变得生动有趣。
        </p>
      </section>

      <div className="grid lg:grid-cols-3 gap-8 items-start">
        <div className="lg:col-span-2">
          <Card className="overflow-hidden shadow-2xl">
            <div className="relative aspect-video bg-gray-900 flex items-center justify-center">
              <Image
                src="https://placehold.co/800x450.png"
                alt="AR虚拟教师"
                width={800}
                height={450}
                data-ai-hint="cartoon teacher"
                className="absolute inset-0 w-full h-full object-cover opacity-30"
              />
              <div className="z-10 text-center text-white p-4">
                 <Video className="w-16 h-16 mx-auto mb-4 opacity-50"/>
                 <h3 className="text-xl font-bold">摄像头未开启</h3>
                 <p className="text-muted-foreground">请允许浏览器访问您的摄像头以开始AR体验。</p>
              </div>
              {/* Placeholder for the AR character */}
              <div className="absolute bottom-0 right-4 h-2/3 w-1/3 z-20">
                 <Image src="https://placehold.co/300x400.png" alt="虚拟教师" data-ai-hint="cartoon teacher" layout="fill" objectFit="contain" className="drop-shadow-2xl" />
              </div>
            </div>
            <div className="p-6 bg-background/80 backdrop-blur-sm border-t flex flex-col items-center">
                <p className="text-muted-foreground mb-4">点击按钮开始与虚拟教师对话</p>
                <Button size="lg" className="rounded-full w-20 h-20 shadow-lg animate-pulse" aria-label="开始录音">
                    <Mic className="w-10 h-10" />
                </Button>
            </div>
          </Card>
        </div>

        <Card className="sticky top-20">
          <CardHeader>
            <CardTitle className="font-headline">控制面板</CardTitle>
            <CardDescription>调整您的AR体验设置</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
              <div className="flex items-center gap-3">
                <Video className="w-5 h-5 text-muted-foreground" />
                <span>开启摄像头</span>
              </div>
              <Button size="sm" variant="outline">开启</Button>
            </div>
            <div className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
              <div className="flex items-center gap-3">
                <Mic className="w-5 h-5 text-muted-foreground" />
                <span>麦克风输入</span>
              </div>
              <Button size="sm" variant="outline">开启</Button>
            </div>
            <div className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
              <div className="flex items-center gap-3">
                <Volume2 className="w-5 h-5 text-muted-foreground" />
                <span>教师音量</span>
              </div>
              <input type="range" className="w-24 accent-primary" defaultValue="70"/>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
