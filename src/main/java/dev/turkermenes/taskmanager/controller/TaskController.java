package dev.turkermenes.taskmanager.controller;

import dev.turkermenes.taskmanager.dto.TaskCreateRequest;
import dev.turkermenes.taskmanager.dto.TaskResponse;
import dev.turkermenes.taskmanager.dto.TaskUpdateRequest;
import dev.turkermenes.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Tasks", description = "Görev işlemleri")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Kullanıcının tüm görevlerini listeler")
    @GetMapping
    public List<TaskResponse> getAll(@AuthenticationPrincipal Jwt jwt) {
        return taskService.getAll(currentUserId(jwt));
    }

    @Operation(summary = "Id ile görev getirir")
    @GetMapping("/{id}")
    public TaskResponse getById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return taskService.getById(currentUserId(jwt), id);
    }

    @Operation(summary = "Yeni görev oluşturur")
    @ApiResponse(responseCode = "201", description = "Görev oluşturuldu")
    @PostMapping
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody TaskCreateRequest request) {
        TaskResponse created = taskService.create(currentUserId(jwt), request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Görevi günceller")
    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal Jwt jwt,
                               @PathVariable Long id,
                               @Valid @RequestBody TaskUpdateRequest request) {
        return taskService.update(currentUserId(jwt), id, request);
    }

    @Operation(summary = "Görevi siler")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        taskService.delete(currentUserId(jwt), id);
    }

    private Long currentUserId(Jwt jwt) {
        return jwt.getClaim("userId");
    }
}