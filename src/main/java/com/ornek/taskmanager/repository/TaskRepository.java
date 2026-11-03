package com.ornek.taskmanager.repository;

import com.ornek.taskmanager.model.Task;
import com.ornek.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(TaskStatus status); //şart mı
}
