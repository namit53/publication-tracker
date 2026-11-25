package com.university.tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class DepartmentSeeder implements CommandLineRunner {

    private final DepartmentRepository repository;

    public DepartmentSeeder(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws IOException {
        ClassPathResource resource = new ClassPathResource("departments.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(name -> !name.isBlank())
                    .forEach(name -> repository.findByName(name)
                            .orElseGet(() -> repository.save(new Department(name))));
        }
    }
}
