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
    trangThai NVARCHAR(50) DEFAULT N'Đang Làm Việc',
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

-- 7. Table LoaiSanPham
CREATE TABLE LoaiSanPham (
    maDanhMuc VARCHAR(20) PRIMARY KEY,
    tenDanhMuc NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(255)
);

-- 8. Table SanPham (Updated with giaGoc and giaBan)
CREATE TABLE SanPham (
    maMon VARCHAR(20) PRIMARY KEY,
    tenMon NVARCHAR(100) NOT NULL,
    giaGoc FLOAT NOT NULL,
    giaBan FLOAT NOT NULL,
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
    thoiGian TIME DEFAULT CONVERT(TIME, GETDATE()),
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

-- ---------------------------------------------------------
-- SAMPLE DATA (ORIGINAL + NEW)
-- ---------------------------------------------------------

-- Insert TaiKhoan
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK001', 'vanhoa', '123456', 'NHAN_VIEN');
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK002', 'thienbao', '123456', 'NHAN_VIEN');
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK003', 'congbinh', '123456', 'NHAN_VIEN');
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK004', 'thienphuc', '123456', 'NHAN_VIEN');
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK005', 'admin', '123456', 'QUAN_LY');

-- Insert NhanVien
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV001', N'Nguyễn Văn Hòa', '0901234567', '123456789012', N'NHAN_VIEN', N'Đang Làm Việc', 'TK001');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV002', N'Trần Thiên Bảo', '0386314739', '987654321098', N'NHAN_VIEN', N'Đang Làm Việc', 'TK002');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV003', N'Nguyễn Công Bình', '0123456789', '987654321092', N'NHAN_VIEN', N'Đang Làm Việc', 'TK003');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV004', N'Trần Thiên Phúc', '0987654321', '987654321093', N'NHAN_VIEN', N'Đang Làm Việc', 'TK004');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('QL001', N'Nguyễn Thị Quản Lý', '0900000001', '000000000001', N'QUAN_LY', N'Đang Làm Việc', 'TK005');

-- Insert KhuVuc
INSERT INTO KhuVuc (maKV, tenKV, moTa) VALUES ('KV001', N'Thường', N'Khu vực trong nhà');
INSERT INTO KhuVuc (maKV, tenKV, moTa) VALUES ('KV002', N'Vip', N'Khu vực ngoài trời');

-- Insert TinhTrangBan
INSERT INTO TinhTrangBan (maTinhTrang, tenTinhTrang) VALUES ('TRONG', N'Trống');
INSERT INTO TinhTrangBan (maTinhTrang, tenTinhTrang) VALUES ('DANG_SD', N'Đang sử dụng');
INSERT INTO TinhTrangBan (maTinhTrang, tenTinhTrang) VALUES ('DAT_TRUOC', N'Đã đặt trước');

-- Insert Ban (15 bàn Thường + 7 bàn VIP)
-- Bàn Thường: soBan 1-5 → 4 người, 6-10 → 6 người, 11-15 → 8 người
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B001',  1, 4, N'Thường', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B002',  2, 4, N'Thường', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B003',  3, 4, N'Thường', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B004',  4, 4, N'Thường', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B005',  5, 4, N'Thường', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B006',  6, 6, N'Thường', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B007',  7, 6, N'Thường', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B008',  8, 6, N'Thường', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B009',  9, 6, N'Thường', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B010', 10, 6, N'Thường', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B011', 11, 8, N'Thường', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B012', 12, 8, N'Thường', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B013', 13, 8, N'Thường', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B014', 14, 8, N'Thường', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B015', 15, 8, N'Thường', 'KV002', 'TRONG');
-- Bàn VIP: soBan 16-22
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B016', 16,  8, N'VIP', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B017', 17,  8, N'VIP', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B018', 18, 10, N'VIP', 'KV001', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B019', 19, 10, N'VIP', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B020', 20, 10, N'VIP', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B021', 21, 12, N'VIP', 'KV002', 'TRONG');
INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES ('B022', 22, 12, N'VIP', 'KV002', 'TRONG');

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

-- 2026-04-26
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD005', '2026-04-26', '20:00:00', 2450000, 1, 'NV002', 'KH001');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD005', 'SP_BEER03', 1, 1200000, 1200000);
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD005', 'SP_TOM02', 1, 1250000, 1250000);

-- 2026-04-27
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD006', '2026-04-27', '12:30:00', 1000000, 1, 'NV001', 'KH002');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD006', 'SP_TOM01', 2, 500000, 1000000);

-- 2026-04-28 (Today)
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD007', '2026-04-28', '10:00:00', 500000, 1, 'NV001', 'KH003');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD007', 'SP_TOM01', 1, 500000, 500000);

-- ---------------------------------------------------------
-- SCHEMA EXTENSIONS for Booking Feature
-- ---------------------------------------------------------
ALTER TABLE HoaDon ADD tienCoc FLOAT DEFAULT 0;
ALTER TABLE DonDatBan ADD khungGio NVARCHAR(50);

-- [auto] 2026-05-01 18:20:25
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH004', N'Vũ Thị Lành', '0387018496');

-- [auto] 2026-05-01 18:20:25
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB001', '2026-05-01 18:20:25', '2026-05-01 00:00:00', 4, 'KH004', 0, 'NV002', 'B001', 'CHIEU');

-- [auto] 2026-05-01 18:20:25
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD008', '2026-05-01 18:20:25', '18:20:25', 500000, 0, 'DDB001', 'NV002', 'KH004', NULL, 500000);

