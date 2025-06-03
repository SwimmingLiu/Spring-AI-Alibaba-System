package com.swimmingliu.service.impl;


import com.swimmingliu.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * @author swimmingliu
 */
@Slf4j
public abstract class AbstractChatClientService {

    public String ask(String question, String chatId) {
        try {
            return doAsk(question, chatId);
        } catch (Exception e) {
            log.error("Chat ask error, question: {}, chatId: {}", question, chatId, e);
            throw new BizException("对话请求失败，请稍后重试");
        }
    }

    public Flux<String> askStream(String question, String chatId) {
        try {
            return doAskStream(question, chatId);
        } catch (Exception e) {
            log.error("Chat stream ask error, question: {}, chatId: {}", question, chatId, e);
            return Flux.error(new BizException("流式对话请求失败，请稍后重试"));
        }
    }

    @Async
    public CompletableFuture<String> askAsync(String question, String chatId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return doAsk(question, chatId);
            } catch (Exception e) {
                log.error("Async chat ask error, question: {}, chatId: {}", question, chatId, e);
                throw new BizException("异步对话请求失败，请稍后重试");
            }
        });
    }

    public String askWithFile(String question, String chatId, String fileUrl){
        try {
            return doAskWithFile(question, chatId, fileUrl);
        } catch (Exception e) {
            log.error("Chat ask error, question: {}, chatId: {}", question, chatId, e);
            throw new BizException("对话请求失败，请稍后重试");
        }
    }

    public Flux<String> askStreamWithFile(String question, String chatId, String fileUrl){
        try {
            return doAskStreamWithFile(question, chatId, fileUrl);
        } catch (Exception e) {
            log.error("Chat ask error, question: {}, chatId: {}", question, chatId, e);
            throw new BizException("对话请求失败，请稍后重试");
        }
    }

    abstract String doAsk(String question, String chatId);

    abstract Flux<String> doAskStream(String question, String chatId);

    abstract String doAskWithFile(String question, String chatId, String fileUrl) throws IOException, InterruptedException;

    public abstract Flux<String> doAskStreamWithFile(String question, String chatId, String fileUrl) throws IOException, InterruptedException;
}
