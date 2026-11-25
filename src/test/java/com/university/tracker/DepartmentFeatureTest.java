package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DepartmentFeatureTest {

    @Autowired
    PublicationRepository repository;

    @Autowired
    PublicationService service;

    @Test
    void departmentFilterUsesExactMatch() {
        repository.save(new Publication("Department marker computer", "Author", "Computer Science",
                2025, "10.30/computer", "Journal", 1));
        repository.save(new Publication("Department marker engineering", "Author", "Engineering",
                2025, "10.30/engineering", "Journal", 1));

        var page = service.search("department marker", "Computer Science", null, null, "newest", 1);

        assertTrue(page.getContent().stream().allMatch(p -> "Computer Science".equals(p.getDepartment())));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void unassignedFilterReturnsOnlyNullDepartments() {
        repository.save(new Publication("Department marker unassigned", "Author", null,
                2025, "10.30/unassigned", "Journal", 1));

        var page = service.search("department marker", PublicationService.UNASSIGNED,
                null, null, "newest", 1);

        assertTrue(page.getContent().stream().allMatch(p -> p.getDepartment() == null));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void mappedAndUnmappedOpenAlexFieldsSetExpectedDepartment() {
        PublicationRepository repository = mock(PublicationRepository.class);
        OpenAlexClient client = mock(OpenAlexClient.class);
        PublicationService importer = new PublicationService(repository, client);
        OpenAlexWork mapped = work("Mapped department paper", "Computer Science");
        OpenAlexWork unmapped = work("Unmapped department paper", "Biology");
        when(client.fetchWorks(1)).thenReturn(new OpenAlexResponse(
                new OpenAlexMeta(2, 1, 100), List.of(mapped, unmapped)));
        when(repository.findByDoi(any())).thenReturn(Optional.empty());
        when(repository.findByTitleAndYear(any(), any(Integer.class))).thenReturn(Optional.empty());

        importer.importFromOpenAlex();

        var captor = org.mockito.ArgumentCaptor.forClass(Publication.class);
        verify(repository, org.mockito.Mockito.times(2)).save(captor.capture());
        var saved = captor.getAllValues();
        assertEquals("Computer Science", saved.get(0).getDepartment());
        assertNull(saved.get(1).getDepartment());
    }

    @Test
    void reimportFillsMissingDepartmentOnExistingPublication() {
        PublicationRepository repository = mock(PublicationRepository.class);
        OpenAlexClient client = mock(OpenAlexClient.class);
        PublicationService importer = new PublicationService(repository, client);
        Publication existing = new Publication("Existing paper", "Author", null,
                2025, "10.30/existing", "Journal", 2);
        OpenAlexWork existingWork = new OpenAlexWork("Existing paper", null, 2025,
                "10.30/existing", null, 1,
                new OpenAlexPrimaryTopic(new OpenAlexTopicField("Physics")));
        when(client.fetchWorks(1)).thenReturn(new OpenAlexResponse(
                new OpenAlexMeta(1, 1, 100), List.of(existingWork)));
        when(repository.findByDoi("10.30/existing")).thenReturn(Optional.of(existing));

        PublicationService.ImportResult result = importer.importFromOpenAlex();

        assertEquals(0, result.imported());
        assertEquals(1, result.updated());
        assertEquals("Physics", existing.getDepartment());
        verify(repository).save(existing);
    }

    private OpenAlexWork work(String title, String field) {
        return new OpenAlexWork(title, null, 2025, "10.30/" + title.replace(' ', '-').toLowerCase(),
                null, 1, new OpenAlexPrimaryTopic(new OpenAlexTopicField(field)));
    }
}
