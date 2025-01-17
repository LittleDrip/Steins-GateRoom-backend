package com.drip.wb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MusicInfo {
    private String id;
    private String name;
    private String picUrl;
    private String author;
    private Long time;
    private String url;
}
