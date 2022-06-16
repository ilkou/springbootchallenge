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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UsersRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void generate_users_with_positive_count() throws Exception {

        // expect unsecured request to return a file with array of random users
        mvc.perform(get("/api/users/generate")
                        .param("count", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    public void generate_users_with_negative_count() throws Exception {

        // expect unsecured request to return a file with array of random users
        mvc.perform(get("/api/users/generate")
                        .param("count", "-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void generate_users_and_batch_them() throws Exception {

        String users = mvc.perform(get("/api/users/generate")
                        .param("count", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        System.out.println(users);

        MockMultipartFile multipartFile = new MockMultipartFile("file", users.getBytes());

        String summaryBatch = mvc.perform(
                        fileUpload("/api/users/batch").file(multipartFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(2)))
                .andExpect(jsonPath("$.successfulImports", is(2)))
                .andExpect(jsonPath("$.failedImports", is(0)))
                .andReturn().getResponse().getContentAsString();

        System.out.println("summary: " + summaryBatch);


    }

    @Test
    public void batch_same_user_should_fail() throws Exception {

        String users = mvc.perform(get("/api/users/generate")
                        .param("count", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        MockMultipartFile multipartFile = new MockMultipartFile("file", users.getBytes());
        mvc.perform(
                        fileUpload("/api/users/batch").file(multipartFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(1)))
                .andExpect(jsonPath("$.successfulImports", is(1)))
                .andExpect(jsonPath("$.failedImports", is(0)));
        mvc.perform(
                        fileUpload("/api/users/batch").file(multipartFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(1)))
                .andExpect(jsonPath("$.successfulImports", is(0)))
                .andExpect(jsonPath("$.failedImports", is(1)));

    }

    @Test
    public void persist_user_with_small_password_should_fail() throws Exception {

        String users = mvc.perform(get("/api/users/generate")
                        .param("count", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        String newUsersList = users.replaceAll("\"password\": \"[^\"]+\"", "\"password\": \"123\"");

        MockMultipartFile multipartFile = new MockMultipartFile("file", newUsersList.getBytes());
        mvc.perform(
                        fileUpload("/api/users/batch").file(multipartFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(1)))
                .andExpect(jsonPath("$.successfulImports", is(0)))
                .andExpect(jsonPath("$.failedImports", is(1)));

    }

    @Test
    public void an_authenticated_user_have_access_to_me_page() throws Exception {

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
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));

    }

    @Test
    public void an_authenticated_user_have_access_to_his_profile() throws Exception {

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

        mvc.perform(get("/api/users/" + user.getUsername())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));

    }

    @Test
    public void an_admin_have_access_to_all_profiles() throws Exception {

        String users = mvc.perform(get("/api/users/generate")
                        .param("count", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        String newUsersList = users.replaceAll("\"role\": \"[^\"]+\"", "\"role\": \"ADMIN\"");
        List<CtUserDto> usersList = objectMapper.readValue(users, new TypeReference<List<CtUserDto>>(){});
        CtUserDto user1 = usersList.get(0);
        CtUserDto user2 = usersList.get(1);

        MockMultipartFile multipartFile = new MockMultipartFile("file", newUsersList.getBytes());
        mvc.perform(
                        fileUpload("/api/users/batch").file(multipartFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(2)))
                .andExpect(jsonPath("$.successfulImports", is(2)))
                .andExpect(jsonPath("$.failedImports", is(0)));

        String token = getTokenForLogin(user1.getUsername(), user1.getPassword(), mvc);

        mvc.perform(get("/api/users/" + user2.getUsername())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.username", is(user2.getUsername())))
                .andExpect(jsonPath("$.email", is(user2.getEmail())));

    }

    @Test
    public void a_normal_user_cant_access_others_profiles() throws Exception {

        String users = mvc.perform(get("/api/users/generate")
                        .param("count", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        String newUsersList = users.replaceAll("\"role\": \"[^\"]+\"", "\"role\": \"USER\"");
        List<CtUserDto> usersList = objectMapper.readValue(users, new TypeReference<List<CtUserDto>>(){});
        CtUserDto user1 = usersList.get(0);
        CtUserDto user2 = usersList.get(1);

        MockMultipartFile multipartFile = new MockMultipartFile("file", newUsersList.getBytes());
        mvc.perform(
                        fileUpload("/api/users/batch").file(multipartFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(2)))
                .andExpect(jsonPath("$.successfulImports", is(2)))
                .andExpect(jsonPath("$.failedImports", is(0)));

        String token = getTokenForLogin(user1.getUsername(), user1.getPassword(), mvc);

        mvc.perform(get("/api/users/" + user2.getUsername())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

}
