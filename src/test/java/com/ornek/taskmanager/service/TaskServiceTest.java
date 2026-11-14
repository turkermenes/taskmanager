package com.ornek.taskmanager.service;

import com.ornek.taskmanager.dto.TaskCreateRequest;
import com.ornek.taskmanager.dto.TaskResponse;
import com.ornek.taskmanager.dto.TaskUpdateRequest;
import com.ornek.taskmanager.exception.TaskNotFoundException;
import com.ornek.taskmanager.model.Task;
import com.ornek.taskmanager.model.TaskStatus;
import com.ornek.taskmanager.model.User;
import com.ornek.taskmanager.repository.TaskRepository;
import com.ornek.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Yeni görev TODO durumuyla oluşturulur ve kaydedilir")
    void create_shouldSaveTaskWithTodoStatus() {
        // given
        User owner = User.builder().id(USER_ID).username("ahmet").build();
        when(userRepository.getReferenceById(USER_ID)).thenReturn(owner);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(10L);
            return task;
        });
        TaskCreateRequest request = new TaskCreateRequest("Rapor yaz", "Q4 raporu");

        // when
        TaskResponse response = taskService.create(USER_ID, request);

        // then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Rapor yaz");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.createdAt()).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Başka kullanıcının görevi istenirse TaskNotFoundException fırlar")
    void getById_whenTaskNotOwned_shouldThrowNotFound() {
        // given
        when(taskRepository.findByIdAndOwnerId(99L, USER_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> taskService.getById(USER_ID, 99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Görev bulunamazsa silme işlemi yapılmaz")
    void delete_whenTaskNotFound_shouldNotDelete() {
        // given
        when(taskRepository.findByIdAndOwnerId(99L, USER_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> taskService.delete(USER_ID, 99L))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Güncelleme alanları ve güncellenme tarihini değiştirir")
    void update_shouldChangeFieldsAndUpdatedAt() {
        // given
        LocalDateTime oldTime = LocalDateTime.now().minusDays(1);
        Task existingTask = Task.builder()
                .id(5L)
                .title("Eski başlık")
                .description("Eski açıklama")
                .status(TaskStatus.TODO)
                .createdAt(oldTime)
                .updatedAt(oldTime)
                .build();

        when(taskRepository.findByIdAndOwnerId(5L, USER_ID)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskUpdateRequest request = new TaskUpdateRequest("Yeni başlık", "Yeni açıklama", TaskStatus.DONE);

        // when
        TaskResponse response = taskService.update(USER_ID, 5L, request);

        // then
        assertThat(response.title()).isEqualTo("Yeni başlık");
        assertThat(response.description()).isEqualTo("Yeni açıklama");
        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.updatedAt()).isAfter(oldTime);
        assertThat(response.createdAt()).isEqualTo(oldTime);
    }

    @Test
    @DisplayName("Kullanıcının tüm görevleri listelenir")
    void getAll_shouldReturnUserTasks() {
        // given
        Task task1 = Task.builder().id(1L).title("Görev 1").status(TaskStatus.TODO).build();
        Task task2 = Task.builder().id(2L).title("Görev 2").status(TaskStatus.DONE).build();

        when(taskRepository.findAllByOwnerId(USER_ID)).thenReturn(List.of(task1, task2));

        // when
        List<TaskResponse> allTasks = taskService.getAll(USER_ID);

        // then
        assertThat(allTasks)
                .hasSize(2)
                .extracting(TaskResponse::title)
                .containsExactly("Görev 1", "Görev 2");
    }
}