-- [auto] 2026-05-01 18:20:26
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-01 18:21:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP_TOM01', 3, 500000, N'', 1500000);

-- [auto] 2026-05-01 18:21:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP_LAU02', 2, 850000, N'', 1700000);

-- [auto] 2026-05-01 18:21:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP_CUA01', 2, 650000, N'', 1300000);

-- [auto] 2026-05-01 18:21:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP_NGOT01', 2, 75000, N'', 150000);

-- [auto] 2026-05-01 18:21:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP_BEER01', 2, 1500000, N'', 3000000);

-- [auto] 2026-05-01 18:21:44
UPDATE HoaDon SET tongTien = 8150000 WHERE maHD = 'HD008';

-- [auto] 2026-05-01 18:21:47
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-01 18:22:09
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP_TOM02', 2, 1250000, N'', 2500000);

-- [auto] 2026-05-01 18:22:10
UPDATE HoaDon SET tongTien = 10650000 WHERE maHD = 'HD008';

-- [auto] 2026-05-01 18:23:11
UPDATE HoaDon SET tongTien = 10150000 WHERE maHD = 'HD008';

-- [auto] 2026-05-01 18:23:11
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD008';

-- [auto] 2026-05-01 18:23:11
UPDATE DonDatBan SET thoiGianDat = '2026-05-01 18:20:25', thoiGianDen = '2026-05-01 00:00:00', soLuongKhach = 4, maKH = 'KH004', trangThai = 1, maNV = 'NV002', maBan = 'B001' WHERE maDon = 'DDB001';

-- [auto] 2026-05-01 18:23:11
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-01 18:24:33
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH005', N'Trần Văn Trí', '0708939399');

-- [auto] 2026-05-01 18:24:33
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB002', '2026-05-01 18:24:33', '2026-05-01 00:00:00', 4, 'KH005', 0, 'NV002', 'B001', 'TOI');

-- [auto] 2026-05-01 18:24:33
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD009', '2026-05-01 18:24:33', '18:24:33', 500000, 0, 'DDB002', 'NV002', 'KH005', NULL, 500000);

-- [auto] 2026-05-01 18:24:33
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-01 18:24:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD009', 'SP_TOM01', 4, 500000, N'', 2000000);

-- [auto] 2026-05-01 18:24:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD009', 'SP_CUA02', 4, 2500000, N'', 10000000);

-- [auto] 2026-05-01 18:24:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD009', 'SP_CUA01', 3, 650000, N'', 1950000);

-- [auto] 2026-05-01 18:24:43
UPDATE HoaDon SET tongTien = 14450000 WHERE maHD = 'HD009';

-- [auto] 2026-05-01 18:26:14
DELETE FROM ChiTietHoaDon WHERE maHD = 'HD009';

-- [auto] 2026-05-01 18:26:14
DELETE FROM HoaDon WHERE maHD = 'HD009';

-- [auto] 2026-05-01 18:26:14
DELETE FROM DonDatBan WHERE maDon = 'DDB002';

-- [auto] 2026-05-01 18:26:14
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-01 18:42:22
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH006', N'Trần Tấn An', '0333913817');

-- [auto] 2026-05-01 18:42:22
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB002', '2026-05-01 18:42:22', '2026-05-01 00:00:00', 4, 'KH006', 0, 'NV002', 'B001', 'TOI');

-- [auto] 2026-05-01 18:42:22
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD009', '2026-05-01 18:42:22', '18:42:22', 500000, 0, 'DDB002', 'NV002', 'KH006', NULL, 500000);

-- [auto] 2026-05-01 18:42:22
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-01 18:44:46
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD009', 'SP_CUA01', 3, 650000, N'', 1950000);

-- [auto] 2026-05-01 18:44:48
UPDATE HoaDon SET tongTien = 2450000 WHERE maHD = 'HD009';

-- [migration] thêm cột ghiChu cho DonDatBan
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='DonDatBan' AND COLUMN_NAME='ghiChu')
    ALTER TABLE DonDatBan ADD ghiChu NVARCHAR(500) NULL;

-- [auto] 2026-05-01 18:59:12
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH007', N'ggfbgvb', '0999888777');

-- [auto] 2026-05-01 18:59:12
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB003', '2026-05-01 18:59:12', '2026-05-01 00:00:00', 4, 'KH007', 0, 'NV002', 'B001', 'CHIEU');

-- [auto] 2026-05-01 18:59:12
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD010', '2026-05-01 18:59:12', '18:59:12', 500000, 0, 'DDB003', 'NV002', 'KH007', NULL, 500000);

-- [auto] 2026-05-01 18:59:12
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-01 18:59:30
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD010', 'SP_COM01', 5, 350000, N'', 1750000);

-- [auto] 2026-05-01 18:59:30
UPDATE HoaDon SET tongTien = 2250000 WHERE maHD = 'HD010';

-- [auto] 2026-05-01 18:59:55
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-01 19:00:06
UPDATE HoaDon SET tongTien = 1750000 WHERE maHD = 'HD010';

-- [auto] 2026-05-01 19:00:06
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD010';

-- [auto] 2026-05-01 19:00:06
UPDATE DonDatBan SET thoiGianDat = '2026-05-01 18:59:12', thoiGianDen = '2026-05-01 00:00:00', soLuongKhach = 4, maKH = 'KH007', trangThai = 1, maNV = 'NV002', maBan = 'B001' WHERE maDon = 'DDB003';

