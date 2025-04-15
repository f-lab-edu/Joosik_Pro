-- Create dummy data for Joosik_Pro application

-- Insert Members
INSERT INTO member (member_id, name, password, email) VALUES
(1, '유저A', '1234', 'a@email.com');

-- Insert Stocks
INSERT INTO stock (stock_id, company_name, ticker, sector, member_number, article_number) VALUES
(1, 'Tesla', 'TSLA', 'CAR', 0, 0);

-- Insert Posts (SingleStockPost)
INSERT INTO post (article_id, member_id, content, view_count) VALUES
(1, 1, '내용1', 0);

-- Insert SingleStockPost (linking posts to stocks)
INSERT INTO single_stock_post (article_id, stock_id) VALUES
(1, 1);