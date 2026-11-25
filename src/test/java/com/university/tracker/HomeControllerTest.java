package com.university.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HomeControllerTest {

    @Test
    void homePageReturnsTitle() {
        assertEquals("Publication Tracker v2", new HomeController().home());
    }
}