package com.techforum.repository;
import com.techforum.entity.ModerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ModerationLogRepository extends JpaRepository<ModerationLog, Long> {
    Page<ModerationLog> findByModeratorIdOrderByCreatedAtDesc(Long moderatorId, Pageable pageable);
    Page<ModerationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
