package service;

import dao.CaLam_DAO;
import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_DAO;
import entity.HinhThucThanhToan;
import entity.HoaDon;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardService {

    private final HoaDon_DAO        hd_dao = new HoaDon_DAO();
    private final ChiTietHoaDon_DAO ct_dao = new ChiTietHoaDon_DAO();

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

    // ── Kết quả trả về cho GUI ───────────────────────────────────────────────
    public static class DashboardResult {
        public double totalRev, profit, tienMat, chuyenKhoan;
        public int totalInv, totalCustomers;
        public Map<String, Double>  chartData;
        public Map<String, Double>  profitByDate;
        public Map<String, Integer> top;
        public List<Object[]>       rows;
    }

    // ── Tính toán toàn bộ dữ liệu dashboard ─────────────────────────────────
    public DashboardResult loadDashboardData(String maCaFilter) {
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Timestamp ts1 = new Timestamp(todayCal.getTimeInMillis());
        todayCal.set(Calendar.HOUR_OF_DAY, 23);
        todayCal.set(Calendar.MINUTE, 59);
        todayCal.set(Calendar.SECOND, 59);
        todayCal.set(Calendar.MILLISECOND, 999);
        Timestamp ts2 = new Timestamp(todayCal.getTimeInMillis());

        List<HoaDon> dsHD;
        Map<String, Double> chartData = new LinkedHashMap<>();

        if (maCaFilter != null && !maCaFilter.isEmpty()) {
            dsHD = hd_dao.getHoaDonByMaCaToday(maCaFilter);
            chartData.put("Theo ca", 0.0);
        } else {
            dsHD = hd_dao.getHoaDonByDateRange(new Date(ts1.getTime()), new Date(ts2.getTime()));
            chartData.put(sdf.format(new Date(ts1.getTime())), 0.0);
        }

        double totalRev = 0, tienMat = 0, chuyenKhoan = 0, profit = 0;
        int totalInv = 0;
        Set<String> customers = new HashSet<>();

        Map<String, Double> profitMap = ct_dao.getProfitGroupedByMaHD(ts1, ts2);
        Map<String, Integer> top = (maCaFilter != null && !maCaFilter.isEmpty())
                ? ct_dao.getTop5SellingByMaCa(maCaFilter)
                : ct_dao.getTop5SellingDishesByDateRange(ts1, ts2);

        List<Object[]> rows = new ArrayList<>();
        SimpleDateFormat dfmt = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat tfmt = new SimpleDateFormat("HH:mm");
        Map<String, Double> profitByDate = new LinkedHashMap<>();
        for (String k : chartData.keySet()) profitByDate.put(k, 0.0);

        for (HoaDon hd : dsHD) {
            boolean paid = hd.isTrangThai();
            double rowProfit = paid ? profitMap.getOrDefault(hd.getMaHD(), 0.0) : 0.0;

            if (paid) {
                totalRev += hd.getTongTien();
                profit += rowProfit;
                totalInv++;
                if (hd.getKhachHang() != null) customers.add(hd.getKhachHang().getMaKH());
                if (hd.getHinhThucThanhToan() == HinhThucThanhToan.TIEN_MAT)
                    tienMat += hd.getTongTien();
                else if (hd.getHinhThucThanhToan() == HinhThucThanhToan.CHUYEN_KHOAN)
                    chuyenKhoan += hd.getTongTien();
                if (maCaFilter == null) {
                    String ds = sdf.format(hd.getNgayLap());
                    if (chartData.containsKey(ds)) chartData.put(ds, chartData.get(ds) + hd.getTongTien());
                    if (profitByDate.containsKey(ds))
                        profitByDate.put(ds, profitByDate.get(ds) + rowProfit);
                } else {
                    chartData.put("Theo ca", chartData.get("Theo ca") + hd.getTongTien());
                    profitByDate.put("Theo ca", profitByDate.get("Theo ca") + rowProfit);
                }
            }

            if (rows.size() < 20) {
                String timeStr = hd.getThoiGian() != null ? tfmt.format(hd.getThoiGian()) : "—";
                String profitStr = paid && rowProfit > 0
                        ? new DecimalFormat("#,###").format(rowProfit) + " VNĐ" : "—";
                rows.add(new Object[]{
                        hd.getMaHD(),
                        dfmt.format(hd.getNgayLap()),
                        timeStr,
                        new DecimalFormat("#,###").format(hd.getTongTien()) + " VNĐ",
                        profitStr
                });
            }
        }

        DashboardResult result = new DashboardResult();
        result.totalRev      = totalRev;
        result.profit        = profit;
        result.totalInv      = totalInv;
        result.totalCustomers = customers.size();
        result.tienMat       = tienMat;
        result.chuyenKhoan   = chuyenKhoan;
        result.chartData     = chartData;
        result.profitByDate  = profitByDate;
        result.top           = top;
        result.rows          = rows;
        return result;
    }
}
