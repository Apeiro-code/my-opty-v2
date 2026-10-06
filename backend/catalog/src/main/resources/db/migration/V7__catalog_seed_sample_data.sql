-- Sample catalog data
-- Band: V2-V99
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.
--
-- Seed rows so the team can build and demo features against realistic data instead
-- of an empty table. This is sample data, not fixtures: it is safe to delete, and a
-- feature test must not depend on a specific id — look rows up by model, name or
-- slug. Ids stay AUTO_INCREMENT on purpose, so a database that already holds rows
-- cannot collide with this file.

INSERT INTO category (name, slug, item_type) VALUES
    ('Men',         'men',          'FRAME'),
    ('Women',       'women',        'FRAME'),
    ('Kids',        'kids',         'FRAME'),
    ('Single Vision', 'single-vision', 'LENS'),
    ('Bifocal',     'bifocal',      'LENS'),
    ('Progressive', 'progressive',  'LENS');

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Astra 2100', 'Matte Black', 'Acetate', 'Round', 4500.00, 12, 5,
       '/images/frames/astra-2100.jpg',
       'Lightweight round acetate frame with spring hinges.', TRUE
FROM category WHERE slug = 'men';

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Astra 2100', 'Tortoise', 'Acetate', 'Round', 4500.00, 3, 5,
       '/images/frames/astra-2100-tortoise.jpg',
       'Lightweight round acetate frame with spring hinges.', TRUE
FROM category WHERE slug = 'men';

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Kestrel 04', 'Gunmetal', 'Stainless Steel', 'Rectangular', 6800.00, 8, 5,
       '/images/frames/kestrel-04.jpg',
       'Thin stainless rectangular frame for a business look.', TRUE
FROM category WHERE slug = 'men';

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Meridian 12', 'Champagne', 'Titanium', 'Cat Eye', 9200.00, 6, 5,
       '/images/frames/meridian-12.jpg',
       'Titanium cat-eye frame, flexible and corrosion resistant.', TRUE
FROM category WHERE slug = 'women';

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Meridian 12', 'Rose Gold', 'Titanium', 'Cat Eye', 9200.00, 2, 5,
       '/images/frames/meridian-12-rose.jpg',
       'Titanium cat-eye frame, flexible and corrosion resistant.', TRUE
FROM category WHERE slug = 'women';

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Willow 08', 'Plum', 'TR-90', 'Oval', 3800.00, 15, 5,
       '/images/frames/willow-08.jpg',
       'Flex TR-90 oval frame that survives being sat on.', TRUE
FROM category WHERE slug = 'women';

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Comet Kids', 'Blue', 'TR-90', 'Round', 2500.00, 20, 5,
       '/images/frames/comet-kids.jpg',
       'Rounded kids frame with grippy temple tips.', TRUE
FROM category WHERE slug = 'kids';

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Comet Kids', 'Pink', 'TR-90', 'Round', 2500.00, 4, 5,
       '/images/frames/comet-kids-pink.jpg',
       'Rounded kids frame with grippy temple tips.', TRUE
FROM category WHERE slug = 'kids';

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Falcon 330', 'Matte Grey', 'Acetate', 'Square', 5400.00, 9, 5,
       '/images/frames/falcon-330.jpg',
       'Square acetate frame with a subtle matte finish.', TRUE
FROM category WHERE slug = 'men';

INSERT INTO frame (category_id, model, color, material, shape, price, stock_qty,
                   low_stock_threshold, image_url, description, is_active)
SELECT id, 'Falcon 330', 'Clear', 'Acetate', 'Square', 5400.00, 0, 5,
       NULL,
       'Discontinued clear colourway, kept for order history.', FALSE
FROM category WHERE slug = 'men';

INSERT INTO lens (category_id, name, type, coating, price, stock_qty,
                  low_stock_threshold, description, is_active)
SELECT id, 'Essential 1.50 Single Vision', 'SINGLE_VISION', 'Anti-reflective', 3500.00,
       30, 5,
       'Standard 1.50 index single vision lens with hard coating.', TRUE
FROM category WHERE slug = 'single-vision';

INSERT INTO lens (category_id, name, type, coating, price, stock_qty,
                  low_stock_threshold, description, is_active)
SELECT id, 'Thin 1.60 Single Vision', 'SINGLE_VISION', 'Blue-light filter', 5800.00,
       3, 5,
       '1.60 index single vision for higher prescriptions, blue-light filter.', TRUE
FROM category WHERE slug = 'single-vision';

INSERT INTO lens (category_id, name, type, coating, price, stock_qty,
                  low_stock_threshold, description, is_active)
SELECT id, 'Essential 1.50 Bifocal', 'BIFOCAL', 'Anti-reflective', 5200.00,
       14, 5,
       'Flat-top bifocal with a 28 mm segment.', TRUE
FROM category WHERE slug = 'bifocal';

INSERT INTO lens (category_id, name, type, coating, price, stock_qty,
                  low_stock_threshold, description, is_active)
SELECT id, 'Comfort Progressive 1.60', 'PROGRESSIVE', 'Anti-reflective', 12500.00,
       7, 5,
       'Wide-corridor progressive for all-day wear.', TRUE
FROM category WHERE slug = 'progressive';

INSERT INTO lens (category_id, name, type, coating, price, stock_qty,
                  low_stock_threshold, description, is_active)
SELECT id, 'Premium Progressive 1.67', 'PROGRESSIVE', 'Photochromic', 16800.00,
       2, 5,
       'Thin progressive that darkens outdoors.', TRUE
FROM category WHERE slug = 'progressive';

INSERT INTO lens (category_id, name, type, coating, price, stock_qty,
                  low_stock_threshold, description, is_active)
SELECT id, 'Drive Progressive 1.60', 'PROGRESSIVE', 'Polarised', 14200.00,
       6, 5,
       'Progressive with polarised tint for driving.', TRUE
FROM category WHERE slug = 'progressive';
