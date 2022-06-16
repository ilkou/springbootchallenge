package io.github.ilkou.springbootchallenge.service;

import io.github.ilkou.springbootchallenge.transverse.dto.BatchUsersSummaryDto;
import io.github.ilkou.springbootchallenge.transverse.dto.CtUserDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    String generate(Integer count);
    BatchUsersSummaryDto batch(MultipartFile content);
    CtUserDto getUserProfile(String username);

}
