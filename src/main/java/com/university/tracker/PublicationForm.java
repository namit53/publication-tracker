package com.university.tracker;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Year;

public class PublicationForm {

    @NotBlank(message = "Title is required.")
    private String title;

    @NotBlank(message = "Authors are required.")
    private String authors;

    @NotNull(message = "Year is required.")
    @Min(value = 1900, message = "Year must be 1900 or later.")
    private Integer year;

    private String doi;
    private String journal;
    private String department;

    @Min(value = 0, message = "Citations cannot be negative.")
    private Integer citations = 0;

    @AssertTrue(message = "Year must not be later than next year.")
    public boolean isYearInRange() {
        return year == null || year <= Year.now().getValue() + 1;
    }

    public Publication toPublication(String normalizedDoi) {
        return new Publication(title.trim(), authors.trim(), blankToNull(department), year,
                blankToNull(normalizedDoi), blankToNull(journal), citations == null ? 0 : citations);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }
    public String getJournal() { return journal; }
    public void setJournal(String journal) { this.journal = journal; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getCitations() { return citations; }
    public void setCitations(Integer citations) { this.citations = citations; }
}
