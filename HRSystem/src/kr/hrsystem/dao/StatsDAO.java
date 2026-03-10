package kr.hrsystem.dao;

import java.sql.*;
import java.util.*;
import kr.util.DBUtil;

public class StatsDAO {
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

    // =========================================================================
    // [1] 인사 상태 통계
    // =========================================================================
    public void showWorkStatusStats(int type, String typeVal, int dateType, String startDate, String endDate) {
        try {
            getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT u.USER_ID, u.USER_NAME, d.DEPT_NAME, p.POSITION_NAME, u.EMP_STATUS, " +
                "TO_CHAR(u.JOIN_DATE, 'YYYY-MM-DD') as JDATE FROM USERTEST u " +
                "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                "LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "WHERE 1=1 "
            );

            // 1. 부서/직급 필터 (LIKE 검색으로 오타 방지)
            if (type == 2) sql.append("AND d.DEPT_NAME LIKE ? ");
            else if (type == 3) sql.append("AND p.POSITION_NAME LIKE ? ");

            // 2. 날짜 필터 💡 핵심 수정: '= ?' 가 아니라 '<= ?' 로 변경하여 재직자 모두 포함
            // (단, 퇴사자는 현재 상태가 RESIGNED인 경우를 제외하거나 리스트에서 필터링)
            if (dateType == 2) sql.append("AND TO_CHAR(u.JOIN_DATE, 'YYYY-MM') <= ? "); // 선택 월까지 입사한 사람
            else if (dateType == 3) sql.append("AND TO_CHAR(u.JOIN_DATE, 'YYYY') <= ? ");    // 선택 년까지 입사한 사람
            else if (dateType == 4) sql.append("AND u.JOIN_DATE <= TO_DATE(?, 'YYYY-MM-DD') "); // 선택 종료일 기준 재직

            sql.append("ORDER BY u.USER_ID ASC");

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;

            // 3. 파라미터 셋팅 순서 (반드시 지켜야 함!)
            // (1) 부서/직급 값 먼저 넣기
            if (type > 1) {
                pstmt.setString(idx++, "%" + typeVal.trim() + "%");
            }
            // (2) 날짜 값 넣기
            if (dateType == 2 || dateType == 3) {
                pstmt.setString(idx++, startDate);
            } else if (dateType == 4) {
                pstmt.setString(idx++, endDate); // 종료일 기준으로 그 전 입사자 검색
            }

            rs = pstmt.executeQuery();
            
            // --- 이하 출력 로직은 기존 UI에 맞춰 유지 ---
            System.out.println("\n  ● 인사 상태 상세 명단");
            System.out.println("  " + "━".repeat(75));
            System.out.println("  " + align("사번", 8) + align("이름", 10) + align("부서", 14) + align("직급", 12) + "근무상태");
            System.out.println("  " + "─".repeat(75));

            int total = 0, work = 0, leave = 0;
            boolean hasData = false;
            
            while(rs.next()) {
                String status = rs.getString("EMP_STATUS").toUpperCase();
                
                // 💡 퇴사자는 통계에서 제외하고 재직/휴직만 출력 (사용자 요청사항)
                if ("RESIGNED".equals(status)) continue; 

                hasData = true;
                total++;
                if ("WORK".equals(status)) work++; 
                else if ("LEAVE".equals(status)) leave++;

                System.out.println("  " + align(String.valueOf(rs.getInt("USER_ID")), 8) + 
                                   align(rs.getString("USER_NAME"), 10) + 
                                   align(rs.getString("DEPT_NAME"), 14) + 
                                   align(rs.getString("POSITION_NAME"), 12) + status);
            }
            
            if (!hasData) System.out.println("  ! 조건에 맞는 사원이 없습니다.");
            
            System.out.println("  " + "─".repeat(75));
            System.out.println("  [📊 인사 결과] 총 " + total + "명 (재직: " + work + " / 휴직: " + leave + ")");
            System.out.println("  " + "━".repeat(75));

        } catch (Exception e) { e.printStackTrace(); }
    }

    // =========================================================================
    // [2] 근태 기록 통계
    // =========================================================================
    public void showAttendanceStats(int type, String typeVal, int dateType, String start, String end) {
        try {
            getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT u.USER_NAME, d.DEPT_NAME, p.POSITION_NAME, TO_CHAR(a.ATT_DATE, 'YYYY-MM-DD') as ADATE, " +
                "a.IN_STATUS, a.OUT_STATUS, TO_CHAR(a.CHECK_IN, 'HH24:MI') as CIN, TO_CHAR(a.CHECK_OUT, 'HH24:MI') as COUT, " +
                "ROUND((a.CHECK_OUT - a.CHECK_IN) * 24, 1) as HRS, TO_CHAR(a.ATT_DATE, 'D') as DNUM " +
                "FROM ATTENDANCE a JOIN USERTEST u ON a.USER_ID = u.USER_ID " +
                "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM WHERE 1=1 "
            );

            if (type == 2) sql.append("AND d.DEPT_NAME LIKE ? ");
            else if (type == 3) sql.append("AND p.POSITION_NAME LIKE ? ");
            if (dateType == 2) sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY-MM-DD') = ? ");
            else if (dateType == 3) sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY-MM') = ? ");
            else if (dateType == 4) sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY') = ? ");
            else if (dateType == 5) sql.append("AND a.ATT_DATE BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') ");

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (type > 1) {
                pstmt.setString(idx++, "%" + typeVal.trim() + "%");
            }
            if (dateType >= 2 && dateType <= 4) {
                pstmt.setString(idx++, start);
            } else if (dateType == 5) {
                pstmt.setString(idx++, start);
                pstmt.setString(idx++, end);
            }

            rs = pstmt.executeQuery();
            System.out.println("\n  ● 근태 기록 상세 현황");
            System.out.println("  " + "━".repeat(95));
            System.out.println("  " + align("날짜", 12) + align("이름", 10) + align("부서", 14) + align("출근", 8) + align("퇴근", 8) + align("상태", 12) + "시간");
            System.out.println("  " + "─".repeat(95));

            int nIn = 0, lIn = 0, nOut = 0, eOut = 0, oOut = 0, hol = 0;
            while(rs.next()) {
                int iS = rs.getInt("IN_STATUS"), oS = rs.getInt("OUT_STATUS"), dN = rs.getInt("DNUM");
                if (dN == 1 || dN == 7) hol++;
                else {
                    if (iS == 1) nIn++; else if (iS == 2) lIn++;
                    if (oS == 1) nOut++; else if (oS == 2) eOut++; else if (oS == 3) oOut++;
                }
                String sStr = (iS == 2 ? "지각" : "정상") + "/" + (oS == 2 ? "조퇴" : (oS == 3 ? "야근" : "정상"));
                System.out.println("  " + align(rs.getString("ADATE"), 12) + align(rs.getString("USER_NAME"), 10) + align(rs.getString("DEPT_NAME"), 14) + align(rs.getString("CIN"), 8) + align(rs.getString("COUT"), 8) + align(sStr, 12) + rs.getDouble("HRS") + "h");
            }
            System.out.println("  " + "─".repeat(95));
            System.out.println("  [📊 집계] 출근(정상 " + nIn + "/지각 " + lIn + ") | 퇴근(정상 " + nOut + "/조퇴 " + eOut + "/야근 " + oOut + ") | 휴일 " + hol + "건");
            System.out.println("  " + "━".repeat(95));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // =========================================================================
    // [3] 급여 통계 (salary_DAO의 테이블 구조 SALARY_MANAGEMENT 활용)
    // =========================================================================
    public void showSalaryStats(int type, String typeVal, int dateType, String start, String end) {
        try {
            getConnection();
            // 💡 핵심: salary_DAO가 사용하는 SALARY_MANAGEMENT 테이블의 값을 그대로 SUM 합니다.
            StringBuilder sql = new StringBuilder(
                "SELECT u.USER_NAME, p.POSITION_NAME, d.DEPT_NAME, " +
                "SUM(s.SAL_BASE) as T_BASE, SUM(s.SAL_OVERTIME) as T_OT, " +
                "SUM(s.SAL_HOLIDAY) as T_HOL, SUM(s.SAL_TAX) as T_TAX " +
                "FROM SALARY_MANAGEMENT s " +
                "JOIN USERTEST u ON s.USER_ID = u.USER_ID " +
                "JOIN ATTENDANCE a ON s.ATT_ID = a.ATT_ID " +
                "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                "LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "WHERE 1=1 "
            );

            if (dateType == 2) sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY-MM') = ? ");
            else if (dateType == 3) sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY') = ? ");
            else if (dateType == 4) sql.append("AND a.ATT_DATE BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') ");
            if (type == 2) sql.append("AND d.DEPT_NAME LIKE ? ");
            else if (type == 3) sql.append("AND p.POSITION_NAME LIKE ? ");

            sql.append("GROUP BY u.USER_ID, u.USER_NAME, p.POSITION_NAME, d.DEPT_NAME ORDER BY T_BASE DESC");

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (type > 1) {
                pstmt.setString(idx++, "%" + typeVal.trim() + "%");
            }
            if (dateType == 2 || dateType == 3) {
                pstmt.setString(idx++, start);
            } else if (dateType == 4) {
                pstmt.setString(idx++, start);
                pstmt.setString(idx++, end);
            }
            

            rs = pstmt.executeQuery();
            System.out.println("\n  ● 급여 지급 집계 리스트");
            System.out.println("  " + "━".repeat(110));
            System.out.println("  " + align("이름", 10) + align("직급", 10) + align("부서", 14) + align("기본급", 14) + align("야근수당", 14) + align("휴일수당", 14) + align("세금", 12) + "총수령액");
            System.out.println("  " + "─".repeat(110));

            long gB = 0, gO = 0, gH = 0, gT = 0, gNet = 0;
            while(rs.next()) {
                long b = rs.getLong("T_BASE"), ot = rs.getLong("T_OT"), h = rs.getLong("T_HOL"), t = rs.getLong("T_TAX");
                long net = b + ot + h - t;
                gB += b; gO += ot; gH += h; gT += t; gNet += net;
                System.out.println("  " + align(rs.getString("USER_NAME"), 10) + align(rs.getString("POSITION_NAME"), 10) + align(rs.getString("DEPT_NAME"), 14) + 
                                   align(String.format("%,d", b), 14) + align(String.format("%,d", ot), 14) + 
                                   align(String.format("%,d", h), 14) + align(String.format("%,d", t), 12) + String.format("%,d원", net));
            }
            System.out.println("  " + "─".repeat(110));
            System.out.printf("  [📊 집계 총괄] 기본급: %,d | 야근: %,d | 휴일: %,d | 세금: %,d\n", gB, gO, gH, gT);
            System.out.printf("  💰 최종 실지급액 총계: %,d원\n", gNet);
            System.out.println("  " + "━".repeat(110));
        } catch (Exception e) { e.printStackTrace(); }
    }
}