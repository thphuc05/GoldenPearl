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
INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES ('TK005', 'admin', '123456', 'QUAN_LY');

-- Insert NhanVien
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV001', N'Nguyễn Văn Hòa', '0901234567', '123456789012', N'NHAN_VIEN', 1, 'TK001');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV002', N'Trần Thiên Bảo', '0386314739', '987654321098', N'NHAN_VIEN', 1, 'TK002');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV003', N'Nguyễn Công Bình', '0123456789', '987654321092', N'NHAN_VIEN', 1, 'TK003');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('NV004', N'Trần Thiên Phúc', '0987654321', '987654321093', N'NHAN_VIEN', 1, 'TK004');
INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES ('QL001', N'Nguyễn Thị Quản Lý', '0900000001', '000000000001', N'QUAN_LY', 1, 'TK005');

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

-- Insert LoaiSanPham
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP_KV',   N'Khai vị',      N'Món ăn khai vị');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP_COM',  N'Cơm',          N'Các món cơm');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP_MI',   N'Mì xào',       N'Các món mì xào');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP_LAU',  N'Lẩu',          N'Các loại lẩu');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP_TOM',  N'Tôm',          N'Các món tôm');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP_CUA',  N'Cua',          N'Các món cua');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP_BEER', N'Beer',         N'Bia các loại');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP_NGOT', N'Ngọt có gas', N'Nước ngọt có gas');
INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES ('LSP_SUOI', N'Suối',         N'Nước khoáng');

-- Insert SanPham
-- Khai vị
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_KV01', N'Ngô chiên bơ',     30000,  55000, 1, 'LSP_KV');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_KV02', N'Khoai lang chiên', 25000,  45000, 1, 'LSP_KV');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_KV03', N'Khoai tây chiên',  20000,  40000, 1, 'LSP_KV');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_KV04', N'Sụn gà chiên',     35000,  65000, 1, 'LSP_KV');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_KV05', N'Nem hải sản',      45000,  80000, 1, 'LSP_KV');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_KV06', N'Gỏi ngó sen tôm thịt', 55000, 95000, 1, 'LSP_KV');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_KV07', N'Salad rong biển', 40000, 75000, 1, 'LSP_KV');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_KV08', N'Chả giò tôm cua', 50000, 90000, 1, 'LSP_KV');
-- Cơm
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_COM01', N'Cơm chiên hải sản',    60000, 110000, 1, 'LSP_COM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_COM02', N'Cơm chiên Dương Châu', 50000,  90000, 1, 'LSP_COM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_COM03', N'Cơm bò hầm',           70000, 130000, 1, 'LSP_COM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_COM04', N'Cơm chiên cá mặn', 55000, 100000, 1, 'LSP_COM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_COM05', N'Cơm trắng niêu', 15000, 30000, 1, 'LSP_COM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_COM06', N'Cơm chiên tỏi', 35000, 65000, 1, 'LSP_COM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_COM07', N'Cơm cháy kho quẹt', 45000, 85000, 1, 'LSP_COM');
-- Mì xào
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_MI01', N'Mì xào hải sản',     65000, 120000, 1, 'LSP_MI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_MI02', N'Mì xào ốc móng tay', 70000, 130000, 1, 'LSP_MI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_MI03', N'Mì xào hoa quả',     55000, 100000, 1, 'LSP_MI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_MI04', N'Mì xào bò',           75000, 140000, 1, 'LSP_MI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_MI05', N'Mì xào giòn hải sản', 70000, 135000, 1, 'LSP_MI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_MI06', N'Mì Ý sốt bò băm', 65000, 125000, 1, 'LSP_MI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_MI07', N'Miến xào cua', 90000, 170000, 1, 'LSP_MI');
-- Lẩu
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_LAU01', N'Lẩu thái',     150000, 280000, 1, 'LSP_LAU');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_LAU02', N'Lẩu hải sản',  200000, 380000, 1, 'LSP_LAU');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_LAU03', N'Lẩu ếch',      180000, 340000, 1, 'LSP_LAU');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_LAU04', N'Lẩu tôm thái', 170000, 320000, 1, 'LSP_LAU');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_LAU05', N'Lẩu cá đuối',  220000, 420000, 1, 'LSP_LAU');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_LAU06', N'Lẩu nấm chim câu', 250000, 480000, 1, 'LSP_LAU');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_LAU07', N'Lẩu riêu cua bắp bò', 230000, 450000, 1, 'LSP_LAU');
-- Tôm
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_TOM01', N'Tôm sú',       120000, 220000, 1, 'LSP_TOM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_TOM02', N'Tôm hùm xanh', 350000, 650000, 1, 'LSP_TOM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_TOM03', N'Tôm mũ ni',    200000, 380000, 1, 'LSP_TOM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_TOM04', N'Tôm tít cháy tỏi', 250000, 480000, 1, 'LSP_TOM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_TOM05', N'Tôm nướng muối ớt', 130000, 240000, 1, 'LSP_TOM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_TOM06', N'Tôm hấp nước dừa', 125000, 230000, 1, 'LSP_TOM');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_TOM07', N'Tôm sốt hoàng kim', 140000, 260000, 1, 'LSP_TOM');
-- Cua
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_CUA01', N'Cua thịt',      250000, 450000, 1, 'LSP_CUA');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_CUA02', N'Cua gạch',      350000, 650000, 1, 'LSP_CUA');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_CUA03', N'Cua càng xanh', 300000, 550000, 1, 'LSP_CUA');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_CUA04', N'Cua hoàng đế (kg)', 1500000, 2800000, 1, 'LSP_CUA');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_CUA05', N'Cua rang me', 280000, 520000, 1, 'LSP_CUA');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_CUA06', N'Cua hấp bia', 270000, 500000, 1, 'LSP_CUA');
-- Beer
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_BEER01', N'Tiger',    20000, 40000, 1, 'LSP_BEER');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_BEER02', N'Saigon',   15000, 30000, 1, 'LSP_BEER');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_BEER03', N'Heineken', 22000, 45000, 1, 'LSP_BEER');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_BEER04', N'Budweiser', 25000, 50000, 1, 'LSP_BEER');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_BEER05', N'Sapporo', 24000, 48000, 1, 'LSP_BEER');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_BEER06', N'Beck''s', 21000, 42000, 1, 'LSP_BEER');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_BEER07', N'Strongbow', 23000, 45000, 1, 'LSP_BEER');
-- Ngọt có gas
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_NGOT01', N'7Up',       8000, 18000, 1, 'LSP_NGOT');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_NGOT02', N'Pepsi',     8000, 18000, 1, 'LSP_NGOT');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_NGOT03', N'Coca Cola', 8000, 18000, 1, 'LSP_NGOT');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_NGOT04', N'Sting',     9000, 20000, 1, 'LSP_NGOT');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_NGOT05', N'Mirinda Cam', 8000, 18000, 1, 'LSP_NGOT');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_NGOT06', N'Sprite', 8000, 18000, 1, 'LSP_NGOT');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_NGOT07', N'Fanta', 8000, 18000, 1, 'LSP_NGOT');
-- Suối
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_SUOI01', N'Aquafina',   6000, 12000, 1, 'LSP_SUOI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_SUOI02', N'LaVie',      6000, 12000, 1, 'LSP_SUOI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_SUOI03', N'Number One', 7000, 15000, 1, 'LSP_SUOI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_SUOI04', N'Dasani', 6000, 12000, 1, 'LSP_SUOI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_SUOI05', N'Evian', 25000, 45000, 1, 'LSP_SUOI');
INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES ('SP_SUOI06', N'Vĩnh Hảo', 7000, 14000, 1, 'LSP_SUOI');

