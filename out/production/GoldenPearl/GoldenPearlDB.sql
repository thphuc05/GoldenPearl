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

-- Insert KhuyenMai
INSERT INTO KhuyenMai (maKM, tenKM, phanTramGiam, ngayBatDau, ngayKetThuc) VALUES ('KM001', N'Khai trương', 10, '2024-01-01', '2024-12-31');
