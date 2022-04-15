package com.example.demo.builder;

import com.example.demo.configuration.SpringApplicationContext;
import com.example.demo.domain.entity.Book;
import com.example.demo.domain.entity.BookDetail;
import com.example.demo.domain.repository.BookRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookBuilder {
    private Book book = new Book();

    public static BookBuilder withDefault() {
        return new BookBuilder()
                .withName("Test Default")
                .withBookDetail(null);
    }

    public BookBuilder withId(Integer id) {
        book.setId(id);
        return this;
    }

    public BookBuilder withName(String name) {
        book.setName(name);
        return this;
    }

    public BookBuilder withBookDetail(BookDetail bookDetail) {
        book.setBookDetail(bookDetail);
        return this;
    }

    public Book build() {
        return book;
    }

    public Book persist() {
        BookRepository repository = SpringApplicationContext.getBean(BookRepository.class);
        return repository.saveAndFlush(book);
    }
}
