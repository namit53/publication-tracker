package com.university.tracker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicationController {

    private final PublicationRepository repo;

    public PublicationController(PublicationRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/publications")
    public String list(Model model) {
        model.addAttribute("publications", repo.findAll());
        return "publications";
    }
}