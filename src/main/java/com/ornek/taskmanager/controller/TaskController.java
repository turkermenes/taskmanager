package com.ornek.taskmanager.controller;

import com.ornek.taskmanager.dto.TaskCreateRequest;
import com.ornek.taskmanager.dto.TaskResponse;
import com.ornek.taskmanager.dto.TaskUpdateRequest;
import com.ornek.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Tüm görevleri listeler")
    @GetMapping
    public List<TaskResponse> getAll() {
        return taskService.getAll();
    }

    @Operation(summary = "Id ile görev getirir")
    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @Operation(summary = "Yeni görev oluşturur")
    @ApiResponse(responseCode = "201", description = "Görev oluşturuldu")
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskCreateRequest request) {
        TaskResponse created = taskService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Görevi günceller")
    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id,
                               @Valid @RequestBody TaskUpdateRequest request) {
        return taskService.update(id, request);
    }

    @Operation(summary = "Görevi siler")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}