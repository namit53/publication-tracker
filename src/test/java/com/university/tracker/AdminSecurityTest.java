package com.university.tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"ADMIN_USERNAME=test-editor", "ADMIN_PASSWORD=test-only-password",
        "spring.datasource.url=jdbc:h2:mem:security-tests"})
class AdminSecurityTest {
    @Autowired WebApplicationContext context;
    @Autowired PublicationRepository publications;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void anonymousBrowsingAndRestrictedActions() throws Exception {
        long id = publications.findAll().getFirst().getId();
        mvc.perform(get("/publications")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Admin login"))))
                .andExpect(content().string(not(containsString("href=\"/publications/new\""))));
        mvc.perform(get("/publications/" + id)).andExpect(status().isOk());
        mvc.perform(get("/admin")).andExpect(redirectedUrl("/admin/login"));
        mvc.perform(get("/publications/new")).andExpect(redirectedUrl("/admin/login"));
        mvc.perform(post("/publications/import")).andExpect(status().isForbidden());
        mvc.perform(post("/publications/import").with(csrf())).andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void wrongPasswordFails() throws Exception {
        mvc.perform(formLogin("/admin/login").user("test-editor").password("incorrect"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/admin/login?error"));
        mvc.perform(get("/admin/login?error")).andExpect(content().string(containsString("Incorrect username or password")));
    }

    @Test
    void adminCreatesDepartmentAndAssignsPreviouslyUnassignedPaper() throws Exception {
        var paper = publications.save(new Publication("Imported paper", "", null, 0, null, null, 4));
        String detail = "/admin/" + paper.getId();
        String assign = detail + "/department";
        String name = "Atmospheric Science";
        mvc.perform(post("/admin/departments").with(csrf()).param("name", name))
                .andExpect(redirectedUrl("/admin/login"));
        mvc.perform(post(assign).with(csrf()).param("department", name))
                .andExpect(redirectedUrl("/admin/login"));
        var result = mvc.perform(formLogin("/admin/login").user("test-editor").password("test-only-password"))
                .andExpect(authenticated()).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        mvc.perform(post("/admin/departments").session(session).with(csrf()).param("name", name))
                .andExpect(redirectedUrl("/admin"));
        mvc.perform(get(detail).session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString(name)));
        mvc.perform(post(assign).session(session).with(csrf()).param("department", name))
                .andExpect(redirectedUrl(detail));
        var saved = publications.findById(paper.getId()).orElseThrow();
        assertEquals(name, saved.getDepartment());
        assertEquals(0, saved.getYear());
        mvc.perform(post(assign).session(session).with(csrf()).param("department", ""))
                .andExpect(redirectedUrl(detail));
        org.junit.jupiter.api.Assertions.assertNull(publications.findById(paper.getId()).orElseThrow().getDepartment());
    }

    @Test
    void authenticatedAdminCanChangeDepartment() throws Exception {
        var result = mvc.perform(formLogin("/admin/login").user("test-editor").password("test-only-password"))
                .andExpect(redirectedUrl("/admin")).andExpect(authenticated().withRoles("ADMIN")).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        var paper = publications.save(new Publication("Editable", "Researcher", "Physics",
                2024, null, null, 0));
        mvc.perform(get("/admin").param("q", "Editable").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/admin/new\"")))
                .andExpect(content().string(containsString("/admin/" + paper.getId() + "?back=")));
        String edit = "/admin/" + paper.getId() + "/edit";
        mvc.perform(get("/admin/new").session(session)).andExpect(status().isOk());
        mvc.perform(get(edit).session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Chemistry")));
        mvc.perform(post(edit).session(session)).andExpect(status().isForbidden());
        mvc.perform(post(edit).session(session).with(csrf()).param("title", "Edited")
                        .param("authors", "Researcher").param("year", "2024").param("department", "Chemistry"))
                .andExpect(redirectedUrl("/admin"));
        var saved = publications.findById(paper.getId()).orElseThrow();
        assertEquals("Edited", saved.getTitle());
        assertEquals("Chemistry", saved.getDepartment());
    }
}
