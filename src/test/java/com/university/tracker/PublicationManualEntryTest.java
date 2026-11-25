package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class PublicationManualEntryTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @Autowired
    PublicationRepository repository;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void missingTitleIsRejected() throws Exception {
        mvc.perform(post("/publications/new")
                        .param("authors", "An Author")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(view().name("publication-form"))
                .andExpect(model().attributeHasFieldErrors("publicationForm", "title"));
    }

    @Test
    void duplicateDoiIsRejected() throws Exception {
        repository.save(new Publication("Existing manual DOI paper", "An Author", null,
                2024, "10.4000/manual-duplicate", "Journal", 0));

        mvc.perform(post("/publications/new")
                        .param("title", "Another manual DOI paper")
                        .param("authors", "Another Author")
                        .param("year", "2025")
                        .param("doi", "https://doi.org/10.4000/manual-duplicate"))
                .andExpect(status().isOk())
                .andExpect(view().name("publication-form"))
                .andExpect(model().attributeExists("publicationForm"));
    }

    @Test
    void validPaperIsSavedAndRedirects() throws Exception {
        mvc.perform(post("/publications/new")
                        .param("title", "Valid manual publication")
                        .param("authors", "Valid Author")
                        .param("year", "2025")
                        .param("doi", "10.4000/manual-valid")
                        .param("journal", "Research Journal")
                        .param("department", "Physics")
                        .param("citations", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        Publication saved = repository.findByDoi("10.4000/manual-valid").orElseThrow();
        assertEquals("Valid manual publication", saved.getTitle());
        assertEquals("Physics", saved.getDepartment());
        assertEquals(3, saved.getCitations());
    }
}
