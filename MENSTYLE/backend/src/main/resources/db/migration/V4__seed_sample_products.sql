IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'urban-man')
    INSERT INTO brands (name, slug, description, status) VALUES ('Urban Man', 'urban-man', 'Modern menswear essentials', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'northline')
    INSERT INTO brands (name, slug, description, status) VALUES ('Northline', 'northline', 'Classic everyday menswear', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'form-studio')
    INSERT INTO brands (name, slug, description, status) VALUES ('Form Studio', 'form-studio', 'Minimal contemporary clothing', 'ACTIVE');

INSERT INTO products (category_id, brand_id, name, slug, description, material, base_price, sale_price, status, featured)
SELECT c.id, b.id, v.name, v.slug, v.description, v.material, v.base_price, v.sale_price, 'ACTIVE', v.featured
FROM (VALUES
 ('Áo thun','urban-man','Áo thun Essential đen','ao-thun-essential-den','Áo thun nam cơ bản, dễ phối đồ.','Cotton 100%',249000,199000,1),
 ('Áo Polo','northline','Áo Polo Classic navy','ao-polo-classic-navy','Áo polo nam cổ bẻ phong cách lịch sự.','Cotton pique',399000,349000,1),
 ('Áo sơ mi','form-studio','Áo sơ mi Oxford trắng','ao-so-mi-oxford-trang','Áo sơ mi Oxford phù hợp đi làm và đi chơi.','Oxford cotton',499000,449000,1),
 ('Quần Jeans','urban-man','Quần Jeans Slim xanh đậm','quan-jeans-slim-xanh-dam','Quần jeans nam dáng slim hiện đại.','Denim',699000,599000,1),
 ('Quần Tây','northline','Quần tây Regular đen','quan-tay-regular-den','Quần tây nam dáng regular thanh lịch.','Poly wool',599000,499000,0),
 ('Áo khoác','form-studio','Áo khoác Bomber basic','ao-khoac-bomber-basic','Áo khoác bomber nam cho ngày se lạnh.','Polyester',799000,699000,1),
 ('Áo thun','northline','Áo thun Heavyweight xám','ao-thun-heavyweight-xam','Áo thun nam dày dặn, form rộng.','Heavy cotton',299000,NULL,0),
 ('Phụ kiện','urban-man','Thắt lưng da tối giản','that-lung-da-toi-gian','Thắt lưng da nam khóa kim loại.','Leather',349000,299000,0)
) v(category_name,brand_slug,name,slug,description,material,base_price,sale_price,featured)
JOIN categories c ON c.name = v.category_name
JOIN brands b ON b.slug = v.brand_slug
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.slug = v.slug);

INSERT INTO product_images (product_id, image_url, is_primary, sort_order)
SELECT p.id, CONCAT('https://images.example.com/menstyle/', p.slug, '.jpg'), 1, 0
FROM products p
WHERE p.slug IN ('ao-thun-essential-den','ao-polo-classic-navy','ao-so-mi-oxford-trang','quan-jeans-slim-xanh-dam','quan-tay-regular-den','ao-khoac-bomber-basic','ao-thun-heavyweight-xam','that-lung-da-toi-gian')
  AND NOT EXISTS (SELECT 1 FROM product_images i WHERE i.product_id = p.id);

INSERT INTO product_variants (product_id, size_id, color_id, sku, price, status)
SELECT p.id, s.id, c.id, CONCAT(UPPER(LEFT(REPLACE(p.slug, '-', ''), 16)), '-', s.name, '-', UPPER(LEFT(c.name, 3))),
       p.base_price, 'ACTIVE'
FROM products p
JOIN sizes s ON s.name IN ('M','L')
JOIN colors c ON c.name IN ('Black','Navy')
WHERE p.slug IN ('ao-thun-essential-den','ao-polo-classic-navy','ao-so-mi-oxford-trang','quan-jeans-slim-xanh-dam','quan-tay-regular-den','ao-khoac-bomber-basic','ao-thun-heavyweight-xam')
  AND NOT EXISTS (
      SELECT 1 FROM product_variants v
      WHERE v.product_id = p.id AND v.size_id = s.id AND v.color_id = c.id
  );

INSERT INTO inventories (variant_id, quantity, reserved_quantity, reorder_level)
SELECT v.id, CASE WHEN v.sku LIKE '%-M-%' THEN 20 ELSE 8 END, 0, 5
FROM product_variants v
WHERE NOT EXISTS (SELECT 1 FROM inventories i WHERE i.variant_id = v.id);
