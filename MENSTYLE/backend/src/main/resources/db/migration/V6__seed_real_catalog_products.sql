-- Replace the V4 demo catalog without deleting rows referenced by carts, wishlists or orders.

UPDATE products
SET status = 'INACTIVE',
    updated_at = SYSUTCDATETIME()
WHERE slug IN (
    'ao-thun-essential-den',
    'ao-polo-classic-navy',
    'ao-so-mi-oxford-trang',
    'quan-jeans-slim-xanh-dam',
    'quan-tay-regular-den',
    'ao-khoac-bomber-basic',
    'ao-thun-heavyweight-xam',
    'that-lung-da-toi-gian'
)
AND status <> 'INACTIVE';

IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'menstyle')
BEGIN
    INSERT INTO brands (name, slug, description, status)
    VALUES (N'MENSTYLE', 'menstyle', N'Thương hiệu demo của dự án MENSTYLE.', 'ACTIVE');
END;

IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'quan-kaki')
BEGIN
    INSERT INTO categories (name, slug, description, status)
    VALUES (N'Quần kaki', 'quan-kaki', N'Quần kaki và chinos nam.', 'ACTIVE');
END;

IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'quan-cargo')
BEGIN
    INSERT INTO categories (name, slug, description, status)
    VALUES (N'Quần cargo', 'quan-cargo', N'Quần cargo nam phong cách đường phố.', 'ACTIVE');
END;

ALTER TABLE colors ALTER COLUMN name NVARCHAR(50) NOT NULL;

IF NOT EXISTS (SELECT 1 FROM colors WHERE name = N'Trắng')
    INSERT INTO colors (name, color_code, status) VALUES (N'Trắng', '#FFFFFF', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM colors WHERE name = N'Đen')
    INSERT INTO colors (name, color_code, status) VALUES (N'Đen', '#000000', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM colors WHERE name = N'Xanh')
    INSERT INTO colors (name, color_code, status) VALUES (N'Xanh', '#2563EB', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM colors WHERE name = N'Be')
    INSERT INTO colors (name, color_code, status) VALUES (N'Be', '#D6B98C', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM colors WHERE name = N'Xanh rêu')
    INSERT INTO colors (name, color_code, status) VALUES (N'Xanh rêu', '#556B2F', 'ACTIVE');

INSERT INTO products (
    category_id, brand_id, name, slug, description, material, base_price, sale_price, status, featured
)
SELECT c.id, b.id, v.name, v.slug, v.description, NULL, v.base_price, NULL, 'ACTIVE', v.featured
FROM (VALUES
    (N'Áo thun', 'ao-thun', N'Áo Thun Oversize', 'ao-thun-oversize', N'Áo thun nam form oversize trẻ trung, phong cách tối giản, phù hợp mặc hằng ngày và phối cùng jeans hoặc quần kaki.', 259000, 1),
    (N'Áo Polo', 'ao-polo', N'Áo Polo Basic', 'ao-polo-basic', N'Áo polo nam phong cách basic, thiết kế gọn gàng, phù hợp đi làm, đi học và các hoạt động thường ngày.', 329000, 1),
    (N'Áo sơ mi', 'ao-so-mi', N'Áo Sơ Mi Oxford', 'ao-so-mi-oxford', N'Áo sơ mi Oxford nam có thiết kế thanh lịch, dễ phối với quần jeans, chinos hoặc quần tây.', 449000, 1),
    (N'Áo khoác', 'ao-khoac', N'Áo Khoác Bomber', 'ao-khoac-bomber', N'Áo khoác bomber nam với kiểu dáng hiện đại, phù hợp phong cách năng động và thời trang đường phố.', 699000, 1),
    (N'Quần Jeans', 'quan-jeans', N'Quần Jeans Straight', 'quan-jeans-straight', N'Quần jeans nam dáng straight, kiểu dáng cơ bản, dễ phối với áo thun, polo và sơ mi.', 559000, 0),
    (N'Quần Tây', 'quan-tay', N'Quần Tây Xếp Ly', 'quan-tay-xep-ly', N'Quần tây nam xếp ly theo phong cách thanh lịch, phù hợp phối cùng sơ mi và các trang phục smart casual.', 499000, 0),
    (N'Quần kaki', 'quan-kaki', N'Quần Chinos Slim Fit', 'quan-chinos-slim-fit', N'Quần chinos nam dáng slim fit, kiểu dáng gọn gàng, phù hợp phong cách casual và lịch sự.', 469000, 0),
    (N'Quần cargo', 'quan-cargo', N'Quần Cargo', 'quan-cargo', N'Quần cargo nam với thiết kế túi hộp đặc trưng, phù hợp phong cách đường phố năng động.', 529000, 1)
) v(category_name, category_slug, name, slug, description, base_price, featured)
JOIN categories c ON c.slug = v.category_slug
JOIN brands b ON b.slug = 'menstyle'
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.slug = v.slug);

