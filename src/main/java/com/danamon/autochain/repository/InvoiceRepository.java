package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

}
