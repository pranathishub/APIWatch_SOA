package com.apiwatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "alerts", indexes = @Index(columnList = "api_id,resolved"))
public class Alert {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "api_id")
  @com.fasterxml.jackson.annotation.JsonIgnore
  public ApiDetails api;

  public String apiName;
  public String alertType = "DOWN";

  @Column(length = 2000)
  public String message;

  public boolean resolved = false;
  public java.time.Instant createdAt = java.time.Instant.now();
  public java.time.Instant resolvedAt;

  public Long getApiId() {
    return api.id;
  }
}
