package com.example.demo.domain.service;

import com.example.demo.builder.BookBuilder;
import com.example.demo.builder.BookDetailBuilder;
import com.example.demo.configuration.IntegrationBaseTest;
import com.example.demo.domain.entity.Book;
import com.example.demo.domain.entity.BookDetail;
import com.example.demo.domain.repository.BookDetailRepository;
import com.example.demo.domain.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookServiceTest extends IntegrationBaseTest {

    public static final int NUMBER_OF_PAGES = 99;
    public static final String BOOK_NAME = "Test";

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookDetailRepository bookDetailRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void init() {
        entityManager.clear();
    }

    @AfterEach
    void clear() {
        entityManager.clear();
    }

    @Test
    @Transactional
    void doService() {
        BookDetail bookDetail = BookDetailBuilder.withDefault()
                .withNumberOfPages(NUMBER_OF_PAGES)
                .persist();
        Book book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .withBookDetail(bookDetail)
                .persist();

        bookRepository.findById(1).ifPresent(savedBook -> {
            assertEquals(BOOK_NAME, savedBook.getName());
            assertEquals(NUMBER_OF_PAGES, savedBook.getBookDetail().getNumberOfPages());
        });
        bookDetailRepository.findById(1).ifPresent(savedBookDetail -> assertEquals(NUMBER_OF_PAGES, savedBookDetail.getNumberOfPages()));
    }

    @Test
    @Transactional
    void should_persist_successfully_when_persist_entity_has_not_id_given_GeneratedValue_annotation() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .build();
        entityManager.persist(book);
        book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .build();
        entityManager.persist(book);
        assertFalse(bookRepository.findAll().isEmpty());
        assertEquals(BOOK_NAME, bookRepository.findById(1).get().getName());
        assertEquals(BOOK_NAME, bookRepository.findById(2).get().getName());
        assertFalse(bookRepository.findAll().isEmpty());
    }

    @Test
    @Transactional
    void should_throw_exception_when_persist_entity_has_id_given_GeneratedValue_annotation() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withId(1)
                .withName(BOOK_NAME)
                .build();
        assertThrows(PersistenceException.class, () -> entityManager.persist(book));
        assertTrue(bookRepository.findAll().isEmpty());
    }
}