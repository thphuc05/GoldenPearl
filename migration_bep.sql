-- Migration: tinh nang Goi mon / Man hinh Bep
-- Chay script nay tren GoldenPearlDB_Test

-- 1. Them cot trangThaiMon
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'ChiTietHoaDon' AND COLUMN_NAME = 'trangThaiMon'
)
BEGIN
    ALTER TABLE [dbo].[ChiTietHoaDon]
        ADD [trangThaiMon] NVARCHAR(20) NOT NULL CONSTRAINT DF_CTHD_TrangThai DEFAULT N'CHO_XU_LY';
    PRINT '[OK] Da them cot trangThaiMon';
END
ELSE
    PRINT '[SKIP] trangThaiMon da ton tai';
GO

-- 2. Them cot thoiGianGoi
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'ChiTietHoaDon' AND COLUMN_NAME = 'thoiGianGoi'
)
BEGIN
    ALTER TABLE [dbo].[ChiTietHoaDon]
        ADD [thoiGianGoi] DATETIME NOT NULL CONSTRAINT DF_CTHD_ThoiGianGoi DEFAULT GETDATE();
    PRINT '[OK] Da them cot thoiGianGoi';
END
ELSE
    PRINT '[SKIP] thoiGianGoi da ton tai';
GO

-- 3. Du lieu cu -> DA_PHUC_VU (khong can bep xu ly lai)
UPDATE [dbo].[ChiTietHoaDon]
SET trangThaiMon = N'DA_PHUC_VU'
WHERE trangThaiMon = N'CHO_XU_LY';
PRINT '[OK] Cap nhat du lieu cu xong';
GO

-- Gia tri hop le cua trangThaiMon:
--   CHO_XU_LY  - Waiter vua goi mon, bep chua nhan
--   DANG_LAM   - Bep dang nau
--   DA_XONG    - Bep xong, cho waiter mang ra ban
--   DA_PHUC_VU - Waiter da mang ra, hoan tat
PRINT 'Migration hoan thanh.';
GO
