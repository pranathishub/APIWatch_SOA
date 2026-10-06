package com.apiwatch.entity;

import jakarta.persistence.*;

@Entity
@Table(
  name = "users",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email"),
  }
)
public class UserAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 40)
  public String username;

  @Column(nullable = false, length = 120)
  public String email;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(nullable = false)
  public String password;

  public String role = "ADMIN";
  public java.time.Instant createdAt = java.time.Instant.now();
}