-- [auto] 2026-05-01 19:00:06
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-02 00:55:09
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH008', N'Nguyễn Phan Tú Nhi', '0914555739');

-- [auto] 2026-05-02 00:55:09
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB004', '2026-05-02 00:55:09', '2026-05-02 00:00:00', 12, 'KH008', 0, 'NV002', 'B022', 'SANG');

-- [auto] 2026-05-02 00:55:09
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD011', '2026-05-02 00:55:09', '00:55:09', 500000, 0, 'DDB004', 'NV002', 'KH008', NULL, 500000);

-- [auto] 2026-05-02 00:55:09
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B022';

-- [auto] 2026-05-02 01:22:23
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD011', 'SP_CUA02', 1, 2500000, N'', 2500000);

-- [auto] 2026-05-02 01:22:23
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD011', 'SP_SUOI01', 1, 65000, N'', 65000);

-- [auto] 2026-05-02 01:22:23
UPDATE HoaDon SET tongTien = 3065000 WHERE maHD = 'HD011';

-- [auto] 2026-05-02 09:51:35
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH009', N'Hồ Quang Linh', '0777333111');

-- [auto] 2026-05-02 09:51:35
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB005', '2026-05-02 09:51:35', '2026-05-07 00:00:00', 4, 'KH009', 0, 'NV002', 'B003', 'SANG');

-- [auto] 2026-05-02 09:51:35
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD012', '2026-05-02 09:51:35', '09:51:35', 500000, 0, 'DDB005', 'NV002', 'KH009', NULL, 500000);

-- [auto] 2026-05-02 09:51:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_TOM01', 2, 500000, N'', 1000000);

-- [auto] 2026-05-02 09:51:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_CUA02', 2, 2500000, N'', 5000000);

-- [auto] 2026-05-02 09:51:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_CUA01', 2, 650000, N'', 1300000);

-- [auto] 2026-05-02 09:51:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_COM01', 2, 350000, N'', 700000);

-- [auto] 2026-05-02 09:51:54
UPDATE HoaDon SET tongTien = 8500000 WHERE maHD = 'HD012';

-- [auto] 2026-05-02 10:02:22
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B022';

-- [auto] 2026-05-02 10:05:52
UPDATE HoaDon SET tongTien = 2565000 WHERE maHD = 'HD011';

-- [auto] 2026-05-02 10:05:52
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD011';

-- [auto] 2026-05-02 10:05:52
UPDATE DonDatBan SET thoiGianDat = '2026-05-02 00:55:09', thoiGianDen = '2026-05-02 00:00:00', soLuongKhach = 12, maKH = 'KH008', trangThai = 1, maNV = 'NV002', maBan = 'B022' WHERE maDon = 'DDB004';

-- [auto] 2026-05-02 10:05:52
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B022';

-- [auto] 2026-05-02 19:02:24
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH011', N'Trần Tiến Đạt', '0999756234');

-- [auto] 2026-05-02 19:02:24
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB006', '2026-05-02 19:02:24', '2026-05-02 00:00:00', 4, 'KH011', 0, 'NV002', 'B001', 'TOI');

-- [auto] 2026-05-02 19:02:24
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD013', '2026-05-02 19:02:24', '19:02:24', 500000, 0, 'DDB006', 'NV002', 'KH011', NULL, 500000);

-- [auto] 2026-05-02 19:02:25
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-02 19:02:39
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD013', 'SP_KV01', 3, 45000, N'', 135000);

-- [auto] 2026-05-02 19:02:39
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD013', 'SP_CUA02', 2, 2500000, N'', 5000000);

-- [auto] 2026-05-02 19:02:39
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD013', 'SP_LAU02', 2, 850000, N'', 1700000);

-- [auto] 2026-05-02 19:02:39
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD013', 'SP_TOM02', 2, 1250000, N'', 2500000);

-- [auto] 2026-05-02 19:02:39
UPDATE HoaDon SET tongTien = 9835000 WHERE maHD = 'HD013';

-- [auto] 2026-05-02 22:21:28
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-02 22:21:38
UPDATE HoaDon SET tongTien = 9335000 WHERE maHD = 'HD013';

-- [auto] 2026-05-02 22:21:38
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD013';

-- [auto] 2026-05-02 22:21:38
UPDATE DonDatBan SET thoiGianDat = '2026-05-02 19:02:24', thoiGianDen = '2026-05-02 00:00:00', soLuongKhach = 4, maKH = 'KH011', trangThai = 1, maNV = 'NV002', maBan = 'B001' WHERE maDon = 'DDB006';

-- [auto] 2026-05-02 22:21:38
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-05 00:08:00
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH012', N'Nguyễn Xuân Trường', '0456789123');

-- [auto] 2026-05-05 00:08:00
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB007', '2026-05-05 00:08:00', '2026-05-05 00:00:00', 4, 'KH012', 0, 'NV002', 'B001', 'SANG');

-- [auto] 2026-05-05 00:08:00
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD014', '2026-05-05 00:08:00', '00:08:00', 500000, 0, 'DDB007', 'NV002', 'KH012', NULL, 500000);

-- [auto] 2026-05-05 00:08:00
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-05 00:08:12
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD014', 'SP_KV01', 2, 45000, N'', 90000);

-- [auto] 2026-05-05 00:08:12
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD014', 'SP_CUA02', 4, 2500000, N'', 10000000);

-- [auto] 2026-05-05 00:08:12
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD014', 'SP_LAU02', 2, 850000, N'', 1700000);

-- [auto] 2026-05-05 00:08:12
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD014', 'SP_TOM02', 2, 1250000, N'', 2500000);

