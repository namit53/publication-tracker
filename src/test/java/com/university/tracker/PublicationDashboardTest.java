package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

class PublicationDashboardTest {

    @Test
    void statisticsAreDerivedFromStoredPublications() {
        PublicationRepository repository = org.mockito.Mockito.mock(PublicationRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                new Publication("Paper one", "Ada Author, Ben Researcher", "Engineering",
                        2024, "10.1/one", "Research Journal", 5),
                new Publication("Paper two", "Ada Author", "Science",
                        2023, null, "Research Journal", 7)));

        PublicationService service = new PublicationService(repository,
                org.mockito.Mockito.mock(OpenAlexClient.class));

        PublicationService.DashboardStatistics statistics = service.statistics();

        assertEquals(2, statistics.publications());
        assertEquals(2, statistics.researchers());
        assertEquals(12, statistics.citations());
        assertEquals(1, statistics.journals());
    }
}
