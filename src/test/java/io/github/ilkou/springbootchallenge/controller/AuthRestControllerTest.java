package io.github.ilkou.springbootchallenge.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ilkou.springbootchallenge.transverse.dto.CtUserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static io.github.ilkou.springbootchallenge.util.LogInUtils.getTokenForLogin;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void valid_credentials_using_username_should_generate_a_valid_token() throws Exception {

        String users = mvc.perform(get("/api/users/generate")
                        .param("count", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        List<CtUserDto> usersList = objectMapper.readValue(users, new TypeReference<List<CtUserDto>>(){});
        CtUserDto user = usersList.get(0);

        MockMultipartFile multipartFile = new MockMultipartFile("file", users.getBytes());
        mvc.perform(
                        fileUpload("/api/users/batch").file(multipartFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(1)))
                .andExpect(jsonPath("$.successfulImports", is(1)))
                .andExpect(jsonPath("$.failedImports", is(0)));

        String token = getTokenForLogin(user.getUsername(), user.getPassword(), mvc);

        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

    }

    @Test
    public void valid_credentials_using_email_should_generate_a_valid_token() throws Exception {

        String users = mvc.perform(get("/api/users/generate")
                        .param("count", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        List<CtUserDto> usersList = objectMapper.readValue(users, new TypeReference<List<CtUserDto>>(){});
        CtUserDto user = usersList.get(0);

        MockMultipartFile multipartFile = new MockMultipartFile("file", users.getBytes());
        mvc.perform(
                        fileUpload("/api/users/batch").file(multipartFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(1)))
                .andExpect(jsonPath("$.successfulImports", is(1)))
                .andExpect(jsonPath("$.failedImports", is(0)));

        String token = getTokenForLogin(user.getEmail(), user.getPassword(), mvc);

        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

    }
}
