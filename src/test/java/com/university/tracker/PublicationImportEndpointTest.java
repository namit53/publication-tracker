package com.university.tracker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PublicationImportEndpointTest {

    private final PublicationService service = mock(PublicationService.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new PublicationController(service))
            .build();

    @Test
    void importEndpointReturnsJsonOnSuccess() throws Exception {
        when(service.importFromOpenAlex()).thenReturn(new PublicationService.ImportResult(3, 1));

        mvc.perform(post("/publications/import"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().json("{\"imported\":3,\"skipped\":1}"));
    }

    @Test
    void importEndpointReturnsMeaningfulJsonOnOpenAlexFailure() throws Exception {
        when(service.importFromOpenAlex()).thenThrow(
                new OpenAlexImportException("OpenAlex returned HTTP 429: rate limit exceeded"));

        mvc.perform(post("/publications/import"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().json(
                        "{\"error\":\"OpenAlex returned HTTP 429: rate limit exceeded\"}"));
    }
}