-- [auto] 2026-05-05 00:08:12
UPDATE HoaDon SET tongTien = 14790000 WHERE maHD = 'HD014';

-- [auto] 2026-05-05 09:19:30
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH013', N'Nguyễn Văn Khải', '0666345123');

-- [auto] 2026-05-05 09:19:30
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB008', '2026-05-05 09:19:30', '2026-05-05 00:00:00', 4, 'KH013', 0, 'NV002', 'B002', 'SANG');

-- [auto] 2026-05-05 09:19:30
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD015', '2026-05-05 09:19:30', '09:19:30', 500000, 0, 'DDB008', 'NV002', 'KH013', NULL, 500000);

-- [auto] 2026-05-05 09:19:30
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B002';

-- [auto] 2026-05-05 09:19:53
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD015', 'SP_CUA02', 1, 2500000, N'', 2500000);

-- [auto] 2026-05-05 09:19:53
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD015', 'SP_LAU02', 1, 850000, N'', 850000);

-- [auto] 2026-05-05 09:19:53
UPDATE HoaDon SET tongTien = 3850000 WHERE maHD = 'HD015';

-- [auto] 2026-05-05 09:44:53
UPDATE KhachHang SET tenKH = N'Nguyễn Phan Tú Nhi', soDT = '0914555739' WHERE maKH = 'KH008';

-- [auto] 2026-05-05 11:07:26
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-05 14:17:24
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH014', N'vbvvbvb', '0999666778');

-- [auto] 2026-05-05 14:17:24
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB009', '2026-05-05 14:17:24', '2026-05-05 00:00:00', 4, 'KH014', 0, 'NV002', 'B001', 'CHIEU');

-- [auto] 2026-05-05 14:17:24
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD016', '2026-05-05 14:17:24', '14:17:24', 500000, 0, 'DDB009', 'NV002', 'KH014', NULL, 500000);

-- [auto] 2026-05-05 14:17:24
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-05 14:17:46
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD016', 'SP_BEER01', 2, 40000, N'', 80000);

-- [auto] 2026-05-05 14:17:46
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD016', 'SP_BEER02', 2, 30000, N'', 60000);

-- [auto] 2026-05-05 14:17:46
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD016', 'SP_BEER03', 2, 45000, N'', 90000);

-- [auto] 2026-05-05 14:17:46
UPDATE HoaDon SET tongTien = 730000 WHERE maHD = 'HD016';

-- [auto] 2026-05-05 18:15:43
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-05 18:16:08
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD016', 'SP_BEER04', 3, 50000, N'', 150000);

-- [auto] 2026-05-05 18:16:08
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD016', 'SP_BEER05', 2, 48000, N'', 96000);

-- [auto] 2026-05-05 18:16:08
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD016', 'SP_CUA05', 3, 520000, N'', 1560000);

-- [auto] 2026-05-05 18:16:08
UPDATE HoaDon SET tongTien = 2536000 WHERE maHD = 'HD016';

-- [auto] 2026-05-05 18:16:13
UPDATE HoaDon SET tongTien = 2036000 WHERE maHD = 'HD016';

-- [auto] 2026-05-05 18:16:13
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD016';

-- [auto] 2026-05-05 18:16:13
UPDATE DonDatBan SET thoiGianDat = '2026-05-05 14:17:24', thoiGianDen = '2026-05-05 00:00:00', soLuongKhach = 4, maKH = 'KH014', trangThai = 1, maNV = 'NV002', maBan = 'B001' WHERE maDon = 'DDB009';

-- [auto] 2026-05-05 18:16:13
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-05 18:17:19
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH015', N'Nguyễn Thảo Hòa', '0987666321');

-- [auto] 2026-05-05 18:17:19
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB010', '2026-05-05 18:17:19', '2026-05-05 00:00:00', 4, 'KH015', 0, 'NV002', 'B001', 'CHIEU');

-- [auto] 2026-05-05 18:17:19
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD017', '2026-05-05 18:17:19', '18:17:19', 500000, 0, 'DDB010', 'NV002', 'KH015', NULL, 500000);

-- [auto] 2026-05-05 18:17:19
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-05 18:17:53
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD017', 'SP_BEER04', 3, 50000, N'', 150000);

-- [auto] 2026-05-05 18:17:53
UPDATE HoaDon SET tongTien = 650000 WHERE maHD = 'HD017';

-- [auto] 2026-05-05 18:17:55
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-05 18:17:58
UPDATE HoaDon SET tongTien = 150000 WHERE maHD = 'HD017';

-- [auto] 2026-05-05 18:17:58
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD017';

-- [auto] 2026-05-05 18:17:58
UPDATE DonDatBan SET thoiGianDat = '2026-05-05 18:17:19', thoiGianDen = '2026-05-05 00:00:00', soLuongKhach = 4, maKH = 'KH015', trangThai = 1, maNV = 'NV002', maBan = 'B001' WHERE maDon = 'DDB010';

-- [auto] 2026-05-05 18:17:58
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-05 18:26:19
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH016', N'Yêu Lắm Cơ', '0808989898');

-- [auto] 2026-05-05 18:26:19
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB011', '2026-05-05 18:26:19', '2026-05-05 00:00:00', 4, 'KH016', 0, 'NV002', 'B002', 'CHIEU');

-- [auto] 2026-05-05 18:26:19
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD018', '2026-05-05 18:26:19', '18:26:19', 1556000, 0, 'DDB011', 'NV002', 'KH016', NULL, 500000);