-- Insert KhuyenMai
INSERT INTO KhuyenMai (maKM, tenKM, phanTramGiam, ngayBatDau, ngayKetThuc) VALUES ('KM001', N'Khai trương', 10, '2024-01-01', '2026-12-31');

-- ---------------------------------------------------------
-- HISTORICAL DATA (Last 7 days for Dashboard)
-- ---------------------------------------------------------

-- 2026-04-22
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD001', '2026-04-22', '12:00:00', 1000000, 1, 'NV001', 'KH001');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD001', 'SP_TOM01', 2, 500000, 1000000);

-- 2026-04-23
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD002', '2026-04-23', '18:30:00', 1700000, 1, 'NV002', 'KH002');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD002', 'SP_TOM01', 1, 500000, 500000);
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD002', 'SP_BEER03', 1, 1200000, 1200000);

-- 2026-04-24
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD003', '2026-04-24', '19:00:00', 2500000, 1, 'NV001', 'KH003');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD003', 'SP_CUA02', 1, 2500000, 2500000);

-- 2026-04-25
INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maNV, maKH) VALUES ('HD004', '2026-04-25', '13:00:00', 850000, 1, 'NV001', 'KH002');
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, thanhTien) VALUES ('HD004', 'SP_LAU02', 1, 850000, 850000);

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

-- [auto] 2026-05-18 13:40:59
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH029', N'Kent', '0987654322');

-- [auto] 2026-05-18 13:40:59
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB024', '2026-05-18 13:40:59', '2026-05-18 00:00:00', 8, 'KH029', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-18 13:40:59
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB024', 'B003');

-- [auto] 2026-05-18 13:40:59
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB024', 'B004');

-- [auto] 2026-05-18 13:40:59
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD031','2026-05-18 13:40:59','13:40:59',500000,N'Đã cọc',NULL,'DDB024','QL001','KH029',NULL,500000,'CA001');

-- [auto] 2026-05-18 13:40:59
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B003';

-- [auto] 2026-05-18 13:40:59
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B004';

-- [auto] 2026-05-18 13:41:47
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD031', 'SP_BEER02', 5, 30000, N'', 150000);

-- [auto] 2026-05-18 13:41:47
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD031', 'SP_BEER05', 7, 48000, N'', 336000);

-- [auto] 2026-05-18 13:41:47
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD031', 'SP_COM01', 4, 110000, N'', 440000);

-- [auto] 2026-05-18 13:41:47
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD031', 'SP_COM04', 4, 100000, N'', 400000);

-- [auto] 2026-05-18 13:41:47
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD031', 'SP_COM05', 1, 30000, N'', 30000);

-- [auto] 2026-05-18 13:41:47
UPDATE HoaDon SET tongTien=1856000 WHERE maHD='HD031';

-- [auto] 2026-05-18 13:41:49
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B003';

-- [auto] 2026-05-18 13:41:49
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B004';

-- [auto] 2026-05-18 13:42:22
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD031';

-- [auto] 2026-05-18 13:43:06
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=NULL WHERE maHD='HD031';

-- [auto] 2026-05-18 13:43:16
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=NULL WHERE maHD='HD031';

-- [auto] 2026-05-18 13:43:45
UPDATE HoaDon SET tongTien=1356000 WHERE maHD='HD031';

-- [auto] 2026-05-18 13:43:45
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD031';

-- [auto] 2026-05-18 13:43:45
UPDATE DonDatBan SET thoiGianDat = '2026-05-18 13:40:59', thoiGianDen = '2026-05-18 00:00:00', soLuongKhach = 8, maKH = 'KH029', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB024';

-- [auto] 2026-05-18 13:43:45
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B003';

-- [auto] 2026-05-18 13:43:45
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B004';

-- [auto] 2026-05-18 13:47:56
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH030', N'Kent', '0967546456');

