
package com.ecotrace.service;

import com.ecotrace.entity.EmissionFactor;
import com.ecotrace.repository.EmissionFactorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
public class EmissionFactorService {

    private final EmissionFactorRepository repository;

    public EmissionFactorService(EmissionFactorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmissionFactor create(EmissionFactor factor) {
        factor.setId(null);

        if (factor.getKgCo2ePerUnit() == null
                || factor.getKgCo2ePerUnit().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Emission factor value must be greater than zero."
            );
        }

        if (Boolean.FALSE.equals(factor.getIllustrative())) {
            requireText(factor.getSourceName(), "Source name");
            requireText(factor.getSourceUrl(), "Source URL");
            requireText(factor.getDatasetVersion(), "Dataset version");
            requireText(factor.getGeography(), "Geography");
            requireText(
                    factor.getBoundaryDescription(),
                    "Boundary description"
            );

            if (factor.getReferenceYear() == null) {
                throw new IllegalArgumentException(
                        "Reference year is required for a non-illustrative factor."
                );
            }

            try {
                URI uri = URI.create(factor.getSourceUrl().trim());

                boolean validScheme =
                        "https".equalsIgnoreCase(uri.getScheme())
                                || "http".equalsIgnoreCase(uri.getScheme());

                if (!validScheme || uri.getHost() == null) {
                    throw new IllegalArgumentException(
                            "Source URL must be a valid HTTP or HTTPS URL."
                    );
                }
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(
                        "Source URL must be a valid HTTP or HTTPS URL."
                );
            }
        }

        return repository.save(factor);
    }

    private void requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    label + " is required for a non-illustrative factor."
            );
        }
    }
}
