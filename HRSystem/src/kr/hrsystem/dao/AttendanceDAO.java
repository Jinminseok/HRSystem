package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import kr.util.DBUtil;

public class AttendanceDAO {

    private LogDAO logDao = new LogDAO();

    private String tsToMinuteStr(Timestamp ts) {
        if (ts == null) return "-";
        String s = ts.toString();
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }

    // ==========================
    // 1. 출근하기 (로그 포함)
    // ==========================
    public void checkIn(int userId, int loginLogId) {
 
        if (!existsUser(userId)) {
            System.out.println("❌ 존재하지 않는 USER_ID 입니다.");
            System.out.println("다시 입력해주세요.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String checkSql =
            "SELECT COUNT(*) FROM attendance " +
            "WHERE USER_ID = ? AND ATT_DATE = TRUNC(SYSDATE)";

        String insertSql =
            "INSERT INTO attendance (ATT_ID, USER_ID, CHECK_IN, IN_STATUS, OUT_STATUS) " +
            "VALUES (ATT_SEQ.NEXTVAL, ?, SYSDATE, ?, 0)";

        String selectTimeSql =
            "SELECT CHECK_IN FROM attendance " +
            "WHERE USER_ID = ? AND ATT_DATE = TRUNC(SYSDATE)";

        try {
            conn = DBUtil.getConnection();

            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("👉 오늘 이미 출근했습니다.");
                System.out.println();
                return;
            }

            DBUtil.executeClose(rs, pstmt, null);

            Timestamp now = new Timestamp(System.currentTimeMillis());
            int inStatus = calculateInStatus(now);

            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, inStatus);

            int count = pstmt.executeUpdate();

            DBUtil.executeClose(null, pstmt, null);

            if (count > 0) {
                pstmt = conn.prepareStatement(selectTimeSql);
                pstmt.setInt(1, userId);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    Timestamp inTs = rs.getTimestamp("CHECK_IN");
                    System.out.println("✅ 출근 처리 완료! 출근시간: " + tsToMinuteStr(inTs));
                } else {
                    System.out.println("✅ 출근 처리 완료!");
                }

                logDao.insertActionLog(
                    userId,
                    "근태관리",
                    "ATT_CHECKIN",
                    "출근 처리 (출근시간 기록)",
                    "ATTENDANCE",
                    null,
                    loginLogId
                );
            }

        } catch (Exception e) {
            System.out.println("❌ 출근 처리 중 오류가 발생했습니다.");
            System.out.println("다시 시도해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 2. 퇴근하기 (로그 포함)
    // ==========================
    public void checkOut(int userId, int loginLogId) {

        if (!existsUser(userId)) {
            System.out.println("❌ 존재하지 않는 USER_ID 입니다.");
            System.out.println("다시 입력해주세요.");
            return;
        }

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

        String selectTimeSql =
            "SELECT CHECK_IN, CHECK_OUT FROM attendance " +
            "WHERE USER_ID = ? AND ATT_DATE = TRUNC(SYSDATE)";

        try {
            conn = DBUtil.getConnection();

            pstmt = conn.prepareStatement(selectSql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("👉 아직 출근하지 않았습니다.");
                System.out.println();
                return;
            }

            Timestamp alreadyOut = rs.getTimestamp("CHECK_OUT");

            if (alreadyOut != null) {
                System.out.println("👉 이미 퇴근 처리되었습니다.");
                System.out.println();
                return;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            int outStatus = calculateOutStatus(now);

            DBUtil.executeClose(rs, pstmt, null);

            pstmt = conn.prepareStatement(updateSql);
            pstmt.setInt(1, outStatus);
            pstmt.setInt(2, userId);

            int count = pstmt.executeUpdate();

            DBUtil.executeClose(null, pstmt, null);

            if (count > 0) {
                pstmt = conn.prepareStatement(selectTimeSql);
                pstmt.setInt(1, userId);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    Timestamp inTs = rs.getTimestamp("CHECK_IN");
                    Timestamp outTs = rs.getTimestamp("CHECK_OUT");

                    System.out.println("✅ 퇴근 처리 완료!");
                    System.out.println("   출근시간: " + tsToMinuteStr(inTs));
                    System.out.println("   퇴근시간: " + tsToMinuteStr(outTs));
                } else {
                    System.out.println("✅ 퇴근 처리 완료!");
                    System.out.println();
                }

                logDao.insertActionLog(
                    userId,
                    "근태관리",
                    "ATT_CHECKOUT",
                    "퇴근 처리 (퇴근시간 기록)",
                    "ATTENDANCE",
                    null,
                    loginLogId
                );
            }

        } catch (Exception e) {
            System.out.println("❌ 퇴근 처리 중 오류가 발생했습니다.");
            System.out.println("다시 시도해주세요.");
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

        return isOnTimeIn ? 1 : 2;
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

        if (isOnTimeOut) return 1;
        if (isEarlyOut) return 2;
        if (isOvertimeOut) return 3;
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

        if (!existsUser(userId)) {
            System.out.println("❌ 존재하지 않는 USER_ID 입니다.");
            System.out.println("다시 입력해주세요.");
            return;
        }

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
            System.out.println("\n========================================== 전체 근태 조회 ==========================================");
            System.out.println();

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
                System.out.println();
            }

            if (!hasData) {
                System.out.println("👉 근태 기록이 없습니다.");
                System.out.println();
            }

        } catch (Exception e) {
            System.out.println("❌ 전체 근태 조회 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 월별 근태 조회
    // ==========================
    public void selectByMonth(int userId, String yearMonth) {

        if (!existsUser(userId)) {
            System.out.println("❌ 존재하지 않는 USER_ID 입니다.");
            System.out.println("다시 입력해주세요.");
            return;
        }

        if (!isValidYearMonth(yearMonth)) {
            System.out.println("❌ 조회 월 형식이 잘못되었습니다.");
            System.out.println("예: 2026-02 형식으로 다시 입력해주세요.");
            return;
        }

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
            System.out.println("\n=========================== " + yearMonth + " 월 근태 조회 ===========================");
            System.out.println();

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
                System.out.println();
            }

            if (!hasData) {
                System.out.println("👉 해당 월의 근태 기록이 없습니다.");
                System.out.println();
            }

        } catch (Exception e) {
            System.out.println("❌ 월별 근태 조회 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 월 총 근무시간 조회
    // ==========================
    public void selectMonthTotal(int userId, String yearMonth) {

        if (!existsUser(userId)) {
            System.out.println("❌ 존재하지 않는 USER_ID 입니다.");
            System.out.println("다시 입력해주세요.");
            return;
        }

        if (!isValidYearMonth(yearMonth)) {
            System.out.println("❌ 조회 월 형식이 잘못되었습니다.");
            System.out.println("예: 2026-02 형식으로 다시 입력해주세요.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql =
            "SELECT COUNT(*) AS RECORD_CNT, " +
            "       ROUND(SUM((CHECK_OUT - CHECK_IN) * 24), 2) AS TOTAL_HOUR " +
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
                int recordCnt = rs.getInt("RECORD_CNT");

                if (recordCnt == 0) {
                    System.out.println("👉 해당 월의 근태 기록이 없습니다.");
                    System.out.println();
                    return;
                }

                double total = rs.getDouble("TOTAL_HOUR");
                System.out.println("\n==== " + yearMonth + " 월 총 근무시간 ====");
                System.out.println();
                System.out.println("총 근무시간 : " + total + "시간");
                System.out.println();
            }

        } catch (Exception e) {
            System.out.println("❌ 월 총 근무시간 조회 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 월별 상태별 횟수 조회
    // ==========================
    public void selectMonthStatusCount(int userId, String yearMonth) {

        if (!existsUser(userId)) {
            System.out.println("❌ 존재하지 않는 USER_ID 입니다.");
            System.out.println("다시 입력해주세요.");
            return;
        }

        if (!isValidYearMonth(yearMonth)) {
            System.out.println("❌ 조회 월 형식이 잘못되었습니다.");
            System.out.println("예: 2026-02 형식으로 다시 입력해주세요.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql =
            "SELECT COUNT(*) AS RECORD_CNT, " +
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

            if (rs.next()) {
                int recordCnt = rs.getInt("RECORD_CNT");

                if (recordCnt == 0) {
                    System.out.println("👉 해당 월의 근태 기록이 없습니다.");
                    System.out.println();
                    return;
                }

                System.out.println("\n======== " + yearMonth + "월 상태별 횟수 ========");
                System.out.println("정상출근 : " + rs.getInt("NORMAL_IN_CNT") + "회");
                System.out.println("지각     : " + rs.getInt("LATE_CNT") + "회");
                System.out.println("정상퇴근 : " + rs.getInt("NORMAL_OUT_CNT") + "회");
                System.out.println("조퇴     : " + rs.getInt("EARLY_CNT") + "회");
                System.out.println("연장     : " + rs.getInt("OVERTIME_CNT") + "회");
                System.out.println("완전정상(정상출근+정상퇴근) : " + rs.getInt("PERFECT_CNT") + "회");
                System.out.println();
            }

        } catch (Exception e) {
            System.out.println("❌ 월 상태별 횟수 조회 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 관리자 근태 수정 (로그 포함 / before→after)
    // ==========================
    public void updateAttendance(int targetUserId, String date, String inTime, String outTime) {
        updateAttendance(targetUserId, date, inTime, outTime, targetUserId, null);
    }

    public void updateAttendance(int targetUserId, String date, String inTime, String outTime,
                                 int actorUserId, Integer loginLogId) {

        if (!existsUser(targetUserId)) {
            System.out.println("❌ 존재하지 않는 USER_ID 입니다.");
            System.out.println("다시 입력해주세요.");
            return;
        }

        if (!isValidDate(date)) {
            System.out.println("❌ 날짜 형식이 잘못되었습니다.");
            System.out.println("예: 2026-02-20 형식으로 다시 입력해주세요.");
            return;
        }

        if (!isValidTimeInput(inTime)) {
            System.out.println("❌ 출근 시간 형식이 잘못되었습니다.");
            System.out.println("예: 08:50 또는 NULL 형식으로 다시 입력해주세요.");
            return;
        }

        if (!isValidTimeInput(outTime)) {
            System.out.println("❌ 퇴근 시간 형식이 잘못되었습니다.");
            System.out.println("예: 17:00 또는 NULL 형식으로 다시 입력해주세요.");
            return;
        }

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
                System.out.println();
                return;
            }

            DBUtil.executeClose(rs, pstmtSel, null);

            Timestamp inTs = parseDateTime(date, inTime);
            Timestamp outTs = parseDateTime(date, outTime);

            if (inTs != null && outTs != null && outTs.before(inTs)) {
                System.out.println("❌ 퇴근 시간은 출근 시간보다 빠를 수 없습니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

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
                System.out.println();

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
                System.out.println();
            }

        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
            System.out.println("다시 입력해주세요.");
        } catch (Exception e) {
            System.out.println("❌ 근태 수정 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 유효성 검사
    // ==========================
    private boolean existsUser(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM USERTEST WHERE USER_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            return false;
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return false;
    }

    private boolean isValidYearMonth(String yearMonth) {
        try {
            YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidTimeInput(String time) {
        if (time == null) return false;

        String value = time.trim();

        if (value.length() == 0) return true;
        if ("NULL".equalsIgnoreCase(value)) return true;

        try {
            LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // "NULL" / "" 처리 + "YYYY-MM-DD HH:MM:SS" 생성
    private Timestamp parseDateTime(String date, String time) {
        if (time == null) return null;

        time = time.trim();

        if (time.equalsIgnoreCase("NULL") || time.length() == 0) {
            return null;
        }

        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 또는 시간 형식이 잘못되었습니다.");
        }

        return Timestamp.valueOf(date + " " + time + ":00");
    }

    private String tsToStr(Timestamp ts) {
        if (ts == null) return "NULL";
        String s = ts.toString();
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }
}