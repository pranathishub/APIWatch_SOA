package com.apiwatch.entity;

import jakarta.persistence.*;

@Entity
@Table(
  name = "monitoring_logs",
  indexes = {
    @Index(columnList = "api_id,checkedAt"),
    @Index(columnList = "checkedAt"),
  }
)
public class MonitoringLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "api_id")
  @com.fasterxml.jackson.annotation.JsonIgnore
  public ApiDetails api;

  public String apiName;

  @Column(length = 2048)
  public String url;

  public String httpMethod;
  public String status;
  public Integer statusCode;
  public long responseTime;

  @Column(length = 2000)
  public String errorMessage;

  public java.time.Instant checkedAt;

  public Long getApiId() {
    return api.id;
  }
}
