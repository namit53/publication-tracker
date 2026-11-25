package com.university.tracker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRepository extends JpaRepository<Publication, Long> {

    boolean existsByDoi(String doi);

    boolean existsByTitleAndYear(String title, int year);
}
