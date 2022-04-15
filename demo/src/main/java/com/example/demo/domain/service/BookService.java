package com.example.demo.domain.service;

import com.example.demo.domain.entity.Book;
import com.example.demo.domain.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public void doService() {
        Book book = bookRepository.findById(1).orElseThrow();
        System.out.println(book);
    }

}
