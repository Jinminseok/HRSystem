package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
            pstmt.setString(3, result);
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
        if (loginLogId <= 0) return;

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
    // 기존 전체 조회
    // =========================================================
    public void selectLoginHistory() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT lh.login_log_id, lh.login_id, u.user_name, "
                       + "       TO_CHAR(lh.login_time, 'YYYY-MM-DD HH24:MI') AS login_time, "
                       + "       TO_CHAR(lh.logout_time, 'YYYY-MM-DD HH24:MI') AS logout_time, "
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
            printDivider(112);

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
                       + "       TO_CHAR(a.action_time, 'YYYY-MM-DD HH24:MI') AS action_time, "
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
            printDivider(112);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // =========================================================
    // 중요 로그 전용 조회
    // =========================================================

    // 4) 중요 로그인 로그 (최근 N건)
    public void selectImportantLoginHistory(int limit) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT * FROM ( " +
                "    SELECT lh.login_log_id, lh.login_id, u.user_name, " +
                "           TO_CHAR(lh.login_time, 'YYYY-MM-DD HH24:MI') AS login_time, " +
                "           TO_CHAR(lh.logout_time, 'YYYY-MM-DD HH24:MI') AS logout_time, " +
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

            printDivider(112);

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

            String sql =
                "SELECT * FROM ( " +
                "    SELECT a.action_log_id, u.user_name, u.login_id, " +
                "           TO_CHAR(a.action_time, 'YYYY-MM-DD HH24:MI') AS action_time, " +
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
            printDivider(112);

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
                "           TO_CHAR(a.action_time, 'YYYY-MM-DD HH24:MI') AS action_time, " +
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
            printDivider(112);

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
                "           TO_CHAR(a.action_time, 'YYYY-MM-DD HH24:MI') AS action_time, " +
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
            printDivider(112);

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
                int boxWidth = 34;

                printBoxLine(boxWidth);
                printBoxCenter("📌 오늘 중요 로그 요약", boxWidth);
                printBoxLine(boxWidth);

                printBoxRow("로그인 성공", rs.getInt("LOGIN_SUCCESS_CNT") + "건", boxWidth);
                printBoxRow("로그인 실패", rs.getInt("LOGIN_FAIL_CNT") + "건", boxWidth);
                printBoxRow("로그아웃", rs.getInt("LOGOUT_CNT") + "건", boxWidth);
                printBoxLine(boxWidth);

                printBoxRow("회원가입", rs.getInt("SIGNUP_CNT") + "건", boxWidth);
                printBoxRow("출근 처리", rs.getInt("ATT_IN_CNT") + "건", boxWidth);
                printBoxRow("퇴근 처리", rs.getInt("ATT_OUT_CNT") + "건", boxWidth);
                printBoxRow("근태 수정", rs.getInt("ATT_UPDATE_CNT") + "건", boxWidth);
                printBoxLine(boxWidth);

                printBoxRow("공지 등록", rs.getInt("NOTICE_CREATE_CNT") + "건", boxWidth);
                printBoxRow("공지 수정", rs.getInt("NOTICE_UPDATE_CNT") + "건", boxWidth);
                printBoxRow("공지 삭제", rs.getInt("NOTICE_DELETE_CNT") + "건", boxWidth);
                printBoxRow("투표 참여", rs.getInt("VOTE_CNT") + "건", boxWidth);
                printBoxLine(boxWidth);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // =========================================================
    // 출력 정렬용 helper
    // =========================================================
    private void printLoginHeader() {
        printDivider(112);
        System.out.println(
                pad("ID", 6) +
                pad("LOGIN_ID", 16) +
                pad("이름", 10) +
                pad("LOGIN_TIME", 18) +
                pad("LOGOUT_TIME", 18) +
                pad("결과", 8) +
                pad("사유", 36)
        );
        printDivider(112);
    }

    private void printLoginRow(ResultSet rs) throws Exception {
        String result = "S".equals(rs.getString("login_result")) ? "성공" : "실패";

        System.out.println(
                pad(String.valueOf(rs.getInt("login_log_id")), 6) +
                pad(rs.getString("login_id"), 16) +
                pad(nvl(rs.getString("user_name")), 10) +
                pad(nvl(rs.getString("login_time")), 18) +
                pad(nvl(rs.getString("logout_time")), 18) +
                pad(result, 8) +
                pad(nvl(rs.getString("fail_reason")), 36)
        );
    }

    private void printActionHeader(String title) {
        System.out.println("\n[ " + title + " ]");
        printDivider(112);
        System.out.println(
                pad("ID", 6) +
                pad("이름", 10) +
                pad("아이디", 14) +
                pad("시간", 18) +
                pad("메뉴", 10) +
                pad("동작", 12)
        );
        printDivider(112);
    }

    private void printActionRow(ResultSet rs) throws Exception {
        Object targetIdObj = rs.getObject("target_id");

        String id = String.valueOf(rs.getInt("action_log_id"));
        String userName = nvl(rs.getString("user_name"));
        String loginId = nvl(rs.getString("login_id"));
        String actionTime = nvl(rs.getString("action_time"));
        String menuName = shortMenuName(rs.getString("menu_name"));
        String actionType = actionTypeToKor(rs.getString("action_type"));
        String actionDesc = nvl(rs.getString("action_desc"));
        String targetTable = targetTableToKor(rs.getString("target_table"));
        String targetId = targetIdObj == null ? "-" : String.valueOf(targetIdObj);

        System.out.println(
                pad(id, 6) +
                pad(userName, 10) +
                pad(loginId, 14) +
                pad(actionTime, 18) +
                pad(menuName, 10) +
                pad(actionType, 12)
        );

        List<String> descLines = wrapText(actionDesc, 92);
        if (descLines.isEmpty()) {
            System.out.println("   설명 : -");
        } else {
            System.out.println("   설명 : " + descLines.get(0));
            for (int i = 1; i < descLines.size(); i++) {
                System.out.println("          " + descLines.get(i));
            }
        }

        System.out.println("   대상 : " + targetTable + " / ID=" + targetId);
        printDivider(112);
    }

    private String shortMenuName(String menuName) {
        if (menuName == null) return "-";

        switch (menuName) {
            case "게시판":
                return "게시판";
            case "근태관리":
                return "근태";
            case "인사발령조회":
                return "인사조회";
            case "부서관리":
                return "부서";
            default:
                return menuName;
        }
    }

    private String actionTypeToKor(String actionType) {
        if (actionType == null) return "-";

        switch (actionType) {
            case "SIGNUP":
                return "회원가입";
            case "ATT_CHECKIN":
                return "출근";
            case "ATT_CHECKOUT":
                return "퇴근";
            case "ATT_UPDATE":
                return "근태수정";
            case "NOTICE_CREATE":
                return "게시글등록";
            case "NOTICE_UPDATE":
                return "게시글수정";
            case "NOTICE_DELETE":
                return "게시글삭제";
            case "VOTE_CAST":
                return "투표참여";
            case "HR_APPT_HISTORY_ALL":
                return "전체조회";
            case "HR_APPT_HISTORY_BY_USER":
                return "사원조회";
            case "HR_APPT_HISTORY_BY_TYPE":
                return "유형조회";
            case "HR_APPT_HISTORY_BY_DATE":
                return "기간조회";
            default:
                return actionType;
        }
    }

    private String targetTableToKor(String targetTable) {
        if (targetTable == null) return "-";

        switch (targetTable) {
            case "NOTICES":
                return "게시글";
            case "NOTICE_VOTES":
                return "투표";
            case "ATTENDANCE":
                return "근태";
            case "DEPT":
                return "부서";
            case "POSITION":
                return "직급";
            case "HR_APPOINTMENT_HISTORY":
                return "인사이력";
            default:
                return targetTable;
        }
    }

    private String nvl(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    private void printDivider(int n) {
        System.out.println("=".repeat(n));
    }

    private boolean isWide(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    private int displayWidth(String s) {
        if (s == null || s.isEmpty()) return 0;

        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            len += isWide(s.charAt(i)) ? 2 : 1;
        }
        return len;
    }

    private String pad(String s, int width) {
        if (s == null || s.trim().isEmpty()) {
            s = "-";
        }

        StringBuilder sb = new StringBuilder();
        int len = 0;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int charWidth = isWide(ch) ? 2 : 1;

            if (len + charWidth > width) {
                break;
            }

            sb.append(ch);
            len += charWidth;
        }

        while (len < width) {
            sb.append(' ');
            len++;
        }

        return sb.toString();
    }

    private List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            lines.add("-");
            return lines;
        }

        StringBuilder line = new StringBuilder();
        int lineWidth = 0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            int charWidth = isWide(ch) ? 2 : 1;

            if (lineWidth + charWidth > width) {
                lines.add(line.toString());
                line.setLength(0);
                lineWidth = 0;
            }

            line.append(ch);
            lineWidth += charWidth;
        }

        if (line.length() > 0) {
            lines.add(line.toString());
        }

        return lines;
    }

    private void printBoxLine(int width) {
        System.out.println("+" + "─".repeat(width) + "+");
    }

    private void printBoxCenter(String text, int width) {
        int textWidth = displayWidth(text);
        int left = Math.max(0, (width - textWidth) / 2);
        int right = Math.max(0, width - textWidth - left);

        System.out.println("│" + " ".repeat(left) + text + " ".repeat(right) + "│");
    }

    private void printBoxRow(String label, String value, int width) {
        String leftText = "  " + label;
        String rightText = value;

        int leftWidth = displayWidth(leftText);
        int rightWidth = displayWidth(rightText);
        int middleSpaces = Math.max(1, width - leftWidth - rightWidth);

        System.out.println("│" + leftText + " ".repeat(middleSpaces) + rightText + "│");
    }
}