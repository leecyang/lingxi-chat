import Link from 'next/link';
import { BrainCircuit } from 'lucide-react';
import { Button } from '@/components/ui/button';

export default function Header() {
  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 items-center">
        <Link href="/" className="flex items-center gap-2 mr-6">
          <BrainCircuit className="h-6 w-6 text-primary" />
          <span className="font-bold font-headline text-lg">灵犀智学 Lite</span>
        </Link>
        <nav className="flex items-center gap-4 text-sm">
          <Link href="/playbook" className="text-muted-foreground transition-colors hover:text-foreground">
            Playbook
          </Link>
          <Link href="/ar-dialogue" className="text-muted-foreground transition-colors hover:text-foreground">
            AR 对话
          </Link>
        </nav>
        <div className="flex flex-1 items-center justify-end gap-2">
          <Button variant="ghost">登录</Button>
          <Button>注册</Button>
        </div>
      </div>
    </header>
  );
}
