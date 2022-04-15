package com.example.demo.builder;

import com.example.demo.configuration.SpringApplicationContext;
import com.example.demo.domain.entity.Book;
import com.example.demo.domain.entity.BookDetail;
import com.example.demo.domain.repository.BookDetailRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookDetailBuilder {
    private final BookDetail bookDetail = new BookDetail();

    public static BookDetailBuilder withDefault() {
        return new BookDetailBuilder()
                .withId(1)
                .withNumberOfPages(100)
                .withBook(null);
    }

    public BookDetailBuilder withId(Integer id) {
        bookDetail.setId(id);
        return this;
    }

    public BookDetailBuilder withNumberOfPages(Integer numberOfPages) {
        bookDetail.setNumberOfPages(numberOfPages);
        return this;
    }

    public BookDetailBuilder withBook(Book book) {
        bookDetail.setBook(book);
        return this;
    }

    public BookDetail build() {
        return bookDetail;
    }

    public BookDetail persist() {
        BookDetailRepository repository = SpringApplicationContext.getBean(BookDetailRepository.class);
        return repository.saveAndFlush(bookDetail);
    }
}
