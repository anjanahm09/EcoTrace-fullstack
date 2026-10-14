
package com.ecotrace.config;

import com.ecotrace.entity.EmissionFactor;
import com.ecotrace.entity.Organization;
import com.ecotrace.repository.EmissionFactorRepository;
import com.ecotrace.repository.OrganizationRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DemoData {

 @Bean
 CommandLineRunner loadDemoData(
         OrganizationRepository organizations,
         EmissionFactorRepository factors) {

  return args -> {
   if (organizations.count() == 0) {
    Organization organization = new Organization();
    organization.setName(
            "EcoTrace Demo Manufacturing"
    );
    organization.setOrganizationType("Manufacturer");
    organization.setCountry("India");
    organization.setBoundaryDescription(
            "Illustrative demo organization"
    );

    organizations.save(organization);
   }

   if (factors.count() == 0) {
    factors.save(createFactor(
            "DEMO ONLY - Electricity sample",
            "SCOPE_2",
            "kWh",
            "0.5",
            "NOT_VERIFIED_DEMO"
    ));

    factors.save(createFactor(
            "DEMO ONLY - Diesel sample",
            "SCOPE_1",
            "litre",
            "2.68",
            "NOT_VERIFIED_DEMO"
    ));

    factors.save(createFactor(
            "DEMO ONLY - Freight sample",
            "SCOPE_3",
            "tonne-km",
            "0.1",
            "NOT_VERIFIED_DEMO"
    ));
   }
  };
 }

 private EmissionFactor createFactor(
         String name,
         String category,
         String unit,
         String value,
         String version) {

  EmissionFactor factor = new EmissionFactor();
  factor.setName(name);
  factor.setCategory(category);
  factor.setActivityUnit(unit);
  factor.setKgCo2ePerUnit(new BigDecimal(value));
  factor.setSourceName("Illustrative placeholder - not verified");
  factor.setSourceUrl("");
  factor.setDatasetVersion(version);
  factor.setGeography("Not verified");
  factor.setBoundaryDescription(
          "Illustrative example only; not suitable for reporting"
  );
  factor.setAccessedDate(LocalDate.now());
  factor.setIllustrative(true);

  return factor;
 }
}
