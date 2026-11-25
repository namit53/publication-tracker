package com.university.tracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleData implements CommandLineRunner {

    private final PublicationRepository repo;

    public SampleData(PublicationRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(new Publication("Sample Paper on Machine Learning", "A. Sharma, R. Gupta",
                    "Computer Science", 2025, "10.0000/sample.1", "Sample Journal of AI", 12));
            repo.save(new Publication("Sample Study of Water Quality", "P. Verma",
                    "Engineering", 2024, "10.0000/sample.2", "Sample Environmental Review", 5));
            repo.save(new Publication("Sample Work on Solar Cells", "S. Khan, M. Singh",
                    "Physics", 2025, "10.0000/sample.3", "Sample Energy Letters", 8));
        }
    }
}
