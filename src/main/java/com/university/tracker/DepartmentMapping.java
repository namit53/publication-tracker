package com.university.tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

final class DepartmentMapping {

    private DepartmentMapping() {
    }

    static Map<String, String> load() {
        Map<String, String> mappings = new HashMap<>();
        ClassPathResource resource = new ClassPathResource("department-mapping.csv");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().skip(1).map(line -> line.split(",", 2))
                    .filter(columns -> columns.length == 2)
                    .forEach(columns -> {
                        String field = columns[0].trim();
                        String department = columns[1].trim();
                        if (!field.isBlank() && !department.isBlank()) {
                            mappings.put(field, department);
                        }
                    });
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load department-mapping.csv", exception);
        }
        return Collections.unmodifiableMap(mappings);
    }
}
