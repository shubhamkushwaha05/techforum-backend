package com.techforum.repository;
import com.techforum.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    Boolean existsByName(String name);
    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
