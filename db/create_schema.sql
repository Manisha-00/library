-- we are using the postgre SQL
--CSV header is below
-- bookId,title,author,rating,description,language,isbn,bookFormat,edition,pages,publisher,publishDate,firstPublishDate,likedPercent,price

-- create the books table
CREATE TABLE books (
                       book_id            SERIAL PRIMARY KEY,
                       title              VARCHAR(500) NOT NULL,
                       rating             NUMERIC(3,2),
                       description        TEXT,
                       language           VARCHAR(50),
                       isbn               VARCHAR(20),
                       book_format        VARCHAR(100),
                       edition            VARCHAR(100),
                       pages              INTEGER,
                       publisher          VARCHAR(255),
                       publish_date       DATE,
                       first_publish_date DATE,
                       liked_percent      NUMERIC(5,2),
                       price              NUMERIC(10,2)
);

-- create the authors table

CREATE TABLE authors (
                         author_id   SERIAL PRIMARY KEY,
                         author_name VARCHAR(255) NOT NULL UNIQUE
);

-- create the books_authors table
CREATE TABLE books_authors (
                               book_id   INTEGER NOT NULL,
                               author_id INTEGER NOT NULL,
                               PRIMARY KEY (book_id, author_id),
                               FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
                               FOREIGN KEY (author_id) REFERENCES authors(author_id) ON DELETE CASCADE
);
