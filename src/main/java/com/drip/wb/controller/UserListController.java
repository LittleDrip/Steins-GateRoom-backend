package com.drip.wb.controller;

import com.alibaba.fastjson2.JSONObject;
import com.drip.wb.component.WebSocketServer;
import com.drip.wb.entity.User;
import jakarta.websocket.Session;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.drip.wb.component.WebSocketServer.rooms;


@RestController
@RequestMapping("/list")
public class UserListController {
/**
 * 稍后使用java集合实现
 */


//    @GetMapping("/{username}")
//    public JSONObject getUsername(@PathVariable("username") String username) {
//        JSONObject jsonObject = new JSONObject();
//        boolean isEmpty = sessionMap.isEmpty();
//        jsonObject.put("isEmpty", isEmpty);
//        jsonObject.put("isExist", false);
//        if (!isEmpty) {
//            boolean isExist = sessionMap.containsKey(username);
//            jsonObject.replace("isExist", isExist);
//        }
//        return jsonObject;
//    }

    @GetMapping("rooms")
    public Map<String, List<User>> getAllRooms() {
        Map<String, List<User>> roomUsers = new ConcurrentHashMap<>();
        for (Map.Entry<String, Map<User, Session>> entry : WebSocketServer.rooms.entrySet()) {
            List<User> userList = new ArrayList<>(entry.getValue().keySet());
            roomUsers.put(entry.getKey(), userList);
        }
        return roomUsers;
    }
}


