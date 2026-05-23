package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.KhachHang;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class PdfHoaDon {

    private static final DecimalFormat  FMT      = new DecimalFormat("#,###");
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private static final String FONT_PATH      = "data/fonts/Inter-Medium.otf";
    private static final String FONT_BOLD_PATH = "data/fonts/Inter-Bold.otf";

    // ── API công khai ────────────────────────────────────────────────────────
    public static void xuatVaMo(HoaDon hd, List<ChiTietHoaDon> cths, String tenBan) {
        try {
            File pdf = taoFile(hd, cths, tenBan);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdf);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo PDF: " + e.getMessage(), e);
        }
    }

    // ── Tạo file PDF ─────────────────────────────────────────────────────────
    private static File taoFile(HoaDon hd, List<ChiTietHoaDon> cths, String tenBan) throws Exception {
        File dir = new File("output/hoadon");
        dir.mkdirs();
        File file = new File(dir, "HoaDon_" + hd.getMaHD() + ".pdf");

        Document doc = new Document(PageSize.A5);
        doc.setMargins(40, 40, 32, 32);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // Font
        BaseFont bf     = BaseFont.createFont(FONT_PATH,      BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(FONT_BOLD_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Font fTitle    = new Font(bfBold, 20, Font.NORMAL, new BaseColor(0x0B, 0x3D, 0x59));
        Font fSub      = new Font(bf,      9,  Font.NORMAL, BaseColor.GRAY);
        Font fHeading  = new Font(bfBold, 12, Font.NORMAL, new BaseColor(0x0B, 0x3D, 0x59));
        Font fLabel    = new Font(bf,     9,  Font.NORMAL, new BaseColor(80, 80, 80));
        Font fValue    = new Font(bfBold,  9,  Font.NORMAL, BaseColor.BLACK);
        Font fNormal   = new Font(bf,     9,  Font.NORMAL, BaseColor.BLACK);
        Font fColHead  = new Font(bfBold,  8,  Font.NORMAL, BaseColor.WHITE);
        Font fTotal    = new Font(bfBold, 11, Font.NORMAL, new BaseColor(0x0B, 0x3D, 0x59));
        Font fFooter   = new Font(bf,     8,  Font.NORMAL, BaseColor.GRAY);

        BaseColor headerBg = new BaseColor(0x0B, 0x3D, 0x59);
        BaseColor rowAlt   = new BaseColor(0xF5, 0xF7, 0xFA);

        // ── Header nhà hàng ──────────────────────────────────────────────────
        Paragraph pName = new Paragraph("GOLDEN PEARL", fTitle);
        pName.setAlignment(Element.ALIGN_CENTER);
        pName.setSpacingAfter(2);
        doc.add(pName);

        Paragraph pTagline = new Paragraph("Nhà hàng – Bar & Restaurant", fSub);
        pTagline.setAlignment(Element.ALIGN_CENTER);
        pTagline.setSpacingAfter(8);
        doc.add(pTagline);

        addLine(doc, new BaseColor(0xC5, 0xA0, 0x59), 1.5f);

        // ── Tiêu đề hóa đơn ─────────────────────────────────────────────────
        Paragraph pHd = new Paragraph("HÓA ĐƠN THANH TOÁN", fHeading);
        pHd.setAlignment(Element.ALIGN_CENTER);
        pHd.setSpacingBefore(6);
        pHd.setSpacingAfter(10);
        doc.add(pHd);

        // ── Thông tin hóa đơn ────────────────────────────────────────────────
        PdfPTable tblInfo = new PdfPTable(new float[]{1f, 1.8f, 1f, 1.8f});
        tblInfo.setWidthPercentage(100);
        tblInfo.setSpacingAfter(10);

        String ngay = hd.getNgayLap() != null ? DATE_FMT.format(hd.getNgayLap()) : "--";
        KhachHang kh = hd.getKhachHang();
        String tenKH = (kh != null && kh.getTenKH() != null) ? kh.getTenKH() : "Khách vãng lai";
        String soDT  = (kh != null && kh.getSoDT()  != null) ? kh.getSoDT()  : "--";
        String tenNV = (hd.getNhanVien() != null) ? hd.getNhanVien().getTenNV() : "--";
        String httt  = (hd.getHinhThucThanhToan() != null) ? hd.getHinhThucThanhToan().getDisplay() : "--";

        addInfoRow(tblInfo, "Số HĐ:",    hd.getMaHD(), "Ngày:",        ngay,   fLabel, fValue);
        addInfoRow(tblInfo, "Khách:",   tenKH,         "Điện thoại:", soDT,   fLabel, fValue);
        addInfoRow(tblInfo, "NV:",      tenNV,          "Bàn:",        tenBan, fLabel, fValue);
        addInfoRow(tblInfo, "HTTT:",    httt,           "Trạng thái:", hd.getTrangThaiThanhToan().getDisplay(), fLabel, fValue);
        doc.add(tblInfo);

        // ── Bảng chi tiết món ────────────────────────────────────────────────
        PdfPTable tblMon = new PdfPTable(new float[]{0.5f, 3f, 0.6f, 1.2f, 1.2f});
        tblMon.setWidthPercentage(100);
        tblMon.setSpacingAfter(6);

        String[] headers = {"STT", "Tên món", "SL", "Đơn giá", "Thành tiền"};
        int[] aligns = {Element.ALIGN_CENTER, Element.ALIGN_LEFT, Element.ALIGN_CENTER,
                        Element.ALIGN_RIGHT,  Element.ALIGN_RIGHT};
        for (int i = 0; i < headers.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(headers[i], fColHead));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(aligns[i]);
            cell.setPadding(5);
            cell.setBorder(Rectangle.NO_BORDER);
            tblMon.addCell(cell);
        }

        int stt = 1;
        for (ChiTietHoaDon ct : cths) {
            boolean alt = stt % 2 == 0;
            BaseColor rowBg = alt ? rowAlt : BaseColor.WHITE;
            String tenMon = ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "?";

            addMonRow(tblMon, stt++, tenMon,
                    ct.getSoLuong(), ct.getDonGia(), ct.getThanhTien(),
                    fNormal, rowBg, aligns);
        }
        doc.add(tblMon);

        addLine(doc, BaseColor.LIGHT_GRAY, 0.5f);

        // ── Tổng kết ─────────────────────────────────────────────────────────
        // tongTienMon = tổng giá gốc từ các món (chưa giảm)
        // hd.getTongTien() = tiền thực thu sau giảm giá (lưu trong DB)
        double tongTienMon = 0;
        for (ChiTietHoaDon ct : cths) tongTienMon += ct.getDonGia() * ct.getSoLuong();

        double coc      = hd.getTienCoc();
        double tongGiam = Math.max(0, tongTienMon - hd.getTongTien());
        double sauGiam  = hd.getTongTien();
        double conLai   = Math.max(0, sauGiam - coc);

        PdfPTable tblSum = new PdfPTable(new float[]{3f, 1.5f});
        tblSum.setWidthPercentage(65);
        tblSum.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tblSum.setSpacingBefore(4);
        tblSum.setSpacingAfter(4);

        addSumRow(tblSum, "Tổng tiền gốc:", FMT.format(tongTienMon) + "đ", fLabel, fNormal);

        if (tongGiam > 0) {
            entity.KhuyenMai km = hd.getKhuyenMai();
            String giamLabel = (km != null && km.getTenKM() != null)
                    ? "Giảm giá (" + km.getTenKM() + "):"
                    : "Giảm giá:";
            Font fGiam = new Font(bf, 9, Font.NORMAL, new BaseColor(0xE6, 0x7E, 0x22));
            addSumRow(tblSum, giamLabel, "-" + FMT.format(tongGiam) + "đ", fLabel, fGiam);
            addSumRow(tblSum, "Sau giảm giá:", FMT.format(sauGiam) + "đ", fLabel, fNormal);
        }

        if (coc > 0)
            addSumRow(tblSum, "Đã cọc:", "-" + FMT.format(coc) + "đ", fLabel, fNormal);

        PdfPCell cSumLabel = new PdfPCell(new Phrase("Còn lại phải trả:", fTotal));
        cSumLabel.setBorder(Rectangle.NO_BORDER);
        cSumLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
        cSumLabel.setPaddingTop(4);
        PdfPCell cSumVal = new PdfPCell(new Phrase(FMT.format(conLai) + "đ", fTotal));
        cSumVal.setBorder(Rectangle.NO_BORDER);
        cSumVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cSumVal.setPaddingTop(4);
        tblSum.addCell(cSumLabel);
        tblSum.addCell(cSumVal);

        doc.add(tblSum);

        addLine(doc, new BaseColor(0xC5, 0xA0, 0x59), 1f);

        // ── Footer ───────────────────────────────────────────────────────────
        Paragraph pFooter = new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ của Golden Pearl!", fFooter);
        pFooter.setAlignment(Element.ALIGN_CENTER);
        pFooter.setSpacingBefore(6);
        doc.add(pFooter);

        Paragraph pSystem = new Paragraph("Hệ thống Golden Pearl Restaurant", fFooter);
        pSystem.setAlignment(Element.ALIGN_CENTER);
        doc.add(pSystem);

        doc.close();
        return file;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private static void addLine(Document doc, BaseColor color, float width) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        line.setSpacingBefore(4);
        line.setSpacingAfter(4);
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidthBottom(width);
        cell.setBorderColorBottom(color);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setFixedHeight(1);
        line.addCell(cell);
        doc.add(line);
    }

    private static void addInfoRow(PdfPTable t,
                                   String l1, String v1, String l2, String v2,
                                   Font fLabel, Font fValue) {
        for (String[] pair : new String[][]{{l1, v1}, {l2, v2}}) {
            PdfPCell lCell = new PdfPCell(new Phrase(pair[0], fLabel));
            lCell.setBorder(Rectangle.NO_BORDER);
            lCell.setPadding(2);
            t.addCell(lCell);
            PdfPCell vCell = new PdfPCell(new Phrase(pair[1], fValue));
            vCell.setBorder(Rectangle.NO_BORDER);
            vCell.setPadding(2);
            t.addCell(vCell);
        }
    }

    private static void addMonRow(PdfPTable t, int stt, String tenMon,
                                  int sl, double donGia, double thanhTien,
                                  Font f, BaseColor bg, int[] aligns) {
        String[] vals = {String.valueOf(stt), tenMon, String.valueOf(sl),
                FMT.format(donGia) + "d", FMT.format(thanhTien) + "d"};
        for (int i = 0; i < vals.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(vals[i], f));
            cell.setBackgroundColor(bg);
            cell.setHorizontalAlignment(aligns[i]);
            cell.setPaddingTop(4);
            cell.setPaddingBottom(4);
            cell.setPaddingLeft(4);
            cell.setPaddingRight(4);
            cell.setBorder(Rectangle.NO_BORDER);
            t.addCell(cell);
        }
    }

    private static void addSumRow(PdfPTable t, String label, String value, Font fL, Font fV) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, fL));
        lCell.setBorder(Rectangle.NO_BORDER);
        lCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        lCell.setPadding(2);
        PdfPCell vCell = new PdfPCell(new Phrase(value, fV));
        vCell.setBorder(Rectangle.NO_BORDER);
        vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vCell.setPadding(2);
        t.addCell(lCell);
        t.addCell(vCell);
    }
}
