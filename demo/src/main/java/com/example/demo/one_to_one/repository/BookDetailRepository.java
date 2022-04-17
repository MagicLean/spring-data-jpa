package com.example.demo.one_to_one.repository;

import com.example.demo.one_to_one.entity.BookDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetail, Integer> {
}
