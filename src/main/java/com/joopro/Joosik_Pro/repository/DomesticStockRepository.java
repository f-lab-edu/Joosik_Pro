package com.joopro.Joosik_Pro.repository;

import com.joopro.Joosik_Pro.domain.DomesticStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomesticStockRepository extends JpaRepository<DomesticStock, Long> {
}
