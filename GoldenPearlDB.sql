-- Drop database if exists and create new one
USE master;
GO
IF EXISTS (SELECT * FROM sys.databases WHERE name = 'GoldenPearlDB')
BEGIN
    ALTER DATABASE GoldenPearlDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE GoldenPearlDB;
END
GO
CREATE DATABASE GoldenPearlDB;
GO
USE GoldenPearlDB;
GO

-- 1. Table TaiKhoan
CREATE TABLE TaiKhoan (
                          maTK VARCHAR(20) PRIMARY KEY,
                          tenTK NVARCHAR(50) NOT NULL UNIQUE,
                          matKhau NVARCHAR(255) NOT NULL,
                          vaiTro NVARCHAR(50) NOT NULL
);

-- 2. Table NhanVien
CREATE TABLE NhanVien (
                          maNV VARCHAR(20) PRIMARY KEY,
                          tenNV NVARCHAR(100) NOT NULL,
                          soDT VARCHAR(15),
                          soCCCD VARCHAR(20),
                          chucVu NVARCHAR(50),
                          trangThai BIT DEFAULT 1,
                          maTK VARCHAR(20),
                          CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK)
);

-- 3. Table KhuVuc
CREATE TABLE KhuVuc (
                        maKV VARCHAR(20) PRIMARY KEY,
                        tenKV NVARCHAR(100) NOT NULL,
                        moTa NVARCHAR(255)
);

-- 4. Table TinhTrangBan
CREATE TABLE TinhTrangBan (
                              maTinhTrang VARCHAR(20) PRIMARY KEY,
                              tenTinhTrang NVARCHAR(50) NOT NULL
);

-- 5. Table Ban
CREATE TABLE Ban (
                     maBan VARCHAR(20) PRIMARY KEY,
                     soBan INT NOT NULL,
                     sucChua INT,
                     loaiBan NVARCHAR(50),
                     maKV VARCHAR(20),
                     maTinhTrang VARCHAR(20),
                     CONSTRAINT FK_Ban_KhuVuc FOREIGN KEY (maKV) REFERENCES KhuVuc(maKV),
                     CONSTRAINT FK_Ban_TinhTrang FOREIGN KEY (maTinhTrang) REFERENCES TinhTrangBan(maTinhTrang)
);

-- 6. Table KhachHang
CREATE TABLE KhachHang (
                           maKH VARCHAR(20) PRIMARY KEY,
                           tenKH NVARCHAR(100) NOT NULL,
                           soDT VARCHAR(15) NOT NULL,
                           email VARCHAR(100)
);

-- 7. Table LoaiSanPham (Updated to match LoaiSanPham.java: maDanhMuc, tenDanhMuc)
CREATE TABLE LoaiSanPham (
                             maDanhMuc VARCHAR(20) PRIMARY KEY,
                             tenDanhMuc NVARCHAR(100) NOT NULL,
                             moTa NVARCHAR(255)
);

-- 8. Table SanPham (Using names from SanPham.java: maMon, tenMon, donGia, maLoai -> maDanhMuc)
CREATE TABLE SanPham (
                         maMon VARCHAR(20) PRIMARY KEY,
                         tenMon NVARCHAR(100) NOT NULL,
                         donGia FLOAT NOT NULL,
                         moTa NVARCHAR(255),
                         hinhAnh NVARCHAR(255),
                         trangThai BIT DEFAULT 1,
                         maDanhMuc VARCHAR(20),
                         CONSTRAINT FK_SanPham_Loai FOREIGN KEY (maDanhMuc) REFERENCES LoaiSanPham(maDanhMuc)
);

-- 9. Table KhuyenMai
CREATE TABLE KhuyenMai (
                           maKM VARCHAR(20) PRIMARY KEY,
                           tenKM NVARCHAR(100) NOT NULL,
                           phanTramGiam FLOAT NOT NULL,
                           ngayBatDau DATETIME,
                           ngayKetThuc DATETIME
);