-- [auto] 2026-05-18 13:47:56
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB025', '2026-05-18 13:47:56', '2026-05-18 00:00:00', 10, 'KH030', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-18 13:47:56
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB025', 'B018');

-- [auto] 2026-05-18 13:47:56
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD032','2026-05-18 13:47:56','13:47:56',500000,N'Đã cọc',NULL,'DDB025','QL001','KH030',NULL,500000,'CA001');

-- [auto] 2026-05-18 13:47:56
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B018';

-- [auto] 2026-05-18 13:48:12
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=NULL WHERE maHD='HD032';

-- [auto] 2026-05-18 13:48:23
UPDATE HoaDon SET trangThaiThanhToan=N'Đã hủy', hinhThucThanhToan=NULL WHERE maHD='HD032';

-- [auto] 2026-05-18 13:57:14
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD032';

-- [auto] 2026-05-18 13:57:25
UPDATE HoaDon SET trangThaiThanhToan=N'Đã hủy', hinhThucThanhToan=NULL WHERE maHD='HD032';

-- [auto] 2026-05-18 13:58:13
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD032';

-- [auto] 2026-05-18 13:58:23
UPDATE HoaDon SET trangThaiThanhToan=N'Đã hủy', hinhThucThanhToan=NULL WHERE maHD='HD032';

-- [auto] 2026-05-18 13:58:47
UPDATE HoaDon SET trangThaiThanhToan=N'Đã hủy', hinhThucThanhToan=NULL WHERE maHD='HD031';

-- [auto] 2026-05-18 13:59:41
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD032';

-- [auto] 2026-05-18 14:01:28
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=NULL WHERE maHD='HD031';

-- [auto] 2026-05-18 14:09:04
UPDATE HoaDon SET trangThaiThanhToan=N'Đã cọc', hinhThucThanhToan=NULL WHERE maHD='HD031';

-- [auto] 2026-05-18 14:09:08
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD031';

-- [auto] 2026-05-18 14:09:11
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=NULL WHERE maHD='HD031';

-- [auto] 2026-05-18 14:09:18
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=NULL WHERE maHD='HD031';

-- [auto] 2026-05-18 14:09:34
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD032', 'SP_BEER04', 15, 50000, N'', 750000);

-- [auto] 2026-05-18 14:09:34
UPDATE HoaDon SET tongTien=1250000 WHERE maHD='HD032';

-- [auto] 2026-05-18 14:09:37
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B018';

-- [auto] 2026-05-18 14:09:40
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B018';

-- [auto] 2026-05-18 14:09:44
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B018';

-- [auto] 2026-05-18 14:09:44
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B018';

-- [auto] 2026-05-18 14:09:48
DELETE FROM ChiTietHoaDon WHERE maHD = 'HD032';

-- [auto] 2026-05-18 14:09:48
DELETE FROM HoaDon WHERE maHD='HD032';

-- [auto] 2026-05-18 14:09:48
DELETE FROM ChiTietDatBan WHERE maDonDatBan = 'DDB025';

-- [auto] 2026-05-18 14:09:48
DELETE FROM DonDatBan WHERE maDon = 'DDB025';

-- [auto] 2026-05-18 14:09:48
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B018';

-- [auto] 2026-05-18 14:15:05
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH031', N'Kent', '0897987987');

-- [auto] 2026-05-18 14:15:05
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB025', '2026-05-18 14:15:05', '2026-05-18 00:00:00', 4, 'KH031', 0, 'QL001', 'CHIEU', '');

-- [auto] 2026-05-18 14:15:05
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB025', 'B003');

-- [auto] 2026-05-18 14:15:05
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD032','2026-05-18 14:15:05','14:15:05',500000,N'Đã cọc',NULL,'DDB025','QL001','KH031',NULL,500000,'CA002');

-- [auto] 2026-05-18 14:15:05
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B003';

-- [auto] 2026-05-18 19:06:29
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH032', N'Kent', '0984225653');

-- [auto] 2026-05-18 19:06:29
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB026', '2026-05-18 19:06:29', '2026-05-18 00:00:00', 4, 'KH032', 0, 'QL001', 'TOI', '');

-- [auto] 2026-05-18 19:06:29
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB026', 'B004');

-- [auto] 2026-05-18 19:06:29
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD033','2026-05-18 19:06:29','19:06:29',3200000,N'Đã cọc',NULL,'DDB026','QL001','KH032',NULL,500000,'CA002');

-- [auto] 2026-05-18 19:06:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD033', 'SP_CUA01', 6, 450000, N'', 2700000);

-- [auto] 2026-05-18 19:06:29
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B004';

-- [auto] 2026-05-18 19:07:23
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD033', 'SP_BEER03', 6, 45000, N'', 270000);

-- [auto] 2026-05-18 19:07:23
UPDATE HoaDon SET tongTien=3470000 WHERE maHD='HD033';

-- [auto] 2026-05-18 19:08:16
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Tiền mặt' WHERE maHD='HD033';

-- [auto] 2026-05-18 19:09:49
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD033', 'SP_BEER06', 3, 42000, N'', 126000);

-- [auto] 2026-05-18 19:09:49
UPDATE HoaDon SET tongTien=3596000 WHERE maHD='HD033';

-- [auto] 2026-05-18 19:14:03
INSERT INTO CaLam (maCa,tenCa,gioBatDau,gioKetThuc,trangThai) VALUES ('CA004',N'Ca CA004','08:00:00','23:00:00',0);

-- [auto] 2026-05-19 10:36:32
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH033', N'Kent', '0984231312');

-- [auto] 2026-05-19 10:36:32
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB027', '2026-05-19 10:36:32', '2026-05-19 00:00:00', 4, 'KH033', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 10:36:32
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB027', 'B001');

-- [auto] 2026-05-19 10:36:32
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD034','2026-05-19 10:36:32','10:36:32',500000,N'Đã cọc',NULL,'DDB027','QL001','KH033',NULL,500000,'CA001');

