package com.university.tracker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @PostMapping("/publications/import")
    @ResponseBody
    public ResponseEntity<?> importFromOpenAlex() {
        try {
            return ResponseEntity.ok(service.importFromOpenAlex());
        } catch (RuntimeException exception) {
            return ResponseEntity.internalServerError()
                    .body(new ImportError("OpenAlex import failed: " + exception.getMessage()));
        }
    }

    private record ImportError(String error) {
    }
}
