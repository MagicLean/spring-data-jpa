package com.example.demo.one_to_one.service;

import com.example.demo.one_to_one.entity.Book;
import com.example.demo.one_to_one.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
@RequiredArgsConstructor
public class BookService {
    @PersistenceContext
    private EntityManager entityManager;

    private final BookRepository bookRepository;

    public void doService() {
        Book book = bookRepository.findById(1).orElseThrow();
        System.out.println(book);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Book initBook() {
        Book book = new Book();
        book.setName("init");
        book.setBookDetail(null);
        return bookRepository.saveAndFlush(book);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Book getBookById(int id) {
        return bookRepository.findById(id).orElseThrow();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void mergeBook(Book book) {
        entityManager.merge(book);
    }

}
