DECLARE @BrandConstraintName SYSNAME;
DECLARE @Sql NVARCHAR(MAX);

SELECT TOP 1 @BrandConstraintName = kc.name
FROM sys.key_constraints kc
JOIN sys.tables t
    ON kc.parent_object_id = t.object_id
JOIN sys.schemas s
    ON s.schema_id = t.schema_id
JOIN sys.index_columns ic
    ON ic.object_id = t.object_id
   AND ic.index_id = kc.unique_index_id
JOIN sys.columns c
    ON c.object_id = t.object_id
   AND c.column_id = ic.column_id
WHERE s.name = N'dbo'
  AND t.name = N'brands'
  AND c.name = N'name'
  AND kc.type = 'UQ';

IF @BrandConstraintName IS NOT NULL
BEGIN
    SET @Sql =
        N'ALTER TABLE dbo.brands DROP CONSTRAINT '
        + QUOTENAME(@BrandConstraintName) + N';';

    EXEC sys.sp_executesql @Sql;
END;

ALTER TABLE dbo.brands ALTER COLUMN name NVARCHAR(120) NOT NULL;
ALTER TABLE dbo.categories ALTER COLUMN name NVARCHAR(150) NOT NULL;
ALTER TABLE dbo.products ALTER COLUMN name NVARCHAR(200) NOT NULL;

IF @BrandConstraintName IS NOT NULL
BEGIN
    SET @Sql =
        N'ALTER TABLE dbo.brands ADD CONSTRAINT '
        + QUOTENAME(@BrandConstraintName)
        + N' UNIQUE (name);';

    EXEC sys.sp_executesql @Sql;
END;

UPDATE categories
SET name = CASE slug
    WHEN 'ao-thun' THEN N'Áo thun'
    WHEN 'ao-polo' THEN N'Áo Polo'
    WHEN 'ao-so-mi' THEN N'Áo sơ mi'
    WHEN 'quan-jeans' THEN N'Quần Jeans'
    WHEN 'quan-tay' THEN N'Quần Tây'
    WHEN 'ao-khoac' THEN N'Áo khoác'
    WHEN 'phu-kien' THEN N'Phụ kiện'
    ELSE name
END
WHERE slug IN ('ao-thun', 'ao-polo', 'ao-so-mi', 'quan-jeans', 'quan-tay', 'ao-khoac', 'phu-kien');

UPDATE brands
SET name = CASE slug
    WHEN 'urban-man' THEN N'Urban Man'
    WHEN 'northline' THEN N'Northline'
    WHEN 'form-studio' THEN N'Form Studio'
    ELSE name
END,
description = CASE slug
    WHEN 'urban-man' THEN N'Modern menswear essentials'
    WHEN 'northline' THEN N'Classic everyday menswear'
    WHEN 'form-studio' THEN N'Minimal contemporary clothing'
    ELSE description
END
WHERE slug IN ('urban-man', 'northline', 'form-studio');

UPDATE products
SET name = CASE slug
        WHEN 'ao-thun-essential-den' THEN N'Áo thun Essential đen'
        WHEN 'ao-polo-classic-navy' THEN N'Áo Polo Classic navy'
        WHEN 'ao-so-mi-oxford-trang' THEN N'Áo sơ mi Oxford trắng'
        WHEN 'quan-jeans-slim-xanh-dam' THEN N'Quần Jeans Slim xanh đậm'
        WHEN 'quan-tay-regular-den' THEN N'Quần tây Regular đen'
        WHEN 'ao-khoac-bomber-basic' THEN N'Áo khoác Bomber basic'
        WHEN 'ao-thun-heavyweight-xam' THEN N'Áo thun Heavyweight xám'
        WHEN 'that-lung-da-toi-gian' THEN N'Thắt lưng da tối giản'
        ELSE name
    END,
    description = CASE slug
        WHEN 'ao-thun-essential-den' THEN N'Áo thun nam cơ bản, dễ phối đồ.'
        WHEN 'ao-polo-classic-navy' THEN N'Áo polo nam cổ bẻ phong cách lịch sự.'
        WHEN 'ao-so-mi-oxford-trang' THEN N'Áo sơ mi Oxford phù hợp đi làm và đi chơi.'
        WHEN 'quan-jeans-slim-xanh-dam' THEN N'Quần jeans nam dáng slim hiện đại.'
        WHEN 'quan-tay-regular-den' THEN N'Quần tây nam dáng regular thanh lịch.'
        WHEN 'ao-khoac-bomber-basic' THEN N'Áo khoác bomber nam cho ngày se lạnh.'
        WHEN 'ao-thun-heavyweight-xam' THEN N'Áo thun nam dày dặn, form rộng.'
        WHEN 'that-lung-da-toi-gian' THEN N'Thắt lưng da nam khóa kim loại.'
        ELSE description
    END,
    material = CASE slug
        WHEN 'ao-thun-essential-den' THEN N'Cotton 100%'
        WHEN 'ao-polo-classic-navy' THEN N'Cotton pique'
        WHEN 'ao-so-mi-oxford-trang' THEN N'Oxford cotton'
        WHEN 'quan-jeans-slim-xanh-dam' THEN N'Denim'
        WHEN 'quan-tay-regular-den' THEN N'Poly wool'
        WHEN 'ao-khoac-bomber-basic' THEN N'Polyester'
        WHEN 'ao-thun-heavyweight-xam' THEN N'Heavy cotton'
        WHEN 'that-lung-da-toi-gian' THEN N'Leather'
        ELSE material
    END
WHERE slug IN (
    'ao-thun-essential-den',
    'ao-polo-classic-navy',
    'ao-so-mi-oxford-trang',
    'quan-jeans-slim-xanh-dam',
    'quan-tay-regular-den',
    'ao-khoac-bomber-basic',
    'ao-thun-heavyweight-xam',
    'that-lung-da-toi-gian'
);
