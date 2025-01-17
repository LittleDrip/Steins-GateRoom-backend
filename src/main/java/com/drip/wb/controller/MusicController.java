package com.drip.wb.controller;

import com.drip.wb.entity.MusicInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/music")
public class MusicController {
    
    // 使用 ConcurrentHashMap 存储房间音乐列表
    private final ConcurrentHashMap<String, List<MusicInfo>> musicCache = new ConcurrentHashMap<>();
    
    @GetMapping("/cache/{roomId}")
    public ResponseEntity<List<MusicInfo>> getCachedList(@PathVariable String roomId) {
        List<MusicInfo> cachedList = musicCache.get(roomId);
        if (cachedList == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cachedList);
    }
    
    @PostMapping("/cache/{roomId}")
    public ResponseEntity<Void> cacheList(
            @PathVariable String roomId,
            @RequestBody List<MusicInfo> musicList) {
        musicCache.put(roomId, musicList);
        return ResponseEntity.ok().build();
    }
}