package entity;

public enum LoaiGiaoDich {
    TICH_LUY("TichLuy"),
    DOI_VOUCHER("DoiVoucher");

    private final String dbValue;

    LoaiGiaoDich(String dbValue) { this.dbValue = dbValue; }

    public String getDbValue() { return dbValue; }

    public static LoaiGiaoDich fromDbValue(String v) {
        for (LoaiGiaoDich lg : values())
            if (lg.dbValue.equalsIgnoreCase(v)) return lg;
        return TICH_LUY;
    }
}
