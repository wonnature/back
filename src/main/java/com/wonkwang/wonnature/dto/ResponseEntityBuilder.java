package com.wonkwang.wonnature.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseEntityBuilder {

    //HTTP 응답을 생성하는데 사용

    //응답에 데이터를 포함시킬때 사용
    public static <T> ResponseEntity<ResponseDTO<T>> build(String message, HttpStatus statusCode, T content) {
        return new ResponseEntity<>(ResponseDTO.<T>builder()
                .message(message)
                .content(content)
                .build(),statusCode);
    }
    //응답에 데이터를 포함시키지 않을때 사용
    public static ResponseEntity<ResponseDTO<?>> build(String message, HttpStatus statusCode) {
        return new ResponseEntity<>(ResponseDTO.builder()
                .message(message)
                .content(null)
                .build(), statusCode);
    }
}