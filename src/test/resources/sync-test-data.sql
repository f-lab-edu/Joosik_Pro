-- Insert Members
INSERT INTO member (member_id, name, password, email) VALUES
    (1, '유저A', '1234', 'a@email.com'),
    (2, '유저B', '1234', 'b@email.com'),
    (3, '유저C', '1234', 'c@email.com'),
    (4, '유저D', '1234', 'd@email.com'),
    (5, '유저E', '1234', 'e@email.com'),
    (6, '유저F', '1234', 'f@email.com'),
    (7, '유저G', '1234', 'g@email.com'),
    (8, '유저H', '1234', 'h@email.com'),
    (9, '유저I', '1234', 'i@email.com'),
    (10, '유저J', '1234', 'j@email.com');

-- Insert Stocks
INSERT INTO stock (stock_id, company_name, ticker, sector, member_number, article_number) VALUES
    (1, 'Tesla', 'TSLA', 'CAR', 0, 0),
    (2, 'Apple', 'AAPL', 'IT', 0, 0),
    (3, 'Amazon', 'AMZN', 'IT', 0, 0),
    (4, 'Meta', 'META', 'IT', 0, 0),
    (5, 'Google', 'GOOG', 'IT', 0, 0),
    (6, 'Nvidia', 'NVDA', 'IT', 0, 0),
    (7, 'Netflix', 'NFLX', 'IT', 0, 0),
    (8, 'Microsoft', 'MSFT', 'IT', 0, 0),
    (9, 'AMD', 'AMD', 'IT', 0, 0),
    (10, 'Intel', 'INTC', 'IT', 0, 0);

-- Insert Posts (SingleStockPost)
INSERT INTO post (article_id, member_id, content, view_count) VALUES
    (1, 1, 'tsla1', 0),
    (2, 2, 'aapl1', 0),
    (3, 3, 'amzn1', 0),
    (4, 4, 'meta1', 0),
    (5, 5, 'goog1', 0),
    (6, 6, 'nvda1', 0),
    (7, 7, 'nflx1', 0),
    (8, 8, 'msft1', 0),
    (9, 9, 'amd1', 0),
    (10, 10, 'intc1', 0);

-- Insert SingleStockPost (linking posts to stocks)
INSERT INTO single_stock_post (article_id, stock_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 5),
    (6, 6),
    (7, 7),
    (8, 8),
    (9, 9),
    (10, 10);
