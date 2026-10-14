
package com.ecotrace.service;

import com.ecotrace.entity.CarbonActivity;
import com.ecotrace.entity.EmissionFactor;
import com.ecotrace.repository.CarbonActivityRepository;
import com.ecotrace.repository.EmissionFactorRepository;
import com.ecotrace.repository.OrganizationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class CarbonService {

 private static final BigDecimal ONE_HUNDRED =
         new BigDecimal("100");

 private final CarbonActivityRepository activities;
 private final EmissionFactorRepository factors;
 private final OrganizationRepository organizations;
 private final ObjectMapper objectMapper;

 public CarbonService(
         CarbonActivityRepository activities,
         EmissionFactorRepository factors,
         OrganizationRepository organizations,
         ObjectMapper objectMapper) {
  this.activities = activities;
  this.factors = factors;
  this.organizations = organizations;
  this.objectMapper = objectMapper;
 }

 @Transactional
 public CarbonActivity createActivity(CarbonActivity input) {
  if (input == null) {
   throw new IllegalArgumentException(
           "Activity request is required.");
  }

  input.setId(null);
  input.setEmissionsKgCo2e(null);
  input.setFactorSnapshot(null);

  if (input.getActivityName() == null
          || input.getActivityName().isBlank()) {
   throw new IllegalArgumentException(
           "Activity name is required.");
  }

  if (input.getQuantity() == null
          || input.getQuantity().signum() <= 0) {
   throw new IllegalArgumentException(
           "Quantity must be greater than zero.");
  }

  if (input.getFactorId() == null) {
   throw new IllegalArgumentException(
           "factorId is required.");
  }

  if (input.getOrganizationId() == null) {
   throw new IllegalArgumentException(
           "organizationId is required.");
  }

  if (input.getUnit() == null
          || input.getUnit().isBlank()) {
   throw new IllegalArgumentException(
           "Activity unit is required.");
  }

  String scope = normalizeScope(input.getScope());
  String category = normalizeCategory(input.getCategory());

  EmissionFactor factor = factors.findById(input.getFactorId())
          .orElseThrow(() ->
                  new NoSuchElementException(
                          "Emission factor not found."));

  organizations.findById(input.getOrganizationId())
          .orElseThrow(() ->
                  new NoSuchElementException(
                          "Organization not found."));

  if (Boolean.TRUE.equals(factor.getIllustrative())) {
   throw new IllegalArgumentException(
           "Illustrative factors cannot be used for "
                   + "verified calculations.");
  }

  if (factor.getKgCo2ePerUnit() == null
          || factor.getKgCo2ePerUnit().signum() <= 0) {
   throw new IllegalArgumentException(
           "Emission factor must be greater than zero.");
  }

  if (factor.getActivityUnit() == null
          || factor.getActivityUnit().isBlank()) {
   throw new IllegalArgumentException(
           "Emission factor unit is missing.");
  }

  if (factor.getSourceName() == null
          || factor.getSourceName().isBlank()
          || factor.getDatasetVersion() == null
          || factor.getDatasetVersion().isBlank()
          || factor.getGeography() == null
          || factor.getGeography().isBlank()
          || factor.getBoundaryDescription() == null
          || factor.getBoundaryDescription().isBlank()
          || factor.getAccessedDate() == null) {
   throw new IllegalArgumentException(
           "Emission factor provenance is incomplete.");
  }

  String factorUnit = normalizeUnit(factor.getActivityUnit());
  String activityUnit = normalizeUnit(input.getUnit());

  BigDecimal emissions;

  if (isFreightCategory(category)) {
   if (!"TONNE-KM".equals(factorUnit)) {
    throw new IllegalArgumentException(
            "Freight requires a factor measured per tonne-km.");
   }

   if (input.getMassTonnes() == null
           || input.getMassTonnes().signum() <= 0
           || input.getDistanceKm() == null
           || input.getDistanceKm().signum() <= 0) {
    throw new IllegalArgumentException(
            "Freight requires positive massTonnes and distanceKm.");
   }

   BigDecimal tonneKm = input.getMassTonnes()
           .multiply(input.getDistanceKm());

   // quantity is not used for freight to avoid double counting.
   emissions = tonneKm.multiply(factor.getKgCo2ePerUnit());

   input.setQuantity(tonneKm);
   input.setUnit("TONNE-KM");
  } else {
   BigDecimal normalizedQuantity =
           convertQuantity(input.getQuantity(), activityUnit, factorUnit);

   emissions = normalizedQuantity
           .multiply(factor.getKgCo2ePerUnit());

   input.setQuantity(normalizedQuantity);
   input.setUnit(factor.getActivityUnit());
  }

  BigDecimal allocation = input.getAllocationPercentage();

  if (allocation != null) {
   if (allocation.signum() <= 0
           || allocation.compareTo(ONE_HUNDRED) > 0) {
    throw new IllegalArgumentException(
            "Allocation percentage must be greater than 0 "
                    + "and no more than 100.");
   }

   if (input.getAllocationBasis() == null
           || input.getAllocationBasis().isBlank()) {
    throw new IllegalArgumentException(
            "Allocation basis is required when allocation is used.");
   }

   emissions = emissions.multiply(allocation)
           .divide(ONE_HUNDRED, 12, RoundingMode.HALF_UP);
  }

  if (input.getTransportLegId() != null
          && !input.getTransportLegId().isBlank()) {
   if (input.getProductBatch() == null
           || input.getProductBatch().isBlank()) {
    throw new IllegalArgumentException(
            "Product batch is required for a transport leg.");
   }

   boolean duplicate =
           activities.existsByOrganizationIdAndTransportLegIdAndProductBatch(
                   input.getOrganizationId(),
                   input.getTransportLegId(),
                   input.getProductBatch());

   if (duplicate) {
    throw new IllegalArgumentException(
            "This transport leg and product batch "
                    + "already have an activity.");
   }
  }

  input.setScope(scope);
  input.setCategory(category);
  input.setEmissionsKgCo2e(
          emissions.setScale(6, RoundingMode.HALF_UP));

  input.setFactorSnapshot(createSnapshot(factor));

  if (input.getDataQuality() == null
          || input.getDataQuality().isBlank()) {
   input.setDataQuality("ESTIMATED");
  }

  if (input.getActivityDate() == null) {
   throw new IllegalArgumentException(
           "Activity date is required.");
  }

  return activities.save(input);
 }

 @Transactional(readOnly = true)
 public List<CarbonActivity> getActivities(Long organizationId) {
  if (organizationId == null) {
   throw new IllegalArgumentException(
           "organizationId is required.");
  }

  organizations.findById(organizationId)
          .orElseThrow(() ->
                  new NoSuchElementException(
                          "Organization not found."));

  return activities.findByOrganizationId(organizationId);
 }

 @Transactional(readOnly = true)
 public Map<String, Object> summary(Long organizationId) {
  if (organizationId == null) {
   throw new IllegalArgumentException(
           "organizationId is required.");
  }

  organizations.findById(organizationId)
          .orElseThrow(() ->
                  new NoSuchElementException(
                          "Organization not found."));

  List<CarbonActivity> records =
          activities.findByOrganizationId(organizationId);

  BigDecimal total = BigDecimal.ZERO;
  Map<String, BigDecimal> byScope = new LinkedHashMap<>();
  Map<String, BigDecimal> byCategory = new LinkedHashMap<>();

  for (CarbonActivity activity : records) {
   BigDecimal value = activity.getEmissionsKgCo2e() == null
           ? BigDecimal.ZERO
           : activity.getEmissionsKgCo2e();

   total = total.add(value);

   byScope.merge(
           activity.getScope() == null
                   ? "UNKNOWN" : activity.getScope(),
           value,
           BigDecimal::add);

   byCategory.merge(
           activity.getCategory() == null
                   ? "UNKNOWN" : activity.getCategory(),
           value,
           BigDecimal::add);
  }

  Map<String, Object> result = new LinkedHashMap<>();
  result.put("organizationId", organizationId);
  result.put("activityCount", records.size());
  result.put("totalKgCo2e",
          total.setScale(6, RoundingMode.HALF_UP));
  result.put("totalTonnesCo2e",
          total.divide(new BigDecimal("1000"),
                  6, RoundingMode.HALF_UP));
  result.put("byScopeKgCo2e", byScope);
  result.put("byCategoryKgCo2e", byCategory);
  result.put("note",
          "Organization inventory summary. Do not add "
                  + "different stakeholders' inventories together "
                  + "to calculate a product footprint.");

  return result;
 }

 private BigDecimal convertQuantity(
         BigDecimal quantity,
         String fromUnit,
         String toUnit) {

  if (fromUnit.equals(toUnit)) {
   return quantity;
  }

  // Only explicitly supported conversions are allowed.
  if ("ML".equals(fromUnit) && "L".equals(toUnit)) {
   return quantity.divide(
           new BigDecimal("1000"), 12, RoundingMode.HALF_UP);
  }

  if ("L".equals(fromUnit) && "ML".equals(toUnit)) {
   return quantity.multiply(new BigDecimal("1000"));
  }

  if ("G".equals(fromUnit) && "KG".equals(toUnit)) {
   return quantity.divide(
           new BigDecimal("1000"), 12, RoundingMode.HALF_UP);
  }

  if ("KG".equals(fromUnit) && "G".equals(toUnit)) {
   return quantity.multiply(new BigDecimal("1000"));
  }

  if ("M".equals(fromUnit) && "KM".equals(toUnit)) {
   return quantity.divide(
           new BigDecimal("1000"), 12, RoundingMode.HALF_UP);
  }

  if ("KM".equals(fromUnit) && "M".equals(toUnit)) {
   return quantity.multiply(new BigDecimal("1000"));
  }

  throw new IllegalArgumentException(
          "Unsupported unit conversion from " + fromUnit
                  + " to " + toUnit + ".");
 }

 private String normalizeScope(String scope) {
  if (scope == null || scope.isBlank()) {
   throw new IllegalArgumentException("Scope is required.");
  }

  String normalized = scope.trim()
          .toUpperCase(Locale.ROOT)
          .replace(' ', '_');

  if (!List.of("SCOPE_1", "SCOPE_2", "SCOPE_3")
          .contains(normalized)) {
   throw new IllegalArgumentException(
           "Scope must be SCOPE_1, SCOPE_2 or SCOPE_3.");
  }

  return normalized;
 }

 private String normalizeCategory(String category) {
  if (category == null || category.isBlank()) {
   throw new IllegalArgumentException(
           "Category is required.");
  }

  return category.trim()
          .toUpperCase(Locale.ROOT)
          .replace(' ', '_')
          .replace('-', '_');
 }

 private String normalizeUnit(String unit) {
  return unit.trim()
          .toUpperCase(Locale.ROOT)
          .replace(" ", "")
          .replace("_", "-");
 }

 private boolean isFreightCategory(String category) {
  String value = category.toUpperCase(Locale.ROOT);

  return value.contains("FREIGHT")
          || value.contains("TRANSPORT")
          || value.contains("UPSTREAM_TRANSPORTATION")
          || value.contains("DOWNSTREAM_TRANSPORTATION");
 }

 private String createSnapshot(EmissionFactor factor) {
  Map<String, Object> snapshot = new LinkedHashMap<>();

  snapshot.put("factorId", factor.getId());
  snapshot.put("name", factor.getName());
  snapshot.put("value", factor.getKgCo2ePerUnit());
  snapshot.put("unit", factor.getActivityUnit());
  snapshot.put("sourceName", factor.getSourceName());
  snapshot.put("sourceUrl", factor.getSourceUrl());
  snapshot.put("datasetVersion", factor.getDatasetVersion());
  snapshot.put("referenceYear", factor.getReferenceYear());
  snapshot.put("geography", factor.getGeography());
  snapshot.put("boundaryDescription",
          factor.getBoundaryDescription());
  snapshot.put("accessedDate", factor.getAccessedDate());
  snapshot.put("illustrative", factor.getIllustrative());

  try {
   return objectMapper.writeValueAsString(snapshot);
  } catch (JsonProcessingException exception) {
   throw new IllegalStateException(
           "Could not create emission factor snapshot.", exception);
  }
 }
}
