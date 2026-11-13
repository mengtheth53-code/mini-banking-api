package com.minibanking.api.repository;

import com.minibanking.api.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccountNumber = :acc OR t.targetAccountNumber = :acc) " +
           "AND (:startDate IS NULL OR t.timestamp >= :startDate) " +
           "AND (:endDate IS NULL OR t.timestamp <= :endDate) " +
           "ORDER BY t.timestamp DESC")
    Page<Transaction> findAccountStatement(
            @Param("acc") String accountNumber,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}