-- [auto] 2026-05-19 10:36:32
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B001';

-- [auto] 2026-05-19 10:37:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD034', 'SP_BEER01', 8, 40000, N'', 320000);

-- [auto] 2026-05-19 10:37:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD034', 'SP_COM01', 7, 110000, N'', 770000);

-- [auto] 2026-05-19 10:37:54
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD034', 'SP_CUA04', 6, 2800000, N'', 16800000);

-- [auto] 2026-05-19 10:37:54
UPDATE HoaDon SET tongTien=18390000 WHERE maHD='HD034';

-- [auto] 2026-05-19 10:37:58
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B001';

-- [auto] 2026-05-19 10:38:19
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Tiền mặt' WHERE maHD='HD034';

-- [auto] 2026-05-19 10:38:34
UPDATE HoaDon SET tongTien=17890000 WHERE maHD='HD034';

-- [auto] 2026-05-19 10:38:34
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Tiền mặt' WHERE maHD='HD034';

-- [auto] 2026-05-19 10:38:34
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 10:36:32', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH033', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB027';

-- [auto] 2026-05-19 10:38:34
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B001';

-- [auto] 2026-05-19 10:39:16
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH034', N'Kent', '0987987987');

-- [auto] 2026-05-19 10:39:16
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB028', '2026-05-19 10:39:16', '2026-05-19 00:00:00', 4, 'KH034', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 10:39:16
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB028', 'B002');

-- [auto] 2026-05-19 10:39:16
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD035','2026-05-19 10:39:16','10:39:16',500000,N'Đã cọc',NULL,'DDB028','QL001','KH034',NULL,500000,'CA001');

-- [auto] 2026-05-19 10:39:16
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B002';

-- [auto] 2026-05-19 10:41:11
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH035', N'Kent', '0987654321');

-- [auto] 2026-05-19 10:41:11
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB029', '2026-05-19 10:41:11', '2026-05-19 00:00:00', 4, 'KH035', 0, 'QL001', 'CHIEU', '');

-- [auto] 2026-05-19 10:41:11
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB029', 'B002');

-- [auto] 2026-05-19 10:41:11
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD036','2026-05-19 10:41:11','10:41:11',500000,N'Đã cọc',NULL,'DDB029','QL001','KH035',NULL,500000,'CA001');

-- [auto] 2026-05-19 10:41:11
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B002';

-- [auto] 2026-05-19 10:41:39
UPDATE HoaDon SET trangThaiThanhToan=N'Đã hủy', hinhThucThanhToan=NULL WHERE maHD='HD036';

-- [auto] 2026-05-19 10:41:39
DELETE FROM ChiTietDatBan WHERE maDonDatBan = 'DDB029';

-- [auto] 2026-05-19 10:41:39
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B002';

-- [auto] 2026-05-19 10:43:33
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH036', N'Kent', '0985324234');

-- [auto] 2026-05-19 10:43:33
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB030', '2026-05-19 10:43:33', '2026-05-19 00:00:00', 4, 'KH036', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 10:43:33
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB030', 'B004');

-- [auto] 2026-05-19 10:43:33
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD037','2026-05-19 10:43:33','10:43:33',500000,N'Đã cọc',NULL,'DDB030','QL001','KH036',NULL,500000,'CA001');

-- [auto] 2026-05-19 10:43:33
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B004';

-- [auto] 2026-05-19 10:44:34
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB031', '2026-05-19 10:44:34', '2026-05-19 00:00:00', 4, 'KH026', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 10:44:34
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB031', 'B005');

-- [auto] 2026-05-19 10:44:34
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD038','2026-05-19 10:44:34','10:44:34',7300000,N'Đã cọc',NULL,'DDB031','QL001','KH026',NULL,500000,'CA001');

-- [auto] 2026-05-19 10:44:34
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD038', 'SP_COM02', 4, 90000, N'', 360000);

-- [auto] 2026-05-19 10:44:34
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD038', 'SP_CUA04', 2, 2800000, N'', 5600000);

-- [auto] 2026-05-19 10:44:34
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD038', 'SP_LAU05', 2, 420000, N'', 840000);

-- [auto] 2026-05-19 10:44:34
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B005';

-- [auto] 2026-05-19 10:45:06
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB032', '2026-05-19 10:45:06', '2026-05-19 00:00:00', 4, 'KH026', 0, 'QL001', 'CHIEU', '');

-- [auto] 2026-05-19 10:45:06
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB032', 'B002');

-- [auto] 2026-05-19 10:45:07
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD039','2026-05-19 10:45:06','10:45:06',500000,N'Đã cọc',NULL,'DDB032','QL001','KH026',NULL,500000,'CA001');

-- [auto] 2026-05-19 10:45:07
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B002';

-- [auto] 2026-05-19 10:47:57
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B005';

-- [auto] 2026-05-19 10:52:37
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD035', 'SP_COM01', 8, 110000, N'', 880000);

-- [auto] 2026-05-19 10:52:37
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD035', 'SP_COM05', 11, 30000, N'', 330000);

-- [auto] 2026-05-19 10:52:37
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD035', 'SP_COM04', 3, 100000, N'', 300000);

-- [auto] 2026-05-19 10:52:37
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD035', 'SP_CUA04', 6, 2800000, N'', 16800000);

-- [auto] 2026-05-19 10:52:37
UPDATE HoaDon SET tongTien=18810000 WHERE maHD='HD035';

-- [auto] 2026-05-19 10:52:49
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-19 10:53:25
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD035';

-- [auto] 2026-05-19 10:53:43
UPDATE HoaDon SET tongTien=18310000 WHERE maHD='HD035';

-- [auto] 2026-05-19 10:53:43
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Tiền mặt' WHERE maHD='HD035';

