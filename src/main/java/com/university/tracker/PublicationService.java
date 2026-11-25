package com.university.tracker;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PublicationService {

    private final PublicationRepository repo;
    private final OpenAlexClient openAlexClient;

    public PublicationService(PublicationRepository repo, OpenAlexClient openAlexClient) {
        this.repo = repo;
        this.openAlexClient = openAlexClient;
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

    public ImportResult importFromOpenAlex() {
        int imported = 0;
        int skipped = 0;
        int page = 1;
        int totalPages;

        do {
            OpenAlexResponse response = openAlexClient.fetchWorks(page);
            if (response == null || response.results() == null) {
                break;
            }

            for (OpenAlexWork work : response.results()) {
                if (work == null || work.title() == null || work.title().isBlank()) {
                    skipped++;
                    continue;
                }

                String doi = normalizeDoi(work.doi());
                if ((doi != null && repo.existsByDoi(doi))
                        || repo.existsByTitleAndYear(work.title(), work.publicationYearOrZero())) {
                    skipped++;
                    continue;
                }

                repo.save(new Publication(
                        work.title().trim(),
                        work.authorNames(),
                        null,
                        work.publicationYearOrZero(),
                        doi,
                        work.journalName(),
                        work.citedByCountOrZero()));
                imported++;
            }

            int perPage = response.meta() == null || response.meta().perPage() <= 0
                    ? 100 : response.meta().perPage();
            int total = response.meta() == null ? response.results().size() : response.meta().count();
            totalPages = Math.max(1, (int) Math.ceil((double) total / perPage));
            page++;
        } while (page <= totalPages);

        return new ImportResult(imported, skipped);
    }

    private String normalizeDoi(String doi) {
        if (doi == null || doi.isBlank()) {
            return null;
        }
        String normalized = doi.trim();
        if (normalized.startsWith("https://doi.org/")) {
            normalized = normalized.substring("https://doi.org/".length());
        } else if (normalized.startsWith("http://doi.org/")) {
            normalized = normalized.substring("http://doi.org/".length());
        }
        return normalized.isBlank() ? null : normalized;
    }

    public record ImportResult(int imported, int skipped) {
    }
}
