
package com.ecotrace.controller;

import com.ecotrace.config.SecurityConfig;
import com.ecotrace.entity.CarbonActivity;
import com.ecotrace.entity.EmissionFactor;
import com.ecotrace.entity.UserAccount;
import com.ecotrace.repository.CarbonActivityRepository;
import com.ecotrace.repository.EmissionFactorRepository;
import com.ecotrace.repository.OrganizationRepository;
import com.ecotrace.repository.UserAccountRepository;
import com.ecotrace.service.CarbonService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        ApiController.class,
        EmissionFactorController.class
})
@Import(SecurityConfig.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarbonService carbonService;

    @MockitoBean
    private EmissionFactorRepository factors;

    @MockitoBean
    private OrganizationRepository organizations;

    @MockitoBean
    private UserAccountRepository userAccountRepository;

    @MockitoBean
    private CarbonActivityRepository activityRepository;

    private UserAccount createAccount(
            String username,
            String role,
            Long organizationId,
            String assignedStage) {

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setRole(role);
        account.setOrganizationId(organizationId);
        account.setAssignedStage(assignedStage);
        account.setEnabled(true);
        return account;
    }

    private CarbonActivity createActivity(
            Long id,
            Long organizationId,
            String stage,
            String activityName) {

        CarbonActivity activity = new CarbonActivity();
        activity.setId(id);
        activity.setOrganizationId(organizationId);
        activity.setStage(stage);
        activity.setActivityName(activityName);
        activity.setScope("SCOPE_1");
        activity.setCategory("STATIONARY_COMBUSTION");
        activity.setQuantity(BigDecimal.ONE);
        activity.setUnit("L");
        activity.setEmissionsKgCo2e(BigDecimal.ONE);
        return activity;
    }

    private void mockStakeholder(Long organizationId, String assignedStage) {
        when(userAccountRepository.findByUsernameIgnoreCase("stakeholder"))
                .thenReturn(Optional.of(
                        createAccount(
                                "stakeholder",
                                "STAKEHOLDER",
                                organizationId,
                                assignedStage)));
    }

    @Test
    void protectedOrganizationsEndpointRejectsUnauthenticatedUser()
            throws Exception {
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedActivitiesEndpointRejectsUnauthenticatedUser()
            throws Exception {
        mockMvc.perform(get("/api/activities")
                        .param("organizationId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadOrganizations() throws Exception {
        when(organizations.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAKEHOLDER")
    void stakeholderCannotReadAllOrganizations() throws Exception {
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    void auditorCannotReadAdminOrganizationList() throws Exception {
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadEmissionFactors() throws Exception {
        when(factors.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/factor-admin"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    void auditorCanReadEmissionFactors() throws Exception {
        when(factors.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/factor-admin"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAKEHOLDER")
    void stakeholderCanReadEmissionFactors() throws Exception {
        when(factors.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/factor-admin"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAKEHOLDER")
    void stakeholderCannotCreateEmissionFactor() throws Exception {
        mockMvc.perform(post("/api/factor-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verify(factors, never()).save(any(EmissionFactor.class));
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    void auditorCannotCreateEmissionFactor() throws Exception {
        mockMvc.perform(post("/api/factor-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verify(factors, never()).save(any(EmissionFactor.class));
    }

    @Test
    @WithMockUser(username = "stakeholder", roles = "STAKEHOLDER")
    void stakeholderCannotReadAnotherOrganizationsActivities()
            throws Exception {
        mockStakeholder(1L, null);

        mockMvc.perform(get("/api/activities")
                        .param("organizationId", "2"))
                .andExpect(status().isForbidden());

        verify(carbonService, never()).getActivities(2L);
    }

    @Test
    @WithMockUser(username = "stakeholder", roles = "STAKEHOLDER")
    void stakeholderCanReadOwnOrganizationsActivities() throws Exception {
        mockStakeholder(1L, null);
        when(organizations.existsById(1L)).thenReturn(true);
        when(carbonService.getActivities(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/activities")
                        .param("organizationId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "stakeholder", roles = "STAKEHOLDER")
    void stakeholderCannotCreateActivityForAnotherOrganization()
            throws Exception {
        mockStakeholder(1L, null);

        String requestBody = """
                {
                  "activityName": "Diesel consumption",
                  "scope": "SCOPE_1",
                  "category": "STATIONARY_COMBUSTION",
                  "quantity": 10,
                  "unit": "L",
                  "factorId": 1,
                  "organizationId": 2
                }
                """;

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());

        verify(carbonService, never())
                .createActivity(any(CarbonActivity.class));
    }

    @Test
    @WithMockUser(username = "stakeholder", roles = "STAKEHOLDER")
    void stakeholderCanSeeOnlyAssignedStageActivities() throws Exception {
        mockStakeholder(1L, "MANUFACTURER");
        when(organizations.existsById(1L)).thenReturn(true);

        CarbonActivity manufacturerActivity =
                createActivity(1L, 1L, "MANUFACTURER", "Factory fuel");

        CarbonActivity logisticsActivity =
                createActivity(2L, 1L, "LOGISTICS", "Freight transport");

        when(carbonService.getActivities(1L))
                .thenReturn(List.of(manufacturerActivity, logisticsActivity));

        mockMvc.perform(get("/api/activities")
                        .param("organizationId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "stakeholder", roles = "STAKEHOLDER")
    void stakeholderCannotCreateActivityOutsideAssignedStage()
            throws Exception {
        mockStakeholder(1L, "MANUFACTURER");

        String requestBody = """
                {
                  "activityName": "Freight transport",
                  "scope": "SCOPE_3",
                  "category": "UPSTREAM_TRANSPORTATION",
                  "quantity": 100,
                  "unit": "TONNE-KM",
                  "factorId": 1,
                  "organizationId": 1,
                  "stage": "LOGISTICS"
                }
                """;

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());

        verify(carbonService, never())
                .createActivity(any(CarbonActivity.class));
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    void auditorCannotCreateCarbonActivity() throws Exception {
        String requestBody = """
                {
                  "activityName": "Diesel consumption",
                  "scope": "SCOPE_1",
                  "category": "STATIONARY_COMBUSTION",
                  "quantity": 10,
                  "unit": "L",
                  "factorId": 1,
                  "organizationId": 1
                }
                """;

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());

        verify(carbonService, never())
                .createActivity(any(CarbonActivity.class));
    }
}
