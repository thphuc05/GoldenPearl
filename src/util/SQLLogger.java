package util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SQLLogger {
    private static final SimpleDateFormat TS_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static File cachedFile = null;

    public static synchronized void log(String sql) {
        File f = findSqlFile();
        if (f == null) return;
        try (FileWriter fw = new FileWriter(f, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("-- [auto] " + TS_FMT.format(new Date()));
            pw.println(sql.trim().endsWith(";") ? sql.trim() : sql.trim() + ";");
            pw.println();
        } catch (IOException e) {
            System.err.println("[SQLLogger] Failed to write: " + e.getMessage());
        }
    }

    private static File findSqlFile() {
        if (cachedFile != null && cachedFile.exists()) return cachedFile;
        File dir = new File(System.getProperty("user.dir"));
        for (int i = 0; i < 6; i++) {
            File candidate = new File(dir, "GoldenPearlDB.sql");
            if (candidate.exists()) { cachedFile = candidate; return cachedFile; }
            File parent = dir.getParentFile();
            if (parent == null) break;
            dir = parent;
        }
        return null;
    }

    public static String nStr(String s) {
        if (s == null) return "NULL";
        return "N'" + s.replace("'", "''") + "'";
    }

    public static String str(String s) {
        if (s == null) return "NULL";
        return "'" + s.replace("'", "''") + "'";
    }

    public static String ts(Date d) {
        if (d == null) return "NULL";
        return "'" + TS_FMT.format(d) + "'";
    }

    public static String bit(boolean b) {
        return b ? "1" : "0";
    }

    public static String num(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}
