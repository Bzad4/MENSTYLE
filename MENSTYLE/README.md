# MENSTYLE

Website thương mại điện tử thời trang nam, gồm React frontend và Spring Boot REST backend.

## Công nghệ

- Backend: Java 21, Spring Boot, Maven, Spring Web, Spring Data JPA, Spring Security, Bean Validation, SQL Server
- Frontend: React, Vite, React Router, Axios

## Cấu trúc

```text
MENSTYLE/
├── backend/
└── frontend/
```

## Yêu cầu môi trường

- JDK 21
- Maven 3.9+
- Node.js 20+
- Microsoft SQL Server (khi bắt đầu phát triển dữ liệu)

## Chạy backend

Đặt các biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` và `JWT_SECRET`, sau đó:

```powershell
$env:DB_URL="jdbc:sqlserver://DESKTOP-V6KAVF8:1433;databaseName=MENSTYLE;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="menstyle_app"
$env:DB_PASSWORD="<your_password>"
$env:JWT_SECRET="<at_least_32_characters>"
$env:MENSTYLE_CORS_ALLOWED_ORIGINS="http://localhost:5173"

cd backend
mvn spring-boot:run '-Dspring-boot.run.profiles=local'
```

Backend mặc định chạy tại `http://localhost:8080`.

Health check công khai:

```text
GET http://localhost:8080/api/v1/public/health
```

Để kiểm tra health mà không cần SQL Server trong môi trường local:

```powershell
cd backend
mvn spring-boot:run '-Dspring-boot.run.profiles=health'
```

Profile thông thường yêu cầu các biến môi trường `DB_USERNAME`, `DB_PASSWORD` và
`JWT_SECRET` (JWT secret tối thiểu 32 ký tự).
Khi triển khai frontend ở domain khác, cấu hình `MENSTYLE_CORS_ALLOWED_ORIGINS`
thành danh sách origin được phép, phân tách bằng dấu phẩy.

Để tạo admin local/dev lần đầu, đặt thêm `ADMIN_EMAIL`, `ADMIN_PASSWORD` và
`ADMIN_FULL_NAME`. Initializer chỉ tạo admin khi cả email và password được cung
cấp; password không được ghi log và không nằm trong migration.

## Database schema

Backend sử dụng Flyway để quản lý schema SQL Server:

- `V1__create_menstyle_schema.sql`: tạo các bảng, khóa, constraint và index.
- `V2__seed_master_data.sql`: seed role, size, color và category cơ bản.
- `V3__create_password_reset_tokens.sql`: lưu token reset password đã hash.
- `V4__seed_sample_products.sql`: seed dữ liệu sản phẩm mẫu phục vụ catalog và manual validation.
- `V5__fix_sample_product_unicode.sql`: sửa Unicode cho dữ liệu sample của V4
  bằng SQL Server Unicode literals (`N'...'`). Migration không thay đổi schema,
  không sửa hoặc xóa dữ liệu người dùng thật.

`spring.jpa.hibernate.ddl-auto` vẫn giữ ở mức `validate`; JPA không tự tạo hoặc
cập nhật schema. Việc chạy migration thực tế cần SQL Server MENSTYLE và các biến
môi trường database hợp lệ.

## Chạy frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend mặc định chạy tại địa chỉ được Vite hiển thị trong terminal.

## Build

```powershell
cd backend
mvn clean package

cd ..\frontend
npm run build
```

> Cấu hình cơ sở dữ liệu mẫu nằm trong `backend/src/main/resources/application-example.properties`.
> Cấu hình local dùng biến môi trường nằm trong `backend/src/main/resources/application-local.properties`.

## Authentication API

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
- `GET /api/v1/admin/ping` (yêu cầu `ROLE_ADMIN`)

Login trả Bearer JWT có thời hạn mặc định 60 phút. Forgot-password luôn trả
thông báo chung để không tiết lộ email có tồn tại hay không; việc gửi email thật
chưa được tích hợp trong phase này.
