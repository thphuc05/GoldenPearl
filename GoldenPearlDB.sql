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

-- Insert NhanVien
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV001', N'Nguyễn Văn Hòa', '0901234567', '123456789012', N'NHAN_VIEN', 1, 'TK001');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV002', N'Trần Thiên Bảo', '0386314739', '987654321098', N'NHAN_VIEN', 1, 'TK002');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV003', N'Nguyễn Công Bình', '0123456789', '987654321092', N'NHAN_VIEN', 1, 'TK003');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV004', N'Trần Thiên Phúc', '0987654321', '987654321093', N'NHAN_VIEN', 1, 'TK004');

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
INSERT INTO KhachHang (maKH, tenKH, soDT, email) VALUES ('KH002', N'Phạm Minh Hoàng', '0912345678', 'hoangpm@gmail.com');
INSERT INTO KhachHang (maKH, tenKH, soDT, email) VALUES ('KH003', N'Nguyễn Thùy Linh', '0988777666', 'linhnt@gmail.com');
INSERT INTO KhachHang (maKH, tenKH, soDT, email) VALUES ('KH002', N'Nguyễn Văn A', '0909123456', 'nguyenvana@gmail.com');
INSERT INTO KhachHang (maKH, tenKH, soDT, email) VALUES ('KH003', N'Trần Thị B', '0911222333', 'tranthib@gmail.com');
INSERT INTO KhachHang (maKH, tenKH, soDT, email) VALUES ('KH004', N'Phạm Văn D', '0933444555', 'phamvand@gmail.com');

-- Insert LoaiSanPham
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP01', N'Món chính', N'Các món chính');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP02', N'Đồ uống', N'Nước giải khát');

-- Insert SanPham (All 12 products with giaGoc and giaBan)
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP001', N'Bò Wagyu nướng', 250000, 500000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP002', N'Rượu Vang Đỏ', 600000, 1200000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP003', N'Súp Bào Ngư Vi Cá', 400000, 850000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP004', N'Tôm Hùm Bỏ Lò Phô Mai', 700000, 1250000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP005', N'Cua Hoàng Đế Hấp', 1500000, 2500000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP006', N'Gan Ngỗng Pháp Áp Chảo', 350000, 650000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP007', N'Cơm Chiên Hải Sản', 180000, 350000, 1, 'LSP01');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP008', N'Rượu Vang Trắng', 750000, 1500000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP009', N'Nước Ép Trái Cây', 40000, 85000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP010', N'Trà Hoa Cúc', 30000, 65000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP011', N'Cà Phê Muối', 35000, 75000, 1, 'LSP02');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP012', N'Cocktail Signature', 90000, 185000, 1, 'LSP02');

-- Insert KhuyenMai
INSERT INTO KhuyenMai (maKM, tenKM, phanTramGiam, ngayBatDau, ngayKetThuc) VALUES ('KM001', N'Khai trương', 10, '2024-01-01', '2026-12-31');

-- ---------------------------------------------------------
-- HISTORICAL DATA (Last 7 days for Dashboard)
-- ---------------------------------------------------------

-- 2026-04-22
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD001', '2026-04-22', '12:00:00', 1000000, 1, 'NV001', 'KH001');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD001', 'SP001', 2, 500000, 1000000);

-- 2026-04-23
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD002', '2026-04-23', '18:30:00', 1700000, 1, 'NV002', 'KH002');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD002', 'SP001', 1, 500000, 500000);
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD002', 'SP002', 1, 1200000, 1200000);

-- 2026-04-24
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD003', '2026-04-24', '19:00:00', 2500000, 1, 'NV001', 'KH003');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD003', 'SP005', 1, 2500000, 2500000);

-- 2026-04-25
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD004', '2026-04-25', '13:00:00', 850000, 1, 'NV001', 'KH002');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD004', 'SP003', 1, 850000, 850000);

-- 2026-04-26
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD005', '2026-04-26', '20:00:00', 2450000, 1, 'NV002', 'KH001');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD005', 'SP002', 1, 1200000, 1200000);
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD005', 'SP004', 1, 1250000, 1250000);

-- 2026-04-27
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD006', '2026-04-27', '12:30:00', 1000000, 1, 'NV001', 'KH002');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD006', 'SP001', 2, 500000, 1000000);

-- 2026-04-28 (Today)
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD007', '2026-04-28', '10:00:00', 500000, 1, 'NV001', 'KH003');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD007', 'SP001', 1, 500000, 500000);

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
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP001', 3, 500000, N'', 1500000);

-- [auto] 2026-05-01 18:21:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP003', 2, 850000, N'', 1700000);

-- [auto] 2026-05-01 18:21:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP006', 2, 650000, N'', 1300000);

-- [auto] 2026-05-01 18:21:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP011', 2, 75000, N'', 150000);

-- [auto] 2026-05-01 18:21:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP008', 2, 1500000, N'', 3000000);

-- [auto] 2026-05-01 18:21:44
UPDATE HoaDon SET tongTien = 8150000 WHERE maHD = 'HD008';

-- [auto] 2026-05-01 18:21:47
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-01 18:22:09
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD008', 'SP004', 2, 1250000, N'', 2500000);

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
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD009', 'SP001', 4, 500000, N'', 2000000);

-- [auto] 2026-05-01 18:24:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD009', 'SP005', 4, 2500000, N'', 10000000);

-- [auto] 2026-05-01 18:24:43
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD009', 'SP006', 3, 650000, N'', 1950000);

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
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD009', 'SP006', 3, 650000, N'', 1950000);

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
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD010', 'SP007', 5, 350000, N'', 1750000);

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
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD011', 'SP005', 1, 2500000, N'', 2500000);

-- [auto] 2026-05-02 01:22:23
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD011', 'SP010', 1, 65000, N'', 65000);

-- [auto] 2026-05-02 01:22:23
UPDATE HoaDon SET tongTien = 3065000 WHERE maHD = 'HD011';

-- [auto] 2026-05-02 09:51:35
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH009', N'Hồ Quang Linh', '0777333111');

-- [auto] 2026-05-02 09:51:35
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES ('DDB005', '2026-05-02 09:51:35', '2026-05-07 00:00:00', 4, 'KH009', 0, 'NV002', 'B003', 'SANG');

-- [auto] 2026-05-02 09:51:35
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES ('HD012', '2026-05-02 09:51:35', '09:51:35', 500000, 0, 'DDB005', 'NV002', 'KH009', NULL, 500000);

-- [auto] 2026-05-02 09:51:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP001', 2, 500000, N'', 1000000);

-- [auto] 2026-05-02 09:51:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP005', 2, 2500000, N'', 5000000);

-- [auto] 2026-05-02 09:51:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP006', 2, 650000, N'', 1300000);

-- [auto] 2026-05-02 09:51:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD012', 'SP007', 2, 350000, N'', 700000);

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
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD013', 'SP013', 3, 45000, N'', 135000);

-- [auto] 2026-05-02 19:02:39
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD013', 'SP005', 2, 2500000, N'', 5000000);

-- [auto] 2026-05-02 19:02:39
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD013', 'SP003', 2, 850000, N'', 1700000);

-- [auto] 2026-05-02 19:02:39
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD013', 'SP004', 2, 1250000, N'', 2500000);

-- [auto] 2026-05-02 19:02:39
UPDATE HoaDon SET tongTien = 9835000 WHERE maHD = 'HD013';

