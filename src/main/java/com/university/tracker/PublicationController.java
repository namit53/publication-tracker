package com.university.tracker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicationController {

    private final PublicationService service;

    public PublicationController(PublicationService service) {
        this.service = service;
    }

    @GetMapping("/publications")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String department,
                       @RequestParam(required = false) Integer year,
                       Model model) {
        model.addAttribute("publications", service.search(q, department, year));
        model.addAttribute("q", q);
        model.addAttribute("department", department);
        model.addAttribute("year", year);
        return "publications";
    }
}