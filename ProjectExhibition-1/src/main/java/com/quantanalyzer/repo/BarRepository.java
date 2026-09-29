package com.quantanalyzer.repo;

//It is the layer responsible for all reads/writes to the price table.

import com.quantanalyzer.domain.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BarRepository extends JpaRepository<Price, Long> {

    List<Price> findByTickerOrderByBarDateAsc(String ticker);   //fetches all bars for a ticker

    Optional<Price> findTopByTickerOrderByBarDateDesc(String ticker);   //fetches only the most recent bar

    boolean existsByTicker(String ticker);  //checks if any data exists at all for a ticker
}