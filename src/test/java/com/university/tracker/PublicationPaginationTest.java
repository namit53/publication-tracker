package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PublicationPaginationTest {

    @Autowired
    PublicationRepository repository;

    @Autowired
    PublicationService service;

    @Test
    void pageContainsAtMostTwentyPublications() {
        for (int i = 0; i < 25; i++) {
            repository.save(new Publication("Pagination marker " + i, "Author " + i,
                    "Testing", 2020 + (i % 3), "10.20/pagination-" + i, "Test Journal", i));
        }

        var page = service.search("pagination marker", null, null, null, "newest", 1);

        assertTrue(page.getContent().size() <= 20);
        assertEquals(20, page.getSize());
        assertEquals(25, page.getTotalElements());
    }

    @Test
    void mostCitedSortOrdersResultsByCitationCount() {
        repository.save(new Publication("Sorting marker low", "Author", "Testing",
                2025, "10.20/sort-low", "Test Journal", 2));
        repository.save(new Publication("Sorting marker high", "Author", "Testing",
                2023, "10.20/sort-high", "Test Journal", 99));

        var page = service.search("sorting marker", null, null, null, "cited", 1);

        assertEquals("Sorting marker high", page.getContent().get(0).getTitle());
    }
}
