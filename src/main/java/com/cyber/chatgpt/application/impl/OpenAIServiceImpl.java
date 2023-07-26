
package com.cyber.chatgpt.application.impl;


import com.cyber.chatgpt.application.OpenAIService;
import com.cyber.chatgpt.infrastructure.config.OpenAiConfig;
import com.cyber.chatgpt.infrastructure.entity.request.OpenAiRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private OpenAiConfig openAiConfig;

    private Map<String, LinkedList<ChatMessage>> chatMessageMap = new ConcurrentHashMap<>();

    public void getRelProduct(String msg) {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful assistant.");
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "Hello!");
        messages.add(systemMessage);
        messages.add(userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(50)
                .logitBias(new HashMap<>())
                .build();
    }

    @Override
    public ChatMessage testMsgSend(OpenAiRequest openAiRequest) {
        int contextNum = null == openAiConfig.getContextNum() ? 6 : openAiConfig.getContextNum() * 2;
        String question = openAiRequest.getQuestion();
        String prompt = openAiRequest.getPrompt();
        String uid = openAiRequest.getUid();
        LinkedList<ChatMessage> messageDictList = chatMessageMap.get(uid);
        if (CollectionUtils.isEmpty(messageDictList)) {
            messageDictList = new LinkedList<>();
            //ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "我是一个中国人，请用中文回答");
            //messageDictList.add(systemMessage);
            chatMessageMap.put(uid, messageDictList);
        }
        ChatMessage userMsg = new ChatMessage(ChatMessageRole.USER.value(), question);
        // 移除前2个元素
        if (messageDictList.size() >= contextNum) {
            messageDictList.removeFirst();
            messageDictList.removeFirst();
        }
        messageDictList.add(userMsg);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] uidSha1 = md.digest(uid.getBytes(StandardCharsets.UTF_8));

            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model(openAiConfig.getModel())
                    .messages(messageDictList)
                    .maxTokens(openAiConfig.getMaxTokens())
                    .presencePenalty(openAiConfig.getPresencePenalty())
                    .frequencyPenalty(openAiConfig.getFrequencyPenalty())
                    .temperature(openAiConfig.getTemperature())
                    .topP(openAiConfig.getTopP())
                    //.user(new String(uidSha1))
                    .logitBias(new HashMap<>())
                    .build();
            ChatMessage chatMessage = openAiService.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
            messageDictList.add(chatMessage);
            return chatMessage;
        } catch (Exception e) {
            logger.debug("ChatGPT gpt-3.5-turbo error.闲聊接口调用失败, 错误原因:" + e.getMessage() + ",具体失败原因请查对应的日志。");
            e.printStackTrace();
            return null;
        }
    }
}
