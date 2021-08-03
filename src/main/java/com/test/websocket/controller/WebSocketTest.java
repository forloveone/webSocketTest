package com.test.websocket.controller;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/test/{username}")
public class WebSocketTest {
    public static final Map<String, Session> ONLINE_USER_SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void openSession(@PathParam("username") String username, Session session) {
        ONLINE_USER_SESSIONS.put(username, session);
        sendMessageAll(username + "服务器连接成功！");
        sendMessage(session, "welcome websocket test! " + username);
        System.out.println(username + "连接成功");
    }

    @OnMessage
    public void onMessage(@PathParam("username") String username, String message) {
        System.out.println("服务器收到" + username + "用户 消息:" + message);
        sendMessageAll(username + "发送消息到服务器: " + message);
    }

    @OnClose
    public void onClose(@PathParam("username") String username, Session session) {
        //当前的Session 移除
        ONLINE_USER_SESSIONS.remove(username);
        //并且通知其他人当前用户已经断开连接了
        sendMessageAll(username + "断开连接！");
        try {
            session.close();
        } catch (IOException e) {
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        try {
            session.close();
        } catch (IOException e) {
        }
    }


    // 单用户推送
    public static void sendMessage(Session session, String message) {
        if (session == null) {
            return;
        }
        final RemoteEndpoint.Basic basic = session.getBasicRemote();
        if (basic == null) {
            return;
        }
        try {
            basic.sendText(message);
        } catch (IOException e) {
            System.out.println("sendMessage IOException " + e);
        }
    }

    // 全用户推送
    public static void sendMessageAll(String message) {
        ONLINE_USER_SESSIONS.forEach((sessionId, session) -> sendMessage(session, message));
    }
}