-- [auto] 2026-05-05 18:26:19
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD018', 'SP_BEER04', 2, 50000, N'', 100000);

-- [auto] 2026-05-05 18:26:19
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD018', 'SP_BEER05', 2, 48000, N'', 96000);

-- [auto] 2026-05-05 18:26:19
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD018', 'SP_COM04', 2, 100000, N'', 200000);

-- [auto] 2026-05-05 18:26:19
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD018', 'SP_COM02', 2, 90000, N'', 180000);

-- [auto] 2026-05-05 18:26:19
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD018', 'SP_KV04', 1, 65000, N'', 65000);

-- [auto] 2026-05-05 18:26:19
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD018', 'SP_MI04', 2, 140000, N'', 280000);

-- [auto] 2026-05-05 18:26:19
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD018', 'SP_MI05', 1, 135000, N'', 135000);

-- [auto] 2026-05-05 18:26:19
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B002';

-- [auto] 2026-05-05 18:26:26
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-05 18:26:34
UPDATE HoaDon SET tongTien = 1056000 WHERE maHD = 'HD018';

-- [auto] 2026-05-05 18:26:34
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD018';

-- [auto] 2026-05-05 18:26:34
UPDATE DonDatBan SET thoiGianDat = '2026-05-05 18:26:19', thoiGianDen = '2026-05-05 00:00:00', soLuongKhach = 4, maKH = 'KH016', trangThai = 1, maNV = 'NV002', maBan = 'B002' WHERE maDon = 'DDB011';

-- [auto] 2026-05-05 18:26:34
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B002';

-- [auto] 2026-05-05 18:36:58
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH017', N'Nguyễn Long', '0777345654');

-- [auto] 2026-05-05 18:36:58
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB012', '2026-05-05 18:36:58', '2026-05-05 00:00:00', 4, 'KH017', 0, 'NV002', 'B001', 'CHIEU');

-- [auto] 2026-05-05 18:36:58
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD019', '2026-05-05 18:36:58', '18:36:58', 500000, 0, 'DDB012', 'NV002', 'KH017', NULL, 500000);

-- [auto] 2026-05-05 18:36:59
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-05 18:37:11
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD019', 'SP_BEER01', 3, 40000, N'', 120000);

-- [auto] 2026-05-05 18:37:11
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD019', 'SP_BEER05', 3, 48000, N'', 144000);

-- [auto] 2026-05-05 18:37:11
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD019', 'SP_BEER07', 2, 45000, N'', 90000);

-- [auto] 2026-05-05 18:37:11
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD019', 'SP_NGOT07', 3, 18000, N'', 54000);

-- [auto] 2026-05-05 18:37:11
UPDATE HoaDon SET tongTien = 908000 WHERE maHD = 'HD019';

-- [auto] 2026-05-05 18:37:18
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD019', 'SP_LAU04', 2, 320000, N'', 640000);

-- [auto] 2026-05-05 18:37:18
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD019', 'SP_LAU05', 1, 420000, N'', 420000);

-- [auto] 2026-05-05 18:37:18
UPDATE HoaDon SET tongTien = 1968000 WHERE maHD = 'HD019';

-- [auto] 2026-05-05 18:37:21
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-05 18:37:26
UPDATE HoaDon SET tongTien = 1468000 WHERE maHD = 'HD019';

-- [auto] 2026-05-05 18:37:26
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD019';

-- [auto] 2026-05-05 18:37:26
UPDATE DonDatBan SET thoiGianDat = '2026-05-05 18:36:58', thoiGianDen = '2026-05-05 00:00:00', soLuongKhach = 4, maKH = 'KH017', trangThai = 1, maNV = 'NV002', maBan = 'B001' WHERE maDon = 'DDB012';

-- [auto] 2026-05-05 18:37:26
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-06 07:53:07
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH018', N'Trần Thiên Bảo', '0123456777');

-- [auto] 2026-05-06 07:53:07
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB013', '2026-05-06 07:53:07', '2026-05-12 00:00:00', 4, 'KH018', 0, 'NV002', 'B001', 'SANG');

-- [auto] 2026-05-06 07:53:07
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD020', '2026-05-06 07:53:07', '07:53:07', 1460000, 0, 'DDB013', 'NV002', 'KH018', NULL, 500000);

-- [auto] 2026-05-06 07:53:07
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD020', 'SP_COM01', 1, 110000, N'', 110000);

-- [auto] 2026-05-06 07:53:07
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD020', 'SP_COM02', 1, 90000, N'', 90000);

-- [auto] 2026-05-06 07:53:07
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD020', 'SP_KV05', 1, 80000, N'', 80000);

-- [auto] 2026-05-06 07:53:07
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD020', 'SP_KV06', 1, 95000, N'', 95000);

-- [auto] 2026-05-06 07:53:07
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD020', 'SP_LAU07', 1, 450000, N'', 450000);

-- [auto] 2026-05-06 07:53:07
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD020', 'SP_MI05', 1, 135000, N'', 135000);

-- [auto] 2026-05-06 07:58:43
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH019', N'Trần Thị Xuân', '0708939392');

-- [auto] 2026-05-06 07:58:43
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB014', '2026-05-06 07:58:43', '2026-05-06 00:00:00', 4, 'KH019', 0, 'NV002', 'B001', 'SANG');

-- [auto] 2026-05-06 07:58:44
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD021', '2026-05-06 07:58:43', '07:58:43', 500000, 0, 'DDB014', 'NV002', 'KH019', NULL, 500000);

