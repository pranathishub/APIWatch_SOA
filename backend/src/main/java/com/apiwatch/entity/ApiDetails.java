package com.apiwatch.entity;

import jakarta.persistence.*;

@Entity
@Table(
  name = "api_details",
  indexes = {
    @Index(columnList = "owner_id"),
    @Index(columnList = "active,nextCheckAt"),
  }
)
public class ApiDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "owner_id")
  @com.fasterxml.jackson.annotation.JsonIgnore
  public UserAccount owner;

  @Column(nullable = false, length = 120)
  public String name;

  @Column(nullable = false, length = 2048)
  public String url;

  @Column(nullable = false)
  public String httpMethod;

  @Column(length = 2000)
  public String description;

  public int expectedStatusCode;
  public int monitoringInterval;
  public int timeout;
  public boolean active;
  public java.time.Instant createdAt = java.time.Instant.now();
  public java.time.Instant updatedAt = java.time.Instant.now();
  public java.time.Instant nextCheckAt = java.time.Instant.now();
  public java.time.Instant lastChecked;
  public String lastStatus = "PENDING";
  public Integer lastStatusCode;
  public Long lastResponseTime;
}
