package io.github.ilkou.springbootchallenge.transverse.dto;

import lombok.Data;

@Data
public class AuthTokenDto {

    private String accessToken;

    public AuthTokenDto(String accessToken) {
        this.accessToken = accessToken;
    }
}
