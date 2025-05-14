package com.swimmingliu.service;

import reactor.core.publisher.Flux;

public interface ChatClientService {
    /**
     * 普通提问
     * @return
     */
    String ask(String question, String chatId);

    /**
     * 普通提问-流式回复
     * @return
     */
    Flux<String> askStream (String question, String chatId);

    /**
     * 检查是否需要进行网络搜索
     * @param question
     * @return
     */
    default Boolean checkWebSearch(String question){
       return true;
    };
}