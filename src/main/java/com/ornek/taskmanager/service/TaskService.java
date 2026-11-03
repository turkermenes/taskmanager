package com.ornek.taskmanager.service;

import com.ornek.taskmanager.dto.TaskCreateRequest;
import com.ornek.taskmanager.dto.TaskResponse;
import com.ornek.taskmanager.dto.TaskUpdateRequest;
import com.ornek.taskmanager.exception.TaskNotFoundException;
import com.ornek.taskmanager.model.Task;
import com.ornek.taskmanager.model.TaskStatus;
import com.ornek.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskResponse create(TaskCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.TODO)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    public List<TaskResponse> getAll() {
        return taskRepository.findAll()
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse getById(Long id) {
        return TaskResponse.from(findTaskOrThrow(id));
    }

    public TaskResponse update(Long id, TaskUpdateRequest request) {
        Task task = findTaskOrThrow(id);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setUpdatedAt(LocalDateTime.now());

        return TaskResponse.from(taskRepository.save(task));
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}