INSERT INTO product_images (product_id, image_url, is_primary, sort_order)
SELECT p.id, v.image_url, CASE WHEN v.sort_order = 0 THEN 1 ELSE 0 END, v.sort_order
FROM (VALUES
    ('ao-thun-oversize', '/images/products/1.jfif', 0), ('ao-thun-oversize', '/images/products/2.jfif', 1), ('ao-thun-oversize', '/images/products/3.jfif', 2),
    ('ao-polo-basic', '/images/products/4.jfif', 0), ('ao-polo-basic', '/images/products/5.jfif', 1), ('ao-polo-basic', '/images/products/6.jfif', 2),
    ('ao-so-mi-oxford', '/images/products/7.jfif', 0), ('ao-so-mi-oxford', '/images/products/8.jfif', 1), ('ao-so-mi-oxford', '/images/products/9.jfif', 2),
    ('ao-khoac-bomber', '/images/products/10.jfif', 0), ('ao-khoac-bomber', '/images/products/11.jfif', 1), ('ao-khoac-bomber', '/images/products/12.jfif', 2),
    ('quan-jeans-straight', '/images/products/13.jfif', 0), ('quan-jeans-straight', '/images/products/14.jfif', 1), ('quan-jeans-straight', '/images/products/15.jfif', 2),
    ('quan-tay-xep-ly', '/images/products/16.jfif', 0), ('quan-tay-xep-ly', '/images/products/17.jfif', 1), ('quan-tay-xep-ly', '/images/products/18.jfif', 2),
    ('quan-chinos-slim-fit', '/images/products/19.jfif', 0), ('quan-chinos-slim-fit', '/images/products/20.jfif', 1), ('quan-chinos-slim-fit', '/images/products/21.jfif', 2),
    ('quan-cargo', '/images/products/22.jfif', 0), ('quan-cargo', '/images/products/23.jfif', 1), ('quan-cargo', '/images/products/24.jfif', 2)
) v(slug, image_url, sort_order)
JOIN products p ON p.slug = v.slug
WHERE NOT EXISTS (
    SELECT 1 FROM product_images i
    WHERE i.product_id = p.id AND i.image_url = v.image_url
);

INSERT INTO product_variants (product_id, size_id, color_id, sku, price, status)
SELECT p.id, s.id, c.id,
       CONCAT(v.sku_prefix, '-', s.name),
       p.base_price,
       'ACTIVE'
FROM (VALUES
    ('ao-thun-oversize', 'MST-TEE-OVR-WHT', N'Trắng', 'M,L,XL'),
    ('ao-polo-basic', 'MST-POLO-BSC-BLK', N'Đen', 'M,L,XL'),
    ('ao-so-mi-oxford', 'MST-SHIRT-OXF-BLU', N'Xanh', 'M,L,XL'),
    ('ao-khoac-bomber', 'MST-JKT-BOM-BEI', N'Be', 'M,L,XL'),
    ('quan-jeans-straight', 'MST-JEANS-STR-BLU', N'Xanh', '29,30,31,32'),
    ('quan-tay-xep-ly', 'MST-TROU-PLE-BLK', N'Đen', '29,30,31,32'),
    ('quan-chinos-slim-fit', 'MST-CHIN-SLM-BEI', N'Be', '29,30,31,32'),
    ('quan-cargo', 'MST-CARG-BAS-OLV', N'Xanh rêu', 'M,L,XL')
) v(slug, sku_prefix, color_name, size_names)
JOIN products p ON p.slug = v.slug
JOIN colors c ON c.name = v.color_name
JOIN sizes s ON ',' + v.size_names + ',' LIKE '%,' + s.name + ',%'
WHERE NOT EXISTS (
    SELECT 1
    FROM product_variants existing
    WHERE existing.product_id = p.id
      AND existing.size_id = s.id
      AND existing.color_id = c.id
);

INSERT INTO inventories (variant_id, quantity, reserved_quantity, reorder_level)
SELECT v.id, 10, 0, 2
FROM product_variants v
JOIN products p ON p.id = v.product_id
WHERE p.slug IN (
    'ao-thun-oversize', 'ao-polo-basic', 'ao-so-mi-oxford', 'ao-khoac-bomber',
    'quan-jeans-straight', 'quan-tay-xep-ly', 'quan-chinos-slim-fit', 'quan-cargo'
)
AND NOT EXISTS (SELECT 1 FROM inventories i WHERE i.variant_id = v.id);
