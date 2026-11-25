package com.university.tracker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.IntStream;

@Controller
public class PublicationController {

    private final PublicationService service;
    private final DepartmentRepository departmentRepository;

    public PublicationController(PublicationService service) {
        this(service, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public PublicationController(PublicationService service, DepartmentRepository departmentRepository) {
        this.service = service;
        this.departmentRepository = departmentRepository;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(jakarta.servlet.http.HttpServletRequest request) {
        return request.isUserInRole("ADMIN") && request.getRequestURI().startsWith(request.getContextPath() + "/admin");
    }

    @GetMapping("/admin/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/publications/{id}/edit", "/admin/{id}/edit"})
    public String edit(@PathVariable long id, Model model, HttpServletResponse response) {
        Publication paper = service.findById(id);
        if (paper == null) {
            response.setStatus(404);
            return "publication-404";
        }
        var form = new PublicationForm();
        form.setTitle(paper.getTitle());
        form.setAuthors(paper.getAuthors());
        form.setDepartment(paper.getDepartment());
        form.setDoi(paper.getDoi());
        form.setYear(paper.getYear());
        form.setJournal(paper.getJournal());
        form.setCitations(paper.getCitations());
        model.addAttribute("publicationForm", form);
        model.addAttribute("editId", id);
        model.addAttribute("departments", service.departmentOptions());
        return "publication-form";
    }

    @PostMapping({"/publications/{id}/edit", "/admin/{id}/edit"})
    public String saveEdit(@PathVariable long id,
                           @Valid @ModelAttribute("publicationForm") PublicationForm form,
                           BindingResult errors, Model model, HttpServletResponse response,
                           RedirectAttributes redirect) {
        Publication paper = service.findById(id);
        if (paper == null) {
            response.setStatus(404);
            return "publication-404";
        }
        if (form.getDepartment() != null && !form.getDepartment().isBlank()
                && departmentRepository.findByName(form.getDepartment()).isEmpty()) {
            errors.reject("department", "Select an existing department.");
        }
        if (!errors.hasErrors()) {
            try {
                service.updatePublication(paper, form);
                redirect.addFlashAttribute("successMessage", "Publication updated successfully.");
                return "redirect:/admin";
            } catch (DuplicatePublicationException exception) {
                errors.reject("duplicate", exception.getMessage());
            }
        }
        model.addAttribute("editId", id);
        model.addAttribute("departments", service.departmentOptions());
        return "publication-form";
    }

    @GetMapping({"/publications", "/admin"})
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String department,
                       @RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Integer yearFrom,
                       @RequestParam(required = false) Integer yearTo,
                       @RequestParam(defaultValue = "newest") String sort,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        String dashboard = Boolean.TRUE.equals(model.getAttribute("isAdmin")) ? "/admin" : "/publications";
        model.addAttribute("dashboard", dashboard);
        if (year != null && yearFrom == null && yearTo == null) {
            yearFrom = year;
            yearTo = year;
        }
        var resultPage = service.search(q, department, yearFrom, yearTo, sort, page);
        int currentPage = resultPage.getNumber() + 1;
        model.addAttribute("publications", resultPage.getContent());
        model.addAttribute("totalResults", resultPage.getTotalElements());
        model.addAttribute("resultStart", resultPage.hasContent() ? resultPage.getNumber() * resultPage.getSize() + 1 : 0);
        model.addAttribute("resultEnd", resultPage.hasContent() ? resultPage.getNumber() * resultPage.getSize() + resultPage.getNumberOfElements() : 0);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("pageNumbers", resultPage.getTotalPages() == 0 ? java.util.List.of() : IntStream.rangeClosed(1, resultPage.getTotalPages()).boxed().toList());
        model.addAttribute("q", q);
        model.addAttribute("department", department);
        model.addAttribute("year", year);
        model.addAttribute("yearFrom", yearFrom);
        model.addAttribute("yearTo", yearTo);
        model.addAttribute("sort", sort);
        model.addAttribute("departments", service.departmentOptions());
        model.addAttribute("hasPrevious", resultPage.hasPrevious());
        model.addAttribute("hasNext", resultPage.hasNext());
        model.addAttribute("backUrl", resultsUrl(dashboard, q, department, yearFrom, yearTo, sort, currentPage));
        model.addAttribute("statistics", service.statistics());
        return "publications";
    }

    @GetMapping({"/publications/new", "/admin/new"})
    public String newPublication(Model model) {
        model.addAttribute("publicationForm", new PublicationForm());
        model.addAttribute("departments", service.departmentOptions());
        return "publication-form";
    }

    @PostMapping({"/publications/new", "/admin/new"})
    public String createPublication(@Valid @ModelAttribute("publicationForm") PublicationForm form,
                                    BindingResult bindingResult, Model model,
                                    RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                service.addManualPublication(form);
                redirectAttributes.addFlashAttribute("successMessage", "Publication added successfully.");
                return "redirect:/admin";
            } catch (DuplicatePublicationException exception) {
                bindingResult.reject("duplicate", exception.getMessage());
            }
        }
        model.addAttribute("departments", service.departmentOptions());
        return "publication-form";
    }

    @GetMapping({"/publications/{id}", "/admin/{id}"})
    public String detail(@PathVariable long id,
                         @RequestParam(required = false) String back,
                         Model model, HttpServletResponse response) {
        Publication publication = service.findById(id);
        if (publication == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return "publication-404";
        }
        model.addAttribute("publication", publication);
        model.addAttribute("backUrl", back == null || back.isBlank() ? (Boolean.TRUE.equals(model.getAttribute("isAdmin")) ? "/admin" : "/publications") : back);
        return "publication-detail";
    }

    private String resultsUrl(String dashboard, String q, String department, Integer yearFrom, Integer yearTo,
                              String sort, int page) {
        StringBuilder url = new StringBuilder(dashboard).append('?');
        append(url, "q", q);
        append(url, "department", department);
        append(url, "yearFrom", yearFrom);
        append(url, "yearTo", yearTo);
        append(url, "sort", sort);
        append(url, "page", page);
        return url.substring(0, url.length() - 1);
    }

    private void append(StringBuilder url, String name, Object value) {
        if (value != null && !value.toString().isBlank()) {
            url.append(name).append('=').append(java.net.URLEncoder.encode(value.toString(), java.nio.charset.StandardCharsets.UTF_8)).append('&');
        }
    }

    @PostMapping({"/publications/import", "/admin/import"})
    @ResponseBody
    public ResponseEntity<?> importFromOpenAlex() {
        try {
            return ResponseEntity.ok(service.importFromOpenAlex());
        } catch (OpenAlexImportException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ImportError(exception.getMessage()));
        } catch (RuntimeException exception) {
            return ResponseEntity.internalServerError()
                    .body(new ImportError("Import failed while saving publications. Please try again."));
        }
    }

    private record ImportError(String error) {
    }
}