-- [auto] 2026-05-06 07:58:44
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-06 09:00:04
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB005', '2026-05-06 09:00:04', '2026-05-06 00:00:00', 4, 'KH008', 0, 'NV001', 'B001', 'SANG');

-- [auto] 2026-05-06 09:00:04
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD012', '2026-05-06 09:00:04', '09:00:04', 500000, 0, 'DDB005', 'NV001', 'KH008', NULL, 500000);

-- [auto] 2026-05-06 09:00:04
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-06 09:07:32
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD021', 'SP_BEER01', 2, 40000, N'', 80000);

-- [auto] 2026-05-06 09:07:32
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD021', 'SP_BEER02', 2, 30000, N'', 60000);

-- [auto] 2026-05-06 09:07:32
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD021', 'SP_BEER05', 2, 48000, N'', 96000);

-- [auto] 2026-05-06 09:07:32
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD021', 'SP_CUA05', 2, 520000, N'', 1040000);

-- [auto] 2026-05-06 09:07:32
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD021', 'SP_CUA06', 1, 500000, N'', 500000);

-- [auto] 2026-05-06 09:07:32
UPDATE HoaDon SET tongTien = 2276000 WHERE maHD = 'HD021';

-- [auto] 2026-05-06 09:24:45
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH020', N'hjm', '0000999988');

-- [auto] 2026-05-06 09:24:45
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB015', '2026-05-06 09:24:45', '2026-05-06 00:00:00', 4, 'KH020', 0, 'NV001', 'B002', 'SANG');

-- [auto] 2026-05-06 09:24:45
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD022', '2026-05-06 09:24:45', '09:24:45', 500000, 0, 'DDB015', 'NV001', 'KH020', NULL, 500000);

-- [auto] 2026-05-06 09:24:45
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B002';

-- [auto] 2026-05-06 12:18:30
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-06 12:18:35
UPDATE HoaDon SET tongTien = 1776000 WHERE maHD = 'HD021';

-- [auto] 2026-05-06 12:18:35
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD021';

-- [auto] 2026-05-06 12:18:35
UPDATE DonDatBan SET thoiGianDat = '2026-05-06 07:58:43', thoiGianDen = '2026-05-06 00:00:00', soLuongKhach = 4, maKH = 'KH019', trangThai = 1, maNV = 'NV002', maBan = 'B001' WHERE maDon = 'DDB014';

-- [auto] 2026-05-06 12:18:35
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-06 23:47:10
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD022', 'SP_BEER03', 1, 45000, N'', 45000);

-- [auto] 2026-05-06 23:47:10
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD022', 'SP_BEER06', 3, 42000, N'', 126000);

-- [auto] 2026-05-06 23:47:10
UPDATE HoaDon SET tongTien = 671000 WHERE maHD = 'HD022';

-- [auto] 2026-05-06 23:47:11
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-06 23:47:12
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-06 23:47:13
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-06 23:47:13
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-06 23:47:13
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-06 23:47:14
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-07 01:03:49
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_BEER02', 1, 30000, N'', 30000);

-- [auto] 2026-05-07 01:03:49
UPDATE HoaDon SET tongTien = 8530000 WHERE maHD = 'HD012';

-- [auto] 2026-05-07 01:40:58
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_BEER05', 2, 48000, N'', 96000);

-- [auto] 2026-05-07 01:40:58
UPDATE HoaDon SET tongTien = 8626000 WHERE maHD = 'HD012';

-- [auto] 2026-05-07 01:41:09
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_MI06', 1, 125000, N'', 125000);

-- [auto] 2026-05-07 01:41:09
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_MI03', 1, 100000, N'', 100000);

-- [auto] 2026-05-07 01:41:09
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_MI02', 1, 130000, N'', 130000);

-- [auto] 2026-05-07 01:41:09
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP_MI01', 1, 120000, N'', 120000);

-- [auto] 2026-05-07 01:41:09
UPDATE HoaDon SET tongTien = 9101000 WHERE maHD = 'HD012';

-- [auto] 2026-05-08 09:24:29
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH021', N'Bao', '0555111222');

-- [auto] 2026-05-08 09:24:29
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB016', '2026-05-08 09:24:29', '2026-05-08 00:00:00', 4, 'KH021', 0, 'QL001', 'B001', 'SANG');

-- [auto] 2026-05-08 09:24:29
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD023', '2026-05-08 09:24:29', '09:24:29', 4000000, 0, 'DDB016', 'QL001', 'KH021', NULL, 500000);

-- [auto] 2026-05-08 09:24:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD023', 'SP_BEER01', 2, 40000, N'', 80000);

-- [auto] 2026-05-08 09:24:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD023', 'SP_BEER02', 2, 30000, N'', 60000);

-- [auto] 2026-05-08 09:24:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD023', 'SP_CUA01', 2, 450000, N'', 900000);

-- [auto] 2026-05-08 09:24:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD023', 'SP_CUA05', 2, 520000, N'', 1040000);

-- [auto] 2026-05-08 09:24:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD023', 'SP_CUA03', 2, 550000, N'', 1100000);

-- [auto] 2026-05-08 09:24:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD023', 'SP_LAU04', 1, 320000, N'', 320000);

-- [auto] 2026-05-08 09:24:29
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-08 09:24:50
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH022', N'hieu', '0333222111');

-- [auto] 2026-05-08 09:24:50
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB017', '2026-05-08 09:24:50', '2026-05-08 00:00:00', 4, 'KH022', 0, 'QL001', 'B002', 'SANG');

