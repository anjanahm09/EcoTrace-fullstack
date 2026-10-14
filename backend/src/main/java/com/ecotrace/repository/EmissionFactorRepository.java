
package com.ecotrace.repository;

import com.ecotrace.entity.EmissionFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmissionFactorRepository
        extends JpaRepository<EmissionFactor, Long> {

    List<EmissionFactor> findByCategoryIgnoreCase(String category);
}
