package com.h2.service;

import com.h2.entity.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class BookServiceTest {
    @Autowired
    private BookService bookService;

    @Test
    void testSearchTermIsEmpty(){
        String searchString = "";
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.searchBooks(searchString));
    }

    @Test
    void testSearchStringIsNull(){
        String searchString = null;
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.searchBooks(searchString));
    }

    @Test
    void testSearchStringIsValid(){
        String searchString = "Future";
        List<Book> books  = bookService.searchBooks(searchString);
        Assertions.assertTrue(books.size() > 0);
    }
}
