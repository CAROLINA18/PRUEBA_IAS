INSERT INTO pre_approved (id, customer_id, status, available_amount)
SELECT 'PRA-1001', 'USR-10', 'ACTIVE', 1000000.00
WHERE NOT EXISTS (SELECT 1 FROM pre_approved WHERE id = 'PRA-1001');

INSERT INTO pre_approved (id, customer_id, status, available_amount)
SELECT 'PRA-1002', 'USR-10', 'BLOCKED', 800000.00
WHERE NOT EXISTS (SELECT 1 FROM pre_approved WHERE id = 'PRA-1002');

INSERT INTO pre_approved (id, customer_id, status, available_amount)
SELECT 'PRA-2001', 'USR-20', 'ACTIVE', 2000000.00
WHERE NOT EXISTS (SELECT 1 FROM pre_approved WHERE id = 'PRA-2001');

