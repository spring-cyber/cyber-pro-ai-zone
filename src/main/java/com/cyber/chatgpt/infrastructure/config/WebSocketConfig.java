/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.cyber.chatgpt.infrastructure.config;

import com.cyber.chatgpt.infrastructure.util.SpringContextHolder;
import com.cyber.chatgpt.presentation.websocket.WebSocketServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Zheng Jie
 * @description 开启WebSocket
 **/
@Configuration
@EnableScheduling
public class WebSocketConfig {

    @Scheduled(fixedRate = 15 * 1000)
    public void configureTasks() {
        SpringContextHolder.getBean(WebSocketServer.class).groupMessage("this is the heartbeat message", null);
    }
}
