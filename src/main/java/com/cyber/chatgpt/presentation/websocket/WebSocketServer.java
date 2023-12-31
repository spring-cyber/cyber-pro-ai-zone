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
package com.cyber.chatgpt.presentation.websocket;

import cn.hutool.json.JSONUtil;
import com.cyber.chatgpt.application.OpenAIService;
import com.cyber.chatgpt.infrastructure.entity.request.OpenAiRequest;
import com.cyber.chatgpt.infrastructure.util.SpringContextHolder;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocketServer
 */
@Component
@ServerEndpoint("/api/ws/{sid}")
public class WebSocketServer {

    private final static Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private static int onlineCount = 0;

    private static final CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //接收sid
    private String sid = "";

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        //加入set中
        webSocketSet.add(this);
        this.sid = sid;
        //在线数加1
        addOnlineCount();
        // 打印日志
        log.info("有新窗口开始监听:" + sid + ",当前在线人数为:" + getOnlineCount());
    }

    @OnClose
    public void onClose() {
        //从set中删除
        webSocketSet.remove(this);
        //在线数减1
        subOnlineCount();
        //断开连接情况下，更新主板占用情况为释放
        log.info("释放的sid为：" + sid);
        //这里写你 释放的时候，要处理的业务
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("收到来自窗口" + sid + "的信息:" + message);
        OpenAiRequest openAiRequest = new OpenAiRequest();
        openAiRequest.setQuestion(message);
        openAiRequest.setUid(sid);
        try {
            ChatMessage chatMessage = SpringContextHolder.getBean(OpenAIService.class).testMsgSend(openAiRequest);
            if (null != chatMessage) {
                sendMessage(chatMessage.getContent());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnError
    public void onError(Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        synchronized (this.session){
            this.session.getBasicRemote().sendText(JSONUtil.toJsonStr(message));
        }
    }

    public void groupMessage(String message, @PathParam("sid") String sid) {
        for (WebSocketServer item : webSocketSet) {
            try {
                if (sid == null) {
                    item.sendMessage(message);
                } else if (item.sid.equals(sid)) {
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

}