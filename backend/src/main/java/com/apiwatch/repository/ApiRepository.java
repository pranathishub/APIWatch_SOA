package com.apiwatch.repository;

import com.apiwatch.entity.ApiDetails;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ApiRepository extends JpaRepository<ApiDetails, Long> {
  List<ApiDetails> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
  Optional<ApiDetails> findByIdAndOwnerId(Long id, Long ownerId);
  List<ApiDetails> findTop50ByActiveTrueAndNextCheckAtLessThanEqualOrderByNextCheckAtAsc(
    Instant now
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from ApiDetails a where a.id=:id")
  Optional<ApiDetails> lockById(@Param("id") Long id);
}
