package com.apiwatch.repository;

import com.apiwatch.entity.Alert;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
  long countByApi_Owner_IdAndResolvedFalse(Long owner);
  Page<Alert> findByApi_Owner_IdOrderByCreatedAtDesc(
    Long owner,
    Pageable pageable
  );
  Optional<Alert> findByIdAndApi_Owner_Id(Long id, Long owner);
  List<Alert> findByApi_IdAndResolvedFalse(Long apiId);
  void deleteByApi_Id(Long id);
}
