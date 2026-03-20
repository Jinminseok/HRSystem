package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
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

    // ==========================
    // 부서 목록 조회
    // ==========================
    public void showDepartmentList() {
        try {
            getConnection();

            String sql = "SELECT DEPT_NUM, DEPT_NAME FROM DEPT ORDER BY DEPT_NUM ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            printDivider(34);
            System.out.println("부서 목록 조회");
            printDivider(34);

            System.out.println(
                    pad("부서번호", 12) +
                    pad("부서명", 20)
            );

            System.out.println("-".repeat(34));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println(
                        pad(String.valueOf(rs.getInt("DEPT_NUM")), 12) +
                        pad(rs.getString("DEPT_NAME"), 20)
                );
            }

            if (!hasData) {
                System.out.println("등록된 부서가 없습니다.");
            }

            printDivider(34);

        } catch (Exception e) {
            System.out.println("❌ 부서 목록 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }
    }

    // ==========================
    // 부서별 / 전체 사원 목록 조회
    // ==========================
    public void showUserListByDept(int deptNum) {
        try {
            getConnection();

            String sql = "SELECT USER_ID, USER_NAME " +
                         "FROM USERTEST " +
                         (deptNum == -1 ? "" : "WHERE DEPT_NUM = ? ") +
                         "ORDER BY USER_ID ASC";

            pstmt = conn.prepareStatement(sql);
            if (deptNum != -1) {
                pstmt.setInt(1, deptNum);
            }

            rs = pstmt.executeQuery();

            printDivider(26);
            System.out.println("사원 목록 조회");
            printDivider(26);

            System.out.println(
                    pad("사번", 12) +
                    pad("이름", 12)
            );

            System.out.println("-".repeat(26));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 12) +
                        pad(rs.getString("USER_NAME"), 12)
                );
            }

            if (!hasData) {
                System.out.println("조회된 사원이 없습니다.");
            }

            printDivider(26);

        } catch (Exception e) {
            System.out.println("❌ 사원 목록 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }
    }

    // ==========================
    // 수당 미등록 내역 조회
    // ==========================
    public void showUnpaidWorkers(int deptNum) {
        try {
            getConnection();

            String sql =
                    "SELECT a.ATT_ID, u.USER_ID, u.USER_NAME, d.DEPT_NAME, " +
                    "       TO_CHAR(a.ATT_DATE, 'YYYY-MM-DD') AS ADATE, " +
                    "       TO_CHAR(a.CHECK_IN, 'HH24:MI') AS CIN, " +
                    "       TO_CHAR(a.CHECK_OUT, 'HH24:MI') AS COUT " +
                    "FROM ATTENDANCE a " +
                    "JOIN USERTEST u ON a.USER_ID = u.USER_ID " +
                    "JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                    "WHERE a.ATT_ID NOT IN (SELECT ATT_ID FROM SALARY_MANAGEMENT WHERE ATT_ID IS NOT NULL) " +
                    "AND (TO_CHAR(a.ATT_DATE, 'D') IN ('1', '7') " +
                    "OR TO_CHAR(a.CHECK_OUT, 'HH24:MI') >= '18:00') ";

            if (deptNum != -1) {
                sql += "AND u.DEPT_NUM = ? ";
            }

            sql += "ORDER BY a.ATT_DATE DESC";

            pstmt = conn.prepareStatement(sql);
            if (deptNum != -1) {
                pstmt.setInt(1, deptNum);
            }

            rs = pstmt.executeQuery();

            printDivider(74);
            System.out.println("수당 미등록 내역 조회");
            printDivider(74);

            System.out.println(
                    pad("근태ID", 8) +
                    pad("사번", 8) +
                    pad("이름", 10) +
                    pad("부서", 12) +
                    pad("날짜", 14) +
                    pad("출근", 8) +
                    pad("퇴근", 8)
            );

            System.out.println("-".repeat(74));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;

                System.out.println(
                        pad(String.valueOf(rs.getInt("ATT_ID")), 8) +
                        pad(String.valueOf(rs.getInt("USER_ID")), 8) +
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(rs.getString("DEPT_NAME"), 12) +
                        pad(rs.getString("ADATE"), 14) +
                        pad(nvl(rs.getString("CIN")), 8) +
                        pad(nvl(rs.getString("COUT")), 8)
                );
            }

            if (!hasData) {
                System.out.println("처리할 미등록 내역이 없습니다.");
            }

            printDivider(74);

        } catch (Exception e) {
            System.out.println("❌ 수당 미등록 내역 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }
    }

    // ==========================
    // 특정 사원의 미등록 수당 대상 근태 조회
    // ==========================
    public boolean showAttendanceList(int userId) {
        boolean hasData = false;

        try {
            getConnection();

            String sql =
                    "SELECT ATT_ID, " +
                    "       TO_CHAR(ATT_DATE, 'YYYY-MM-DD') AS ADATE, " +
                    "       TO_CHAR(CHECK_IN, 'HH24:MI') AS CIN, " +
                    "       TO_CHAR(CHECK_OUT, 'HH24:MI') AS COUT " +
                    "FROM ATTENDANCE " +
                    "WHERE USER_ID = ? " +
                    "AND ATT_ID NOT IN (SELECT ATT_ID FROM SALARY_MANAGEMENT WHERE ATT_ID IS NOT NULL)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                hasData = true;

                printDivider(44);
                System.out.println("[" + userId + "]번 사원 미등록 수당 내역");
                printDivider(44);

                System.out.println(
                        pad("근태ID", 10) +
                        pad("날짜", 14) +
                        pad("출근", 8) +
                        pad("퇴근", 8)
                );

                System.out.println("-".repeat(44));

                do {
                    System.out.println(
                            pad(String.valueOf(rs.getInt("ATT_ID")), 10) +
                            pad(rs.getString("ADATE"), 14) +
                            pad(nvl(rs.getString("CIN")), 8) +
                            pad(nvl(rs.getString("COUT")), 8)
                    );
                } while (rs.next());

                printDivider(44);
            }

        } catch (Exception e) {
            System.out.println("❌ 사원 근태 목록 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }

        return hasData;
    }

    // ==========================
    // 등록된 급여 내역 조회
    // ==========================
    public List<Map<String, Object>> selectSalaryList(int deptNum) {
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            getConnection();

            String sql =
                    "SELECT s.*, u.USER_NAME " +
                    "FROM SALARY_MANAGEMENT s " +
                    "JOIN USERTEST u ON s.USER_ID = u.USER_ID ";

            if (deptNum != -1) {
                sql += "WHERE u.DEPT_NUM = ? ";
            }

            sql += "ORDER BY s.SAL_ID DESC";

            pstmt = conn.prepareStatement(sql);
            if (deptNum != -1) {
                pstmt.setInt(1, deptNum);
            }

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

        } catch (Exception e) {
            System.out.println("❌ 급여 목록 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }

        return list;
    }

    // ==========================
    // 월별 총급여 조회
    // ==========================
    public Map<String, Object> selectMonthlySummary(int userId, String month) {
        Map<String, Object> map = null;

        try {
            getConnection();

            String sql =
                    "SELECT u.USER_NAME, p.POSITION_SAL, " +
                    "       NVL(SUM(s.SUM_OT), 0) AS SUM_OT, " +
                    "       NVL(SUM(s.SUM_HOLI), 0) AS SUM_HOLI " +
                    "FROM USERTEST u " +
                    "JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                    "LEFT JOIN ( " +
                    "    SELECT sm.USER_ID, sm.SAL_OVERTIME AS SUM_OT, sm.SAL_HOLIDAY AS SUM_HOLI " +
                    "    FROM SALARY_MANAGEMENT sm " +
                    "    JOIN ATTENDANCE a ON sm.ATT_ID = a.ATT_ID " +
                    "    WHERE TO_CHAR(a.ATT_DATE, 'YYYY-MM') = ? " +
                    ") s ON u.USER_ID = s.USER_ID " +
                    "WHERE u.USER_ID = ? " +
                    "GROUP BY u.USER_NAME, p.POSITION_SAL";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);
            pstmt.setInt(2, userId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                map = new HashMap<>();

                map.put("userName", rs.getString("USER_NAME"));

                int base = rs.getInt("POSITION_SAL");
                int ot = rs.getInt("SUM_OT");
                int holi = rs.getInt("SUM_HOLI");
                int tax = (int) ((base + ot + holi) * 0.1);

                map.put("salBase", base);
                map.put("salOvertime", ot);
                map.put("salHoliday", holi);
                map.put("salTax", tax);
                map.put("salTotal", (base + ot + holi) - tax);
            }

        } catch (Exception e) {
            System.out.println("❌ 월별 총급여 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }

        return map;
    }

    // ==========================
    // 급여 1건 등록
    // ==========================
    public int insertSalary(int userId, int attId) {
        try {
            getConnection();

            String userPosSql =
                    "SELECT u.POSITION_NUM, p.POSITION_SAL " +
                    "FROM USERTEST u " +
                    "JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                    "WHERE u.USER_ID = ?";

            pstmt = conn.prepareStatement(userPosSql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            int posNum = 0;
            int basePay = 0;

            if (rs.next()) {
                posNum = rs.getInt("POSITION_NUM");
                basePay = rs.getInt("POSITION_SAL");
            } else {
                return 0;
            }

            DBUtil.executeClose(rs, pstmt, null);
            rs = null;
            pstmt = null;

            String attSql =
                    "SELECT CHECK_IN, CHECK_OUT, TO_CHAR(ATT_DATE, 'D') AS DAY_NUM " +
                    "FROM ATTENDANCE " +
                    "WHERE ATT_ID = ? AND USER_ID = ?";

            pstmt = conn.prepareStatement(attSql);
            pstmt.setInt(1, attId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();

            int otPay = 0;
            int holiPay = 0;

            if (rs.next()) {
                Timestamp in = rs.getTimestamp("CHECK_IN");
                Timestamp out = rs.getTimestamp("CHECK_OUT");

                if (out == null) {
                    return 0;
                }

                int hours = (int) ((out.getTime() - in.getTime()) / 3600000);

                if ("1".equals(rs.getString("DAY_NUM")) || "7".equals(rs.getString("DAY_NUM"))) {
                    holiPay = hours * 25000;
                } else {
                    long seventeen = Timestamp.valueOf(
                            out.toLocalDateTime().toLocalDate().atTime(17, 0)
                    ).getTime();

                    if (out.getTime() > seventeen) {
                        otPay = (int) ((out.getTime() - seventeen) / 3600000) * 15000;
                    }
                }
            } else {
                return 0;
            }

            DBUtil.executeClose(rs, pstmt, null);
            rs = null;
            pstmt = null;

            int tax = (int) ((basePay + otPay + holiPay) * 0.1);

            String ins =
                    "INSERT INTO SALARY_MANAGEMENT " +
                    "(SAL_ID, USER_ID, POSITION_NUM, ATT_ID, SAL_BASE, SAL_OVERTIME, SAL_HOLIDAY, SAL_TAX) " +
                    "VALUES (SAL_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(ins);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, posNum);
            pstmt.setInt(3, attId);
            pstmt.setInt(4, basePay);
            pstmt.setInt(5, otPay);
            pstmt.setInt(6, holiPay);
            pstmt.setInt(7, tax);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("❌ 급여 등록 중 오류가 발생했습니다.");
            return 0;
        } finally {
            closeResources();
        }
    }

    // ==========================
    // 급여 일괄 등록
    // ==========================
    public int insertSalaryBatch(int deptNum) {
        int count = 0;
        ResultSet rsList = null;
        PreparedStatement pstmtList = null;

        try {
            getConnection();

            String sql =
                    "SELECT a.ATT_ID, a.USER_ID " +
                    "FROM ATTENDANCE a " +
                    "JOIN USERTEST u ON a.USER_ID = u.USER_ID " +
                    "WHERE a.ATT_ID NOT IN (SELECT ATT_ID FROM SALARY_MANAGEMENT WHERE ATT_ID IS NOT NULL) " +
                    "AND (TO_CHAR(a.ATT_DATE, 'D') IN ('1', '7') " +
                    "OR TO_CHAR(a.CHECK_OUT, 'HH24:MI') >= '18:00') ";

            if (deptNum != -1) {
                sql += "AND u.DEPT_NUM = ? ";
            }

            pstmtList = conn.prepareStatement(sql);
            if (deptNum != -1) {
                pstmtList.setInt(1, deptNum);
            }

            rsList = pstmtList.executeQuery();

            while (rsList.next()) {
                if (insertSalary(rsList.getInt("USER_ID"), rsList.getInt("ATT_ID")) > 0) {
                    count++;
                }
            }

        } catch (Exception e) {
            System.out.println("❌ 급여 일괄 등록 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(rsList, pstmtList, null);
            closeResources();
        }

        return count;
    }

    // ==========================
    // 급여 수정
    // ==========================
    public int updateSalary(int sid, int ot, int ho) {
        try {
            getConnection();

            String sql =
                    "UPDATE SALARY_MANAGEMENT " +
                    "SET SAL_OVERTIME = ?, " +
                    "    SAL_HOLIDAY = ?, " +
                    "    SAL_TAX = (SAL_BASE + ? + ?) * 0.1 " +
                    "WHERE SAL_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, ot);
            pstmt.setInt(2, ho);
            pstmt.setInt(3, ot);
            pstmt.setInt(4, ho);
            pstmt.setInt(5, sid);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("❌ 급여 수정 중 오류가 발생했습니다.");
            return 0;
        } finally {
            closeResources();
        }
    }

    // ==========================
    // 급여 삭제
    // ==========================
    public int deleteSalary(int sid) {
        try {
            getConnection();

            String sql = "DELETE FROM SALARY_MANAGEMENT WHERE SAL_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sid);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("❌ 급여 삭제 중 오류가 발생했습니다.");
            return 0;
        } finally {
            closeResources();
        }
    }

    // ==========================
    // 급여 일괄 삭제
    // ==========================
    public int deleteSalaryBatch(int deptNum) {
        try {
            getConnection();

            String sql = "DELETE FROM SALARY_MANAGEMENT ";
            if (deptNum != -1) {
                sql += "WHERE USER_ID IN (SELECT USER_ID FROM USERTEST WHERE DEPT_NUM = ?)";
            }

            pstmt = conn.prepareStatement(sql);
            if (deptNum != -1) {
                pstmt.setInt(1, deptNum);
            }

            return pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("❌ 급여 일괄 삭제 중 오류가 발생했습니다.");
            return 0;
        } finally {
            closeResources();
        }
    }

    // ==========================
    // 부서 존재 여부 확인
    // ==========================
    public boolean checkDeptExists(int deptNum) {
        boolean exists = false;

        try {
            getConnection();

            String sql = "SELECT COUNT(*) FROM DEPT WHERE DEPT_NUM = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, deptNum);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                exists = true;
            }

        } catch (Exception e) {
            System.out.println("❌ 부서 확인 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }

        return exists;
    }

    // ==========================
    // 사원 존재 여부 확인
    // ==========================
    public boolean checkUserExists(int userId) {
        boolean exists = false;

        try {
            getConnection();

            String sql = "SELECT COUNT(*) FROM USERTEST WHERE USER_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                exists = true;
            }

        } catch (Exception e) {
            System.out.println("❌ 사원 확인 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }

        return exists;
    }
}