-- 10. Table DonDatBan
CREATE TABLE DonDatBan (
                           maDon VARCHAR(20) PRIMARY KEY,
                           thoiGianDat DATETIME DEFAULT GETDATE(),
                           thoiGianDen DATETIME,
                           soLuongKhach INT,
                           maKH VARCHAR(20),
                           trangThai BIT DEFAULT 0,
                           maNV VARCHAR(20),
                           maBan VARCHAR(20),
                           CONSTRAINT FK_DonDatBan_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
                           CONSTRAINT FK_DonDatBan_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
                           CONSTRAINT FK_DonDatBan_Ban FOREIGN KEY (maBan) REFERENCES Ban(maBan)
);

-- 11. Table HoaDon
CREATE TABLE HoaDon (
                        maHD VARCHAR(20) PRIMARY KEY,
                        ngayLap DATETIME DEFAULT GETDATE(),
                        tongTien FLOAT,
                        trangThai BIT DEFAULT 0,
                        maDon VARCHAR(20),
                        maNV VARCHAR(20),
                        maKH VARCHAR(20),
                        maKM VARCHAR(20),
                        CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
                        CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
                        CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM),
                        CONSTRAINT FK_HoaDon_DonDatBan FOREIGN KEY (maDon) REFERENCES DonDatBan(maDon)
);

-- 12. Table ChiTietHoaDon
CREATE TABLE ChiTietHoaDon (
                               maHD VARCHAR(20),
                               maMon VARCHAR(20),
                               soLuong INT NOT NULL,
                               donGia FLOAT NOT NULL,
                               ghiChu NVARCHAR(255),
                               thanhTien FLOAT,
                               PRIMARY KEY (maHD, maMon),
                               CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
                               CONSTRAINT FK_CTHD_SanPham FOREIGN KEY (maMon) REFERENCES SanPham(maMon)
);

-- SAMPLE DATA
-- Insert TaiKhoan first
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK001', 'vanhoa', '123456', 'NHAN_VIEN');
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK002', 'thienbao', '123456', 'NHAN_VIEN');
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK003', 'congbinh', '123456', 'NHAN_VIEN');
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK004', 'thienphuc', '123456', 'NHAN_VIEN');

-- Insert NhanVien
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV001', N'Nguyễn Văn Hòa', '0901234567', '123456789012', N'NHAN_VIEN', 1, 'TK001');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV002', N'Trần Thiên Bảo', '0386314739', '987654321098', N'NHAN_VIEN', 1, 'TK002');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV003', N'Nguyễn Công Bình', '0123456789', '987654321092', N'NHAN_VIEN', 1, 'TK003');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV004', N'Trần Thiên Phúc', '0987654321', '987654321093', N'NHAN_VIEN', 1, 'TK004');

-- Insert KhuVuc
INSERT INTO KhuVuc (maKV, tenKV, moTa) VALUES ('KV001', N'Khu vực A', N'Khu vực trong nhà');
INSERT INTO KhuVuc (maKV, tenKV, moTa) VALUES ('KV002', N'Khu vực B', N'Khu vực ngoài trời');

-- Insert TinhTrangBan
INSERT INTO TinhTrangBan (maTinhTrang, tenTinhTrang) VALUES ('TRONG', N'Trống');
INSERT INTO TinhTrangBan (maTinhTrang, tenTinhTrang) VALUES ('DANG_SD', N'Đang sử dụng');
INSERT INTO TinhTrangBan (maTinhTrang, tenTinhTrang) VALUES ('DAT_TRUOC', N'Đã đặt trước');

-- Insert Ban
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B001', 1, 4, N'Bàn gỗ', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B002', 2, 4, N'Bàn gỗ', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B003', 3, 2, N'Bàn gỗ', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B004', 4, 4, N'Bàn gỗ', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B005', 5, 6, N'Bàn gỗ', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B006', 6, 4, N'Bàn gỗ', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B007', 7, 2, N'Bàn gỗ', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B008', 8, 4, N'Bàn gỗ', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B009', 9, 4, N'Bàn gỗ', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B010', 10, 4, N'Bàn gỗ', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B011', 11, 2, N'Bàn gỗ', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B012', 12, 6, N'Bàn gỗ', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B013', 13, 4, N'Bàn gỗ', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B014', 14, 4, N'Bàn gỗ', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B015', 15, 2, N'Bàn gỗ', 'KV002', 'TRONG');

