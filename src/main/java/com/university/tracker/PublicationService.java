package com.university.tracker;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PublicationService {

    private final PublicationRepository repo;

    public PublicationService(PublicationRepository repo) {
        this.repo = repo;
    }

    public List<Publication> search(String q, String department, Integer year) {
        return repo.findAll().stream()
                .filter(p -> q == null || q.isBlank()
                        || contains(p.getTitle(), q) || contains(p.getAuthors(), q))
                .filter(p -> department == null || department.isBlank()
                        || contains(p.getDepartment(), department))
                .filter(p -> year == null || p.getYear() == year)
                .sorted(Comparator.comparingInt(Publication::getYear).reversed())
                .toList();
    }

    private boolean contains(String text, String part) {
        return text != null && text.toLowerCase().contains(part.toLowerCase());
    }
}