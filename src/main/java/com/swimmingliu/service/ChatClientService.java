package com.swimmingliu.service;

import reactor.core.publisher.Flux;
import java.io.IOException;

public interface ChatClientService {
    /**
     * 提问
     * @return
     */
    String ask(String question, String chatId);

    /**
     * 提问-流式回复
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

    /**
     * 提问-文档检索
     * @param question
     * @param chatId
     * @param fileUrl
     * @return
     */
    default String askWithFile(String question, String chatId, String fileUrl) throws IOException, InterruptedException {return "";};

    /**
     * 提问-文档检索 - 流式回复
     * @param question
     * @param chatId
     * @param fileUrl
     * @return
     */
    default Flux<String> askStreamWithFile(String question, String chatId, String fileUrl) throws IOException, InterruptedException {return Flux.empty();};
}