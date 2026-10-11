package com.rehancode.finpay.Repository;

import com.rehancode.finpay.Model.LedgerModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepo extends JpaRepository<LedgerModel, Long> {
}
