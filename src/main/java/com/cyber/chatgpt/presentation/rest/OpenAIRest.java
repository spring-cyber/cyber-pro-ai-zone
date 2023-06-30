
package com.cyber.chatgpt.presentation.rest;


import com.cyber.chatgpt.application.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/openai")
public class OpenAIRest {

    @Autowired
    private final OpenAIService openAIService;

    @PostMapping
    public ResponseEntity<Object> creditQuery(@RequestBody OpenAiRequest openAiDto) {
        return new ResponseEntity<>(openAiService.creditQuery(openAiDto), HttpStatus.OK);
    }
}
