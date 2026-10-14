
package com.ecotrace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "carbon_activities")
public class CarbonActivity {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @NotBlank
 @Size(max = 180)
 @Column(nullable = false, length = 180)
 private String activityName;

 @NotBlank
 @Size(max = 20)
 @Column(nullable = false, length = 20)
 private String scope;

 @NotBlank
 @Size(max = 100)
 @Column(nullable = false, length = 100)
 private String category;

 @NotNull
 @DecimalMin(value = "0.0", inclusive = false)
 @Column(nullable = false, precision = 18, scale = 6)
 private BigDecimal quantity;

 @NotBlank
 @Size(max = 40)
 @Column(nullable = false, length = 40)
 private String unit;

 @NotNull
 @Column(nullable = false)
 private Long factorId;

 @NotNull
 @Column(nullable = false)
 private Long organizationId;

 @Size(max = 120)
 private String productBatch;

 @Size(max = 120)
 private String stage;

 @Size(max = 120)
 private String transportLegId;

 @Size(max = 40)
 private String allocationBasis;

 @DecimalMin("0.0")
 @DecimalMax("100.0")
 @Column(precision = 7, scale = 3)
 private BigDecimal allocationPercentage;

 @DecimalMin(value = "0.0", inclusive = false)
 @Column(precision = 18, scale = 6)
 private BigDecimal massTonnes;

 @DecimalMin(value = "0.0", inclusive = false)
 @Column(precision = 18, scale = 6)
 private BigDecimal distanceKm;

 @NotBlank
 @Size(max = 30)
 @Column(nullable = false, length = 30)
 private String dataQuality = "ESTIMATED";

 @NotNull
 @Column(nullable = false)
 private LocalDate activityDate = LocalDate.now();

 @Column(precision = 18, scale = 6)
 private BigDecimal emissionsKgCo2e;

 @Column(columnDefinition = "TEXT")
 private String factorSnapshot;

 public Long getId() { return id; }
 public void setId(Long id) { this.id = id; }

 public String getActivityName() { return activityName; }
 public void setActivityName(String activityName) { this.activityName = activityName; }

 public String getScope() { return scope; }
 public void setScope(String scope) { this.scope = scope; }

 public String getCategory() { return category; }
 public void setCategory(String category) { this.category = category; }

 public BigDecimal getQuantity() { return quantity; }
 public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

 public String getUnit() { return unit; }
 public void setUnit(String unit) { this.unit = unit; }

 public Long getFactorId() { return factorId; }
 public void setFactorId(Long factorId) { this.factorId = factorId; }

 public Long getOrganizationId() { return organizationId; }
 public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

 public String getProductBatch() { return productBatch; }
 public void setProductBatch(String productBatch) { this.productBatch = productBatch; }

 public String getStage() { return stage; }
 public void setStage(String stage) { this.stage = stage; }

 public String getTransportLegId() { return transportLegId; }
 public void setTransportLegId(String transportLegId) { this.transportLegId = transportLegId; }

 public String getAllocationBasis() { return allocationBasis; }
 public void setAllocationBasis(String allocationBasis) { this.allocationBasis = allocationBasis; }

 public BigDecimal getAllocationPercentage() { return allocationPercentage; }
 public void setAllocationPercentage(BigDecimal allocationPercentage) {
  this.allocationPercentage = allocationPercentage;
 }

 public BigDecimal getMassTonnes() { return massTonnes; }
 public void setMassTonnes(BigDecimal massTonnes) { this.massTonnes = massTonnes; }

 public BigDecimal getDistanceKm() { return distanceKm; }
 public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }

 public String getDataQuality() { return dataQuality; }
 public void setDataQuality(String dataQuality) { this.dataQuality = dataQuality; }

 public LocalDate getActivityDate() { return activityDate; }
 public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }

 public BigDecimal getEmissionsKgCo2e() { return emissionsKgCo2e; }
 public void setEmissionsKgCo2e(BigDecimal emissionsKgCo2e) {
  this.emissionsKgCo2e = emissionsKgCo2e;
 }

 public String getFactorSnapshot() { return factorSnapshot; }
 public void setFactorSnapshot(String factorSnapshot) {
  this.factorSnapshot = factorSnapshot;
 }
}
