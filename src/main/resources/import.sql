INSERT INTO transaction(id, name, description) VALUES (1, 'Cherry', 'Cherry on top');
INSERT INTO transaction(id, name) VALUES (2, 'Apple');
INSERT INTO transaction(id, name) VALUES (3, 'Banana');
ALTER SEQUENCE transaction_seq RESTART WITH 4;