package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PublicationServiceTest {

    @Autowired
    PublicationService service;

    @Autowired
    PublicationRepository repo;

    @Test
    void filteringByYearReturnsOnlyThatYear() {
        repo.save(new Publication("Old paper", "A. Old", "Physics",
                1999, "10.0000/old", "Old Journal", 1));
        List<Publication> result = service.search(null, null, 1999);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(p -> p.getYear() == 1999));
    }

    @Test
    void searchByAuthorIgnoresCapitalLetters() {
        repo.save(new Publication("Case test", "Zed Unique", "Physics",
                2001, "10.0000/case", "Test Journal", 0));
        List<Publication> result = service.search("zed unique", null, null);
        assertEquals(1, result.size());
    }
}