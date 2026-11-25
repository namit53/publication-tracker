package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

class OpenAlexImportTest {

    private final PublicationRepository repository = org.mockito.Mockito.mock(PublicationRepository.class);
    private final OpenAlexClient client = org.mockito.Mockito.mock(OpenAlexClient.class);
    private final PublicationService service = new PublicationService(repository, client);

    @Test
    void importsPublicationFieldsAndSkipsExistingDoi() {
        OpenAlexWork work = new OpenAlexWork(
                "OpenAlex title",
                List.of(new OpenAlexAuthorship(new OpenAlexAuthor("Ada Author"))),
                2024,
                "https://doi.org/10.1234/example",
                new OpenAlexLocation(new OpenAlexSource("Example Journal")),
                7);
        when(client.fetchWorks(1)).thenReturn(new OpenAlexResponse(
                new OpenAlexMeta(1, 1, 100), List.of(work)));
        when(repository.existsByDoi("10.1234/example")).thenReturn(false);
        when(repository.existsByTitleAndYear("OpenAlex title", 2024)).thenReturn(false);

        PublicationService.ImportResult result = service.importFromOpenAlex();

        assertEquals(1, result.imported());
        assertEquals(0, result.skipped());
        verify(repository).save(any(Publication.class));
    }

    @Test
    void skipsDuplicateAndTitlelessWorksWithoutFailingImport() {
        OpenAlexWork duplicate = new OpenAlexWork(
                "Already imported", null, 2023, "10.1234/existing", null, null);
        OpenAlexWork missingTitle = new OpenAlexWork(
                null, null, null, null, null, null);
        when(client.fetchWorks(1)).thenReturn(new OpenAlexResponse(
                new OpenAlexMeta(2, 1, 100), List.of(duplicate, missingTitle)));
        when(repository.existsByDoi("10.1234/existing")).thenReturn(true);

        PublicationService.ImportResult result = service.importFromOpenAlex();

        assertEquals(0, result.imported());
        assertEquals(2, result.skipped());
        verify(repository, never()).save(any(Publication.class));
    }
}
