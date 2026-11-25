package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HomeControllerTest {

    @Test
    void homePageRedirectsToDashboard() {
        assertEquals("redirect:/publications", new HomeController().home());
    }
}
