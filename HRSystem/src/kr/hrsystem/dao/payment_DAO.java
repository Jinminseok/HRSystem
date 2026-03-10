package kr.hrsystem.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.util.DBUtil;

public class payment_DAO {
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    private void getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        conn = DBUtil.getConnection();
    }

    // 💡 한글/영어 너비를 계산해서 정렬을 맞춰주는 메소드
    public String align(String text, int length) {
        if (text == null) text = "-";
        int currentLength = 0;
        for (char c : text.toCharArray()) {
            if (c >= '\uAC00' && c <= '\uD7A3') currentLength += 2; // 한글은 2칸
            else currentLength += 1; // 영어, 숫자, 공백은 1칸
        }
        return text + " ".repeat(Math.max(0, length - currentLength));
    }
  
    // [조회] 급여 지급 현황 목록 (부서/전체)
    public void showDeptPaymentStatus(int deptNum, String month) {
        try {
            getConnection();
            String sql = "SELECT u.USER_ID, u.USER_NAME, p.POSITION_SAL, " +
                         "       NVL(S.OT_SUM, 0) as OT_SUM, NVL(S.HOLI_SUM, 0) as HOLI_SUM, " +
                         "       NVL(P.PAY_STATUS, 'N') AS STATUS, P.ACTUAL_DATE " +
                         "FROM USERTEST u " +
                         "JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                         "LEFT JOIN (" +
                         "    SELECT S1.USER_ID, SUM(S1.SAL_OVERTIME) as OT_SUM, SUM(S1.SAL_HOLIDAY) as HOLI_SUM " +
                         "    FROM SALARY_MANAGEMENT S1 " +
                         "    JOIN ATTENDANCE A ON S1.ATT_ID = A.ATT_ID " +
                         "    WHERE TO_CHAR(A.ATT_DATE, 'YYYY-MM') = ? " +
                         "    GROUP BY S1.USER_ID" +
                         ") S ON u.USER_ID = S.USER_ID " +
                         "LEFT JOIN SALARY_PAYMENT P ON u.USER_ID = P.USER_ID AND P.PAY_MONTH = ? ";
            
            if (deptNum != -1) sql += "WHERE u.DEPT_NUM = ? ";
            sql += "ORDER BY u.USER_ID ASC";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);
            pstmt.setString(2, month);
            if (deptNum != -1) pstmt.setInt(3, deptNum);
            rs = pstmt.executeQuery();

            System.out.println("+─────────────────────────────────────────────────────────────────────────+");
            System.out.println("│                    " + month + " 급여 지급 현황                               │");
            System.out.println("+─────────────────────────────────────────────────────────────────────────+"); 
            System.out.println("  " + align("사번", 9) + align("이름", 11) + align("실수령액", 17) + align("지급일", 17) + align("상태", 13));
            System.out.println("──────────────────────────────────────────────────────────────────────────");
            
            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                int total = rs.getInt("POSITION_SAL") + rs.getInt("OT_SUM") + rs.getInt("HOLI_SUM");
                String netPay = String.format("%,d", (int)(total * 0.9)) + "원";
                String date = rs.getDate("ACTUAL_DATE") == null ? "-" : rs.getDate("ACTUAL_DATE").toString();
                String status = rs.getString("STATUS").equals("Y") ? "✅ 지급완료" : "❌ 미지급(N)";

                System.out.println("  " + align(String.valueOf(rs.getInt("USER_ID")), 8) + 
                                   align(rs.getString("USER_NAME"), 12) + 
                                   align(netPay, 16) + 
                                   align(date, 14) + status);
            }
            if(!hasData) System.out.println("  ! 데이터가 존재하지 않습니다.");
            System.out.println("+─────────────────────────────────────────────────────────────────────────+"); 
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    // [필터링 조회] 완료/취소 시 대상자만 출력
    public void showFilteredPaymentList(int deptNum, String month, String targetStatus) {
        try {
            getConnection();
            String sql = "SELECT u.USER_ID, u.USER_NAME, p.POSITION_SAL, " +
                         "       NVL(S.OT_SUM, 0) as OT_SUM, NVL(S.HOLI_SUM, 0) as HOLI_SUM " +
                         "FROM USERTEST u " +
                         "JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                         "LEFT JOIN (" +
                         "    SELECT S1.USER_ID, SUM(S1.SAL_OVERTIME) as OT_SUM, SUM(S1.SAL_HOLIDAY) as HOLI_SUM " +
                         "    FROM SALARY_MANAGEMENT S1 " +
                         "    JOIN ATTENDANCE A ON S1.ATT_ID = A.ATT_ID " +
                         "    WHERE TO_CHAR(A.ATT_DATE, 'YYYY-MM') = ? " +
                         "    GROUP BY S1.USER_ID" +
                         ") S ON u.USER_ID = S.USER_ID ";
            
            if (targetStatus.equals("N")) sql += "WHERE u.USER_ID NOT IN (SELECT USER_ID FROM SALARY_PAYMENT WHERE PAY_MONTH = ?) ";
            else sql += "WHERE u.USER_ID IN (SELECT USER_ID FROM SALARY_PAYMENT WHERE PAY_MONTH = ?) ";

            if (deptNum != -1) sql += "AND u.DEPT_NUM = ? ";
            sql += "ORDER BY u.USER_ID ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month); pstmt.setString(2, month);
            if (deptNum != -1) pstmt.setInt(3, deptNum);
            rs = pstmt.executeQuery();

            String title = targetStatus.equals("N") ? "미지급자 (처리 가능)" : "지급 완료자 (취소 가능)";
            System.out.println("+─────────────────────────────────────────────────────────────────────────+");
            System.out.println("                     " + month + " " + title + "                          ");
            System.out.println("+─────────────────────────────────────────────────────────────────────────+");
            System.out.println("  " + align("사번", 10) + align("이름", 12) + align("기본급", 16) + "실수령액");
            System.out.println("+─────────────────────────────────────────────────────────────────────────+");
            
            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                int total = rs.getInt("POSITION_SAL") + rs.getInt("OT_SUM") + rs.getInt("HOLI_SUM");
                System.out.println("  " + align(String.valueOf(rs.getInt("USER_ID")), 8) + 
                                   align(rs.getString("USER_NAME"), 12) + 
                                   align(String.format("%,d", rs.getInt("POSITION_SAL")) + "원", 16) + 
                                   String.format("%,d", (int)(total * 0.9)) + "원");
            }
            if(!hasData) System.out.println("  ! 대상 사원이 없습니다.");
            System.out.println("───────────────────────────────────────────────────────────────────────────");
        } catch (Exception e) { e.printStackTrace(); }
    }

    // [처리] 개별 지급 완료 (로직 유지, DTO 불필요)`
    public int processPayment(int userId, String month) {
        try {
            getConnection();
            String sql = "INSERT INTO SALARY_PAYMENT (PAY_ID, USER_ID, PAY_MONTH, ACTUAL_DATE, PAY_STATUS) VALUES (PAY_SEQ.NEXTVAL, ?, ?, SYSDATE, 'Y')";
            pstmt = conn.prepareStatement(sql); pstmt.setInt(1, userId); pstmt.setString(2, month);
            return pstmt.executeUpdate();
        } catch (Exception e) { return 0; }
    }

    // [처리] 부서별 일괄 지급 완료
    public int processPaymentBatch(int deptNum, String month) {
        int count = 0;
        try {
            getConnection();
            String sql = "SELECT USER_ID FROM USERTEST WHERE USER_ID NOT IN (SELECT USER_ID FROM SALARY_PAYMENT WHERE PAY_MONTH = ?) ";
            if (deptNum != -1) sql += "AND DEPT_NUM = ?";
            pstmt = conn.prepareStatement(sql); pstmt.setString(1, month);
            if (deptNum != -1) pstmt.setInt(2, deptNum);
            ResultSet rsList = pstmt.executeQuery();
            while(rsList.next()) { if(processPayment(rsList.getInt("USER_ID"), month) > 0) count++; }
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    // [처리] 개별 지급 취소
    public int cancelPayment(int userId, String month) {
        try {
            getConnection();
            String sql = "DELETE FROM SALARY_PAYMENT WHERE USER_ID = ? AND PAY_MONTH = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, month);
            return pstmt.executeUpdate();
        } catch (Exception e) { return 0; }
    }

    // [처리] 부서별 일괄 지급 취소
    public int cancelPaymentBatch(int deptNum, String month) {
        try {
            getConnection();
            String sql = "DELETE FROM SALARY_PAYMENT WHERE PAY_MONTH = ? ";
            if (deptNum != -1) sql += "AND USER_ID IN (SELECT USER_ID FROM USERTEST WHERE DEPT_NUM = ?)";
            pstmt = conn.prepareStatement(sql); pstmt.setString(1, month);
            if (deptNum != -1) pstmt.setInt(2, deptNum);
            return pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }
}