package com.joopro.Joosik_Pro.repository;

import com.joopro.Joosik_Pro.domain.ForeignStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForeignStockRepository extends JpaRepository<ForeignStock, Long> {
}
