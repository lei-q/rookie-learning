package com.lay.rookie.rookielearning.nettysocketio;

import com.lay.rookie.rookielearning.dto.Result;
import lombok.Data;

/**
 * 自定义socket消息体结构
 */
@Data
public class Message {
    private String token;
    private Object content;

    public Message(){}

    public Message(String token) {
        super();
        this.token = token;
        this.content = Result.success();
    }

    public Message(String token, Object data) {
        super();
        this.token = token;
        this.content = Result.success(data);
    }
}