-- [auto] 2026-05-19 10:53:43
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 10:39:16', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH034', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB028';

-- [auto] 2026-05-19 10:53:43
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B002';

-- [auto] 2026-05-19 10:54:26
UPDATE HoaDon SET tongTien=6800000 WHERE maHD='HD038';

-- [auto] 2026-05-19 10:54:26
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD038';

-- [auto] 2026-05-19 10:54:26
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 10:44:34', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH026', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB031';

-- [auto] 2026-05-19 10:54:26
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B005';

-- [auto] 2026-05-19 10:55:15
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD037', 'SP_CUA04', 7, 2800000, N'', 19600000);

-- [auto] 2026-05-19 10:55:15
UPDATE HoaDon SET tongTien=20100000 WHERE maHD='HD037';

-- [auto] 2026-05-19 10:55:17
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B004';

-- [auto] 2026-05-19 10:56:53
UPDATE HoaDon SET tongTien=19600000 WHERE maHD='HD037';

-- [auto] 2026-05-19 10:56:53
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD037';

-- [auto] 2026-05-19 10:56:53
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 10:43:33', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH036', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB030';

-- [auto] 2026-05-19 10:56:53
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B004';

-- [auto] 2026-05-19 11:05:59
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB033', '2026-05-19 11:05:59', '2026-05-19 00:00:00', 6, 'KH035', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 11:05:59
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB033', 'B010');

-- [auto] 2026-05-19 11:05:59
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD040','2026-05-19 11:05:59','11:05:59',500000,N'Đã cọc',NULL,'DDB033','QL001','KH035',NULL,500000,'CA001');

-- [auto] 2026-05-19 11:05:59
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B010';

-- [auto] 2026-05-19 11:07:26
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD040', 'SP_BEER02', 4, 30000, N'', 120000);

-- [auto] 2026-05-19 11:07:26
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD040', 'SP_COM01', 10, 110000, N'', 1100000);

-- [auto] 2026-05-19 11:07:26
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD040', 'SP_CUA05', 1, 520000, N'', 520000);

-- [auto] 2026-05-19 11:07:26
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD040', 'SP_CUA06', 1, 500000, N'', 500000);

-- [auto] 2026-05-19 11:07:26
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD040', 'SP_CUA03', 1, 550000, N'', 550000);

-- [auto] 2026-05-19 11:07:26
UPDATE HoaDon SET tongTien=3290000 WHERE maHD='HD040';

-- [auto] 2026-05-19 11:07:30
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B010';

-- [auto] 2026-05-19 11:08:05
UPDATE HoaDon SET tongTien=2790000 WHERE maHD='HD040';

-- [auto] 2026-05-19 11:08:05
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD040';

-- [auto] 2026-05-19 11:08:05
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 11:05:59', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 6, maKH = 'KH035', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB033';

-- [auto] 2026-05-19 11:08:05
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B010';

-- [auto] 2026-05-19 11:11:51
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH037', N'Kent', '0984233249');

-- [auto] 2026-05-19 11:11:51
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB034', '2026-05-19 11:11:51', '2026-05-19 00:00:00', 4, 'KH037', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 11:11:51
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB034', 'B004');

-- [auto] 2026-05-19 11:11:51
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD041','2026-05-19 11:11:51','11:11:51',500000,N'Đã cọc',NULL,'DDB034','QL001','KH037',NULL,500000,'CA001');

-- [auto] 2026-05-19 11:11:51
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B004';

-- [auto] 2026-05-19 11:12:36
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD041', 'SP_BEER01', 25, 40000, N'', 1000000);

-- [auto] 2026-05-19 11:12:36
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD041', 'SP_KV01', 1, 55000, N'', 55000);

-- [auto] 2026-05-19 11:12:36
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD041', 'SP_CUA04', 1, 2800000, N'', 2800000);

-- [auto] 2026-05-19 11:12:36
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD041', 'SP_CUA05', 1, 520000, N'', 520000);

-- [auto] 2026-05-19 11:12:36
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD041', 'SP_CUA06', 1, 500000, N'', 500000);

-- [auto] 2026-05-19 11:12:36
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD041', 'SP_LAU04', 1, 320000, N'', 320000);

-- [auto] 2026-05-19 11:12:36
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD041', 'SP_LAU07', 1, 450000, N'', 450000);

-- [auto] 2026-05-19 11:12:36
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD041', 'SP_LAU01', 1, 280000, N'', 280000);

-- [auto] 2026-05-19 11:12:36
UPDATE HoaDon SET tongTien=6425000 WHERE maHD='HD041';

-- [auto] 2026-05-19 11:12:41
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B004';

-- [auto] 2026-05-19 11:13:11
UPDATE HoaDon SET tongTien=5925000 WHERE maHD='HD041';

-- [auto] 2026-05-19 11:13:11
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD041';

-- [auto] 2026-05-19 11:13:11
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 11:11:51', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH037', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB034';

-- [auto] 2026-05-19 11:13:11
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B004';

-- [auto] 2026-05-19 10:06:43
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB035', '2026-05-19 10:06:43', '2026-05-19 00:00:00', 4, 'KH026', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 10:06:43
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB035', 'B002');

-- [auto] 2026-05-19 10:06:43
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD042','2026-05-19 10:06:43','10:06:43',500000,N'Đã cọc',NULL,'DDB035','QL001','KH026',NULL,500000,'CA001');

-- [auto] 2026-05-19 10:06:43
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B002';

-- [auto] 2026-05-19 10:07:04
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD042', 'SP_BEER01', 20, 40000, N'', 800000);

-- [auto] 2026-05-19 10:07:04
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD042', 'SP_COM03', 4, 130000, N'', 520000);

-- [auto] 2026-05-19 10:07:04
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD042', 'SP_COM02', 2, 90000, N'', 180000);

