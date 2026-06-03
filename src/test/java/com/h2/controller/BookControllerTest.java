package com.h2.controller;

import com.h2.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookControllerTest {
    @LocalServerPort
    private int port;
   private RestClient restClient;
   @BeforeEach
   void setup(){
       System.out.println("PORT = " + port);
       restClient = RestClient.builder()
               .baseUrl("http://localhost:" + port)
               .build();
   }

    @Test
    void testSearchBooks(){
       ResponseEntity<Book[]> response = restClient.get()
               .uri("/api/books/search?query=java")
               .retrieve()
               .toEntity(Book[].class);
       Book[] books = response.getBody();
       for(Book book : books) {
           System.out.println("Returned books: " + book);
       }
       assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
