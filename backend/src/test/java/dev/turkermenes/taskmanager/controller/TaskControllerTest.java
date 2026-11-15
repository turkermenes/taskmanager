package dev.turkermenes.taskmanager.controller;

import dev.turkermenes.taskmanager.config.SecurityConfig;
import dev.turkermenes.taskmanager.controller.TaskController;
import dev.turkermenes.taskmanager.dto.TaskCreateRequest;
import dev.turkermenes.taskmanager.dto.TaskResponse;
import dev.turkermenes.taskmanager.exception.TaskNotFoundException;
import dev.turkermenes.taskmanager.model.TaskStatus;
import dev.turkermenes.taskmanager.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

    private static final String BASE_URL = "/api/v1/tasks";
    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor authenticatedUser() {
        return jwt().jwt(token -> token
                .subject("ahmet")
                .claim("userId", USER_ID));
    }

    private static TaskResponse sampleTask(Long id, String title) {
        LocalDateTime now = LocalDateTime.now();
        return new TaskResponse(id, title, null, TaskStatus.TODO, now, now);
    }

    @Test
    @DisplayName("Token olmadan istek 401 döner")
    void getAll_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("Token ile kullanıcının görevleri listelenir")
    void getAll_withToken_shouldReturnTasks() throws Exception {
        when(taskService.getAll(USER_ID)).thenReturn(List.of(sampleTask(1L, "Rapor yaz")));

        mockMvc.perform(get(BASE_URL).with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Rapor yaz"))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    @Test
    @DisplayName("Geçerli görev oluşturulunca 201 ve Location header döner")
    void create_withValidBody_shouldReturn201() throws Exception {
        when(taskService.create(eq(USER_ID), any(TaskCreateRequest.class)))
                .thenReturn(sampleTask(10L, "Rapor yaz"));

        String body = """
                {
                  "title": "Rapor yaz",
                  "description": "Q4 raporu"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/tasks/10")))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("Boş başlıkla istek 400 ve alan hatası döner")
    void create_withBlankTitle_shouldReturn400() throws Exception {
        String body = """
                { "title": "" }
                """;

        mockMvc.perform(post(BASE_URL)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Başlık boş olamaz"));

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("Görev bulunamazsa 404 ProblemDetail döner")
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(taskService.getById(USER_ID, 99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get(BASE_URL + "/99").with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Görev bulunamadı"))
                .andExpect(jsonPath("$.detail").value("Görev bulunamadı. id: 99"));
    }

    @Test
    @DisplayName("Token ile görev silindiğinde 204 döner")
    void delete_withToken_shouldReturn204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/5").with(authenticatedUser()))
                .andExpect(status().isNoContent());

        verify(taskService).delete(USER_ID, 5L);
    }
}
