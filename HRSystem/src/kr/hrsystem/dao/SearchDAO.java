package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.util.DBUtil;

public class SearchDAO {

    private void printDivider(int length) {
        System.out.println("=".repeat(length));
    }

    private boolean isWide(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
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

    private void printHeader() {
        printDivider(90);
        System.out.println(
                pad("ID", 8) +
                pad("LOGIN_ID", 18) +
                pad("NAME", 12) +
                pad("DEPT", 14) +
                pad("POS", 14) +
                pad("JOIN_DATE", 14)
        );
        printDivider(90);
    }

    private void printRow(ResultSet rs) throws Exception {
        System.out.println(
                pad(String.valueOf(rs.getInt("USER_ID")), 8) +
                pad(rs.getString("LOGIN_ID"), 18) +
                pad(rs.getString("USER_NAME"), 12) +
                pad(rs.getString("DEPT_NAME"), 14) +
                pad(rs.getString("POSITION_NAME"), 14) +
                pad(rs.getString("JOIN_DATE_STR"), 14)
        );
    }

    private void noData() {
        System.out.println("검색 결과가 없습니다.");
    }

    // 1. 사번 검색
    public void searchUserByUserId(int userId) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "NVL(d.dept_name,'-') AS dept_name, " +
            "NVL(p.position_name,'-') AS position_name, " +
            "TO_CHAR(u.join_date, 'YYYY-MM-DD') AS join_date_str " +
            "FROM usertest u " +
            "LEFT JOIN dept d ON u.dept_num = d.dept_num " +
            "LEFT JOIN position p ON u.position_num = p.position_num " +
            "WHERE u.user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }

                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
                printDivider(90);
            }

        } catch (Exception e) {
            System.out.println("❌ 사번 검색 중 오류가 발생했습니다.");
        }
    }

    // 2. 이름 부분검색
    public void searchUserByName(String name) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "NVL(d.dept_name,'-') AS dept_name, " +
            "NVL(p.position_name,'-') AS position_name, " +
            "TO_CHAR(u.join_date, 'YYYY-MM-DD') AS join_date_str " +
            "FROM usertest u " +
            "LEFT JOIN dept d ON u.dept_num = d.dept_num " +
            "LEFT JOIN position p ON u.position_num = p.position_num " +
            "WHERE u.user_name LIKE ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }

                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
                printDivider(90);
            }

        } catch (Exception e) {
            System.out.println("❌ 이름 검색 중 오류가 발생했습니다.");
        }
    }

    // 3. 부서 검색
    public void searchUserByDeptName(String deptName) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "d.dept_name AS dept_name, " +
            "NVL(p.position_name,'-') AS position_name, " +
            "TO_CHAR(u.join_date, 'YYYY-MM-DD') AS join_date_str " +
            "FROM usertest u " +
            "JOIN dept d ON u.dept_num = d.dept_num " +
            "LEFT JOIN position p ON u.position_num = p.position_num " +
            "WHERE d.dept_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, deptName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }

                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
                printDivider(90);
            }

        } catch (Exception e) {
            System.out.println("❌ 부서명 검색 중 오류가 발생했습니다.");
        }
    }

    // 4. 직급 검색
    public void searchUserByPositionName(String posName) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "NVL(d.dept_name,'-') AS dept_name, " +
            "p.position_name AS position_name, " +
            "TO_CHAR(u.join_date, 'YYYY-MM-DD') AS join_date_str " +
            "FROM usertest u " +
            "LEFT JOIN dept d ON u.dept_num = d.dept_num " +
            "JOIN position p ON u.position_num = p.position_num " +
            "WHERE p.position_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, posName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }

                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
                printDivider(90);
            }

        } catch (Exception e) {
            System.out.println("❌ 직급명 검색 중 오류가 발생했습니다.");
        }
    }

    // 5. 가입일 기간 검색
    public void searchUserByJoinDateRange(Date start, Date end) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "NVL(d.dept_name,'-') AS dept_name, " +
            "NVL(p.position_name,'-') AS position_name, " +
            "TO_CHAR(u.join_date, 'YYYY-MM-DD') AS join_date_str " +
            "FROM usertest u " +
            "LEFT JOIN dept d ON u.dept_num = d.dept_num " +
            "LEFT JOIN position p ON u.position_num = p.position_num " +
            "WHERE u.join_date >= ? AND u.join_date < (? + 1) " +
            "ORDER BY u.join_date";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, start);
            pstmt.setDate(2, end);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }

                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
                printDivider(90);
            }

        } catch (Exception e) {
            System.out.println("❌ 가입일 기간 검색 중 오류가 발생했습니다.");
        }
    }

    // 부서명 목록 가져오기
    public List<String> getDeptNameList() {
        List<String> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT dept_name FROM dept ORDER BY dept_name";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("dept_name"));
            }
        } catch (Exception e) {
            System.out.println("❌ 부서 목록 조회 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return list;
    }

    // 직급명 목록 가져오기
    public List<String> getPositionNameList() {
        List<String> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT position_name FROM position ORDER BY position_num";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("position_name"));
            }
        } catch (Exception e) {
            System.out.println("❌ 직급 목록 조회 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return list;
    }
}