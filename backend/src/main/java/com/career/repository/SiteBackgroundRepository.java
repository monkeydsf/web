package com.career.repository;

import com.career.model.SiteBackground;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteBackgroundRepository extends JpaRepository<SiteBackground, String> {
    List<SiteBackground> findAllByOrderByPageKeyAsc();
}