-- [auto] 2026-05-19 10:07:04
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD042', 'SP_COM01', 1, 110000, N'', 110000);

-- [auto] 2026-05-19 10:07:04
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD042', 'SP_COM04', 1, 100000, N'', 100000);

-- [auto] 2026-05-19 10:07:04
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD042', 'SP_COM05', 1, 30000, N'', 30000);

-- [auto] 2026-05-19 10:07:04
UPDATE HoaDon SET tongTien=2240000 WHERE maHD='HD042';

-- [auto] 2026-05-19 10:07:19
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-19 10:16:29
UPDATE HoaDon SET tongTien=1740000 WHERE maHD='HD042';

-- [auto] 2026-05-19 10:16:29
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Tiền mặt' WHERE maHD='HD042';

-- [auto] 2026-05-19 10:16:29
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 10:06:43', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH026', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB035';

-- [auto] 2026-05-19 10:16:29
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B002';

-- [auto] 2026-05-19 13:06:12
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB036', '2026-05-19 13:06:12', '2026-05-19 00:00:00', 4, 'KH026', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 13:06:12
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB036', 'B005');

-- [auto] 2026-05-19 13:06:12
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD043','2026-05-19 13:06:12','13:06:12',500000,N'Đã cọc',NULL,'DDB036','QL001','KH026',NULL,500000,'CA001');

-- [auto] 2026-05-19 13:06:12
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B005';

-- [auto] 2026-05-19 13:06:42
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD043', 'SP_BEER02', 7, 30000, N'', 210000);

-- [auto] 2026-05-19 13:06:42
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD043', 'SP_BEER01', 30, 40000, N'', 1200000);

-- [auto] 2026-05-19 13:06:42
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD043', 'SP_COM02', 8, 90000, N'', 720000);

-- [auto] 2026-05-19 13:06:42
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD043', 'SP_CUA04', 2, 2800000, N'', 5600000);

-- [auto] 2026-05-19 13:06:42
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD043', 'SP_CUA05', 2, 520000, N'', 1040000);

-- [auto] 2026-05-19 13:06:42
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD043', 'SP_LAU04', 2, 320000, N'', 640000);

-- [auto] 2026-05-19 13:06:42
UPDATE HoaDon SET tongTien=9910000 WHERE maHD='HD043';

-- [auto] 2026-05-19 13:06:51
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B005';

-- [auto] 2026-05-19 13:13:37
UPDATE ChiTietHoaDon SET soLuong = 10, thanhTien = 300000 WHERE maHD = 'HD043' AND maMon = 'SP_BEER02';

-- [auto] 2026-05-19 13:13:37
UPDATE HoaDon SET tongTien=10000000 WHERE maHD='HD043';

-- [auto] 2026-05-19 13:13:45
UPDATE HoaDon SET tongTien=9500000 WHERE maHD='HD043';

-- [auto] 2026-05-19 13:13:45
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD043';

-- [auto] 2026-05-19 13:13:45
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 13:06:12', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH026', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB036';

-- [auto] 2026-05-19 13:13:45
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B005';

-- [auto] 2026-05-19 13:17:31
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB037', '2026-05-19 13:17:31', '2026-05-19 00:00:00', 4, 'KH026', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 13:17:31
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB037', 'B005');

-- [auto] 2026-05-19 13:17:31
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD044','2026-05-19 13:17:31','13:17:31',500000,N'Đã cọc',NULL,'DDB037','QL001','KH026',NULL,500000,'CA001');

-- [auto] 2026-05-19 13:17:31
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B005';

-- [auto] 2026-05-19 13:18:00
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD044', 'SP_BEER03', 30, 45000, N'', 1350000);

-- [auto] 2026-05-19 13:18:00
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD044', 'SP_BEER06', 20, 42000, N'', 840000);

-- [auto] 2026-05-19 13:18:00
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD044', 'SP_COM03', 10, 130000, N'', 1300000);

-- [auto] 2026-05-19 13:18:00
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD044', 'SP_COM06', 10, 65000, N'', 650000);

-- [auto] 2026-05-19 13:18:00
UPDATE HoaDon SET tongTien=4640000 WHERE maHD='HD044';

-- [auto] 2026-05-19 13:18:48
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B005';

-- [auto] 2026-05-19 13:18:52
UPDATE HoaDon SET tongTien=4140000 WHERE maHD='HD044';

-- [auto] 2026-05-19 13:18:52
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD044';

-- [auto] 2026-05-19 13:18:52
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 13:17:31', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH026', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB037';

-- [auto] 2026-05-19 13:18:52
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B005';

-- [auto] 2026-05-19 13:38:26
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB038', '2026-05-19 13:38:26', '2026-05-19 00:00:00', 4, 'KH026', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 13:38:26
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB038', 'B005');

-- [auto] 2026-05-19 13:38:26
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD045','2026-05-19 13:38:26','13:38:26',500000,N'Đã cọc',NULL,'DDB038','QL001','KH026',NULL,500000,'CA001');

-- [auto] 2026-05-19 13:38:26
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B005';

-- [auto] 2026-05-19 13:38:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD045', 'SP_BEER02', 9, 30000, N'', 270000);

-- [auto] 2026-05-19 13:38:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD045', 'SP_BEER05', 7, 48000, N'', 336000);

-- [auto] 2026-05-19 13:38:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD045', 'SP_BEER04', 4, 50000, N'', 200000);

-- [auto] 2026-05-19 13:38:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD045', 'SP_BEER07', 4, 45000, N'', 180000);

-- [auto] 2026-05-19 13:38:44
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD045', 'SP_BEER06', 3, 42000, N'', 126000);

-- [auto] 2026-05-19 13:38:44
UPDATE HoaDon SET tongTien=1612000 WHERE maHD='HD045';

-- [auto] 2026-05-19 13:38:50
DELETE FROM ChiTietHoaDon WHERE maHD = 'HD045' AND maMon = 'SP_BEER02';

