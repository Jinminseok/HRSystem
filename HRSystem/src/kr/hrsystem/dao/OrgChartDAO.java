package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class OrgChartDAO {

    // ==========================
    // 현재 DEPT 테이블 부서목록 안내문 출력
    // ==========================
    public void printDeptGuide() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT DEPT_NUM, DEPT_NAME " +
                "FROM DEPT " +
                "ORDER BY DEPT_NUM";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            System.out.println();
            printDivider(60);
            System.out.println("현재 등록된 부서 목록");
            printDivider(60);

            System.out.println(
                    pad("부서번호", 10) +
                    pad("부서명", 20)
            );

            System.out.println("-".repeat(60));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;

                System.out.println(
                        pad(String.valueOf(rs.getInt("DEPT_NUM")), 10) +
                        pad(rs.getString("DEPT_NAME"), 20)
                );
            }

            if (!hasData) {
                System.out.println("등록된 부서가 없습니다.");
                System.out.println();
            }

            printDivider(60);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.excuteClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 전체 조직도 조회
    // ==========================
    public void selectAllOrgChart() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT d.DEPT_NAME AS DEPT_NAME, " +
                "       NVL(p.POSITION_NAME, '미지정직급') AS POSITION_NAME, " +
                "       u.USER_ID, u.USER_NAME, u.LOGIN_ID, " +
                "       NVL(u.EMAIL, '-') AS EMAIL, " +
                "       NVL(u.PHONE, '-') AS PHONE, " +
                "       u.EMP_STATUS " +
                "FROM USERTEST u " +
                "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                "LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "WHERE u.APPROVAL_STATUS = 'APPROVED' " +
                "  AND u.DEPT_NUM IS NOT NULL " +
                "ORDER BY u.DEPT_NUM, NVL(u.POSITION_NUM, 9999), u.USER_NAME";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            System.out.println();
            printDivider(120);
            System.out.println("전체 조직도");
            printDivider(120);

            String currentDept = "";
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;

                String deptName = rs.getString("DEPT_NAME");
                if (deptName == null) deptName = "-";

                if (!deptName.equals(currentDept)) {
                    currentDept = deptName;
                    System.out.println();
                    System.out.println("[" + currentDept + "]");
                    System.out.println("-".repeat(120));

                    System.out.println(
                            pad("사번", 8) +
                            pad("이름", 10) +
                            pad("직급", 14) +
                            pad("재직상태", 10) +
                            pad("아이디", 18) +
                            pad("이메일", 32) +
                            pad("전화번호", 16)
                    );

                    System.out.println("-".repeat(120));
                }

                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 8) +
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(rs.getString("POSITION_NAME"), 14) +
                        pad(empStatusKor(rs.getString("EMP_STATUS")), 10) +
                        pad(rs.getString("LOGIN_ID"), 18) +
                        pad(rs.getString("EMAIL"), 32) +
                        pad(rs.getString("PHONE"), 16)
                );
            }

            if (!hasData) {
                System.out.println("조회할 조직도 정보가 없습니다.");
            }

            printDivider(120);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.excuteClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 부서별 조직도 조회
    // ==========================
    public void selectOrgChartByDeptName(String deptKeyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT NVL(d.DEPT_NAME, '관리자') AS DEPT_NAME, " +
                "       NVL(p.POSITION_NAME, '미지정직급') AS POSITION_NAME, " +
                "       u.USER_ID, u.USER_NAME, u.LOGIN_ID, " +
                "       NVL(u.EMAIL, '-') AS EMAIL, " +
                "       NVL(u.PHONE, '-') AS PHONE, " +
                "       u.EMP_STATUS " +
                "FROM USERTEST u " +
                "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                "LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "WHERE u.APPROVAL_STATUS = 'APPROVED' " +
                "  AND d.DEPT_NAME LIKE ? " +
                "ORDER BY NVL(u.POSITION_NUM, 9999), u.USER_NAME";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + deptKeyword + "%");

            rs = pstmt.executeQuery();

            System.out.println();
            printDivider(130);
            System.out.println("부서별 조직도 조회 (검색어: " + deptKeyword + ")");
            printDivider(130);

            boolean hasData = false;

            System.out.println(
                    pad("사번", 8) +
                    pad("이름", 10) +
                    pad("부서", 14) +
                    pad("직급", 14) +
                    pad("재직상태", 10) +
                    pad("아이디", 18) +
                    pad("이메일", 32) +
                    pad("전화번호", 16)
            );

            System.out.println("-".repeat(130));

            while (rs.next()) {
                hasData = true;

                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 8) +
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(rs.getString("DEPT_NAME"), 14) +
                        pad(rs.getString("POSITION_NAME"), 14) +
                        pad(empStatusKor(rs.getString("EMP_STATUS")), 10) +
                        pad(rs.getString("LOGIN_ID"), 18) +
                        pad(rs.getString("EMAIL"), 32) +
                        pad(rs.getString("PHONE"), 16)
                );
            }

            if (!hasData) {
                System.out.println("해당 부서 조직도 정보가 없습니다.");
            }

            printDivider(130);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.excuteClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 재직상태 한글 변환
    // ==========================
    private String empStatusKor(String status) {
        if (status == null) return "-";

        switch (status.toUpperCase()) {
            case "WORK":     return "재직";
            case "LEAVE":    return "휴직";
            case "RESIGNED": return "퇴직";
            case "WAIT":     return "대기";
            default:         return status;
        }
    }

    // ==========================
    // 콘솔 정렬용 유틸
    // ==========================
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
}