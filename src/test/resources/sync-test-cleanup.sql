START TRANSACTION;

SET FOREIGN_KEY_CHECKS = 0;

DELETE ssp
FROM single_stock_post AS ssp
JOIN post  AS p ON p.article_id = ssp.article_id
JOIN stock AS s ON s.stock_id   = ssp.stock_id
WHERE p.content IN ('tsla1','aapl1','amzn1','meta1','goog1','nvda1','nflx1','msft1','amd1','intc1')
  AND s.ticker  IN ('TSLA','AAPL','AMZN','META','GOOG','NVDA','NFLX','MSFT','AMD','INTC');

-- 3) post 삭제
DELETE p
FROM post AS p
WHERE p.content IN ('tsla1','aapl1','amzn1','meta1','goog1','nvda1','nflx1','msft1','amd1','intc1');

-- 4) stock 삭제
DELETE s
FROM stock AS s
WHERE s.ticker IN ('TSLA','AAPL','AMZN','META','GOOG','NVDA','NFLX','MSFT','AMD','INTC');

-- 5) member 삭제
DELETE m
FROM member AS m
WHERE m.email IN ('a@email.com','b@email.com','c@email.com','d@email.com','e@email.com',
                  'f@email.com','g@email.com','h@email.com','i@email.com','j@email.com');

ALTER TABLE single_stock_post AUTO_INCREMENT = 1;
ALTER TABLE post             AUTO_INCREMENT = 1;
ALTER TABLE stock            AUTO_INCREMENT = 1;
ALTER TABLE member           AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

COMMIT;