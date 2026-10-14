
package com.ecotrace;

import com.ecotrace.entity.CarbonActivity;
import com.ecotrace.entity.EmissionFactor;
import com.ecotrace.entity.Organization;
import com.ecotrace.repository.CarbonActivityRepository;
import com.ecotrace.repository.EmissionFactorRepository;
import com.ecotrace.repository.OrganizationRepository;
import com.ecotrace.service.CarbonService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CarbonServiceTest {

    @Mock
    private CarbonActivityRepository activities;

    @Mock
    private EmissionFactorRepository factors;

    @Mock
    private OrganizationRepository organizations;

    private CarbonService carbonService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules();

        carbonService = new CarbonService(
                activities,
                factors,
                organizations,
                objectMapper
        );
    }

    @Test
    void shouldCalculateElectricityEmissionsCorrectly() {
        EmissionFactor factor = createTestFactor(
                "KWH", new BigDecimal("0.710")
        );

        CarbonActivity activity = createTestActivity(
                new BigDecimal("100"), "KWH", 1L, 1L
        );

        when(factors.findById(1L))
                .thenReturn(Optional.of(factor));
        when(organizations.findById(1L))
                .thenReturn(Optional.of(createTestOrganization(1L)));
        when(activities.save(any(CarbonActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CarbonActivity result = carbonService.createActivity(activity);

        // 100 kWh × 0.710 = 71 kg CO2e
        assertEquals(
                0,
                new BigDecimal("71.000000")
                        .compareTo(result.getEmissionsKgCo2e())
        );

        verify(activities).save(activity);
    }

    @Test
    void shouldRejectZeroQuantity() {
        CarbonActivity activity = createTestActivity(
                BigDecimal.ZERO, "KWH", 1L, 1L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> carbonService.createActivity(activity)
        );

        verify(activities, never()).save(any(CarbonActivity.class));
    }

    @Test
    void shouldRejectNegativeQuantity() {
        CarbonActivity activity = createTestActivity(
                new BigDecimal("-10"), "KWH", 1L, 1L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> carbonService.createActivity(activity)
        );

        verify(activities, never()).save(any(CarbonActivity.class));
    }

    @Test
    void shouldRejectActivityWhenEmissionFactorDoesNotExist() {
        CarbonActivity activity = createTestActivity(
                new BigDecimal("100"), "KWH", 999L, 1L
        );

        when(factors.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> carbonService.createActivity(activity)
        );

        verify(activities, never()).save(any(CarbonActivity.class));
    }

    @Test
    void shouldRejectActivityWhenOrganizationDoesNotExist() {
        EmissionFactor factor = createTestFactor(
                "KWH", new BigDecimal("0.710")
        );

        CarbonActivity activity = createTestActivity(
                new BigDecimal("100"), "KWH", 1L, 999L
        );

        when(factors.findById(1L))
                .thenReturn(Optional.of(factor));
        when(organizations.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> carbonService.createActivity(activity)
        );

        verify(activities, never()).save(any(CarbonActivity.class));
    }

    @Test
    void shouldConvertGramsToKilogramsBeforeCalculation() {
        EmissionFactor factor = createTestFactor(
                "KG", new BigDecimal("0.710")
        );

        CarbonActivity activity = createTestActivity(
                new BigDecimal("1000"), "G", 1L, 1L
        );

        when(factors.findById(1L))
                .thenReturn(Optional.of(factor));
        when(organizations.findById(1L))
                .thenReturn(Optional.of(createTestOrganization(1L)));
        when(activities.save(any(CarbonActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CarbonActivity result = carbonService.createActivity(activity);

        // 1000 grams = 1 kg; 1 × 0.710 = 0.710 kg CO2e
        assertEquals(
                0,
                new BigDecimal("0.710000")
                        .compareTo(result.getEmissionsKgCo2e())
        );
    }

    @Test
    void shouldCalculateFreightEmissionsUsingTonneKilometres() {
        EmissionFactor factor = createTestFactor(
                "TONNE-KM", new BigDecimal("0.100")
        );

        CarbonActivity activity = createTestActivity(
                BigDecimal.ONE, "TONNE-KM", 1L, 1L
        );

        activity.setCategory("UPSTREAM_TRANSPORTATION");
        activity.setMassTonnes(new BigDecimal("2"));
        activity.setDistanceKm(new BigDecimal("100"));
        activity.setTransportLegId("LEG-001");
        activity.setProductBatch("BATCH-001");

        when(factors.findById(1L))
                .thenReturn(Optional.of(factor));
        when(organizations.findById(1L))
                .thenReturn(Optional.of(createTestOrganization(1L)));
        when(activities
                .existsByOrganizationIdAndTransportLegIdAndProductBatch(
                        1L, "LEG-001", "BATCH-001"))
                .thenReturn(false);
        when(activities.save(any(CarbonActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CarbonActivity result = carbonService.createActivity(activity);

        // 2 tonnes × 100 km × 0.100 = 20 kg CO2e
        assertEquals(
                0,
                new BigDecimal("20.000000")
                        .compareTo(result.getEmissionsKgCo2e())
        );
    }

    @Test
    void shouldApplyAllocationPercentageCorrectly() {
        EmissionFactor factor = createTestFactor(
                "KWH", new BigDecimal("0.710")
        );

        CarbonActivity activity = createTestActivity(
                new BigDecimal("100"), "KWH", 1L, 1L
        );

        activity.setAllocationBasis("PRODUCTION_VOLUME");
        activity.setAllocationPercentage(new BigDecimal("50"));

        when(factors.findById(1L))
                .thenReturn(Optional.of(factor));
        when(organizations.findById(1L))
                .thenReturn(Optional.of(createTestOrganization(1L)));
        when(activities.save(any(CarbonActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CarbonActivity result = carbonService.createActivity(activity);

        // 100 × 0.710 × 50% = 35.5 kg CO2e
        assertEquals(
                0,
                new BigDecimal("35.500000")
                        .compareTo(result.getEmissionsKgCo2e())
        );
    }

    @Test
    void shouldRejectAllocationWithoutAllocationBasis() {
        EmissionFactor factor = createTestFactor(
                "KWH", new BigDecimal("0.710")
        );

        CarbonActivity activity = createTestActivity(
                new BigDecimal("100"), "KWH", 1L, 1L
        );

        activity.setAllocationPercentage(new BigDecimal("50"));
        activity.setAllocationBasis(null);

        when(factors.findById(1L))
                .thenReturn(Optional.of(factor));
        when(organizations.findById(1L))
                .thenReturn(Optional.of(createTestOrganization(1L)));

        assertThrows(
                IllegalArgumentException.class,
                () -> carbonService.createActivity(activity)
        );

        verify(activities, never()).save(any(CarbonActivity.class));
    }

    private EmissionFactor createTestFactor(
            String unit,
            BigDecimal factorValue) {

        EmissionFactor factor = new EmissionFactor();
        factor.setId(1L);
        factor.setName("Test Emission Factor");
        factor.setCategory("PURCHASED_ELECTRICITY");
        factor.setActivityUnit(unit);
        factor.setKgCo2ePerUnit(factorValue);
        factor.setIllustrative(false);
        factor.setSourceName("Test Reference Source");
        factor.setSourceUrl("https://example.com/emission-factor");
        factor.setDatasetVersion("Test Dataset Version");
        factor.setReferenceYear(2025);
        factor.setGeography("IN");
        factor.setBoundaryDescription("Test boundary");
        factor.setAccessedDate(java.time.LocalDate.now());

        return factor;
    }

    private CarbonActivity createTestActivity(
            BigDecimal quantity,
            String unit,
            Long factorId,
            Long organizationId) {

        CarbonActivity activity = new CarbonActivity();
        activity.setActivityName("Test Activity");
        activity.setScope("SCOPE_2");
        activity.setCategory("PURCHASED_ELECTRICITY");
        activity.setQuantity(quantity);
        activity.setUnit(unit);
        activity.setFactorId(factorId);
        activity.setOrganizationId(organizationId);
        activity.setDataQuality("ESTIMATED");
        activity.setActivityDate(java.time.LocalDate.now());

        return activity;
    }

    private Organization createTestOrganization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }
}
