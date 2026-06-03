package com.h2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

public class DbImporter {
    private static final String CSV_URL ="https://gist.githubusercontent.com/books.csv";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/library";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "admin123";

    public static void main(String[] args) throws IOException {
        try{
            System.out.println("Starting data ingestion");

            // Step 1: Download CSV data
            System.out.println("Downloading CSV Data...");
            InputStream csvStream = download(CSV_URL);

            //Step 2: Parse CSv Data
            System.out.println("Parsing CSV Data");
            List<String[]> records = parseCSV(csvStream);

            // step 3: Insert Data into DataBase
            System.out.println("Inserting data into database");
            insertData(records);
            System.out.println("Data ingestion completed successfully");

        } catch (Exception e) {
            System.err.println("An error occurred during data Ingestion:");
            e.printStackTrace();
        }
    }




    private static InputStream download(String csvUrl) throws IOException{
        URI uri = URI.create(csvUrl);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return conn.getInputStream();
    }

    // reprocess and parse the XCSV file using OpenCSV
    private static List<String[]> parseCSV(InputStream csvStream) throws IOException, CsvValidationException {
        List<String[]> records = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream));
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(',')
                .build();

        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(csvParser)
                .build(); // don't skip the header

        String[] nextLine;
        while((nextLine= csvReader.readNext()) != null){
            records.add(nextLine);
        }
        csvReader.close();
        return records;
    }

    // Inserting data into the database including authors, books and books_authors table
    private static void insertData(List<String[]> records) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

        String insertAuthorSQL = "Insert INTO authors (author_name) VALUES (?) ON CONFLICT (author_name) DO NOTHING RETURNING author_id";
        String selectAuthorSQL = "SELECT author_id FROM authors WHERE author_name = ?";
        String insertBookSQL = "Insert INTO books (title, rating, description, language, isbn, book_format, edition, pages, publisher, publish_date, first_publish_date, liked_percent, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING book_id";
        String insertBookAuthorSQL = "INSERT INTO books_authors (book_id, author_id) VALUES (?,?)";
        conn.setAutoCommit(false);

        try(PreparedStatement authorStmt = conn.prepareStatement(insertAuthorSQL);
        PreparedStatement selectAuthorStmt = conn.prepareStatement(selectAuthorSQL);
        PreparedStatement bookStmt = conn.prepareStatement(insertBookSQL);
        PreparedStatement bookAuthrStmt = conn.prepareStatement(insertBookAuthorSQL);
        ){
            String[] header = records.get(0);
            Map<String, Integer> headerMap = new HashMap<>();
            for(int i = 0; i< header.length;i++){
                headerMap.put(header[i], i );
            }

            for (int i =1; i< records.size(); i++){
                String[] record = records.get(i);

                String title = record[headerMap.get("title")];
                String authorName = record[headerMap.get("author")];
                double rating = Double.parseDouble(record[headerMap.get("rating")]);
                String description = record[headerMap.get("description")];
                String language = record[headerMap.get("language")];
                String isbn = record[headerMap.get("isbn")];
                String bookFormat = record[headerMap.get("bookFormat")];
                String edition = record[headerMap.get("edition")];
                int pages = Integer.parseInt(record[headerMap.get("pages")]);
                String publisher = record[headerMap.get("publisher")];
                String publishDateStr = record[headerMap.get("publishDate")];
                String firstPublishDateStr = record[headerMap.get("firstPublishDate")];
                double likedPercent = Double.parseDouble(record[headerMap.get("likedPercent")]);
                double price = Double.parseDouble(record[headerMap.get("price")]);

                // Insert into authors
                int authorId;
                authorStmt.setString(1,authorName);
                ResultSet authorRS = authorStmt.executeQuery();
                if(authorRS.next()){
                    authorId = authorRS.getInt("author_id");
                }else {
                    // Author already exists retrieve ID
                    selectAuthorStmt.setString(1, authorName);
                    ResultSet selectAuthorRS = selectAuthorStmt.executeQuery();
                    if(selectAuthorRS.next()){
                        authorId = selectAuthorRS.getInt("author_id");

                    }else{
                        throw new SQLException("Failed to retieve author_id for " +authorName);
                    }
                    selectAuthorRS.close();
                }
                authorRS.close();


                // Insert into books
                int bookID;
                bookStmt.setString(1, title);
                bookStmt.setDouble(2, rating);
                bookStmt.setString(3, description);
                bookStmt.setString(4, language);
                bookStmt.setString(5, isbn);
                bookStmt.setString(6, bookFormat);
                bookStmt.setString(7, edition);
                bookStmt.setInt(8, pages);
                bookStmt.setString(9, publisher);
                // Handle possible null dates
                Date publishDate = null;
                Date firstPublishDate = null;
                try{
                    publishDate = Date.valueOf(publishDateStr);

                }catch (Exception e){
                    e.printStackTrace();
                }
                try{
                    firstPublishDate = Date.valueOf(firstPublishDateStr);
                }catch (Exception e){
                    e.printStackTrace();
                }
                bookStmt.setDate(10, publishDate);
                bookStmt.setDate(11, firstPublishDate);
                bookStmt.setDouble(12, likedPercent);
                bookStmt.setDouble(13, price);
                ResultSet bookRS = bookStmt.executeQuery();
                if(bookRS.next()){
                    bookID = bookRS.getInt("book_id");
                }else{
                    throw new SQLException("Failed to inset book" +title);

                }
                bookRS.close();
                // Insert into book_authors
                bookAuthrStmt.setInt(1, bookID);
                bookAuthrStmt.setInt(2, authorId);
                bookAuthrStmt.executeUpdate();


            }
            conn.commit();

        }catch (SQLException e){
            conn.rollback();
            throw e;
        }finally{
            conn.setAutoCommit(true);
            conn.close();
        }
    }


}
