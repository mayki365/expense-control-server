INSERT INTO invoice(id, name, description) VALUES (1, 'Cherry', 'Cherry on top');
INSERT INTO invoice(id, name) VALUES (2, 'Apple');
INSERT INTO invoice(id, name) VALUES (3, 'Banana');
ALTER SEQUENCE invoice_seq RESTART WITH 4;