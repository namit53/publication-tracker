package com.university.tracker;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 1000)
    private String title;
    @Column(length = 4000)
    private String authors;
    @Column(length = 500)
    private String department;
    @Column(name = "pub_year")
    private int year;
    @Column(length = 500)
    private String doi;
    @Column(length = 500)
    private String journal;
    private int citations;

    public Publication() {}

    public Publication(String title, String authors, String department,
                       int year, String doi, String journal, int citations) {
        this.title = title;
        this.authors = authors;
        this.department = department;
        this.year = year;
        this.doi = doi;
        this.journal = journal;
        this.citations = citations;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }
    public String getJournal() { return journal; }
    public void setJournal(String journal) { this.journal = journal; }
    public int getCitations() { return citations; }
    public void setCitations(int citations) { this.citations = citations; }
}
