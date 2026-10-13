package com.cristian.tasknotesapi.service;

import com.cristian.tasknotesapi.domain.task.Task;
import com.cristian.tasknotesapi.domain.task.TaskStatus;
import com.cristian.tasknotesapi.domain.user.User;
import com.cristian.tasknotesapi.repository.TaskRepository;
import com.cristian.tasknotesapi.repository.UserRepository;
import com.cristian.tasknotesapi.web.dto.TaskRequest;
import com.cristian.tasknotesapi.web.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public Task create(TaskRequest request, String ownerUsername) {
        User owner = findOwner(ownerUsername);
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .dueDate(request.dueDate())
                .owner(owner)
                .build();
        return taskRepository.save(task);
    }

    public Page<Task> findAll(String ownerUsername, TaskStatus status, Pageable pageable) {
        return status == null
                ? taskRepository.findByOwnerUsername(ownerUsername, pageable)
                : taskRepository.findByOwnerUsernameAndStatus(ownerUsername, status, pageable);
    }

    public Task findById(Long id, String ownerUsername) {
        return taskRepository.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    @Transactional
    public Task update(Long id, TaskRequest request, String ownerUsername) {
        Task task = findById(id, ownerUsername);
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        task.setDueDate(request.dueDate());
        return taskRepository.save(task);
    }

    @Transactional
    public void delete(Long id, String ownerUsername) {
        Task task = findById(id, ownerUsername);
        taskRepository.delete(task);
    }

    private User findOwner(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
