
package com.ecotrace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "organizations")
public class Organization {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @NotBlank
 @Size(max = 150)
 @Column(nullable = false, length = 150)
 private String name;

 @NotBlank
 @Size(max = 40)
 @Column(nullable = false, length = 40)
 private String organizationType;

 @NotBlank
 @Size(max = 80)
 @Column(nullable = false, length = 80)
 private String country;

 @Size(max = 1000)
 @Column(length = 1000)
 private String boundaryDescription;

 public Long getId() {
  return id;
 }

 public void setId(Long id) {
  this.id = id;
 }

 public String getName() {
  return name;
 }

 public void setName(String name) {
  this.name = name;
 }

 public String getOrganizationType() {
  return organizationType;
 }

 public void setOrganizationType(String organizationType) {
  this.organizationType = organizationType;
 }

 public String getCountry() {
  return country;
 }

 public void setCountry(String country) {
  this.country = country;
 }

 public String getBoundaryDescription() {
  return boundaryDescription;
 }

 public void setBoundaryDescription(String boundaryDescription) {
  this.boundaryDescription = boundaryDescription;
 }
}
