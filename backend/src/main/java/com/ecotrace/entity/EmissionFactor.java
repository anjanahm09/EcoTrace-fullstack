
package com.ecotrace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "emission_factors")
public class EmissionFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String name;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String category;

    @NotBlank
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String activityUnit;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal kgCo2ePerUnit;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String sourceName;

    @Size(max = 1000)
    @Column(length = 1000)
    private String sourceUrl;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String datasetVersion;

    @Min(1900)
    @Max(2200)
    private Integer referenceYear;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String geography;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String boundaryDescription;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate accessedDate = LocalDate.now();

    @NotNull
    @Column(nullable = false)
    private Boolean illustrative = true;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getActivityUnit() {
        return activityUnit;
    }

    public void setActivityUnit(String activityUnit) {
        this.activityUnit = activityUnit;
    }

    public BigDecimal getKgCo2ePerUnit() {
        return kgCo2ePerUnit;
    }

    public void setKgCo2ePerUnit(BigDecimal kgCo2ePerUnit) {
        this.kgCo2ePerUnit = kgCo2ePerUnit;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(String datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public Integer getReferenceYear() {
        return referenceYear;
    }

    public void setReferenceYear(Integer referenceYear) {
        this.referenceYear = referenceYear;
    }

    public String getGeography() {
        return geography;
    }

    public void setGeography(String geography) {
        this.geography = geography;
    }

    public String getBoundaryDescription() {
        return boundaryDescription;
    }

    public void setBoundaryDescription(String boundaryDescription) {
        this.boundaryDescription = boundaryDescription;
    }

    public LocalDate getAccessedDate() {
        return accessedDate;
    }

    public void setAccessedDate(LocalDate accessedDate) {
        this.accessedDate = accessedDate;
    }

    public Boolean getIllustrative() {
        return illustrative;
    }

    public void setIllustrative(Boolean illustrative) {
        this.illustrative = illustrative;
    }
}
