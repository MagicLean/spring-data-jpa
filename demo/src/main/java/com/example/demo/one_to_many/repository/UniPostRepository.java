package com.example.demo.one_to_many.repository;

import com.example.demo.one_to_many.entity.unidirectional.UniPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniPostRepository extends JpaRepository<UniPost, Long> {
}
