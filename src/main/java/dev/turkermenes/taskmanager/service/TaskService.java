package dev.turkermenes.taskmanager.service;

import dev.turkermenes.taskmanager.dto.TaskCreateRequest;
import dev.turkermenes.taskmanager.dto.TaskResponse;
import dev.turkermenes.taskmanager.dto.TaskUpdateRequest;
import dev.turkermenes.taskmanager.exception.TaskNotFoundException;
import dev.turkermenes.taskmanager.model.Task;
import dev.turkermenes.taskmanager.model.TaskStatus;
import dev.turkermenes.taskmanager.model.User;
import dev.turkermenes.taskmanager.repository.TaskRepository;
import dev.turkermenes.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse create(Long userId, TaskCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        User owner = userRepository.getReferenceById(userId);

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.TODO)
                .createdAt(now)
                .updatedAt(now)
                .owner(owner)
                .build();

        Task saved = taskRepository.save(task);
        log.info("Görev oluşturuldu. id={}, userId={}", saved.getId(), userId);
        return TaskResponse.from(saved);
    }

    public List<TaskResponse> getAll(Long userId) {
        return taskRepository.findAllByOwnerId(userId)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse getById(Long userId, Long id) {
        return TaskResponse.from(findOwnedTaskOrThrow(userId, id));
    }

    @Transactional
    public TaskResponse update(Long userId, Long id, TaskUpdateRequest request) {
        Task task = findOwnedTaskOrThrow(userId, id);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setUpdatedAt(LocalDateTime.now());

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Task task = findOwnedTaskOrThrow(userId, id);
        taskRepository.delete(task);
        log.info("Görev silindi. id={}, userId={}", id, userId);
    }

    private Task findOwnedTaskOrThrow(Long userId, Long id) {
        return taskRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}