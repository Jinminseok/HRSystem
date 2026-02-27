package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class OrgChartDAO {

    // ==========================
    // ✅ 현재 DEPT 테이블 부서목록 안내문 출력 (항상 최신)
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
            System.out.println("=".repeat(60));
            System.out.println("📌 현재 등록된 부서 목록");
            System.out.println("=".repeat(60));
            System.out.println("부서번호\t부서명");
            System.out.println("-".repeat(60));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println(rs.getInt("DEPT_NUM") + "\t\t" + rs.getString("DEPT_NAME"));
            }

            if (!hasData) {
                System.out.println("등록된 부서가 없습니다.");
                System.out.println();
            }

            System.out.println("=".repeat(60));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.excuteClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 전체 조직도 조회 (관리자/부서NULL 제외)
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
            System.out.println("=".repeat(80));
            System.out.println("📌 전체 조직도");
            System.out.println("=".repeat(80));

            String currentDept = "";
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;

                String deptName = rs.getString("DEPT_NAME");
                if (!deptName.equals(currentDept)) {
                    currentDept = deptName;
                    System.out.println();
                    System.out.println("[" + currentDept + "]");
                    System.out.println("-".repeat(80));
                    System.out.println("사번\t이름\t직급\t재직상태\t아이디 \t이메일\t\t전화번호");
                    System.out.println("-".repeat(80));
                }

                System.out.print(rs.getInt("USER_ID") + "\t");
                System.out.print(rs.getString("USER_NAME") + "\t");
                System.out.print(rs.getString("POSITION_NAME") + "\t");
                System.out.print(empStatusKor(rs.getString("EMP_STATUS")) + "\t\t");
                System.out.print(rs.getString("LOGIN_ID") + "\t");
                System.out.print(rs.getString("EMAIL") + "\t");
                System.out.print(rs.getString("PHONE") + "\n");
            }

            if (!hasData) {
                System.out.println("조회할 조직도 정보가 없습니다.");
            }

            System.out.println("=".repeat(80));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.excuteClose(rs, pstmt, conn);
        }
    }

    // 부서별 조직도 조회 (
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
            System.out.println("=".repeat(100));
            System.out.println("📌 부서별 조직도 조회 (검색어: " + deptKeyword + ")");
            System.out.println("=".repeat(100));

            boolean hasData = false;

            System.out.println("사번\t이름\t부서\t직급\t재직상태\t아이디 \t이메일\t\t전화번호");
            System.out.println("-".repeat(100));

            while (rs.next()) {
                hasData = true;

                System.out.print(rs.getInt("USER_ID") + "\t");
                System.out.print(rs.getString("USER_NAME") + "\t");
                System.out.print(rs.getString("DEPT_NAME") + "\t");
                System.out.print(rs.getString("POSITION_NAME") + "\t");
                System.out.print(empStatusKor(rs.getString("EMP_STATUS")) + "\t\t");
                System.out.print(rs.getString("LOGIN_ID") + "\t");
                System.out.print(rs.getString("EMAIL") + "\t");
                System.out.print(rs.getString("PHONE") + "\n");
            }

            if (!hasData) {
                System.out.println("해당 부서 조직도 정보가 없습니다.");
            }

            System.out.println("=".repeat(100));

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
}