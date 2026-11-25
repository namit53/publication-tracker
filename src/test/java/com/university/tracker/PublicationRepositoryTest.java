package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PublicationRepositoryTest {

    @Autowired
    PublicationRepository repo;

    @Test
    void savedPaperCanBeFoundAgain() {
        Publication saved = repo.save(new Publication("Test paper", "A. Author",
                "Computer Science", 2025, "10.0000/test", "Test Journal", 3));
        assertTrue(repo.findById(saved.getId()).isPresent());
    }
}