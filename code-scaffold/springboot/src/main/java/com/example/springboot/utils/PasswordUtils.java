package com.example.springboot.utils;

import cn.hutool.core.util.StrUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码散列工具（BCrypt）。
 *
 * 兼容策略：存量数据里的明文密码（如种子数据 '123'）继续可用——
 * 校验时若库存的是 BCrypt 散列（$2a$/$2b$/$2y$ 前缀）走 matches，
 * 否则退回明文比较；登录成功后调用方应调用 maybeUpgrade() 静默把明文升级为散列。
 */
public final class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtils() {
    }

    /** 明文 → BCrypt 散列 */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** 校验：兼容 BCrypt 散列与存量明文 */
    public static boolean matches(String rawPassword, String stored) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(stored)) {
            return false;
        }
        if (isBcrypt(stored)) {
            return ENCODER.matches(rawPassword, stored);
        }
        return rawPassword.equals(stored);
    }

    /** 库里是否已是 BCrypt 散列 */
    public static boolean isBcrypt(String stored) {
        return stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }

    /** 是否需要静默升级（明文命中后调用） */
    public static boolean needUpgrade(String stored) {
        return !isBcrypt(stored);
    }
}
