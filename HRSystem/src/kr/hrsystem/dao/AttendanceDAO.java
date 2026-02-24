package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import kr.util.DBUtil;

public class AttendanceDAO {

    private LogDAO logDao = new LogDAO();

    // ==========================
    // 1. 출근하기 (로그 포함)
    // ==========================

    public void checkIn(int userId, int loginLogId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String checkSql =
            "SELECT COUNT(*) FROM attendance " +
            "WHERE USER_ID = ? AND ATT_DATE = TRUNC(SYSDATE)";

        String insertSql =
        	    "INSERT INTO attendance (ATT_ID, USER_ID, CHECK_IN, IN_STATUS, OUT_STATUS) " +
        	    "VALUES (ATT_SEQ.NEXTVAL, ?, SYSDATE, ?, 0)";

        try {
            conn = DBUtil.getConnection();

            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("👉 오늘 이미 출근했습니다.");
                return;
            }

            DBUtil.executeClose(rs, pstmt, null);

            Timestamp now = new Timestamp(System.currentTimeMillis());
            int inStatus = calculateInStatus(now);

            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, inStatus);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 출근 완료! (" + inStatusToString(inStatus) + ")");

                // ✅ 중요 로그 기록 (action_type 이름 통일!)
                LogDAO logDao = new LogDAO();
                logDao.insertActionLog(
                    userId,
                    "근태관리",
                    "ATT_CHECKIN",
                    "출근 처리 (" + inStatusToString(inStatus) + ")",
                    "ATTENDANCE",
                    null,          // target_id는 없어도 됨 (원하면 ATT_ID 조회해서 넣기)
                    loginLogId
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 2. 퇴근하기 (로그 포함)
    // ==========================
    public void checkOut(int userId, int loginLogId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String selectSql =
            "SELECT CHECK_IN, CHECK_OUT, IN_STATUS " +
            "FROM attendance " +
            "WHERE USER_ID = ? AND ATT_DATE = TRUNC(SYSDATE)";

        String updateSql =
            "UPDATE attendance " +
            "SET CHECK_OUT = SYSDATE, OUT_STATUS = ? " +
            "WHERE USER_ID = ? AND ATT_DATE = TRUNC(SYSDATE)";

        try {
            conn = DBUtil.getConnection();

            pstmt = conn.prepareStatement(selectSql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("👉 아직 출근하지 않았습니다.");
                return;
            }

            Timestamp alreadyOut = rs.getTimestamp("CHECK_OUT");
            int inStatus = rs.getInt("IN_STATUS");

            if (alreadyOut != null) {
                System.out.println("👉 이미 퇴근 처리되었습니다.");
                return;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            int outStatus = calculateOutStatus(now);

            DBUtil.executeClose(rs, pstmt, null);

            pstmt = conn.prepareStatement(updateSql);
            pstmt.setInt(1, outStatus);
            pstmt.setInt(2, userId);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 퇴근 완료! 상태: " + finalStatusToString(inStatus, outStatus));

                // ✅ 중요 로그 기록
                LogDAO logDao = new LogDAO();
                logDao.insertActionLog(
                    userId,
                    "근태관리",
                    "ATT_CHECKOUT",
                    "퇴근 처리 (" + finalStatusToString(inStatus, outStatus) + ")",
                    "ATTENDANCE",
                    null,
                    loginLogId
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 출근 상태 계산
    // ==========================
    private int calculateInStatus(Timestamp checkIn) {
        int inHour = checkIn.toLocalDateTime().getHour();
        int inMinute = checkIn.toLocalDateTime().getMinute();

        int baseInHour = 9;
        int baseInMinute = 0;

        boolean isOnTimeIn = (inHour < baseInHour) || (inHour == baseInHour && inMinute <= baseInMinute);

        return isOnTimeIn ? 1 : 2; // 1:정상출근, 2:지각
    }

    // ==========================
    // 퇴근 상태 계산
    // ==========================
    private int calculateOutStatus(Timestamp checkOut) {
        int outHour = checkOut.toLocalDateTime().getHour();
        int outMinute = checkOut.toLocalDateTime().getMinute();

        int baseOutHour = 17;
        int baseOutMinute = 0;

        boolean isOnTimeOut   = (outHour == baseOutHour && outMinute == baseOutMinute);
        boolean isEarlyOut    = (outHour < baseOutHour) || (outHour == baseOutHour && outMinute < baseOutMinute);
        boolean isOvertimeOut = (outHour > baseOutHour) || (outHour == baseOutHour && outMinute > baseOutMinute);

        if (isOnTimeOut) return 1;   // 정상퇴근
        if (isEarlyOut) return 2;    // 조퇴
        if (isOvertimeOut) return 3; // 연장
        return 0;
    }

    // ==========================
    // 상태 문자열
    // ==========================
    private String inStatusToString(int status) {
        switch (status) {
            case 1: return "정상출근";
            case 2: return "지각";
            default: return "미정";
        }
    }

    private String outStatusToString(int status) {
        switch (status) {
            case 1: return "정상퇴근";
            case 2: return "조퇴";
            case 3: return "연장";
            default: return "퇴근전";
        }
    }

    private String makeStatusLabel(int inStatus, int outStatus) {
        return inStatusToString(inStatus) + " / " + outStatusToString(outStatus);
    }

    private String finalStatusToString(int inStatus, int outStatus) {
        if (inStatus == 2) return "지각";
        if (outStatus == 2) return "조퇴";
        if (outStatus == 3) return "연장";
        if (inStatus == 1 && outStatus == 1) return "정상";
        if (inStatus > 0 && outStatus == 0) return "퇴근전";
        return "미정";
    }

    // ==========================
    // 전체 근태 조회
    // ==========================
    public void selectAll(int userId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql =
            "SELECT TO_CHAR(ATT_DATE, 'YYYY-MM-DD') AS ATT_DATE, " +
            "       TO_CHAR(CHECK_IN, 'YYYY-MM-DD HH24:MI') AS CHECK_IN, " +
            "       TO_CHAR(CHECK_OUT, 'YYYY-MM-DD HH24:MI') AS CHECK_OUT, " +
            "       ROUND((CHECK_OUT - CHECK_IN) * 24, 2) AS WORK_HOUR, " +
            "       NVL(IN_STATUS, 0) AS IN_STATUS, " +
            "       NVL(OUT_STATUS, 0) AS OUT_STATUS " +
            "FROM attendance " +
            "WHERE USER_ID = ? " +
            "ORDER BY ATT_DATE DESC";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            boolean hasData = false;
            System.out.println("\n==== 전체 근태 조회 ====");

            while (rs.next()) {
                hasData = true;

                String attDate = rs.getString("ATT_DATE");
                String checkIn = rs.getString("CHECK_IN");
                String checkOut = rs.getString("CHECK_OUT");
                double hour = rs.getDouble("WORK_HOUR");
                int inStatus = rs.getInt("IN_STATUS");
                int outStatus = rs.getInt("OUT_STATUS");

                System.out.println(
                    "[" + attDate + "] "
                    + (checkIn == null ? "출근기록없음" : checkIn)
                    + " ~ "
                    + (checkOut == null ? "퇴근 전" : checkOut)
                    + " | 상태 : " + makeStatusLabel(inStatus, outStatus)
                    + " | 근무시간 : " + (checkOut == null ? "-" : hour + "시간")
                );
            }

            if (!hasData) {
                System.out.println("👉 근태 기록이 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 월별 근태 조회
    // ==========================
    public void selectByMonth(int userId, String yearMonth) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql =
            "SELECT TO_CHAR(ATT_DATE, 'YYYY-MM-DD') AS ATT_DATE, " +
            "       TO_CHAR(CHECK_IN, 'YYYY-MM-DD HH24:MI') AS CHECK_IN, " +
            "       TO_CHAR(CHECK_OUT, 'YYYY-MM-DD HH24:MI') AS CHECK_OUT, " +
            "       NVL(IN_STATUS,0) AS IN_STATUS, " +
            "       NVL(OUT_STATUS,0) AS OUT_STATUS " +
            "FROM attendance " +
            "WHERE USER_ID = ? " +
            "AND TO_CHAR(ATT_DATE, 'YYYY-MM') = ? " +
            "ORDER BY ATT_DATE DESC";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, yearMonth);
            rs = pstmt.executeQuery();

            boolean hasData = false;
            System.out.println("\n==== " + yearMonth + " 월 근태 조회 ====");

            while (rs.next()) {
                hasData = true;

                String attDate = rs.getString("ATT_DATE");
                String checkIn = rs.getString("CHECK_IN");
                String checkOut = rs.getString("CHECK_OUT");
                int inStatus = rs.getInt("IN_STATUS");
                int outStatus = rs.getInt("OUT_STATUS");

                System.out.println(
                    "[" + attDate + "] "
                    + (checkIn == null ? "출근기록없음" : checkIn)
                    + " ~ "
                    + (checkOut == null ? "퇴근 전" : checkOut)
                    + " | 상태 : " + makeStatusLabel(inStatus, outStatus)
                );
            }

            if (!hasData) {
                System.out.println("👉 해당 월의 근태 기록이 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 월 총 근무시간 조회
    // ==========================
    public void selectMonthTotal(int userId, String yearMonth) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql =
            "SELECT ROUND(SUM((CHECK_OUT - CHECK_IN) * 24), 2) AS TOTAL_HOUR " +
            "FROM attendance " +
            "WHERE USER_ID = ? " +
            "AND TO_CHAR(ATT_DATE, 'YYYY-MM') = ? " +
            "AND CHECK_IN IS NOT NULL " +
            "AND CHECK_OUT IS NOT NULL";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, yearMonth);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("TOTAL_HOUR");
                System.out.println("\n==== " + yearMonth + " 월 총 근무시간 ====");
                System.out.println("총 근무시간 : " + total + "시간");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 월별 상태별 횟수 조회
    // ==========================
    public void selectMonthStatusCount(int userId, String yearMonth) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql =
            "SELECT " +
            "  SUM(CASE WHEN IN_STATUS = 1 THEN 1 ELSE 0 END) AS NORMAL_IN_CNT, " +
            "  SUM(CASE WHEN IN_STATUS = 2 THEN 1 ELSE 0 END) AS LATE_CNT, " +
            "  SUM(CASE WHEN OUT_STATUS = 1 THEN 1 ELSE 0 END) AS NORMAL_OUT_CNT, " +
            "  SUM(CASE WHEN OUT_STATUS = 2 THEN 1 ELSE 0 END) AS EARLY_CNT, " +
            "  SUM(CASE WHEN OUT_STATUS = 3 THEN 1 ELSE 0 END) AS OVERTIME_CNT, " +
            "  SUM(CASE WHEN IN_STATUS = 1 AND OUT_STATUS = 1 THEN 1 ELSE 0 END) AS PERFECT_CNT " +
            "FROM attendance " +
            "WHERE USER_ID = ? " +
            "AND TO_CHAR(ATT_DATE, 'YYYY-MM') = ?";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, yearMonth);
            rs = pstmt.executeQuery();

            System.out.println("\n==== " + yearMonth + " 월 상태별 횟수 ====");

            if (rs.next()) {
                System.out.println("정상출근 : " + rs.getInt("NORMAL_IN_CNT") + "회");
                System.out.println("지각     : " + rs.getInt("LATE_CNT") + "회");
                System.out.println("정상퇴근 : " + rs.getInt("NORMAL_OUT_CNT") + "회");
                System.out.println("조퇴     : " + rs.getInt("EARLY_CNT") + "회");
                System.out.println("연장     : " + rs.getInt("OVERTIME_CNT") + "회");
                System.out.println("완전정상(정상출근+정상퇴근) : " + rs.getInt("PERFECT_CNT") + "회");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 관리자 근태 수정 (로그 포함 / before→after)
    // ==========================
    public void updateAttendance(int targetUserId, String date, String inTime, String outTime) {
        // 기존 호출 호환용 (행위자 정보 없으면 대상 사용자로 기록)
        updateAttendance(targetUserId, date, inTime, outTime, targetUserId, null);
    }

    public void updateAttendance(int targetUserId, String date, String inTime, String outTime,
                                 int actorUserId, Integer loginLogId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmtSel = null;
        ResultSet rs = null;

        String selectSql =
            "SELECT CHECK_IN, CHECK_OUT, NVL(IN_STATUS,0) IN_STATUS, NVL(OUT_STATUS,0) OUT_STATUS " +
            "FROM attendance " +
            "WHERE USER_ID = ? AND ATT_DATE = TO_DATE(?, 'YYYY-MM-DD')";

        String updateSql =
            "UPDATE attendance " +
            "SET CHECK_IN = ?, CHECK_OUT = ?, IN_STATUS = ?, OUT_STATUS = ? " +
            "WHERE USER_ID = ? AND ATT_DATE = TO_DATE(?, 'YYYY-MM-DD')";

        try {
            conn = DBUtil.getConnection();

            // ✅ 변경 전 조회 (before)
            pstmtSel = conn.prepareStatement(selectSql);
            pstmtSel.setInt(1, targetUserId);
            pstmtSel.setString(2, date);
            rs = pstmtSel.executeQuery();

            Timestamp beforeIn = null;
            Timestamp beforeOut = null;
            int beforeInStatus = 0;
            int beforeOutStatus = 0;

            if (rs.next()) {
                beforeIn = rs.getTimestamp("CHECK_IN");
                beforeOut = rs.getTimestamp("CHECK_OUT");
                beforeInStatus = rs.getInt("IN_STATUS");
                beforeOutStatus = rs.getInt("OUT_STATUS");
            } else {
                System.out.println("👉 해당 날짜의 근태 기록이 없습니다.");
                return;
            }

            DBUtil.executeClose(rs, pstmtSel, null);

            Timestamp inTs = parseDateTime(date, inTime);
            Timestamp outTs = parseDateTime(date, outTime);

            int inStatus = (inTs == null) ? 0 : calculateInStatus(inTs);
            int outStatus = (outTs == null) ? 0 : calculateOutStatus(outTs);

            pstmt = conn.prepareStatement(updateSql);

            if (inTs == null) pstmt.setNull(1, java.sql.Types.TIMESTAMP);
            else pstmt.setTimestamp(1, inTs);

            if (outTs == null) pstmt.setNull(2, java.sql.Types.TIMESTAMP);
            else pstmt.setTimestamp(2, outTs);

            pstmt.setInt(3, inStatus);
            pstmt.setInt(4, outStatus);
            pstmt.setInt(5, targetUserId);
            pstmt.setString(6, date);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                String finalStatus = finalStatusToString(inStatus, outStatus);
                System.out.println("✅ 근태 수정 완료! 상태: " + finalStatus);

                // ✅ 관리자 수정 로그 (before -> after)
                String beforeText =
                    "before[in=" + tsToStr(beforeIn) +
                    ", out=" + tsToStr(beforeOut) +
                    ", status=" + finalStatusToString(beforeInStatus, beforeOutStatus) + "]";

                String afterText =
                    "after[in=" + tsToStr(inTs) +
                    ", out=" + tsToStr(outTs) +
                    ", status=" + finalStatus + "]";

                logDao.insertActionLog(
                    actorUserId,
                    "근태관리",
                    "ATT_UPDATE",
                    "targetUser=" + targetUserId + ", date=" + date + " | " + beforeText + " -> " + afterText,
                    "ATTENDANCE",
                    null,
                    loginLogId
                );

            } else {
                System.out.println("👉 해당 날짜의 근태 기록이 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // "NULL" / "" 처리 + "YYYY-MM-DD HH:MM:SS" 생성
    private Timestamp parseDateTime(String date, String time) {
        if (time == null) return null;
        time = time.trim();

        if (time.equalsIgnoreCase("NULL") || time.length() == 0) {
            return null;
        }

        return Timestamp.valueOf(date + " " + time + ":00");
    }

    private String tsToStr(Timestamp ts) {
        if (ts == null) return "NULL";
        String s = ts.toString(); // 2026-02-20 08:50:00.0
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }
}
