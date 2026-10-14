
package com.ecotrace.repository;

import com.ecotrace.entity.CarbonActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarbonActivityRepository
        extends JpaRepository<CarbonActivity, Long> {

    List<CarbonActivity> findByOrganizationId(Long organizationId);

    boolean existsByOrganizationIdAndTransportLegIdAndProductBatch(
            Long organizationId,
            String transportLegId,
            String productBatch
    );
}
