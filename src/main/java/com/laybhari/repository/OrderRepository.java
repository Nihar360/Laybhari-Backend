package com.laybhari.repository;

import com.laybhari.entity.Order;
import com.laybhari.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    Optional<Order> findByIdAndUser(Long id, User user);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Order> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    long countByUser(User user);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user = :user AND o.paymentStatus = :paymentStatus")
    BigDecimal sumTotalAmountByUserAndPaymentStatus(@Param("user") User user, @Param("paymentStatus") String paymentStatus);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = :paymentStatus")
    BigDecimal sumTotalAmountByPaymentStatus(@Param("paymentStatus") String paymentStatus);

    List<Order> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();
}