-- Insert KhachHang
INSERT INTO KhachHang (maKH, tenKH, soDT, email) VALUES ('KH001', N'Lê Văn C', '0888111222', 'levanc@gmail.com');

-- Insert LoaiSanPham
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP01', N'Món chính', N'Các món chính của nhà hàng');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP02', N'Đồ uống', N'Nước giải khát, rượu, bia');

-- Insert SanPham
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP001', N'Bò Wagyu nướng', 500000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP002', N'Rượu Vang Đỏ', 1200000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP003', N'Súp Bào Ngư Vi Cá', 850000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP004', N'Tôm Hùm Bỏ Lò Phô Mai', 1250000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP005', N'Cua Hoàng Đế Hấp Thủy Nhiệt', 2500000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP006', N'Gan Ngỗng Pháp Áp Chảo', 650000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP007', N'Cơm Chiên Hải Sản Cung Đình', 350000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP008', N'Rượu Vang Trắng Sauvignon', 1500000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP009', N'Nước Ép Trái Cây Nhiệt Đới', 85000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP010', N'Trà Hoa Cúc Mật Ong', 65000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP011', N'Cà Phê Muối Golden Pearl', 75000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES ('SP012', N'Cocktail Signature Night', 185000, 1, 'LSP02');

-- Insert KhuyenMai
INSERT INTO KhuyenMai (maKM, tenKM, phanTramGiam, ngayBatDau, ngayKetThuc) VALUES ('KM001', N'Khai trương', 10, '2024-01-01', '2024-12-31');

-- NEW SAMPLE DATA (5 Invoices)
-- Extra Customers
INSERT INTO KhachHang (maKH, tenKH, soDT, email) VALUES ('KH002', N'Phạm Minh Hoàng', '0912345678', 'hoangpm@gmail.com');
INSERT INTO KhachHang (maKH, tenKH, soDT, email) VALUES ('KH003', N'Nguyễn Thùy Linh', '0988777666', 'linhnt@gmail.com');

-- Booking Orders
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan) 
VALUES ('DDB001', '2026-04-20 10:00:00', '2026-04-20 12:00:00', 4, 'KH001', 1, 'NV001', 'B001');
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan) 
VALUES ('DDB002', '2026-04-21 17:30:00', '2026-04-21 19:00:00', 2, 'KH002', 1, 'NV002', 'B002');
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan) 
VALUES ('DDB003', '2026-04-22 18:00:00', '2026-04-22 20:00:00', 6, 'KH003', 1, 'NV001', 'B005');
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan) 
VALUES ('DDB004', '2026-04-23 11:00:00', '2026-04-23 13:00:00', 3, 'KH001', 1, 'NV003', 'B004');
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan) 
VALUES ('DDB005', '2026-04-24 19:00:00', '2026-04-24 21:00:00', 2, 'KH002', 1, 'NV004', 'B009');

-- 5 Invoices
INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, maDon, maNV, maKH, maKM)
VALUES ('HD001', '2026-04-20 13:45:00', 1550000, 1, 'DDB001', 'NV001', 'KH001', 'KM001');
INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, maDon, maNV, maKH, maKM)
VALUES ('HD002', '2026-04-21 20:15:00', 890000, 1, 'DDB002', 'NV002', 'KH002', NULL);
INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, maDon, maNV, maKH, maKM)
VALUES ('HD003', '2026-04-22 21:30:00', 2450000, 1, 'DDB003', 'NV001', 'KH003', 'KM001');
INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, maDon, maNV, maKH, maKM)
VALUES ('HD004', '2026-04-23 14:20:00', 1200000, 1, 'DDB004', 'NV003', 'KH001', NULL);
INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, maDon, maNV, maKH, maKM)
VALUES ('HD005', '2026-04-24 22:10:00', 3150000, 1, 'DDB005', 'NV004', 'KH002', 'KM001');
