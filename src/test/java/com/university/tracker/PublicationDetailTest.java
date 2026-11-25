package com.university.tracker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PublicationDetailTest {

    private final PublicationService service = mock(PublicationService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new PublicationController(service)).build();

    @Test
    void existingPublicationReturnsDetailPage() throws Exception {
        when(service.findById(7L)).thenReturn(new Publication("A detail paper", "An Author",
                "Research", 2025, "10.1000/detail", "Journal", 4));

        mvc.perform(get("/publications/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("publication-detail"));
    }

    @Test
    void missingPublicationReturnsFriendlyNotFoundPage() throws Exception {
        when(service.findById(404L)).thenReturn(null);

        mvc.perform(get("/publications/404"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("publication-404"));
    }
}
