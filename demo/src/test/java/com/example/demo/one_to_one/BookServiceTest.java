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
//        ??????merge????????????
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
         //refresh????????????
        entityManager.refresh(book);
//        assertThrows(IllegalArgumentException.class, () -> entityManager.refresh(book));
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_throw_exception_when_persist_entity_has_id_given_GeneratedValue_annotation() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withId(1)
                .withName(BOOK_NAME)
                .build();
        // persist????????????
        entityManager.persist(book);
        assertThrows(PersistenceException.class, () -> entityManager.persist(book));
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_auto_update_database_when_entity_is_in_managed_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        Book book = BookBuilder.withDefault()
                .withName(BOOK_NAME)
                .build();
        // ??????persist?????????entity??????managed??????
        entityManager.persist(book);
        Book savedBook = bookRepository.findAll().get(0);
        assertEquals(BOOK_NAME, savedBook.getName());
        // ????????????entity??????????????????????????????????????????????????????????????????????????????????????????????????????flush????????????????????????????????????
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
        // ??????persist?????????entity??????managed??????
        entityManager.persist(book);
        assertFalse(bookRepository.findAll().isEmpty());
        // ????????????remove??????????????????????????????????????????
        entityManager.remove(book);
        assertTrue(bookRepository.findAll().isEmpty());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_not_update_database_when_entity_is_in_detached_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        // ??????initBook??????????????????????????????entity??????detached??????
        Book book = bookService.initBook();
        assertEquals("init", book.getName());
        // ??????update?????????????????????????????????
        book.setName("update");
        Book savedBook = bookService.getBookById(1);
        assertEquals("init", savedBook.getName());
        // ??????merge???entity??????managed?????????????????????????????????
        bookService.mergeBook(book);
        savedBook = bookService.getBookById(1);
        assertEquals("update", savedBook.getName());
    }

    @Test
    @Transactional
    void should_throw_exception_when_calling_persist_given_entity_is_in_detached_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        // ??????initBook??????????????????????????????entity??????detached??????
        Book book = bookService.initBook();
        // ????????????persist???????????????????????? PersistenceException
//        entityManager.persist(book);
        assertThrows(PersistenceException.class, () -> entityManager.persist(book));
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void should_throw_exception_when_calling_refresh_or_remove_given_entity_is_in_detached_state() {
        assertTrue(bookRepository.findAll().isEmpty());
        // ??????initBook??????????????????????????????entity??????detached??????
        Book book = bookService.initBook();
        assertEquals("init", book.getName());
        // ????????????refresh??????remove???????????????????????? IllegalArgumentException
        entityManager.refresh(book);
//        entityManager.remove(book);
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
        // ??????persist?????????entity??????managed??????
        entityManager.persist(book);
        // ????????????remove??????????????????????????????????????????
        entityManager.remove(book);
        // ????????????refresh??????merge??????????????? IllegalArgumentException
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
        // ??????persist?????????entity??????managed??????
        entityManager.persist(book);
        // ????????????remove?????????entity??????removed???????????????merge???????????????
        entityManager.remove(book);
        assertThrows(IllegalArgumentException.class, () -> entityManager.merge(book));
        // ??????persist???entity??????managed???????????????merge????????????
        entityManager.persist(book);
        assertDoesNotThrow(() -> entityManager.merge(book));
        assertFalse(bookRepository.findAll().isEmpty());
    }

}