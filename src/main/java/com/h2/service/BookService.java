package com.h2.service;

import com.h2.entity.Book;
import com.h2.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    // we are doing constructor injection
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> searchBooks(String searchString) {
        if (searchString == null || searchString.isEmpty()) {
            throw new IllegalArgumentException("Search String cannot be null or empty");
        }
        return bookRepository.searchBooks(searchString);


    }
}
