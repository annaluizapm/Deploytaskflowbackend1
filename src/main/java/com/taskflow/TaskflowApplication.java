package com.taskflow;

import com.taskflow.config.RenderDatabaseUrl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskflowApplication {

	public static void main(String[] args) {
		RenderDatabaseUrl.applyIfPresent();
		SpringApplication.run(TaskflowApplication.class, args);
	}

}
