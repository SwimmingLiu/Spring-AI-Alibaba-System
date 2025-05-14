package com.swimmingliu;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class DeepseekClientTest {

    @Qualifier("openAiChatModel")
    @Autowired
    private ChatModel chatModel;

    @Test
    public void test() {
        String prompt = "请用中文回答：你是谁？";
        String text = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
        log.info(text);
    }

}
