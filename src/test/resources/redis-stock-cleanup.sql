-- Clean up SingleStockPost first due to foreign key constraint
DELETE FROM single_stock_post WHERE article_id = 1;

-- Delete from Post
DELETE FROM post WHERE article_id = 1;

-- Delete from Stock
DELETE FROM stock WHERE stock_id = 1;

-- Delete from Member
DELETE FROM member WHERE member_id = 1;