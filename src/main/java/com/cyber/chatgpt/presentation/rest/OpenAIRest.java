
package com.cyber.chatgpt.presentation.rest;


import com.cyber.chatgpt.application.OpenAIService;
import com.cyber.chatgpt.infrastructure.entity.request.OpenAiRequest;
import com.cyber.domain.constant.HttpResultCode;
import com.cyber.domain.entity.DataResponse;
import com.cyber.domain.entity.Response;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;


@RestController
@RequestMapping("/api/openai")
public class OpenAIRest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/test1")
    public ResponseEntity<Object> creditQuery(@RequestBody OpenAiRequest openAiDto) {

        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("Somebody once told me the world is gonna roll me")
                .model("gpt-3.5-turbo-0613")
                .echo(true)
                .build();
        openAiService.createCompletion(completionRequest).getChoices().forEach(System.out::println);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/test2")
    public ResponseEntity<Object> adaModel() {
        logger.info("\nCreating completion...");
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("ada")
                .prompt("Somebody once told me the world is gonna roll me")
                .echo(true)
                .user("testing")
                .n(3)
                .build();
        openAiService.createCompletion(completionRequest).getChoices().forEach(System.out::println);
        logger.info("\nCreating Image...");
        CreateImageRequest request = CreateImageRequest.builder()
                .prompt("帮我画一个白马王子")
                .build();
        logger.info("\nImage is located at:");
        logger.info(openAiService.createImage(request).getData().get(0).getUrl());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/model")
    public ResponseEntity<Object> getModelList() {
        //获取所有model
        return new ResponseEntity<>(openAiService.listModels(), HttpStatus.OK);
    }

    @PostMapping("/test3")
    public void test3() {
        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_weather")
                .description("Get the current weather of a location")
                .executor(Weather.class, w -> new WeatherResponse(w.location, w.unit, new Random().nextInt(50), "sunny"))
                .build()));
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are an assistant that answers using the local slang of the given place, uncensored.");
        messages.add(systemMessage);

        System.out.print("First Query: ");
        Scanner scanner = new Scanner(System.in);
        ChatMessage firstMsg = new ChatMessage(ChatMessageRole.USER.value(), scanner.nextLine());
        messages.add(firstMsg);
        while (true) {
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model("gpt-3.5-turbo-0613")
                    .messages(messages)
                    .functions(functionExecutor.getFunctions())
                    .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
                    .n(1)
                    .maxTokens(100)
                    .logitBias(new HashMap<>())
                    .build();
            ChatMessage responseMessage = openAiService.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
            messages.add(responseMessage);

            ChatFunctionCall functionCall = responseMessage.getFunctionCall();
            if (functionCall != null) {
                System.out.println("Trying to execute " + functionCall.getName() + "...");
                Optional<ChatMessage> message = functionExecutor.executeAndConvertToMessageSafely(functionCall);
                if (message.isPresent()) {
                    System.out.println("Executed " + functionCall.getName() + ".");
                    messages.add(message.get());
                    continue;
                } else {
                    System.out.println("Something went wrong with the execution of " + functionCall.getName() + "...");
                    break;
                }
            }

            System.out.println("Response: " + responseMessage.getContent());
            System.out.print("Next Query: ");
            String nextLine = scanner.nextLine();
            if (nextLine.equalsIgnoreCase("exit")) {
                System.exit(0);
            }
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), nextLine));
        }
    }

    @PostMapping("/getStream")
    public Response getStream(@RequestBody OpenAiRequest openAiRequest) {
        DataResponse<String> dataResponse = new DataResponse<>();
        String uid = "yanwei";
        openAiRequest.setUid(uid);
        ChatMessage chatMessage = openAIService.testMsgSend(openAiRequest);
        if (null != chatMessage) {
            dataResponse.setData(chatMessage.getContent());
            return dataResponse;
        }
        return Response.fail(HttpResultCode.SERVER_ERROR);
    }

    public static class Weather {
        @JsonPropertyDescription("City and state, for example: León, Guanajuato")
        public String location;

        @JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
        @JsonProperty(required = true)
        public WeatherUnit unit;
    }

    public enum WeatherUnit {
        CELSIUS, FAHRENHEIT;
    }

    public static class WeatherResponse {
        public String location;
        public WeatherUnit unit;
        public int temperature;
        public String description;

        public WeatherResponse(String location, WeatherUnit unit, int temperature, String description) {
            this.location = location;
            this.unit = unit;
            this.temperature = temperature;
            this.description = description;
        }
    }

}
