package dev.turkermenes.taskmanager.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Görev bulunamadı. id: " + id);
    }
}
