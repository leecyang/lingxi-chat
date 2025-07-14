package com.lingxi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 注册请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少6个字符")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Size(max = 100, message = "昵称长度不能超过100个字符")
    private String nickname;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phoneNumber;

    @Size(max = 100, message = "组织名称长度不能超过100个字符")
    private String organization;

    // 角色申请（user, developer）
    private String roleRequest;

    // 邀请码（用于特殊角色注册）
    private String inviteCode;

    // 验证码
    private String captcha;

    // 同意条款
    private Boolean agreeToTerms = false;

    // 设备信息
    private String deviceInfo;

    // IP地址
    private String ipAddress;

    /**
     * 验证密码确认
     */
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}