# 灵犀智学前端部署指南

本指南将帮助你将灵犀智学前端项目部署到 Vercel 平台。

## 📋 部署前准备

### 1. 确保项目已推送到 GitHub
   ```bash
   git add .
   git commit -m "feat: 准备部署到 Vercel"
   git push origin main
   ```

### 2. 重要：项目结构说明
   - 项目根目录包含 `vercel.json` 配置文件
   - 前端代码位于 `frontend/` 子目录
   - Vercel 会自动识别并构建 `frontend/` 目录中的 Next.js 应用

### 3. 确保项目可以正常构建

```bash
# 安装依赖
npm install

# 本地构建测试
npm run build

# 启动生产服务器测试
npm run start
```

### 2. 准备 GitHub 仓库

确保你的代码已经推送到 GitHub 仓库中。

## 🚀 Vercel 部署步骤

### 方法一：通过 Vercel 网站部署（推荐）

1. **访问 Vercel**
   - 打开 [vercel.com](https://vercel.com)
   - 使用 GitHub 账号登录

2. **创建新项目**
   - 点击 "New Project" 按钮
   - 选择你的 GitHub 仓库
   - 如果是私有仓库，需要先授权 Vercel 访问

3. **配置项目设置**
   ```
   Project Name: lingxi-chat-frontend
   Framework Preset: Next.js
   Root Directory: frontend
   Build Command: npm run build
   Output Directory: .next
   Install Command: npm install
   Node.js Version: 18.x
   ```

4. **配置环境变量**
   在 "Environment Variables" 部分添加：
   ```
   NEXT_PUBLIC_APP_URL = https://your-project-name.vercel.app
   NODE_ENV = production
   ```

5. **部署**
   - 点击 "Deploy" 按钮
   - 等待构建完成（通常需要 2-5 分钟）

### 方法二：通过 Vercel CLI 部署

1. **安装 Vercel CLI**
   ```bash
   npm i -g vercel
   ```

2. **登录 Vercel**
   ```bash
   vercel login
   ```

3. **初始化项目**
   ```bash
   cd frontend
   vercel
   ```

4. **按照提示配置**
   ```
   ? Set up and deploy "~/frontend"? [Y/n] y
   ? Which scope do you want to deploy to? [选择你的账户]
   ? Link to existing project? [N/y] n
   ? What's your project's name? lingxi-chat-frontend
   ? In which directory is your code located? ./
   ```

5. **后续部署**
   ```bash
   # 部署到预览环境
   vercel
   
   # 部署到生产环境
   vercel --prod
   ```

## ⚙️ 高级配置

### 自定义域名

1. 在 Vercel 项目设置中点击 "Domains"
2. 添加你的自定义域名
3. 按照提示配置 DNS 记录

### 环境变量管理

在 Vercel 项目设置的 "Environment Variables" 中可以添加：

- **Development**: 开发环境变量
- **Preview**: 预览环境变量  
- **Production**: 生产环境变量

### 构建优化

在 `vercel.json` 中已经配置了：
- 构建缓存优化
- 函数超时设置
- 区域设置（香港节点）

## 🔍 部署后验证

### 1. 功能测试
- [ ] 首页正常加载
- [ ] 演示场景页面可访问
- [ ] 智能体矩阵页面正常
- [ ] 侧边栏导航功能正常
- [ ] 响应式设计在移动端正常

### 2. 性能检查
- 使用 [PageSpeed Insights](https://pagespeed.web.dev/) 检查性能
- 使用 [GTmetrix](https://gtmetrix.com/) 检查加载速度

### 3. SEO 检查
- 检查页面标题和描述
- 验证 Open Graph 标签
- 确认 robots.txt 可访问

## 🐛 常见问题

### 构建失败

1. **TypeScript 错误**
   ```bash
   # 本地检查类型错误
   npm run typecheck
   ```

2. **依赖问题**
   ```bash
   # 清理缓存重新安装
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **环境变量问题**
   - 确保所有 `NEXT_PUBLIC_` 前缀的变量都已配置
   - 检查变量名拼写是否正确

### 部署后页面空白

1. 检查浏览器控制台错误
2. 验证环境变量配置
3. 检查 Next.js 配置文件

### 静态资源加载失败

1. 确保图片路径正确
2. 检查 `next.config.ts` 中的图片配置
3. 验证 public 目录下的文件

## 📞 获取帮助

- [Vercel 官方文档](https://vercel.com/docs)
- [Next.js 部署文档](https://nextjs.org/docs/deployment)
- [项目 GitHub Issues](https://github.com/your-username/lingxi-chat/issues)

## 🎉 部署成功

部署成功后，你将获得：
- 生产环境 URL: `https://your-project-name.vercel.app`
- 自动 HTTPS 证书
- 全球 CDN 加速
- 自动部署（每次推送到主分支）

恭喜！你的灵犀智学前端项目已经成功部署到 Vercel！