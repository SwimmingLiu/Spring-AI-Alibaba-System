package com.swimmingliu.common.utils;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

/**
*   @author SwimmingLiu
*   @date 2025-06-03 18:06
*   @description: 随机数工具类
*/


public class RandomUtil {
    /**
     * 使用Mybatis-Plus的雪花算法生成chatId
     * @return 全局唯一的chatId
     */
    public static Long generateChatId() {
        return IdWorker.getId();
    }
}