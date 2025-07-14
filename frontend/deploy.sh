#!/bin/bash

# 灵犀智学前端快速部署脚本
# 使用方法: ./deploy.sh 或 bash deploy.sh

echo "🚀 开始部署灵犀智学前端项目..."

# 检查是否安装了必要的工具
if ! command -v node &> /dev/null; then
    echo "❌ Node.js 未安装，请先安装 Node.js 18+"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo "❌ npm 未安装，请先安装 npm"
    exit 1
fi

echo "📦 安装依赖..."
npm install

if [ $? -ne 0 ]; then
    echo "❌ 依赖安装失败"
    exit 1
fi

echo "🔨 构建项目..."
npm run build

if [ $? -ne 0 ]; then
    echo "❌ 项目构建失败"
    exit 1
fi

echo "✅ 项目构建成功！"

# 检查是否安装了 Vercel CLI
if ! command -v vercel &> /dev/null; then
    echo "📥 Vercel CLI 未安装，正在安装..."
    npm install -g vercel
fi

echo "🌐 开始部署到 Vercel..."
echo "请按照提示完成部署配置："
echo "1. 选择你的 Vercel 账户"
echo "2. 项目名称建议: lingxi-chat-frontend"
echo "3. 代码目录: ./"
echo ""

vercel --prod

if [ $? -eq 0 ]; then
    echo "🎉 部署成功！"
    echo "你的应用已经部署到 Vercel，请查看上方的 URL"
else
    echo "❌ 部署失败，请检查错误信息"
    exit 1
fi