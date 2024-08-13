package com.drip.wb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String type;
    private String name;
    private String time;
    private String avatar;
    private String msg;
}
