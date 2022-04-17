package com.example.demo.one_to_one;

import com.example.demo.builder.BookBuilder;
import com.example.demo.configuration.IntegrationBaseTest;
import com.example.demo.one_to_one.entity.Book;
import com.example.demo.one_to_one.repository.BookDetailRepository;
import com.example.demo.one_to_one.repository.BookRepository;
import com.example.demo.one_to_one.service.BookService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookServiceTest extends IntegrationBaseTest {

    public static final int NUMBER_OF_PAGES = 99;
    public static final String BOOK_NAME = "Test";

    @Autowired
    private BookService bookService;

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_persist_successfully_when_persist_entity_has_not_id_given_GeneratedValue_annotation() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .build();
        assertTrue(bookRepository.findAll().isEmpty());
        entityManager.persist(book);
//        使用merge效果一样
//        entityManager.merge(book);
        assertEquals(BOOK_NAME, bookRepository.findById(1).get().getName());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_throw_exception_when_persist_entity_calling_refresh() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .build();
        assertTrue(bookRepository.findAll().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> entityManager.refresh(book));
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_throw_exception_when_persist_entity_has_id_given_GeneratedValue_annotation() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withId(1)
                .withName(BOOK_NAME)
                .build();
        assertThrows(PersistenceException.class, () -> entityManager.persist(book));
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_auto_update_database_when_entity_is_in_managed_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .build();
        // 调用persist之后，entity处于managed状态
        entityManager.persist(book);
        Book savedBook = bookRepository.findAll().get(0);
        assertEquals(BOOK_NAME, savedBook.getName());
        // 此时设置entity的属性，所有的修改都暂时保持在持久化上下文中，在持久化上下文关闭或者flush时候，会自动同步到数据库
        book.setName("auto");
        savedBook = bookRepository.findAll().get(0);
        assertEquals("auto", savedBook.getName());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_remove_successfully_when_calling_remove_given_entity_is_in_managed_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .build();
        // 调用persist之后，entity处于managed状态
        entityManager.persist(book);
        assertFalse(bookRepository.findAll().isEmpty());
        // 此时调用remove方法，自动将数据库的数据删除
        entityManager.remove(book);
        assertTrue(bookRepository.findAll().isEmpty());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_not_update_database_when_entity_is_in_detached_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        // 调用initBook，此时事务已经提交，entity处于detached状态
        Book book = bookService.initBook();
        assertEquals("init", book.getName());
        // 此时update，数据库的数据不应改变
        book.setName("update");
        Book savedBook = bookService.getBookById(1);
        assertEquals("init", savedBook.getName());
        // 通过merge将entity变为managed状态，数据库会自动更新
        bookService.mergeBook(book);
        savedBook = bookService.getBookById(1);
        assertEquals("update", savedBook.getName());
    }

    @Test
    @Transactional
    void should_throw_exception_when_calling_persist_given_entity_is_in_detached_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        // 调用initBook，此时事务已经提交，entity处于detached状态
        Book book = bookService.initBook();
        // 此时调用persist方法，会抛出异常 PersistenceException
        assertThrows(PersistenceException.class, () -> entityManager.persist(book));
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_throw_exception_when_calling_refresh_or_remove_given_entity_is_in_detached_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        // 调用initBook，此时事务已经提交，entity处于detached状态
        Book book = bookService.initBook();
        assertEquals("init", book.getName());
        // 此时调用refresh或者remove方法，会抛出异常 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> entityManager.refresh(book));
        assertThrows(IllegalArgumentException.class, () -> entityManager.remove(book));
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_throw_exception_when_calling_refresh_or_remove_given_entity_is_in_removed_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .build();
        // 调用persist之后，entity处于managed状态
        entityManager.persist(book);
        // 此时调用remove方法，自动将数据库的数据删除
        entityManager.remove(book);
        // 最终调用refresh或者merge，抛出异常 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> entityManager.refresh(book));
        assertThrows(IllegalArgumentException.class, () -> entityManager.merge(book));
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_persist_successfully_when_calling_persist_given_entity_is_in_removed_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .build();
        // 调用persist之后，entity处于managed状态
        entityManager.persist(book);
        // 此时调用remove方法，entity处于removed状态，调用merge会抛出异常
        entityManager.remove(book);
        assertThrows(IllegalArgumentException.class, () -> entityManager.merge(book));
        // 调用persist，entity变为managed状态，调用merge不会出错
        entityManager.persist(book);
        assertDoesNotThrow(() -> entityManager.merge(book));
        assertFalse(bookRepository.findAll().isEmpty());
    }

}