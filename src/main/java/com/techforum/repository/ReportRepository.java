package com.techforum.repository;
import com.techforum.entity.Report;
import com.techforum.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Long countByStatus(ReportStatus status);
}
