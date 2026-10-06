package com.apiwatch.repository;

import com.apiwatch.entity.MonitoringLog;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface LogRepository
  extends
    JpaRepository<MonitoringLog, Long>,
    JpaSpecificationExecutor<MonitoringLog>
{
  void deleteByApi_Id(Long id);

  @Query(
    "select count(l), coalesce(sum(case when l.status='UP' then 1 else 0 end),0), coalesce(avg(l.responseTime),0), coalesce(min(l.responseTime),0), coalesce(max(l.responseTime),0) from MonitoringLog l where l.api.owner.id=:owner and (:apiId is null or l.api.id=:apiId)"
  )
  List<Object[]> aggregate(Long owner, Long apiId);

  List<MonitoringLog> findTop60ByApi_Owner_IdOrderByCheckedAtDesc(Long ownerId);
  List<MonitoringLog> findTop60ByApi_IdAndApi_Owner_IdOrderByCheckedAtDesc(
    Long apiId,
    Long ownerId
  );
}
