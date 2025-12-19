package com.vivekanand.manager.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaticContentPageRepository extends JpaRepository<StaticContentPage, Long> {
    Optional<StaticContentPage> findBySlug(String slug);
}