-- [auto] 2026-05-19 13:38:50
UPDATE HoaDon SET tongTien=842000 WHERE maHD='HD045';

-- [auto] 2026-05-19 13:38:53
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B005';

-- [auto] 2026-05-19 13:39:03
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD045', 'SP_CUA01', 6, 450000, N'', 2700000);

-- [auto] 2026-05-19 13:39:03
UPDATE HoaDon SET tongTien=3542000 WHERE maHD='HD045';

-- [auto] 2026-05-19 13:41:33
UPDATE HoaDon SET tongTien=3542000 WHERE maHD='HD045';

-- [auto] 2026-05-19 13:41:33
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Tiền mặt' WHERE maHD='HD045';

-- [auto] 2026-05-19 13:41:33
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 13:38:26', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH026', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB038';

-- [auto] 2026-05-19 13:41:33
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B005';

-- [auto] 2026-05-19 13:41:57
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB039', '2026-05-19 13:41:57', '2026-05-19 00:00:00', 4, 'KH026', 0, 'QL001', 'SANG', '');

-- [auto] 2026-05-19 13:41:57
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB039', 'B004');

-- [auto] 2026-05-19 13:41:57
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD046','2026-05-19 13:41:57','13:41:57',500000,N'Đã cọc',NULL,'DDB039','QL001','KH026',NULL,500000,'CA001');

-- [auto] 2026-05-19 13:41:57
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B004';

-- [auto] 2026-05-19 13:44:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD046', 'SP_BEER02', 7, 30000, N'', 210000);

-- [auto] 2026-05-19 13:44:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD046', 'SP_BEER05', 11, 48000, N'', 528000);

-- [auto] 2026-05-19 13:44:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD046', 'SP_COM01', 5, 110000, N'', 550000);

-- [auto] 2026-05-19 13:44:29
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD046', 'SP_COM04', 3, 100000, N'', 300000);

-- [auto] 2026-05-19 13:44:29
UPDATE HoaDon SET tongTien=2088000 WHERE maHD='HD046';

-- [auto] 2026-05-19 14:48:50
INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES ('KH038', N'Kent', '0942249540');

-- [auto] 2026-05-19 14:48:50
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB040', '2026-05-19 14:48:50', '2026-05-19 00:00:00', 4, 'KH038', 0, 'QL001', 'CHIEU', '');

-- [auto] 2026-05-19 14:48:50
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB040', 'B003');

-- [auto] 2026-05-19 14:48:50
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD047','2026-05-19 14:48:50','14:48:50',500000,N'Đã cọc',NULL,'DDB040','QL001','KH038',NULL,500000,'CA002');

-- [auto] 2026-05-19 14:48:50
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B003';

-- [auto] 2026-05-19 14:49:04
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD047', 'SP_COM01', 4, 110000, N'', 440000);

-- [auto] 2026-05-19 14:49:04
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD047', 'SP_COM04', 3, 100000, N'', 300000);

-- [auto] 2026-05-19 14:49:04
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD047', 'SP_CUA05', 2, 520000, N'', 1040000);

-- [auto] 2026-05-19 14:49:04
UPDATE HoaDon SET tongTien=2280000 WHERE maHD='HD047';

-- [auto] 2026-05-19 15:11:23
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B003';

-- [auto] 2026-05-19 15:11:56
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD039', 'SP_BEER03', 10, 45000, N'', 450000);

-- [auto] 2026-05-19 15:11:56
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD039', 'SP_COM01', 7, 110000, N'', 770000);

-- [auto] 2026-05-19 15:11:56
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD039', 'SP_COM04', 7, 100000, N'', 700000);

-- [auto] 2026-05-19 15:11:56
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD039', 'SP_BEER04', 10, 50000, N'', 500000);

-- [auto] 2026-05-19 15:11:56
UPDATE HoaDon SET tongTien=2920000 WHERE maHD='HD039';

-- [auto] 2026-05-19 15:12:03
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B002';

-- [auto] 2026-05-19 15:13:25
UPDATE HoaDon SET tongTien=2420000 WHERE maHD='HD039';

-- [auto] 2026-05-19 15:13:25
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD039';

-- [auto] 2026-05-19 15:13:25
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 10:45:06', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH026', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB032';

-- [auto] 2026-05-19 15:13:25
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B002';

-- [auto] 2026-05-19 15:23:49
UPDATE HoaDon SET tongTien=1780000 WHERE maHD='HD047';

-- [auto] 2026-05-19 15:23:49
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Tiền mặt' WHERE maHD='HD047';

-- [auto] 2026-05-19 15:23:49
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 14:48:50', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH038', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB040';

-- [auto] 2026-05-19 15:23:49
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B003';

-- [auto] 2026-05-19 15:46:21
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB041', '2026-05-19 15:46:21', '2026-05-19 00:00:00', 8, 'KH027', 0, 'QL001', 'CHIEU', '');

-- [auto] 2026-05-19 15:46:21
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB041', 'B011');

-- [auto] 2026-05-19 15:46:21
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD048','2026-05-19 15:46:21','15:46:21',500000,N'Đã cọc',NULL,'DDB041','QL001','KH027',NULL,500000,'CA002');

-- [auto] 2026-05-19 15:46:21
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B011';

-- [auto] 2026-05-19 15:54:08
INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ('DDB042', '2026-05-19 15:54:08', '2026-05-19 00:00:00', 4, 'KH027', 0, 'QL001', 'CHIEU', '');

-- [auto] 2026-05-19 15:54:08
INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ('DDB042', 'B004');

-- [auto] 2026-05-19 15:54:08
INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ('HD049','2026-05-19 15:54:08','15:54:08',710000,N'Đã cọc',NULL,'DDB042','QL001','KH027',NULL,500000,'CA002');

