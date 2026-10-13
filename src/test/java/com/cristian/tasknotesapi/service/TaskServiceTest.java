package com.cristian.tasknotesapi.service;

import com.cristian.tasknotesapi.domain.task.Task;
import com.cristian.tasknotesapi.domain.task.TaskStatus;
import com.cristian.tasknotesapi.domain.user.Role;
import com.cristian.tasknotesapi.domain.user.User;
import com.cristian.tasknotesapi.repository.TaskRepository;
import com.cristian.tasknotesapi.repository.UserRepository;
import com.cristian.tasknotesapi.web.dto.TaskRequest;
import com.cristian.tasknotesapi.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    private TaskService taskService;

    private User owner;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, userRepository);
        owner = User.builder().id(1L).username("cristian").email("c@example.com").password("hash").role(Role.USER).build();
    }

    @Test
    void create_persistsTaskOwnedByCaller() {
        when(userRepository.findByUsername("cristian")).thenReturn(Optional.of(owner));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskRequest request = new TaskRequest("Write tests", "Cover the service layer", null, null);
        Task result = taskService.create(request, "cristian");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());

        assertThat(captor.getValue().getTitle()).isEqualTo("Write tests");
        assertThat(captor.getValue().getOwner()).isEqualTo(owner);
        assertThat(result.getTitle()).isEqualTo("Write tests");
    }

    @Test
    void findById_throwsWhenTaskDoesNotBelongToCaller() {
        when(taskRepository.findByIdAndOwnerUsername(99L, "cristian")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L, "cristian"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_changesStatusWhenProvided() {
        Task existing = Task.builder().id(5L).title("Old").status(TaskStatus.PENDING).owner(owner).build();
        when(taskRepository.findByIdAndOwnerUsername(5L, "cristian")).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskRequest request = new TaskRequest("New title", null, TaskStatus.DONE, null);
        Task updated = taskService.update(5L, request, "cristian");

        assertThat(updated.getTitle()).isEqualTo("New title");
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void delete_removesOwnedTask() {
        Task existing = Task.builder().id(7L).owner(owner).build();
        when(taskRepository.findByIdAndOwnerUsername(7L, "cristian")).thenReturn(Optional.of(existing));

        taskService.delete(7L, "cristian");

        verify(taskRepository).delete(existing);
    }
}
