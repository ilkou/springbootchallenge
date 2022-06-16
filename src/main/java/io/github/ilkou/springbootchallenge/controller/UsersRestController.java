package io.github.ilkou.springbootchallenge.controller;

import io.github.ilkou.springbootchallenge.service.UserService;
import io.github.ilkou.springbootchallenge.transverse.dto.BatchUsersSummaryDto;
import io.github.ilkou.springbootchallenge.transverse.dto.CtUserDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UsersRestController {

    private final UserService service;

    @GetMapping(value = "/generate")
    public ResponseEntity<Resource> generateUsers(@RequestParam("count") Integer count) {
        try {
            Path temp = Files.createTempFile(null, ".json");
            Files.write(temp, service.generate(count).getBytes());

            File file = temp.toFile();
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("Content-Disposition", "attachment; filename=\"" + temp.getFileName() + "\"")
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BatchUsersSummaryDto> batchUsers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.batch(file));
    }

    @SecurityRequirement(name = "cirestechapi")
    @GetMapping("/me")
    public ResponseEntity<CtUserDto> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.accepted().body(service.getUserProfile(authentication.getName()));
    }

    @SecurityRequirement(name = "cirestechapi")
    @GetMapping("/{username}")
    public ResponseEntity<CtUserDto> getProfile(@PathVariable("username") final String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (username.equals(authentication.getName())) {
            return ResponseEntity.accepted().body(service.getUserProfile(authentication.getName()));
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.accepted().body(service.getUserProfile(username));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
