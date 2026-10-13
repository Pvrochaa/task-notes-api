package com.cristian.tasknotesapi.web;

import com.cristian.tasknotesapi.domain.task.Task;
import com.cristian.tasknotesapi.domain.task.TaskStatus;
import com.cristian.tasknotesapi.service.TaskService;
import com.cristian.tasknotesapi.web.dto.TaskRequest;
import com.cristian.tasknotesapi.web.dto.TaskResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "CRUD operations scoped to the authenticated user")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request, Authentication authentication) {
        Task task = taskService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.fromEntity(task));
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> findAll(
            @RequestParam(required = false) TaskStatus status,
            Pageable pageable,
            Authentication authentication
    ) {
        Page<TaskResponse> page = taskService.findAll(authentication.getName(), status, pageable)
                .map(TaskResponse::fromEntity);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> findById(@PathVariable Long id, Authentication authentication) {
        Task task = taskService.findById(id, authentication.getName());
        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id, @Valid @RequestBody TaskRequest request, Authentication authentication) {
        Task task = taskService.update(id, request, authentication.getName());
        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        taskService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
