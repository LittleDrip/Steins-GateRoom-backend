package com.drip.wb.component;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.drip.wb.entity.Message;
import com.drip.wb.entity.User;
import com.drip.wb.entity.UserList;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
@ServerEndpoint("/socket/{room}/{username}/{avatar}")
public class WebSocketServer {
    /**
     * 存储各个房间的 sessionMap  第一个string是房间id，第二个map是用户类和session
     */
    public static final Map<String, Map<User, Session>> rooms = new ConcurrentHashMap<>();
    private static final Map<Session, Long> heartbeatTracker = new ConcurrentHashMap<>();
    private static final long HEARTBEAT_TIMEOUT = 15000; // 心跳超时时间，15秒

    /***
     * WebSocket 建立连接事件
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("room") String room, @PathParam("username") String username, @PathParam("avatar") String avatar) {
        rooms.putIfAbsent(room, Collections.synchronizedMap(new LinkedHashMap<>()));
        Map<User, Session> sessionMap = rooms.get(room);
        User user = new User(username, avatar,room);
        if (!sessionMap.containsKey(user)) {
//            System.out.println(username + "进入了房间 " + room);
            Message msg=new Message();
            msg.setType("portalMsg");
            msg.setMsg("【"+username+"】" + "进入了房间");
            sessionMap.put(user, session);
            sendAllMessage(setUserList(sessionMap), room);
            sendAllMessage(JSON.toJSONString(msg), room);
//            showUserList(sessionMap, room);
        }
        // 初始化心跳时间
        heartbeatTracker.put(session, System.currentTimeMillis());
    }




    /**
     * WebSocket 关闭连接事件
     * 1.把登出的用户从 sessionMap 中剃除
     * 2.发送给所有人当前登录人员信息
     */
    @OnClose
    public void onClose(@PathParam("room") String room, @PathParam("username") String username) throws IOException {
        Map<User, Session> sessionMap = rooms.get(room);
        if (sessionMap != null && username != null) {
            User userToRemove = null;
            for (User user : sessionMap.keySet()) {
                if (user.getUsername().equals(username)) {
                    userToRemove = user;
                    break;
                }
            }

            if (userToRemove != null) {
//                System.out.println(username + "退出了房间 " + room);
                Message msg = new Message();
                msg.setType("portalMsg");
                msg.setMsg("【"+username+"】"+ "离开了房间");
                Session session = sessionMap.remove(userToRemove);
                sendAllMessage(JSON.toJSONString(msg), room);
                sendAllMessage(setUserList(sessionMap), room);
//                showUserList(sessionMap, room);
                if (session != null) {
                    heartbeatTracker.remove(session); // 清理心跳跟踪
                    session.close();
                }
            }
        }
    }

    /**
     * WebSocket 接受信息事件
     * 接收处理客户端发来的数据
     * @param message 信息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("room") String room, @PathParam("username") String username,Session session) {
        try {
            JSONObject jsonMessage = JSON.parseObject(message);
            // 处理心跳消息
            if ("heartbeat".equals(jsonMessage.getString("type"))) {
                heartbeatTracker.put(session, System.currentTimeMillis());
//                System.out.println("心跳");
                return; // 不继续处理其他消息
            }
            if (jsonMessage.containsKey("type") && "musicInfo".equals(jsonMessage.getString("type"))) {
//                System.out.println("zhixing!!!!!!!!!!!!!!");
                // 处理音乐信息
                handleMusicInfo(message, room, jsonMessage.getString("user"),username);
            }

            else {
                // 处理普通消息
                Message msg = JSON.parseObject(message, Message.class);
                sendAllMessage(JSON.toJSONString(msg), room);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * WebSocket 错误事件
     * @param session 用户 Session
     * @param error 错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 显示在线用户
     */
    private void showUserList(Map<User, Session> sessionMap, String room) {
        System.out.println("------------------------------------------");
        System.out.println("房间 " + room + " 当前在线用户");
        System.out.println("------------------------------------------");
        for (User user : sessionMap.keySet()) {
            System.out.println(user.getUsername());
        }
        System.out.println("------------------------------------------");
        System.out.println();
    }

    /**
     * 设置接收消息的用户列表
     * @return 用户列表
     */

    private String setUserList(Map<User, Session> sessionMap) {
        List<User> list = new ArrayList<>(sessionMap.keySet());
        UserList userList = new UserList();
        userList.setUserlist(list);
//        System.out.println(JSON.toJSONString(userList)); //日志
        return JSON.toJSONString(userList);
    }
    /**
     * 发送消息到房间所有用户
     * @param message 消息
     */
    private void sendAllMessage(String message, String room) {
        try {
            Map<User, Session> sessionMap = rooms.get(room);
            if (sessionMap != null) {
                for (Session session : sessionMap.values()) {
                    session.getBasicRemote().sendText(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    -----------------------------------------------

    private void sendAllMessageExceptSelf(String message, String room, String username) {
        try {
            Map<User, Session> sessionMap = rooms.get(room);
            if (sessionMap != null) {
                for (Map.Entry<User, Session> entry : sessionMap.entrySet()) {
                    // 排除掉发送消息的用户
                    if (!entry.getKey().getUsername().equals(username)) {
                        entry.getValue().getBasicRemote().sendText(message);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleMusicInfo(String message, String room, String newUser,String username) {
        try {
            Map<User, Session> sessionMap = rooms.get(room);
            if (sessionMap != null) {
                for (User user : sessionMap.keySet()) {
                    if (user.getUsername().equals(newUser)&& !username.equals(newUser)) {
                        System.out.println("发送");
                        Session session = sessionMap.get(user);
                        session.getBasicRemote().sendText(message);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
//    -----------------------------------------------

}
