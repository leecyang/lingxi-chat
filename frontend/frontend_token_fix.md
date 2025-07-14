# 前端Token自动刷新功能修复说明

## 问题描述
前端在调用后端API时，当遇到"Authentication Token已过期"错误时，没有自动刷新Token的机制，导致用户需要手动重新登录。

## 修复内容

### 1. API请求层面修复 (`src/lib/api.ts`)

#### 新增功能：
- **Token过期检测**: 增加了 `isAuthenticationError()` 函数，能够识别多种认证错误
  - HTTP状态码：401、403
  - 错误关键字：authentication、token、unauthorized、身份验证、已过期等
  - 九天平台特定错误码：1001、1002

- **自动Token刷新**: 增加了 `refreshAuthToken()` 函数
  - 防止并发刷新（使用标志位和Promise缓存）
  - 自动更新localStorage中的token
  - 刷新失败时清除无效token

- **请求重试机制**: 修改了 `apiRequest()` 函数
  - 检测到认证错误时自动刷新Token
  - Token刷新成功后自动重试原请求
  - 最多重试1次，避免无限循环
  - 刷新失败时自动跳转到登录页面

#### 修复前的问题：
```typescript
// 修复前：遇到认证错误直接返回失败
if (!response.ok) {
  return {
    success: false,
    message: data.message || data.error || `HTTP ${response.status}: ${response.statusText}`,
    code: response.status
  };
}
```

#### 修复后的逻辑：
```typescript
// 修复后：检测认证错误并自动处理
if (!response.ok) {
  // 检查是否为认证错误且未重试过
  if (isAuthenticationError(response, data) && retryCount === 0) {
    console.log('检测到认证错误，尝试刷新Token...');
    const refreshSuccess = await refreshAuthToken();
    
    if (refreshSuccess) {
      console.log('Token刷新成功，重试请求...');
      // 递归调用，重试请求
      return apiRequest(endpoint, config, retryCount + 1);
    } else {
      console.log('Token刷新失败，跳转到登录页面');
      // Token刷新失败，跳转到登录页面
      if (typeof window !== 'undefined') {
        window.location.href = '/login';
      }
    }
  }
  // ... 其他错误处理
}
```

### 2. 登录功能确认 (`src/app/login/page.tsx`)

确认登录页面正确保存了refresh token：
```typescript
if (response.success && response.data) {
  // 存储token
  localStorage.setItem('access_token', response.data.accessToken);
  localStorage.setItem('refresh_token', response.data.refreshToken); // ✅ 已正确保存
  // ...
}
```

### 3. 测试页面 (`src/app/test-token/page.tsx`)

创建了专门的测试页面来验证Token自动刷新功能：
- **Token验证测试**: 验证当前Token是否有效
- **API调用测试**: 发送测试消息，验证自动刷新机制
- **Token刷新测试**: 手动触发Token刷新
- **实时结果显示**: 显示每个测试的成功/失败状态

## 工作流程

### 正常API调用流程：
1. 用户发起API请求
2. 前端添加Authorization头
3. 后端处理请求并返回结果
4. 前端接收并处理响应

### Token过期时的自动处理流程：
1. 用户发起API请求
2. 后端返回认证错误（如"Authentication Token已过期"）
3. 前端检测到认证错误
4. 自动调用refresh token接口
5. 如果刷新成功：
   - 更新localStorage中的token
   - 使用新token重试原请求
   - 返回重试结果给用户
6. 如果刷新失败：
   - 清除本地token
   - 自动跳转到登录页面

## 支持的错误类型

### HTTP状态码：
- `401 Unauthorized`
- `403 Forbidden`

### 错误消息关键字：
- `authentication`
- `token`
- `unauthorized`
- `身份验证`
- `已过期`
- `invalid token`
- `token expired`

### 九天平台特定错误码：
- `code: 1001`
- `code: 1002`

## 测试方法

### 1. 访问测试页面
```
http://localhost:3000/test-token
```

### 2. 手动测试步骤
1. 登录系统
2. 访问测试页面
3. 点击"测试API调用"按钮
4. 观察是否能正常发送消息
5. 如果Token过期，观察是否自动刷新

### 3. 模拟Token过期测试
1. 在浏览器开发者工具中修改localStorage中的access_token为无效值
2. 尝试发送消息
3. 观察是否自动刷新Token并重试

## 注意事项

1. **防止并发刷新**: 使用标志位确保同时只有一个刷新请求
2. **避免无限重试**: 每个请求最多重试1次
3. **安全处理**: 刷新失败时自动清除本地token并跳转登录
4. **用户体验**: 整个过程对用户透明，无需手动操作

## 兼容性

- ✅ 支持所有现有的API调用
- ✅ 向后兼容，不影响现有功能
- ✅ 适用于所有使用 `api` 对象的服务
- ✅ 支持聊天、智能体、管理等所有模块

## 预期效果

修复后，用户在使用聊天功能时：
1. 不会再看到"Authentication Token已过期"的错误提示
2. Token过期时会自动刷新，用户无感知
3. 只有在refresh token也过期时才需要重新登录
4. 提升了用户体验和系统的健壮性