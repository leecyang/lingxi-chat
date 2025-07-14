@echo off
chcp 65001 >nul
echo 🚀 开始部署灵犀智学前端项目...

REM 检查Node.js是否安装
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js 未安装，请先安装 Node.js 18+
    pause
    exit /b 1
)

REM 检查npm是否安装
npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ npm 未安装，请先安装 npm
    pause
    exit /b 1
)

echo 📦 安装依赖...
npm install
if %errorlevel% neq 0 (
    echo ❌ 依赖安装失败
    pause
    exit /b 1
)

echo 🔨 构建项目...
npm run build
if %errorlevel% neq 0 (
    echo ❌ 项目构建失败
    pause
    exit /b 1
)

echo ✅ 项目构建成功！

REM 检查Vercel CLI是否安装
vercel --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 📥 Vercel CLI 未安装，正在安装...
    npm install -g vercel
)

echo 🌐 开始部署到 Vercel...
echo 请按照提示完成部署配置：
echo 1. 选择你的 Vercel 账户
echo 2. 项目名称建议: lingxi-chat-frontend
echo 3. 代码目录: ./
echo.

vercel --prod

if %errorlevel% equ 0 (
    echo 🎉 部署成功！
    echo 你的应用已经部署到 Vercel，请查看上方的 URL
) else (
    echo ❌ 部署失败，请检查错误信息
)

pause