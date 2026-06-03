Dockerizing the Project with PostgreSQL
tart container

nerdctl -compose up -d
Connect to database

nerdctl exec -it library-db psql -U admin -d library
Verify version

SELECT version();
List databases
\l
Stop container

nerdctl-compose down
Test the connection between the Java application and PostgreSQL

mvn spring-boot:run

Module 4: Designing the Database Schema and Implementing Full-Text Search
Create network
rancher network create db-network
Connect library-db to the network
rancher network connect db-network library-db
Setup PGAdmin

nerdctl pull dpage/pgadmin4
and run the container

nerdctl run -p 80:80 -e PGADMIN_DEFAULT_EMAIL=user@domain.com -e PGADMIN_DEFAULT_PASSWORD=SuperSecret --name pgadmin --network db-network -d dpage/pgadmin4

nerdctl run -p 80:80 -e PGADMIN_DEFAULT_EMAIL=user@domain.com -e PGADMIN_DEFAULT_PASSWORD=SuperSecret --name pgadmin --network book-search_default -d dpage/pgadmin4
Visit http://localhost:80 in your browser

Create tables and relationships

nerdctl cp db/create_schema.sql library-db:/create_schema.sql
nerdctl exec -it library-db psql -U admin -d library -f /create_schema.sql

Some Basic queries:-
INSERT INTO books (title, rating, description, language, isbn, book_format, edition, pages, publisher, publish_date, first_publish_date, liked_percent, price)
VALUES
('Introduction to Algorithms', 4.5, 'A comprehensive update of the leading algorithms text, with new material on matchings in bipartite graphs, online algorithms, machine learning, and other topics.', 'English', '9780262046305', 'Hardcover', '4th Edition', 1312, 'MIT Press', '2022-04-05', '1990-01-01', 89.75, 89.99),
('Clean Code: A Handbook of Agile Software Craftsmanship', 4.4, 'Even bad code can function. But if code isn''t clean, it can bring a development organization to its knees. This book is a must for any developer, software engineer, project manager, team lead, or systems analyst with an interest in producing better code.', 'English', '9780132350884', 'Paperback', '1st Edition', 464, 'Prentice Hall', '2008-08-11', '2008-08-11', 87.50, 49.99),
('Design Patterns: Elements of Reusable Object-Oriented Software', 4.2, 'Capturing a wealth of experience about the design of object-oriented software, four top-notch designers present a catalog of simple and succinct solutions to commonly occurring design problems.', 'English', '9780201633610', 'Hardcover', '1st Edition', 395, 'Addison-Wesley Professional', '1994-10-31', '1994-10-31', 83.75, 59.99),
('The Pragmatic Programmer: Your Journey to Mastery', 4.4, 'The Pragmatic Programmer is one of those rare tech books you''ll read, re-read, and read again over the years. Whether you''re new to the field or an experienced practitioner, you''ll come away with fresh insights each and every time.', 'English', '9780135957059', 'Paperback', '20th Anniversary Edition', 352, 'Addison-Wesley Professional', '2019-09-13', '1999-10-30', 87.25, 39.99);

ALTER TABLE books ADD COLUMN search_vector tsvector;

UPDATE books SET search_vector =
    setweight(to_tsvector('english', coalesce(title,'')), 'A') ||
    setweight(to_tsvector('english', coalesce(description,'')), 'B') ||
    setweight(to_tsvector('english', coalesce(isbn,'')), 'C');


-- Example 1: to_tsvector
SELECT to_tsvector('english', 'Introduction to Algorithms');

-- Example 2: to_tsvector with a longer text
SELECT to_tsvector('english', 'A comprehensive update of the leading algorithms text, with new material on matchings in bipartite graphs, online algorithms, machine learning, and other topics.');

-- Example 3: to_tsquery with a single word
SELECT to_tsquery('english', 'algorithms');

-- Example 4: to_tsquery with multiple words and operators
SELECT to_tsquery('english', 'algorithms & (graphs | learning)');

-- Example 5: plainto_tsquery
SELECT plainto_tsquery('english', 'introduction to algorithms');

-- Example 6: Comparing to_tsquery and plainto_tsquery
SELECT to_tsquery('english', 'clean & code'), plainto_tsquery('english', 'clean code');

-- Example 7: Using to_tsquery with prefix matching
SELECT to_tsquery('english', 'algorithm:*');

-- Example 8: Demonstrating normalization and stop word removal
SELECT to_tsvector('english', 'The Algorithms are running quickly and efficiently');


-- Example 1a: Basic search using to_tsquery with a single term
SELECT title, ts_rank(search_vector, to_tsquery('english', 'algorithms')) as rank
FROM books
WHERE search_vector @@ to_tsquery('english', 'algorithms')
ORDER BY rank DESC;

-- Example 1b: Search using to_tsquery with multiple terms (OR)
SELECT title, ts_rank(search_vector, to_tsquery('english', 'algorithms | design')) as rank
FROM books
WHERE search_vector @@ to_tsquery('english', 'algorithms | design')
ORDER BY rank DESC;

-- Example 1c: Search using to_tsquery with multiple terms (AND)
SELECT title, ts_rank(search_vector, to_tsquery('english', 'algorithms & learning')) as rank
FROM books
WHERE search_vector @@ to_tsquery('english', 'algorithms & learning')
ORDER BY rank DESC;

-- Example 1d: Search using to_tsquery with prefix matching
SELECT title, ts_rank(search_vector, to_tsquery('english', 'algorithm:*')) as rank
FROM books
WHERE search_vector @@ to_tsquery('english', 'algorithm:*')
ORDER BY rank DESC;

-- Example 2: Phrase search using phraseto_tsquery
SELECT title, ts_rank(search_vector, phraseto_tsquery('english', 'object-oriented software')) as rank
FROM books
WHERE search_vector @@ phraseto_tsquery('english', 'object-oriented software')
ORDER BY rank DESC;

-- Example 3: Natural language search using plainto_tsquery
SELECT title, ts_rank(search_vector, plainto_tsquery('english', 'clean code software development')) as rank
FROM books
WHERE search_vector @@ plainto_tsquery('english', 'clean code software development')
ORDER BY rank DESC;

-- Example 4: Combining fields in search
SELECT title, ts_rank(search_vector, to_tsquery('english', 'programmer & (mastery | craftsmanship)')) as rank
FROM books
WHERE search_vector @@ to_tsquery('english', 'programmer & (mastery | craftsmanship)')
ORDER BY rank DESC;

-- Example 5: Searching with ISBN
SELECT title, ts_rank(search_vector, plainto_tsquery('english', '9780135957059')) as rank
FROM books
WHERE search_vector @@ plainto_tsquery('english', '9780135957059')
ORDER BY rank DESC;

-- Example 6: Complex query with multiple terms
SELECT title, ts_rank(search_vector, to_tsquery('english', 'design & (patterns | algorithms) & !pragmatic')) as rank
FROM books
WHERE search_vector @@ to_tsquery('english', 'design & (patterns | algorithms) & !pragmatic')
ORDER BY rank DESC;
