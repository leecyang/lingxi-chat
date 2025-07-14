# 灵犀智学 - 前端项目

基于 Next.js 15 构建的智能教学辅助平台前端应用。

## 🚀 快速开始

### 本地开发

1. 安装依赖
```bash
npm install
```

2. 配置环境变量
```bash
cp .env.example .env.local
```

3. 启动开发服务器
```bash
npm run dev
```

访问 [http://localhost:9002](http://localhost:9002) 查看应用。

## 📦 部署到 Vercel

### 方法一：通过 Vercel CLI

1. 安装 Vercel CLI
```bash
npm i -g vercel
```

2. 登录 Vercel
```bash
vercel login
```

3. 部署项目
```bash
vercel
```

### 方法二：通过 GitHub 集成

1. 将代码推送到 GitHub 仓库
2. 访问 [Vercel Dashboard](https://vercel.com/dashboard)
3. 点击 "New Project"
4. 选择你的 GitHub 仓库
5. 配置项目设置：
   - Framework Preset: Next.js
   - Root Directory: `frontend`
   - Build Command: `npm run build`
   - Output Directory: `.next`
   - Install Command: `npm install`

### 环境变量配置

在 Vercel 项目设置中添加以下环境变量：

```
NEXT_PUBLIC_APP_URL=https://your-domain.vercel.app
NEXT_PUBLIC_API_URL=https://your-backend-api.com
```

## 🛠️ 技术栈

- **框架**: Next.js 15 (App Router)
- **样式**: Tailwind CSS
- **UI组件**: Radix UI + shadcn/ui
- **图标**: Lucide React
- **状态管理**: React Context
- **类型检查**: TypeScript
- **部署**: Vercel

## 📁 项目结构

```
src/
├── app/                 # App Router 页面
│   ├── demo/           # 演示场景页面
│   ├── login/          # 登录页面
│   └── page.tsx        # 首页
├── components/         # 可复用组件
│   ├── ui/            # 基础UI组件
│   └── layout/        # 布局组件
├── contexts/          # React Context
├── hooks/             # 自定义Hooks
├── lib/               # 工具函数
└── services/          # API服务
```

## 🔧 开发脚本

- `npm run dev` - 启动开发服务器
- `npm run build` - 构建生产版本
- `npm run start` - 启动生产服务器
- `npm run lint` - 运行 ESLint
- `npm run typecheck` - 类型检查

## 📝 注意事项

1. 当前版本为前端展示版本，后端API尚未完成
2. 部分功能（如用户认证、AI对话）需要后端支持
3. 演示场景和智能体矩阵可以正常访问和展示
4. 建议使用 Node.js 18+ 版本

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License