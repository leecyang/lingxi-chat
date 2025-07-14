# 九天平台Token修复测试指南

## 概述
本指南说明如何测试修复后的九天平台Authentication Token生成和管理功能。

## 修复内容

### 1. Token生成格式修复
- 修复了JWT Header，添加了九天平台要求的字段：`alg: HS256`, `typ: JWT`, `sign_type: SIGN`
- 修复了Payload结构，使用正确的字段：`api_key`, `exp`, `timestamp`
- 修复了Token过期时间解析逻辑

### 2. 错误处理增强
- 增强了认证错误识别，支持九天平台的错误码（如`code:1001`, `code:1002`）
- 改进了JSON格式错误响应的解析
- 增加了更多认证相关关键字的识别

### 3. 新增验证服务
- 创建了`JiutianTokenValidationService`用于验证Token格式
- 添加了Token刷新机制测试功能

## 测试端点

### 1. 验证Token格式
```bash
POST /api/token/test/validate?apiKey=646ae749bcf5bc1a1498aeaf.lbIpYGaWQ8VwQ2HYTOkhDCKJP/aGgGAc
```

**响应示例：**
```json
{
  "success": true,
  "valid": true,
  "message": "Token格式验证通过",
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ..."
}
```

### 2. 测试Token刷新机制
```bash
POST /api/token/test/test-refresh?apiKey=646ae749bcf5bc1a1498aeaf.lbIpYGaWQ8VwQ2HYTOkhDCKJP/aGgGAc
```

### 3. 测试API调用
```bash
POST /api/token/test/test-api-call
```

### 4. 生成新Token
```bash
POST /api/token/test/generate?apiKey=646ae749bcf5bc1a1498aeaf.lbIpYGaWQ8VwQ2HYTOkhDCKJP/aGgGAc
```

### 5. 检查Token状态
```bash
GET /api/token/test/status?apiKey=646ae749bcf5bc1a1498aeaf.lbIpYGaWQ8VwQ2HYTOkhDCKJP/aGgGAc
```

## 验证步骤

1. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

2. **验证Token格式**
   - 调用`/api/token/test/validate`端点
   - 确认返回`valid: true`

3. **测试Token生成**
   - 调用`/api/token/test/generate`端点
   - 检查生成的Token格式

4. **测试刷新机制**
   - 调用`/api/token/test/test-refresh`端点
   - 查看日志确认刷新逻辑正常

5. **验证API调用**
   - 调用`/api/token/test/test-api-call`端点
   - 确认Token可以正常用于API调用

## 注意事项

1. **API Key格式**：确保使用正确的格式`kid.secret`
2. **Token有效期**：默认设置为1小时，可根据需要调整
3. **错误处理**：如果遇到认证错误，系统会自动尝试刷新Token
4. **日志监控**：关注应用日志中的Token相关信息

## 常见问题

### Q: Token验证失败怎么办？
A: 检查API Key格式是否正确，确保包含kid和secret两部分。

### Q: 如何确认Token过期处理是否正常？
A: 可以通过`/api/token/test/test-refresh`端点测试刷新机制。

### Q: 如何查看Token的详细信息？
A: 使用`/api/token/test/status`端点可以查看Token的状态和剩余时间。