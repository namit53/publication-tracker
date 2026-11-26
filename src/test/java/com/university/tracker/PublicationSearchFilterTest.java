package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PublicationSearchFilterTest {

    @Autowired
    PublicationRepository repository;

    @Autowired
    PublicationService service;

    @Test
    void departmentFilterReturnsOnlyPapersFromThatDepartment() {
        String marker = UUID.randomUUID().toString();
        String requestedDepartment = "Department " + marker;
        Publication requested = save("Requested paper " + marker, requestedDepartment, 2024);
        save("Other department paper " + marker, "Other department " + marker, 2024);

        var results = service.search(null, requestedDepartment, null, null, "newest", 1);

        assertEquals(Set.of(requested.getId()), results.getContent().stream()
                .map(Publication::getId).collect(Collectors.toSet()));
    }

    @Test
    void searchWithNoMatchReturnsNoPapers() {
        var results = service.search("no-match-" + UUID.randomUUID(), null,
                null, null, "newest", 1);

        assertTrue(results.getContent().isEmpty());
        assertEquals(0, results.getTotalElements());
    }

    @Test
    void yearRangeReturnsOnlyPapersInsideTheRange() {
        String marker = UUID.randomUUID().toString();
        save("Before range " + marker, "Year range " + marker, 2018);
        Publication lowerBound = save("Lower bound " + marker, "Year range " + marker, 2020);
        Publication upperBound = save("Upper bound " + marker, "Year range " + marker, 2024);
        save("After range " + marker, "Year range " + marker, 2026);

        var results = service.search(marker, null, 2020, 2024, "newest", 1);

        assertEquals(Set.of(lowerBound.getId(), upperBound.getId()), results.getContent().stream()
                .map(Publication::getId).collect(Collectors.toSet()));
        assertTrue(results.getContent().stream()
                .allMatch(paper -> paper.getYear() >= 2020 && paper.getYear() <= 2024));
    }

    private Publication save(String title, String department, int year) {
        return repository.save(new Publication(title, "Test Author", department, year,
                "10.9999/" + UUID.randomUUID(), "Test Journal", 0));
    }
}
