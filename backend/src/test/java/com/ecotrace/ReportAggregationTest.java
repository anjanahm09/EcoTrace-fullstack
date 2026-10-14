
package com.ecotrace;

import com.ecotrace.entity.CarbonActivity;
import com.ecotrace.entity.Organization;
import com.ecotrace.repository.CarbonActivityRepository;
import com.ecotrace.repository.EmissionFactorRepository;
import com.ecotrace.repository.OrganizationRepository;
import com.ecotrace.service.CarbonService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportAggregationTest {

    private CarbonActivityRepository activities;
    private EmissionFactorRepository factors;
    private OrganizationRepository organizations;
    private CarbonService carbonService;

    @BeforeEach
    void setUp() {
        activities = mock(CarbonActivityRepository.class);
        factors = mock(EmissionFactorRepository.class);
        organizations = mock(OrganizationRepository.class);

        carbonService = new CarbonService(
                activities,
                factors,
                organizations,
                new ObjectMapper().findAndRegisterModules()
        );

        Organization organization = new Organization();
        organization.setId(1L);

        when(organizations.findById(1L))
                .thenReturn(Optional.of(organization));
    }

    private CarbonActivity activity(
            String scope,
            String category,
            String emissions) {

        CarbonActivity activity = new CarbonActivity();
        activity.setScope(scope);
        activity.setCategory(category);
        activity.setEmissionsKgCo2e(new BigDecimal(emissions));

        return activity;
    }

    @Test
    void shouldCalculateTotalEmissions() {
        when(activities.findByOrganizationId(1L))
                .thenReturn(List.of(
                        activity("SCOPE_1", "STATIONARY_COMBUSTION", "100"),
                        activity("SCOPE_2", "ELECTRICITY", "71"),
                        activity("SCOPE_3", "PURCHASED_GOODS", "29")
                ));

        Map<String, Object> result = carbonService.summary(1L);

        assertEquals(
                0,
                new BigDecimal(result.get("totalKgCo2e").toString())
                        .compareTo(new BigDecimal("200"))
        );
    }

    @Test
    void shouldConvertKilogramsToTonnes() {
        when(activities.findByOrganizationId(1L))
                .thenReturn(List.of(
                        activity("SCOPE_2", "ELECTRICITY", "2500")
                ));

        Map<String, Object> result = carbonService.summary(1L);

        assertEquals(
                0,
                new BigDecimal(result.get("totalTonnesCo2e").toString())
                        .compareTo(new BigDecimal("2.5"))
        );
    }

    @Test
    void shouldAggregateEmissionsByScope() {
        when(activities.findByOrganizationId(1L))
                .thenReturn(List.of(
                        activity("SCOPE_1", "FUEL", "40"),
                        activity("SCOPE_1", "PROCESS", "10"),
                        activity("SCOPE_2", "ELECTRICITY", "70"),
                        activity("SCOPE_3", "FREIGHT", "30")
                ));

        Map<String, Object> result = carbonService.summary(1L);

        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> byScope =
                (Map<String, BigDecimal>) result.get("byScopeKgCo2e");

        assertEquals(0, byScope.get("SCOPE_1")
                .compareTo(new BigDecimal("50")));
        assertEquals(0, byScope.get("SCOPE_2")
                .compareTo(new BigDecimal("70")));
        assertEquals(0, byScope.get("SCOPE_3")
                .compareTo(new BigDecimal("30")));
    }

    @Test
    void shouldAggregateEmissionsByCategory() {
        when(activities.findByOrganizationId(1L))
                .thenReturn(List.of(
                        activity("SCOPE_2", "ELECTRICITY", "50"),
                        activity("SCOPE_2", "ELECTRICITY", "20"),
                        activity("SCOPE_3", "FREIGHT", "30")
                ));

        Map<String, Object> result = carbonService.summary(1L);

        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> byCategory =
                (Map<String, BigDecimal>) result.get("byCategoryKgCo2e");

        assertEquals(0, byCategory.get("ELECTRICITY")
                .compareTo(new BigDecimal("70")));
        assertEquals(0, byCategory.get("FREIGHT")
                .compareTo(new BigDecimal("30")));
    }

    @Test
    void shouldReturnCorrectActivityCount() {
        when(activities.findByOrganizationId(1L))
                .thenReturn(List.of(
                        activity("SCOPE_1", "FUEL", "10"),
                        activity("SCOPE_2", "ELECTRICITY", "20"),
                        activity("SCOPE_3", "FREIGHT", "30")
                ));

        Map<String, Object> result = carbonService.summary(1L);

        assertEquals(3, result.get("activityCount"));
    }

    @Test
    void shouldReturnZeroTotalsForEmptyReport() {
        when(activities.findByOrganizationId(1L))
                .thenReturn(List.of());

        Map<String, Object> result = carbonService.summary(1L);

        assertEquals(
                0,
                new BigDecimal(result.get("totalKgCo2e").toString())
                        .compareTo(BigDecimal.ZERO)
        );

        assertEquals(0, result.get("activityCount"));
        assertTrue(((Map<?, ?>) result.get("byScopeKgCo2e")).isEmpty());
        assertTrue(((Map<?, ?>) result.get("byCategoryKgCo2e")).isEmpty());
    }

    @Test
    void shouldRejectNullOrganizationId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> carbonService.summary(null)
        );
    }

    @Test
    void shouldRejectUnknownOrganization() {
        when(organizations.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> carbonService.summary(99L)
        );

        verify(activities, never()).findByOrganizationId(99L);
    }

    @Test
    void shouldIncludeOrganizationIdAndReportingNote() {
        when(activities.findByOrganizationId(1L))
                .thenReturn(List.of());

        Map<String, Object> result = carbonService.summary(1L);

        assertEquals(1L, result.get("organizationId"));
        assertTrue(
                result.get("note").toString().contains("product footprint")
        );
    }
}
