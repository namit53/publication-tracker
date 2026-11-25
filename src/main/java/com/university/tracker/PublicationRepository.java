package com.university.tracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface PublicationRepository extends JpaRepository<Publication, Long>, JpaSpecificationExecutor<Publication> {

    boolean existsByDoi(String doi);

    boolean existsByTitleAndYear(String title, int year);

    Optional<Publication> findByDoi(String doi);

    Optional<Publication> findByTitleAndYear(String title, int year);

    long countByDepartment(String department);

    long countByDepartmentIsNull();

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update Publication p set p.department = :department where p.id = :id")
    int assignDepartment(long id, String department);
}
