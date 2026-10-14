
package com.ecotrace.controller;

import com.ecotrace.entity.CarbonActivity;
import com.ecotrace.entity.UserAccount;
import com.ecotrace.repository.CarbonActivityRepository;
import com.ecotrace.repository.EmissionFactorRepository;
import com.ecotrace.repository.OrganizationRepository;
import com.ecotrace.repository.UserAccountRepository;
import com.ecotrace.service.CarbonService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class ApiController {

 private final CarbonService carbonService;
 private final EmissionFactorRepository factorRepository;
 private final OrganizationRepository organizationRepository;
 private final UserAccountRepository userAccountRepository;
 private final CarbonActivityRepository activityRepository;

 public ApiController(
         CarbonService carbonService,
         EmissionFactorRepository factorRepository,
         OrganizationRepository organizationRepository,
         UserAccountRepository userAccountRepository,
         CarbonActivityRepository activityRepository) {
  this.carbonService = carbonService;
  this.factorRepository = factorRepository;
  this.organizationRepository = organizationRepository;
  this.userAccountRepository = userAccountRepository;
  this.activityRepository = activityRepository;
 }

 @GetMapping("/health")
 public Map<String, String> health() {
  return Map.of("status", "UP", "app", "EcoTrace");
 }

 @GetMapping("/categories")
 public List<Map<String, String>> categories() {
  return List.of(
          Map.of("scope", "SCOPE_1", "category", "Stationary Combustion"),
          Map.of("scope", "SCOPE_1", "category", "Mobile Combustion"),
          Map.of("scope", "SCOPE_1", "category", "Fugitive Emissions"),
          Map.of("scope", "SCOPE_1", "category", "Process Emissions"),
          Map.of("scope", "SCOPE_2", "category", "Purchased Electricity"),
          Map.of("scope", "SCOPE_2", "category", "Purchased Steam, Heating and Cooling"),
          Map.of("scope", "SCOPE_3", "category", "Purchased Goods and Services"),
          Map.of("scope", "SCOPE_3", "category", "Capital Goods"),
          Map.of("scope", "SCOPE_3", "category", "Fuel and Energy Related Activities"),
          Map.of("scope", "SCOPE_3", "category", "Upstream Transportation and Distribution"),
          Map.of("scope", "SCOPE_3", "category", "Waste Generated in Operations"),
          Map.of("scope", "SCOPE_3", "category", "Business Travel"),
          Map.of("scope", "SCOPE_3", "category", "Employee Commuting"),
          Map.of("scope", "SCOPE_3", "category", "Upstream Leased Assets"),
          Map.of("scope", "SCOPE_3", "category", "Downstream Transportation and Distribution"),
          Map.of("scope", "SCOPE_3", "category", "Processing of Sold Products"),
          Map.of("scope", "SCOPE_3", "category", "Use of Sold Products"),
          Map.of("scope", "SCOPE_3", "category", "End-of-Life Treatment of Sold Products"),
          Map.of("scope", "SCOPE_3", "category", "Downstream Leased Assets"),
          Map.of("scope", "SCOPE_3", "category", "Franchises"),
          Map.of("scope", "SCOPE_3", "category", "Investments")
  );
 }

 @GetMapping("/factors")
 @PreAuthorize("hasAnyRole('ADMIN', 'STAKEHOLDER', 'AUDITOR')")
 public List<?> getFactors() {
  return factorRepository.findAll();
 }

 @GetMapping("/organizations")
 @PreAuthorize("hasRole('ADMIN')")
 public List<?> getOrganizations() {
  return organizationRepository.findAll();
 }

 @GetMapping("/activities")
 @PreAuthorize("hasAnyRole('ADMIN', 'STAKEHOLDER', 'AUDITOR')")
 public List<CarbonActivity> getActivities(
         @RequestParam Long organizationId,
         Authentication authentication) {

  requireOrganizationAccess(organizationId, authentication);

  List<CarbonActivity> activities =
          carbonService.getActivities(organizationId);

  if (hasRole(authentication, "STAKEHOLDER")) {
   UserAccount account = getCurrentAccount(authentication);
   String assignedStage = normalize(account.getAssignedStage());

   if (assignedStage != null) {
    return activities.stream()
            .filter(activity -> assignedStage.equals(
                    normalize(activity.getStage())))
            .toList();
   }
  }

  return activities;
 }

 @PostMapping("/activities")
 @PreAuthorize("hasAnyRole('ADMIN', 'STAKEHOLDER')")
 public ResponseEntity<CarbonActivity> createActivity(
         @RequestBody CarbonActivity activity,
         Authentication authentication) {

  if (hasRole(authentication, "STAKEHOLDER")) {
   UserAccount account = getCurrentAccount(authentication);

   if (account.getOrganizationId() == null) {
    throw new AccessDeniedException(
            "Your account is not assigned to an organization.");
   }

   Long requestedOrganizationId = activity.getOrganizationId();
   Long actualOrganizationId = account.getOrganizationId();

   if (requestedOrganizationId != null
           && !Objects.equals(
           requestedOrganizationId,
           actualOrganizationId)) {
    throw new AccessDeniedException(
            "You cannot create activities for another organization.");
   }

   activity.setOrganizationId(actualOrganizationId);

   String assignedStage = normalize(account.getAssignedStage());

   if (assignedStage != null
           && !assignedStage.equals(normalize(activity.getStage()))) {
    throw new AccessDeniedException(
            "You can create activities only for your assigned stage.");
   }
  }

  CarbonActivity saved = carbonService.createActivity(activity);

  return ResponseEntity.status(HttpStatus.CREATED).body(saved);
 }

 @GetMapping("/reports/summary")
 @PreAuthorize("hasAnyRole('ADMIN', 'STAKEHOLDER', 'AUDITOR')")
 public Map<String, Object> getSummary(
         @RequestParam Long organizationId,
         Authentication authentication) {

  requireOrganizationAccess(organizationId, authentication);

  if (hasRole(authentication, "STAKEHOLDER")) {
   UserAccount account = getCurrentAccount(authentication);
   String assignedStage = normalize(account.getAssignedStage());

   if (assignedStage != null) {
    List<CarbonActivity> allowedActivities =
            carbonService.getActivities(organizationId).stream()
                    .filter(activity -> assignedStage.equals(
                            normalize(activity.getStage())))
                    .toList();

    return buildStageSummary(organizationId, allowedActivities);
   }
  }

  return carbonService.summary(organizationId);
 }

 private void requireOrganizationAccess(
         Long requestedOrganizationId,
         Authentication authentication) {

  if (requestedOrganizationId == null) {
   throw new IllegalArgumentException("Organization ID is required.");
  }

  if (hasRole(authentication, "ADMIN")
          || hasRole(authentication, "AUDITOR")) {

   if (!organizationRepository.existsById(requestedOrganizationId)) {
    throw new IllegalArgumentException(
            "The requested organization does not exist.");
   }
   return;
  }

  if (hasRole(authentication, "STAKEHOLDER")) {
   UserAccount account = getCurrentAccount(authentication);

   if (account.getOrganizationId() == null
           || !Objects.equals(
           account.getOrganizationId(),
           requestedOrganizationId)) {
    throw new AccessDeniedException(
            "You cannot access another organization's data.");
   }

   if (!organizationRepository.existsById(requestedOrganizationId)) {
    throw new IllegalArgumentException(
            "The requested organization does not exist.");
   }
   return;
  }

  throw new AccessDeniedException("You are not authorized.");
 }

 private UserAccount getCurrentAccount(Authentication authentication) {
  if (authentication == null || authentication.getName() == null) {
   throw new AccessDeniedException("Authentication is required.");
  }

  return userAccountRepository
          .findByUsernameIgnoreCase(authentication.getName())
          .filter(UserAccount::isEnabled)
          .orElseThrow(() -> new AccessDeniedException(
                  "No enabled EcoTrace account was found for this user."));
 }

 private boolean hasRole(
         Authentication authentication,
         String role) {

  if (authentication == null) {
   return false;
  }

  String expected = "ROLE_" + role;

  return authentication.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .anyMatch(expected::equals);
 }

 private String normalize(String value) {
  if (value == null || value.isBlank()) {
   return null;
  }

  return value.trim().replace('_', ' ').toUpperCase();
 }

 private Map<String, Object> buildStageSummary(
         Long organizationId,
         List<CarbonActivity> activities) {

  BigDecimal total = BigDecimal.ZERO;
  Map<String, BigDecimal> byScope = new LinkedHashMap<>();
  Map<String, BigDecimal> byCategory = new LinkedHashMap<>();

  for (CarbonActivity activity : activities) {
   BigDecimal emissions = activity.getEmissionsKgCo2e() == null
           ? BigDecimal.ZERO
           : activity.getEmissionsKgCo2e();

   total = total.add(emissions);

   String scope = activity.getScope() == null
           ? "UNSPECIFIED"
           : activity.getScope();

   String category = activity.getCategory() == null
           ? "UNSPECIFIED"
           : activity.getCategory();

   byScope.merge(scope, emissions, BigDecimal::add);
   byCategory.merge(category, emissions, BigDecimal::add);
  }

  Map<String, Object> result = new LinkedHashMap<>();
  result.put("organizationId", organizationId);
  result.put("activityCount", activities.size());
  result.put("totalEmissionsKgCo2e", total);
  result.put(
          "totalEmissionsTonnesCo2e",
          total.divide(
                  BigDecimal.valueOf(1000),
                  6,
                  RoundingMode.HALF_UP));
  result.put("byScope", byScope);
  result.put("byCategory", byCategory);
  result.put(
          "note",
          "Summary is restricted to the stakeholder's assigned stage.");

  return result;
 }
}
