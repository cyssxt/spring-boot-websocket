package com.cyssxt.springbootwebsocket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket/{gid}/{sid}")
@Component
public class NotifyWebSocket {

    private final static Logger logger = LoggerFactory.getLogger(NotifyWebSocket.class);

    private static Map<String,Map<String,Session>> sockets= new ConcurrentHashMap<>();

    public void put(String sid,String uip,Session session){
        Map<String,Session> sessionMap = sockets.get(sid);
        if(sessionMap==null){
            sessionMap = new HashMap<>();
            sockets.put(sid,sessionMap);
        }
        sessionMap.put(uip,session);
    }

    public Session get(String sid,String uip){
        Map<String,Session> sessionMap = sockets.get(sid);
        if(sessionMap==null){
            return null;
        }
        return sessionMap.get(uip);
    }

    @OnOpen
    public void onOpen(Session session,@PathParam("sid") String sid,@PathParam("uip") String uip) throws IOException {
        put(sid,uip,session);     //加入set中
        logger.info("onOpen={},uip={}",sid,uip);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid,@PathParam("uip") String uip) {
        sockets.remove(sid);
        logger.info("close:{}",sid);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(@PathParam("sid") String sid,@PathParam("uip") String uip,String message, Session session) {
        logger.info("收到来自窗口"+sid+"的信息:"+message);
        session.getAsyncRemote().sendText("heartbit");
    }

    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(@PathParam("gid")String gid,@PathParam("sid") String sid, Session session, Throwable error) {
        logger.error("发生错误={}",sid);
        error.printStackTrace();
    }
    /**
     * 实现服务器主动推送
     */
    public void sendMessage(Session session,String message) throws IOException {
        session.getAsyncRemote().sendText(message);
    }

    public void sendMessageBySid(String gid,String sid,String message,String crawlerId) throws IOException {
        Session session = get(gid,sid);
        logger.info("sendMessageBySid={}，uip={},message={}",gid,sid,message);
        if(session!=null) {
            session.getAsyncRemote().sendText(message);
        }
    }
}
