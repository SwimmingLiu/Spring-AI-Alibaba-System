package com.swimmingliu.common.utils;

public class RandomUtil {
    /**
     * 根据时间戳生成chatId（Long类型）
     * 格式：timestamp后6位 + 随机4位数
     *
     * @return chatId
     */
    public static Long generateChatId() {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        // 获取时间戳的后6位
        long lastSixDigits = timestamp % 1000000;
        // 生成4位随机数 (0-9999)
        int randomNum = (int) (Math.random() * 10000);
        // 组合：后6位时间戳 * 10000 + 随机4位数
        return lastSixDigits * 10000L + randomNum;
    }
}