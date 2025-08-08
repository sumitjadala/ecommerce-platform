package com.sj.product_service.repository;

import com.sj.product_service.entity.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductMetricsRepository extends JpaRepository<ProductMetrics, UUID> {

    Optional<ProductMetrics> findByProductIdAndDate(UUID productId, LocalDate date);

    List<ProductMetrics> findByProductId(UUID productId);

    List<ProductMetrics> findByProductIdOrderByDateDesc(UUID productId);

    @Query("SELECT pm FROM ProductMetrics pm WHERE pm.product.id = :productId AND pm.date BETWEEN :startDate AND :endDate ORDER BY pm.date DESC")
    List<ProductMetrics> findByProductIdAndDateRange(@Param("productId") UUID productId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT pm FROM ProductMetrics pm WHERE pm.date = :date")
    List<ProductMetrics> findByDate(@Param("date") LocalDate date);

    @Query("SELECT pm FROM ProductMetrics pm WHERE pm.date BETWEEN :startDate AND :endDate ORDER BY pm.revenue DESC")
    List<ProductMetrics> findTopProductsByRevenue(@Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT pm FROM ProductMetrics pm WHERE pm.date BETWEEN :startDate AND :endDate ORDER BY pm.views DESC")
    List<ProductMetrics> findTopProductsByViews(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(pm.revenue) FROM ProductMetrics pm WHERE pm.product.id = :productId AND pm.date BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByProductAndDateRange(@Param("productId") UUID productId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(pm) FROM ProductMetrics pm WHERE pm.product.id = :productId")
    Long countByProductId(@Param("productId") UUID productId);
}
