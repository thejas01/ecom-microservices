package com.ecommerce.common.utils.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private PageInfo pageInfo;
    
    public ApiResponse() {}
    
    public ApiResponse(boolean success, String message, T data, LocalDateTime timestamp, PageInfo pageInfo) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.pageInfo = pageInfo;
    }
    
    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public PageInfo getPageInfo() { return pageInfo; }
    public void setPageInfo(PageInfo pageInfo) { this.pageInfo = pageInfo; }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data, LocalDateTime.now(), null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now(), null);
    }
    
    public static <T> ApiResponse<T> success(T data, PageInfo pageInfo) {
        return new ApiResponse<>(true, null, data, LocalDateTime.now(), pageInfo);
    }
    
    public static <T> ApiResponse<T> success(T data, String message, PageInfo pageInfo) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now(), pageInfo);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), null);
    }
}