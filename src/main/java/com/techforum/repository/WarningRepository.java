package com.techforum.repository;
import com.techforum.entity.Warning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface WarningRepository extends JpaRepository<Warning, Long> {
    List<Warning> findByUserIdOrderByCreatedAtDesc(Long userId);
    Long countByUserId(Long userId);
}
