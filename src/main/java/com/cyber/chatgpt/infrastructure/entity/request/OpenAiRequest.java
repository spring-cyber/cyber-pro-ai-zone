package com.cyber.chatgpt.infrastructure.entity.request;

import lombok.Data;

/**
 * @author yanwei
 * @desc
 * @since 2023/7/13
 */
@Data
public class OpenAiRequest {
    private String prompt;
    private String question;
    private String uid;
}
