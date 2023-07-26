package com.cyber.chatgpt.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

/**
 * @author yanwei
 * @desc
 * @since 2023/7/16
 */
@Configuration
public class Config {

    @Bean
    public OpenAiService openAiService() {
        ObjectMapper mapper = OpenAiService.defaultObjectMapper();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 17890));
        OkHttpClient client = OpenAiService.defaultClient("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", Duration.ofSeconds(100))
                .newBuilder()
                .proxy(proxy)
                .build();
        Retrofit retrofit = OpenAiService.defaultRetrofit(client, mapper);
        OpenAiApi api = retrofit.create(OpenAiApi.class);
        OpenAiService service = new OpenAiService(api);
        return service;
    }
}
