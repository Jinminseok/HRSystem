package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class LogDAO {

    // 1) 로그인 시도 기록 (성공/실패)
    public int insertLoginHistory(Integer userId, String loginId, String result, String failReason) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int loginLogId = -1;

        try {
            conn = DBUtil.getConnection();

            String sql = "INSERT INTO login_history "
                       + "(login_log_id, user_id, login_id, login_result, fail_reason) "
                       + "VALUES (seq_login_history.NEXTVAL, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            if (userId == null) pstmt.setNull(1, java.sql.Types.NUMERIC);
            else pstmt.setInt(1, userId);

            pstmt.setString(2, loginId);
            pstmt.setString(3, result); // S / F
            pstmt.setString(4, failReason);

            pstmt.executeUpdate();

            DBUtil.executeClose(null, pstmt, null);

            String seqSql = "SELECT seq_login_history.CURRVAL AS id FROM dual";
            pstmt = conn.prepareStatement(seqSql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                loginLogId = rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return loginLogId;
    }

    // 2) 로그아웃 기록
    public void updateLogoutTime(int loginLogId) {
        if (loginLogId <= 0) return; // ✅ 로그인 세션 없으면 종료

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "UPDATE login_history "
                       + "SET logout_time = SYSDATE "
                       + "WHERE login_log_id = ? AND logout_time IS NULL";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, loginLogId);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 3) 사용자 행동 로그 기록
    public void insertActionLog(int userId, String menuName, String actionType,
                                String actionDesc, String targetTable, Integer targetId,
                                Integer loginLogId) {

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "INSERT INTO user_action_log "
                       + "(action_log_id, user_id, menu_name, action_type, action_desc, target_table, target_id, login_log_id) "
                       + "VALUES (seq_user_action_log.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, menuName);
            pstmt.setString(3, actionType);
            pstmt.setString(4, actionDesc);
            pstmt.setString(5, targetTable);

            if (targetId == null) pstmt.setNull(6, java.sql.Types.NUMERIC);
            else pstmt.setInt(6, targetId);

            if (loginLogId == null || loginLogId <= 0) {
                pstmt.setNull(7, java.sql.Types.NUMERIC);
            } else {
                pstmt.setInt(7, loginLogId);
            }

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // =========================================================
    // 기존 전체 조회 (원하면 유지)
    // =========================================================
    public void selectLoginHistory() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT lh.login_log_id, lh.login_id, u.user_name, "
                       + "       TO_CHAR(lh.login_time, 'YYYY-MM-DD HH24:MI:SS') AS login_time, "
                       + "       TO_CHAR(lh.logout_time, 'YYYY-MM-DD HH24:MI:SS') AS logout_time, "
                       + "       lh.login_result, lh.fail_reason "
                       + "FROM login_history lh "
                       + "LEFT JOIN usertest u ON lh.user_id = u.user_id "
                       + "ORDER BY lh.login_log_id DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            printLoginHeader();
            while (rs.next()) {
                printLoginRow(rs);
            }
            printLine(130);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    public void selectActionLog() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT a.action_log_id, u.user_name, u.login_id, "
                       + "       TO_CHAR(a.action_time, 'YYYY-MM-DD HH24:MI:SS') AS action_time, "
                       + "       a.menu_name, a.action_type, a.action_desc, a.target_table, a.target_id "
                       + "FROM user_action_log a "
                       + "JOIN usertest u ON a.user_id = u.user_id "
                       + "ORDER BY a.action_log_id DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            printActionHeader("전체 행동 로그");
            while (rs.next()) {
                printActionRow(rs);
            }
            printLine(150);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // =========================================================
    // ✅ 중요 로그 전용 조회
    // =========================================================

    // 4) 중요 로그인 로그 (최근 N건)
    public void selectImportantLoginHistory(int limit) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 로그인 성공/실패 + 로그아웃시간 같이 보여줌 (최근 N건)
            String sql =
                "SELECT * FROM ( " +
                "    SELECT lh.login_log_id, lh.login_id, u.user_name, " +
                "           TO_CHAR(lh.login_time, 'YYYY-MM-DD HH24:MI:SS') AS login_time, " +
                "           TO_CHAR(lh.logout_time, 'YYYY-MM-DD HH24:MI:SS') AS logout_time, " +
                "           lh.login_result, lh.fail_reason " +
                "    FROM login_history lh " +
                "    LEFT JOIN usertest u ON lh.user_id = u.user_id " +
                "    ORDER BY lh.login_log_id DESC " +
                ") WHERE ROWNUM <= ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();

            System.out.println("\n[ 중요 로그인 로그 - 최근 " + limit + "건 ]");
            printLoginHeader();

            while (rs.next()) {
                printLoginRow(rs);
            }

            printLine(130);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 5) 중요 행동 로그 전체 (최근 N건)
    public void selectImportantActionLog(int limit) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 중요 action_type만 조회
            String sql =
                "SELECT * FROM ( " +
                "    SELECT a.action_log_id, u.user_name, u.login_id, " +
                "           TO_CHAR(a.action_time, 'YYYY-MM-DD HH24:MI:SS') AS action_time, " +
                "           a.menu_name, a.action_type, a.action_desc, a.target_table, a.target_id " +
                "    FROM user_action_log a " +
                "    JOIN usertest u ON a.user_id = u.user_id " +
                "    WHERE a.action_type IN ( " +
                "         'SIGNUP', " +
                "         'ATT_CHECKIN', 'ATT_CHECKOUT', 'ATT_UPDATE', " +
                "         'NOTICE_CREATE', 'NOTICE_UPDATE', 'NOTICE_DELETE', " +
                "         'VOTE_CAST' " +
                "    ) " +
                "    ORDER BY a.action_log_id DESC " +
                ") WHERE ROWNUM <= ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();

            printActionHeader("중요 행동 로그 - 최근 " + limit + "건");
            while (rs.next()) {
                printActionRow(rs);
            }
            printLine(150);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 6) 근태 중요 로그만 (최근 N건)
    public void selectImportantAttendanceActionLog(int limit) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT * FROM ( " +
                "    SELECT a.action_log_id, u.user_name, u.login_id, " +
                "           TO_CHAR(a.action_time, 'YYYY-MM-DD HH24:MI:SS') AS action_time, " +
                "           a.menu_name, a.action_type, a.action_desc, a.target_table, a.target_id " +
                "    FROM user_action_log a " +
                "    JOIN usertest u ON a.user_id = u.user_id " +
                "    WHERE a.action_type IN ('ATT_CHECKIN', 'ATT_CHECKOUT', 'ATT_UPDATE') " +
                "    ORDER BY a.action_log_id DESC " +
                ") WHERE ROWNUM <= ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();

            printActionHeader("근태 중요 로그 - 최근 " + limit + "건");
            while (rs.next()) {
                printActionRow(rs);
            }
            printLine(150);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 7) 게시판/투표 중요 로그만 (최근 N건)
    public void selectImportantBoardActionLog(int limit) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT * FROM ( " +
                "    SELECT a.action_log_id, u.user_name, u.login_id, " +
                "           TO_CHAR(a.action_time, 'YYYY-MM-DD HH24:MI:SS') AS action_time, " +
                "           a.menu_name, a.action_type, a.action_desc, a.target_table, a.target_id " +
                "    FROM user_action_log a " +
                "    JOIN usertest u ON a.user_id = u.user_id " +
                "    WHERE a.action_type IN ('NOTICE_CREATE', 'NOTICE_UPDATE', 'NOTICE_DELETE', 'VOTE_CAST') " +
                "    ORDER BY a.action_log_id DESC " +
                ") WHERE ROWNUM <= ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();

            printActionHeader("게시판/투표 중요 로그 - 최근 " + limit + "건");
            while (rs.next()) {
                printActionRow(rs);
            }
            printLine(150);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 8) 오늘 중요 로그 요약
    public void selectTodayImportantSummary() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT " +
                "    L.LOGIN_SUCCESS_CNT, " +
                "    L.LOGIN_FAIL_CNT, " +
                "    L.LOGOUT_CNT, " +
                "    A.SIGNUP_CNT, " +
                "    A.ATT_IN_CNT, " +
                "    A.ATT_OUT_CNT, " +
                "    A.ATT_UPDATE_CNT, " +
                "    A.NOTICE_CREATE_CNT, " +
                "    A.NOTICE_UPDATE_CNT, " +
                "    A.NOTICE_DELETE_CNT, " +
                "    A.VOTE_CNT " +
                "FROM " +
                "    ( " +
                "      SELECT " +
                "        SUM(CASE WHEN login_result = 'S' " +
                "                  AND login_time >= TRUNC(SYSDATE) " +
                "                  AND login_time < TRUNC(SYSDATE) + 1 " +
                "                 THEN 1 ELSE 0 END) AS LOGIN_SUCCESS_CNT, " +
                "        SUM(CASE WHEN login_result = 'F' " +
                "                  AND login_time >= TRUNC(SYSDATE) " +
                "                  AND login_time < TRUNC(SYSDATE) + 1 " +
                "                 THEN 1 ELSE 0 END) AS LOGIN_FAIL_CNT, " +
                "        SUM(CASE WHEN logout_time IS NOT NULL " +
                "                  AND logout_time >= TRUNC(SYSDATE) " +
                "                  AND logout_time < TRUNC(SYSDATE) + 1 " +
                "                 THEN 1 ELSE 0 END) AS LOGOUT_CNT " +
                "      FROM login_history " +
                "    ) L " +
                "CROSS JOIN " +
                "    ( " +
                "      SELECT " +
                "        NVL(SUM(CASE WHEN action_type = 'SIGNUP' THEN 1 ELSE 0 END), 0) AS SIGNUP_CNT, " +
                "        NVL(SUM(CASE WHEN action_type = 'ATT_CHECKIN' THEN 1 ELSE 0 END), 0) AS ATT_IN_CNT, " +
                "        NVL(SUM(CASE WHEN action_type = 'ATT_CHECKOUT' THEN 1 ELSE 0 END), 0) AS ATT_OUT_CNT, " +
                "        NVL(SUM(CASE WHEN action_type = 'ATT_UPDATE' THEN 1 ELSE 0 END), 0) AS ATT_UPDATE_CNT, " +
                "        NVL(SUM(CASE WHEN action_type = 'NOTICE_CREATE' THEN 1 ELSE 0 END), 0) AS NOTICE_CREATE_CNT, " +
                "        NVL(SUM(CASE WHEN action_type = 'NOTICE_UPDATE' THEN 1 ELSE 0 END), 0) AS NOTICE_UPDATE_CNT, " +
                "        NVL(SUM(CASE WHEN action_type = 'NOTICE_DELETE' THEN 1 ELSE 0 END), 0) AS NOTICE_DELETE_CNT, " +
                "        NVL(SUM(CASE WHEN action_type = 'VOTE_CAST' THEN 1 ELSE 0 END), 0) AS VOTE_CNT " +
                "      FROM user_action_log " +
                "      WHERE action_time >= TRUNC(SYSDATE) " +
                "        AND action_time < TRUNC(SYSDATE) + 1 " +
                "    ) A";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n======================================");
                System.out.println("📌 오늘 중요 로그 요약");
                System.out.println("======================================");
                System.out.println("로그인 성공   : " + rs.getInt("LOGIN_SUCCESS_CNT"));
                System.out.println("로그인 실패   : " + rs.getInt("LOGIN_FAIL_CNT"));
                System.out.println("로그아웃      : " + rs.getInt("LOGOUT_CNT"));
                System.out.println("--------------------------------------");
                System.out.println("회원가입      : " + rs.getInt("SIGNUP_CNT"));
                System.out.println("출근 처리     : " + rs.getInt("ATT_IN_CNT"));
                System.out.println("퇴근 처리     : " + rs.getInt("ATT_OUT_CNT"));
                System.out.println("근태 수정     : " + rs.getInt("ATT_UPDATE_CNT"));
                System.out.println("--------------------------------------");
                System.out.println("공지 등록     : " + rs.getInt("NOTICE_CREATE_CNT"));
                System.out.println("공지 수정     : " + rs.getInt("NOTICE_UPDATE_CNT"));
                System.out.println("공지 삭제     : " + rs.getInt("NOTICE_DELETE_CNT"));
                System.out.println("투표 참여     : " + rs.getInt("VOTE_CNT"));
                System.out.println("======================================");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // =========================================================
    // 출력 정렬용 private helper (콘솔 밀림 완화)
    // =========================================================
    private void printLoginHeader() {
        printLine(130);
        System.out.printf("%-6s %-15s %-10s %-20s %-20s %-8s %-35s%n",
                "ID", "LOGIN_ID", "이름", "LOGIN_TIME", "LOGOUT_TIME", "결과", "사유");
        printLine(130);
    }

    private void printLoginRow(ResultSet rs) throws Exception {
        String result = "S".equals(rs.getString("login_result")) ? "성공" : "실패";

        System.out.printf("%-6d %-15s %-10s %-20s %-20s %-8s %-35s%n",
                rs.getInt("login_log_id"),
                cut(rs.getString("login_id"), 14),
                cut(nvl(rs.getString("user_name")), 9),
                nvl(rs.getString("login_time")),
                nvl(rs.getString("logout_time")),
                result,
                cut(nvl(rs.getString("fail_reason")), 34));
    }

    private void printActionHeader(String title) {
        System.out.println("\n[ " + title + " ]");
        printLine(150);
        System.out.printf("%-6s %-10s %-15s %-20s %-12s %-15s %-35s %-12s %-8s%n",
                "ID", "이름", "아이디", "시간", "메뉴", "동작", "설명", "대상테이블", "대상ID");
        printLine(150);
    }

    private void printActionRow(ResultSet rs) throws Exception {
        Object targetIdObj = rs.getObject("target_id");

        System.out.printf("%-6d %-10s %-15s %-20s %-12s %-15s %-35s %-12s %-8s%n",
                rs.getInt("action_log_id"),
                cut(nvl(rs.getString("user_name")), 9),
                cut(nvl(rs.getString("login_id")), 14),
                nvl(rs.getString("action_time")),
                cut(nvl(rs.getString("menu_name")), 11),
                cut(nvl(rs.getString("action_type")), 14),
                cut(nvl(rs.getString("action_desc")), 34),
                cut(nvl(rs.getString("target_table")), 11),
                targetIdObj == null ? "-" : String.valueOf(targetIdObj));
    }

    private String nvl(String s) {
        return s == null ? "-" : s;
    }

    private String cut(String s, int len) {
        if (s == null) return "-";
        return s.length() > len ? s.substring(0, len - 3) + "..." : s;
    }

    private void printLine(int n) {
        System.out.println("=".repeat(n));
    }
}
