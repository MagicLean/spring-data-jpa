package com.example.demo.domain.repository;

import com.example.demo.domain.entity.BookDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetail, Integer> {
}