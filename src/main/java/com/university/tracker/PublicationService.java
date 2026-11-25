package com.university.tracker;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublicationService {

    public static final int PAGE_SIZE = 20;
    public static final String UNASSIGNED = "__unassigned__";

    private final PublicationRepository repo;
    private final OpenAlexClient openAlexClient;
    private final DepartmentRepository departmentRepository;
    private final Map<String, String> departmentMappings;

    public PublicationService(PublicationRepository repo, OpenAlexClient openAlexClient) {
        this(repo, openAlexClient, null);
    }

    @Autowired
    public PublicationService(PublicationRepository repo, OpenAlexClient openAlexClient,
                              DepartmentRepository departmentRepository) {
        this.repo = repo;
        this.openAlexClient = openAlexClient;
        this.departmentRepository = departmentRepository;
        this.departmentMappings = DepartmentMapping.load();
    }

    public List<Publication> search(String q, String department, Integer year) {
        return search(q, department, year, year, "newest", 1).getContent();
    }

    public Page<Publication> search(String q, String department, Integer yearFrom,
                                    Integer yearTo, String sort, int page) {
        int pageNumber = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sortFor(sort));
        return repo.findAll(specification(q, department, yearFrom, yearTo), pageable);
    }

    public Publication findById(long id) {
        return repo.findById(id).orElse(null);
    }

    public void updatePublication(Publication paper, PublicationForm form) {
        Publication values = form.toPublication(normalizeDoi(form.getDoi()));
        if (values.getDoi() != null && !values.getDoi().equals(paper.getDoi())
                && repo.existsByDoi(values.getDoi())) {
            throw new DuplicatePublicationException("A publication with this DOI already exists.");
        }
        if ((!values.getTitle().equals(paper.getTitle()) || values.getYear() != paper.getYear())
                && repo.existsByTitleAndYear(values.getTitle(), values.getYear())) {
            throw new DuplicatePublicationException("A publication with this title and year already exists.");
        }
        paper.setTitle(values.getTitle());
        paper.setAuthors(values.getAuthors());
        paper.setYear(values.getYear());
        paper.setDoi(values.getDoi());
        paper.setJournal(values.getJournal());
        paper.setDepartment(values.getDepartment());
        paper.setCitations(values.getCitations());
        repo.save(paper);
    }

    public Publication addManualPublication(PublicationForm form) {
        String doi = normalizeDoi(form.getDoi());
        String title = form.getTitle().trim();
        int year = form.getYear();
        if (doi != null && repo.existsByDoi(doi)) {
            throw new DuplicatePublicationException("A publication with this DOI already exists.");
        }
        if (repo.existsByTitleAndYear(title, year)) {
            throw new DuplicatePublicationException("A publication with this title and year already exists.");
        }
        return repo.save(form.toPublication(doi));
    }

    private Sort sortFor(String sort) {
        if ("oldest".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Order.asc("year"), Sort.Order.asc("id"));
        }
        if ("cited".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Order.desc("citations"), Sort.Order.desc("year"), Sort.Order.asc("id"));
        }
        return Sort.by(Sort.Order.desc("year"), Sort.Order.desc("id"));
    }

    private Specification<Publication> specification(String q, String department,
                                                      Integer yearFrom, Integer yearTo) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), pattern),
                        builder.like(builder.lower(root.get("authors")), pattern)));
            }
            if (department != null && !department.isBlank()) {
                if (UNASSIGNED.equals(department)) {
                    predicates.add(builder.isNull(root.get("department")));
                } else {
                    predicates.add(builder.equal(root.get("department"), department.trim()));
                }
            }
            if (yearFrom != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("year"), yearFrom));
            }
            if (yearTo != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("year"), yearTo));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public DashboardStatistics statistics() {
        List<Publication> publications = repo.findAll();
        Set<String> researchers = publications.stream()
                .map(Publication::getAuthors)
                .filter(authors -> authors != null && !authors.isBlank())
                .flatMap(authors -> java.util.Arrays.stream(authors.split(",")))
                .map(String::trim)
                .filter(author -> !author.isBlank())
                .collect(Collectors.toSet());
        long citations = publications.stream()
                .mapToLong(Publication::getCitations)
                .sum();
        long journals = publications.stream()
                .map(Publication::getJournal)
                .filter(journal -> journal != null && !journal.isBlank())
                .map(String::trim)
                .distinct()
                .count();
        return new DashboardStatistics(publications.size(), researchers.size(), citations, journals);
    }

    public ImportResult importFromOpenAlex() {
        int imported = 0;
        int skipped = 0;
        int updated = 0;
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
                String department = departmentMappings.get(work.primaryFieldName());
                ExistingPublication lookup = findExisting(doi, work.title(), work.publicationYearOrZero());
                if (lookup.exists()) {
                    Publication existing = lookup.publication();
                    if (existing != null && existing.getDepartment() == null && department != null) {
                        existing.setDepartment(department);
                        repo.save(existing);
                        updated++;
                    } else {
                        skipped++;
                    }
                    continue;
                }

                repo.save(new Publication(
                        work.title().trim(),
                        work.authorNames(),
                        department,
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
        } while (page <= totalPages && openAlexClient.shouldFetchPage(page));

        return new ImportResult(imported, skipped, updated);
    }

    private ExistingPublication findExisting(String doi, String title, int year) {
        if (doi != null) {
            var byDoi = repo.findByDoi(doi);
            if (byDoi != null && byDoi.isPresent()) {
                return new ExistingPublication(byDoi.get(), true);
            }
            if (repo.existsByDoi(doi)) {
                return new ExistingPublication(null, true);
            }
        }
        var byTitle = repo.findByTitleAndYear(title, year);
        if (byTitle != null && byTitle.isPresent()) {
            return new ExistingPublication(byTitle.get(), true);
        }
        if (repo.existsByTitleAndYear(title, year)) {
            return new ExistingPublication(null, true);
        }
        return new ExistingPublication(null, false);
    }

    public boolean assignDepartment(long id, String department) {
        return repo.assignDepartment(id, department) == 1;
    }

    public List<DepartmentOption> departmentOptions() {
        if (departmentRepository == null) {
            return List.of();
        }
        List<DepartmentOption> options = departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(department -> new DepartmentOption(department.getName(), repo.countByDepartment(department.getName())))
                .toList();
        options = new java.util.ArrayList<>(options);
        options.add(new DepartmentOption(UNASSIGNED, repo.countByDepartmentIsNull()));
        return options;
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

    public record ImportResult(int imported, int skipped, int updated) {
        public ImportResult(int imported, int skipped) {
            this(imported, skipped, 0);
        }
    }

    public record DepartmentOption(String name, long count) {
    }

    private record ExistingPublication(Publication publication, boolean exists) {
    }

    public record DashboardStatistics(long publications, long researchers,
                                      long citations, long journals) {
    }
}
