package com.cristian.tasknotesapi.web;

import com.cristian.tasknotesapi.domain.task.Task;
import com.cristian.tasknotesapi.domain.task.TaskStatus;
import com.cristian.tasknotesapi.domain.user.Role;
import com.cristian.tasknotesapi.domain.user.User;
import com.cristian.tasknotesapi.security.JwtService;
import com.cristian.tasknotesapi.service.TaskService;
import com.cristian.tasknotesapi.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private Task sampleTask() {
        User owner = User.builder().id(1L).username("cristian").role(Role.USER).build();
        return Task.builder().id(10L).title("Write README").status(TaskStatus.PENDING).owner(owner).build();
    }

    @Test
    @WithMockUser(username = "cristian")
    void create_returns201WithLocationBody() throws Exception {
        when(taskService.create(any(), eq("cristian"))).thenReturn(sampleTask());

        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {"title":"Write README"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write README"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "cristian")
    void create_withBlankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {"title":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    @WithMockUser(username = "cristian")
    void findAll_returnsPagedResults() throws Exception {
        Page<Task> page = new PageImpl<>(List.of(sampleTask()), PageRequest.of(0, 20), 1);
        when(taskService.findAll(eq("cristian"), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Write README"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "cristian")
    void findById_whenMissing_returns404() throws Exception {
        when(taskService.findById(eq(404L), eq("cristian"))).thenThrow(new ResourceNotFoundException("Task not found: 404"));

        mockMvc.perform(get("/api/tasks/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void create_withoutAuthentication_isRejected() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {"title":"No auth"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
