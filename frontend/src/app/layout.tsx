import type { Metadata } from 'next';
import './globals.css';
import { cn } from '@/lib/utils';
import { Toaster } from '@/components/ui/toaster';
import { Providers } from '@/components/layout/providers';
import RouteGuard from '@/components/layout/route-guard';
import LayoutContent from '@/components/layout/layout-content';

export const metadata: Metadata = {
  title: '灵犀智学 - 智能教育平台',
  description: '融合人工智能多智能体系统、AR互动、教育数据分析与可视化的教学辅助平台。',
  icons: {
    icon: '/favicon.ico',
    apple: '/logo.png',
    shortcut: '/favicon.ico',
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <head>
        <link rel="icon" href="/favicon.ico" sizes="any" />
        <link rel="apple-touch-icon" href="/logo.png" />
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=PT+Sans:ital,wght@0,400;0,700;1,400;1,700&display=swap" rel="stylesheet" />
      </head>
      <body className={cn('font-body antialiased min-h-screen')}>
        <Providers>
          <RouteGuard>
            <LayoutContent>
              {children}
            </LayoutContent>
            <Toaster />
          </RouteGuard>
        </Providers>
      </body>
    </html>
  );
}
