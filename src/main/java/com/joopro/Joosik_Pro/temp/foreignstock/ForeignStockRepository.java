package com.joopro.Joosik_Pro.temp.foreignstock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForeignStockRepository extends JpaRepository<ForeignStock, Long> {
}
