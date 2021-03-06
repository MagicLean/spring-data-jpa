package com.example.demo.fetch_mode.repository;

import com.example.demo.fetch_mode.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    public Customer findByName(String name);
}