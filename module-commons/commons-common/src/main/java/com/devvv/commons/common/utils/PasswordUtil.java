package com.devvv.commons.common.utils;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.crypto.SecureUtil;
import com.devvv.commons.common.exception.BusiException;

import java.util.regex.Pattern;

/**
 * Create by WangSJ on 2025/04/25
 */
public class PasswordUtil {

    /**
     * 加密密码
     */
    public static String encodePassword(String password) {
        return  SecureUtil.md5(password);
    }

    /**
     * 检查对比密码
     */
    public static boolean verifyPassword(String hashedPassword, String password) {
        return ObjUtil.equal(hashedPassword, encodePassword(password));
    }

    /**
     * 检查是否为简单密码
     */
    public static void checkSimplePassword(String password) {
        // 长度小于6位
        if (password == null || password.length() < 6) {
            throw new BusiException("密码长度不能小于6位");
        }
        // 必须包含字母和数字
        if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$")) {
            throw new BusiException("密码必须包含字母和数字");
        }
        // 检查重复字符（任意字符连续出现3次或以上）
        if (Pattern.matches("(.)\\1{3,}", password)) {
            throw new BusiException("密码中不能包含连续重复字符");
        }
        // 不能出现连续递增字符
        if (hasSequentialChars(password, 4)) {
            throw new BusiException("密码中不能包含连续字符");
        }
        // 检查常见弱密码模式
        String[] commonPatterns = {"password", "qwerty", "abc123", "letmein"};
        for (String pattern : commonPatterns) {
            if (password.toLowerCase().contains(pattern)) {
                throw new BusiException("密码不能是常见弱密码");
            }
        }
    }

    // 检查是否有n位及以上的连续数字或字母
    private static boolean hasSequentialChars(String password, int n) {
        int len = password.length();
        for (int i = 0; i <= len - n; i++) {
            boolean inc = true;
            boolean dec = true;
            for (int j = 1; j < n; j++) {
                char prev = password.charAt(i + j - 1);
                char curr = password.charAt(i + j);
                // 递增判断
                if (curr - prev != 1) inc = false;
                // 递减判断
                if (curr - prev != -1) dec = false;
            }
            if (inc || dec) return true;
        }
        return false;
    }

}
