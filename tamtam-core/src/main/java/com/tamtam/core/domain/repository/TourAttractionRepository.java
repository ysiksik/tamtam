package com.tamtam.core.domain.repository;

import com.tamtam.core.domain.entity.TourAttraction;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourAttractionRepository extends JpaRepository<TourAttraction, Long> {

    Optional<TourAttraction> findByContentId(String contentId);

    List<TourAttraction> findAllByContentIdIn(Collection<String> contentIds);
}
