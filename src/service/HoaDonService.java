package service;

import dao.HoaDon_DAO;
import entity.HoaDon;
import entity.TrangThaiThanhToan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HoaDonService {

    private final HoaDon_DAO hdDao = new HoaDon_DAO();

    // ── Lọc danh sách HoaDon theo khu vực và trạng thái ─────────────────────
    public List<HoaDon> filter(List<HoaDon> list, String selKV, String selTT,
                               Map<String, String> maHDToKhuVuc) {
        boolean filterKV = selKV != null && !selKV.equals("Tất cả");
        TrangThaiThanhToan filterTT = resolveTrangThaiFilter(selTT);

        List<HoaDon> result = new ArrayList<>();
        for (HoaDon hd : list) {
            if (filterKV) {
                String kv = maHDToKhuVuc.get(hd.getMaHD());
                if (!selKV.equals(kv)) continue;
            }
            if (filterTT != null && hd.getTrangThaiThanhToan() != filterTT) continue;
            result.add(hd);
        }
        return result;
    }

    // ── Tìm kiếm hóa đơn theo mã hoặc khoảng ngày ───────────────────────────
    public List<HoaDon> searchByKeywordAndDateRange(String keyword, Date from, Date to) {
        List<HoaDon> result = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            HoaDon hd = hdDao.getHoaDonByMa(keyword);
            if (hd != null && isInDateRange(hd.getNgayLap(), from, to))
                result.add(hd);
        } else {
            List<HoaDon> ds = hdDao.getHoaDonByDateRange(from, to);
            if (ds != null) result.addAll(ds);
        }
        return result;
    }

    // ── Chuyển chuỗi hiển thị sang enum TrangThaiThanhToan ──────────────────
    public static TrangThaiThanhToan resolveTrangThaiFilter(String sel) {
        if (sel == null || sel.equals("Tất cả")) return null;
        return TrangThaiThanhToan.fromDisplay(sel);
    }

    private boolean isInDateRange(Date date, Date from, Date to) {
        return date != null && !date.before(from) && !date.after(to);
    }
}
