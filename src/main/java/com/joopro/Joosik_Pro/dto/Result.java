package com.joopro.Joosik_Pro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class Result <T>{
    private int code;
    private HttpStatus status;
    private String message;
    private T data;

    public Result(HttpStatus status, String message, T data) {
        this.code = status.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> of(HttpStatus httpStatus, String message, T data) {
        return new Result<>(httpStatus, message, data);
    }

    public static <T> Result<T> of(HttpStatus httpStatus, T data) {
        return of(httpStatus, httpStatus.name(), data);
    }

    public static <T> Result<T> ok(T data) {
        return of(HttpStatus.OK, HttpStatus.OK.name(), data);
    }

}
