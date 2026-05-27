-- ============================================================
--  GOLDEN PEARL — Reset dữ liệu giao dịch (giữ master data)
--  Chạy trên database: GoldenPearlDB (hoặc GoldenPearlDB_Test)
--  Giữ nguyên: SanPham, LoaiSanPham, Ban, KhuVuc,
--              NhanVien, TaiKhoan, KhuyenMai, CaLam
-- ============================================================

USE GoldenPearlDB;   -- ← đổi thành GoldenPearlDB_Test nếu cần
GO

BEGIN TRANSACTION;

-- 1. Con của HoaDon (xóa trước)
DELETE FROM ChiTietHoaDon;

-- 2. HoaDon (con của DonDatBan, KhachHang, CaLam)
DELETE FROM HoaDon;

-- 3. Con của DonDatBan
DELETE FROM ChiTietDatBan;

-- 4. PhanCongCa (con của CaLam + NhanVien, nếu bảng tồn tại)
IF OBJECT_ID('PhanCongCa', 'U') IS NOT NULL
    DELETE FROM PhanCongCa;

-- 5. DonDatBan (con của KhachHang)
DELETE FROM DonDatBan;

-- 6. KhachHang (data test, xóa sạch)
DELETE FROM KhachHang;

-- 7. Reset trạng thái toàn bộ bàn về Trống
UPDATE Ban SET maTinhTrang = 'TRONG';

COMMIT TRANSACTION;

PRINT N'✅ Xóa xong. Các bảng master data giữ nguyên.';
GO
