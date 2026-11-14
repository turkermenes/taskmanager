package com.ornek.taskmanager.integration;

import com.jayway.jsonpath.JsonPath;
import com.ornek.taskmanager.TestcontainersConfiguration;
import com.ornek.taskmanager.repository.TaskRepository;
import com.ornek.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class TaskFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Kayıt, giriş ve görev oluşturma akışı uçtan uca çalışır")
    void registerLoginAndCreateTask() throws Exception {
        register("ahmet");
        String token = login("ahmet");
        createTask(token, "Rapor yaz");

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Rapor yaz"))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    @Test
    @DisplayName("Kullanıcı başkasının görevini göremez ve silemez")
    void userCannotAccessOthersTask() throws Exception {
        register("ahmet");
        register("mehmet");
        String ahmetToken = login("ahmet");
        String mehmetToken = login("mehmet");

        Long ahmetTaskId = createTask(ahmetToken, "Ahmet'in görevi");

        mockMvc.perform(get("/api/v1/tasks/" + ahmetTaskId)
                        .header("Authorization", "Bearer " + mehmetToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/tasks/" + ahmetTaskId)
                        .header("Authorization", "Bearer " + mehmetToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + mehmetToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        assertThat(taskRepository.existsById(ahmetTaskId)).isTrue();
    }

    @Test
    @DisplayName("Aynı kullanıcı adıyla ikinci kayıt 409 döner")
    void registerTwice_shouldReturn409() throws Exception {
        register("ahmet");

        String body = """
                {
                  "username": "ahmet",
                  "email": "baska@ornek.com",
                  "password": "Sifre1234"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Kullanıcı zaten mevcut"))
                .andExpect(jsonPath("$.detail").value(containsString("kullanıcı adı")));
    }

    // ---------- Yardımcı metotlar ----------

    private void register(String username) throws Exception {
        String body = """
                {
                  "username": "%s",
                  "email": "%s@ornek.com",
                  "password": "Sifre1234"
                }
                """.formatted(username, username);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private String login(String username) throws Exception {
        String body = """
                { "username": "%s", "password": "Sifre1234" }
                """.formatted(username);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private Long createTask(String token, String title) throws Exception {
        String body = """
                { "title": "%s" }
                """.formatted(title);

        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
