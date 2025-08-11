
DELETE FROM first_come_event_participation
WHERE stock_id IN (
    SELECT stock_id FROM stock WHERE ticker IN ('TSLA', 'AAPL', 'NVDA')
);

DELETE FROM stock
WHERE ticker IN ('TSLA', 'AAPL', 'NVDA');

DELETE FROM member
WHERE member_id BETWEEN 1 AND 500;