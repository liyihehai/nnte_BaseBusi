package com.nnte.basebusi.entity;

import lombok.Data;

@Data
public class ResponseResult {
    // if request is success
    private boolean success;
    // response data
    private Object data;
    // code for errorType
    private String errorCode;
    // message display to user
    private String errorMessage;
    // error display type： 0 silent; 1 message.warn; 2 message.error; 4 notification; 9 page
    private Integer showType;
    // Convenient for back-end Troubleshooting: unique request ID
    private String traceId;
    // onvenient for backend Troubleshooting: host of current access server
    private String host;
}
