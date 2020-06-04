package com.example.socket;

import lombok.Data;

/**
 * 自定义socket消息体结构
 */
@Data
public class MsgObject {
    private String nickName;
    private String room;
    private Object content;
}
