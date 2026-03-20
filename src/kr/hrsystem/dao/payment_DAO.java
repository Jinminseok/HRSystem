package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class payment_DAO {

    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    private void getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        conn = DBUtil.getConnection();
    }

    private void closeResources() {
        DBUtil.executeClose(rs, pstmt, conn);
        rs = null;
        pstmt = null;
        conn = null;
    }

    // ==========================
    // 공통 출력 유틸
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

    private String pad(String text, int width) {
        if (text == null || text.trim().isEmpty()) {
            text = "-";
        }

        StringBuilder sb = new StringBuilder();
        int len = 0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
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

    private String nvl(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    private String formatMoney(int amount) {
        return String.format("%,d원", amount);
    }

    private String getPayStatusKor(String status) {
        if ("Y".equalsIgnoreCase(status)) {
            return "지급완료";
        }
        return "미지급";
    }

    // ==========================
    // [조회] 급여 지급 현황 목록
    // ==========================
    public void showDeptPaymentStatus(int deptNum, String month) {
        try {
            getConnection();

            String sql =
                "SELECT u.USER_ID, u.USER_NAME, p.POSITION_SAL, " +
                "       NVL(S.OT_SUM, 0) AS OT_SUM, " +
                "       NVL(S.HOLI_SUM, 0) AS HOLI_SUM, " +
                "       NVL(PAY.PAY_STATUS, 'N') AS STATUS, " +
                "       PAY.ACTUAL_DATE " +
                "FROM USERTEST u " +
                "JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "LEFT JOIN ( " +
                "    SELECT sm.USER_ID, " +
                "           SUM(sm.SAL_OVERTIME) AS OT_SUM, " +
                "           SUM(sm.SAL_HOLIDAY) AS HOLI_SUM " +
                "    FROM SALARY_MANAGEMENT sm " +
                "    JOIN ATTENDANCE a ON sm.ATT_ID = a.ATT_ID " +
                "    WHERE TO_CHAR(a.ATT_DATE, 'YYYY-MM') = ? " +
                "    GROUP BY sm.USER_ID " +
                ") S ON u.USER_ID = S.USER_ID " +
                "LEFT JOIN SALARY_PAYMENT PAY " +
                "       ON u.USER_ID = PAY.USER_ID " +
                "      AND PAY.PAY_MONTH = ? ";

            if (deptNum != -1) {
                sql += "WHERE u.DEPT_NUM = ? ";
            }

            sql += "ORDER BY u.USER_ID ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);
            pstmt.setString(2, month);

            if (deptNum != -1) {
                pstmt.setInt(3, deptNum);
            }

            rs = pstmt.executeQuery();

            System.out.println();
            printDivider(76);
            System.out.println(month + " 급여 지급 현황");
            printDivider(76);

            System.out.println(
                    pad("사번", 8) +
                    pad("이름", 12) +
                    pad("실수령액", 16) +
                    pad("지급일", 14) +
                    pad("상태", 12)
            );

            System.out.println("-".repeat(76));

            boolean hasData = false;

            while (rs.next()) {
                hasData = true;

                int total = rs.getInt("POSITION_SAL")
                        + rs.getInt("OT_SUM")
                        + rs.getInt("HOLI_SUM");

                String netPay = formatMoney((int) (total * 0.9));

                Date actualDate = rs.getDate("ACTUAL_DATE");
                String payDate = (actualDate == null) ? "-" : actualDate.toString();

                String status = getPayStatusKor(rs.getString("STATUS"));

                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 8) +
                        pad(rs.getString("USER_NAME"), 12) +
                        pad(netPay, 16) +
                        pad(payDate, 14) +
                        pad(status, 12)
                );
            }

            if (!hasData) {
                System.out.println("조회된 급여 지급 현황이 없습니다.");
            }

            printDivider(76);

        } catch (Exception e) {
            System.out.println("❌ 급여 지급 현황 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }
    }

    // ==========================
    // [조회] 완료/취소 대상자 목록
    // ==========================
    public void showFilteredPaymentList(int deptNum, String month, String targetStatus) {
        try {
            getConnection();

            String sql =
                "SELECT u.USER_ID, u.USER_NAME, p.POSITION_SAL, " +
                "       NVL(S.OT_SUM, 0) AS OT_SUM, " +
                "       NVL(S.HOLI_SUM, 0) AS HOLI_SUM " +
                "FROM USERTEST u " +
                "JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "LEFT JOIN ( " +
                "    SELECT sm.USER_ID, " +
                "           SUM(sm.SAL_OVERTIME) AS OT_SUM, " +
                "           SUM(sm.SAL_HOLIDAY) AS HOLI_SUM " +
                "    FROM SALARY_MANAGEMENT sm " +
                "    JOIN ATTENDANCE a ON sm.ATT_ID = a.ATT_ID " +
                "    WHERE TO_CHAR(a.ATT_DATE, 'YYYY-MM') = ? " +
                "    GROUP BY sm.USER_ID " +
                ") S ON u.USER_ID = S.USER_ID ";

            if ("N".equals(targetStatus)) {
                sql += "WHERE u.USER_ID NOT IN (SELECT USER_ID FROM SALARY_PAYMENT WHERE PAY_MONTH = ?) ";
            } else {
                sql += "WHERE u.USER_ID IN (SELECT USER_ID FROM SALARY_PAYMENT WHERE PAY_MONTH = ?) ";
            }

            if (deptNum != -1) {
                sql += "AND u.DEPT_NUM = ? ";
            }

            sql += "ORDER BY u.USER_ID ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);
            pstmt.setString(2, month);

            if (deptNum != -1) {
                pstmt.setInt(3, deptNum);
            }

            rs = pstmt.executeQuery();

            String title;
            if ("N".equals(targetStatus)) {
                title = month + " 미지급 대상자 목록";
            } else {
                title = month + " 지급 완료자 목록";
            }

            System.out.println();
            printDivider(64);
            System.out.println(title);
            printDivider(64);

            System.out.println(
                    pad("사번", 8) +
                    pad("이름", 12) +
                    pad("기본급", 14) +
                    pad("실수령액", 16)
            );

            System.out.println("-".repeat(64));

            boolean hasData = false;

            while (rs.next()) {
                hasData = true;

                int total = rs.getInt("POSITION_SAL")
                        + rs.getInt("OT_SUM")
                        + rs.getInt("HOLI_SUM");

                String basePay = formatMoney(rs.getInt("POSITION_SAL"));
                String netPay = formatMoney((int) (total * 0.9));

                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 8) +
                        pad(rs.getString("USER_NAME"), 12) +
                        pad(basePay, 14) +
                        pad(netPay, 16)
                );
            }

            if (!hasData) {
                System.out.println("대상 사원이 없습니다.");
            }

            printDivider(64);

        } catch (Exception e) {
            System.out.println("❌ 지급 대상 목록 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }
    }

    // ==========================
    // [처리] 개별 지급 완료
    // ==========================
    public int processPayment(int userId, String month) {
        try {
            getConnection();

            String sql =
                "INSERT INTO SALARY_PAYMENT " +
                "(PAY_ID, USER_ID, PAY_MONTH, ACTUAL_DATE, PAY_STATUS) " +
                "VALUES (PAY_SEQ.NEXTVAL, ?, ?, SYSDATE, 'Y')";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, month);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            return 0;
        } finally {
            closeResources();
        }
    }

    // ==========================
    // [처리] 부서별 일괄 지급 완료
    // ==========================
    public int processPaymentBatch(int deptNum, String month) {
        int count = 0;
        ResultSet rsList = null;
        PreparedStatement pstmtList = null;

        try {
            getConnection();

            String sql =
                "SELECT USER_ID " +
                "FROM USERTEST " +
                "WHERE USER_ID NOT IN (SELECT USER_ID FROM SALARY_PAYMENT WHERE PAY_MONTH = ?) ";

            if (deptNum != -1) {
                sql += "AND DEPT_NUM = ? ";
            }

            pstmtList = conn.prepareStatement(sql);
            pstmtList.setString(1, month);

            if (deptNum != -1) {
                pstmtList.setInt(2, deptNum);
            }

            rsList = pstmtList.executeQuery();

            while (rsList.next()) {
                if (processPayment(rsList.getInt("USER_ID"), month) > 0) {
                    count++;
                }
            }

        } catch (Exception e) {
            System.out.println("❌ 일괄 지급 처리 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(rsList, pstmtList, null);
            closeResources();
        }

        return count;
    }

    // ==========================
    // [처리] 개별 지급 취소
    // ==========================
    public int cancelPayment(int userId, String month) {
        try {
            getConnection();

            String sql =
                "DELETE FROM SALARY_PAYMENT " +
                "WHERE USER_ID = ? AND PAY_MONTH = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, month);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            return 0;
        } finally {
            closeResources();
        }
    }

    // ==========================
    // [처리] 부서별 일괄 지급 취소
    // ==========================
    public int cancelPaymentBatch(int deptNum, String month) {
        try {
            getConnection();

            String sql =
                "DELETE FROM SALARY_PAYMENT " +
                "WHERE PAY_MONTH = ? ";

            if (deptNum != -1) {
                sql += "AND USER_ID IN (SELECT USER_ID FROM USERTEST WHERE DEPT_NUM = ?)";
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);

            if (deptNum != -1) {
                pstmt.setInt(2, deptNum);
            }

            return pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("❌ 일괄 지급 취소 중 오류가 발생했습니다.");
            return 0;
        } finally {
            closeResources();
        }
    }
}