-- [auto] 2026-05-19 15:54:08
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD049', 'SP_BEER02', 7, 30000, N'', 210000);

-- [auto] 2026-05-19 15:54:08
UPDATE Ban SET maTinhTrang = 'DAT_TRUOC' WHERE maBan = 'B004';

-- [auto] 2026-05-19 15:54:13
UPDATE ChiTietHoaDon SET soLuong = 8, thanhTien = 240000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER02';

-- [auto] 2026-05-19 15:54:13
UPDATE HoaDon SET tongTien=240000 WHERE maHD='HD049';

-- [auto] 2026-05-19 15:54:13
UPDATE ChiTietHoaDon SET soLuong = 9, thanhTien = 270000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER02';

-- [auto] 2026-05-19 15:54:13
UPDATE HoaDon SET tongTien=270000 WHERE maHD='HD049';

-- [auto] 2026-05-19 15:54:14
UPDATE ChiTietHoaDon SET soLuong = 10, thanhTien = 300000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER02';

-- [auto] 2026-05-19 15:54:14
UPDATE HoaDon SET tongTien=300000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:00:07
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD049', 'SP_BEER01', 20, 40000, N'', 800000);

-- [auto] 2026-05-19 16:00:07
UPDATE HoaDon SET tongTien=1100000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:38
UPDATE ChiTietHoaDon SET soLuong = 19, thanhTien = 760000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:38
UPDATE HoaDon SET tongTien=1060000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:38
UPDATE ChiTietHoaDon SET soLuong = 18, thanhTien = 720000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:38
UPDATE HoaDon SET tongTien=1020000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:38
UPDATE ChiTietHoaDon SET soLuong = 17, thanhTien = 680000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:38
UPDATE HoaDon SET tongTien=980000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:39
UPDATE ChiTietHoaDon SET soLuong = 16, thanhTien = 640000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:39
UPDATE HoaDon SET tongTien=940000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:39
UPDATE ChiTietHoaDon SET soLuong = 15, thanhTien = 600000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:39
UPDATE HoaDon SET tongTien=900000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:39
UPDATE ChiTietHoaDon SET soLuong = 14, thanhTien = 560000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:39
UPDATE HoaDon SET tongTien=860000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:39
UPDATE ChiTietHoaDon SET soLuong = 13, thanhTien = 520000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:39
UPDATE HoaDon SET tongTien=820000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:40
UPDATE ChiTietHoaDon SET soLuong = 12, thanhTien = 480000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:40
UPDATE HoaDon SET tongTien=780000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:40
UPDATE ChiTietHoaDon SET soLuong = 11, thanhTien = 440000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:40
UPDATE HoaDon SET tongTien=740000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:40
UPDATE ChiTietHoaDon SET soLuong = 12, thanhTien = 480000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:40
UPDATE HoaDon SET tongTien=780000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:41
UPDATE ChiTietHoaDon SET soLuong = 13, thanhTien = 520000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:41
UPDATE HoaDon SET tongTien=820000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:41
UPDATE ChiTietHoaDon SET soLuong = 14, thanhTien = 560000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:41
UPDATE HoaDon SET tongTien=860000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:41
UPDATE ChiTietHoaDon SET soLuong = 15, thanhTien = 600000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:41
UPDATE HoaDon SET tongTien=900000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:41
UPDATE ChiTietHoaDon SET soLuong = 16, thanhTien = 640000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:41
UPDATE HoaDon SET tongTien=940000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:41
UPDATE ChiTietHoaDon SET soLuong = 17, thanhTien = 680000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:41
UPDATE HoaDon SET tongTien=980000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:42
UPDATE ChiTietHoaDon SET soLuong = 18, thanhTien = 720000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:42
UPDATE HoaDon SET tongTien=1020000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:42
UPDATE ChiTietHoaDon SET soLuong = 19, thanhTien = 760000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:42
UPDATE HoaDon SET tongTien=1060000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:01:42
UPDATE ChiTietHoaDon SET soLuong = 20, thanhTien = 800000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:01:42
UPDATE HoaDon SET tongTien=1100000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:12:21
UPDATE ChiTietHoaDon SET soLuong = 10, thanhTien = 400000 WHERE maHD = 'HD049' AND maMon = 'SP_BEER01';

-- [auto] 2026-05-19 16:12:21
UPDATE HoaDon SET tongTien=700000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:12:32
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD049', 'SP_BEER03', 6, 45000, N'', 270000);

-- [auto] 2026-05-19 16:12:32
UPDATE HoaDon SET tongTien=970000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:12:39
INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES ('HD049', 'SP_COM02', 10, 90000, N'', 900000);

-- [auto] 2026-05-19 16:12:39
UPDATE HoaDon SET tongTien=1870000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:12:41
UPDATE Ban SET maTinhTrang = 'DANG_SD' WHERE maBan = 'B004';

-- [auto] 2026-05-19 16:15:18
UPDATE HoaDon SET tongTien=1870000 WHERE maHD='HD049';

-- [auto] 2026-05-19 16:15:18
UPDATE HoaDon SET trangThaiThanhToan=N'Đã thanh toán', hinhThucThanhToan=N'Chuyển khoản' WHERE maHD='HD049';

-- [auto] 2026-05-19 16:15:18
UPDATE DonDatBan SET thoiGianDat = '2026-05-19 15:54:08', thoiGianDen = '2026-05-19 00:00:00', soLuongKhach = 4, maKH = 'KH027', trangThai = 1, maNV = 'QL001' WHERE maDon = 'DDB042';

-- [auto] 2026-05-19 16:15:18
UPDATE Ban SET maTinhTrang = 'TRONG' WHERE maBan = 'B004';

-- [auto] 2026-05-19 17:24:14
DELETE FROM LichLamViec WHERE maLich = 3;

