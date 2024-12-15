package com.ss.heartlinkapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ChatHandler extends TextWebSocketHandler {

    private static Map<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

//        payload에 targetid 붙여서 같이 받은다음 파싱해서 비교하면 되지 않을까??

        String payload = message.getPayload();
        log.info("넘어온 값: {}", payload);
//        log.info("세션 확인 -> {}", sessions);
        // 모든 사용자에게 메시지 전송
        for(WebSocketSession s : sessions.values()) {
            if(s.isOpen()&& ! s.equals(session)) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info("사용자 연결 종료: {}" , session.getId());
    }
}
