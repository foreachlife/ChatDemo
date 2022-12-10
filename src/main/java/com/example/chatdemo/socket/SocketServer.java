package com.example.chatdemo.socket;

import com.example.chatdemo.pojo.DataMessage;
import com.example.chatdemo.config.ApplicationContextRegister;
import com.example.chatdemo.util.RedisUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.deploy.cache.BaseLocalApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yy
 * @data 2022/12/9 21:36
 */

@Component
@ServerEndpoint(value = "/websocket/{room}/{userId}")
public class SocketServer implements MessageListener {

    static RedisUtil redisUtil;

    private String userId;
    private String room;

    private static final ConcurrentHashMap<String, Session> userSession = new ConcurrentHashMap<String, Session>();

    static {
        redisUtil = ApplicationContextRegister.getApplicationContext().getBean(RedisUtil.class);

    }

    @OnOpen
    public void onOpen(@PathParam("room") String room, @PathParam("userId") String userId, Session session) {
        System.out.println("room:" + room);
        System.out.println("userId:" + userId);
        this.userId = userId;
        this.room = room;
        userSession.put(userId, session);
        redisUtil.sSet(room, userId);
    }

    @OnClose
    public void onClose() {
        //关闭 用户移除房间
        redisUtil.sDel(room, userId);
        //移除session
        userSession.remove(userId);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId, Session session) {
        System.out.println("收到客户端的信息:" + new Gson().toJson(message));
        redisUtil.convertAndSend("chat", message);
    }

    //群发消息
    private void broadCast(String message) {
        Gson gson = new Gson();
        DataMessage dataMessage = gson.fromJson(message, DataMessage.class);
        Set roomUsers = redisUtil.sGet(dataMessage.getRoomId());
        try {
            //查询房间中的用户与当前服务器中的用户取交集 发送消息
            for (Map.Entry<String, Session> entry : userSession.entrySet()) {
                String currentServerUser = entry.getKey();
                if (roomUsers.contains(currentServerUser)) {
                    entry.getValue().getBasicRemote().sendText(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //接收redis中的消息
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            System.out.println("Redis onMessage: " + message);
            //消息内容
            byte[] body = message.getBody();
            String topic = new String(pattern);
            String msg = new String(body, StandardCharsets.UTF_8);
            broadCast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param session 用户session
     * @param error   错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
    }

}