-- [auto] 2026-05-08 09:24:50
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD024', '2026-05-08 09:24:50', '09:24:50', 698000, 0, 'DDB017', 'QL001', 'KH022', NULL, 500000);

-- [auto] 2026-05-08 09:24:50
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD024', 'SP_BEER07', 2, 45000, N'', 90000);

-- [auto] 2026-05-08 09:24:50
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD024', 'SP_NGOT07', 2, 18000, N'', 36000);

-- [auto] 2026-05-08 09:24:50
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD024', 'SP_NGOT05', 2, 18000, N'', 36000);

-- [auto] 2026-05-08 09:24:50
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD024', 'SP_NGOT06', 2, 18000, N'', 36000);

-- [auto] 2026-05-08 09:24:50
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B002';

-- [auto] 2026-05-08 09:25:10
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH023', N'phong', '0999444555');

-- [auto] 2026-05-08 09:25:10
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB018', '2026-05-08 09:25:10', '2026-05-08 00:00:00', 4, 'KH023', 0, 'QL001', 'B003', 'SANG');

-- [auto] 2026-05-08 09:25:10
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD025', '2026-05-08 09:25:10', '09:25:10', 500000, 0, 'DDB018', 'QL001', 'KH023', NULL, 500000);

-- [auto] 2026-05-08 09:25:10
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B003';

-- [auto] 2026-05-08 09:25:24
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD025', 'SP_LAU04', 3, 320000, N'', 960000);

-- [auto] 2026-05-08 09:25:24
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD025', 'SP_LAU05', 2, 420000, N'', 840000);

-- [auto] 2026-05-08 09:25:24
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD025', 'SP_LAU03', 2, 340000, N'', 680000);

-- [auto] 2026-05-08 09:25:24
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD025', 'SP_TOM07', 3, 260000, N'', 780000);

-- [auto] 2026-05-08 09:25:24
UPDATE HoaDon SET tongTien = 3760000 WHERE maHD = 'HD025';

-- [auto] 2026-05-08 09:27:29
DELETE FROM ChiTietHoaDon WHERE maHD = 'HD023';

-- [auto] 2026-05-08 09:27:29
DELETE FROM HoaDon WHERE maHD = 'HD023';

-- [auto] 2026-05-08 09:27:29
DELETE FROM DonDatBan WHERE maDon = 'DDB016';

-- [auto] 2026-05-08 09:27:29
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-08 09:28:15
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH024', N'hi', '0666444222');

-- [auto] 2026-05-08 09:28:15
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB019', '2026-05-08 09:28:15', '2026-05-08 00:00:00', 4, 'KH024', 0, 'QL001', 'B001', 'SANG');

-- [auto] 2026-05-08 09:28:15
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD026', '2026-05-08 09:28:15', '09:28:15', 2486000, 0, 'DDB019', 'QL001', 'KH024', NULL, 500000);

-- [auto] 2026-05-08 09:28:15
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD026', 'SP_BEER05', 2, 48000, N'', 96000);

-- [auto] 2026-05-08 09:28:15
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD026', 'SP_COM01', 2, 110000, N'', 220000);

-- [auto] 2026-05-08 09:28:15
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD026', 'SP_COM04', 2, 100000, N'', 200000);

-- [auto] 2026-05-08 09:28:15
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD026', 'SP_CUA02', 2, 650000, N'', 1300000);

-- [auto] 2026-05-08 09:28:15
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD026', 'SP_MI07', 1, 170000, N'', 170000);

-- [auto] 2026-05-08 09:28:15
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-08 09:28:40
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH025', N'hi', '0444777111');

-- [auto] 2026-05-08 09:28:40
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB020', '2026-05-08 09:28:40', '2026-05-08 00:00:00', 4, 'KH025', 0, 'QL001', 'B004', 'SANG');

-- [auto] 2026-05-08 09:28:40
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD027', '2026-05-08 09:28:40', '09:28:40', 500000, 0, 'DDB020', 'QL001', 'KH025', NULL, 500000);

-- [auto] 2026-05-08 09:28:40
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B004';

-- [auto] 2026-05-08 09:28:45
DELETE FROM HoaDon WHERE maHD = 'HD027';

-- [auto] 2026-05-08 09:28:45
DELETE FROM DonDatBan WHERE maDon = 'DDB020';

-- [auto] 2026-05-08 09:28:45
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B004';

-- [auto] 2026-05-08 10:31:13
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-10 13:38:43
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH026', N'Kent', '0984224954');

-- [auto] 2026-05-10 13:38:43
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB020', '2026-05-10 13:38:43', '2026-05-10 00:00:00', 14, 'KH026', 0, NULL, 'SANG', '');

-- [auto] 2026-05-10 13:38:43
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB020', 'B004');

-- [auto] 2026-05-10 13:38:43
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB020', 'B005');

-- [auto] 2026-05-10 13:38:43
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB020', 'B006');

-- [auto] 2026-05-10 13:38:43
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD027', '2026-05-10 13:38:43', '13:38:43', 743000, 0, 'DDB020', NULL, 'KH026', NULL, 500000);

-- [auto] 2026-05-10 13:38:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD027', 'SP_BEER01', 1, 40000, N'', 40000);

-- [auto] 2026-05-10 13:38:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD027', 'SP_BEER02', 2, 30000, N'', 60000);

-- [auto] 2026-05-10 13:38:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD027', 'SP_BEER05', 1, 48000, N'', 48000);

