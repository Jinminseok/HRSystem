package kr.hrsystem.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.util.DBUtil;

public class salary_DAO {
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    private void getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        conn = DBUtil.getConnection();
    }

    public String align(String text, int length) {
        if (text == null) text = "-";
        int currentLength = 0;
        for (char c : text.toCharArray()) {
            if (c >= '\uAC00' && c <= '\uD7A3') currentLength += 2; 
            else currentLength += 1;
        }
        return text + " ".repeat(Math.max(0, length - currentLength));
    }

    public void showDepartmentList() {
        try {
            getConnection();
            String sql = "SELECT DEPT_NUM, DEPT_NAME FROM DEPT ORDER BY DEPT_NUM ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│              부서 목록 조회              │");
            System.out.println("+──────────────────────────────────────────+");            
            System.out.println("  " + align("부서번호", 14) + "부서이름   ");
            System.out.println("+──────────────────────────────────────────+");   
            while(rs.next()) {
                System.out.println("  " + align(String.valueOf(rs.getInt("DEPT_NUM")), 12) + rs.getString("DEPT_NAME"));
            }
            System.out.println("+──────────────────────────────────────────+");   
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void showUserListByDept(int deptNum) {
        try {
            getConnection();
            String sql = "SELECT USER_ID, USER_NAME FROM USERTEST " + (deptNum == -1 ? "" : "WHERE DEPT_NUM = ?") + " ORDER BY USER_ID ASC";
            pstmt = conn.prepareStatement(sql);
            if (deptNum != -1) pstmt.setInt(1, deptNum);
            rs = pstmt.executeQuery();
            System.out.println("+──────────────────────────────────────────+");   
            System.out.println("│              사원 목록 조회              │");
            System.out.println("+──────────────────────────────────────────+");  
            System.out.println("│사번              이름                    │");
            System.out.println("────────────────────────────────────────────");   
            while(rs.next()) {
                System.out.println(" " + align(String.valueOf(rs.getInt("USER_ID")), 12) + rs.getString("USER_NAME"));
            }
            System.out.println("+──────────────────────────────────────────+");   
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ✅ [1번 메뉴용] 모든 미등록 근태 조회 (야근 여부 상관없이!)
    public void showUnpaidWorkers(int deptNum) {
        try {
            getConnection();
            // 기존의 OUT_STATUS 필터를 제거하여 '출근은 했는데 급여 등록 안 된 모든 건'을 찾습니다.
            String sql = "SELECT a.ATT_ID, u.USER_ID, u.USER_NAME, d.DEPT_NAME, " +
                         "TO_CHAR(a.ATT_DATE, 'YYYY-MM-DD') as ADATE, " +
                         "TO_CHAR(a.CHECK_IN, 'HH24:MI') as CIN, TO_CHAR(a.CHECK_OUT, 'HH24:MI') as COUT " +
                         "FROM ATTENDANCE a " +
                         "JOIN USERTEST u ON a.USER_ID = u.USER_ID " +
                         "JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                         "WHERE a.ATT_ID NOT IN (SELECT ATT_ID FROM SALARY_MANAGEMENT WHERE ATT_ID IS NOT NULL) ";
            
            if (deptNum != -1) sql += "AND u.DEPT_NUM = ? ";
            sql += "ORDER BY a.ATT_DATE DESC";

            pstmt = conn.prepareStatement(sql);
            if (deptNum != -1) pstmt.setInt(1, deptNum);
            rs = pstmt.executeQuery();

            System.out.println("+─────────────────────────────────────────────────────────────────────────+");   
            System.out.println("│                   수당 미등록 내역 (등록이 필요한 내역)                 │");
            System.out.println("+─────────────────────────────────────────────────────────────────────────+");            
            System.out.println("  " + align("근태ID", 10) + align("사번", 12) + align("이름", 12) + align("부서", 12) + align("날짜", 14) + "시각(출/퇴)");
            System.out.println("───────────────────────────────────────────────────────────────────────────");
            
            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                String time = rs.getString("CIN") + " ~ " + rs.getString("COUT");
                System.out.println(" " + align(String.valueOf(rs.getInt("ATT_ID")), 10) + 
                                   align(String.valueOf(rs.getInt("USER_ID")), 10) + 
                                   align(rs.getString("USER_NAME"), 12) + 
                                   align(rs.getString("DEPT_NAME"), 12) + 
                                   align(rs.getString("ADATE"), 14) + time);
            }
            if(!hasData) System.out.println("  ! 처리할 미등록 내역이 없습니다.");
            System.out.println("+─────────────────────────────────────────────────────────────────────────+");  
        } catch (Exception e) { e.printStackTrace(); }
    }

    public boolean showAttendanceList(int userId) {
        boolean hasData = false;
        try {
            getConnection();
            String sql = "SELECT ATT_ID, TO_CHAR(ATT_DATE, 'YYYY-MM-DD') as ADATE, TO_CHAR(CHECK_IN, 'HH24:MI') as CIN, TO_CHAR(CHECK_OUT, 'HH24:MI') as COUT " +
                         "FROM ATTENDANCE WHERE USER_ID = ? AND ATT_ID NOT IN (SELECT ATT_ID FROM SALARY_MANAGEMENT WHERE ATT_ID IS NOT NULL)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            // 데이터가 있는지 먼저 확인
            if (rs.next()) {
                hasData = true;
                System.out.println("+────────────────────────────────────────────────────────────────────────+");                                        
                System.out.println("│                ○ [" + userId + "]번 사원 미등록 수당 내역                │");
                System.out.println("+────────────────────────────────────────────────────────────────────────+");   
                System.out.println("  " + align("근태ID", 10) + align("날짜", 14) + "시각(출근/퇴근)         ");
                do {
                    System.out.println("  " + align(String.valueOf(rs.getInt("ATT_ID")), 10) + 
                                       align(rs.getString("ADATE"), 14) + rs.getString("CIN") + " / " + rs.getString("COUT"));
                } while(rs.next());
                System.out.println("──────────────────────────────────────────────────────────────────────────");  
            }
        } catch (Exception e) { e.printStackTrace(); }
        return hasData; // ★ [FIXED: 데이터가 없으면 false 반환]
    }

    // ✅ [3번 메뉴용] 이미 등록된 내역만 조회 (INNER JOIN)
    public List<Map<String, Object>> selectSalaryList(int deptNum) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            getConnection();
            // INNER JOIN을 사용하여 SALARY_MANAGEMENT에 데이터가 있는 경우만 가져옵니다.
            String sql = "SELECT s.*, u.USER_NAME " +
                         "FROM SALARY_MANAGEMENT s " +
                         "JOIN USERTEST u ON s.USER_ID = u.USER_ID ";
            if (deptNum != -1) sql += "WHERE u.DEPT_NUM = ? ";
            sql += "ORDER BY s.SAL_ID DESC";
            
            pstmt = conn.prepareStatement(sql);
            if (deptNum != -1) pstmt.setInt(1, deptNum);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("salId", rs.getInt("SAL_ID"));
                map.put("userId", rs.getInt("USER_ID"));
                map.put("userName", rs.getString("USER_NAME"));
                map.put("salBase", rs.getInt("SAL_BASE"));
                map.put("salOvertime", rs.getInt("SAL_OVERTIME"));
                map.put("salHoliday", rs.getInt("SAL_HOLIDAY"));
                map.put("salTax", rs.getInt("SAL_TAX"));
                list.add(map);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public Map<String, Object> selectMonthlySummary(int userId, String month) {
        Map<String, Object> map = null;
        try {
            getConnection();
            // 💡 핵심 수정: 서브쿼리를 사용하여 특정 월(month)의 급여 데이터만 미리 필터링한 후 JOIN 합니다.
            String sql = "SELECT u.USER_NAME, p.POSITION_SAL, " +
                         "       NVL(SUM(s.SUM_OT), 0) as SUM_OT, " +
                         "       NVL(SUM(s.SUM_HOLI), 0) as SUM_HOLI " +
                         "FROM USERTEST u " +
                         "JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                         "LEFT JOIN (" +
                         "    SELECT sm.USER_ID, sm.SAL_OVERTIME as SUM_OT, sm.SAL_HOLIDAY as SUM_HOLI " +
                         "    FROM SALARY_MANAGEMENT sm " +
                         "    JOIN ATTENDANCE a ON sm.ATT_ID = a.ATT_ID " +
                         "    WHERE TO_CHAR(a.ATT_DATE, 'YYYY-MM') = ?" +
                         ") s ON u.USER_ID = s.USER_ID " +
                         "WHERE u.USER_ID = ? " +
                         "GROUP BY u.USER_NAME, p.POSITION_SAL";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month); // 서브쿼리의 날짜 조건
            pstmt.setInt(2, userId);    // 전체 쿼리의 사번 조건
            rs = pstmt.executeQuery();

            if (rs.next()) {
                map = new HashMap<>();
                map.put("userName", rs.getString("USER_NAME"));
                int base = rs.getInt("POSITION_SAL");
                int ot = rs.getInt("SUM_OT");
                int holi = rs.getInt("SUM_HOLI");
                
                // 세금 10% 계산
                int tax = (int)((base + ot + holi) * 0.1);
                
                map.put("salBase", base);
                map.put("salOvertime", ot);
                map.put("salHoliday", holi);
                map.put("salTax", tax);
                map.put("salTotal", (base + ot + holi) - tax);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return map;
    }

    public int insertSalary(int userId, int attId) {
        try {
            getConnection();
            // 1. 사원의 직급 및 기본급 정보 가져오기
            String userPosSql = "SELECT u.POSITION_NUM, p.POSITION_SAL FROM USERTEST u JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM WHERE u.USER_ID = ?";
            pstmt = conn.prepareStatement(userPosSql); pstmt.setInt(1, userId); rs = pstmt.executeQuery();
            int posNum = 0, basePay = 0; 
            if(rs.next()) { 
                posNum = rs.getInt("POSITION_NUM"); 
                basePay = rs.getInt("POSITION_SAL"); 
            } else { return 0; } // 사원이 없으면 실패

            // 2. ★ [FIXED: 근태ID와 사번이 일치하는지 엄격 체크]
            String attSql = "SELECT CHECK_IN, CHECK_OUT, TO_CHAR(ATT_DATE, 'D') as DAY_NUM " +
                            "FROM ATTENDANCE WHERE ATT_ID = ? AND USER_ID = ?"; // 💡 USER_ID 조건 추가
            pstmt = conn.prepareStatement(attSql); 
            pstmt.setInt(1, attId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            
            int otPay = 0, holiPay = 0;
            if(rs.next()) {
                Timestamp in = rs.getTimestamp("CHECK_IN"); 
                Timestamp out = rs.getTimestamp("CHECK_OUT");
                if(out == null) return 0; // 퇴근 기록 없으면 수당 산출 불가

                int hours = (int)((out.getTime() - in.getTime()) / 3600000);
                if(rs.getString("DAY_NUM").equals("1") || rs.getString("DAY_NUM").equals("7")) {
                    holiPay = hours * 25000;
                } else { 
                    long seventeen = Timestamp.valueOf(out.toLocalDateTime().toLocalDate().atTime(17, 0)).getTime();
                    if(out.getTime() > seventeen) otPay = (int)((out.getTime() - seventeen) / 3600000) * 15000; 
                }
            } else {
                // ★ [FIXED: 근태ID가 없거나 해당 사원의 것이 아니면 여기서 0을 반환]
                return 0; 
            }

            // 3. 실제 등록 진행
            int tax = (int)((basePay + otPay + holiPay) * 0.1);
            String ins = "INSERT INTO SALARY_MANAGEMENT (SAL_ID, USER_ID, POSITION_NUM, ATT_ID, SAL_BASE, SAL_OVERTIME, SAL_HOLIDAY, SAL_TAX) VALUES (SAL_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(ins); 
            pstmt.setInt(1, userId); pstmt.setInt(2, posNum); pstmt.setInt(3, attId); 
            pstmt.setInt(4, basePay); pstmt.setInt(5, otPay); pstmt.setInt(6, holiPay); pstmt.setInt(7, tax);
            
            return pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    public int insertSalaryBatch(int deptNum) {
        int count = 0;
        try {
            getConnection();
            // 수정 포인트: 필터 없이 모든 미등록 건을 일괄 처리
            String sql = "SELECT ATT_ID, USER_ID FROM ATTENDANCE WHERE ATT_ID NOT IN (SELECT ATT_ID FROM SALARY_MANAGEMENT WHERE ATT_ID IS NOT NULL) ";
            if (deptNum != -1) sql += "AND USER_ID IN (SELECT USER_ID FROM USERTEST WHERE DEPT_NUM = ?)";
            pstmt = conn.prepareStatement(sql); if (deptNum != -1) pstmt.setInt(1, deptNum);
            ResultSet rsList = pstmt.executeQuery();
            while(rsList.next()) if(insertSalary(rsList.getInt("USER_ID"), rsList.getInt("ATT_ID")) > 0) count++;
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    public int updateSalary(int sid, int ot, int ho) {
        try { getConnection(); String sql = "UPDATE SALARY_MANAGEMENT SET SAL_OVERTIME = ?, SAL_HOLIDAY = ?, SAL_TAX = (SAL_BASE + ? + ?) * 0.1 WHERE SAL_ID = ?";
            pstmt = conn.prepareStatement(sql); pstmt.setInt(1, ot); pstmt.setInt(2, ho); pstmt.setInt(3, ot); pstmt.setInt(4, ho); pstmt.setInt(5, sid); return pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    public int deleteSalary(int sid) {
        try { getConnection(); String sql = "DELETE FROM SALARY_MANAGEMENT WHERE SAL_ID = ?"; pstmt = conn.prepareStatement(sql); pstmt.setInt(1, sid); return pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    public int deleteSalaryBatch(int deptNum) {
        try { getConnection(); String sql = "DELETE FROM SALARY_MANAGEMENT "; if (deptNum != -1) sql += "WHERE USER_ID IN (SELECT USER_ID FROM USERTEST WHERE DEPT_NUM = ?)";
            pstmt = conn.prepareStatement(sql); if (deptNum != -1) pstmt.setInt(1, deptNum); return pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }
    
 // ★ [FIXED: 부서 존재 여부 확인 메서드 추가]
    public boolean checkDeptExists(int deptNum) {
        boolean exists = false;
        try {
            getConnection();
            String sql = "SELECT COUNT(*) FROM DEPT WHERE DEPT_NUM = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, deptNum);
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) exists = true;
        } catch (Exception e) { e.printStackTrace(); }
        return exists;
    }
    
 // ★ [FIXED: 사원 존재 여부 확인 메서드 추가]
    public boolean checkUserExists(int userId) {
        boolean exists = false;
        try {
            getConnection();
            String sql = "SELECT COUNT(*) FROM USERTEST WHERE USER_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) exists = true;
        } catch (Exception e) { e.printStackTrace(); }
        return exists;
    }
}