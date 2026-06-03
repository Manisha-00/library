package com.h2.repository;

import com.h2.entity.Book;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;
    @Test
    void testSearchBooks(){
        List<Book> books = bookRepository.searchBooks("algorithm");
        assertTrue(books.size() > 0);
        for(Book book : books){
            System.out.println(">>> Book Title: " +book.getTitle());
        }
    }

}
