package com.cristian.tasknotesapi;

import org.springframework.boot.SpringApplication;

public class TestTaskNotesApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskNotesApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