-- [auto] 2026-05-10 13:38:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD027', 'SP_BEER04', 1, 50000, N'', 50000);

-- [auto] 2026-05-10 13:38:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD027', 'SP_BEER07', 1, 45000, N'', 45000);

-- [auto] 2026-05-10 13:38:43
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B004';

-- [auto] 2026-05-10 13:38:43
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B005';

-- [auto] 2026-05-10 13:38:43
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B006';

-- [auto] 2026-05-10 13:40:34
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB021', '2026-05-10 13:40:34', '2026-05-10 00:00:00', 14, 'KH026', 0, NULL, 'SANG', '');

-- [auto] 2026-05-10 13:40:34
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB021', 'B010');

-- [auto] 2026-05-10 13:40:34
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB021', 'B011');

-- [auto] 2026-05-10 13:40:34
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD028', '2026-05-10 13:40:34', '13:40:34', 740000, 0, 'DDB021', NULL, 'KH026', NULL, 500000);

-- [auto] 2026-05-10 13:40:34
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD028', 'SP_BEER03', 4, 45000, N'', 180000);

-- [auto] 2026-05-10 13:40:34
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD028', 'SP_BEER02', 2, 30000, N'', 60000);

-- [auto] 2026-05-10 13:40:34
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B010';

-- [auto] 2026-05-10 13:40:34
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B011';

-- [auto] 2026-05-11 15:46:25
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH027', N'Kent', '0942249548');

-- [auto] 2026-05-11 15:46:25
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB022', '2026-05-11 15:46:25', '2026-05-11 00:00:00', 30, 'KH027', 0, 'QL001', 'CHIEU', 'Ko ăn hành');

-- [auto] 2026-05-11 15:46:25
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB022', 'B020');

-- [auto] 2026-05-11 15:46:25
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB022', 'B019');

-- [auto] 2026-05-11 15:46:25
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB022', 'B018');

-- [auto] 2026-05-11 15:46:25
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD029', '2026-05-11 15:46:25', '15:46:25', 500000, 0, 'DDB022', 'QL001', 'KH027', NULL, 500000);

-- [auto] 2026-05-11 15:46:25
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B020';

-- [auto] 2026-05-11 15:46:25
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B019';

-- [auto] 2026-05-11 15:46:25
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B018';

-- [auto] 2026-05-11 15:46:45
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD029', 'SP_BEER02', 6, 30000, N'', 180000);

-- [auto] 2026-05-11 15:46:45
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD029', 'SP_BEER05', 6, 48000, N'', 288000);

-- [auto] 2026-05-11 15:46:45
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD029', 'SP_COM02', 6, 90000, N'', 540000);

-- [auto] 2026-05-11 15:46:45
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD029', 'SP_COM05', 5, 30000, N'', 150000);

-- [auto] 2026-05-11 15:46:45
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD029', 'SP_COM06', 7, 65000, N'', 455000);

-- [auto] 2026-05-11 15:46:45
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD029', 'SP_CUA05', 3, 520000, N'', 1560000);

-- [auto] 2026-05-11 15:46:45
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD029', 'SP_CUA04', 4, 2800000, N'', 11200000);

-- [auto] 2026-05-11 15:46:45
UPDATE HoaDon SET tongTien = 14873000 WHERE maHD = 'HD029';

-- [auto] 2026-05-11 15:46:59
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B018';

-- [auto] 2026-05-11 15:46:59
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B019';

-- [auto] 2026-05-11 15:46:59
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B020';

-- [auto] 2026-05-11 15:53:21
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH028', N'ahihi', '0356209690');

-- [auto] 2026-05-11 15:53:21
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB023', '2026-05-11 15:53:21', '2026-05-11 00:00:00', 4, 'KH028', 0, 'QL001', 'CHIEU', '');

-- [auto] 2026-05-11 15:53:21
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB023', 'B003');

-- [auto] 2026-05-11 15:53:21
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD030', '2026-05-11 15:53:21', '15:53:21', 794000, 0, 'DDB023', 'QL001', 'KH028', NULL, 500000);

-- [auto] 2026-05-11 15:53:21
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD030', 'SP_BEER06', 7, 42000, N'', 294000);

-- [auto] 2026-05-11 15:53:21
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B003';

-- [auto] 2026-05-11 15:53:25
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B003';

-- [auto] 2026-05-11 17:52:01
UPDATE HoaDon SET tongTien = 14373000 WHERE maHD = 'HD029';

-- [auto] 2026-05-11 17:52:01
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD029';

-- [auto] 2026-05-11 17:52:01
UPDATE DonDatBan SET thoiGianDat = '2026-05-11 15:46:25', thoiGianDen = '2026-05-11 00:00:00', soLuongKhach = 30, maKH = 'KH027', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB022';

-- [auto] 2026-05-11 17:52:01
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B018';

-- [auto] 2026-05-11 17:52:01
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B019';

-- [auto] 2026-05-11 17:52:01
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B020';

-- [auto] 2026-05-11 17:53:15
UPDATE HoaDon SET tongTien = 294000 WHERE maHD = 'HD030';

-- [auto] 2026-05-11 17:53:15
UPDATE HoaDon SET trangThai = 1 WHERE maHD = 'HD030';

-- [auto] 2026-05-11 17:53:15
UPDATE DonDatBan SET thoiGianDat = '2026-05-11 15:53:21', thoiGianDen = '2026-05-11 00:00:00', soLuongKhach = 4, maKH = 'KH028', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB023';

-- [auto] 2026-05-11 17:53:15
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B003';

