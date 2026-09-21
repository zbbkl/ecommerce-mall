package com.example.springboot.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordUtils 纯单元测试（不依赖 Spring 上下文与数据库）。
 */
@DisplayName("PasswordUtils BCrypt 工具")
class PasswordUtilsTest {

    @Test
    @DisplayName("encode 生成 BCrypt 散列且每次盐不同")
    void encodeProducesBcryptWithRandomSalt() {
        String h1 = PasswordUtils.encode("123");
        String h2 = PasswordUtils.encode("123");
        assertTrue(PasswordUtils.isBcrypt(h1), "散列应为 $2a/$2b/$2y 前缀");
        assertNotEquals(h1, h2, "BCrypt 随机盐：同明文两次散列应不同");
        assertNotEquals("123", h1);
    }

    @Test
    @DisplayName("matches：散列存储时正确校验对错密码")
    void matchesWithBcryptHash() {
        String hash = PasswordUtils.encode("s3cret!");
        assertTrue(PasswordUtils.matches("s3cret!", hash));
        assertFalse(PasswordUtils.matches("wrong", hash));
    }

    @Test
    @DisplayName("matches：存量明文密码兼容（直等比较）")
    void matchesWithLegacyPlainPassword() {
        assertTrue(PasswordUtils.matches("123", "123"), "存量明文 '123' 应可登录");
        assertFalse(PasswordUtils.matches("456", "123"));
    }

    @Test
    @DisplayName("matches：空值一律 false（不抛异常）")
    void matchesWithBlankValues() {
        assertFalse(PasswordUtils.matches(null, "123"));
        assertFalse(PasswordUtils.matches("123", null));
        assertFalse(PasswordUtils.matches("", "123"));
        assertFalse(PasswordUtils.matches("123", ""));
    }

    @Test
    @DisplayName("isBcrypt 识别三种前缀，普通文本不误判")
    void isBcryptPrefixes() {
        assertTrue(PasswordUtils.isBcrypt("$2a$10$abcdefghijklmnopqrstuv"));
        assertTrue(PasswordUtils.isBcrypt("$2b$10$abcdefghijklmnopqrstuv"));
        assertTrue(PasswordUtils.isBcrypt("$2y$10$abcdefghijklmnopqrstuv"));
        assertFalse(PasswordUtils.isBcrypt("123"));
        assertFalse(PasswordUtils.isBcrypt("$3a$10$not-bcrypt"));
        assertFalse(PasswordUtils.isBcrypt(null));
    }

    @Test
    @DisplayName("needUpgrade：明文需要升级，散列不需要")
    void needUpgradeSemantics() {
        assertTrue(PasswordUtils.needUpgrade("123"));
        assertFalse(PasswordUtils.needUpgrade(PasswordUtils.encode("123")));
    }
}
