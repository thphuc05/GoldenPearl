package util;

public final class Constants {

    private Constants() {}

    // Thời gian rời dự kiến theo số lượng khách (phút)
    public static final int THOI_GIAN_2_KHACH     = 90;
    public static final int THOI_GIAN_4_KHACH     = 120;
    public static final int THOI_GIAN_8_KHACH     = 180;
    public static final int THOI_GIAN_NHIEU_KHACH = 240;

    // Hủy đặt bàn
    public static final int GIO_HAN_HOAN_COC = 4; // Trước X tiếng → hoàn cọc; trong X tiếng → mất cọc

    // Tài chính
    public static final double TIEN_COC_MAC_DINH = 500_000;
    public static final double GIA_TRI_1_DIEM    = 1_000;

    // Voucher điểm
    public static final int DIEM_VOUCHER_NHO  = 500;
    public static final int DIEM_VOUCHER_VUA  = 1_000;
    public static final int DIEM_VOUCHER_LON  = 2_000;
    public static final double GIAM_VOUCHER_NHO  = 50_000;
    public static final double GIAM_VOUCHER_VUA  = 100_000;
    public static final double GIAM_VOUCHER_LON  = 200_000;
}
