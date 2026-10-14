
package com.ecotrace.controller;

import com.ecotrace.entity.EmissionFactor;
import com.ecotrace.repository.EmissionFactorRepository;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factor-admin")
public class EmissionFactorController {

    private final EmissionFactorRepository factors;

    public EmissionFactorController(EmissionFactorRepository factors) {
        this.factors = factors;
    }

    // ADMIN, STAKEHOLDER, and AUDITOR can view all emission factors.
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAKEHOLDER', 'AUDITOR')")
    public List<EmissionFactor> getAllFactors() {
        return factors.findAll();
    }

    // ADMIN, STAKEHOLDER, and AUDITOR can view one emission factor.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAKEHOLDER', 'AUDITOR')")
    public ResponseEntity<EmissionFactor> getFactorById(
            @PathVariable Long id) {

        return factors.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Only ADMIN can create an emission factor.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmissionFactor> createFactor(
            @Valid @RequestBody EmissionFactor factor) {

        factor.setId(null);

        if (factor.getCategory() != null) {
            String category = factor.getCategory()
                    .trim()
                    .toUpperCase()
                    .replace(" ", "_");

            factor.setCategory(category);
        }

        EmissionFactor savedFactor = factors.save(factor);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedFactor);
    }
}

