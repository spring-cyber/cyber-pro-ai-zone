package com.cyber.chatgpt.application;

import com.cyber.chatgpt.infrastructure.entity.request.OpenAiRequest;
import com.theokanning.openai.completion.chat.ChatMessage;

public interface OpenAIService {

    ChatMessage testMsgSend(OpenAiRequest openAiRequest);
}
