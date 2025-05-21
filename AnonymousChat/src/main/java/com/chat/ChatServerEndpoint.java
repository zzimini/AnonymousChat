package com.chat;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/chat")
public class ChatServerEndpoint {

    private static final Set<Session> clients = new CopyOnWriteArraySet<>();
    private static final Map<Session, String> nicknames = new ConcurrentHashMap<>();
    private static int userCount = 0;

    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        String nickname = "익명" + (++userCount);
        nicknames.put(session, nickname);
        System.out.println(nickname + " 접속 (" + session.getId() + ")");
    }

    @OnMessage
    public void onMessage(String message, Session sender) throws IOException {
        String nickname = nicknames.getOrDefault(sender, "익명?");
        System.out.println("[" + nickname + "] 메시지 수신: " + message);

        for (Session session : clients) {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(nickname + ": " + message);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        clients.remove(session);
        String nickname = nicknames.remove(session);
        System.out.println(nickname + " 연결 종료 (" + session.getId() + ")");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket 에러 (" + session.getId() + "): " + throwable.getMessage());
    }
}
