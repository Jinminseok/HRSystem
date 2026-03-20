package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import kr.util.DBUtil;

public class StatsDAO {

    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    private void getConnection() throws Exception {
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

    private String empStatusToKor(String status) {
        if (status == null) return "-";

        switch (status.toUpperCase()) {
            case "WORK":
                return "재직";
            case "LEAVE":
                return "휴직";
            case "RESIGNED":
                return "퇴직";
            default:
                return status;
        }
    }

    private String makeAttendanceStatus(int inStatus, int outStatus) {
        String in = (inStatus == 2) ? "지각" : "정상";
        String out;

        if (outStatus == 2) out = "조퇴";
        else if (outStatus == 3) out = "야근";
        else if (outStatus == 1) out = "정상";
        else out = "미정";

        return in + "/" + out;
    }

    private String formatHour(ResultSet rs, String columnName) throws Exception {
        Object obj = rs.getObject(columnName);
        if (obj == null) return "-";
        return rs.getDouble(columnName) + "h";
    }

    // ==========================
    // 입력값 검증
    // ==========================
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidYear(String value) {
        if (isBlank(value)) return false;
        return value.matches("\\d{4}");
    }

    private boolean isValidYearMonth(String value) {
        if (isBlank(value)) return false;

        try {
            YearMonth.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidDate(String value) {
        if (isBlank(value)) return false;

        try {
            LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidDateRange(String start, String end) {
        if (!isValidDate(start) || !isValidDate(end)) return false;

        try {
            LocalDate s = LocalDate.parse(start.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate e = LocalDate.parse(end.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return !s.isAfter(e);
        } catch (Exception ex) {
            return false;
        }
    }

    // ==========================
    // 부서명 / 직급명 존재 여부 체크
    // ==========================
    public boolean existsDeptName(String deptName) {
        Connection checkConn = null;
        PreparedStatement checkPstmt = null;
        ResultSet checkRs = null;

        try {
            checkConn = DBUtil.getConnection();

            String sql = "SELECT COUNT(*) FROM DEPT WHERE DEPT_NAME = ?";
            checkPstmt = checkConn.prepareStatement(sql);
            checkPstmt.setString(1, deptName.trim());

            checkRs = checkPstmt.executeQuery();
            if (checkRs.next()) {
                return checkRs.getInt(1) > 0;
            }

        } catch (Exception e) {
            System.out.println("❌ 부서명 확인 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(checkRs, checkPstmt, checkConn);
        }

        return false;
    }

    public boolean existsPositionName(String positionName) {
        Connection checkConn = null;
        PreparedStatement checkPstmt = null;
        ResultSet checkRs = null;

        try {
            checkConn = DBUtil.getConnection();

            String sql = "SELECT COUNT(*) FROM POSITION WHERE POSITION_NAME = ?";
            checkPstmt = checkConn.prepareStatement(sql);
            checkPstmt.setString(1, positionName.trim());

            checkRs = checkPstmt.executeQuery();
            if (checkRs.next()) {
                return checkRs.getInt(1) > 0;
            }

        } catch (Exception e) {
            System.out.println("❌ 직급명 확인 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(checkRs, checkPstmt, checkConn);
        }

        return false;
    }

    private boolean validateCommonType(int type, String typeVal) {
        if (type < 1 || type > 3) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if ((type == 2 || type == 3) && isBlank(typeVal)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (type == 2 && !existsDeptName(typeVal)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (type == 3 && !existsPositionName(typeVal)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        return true;
    }

    private boolean validateWorkStatusInput(int type, String typeVal, int dateType, String startDate, String endDate) {
        if (!validateCommonType(type, typeVal)) return false;

        if (dateType < 1 || dateType > 4) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 2 && !isValidYearMonth(startDate)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 3 && !isValidYear(startDate)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 4) {
            if (!isValidDate(startDate) || !isValidDate(endDate)) {
                System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                return false;
            }
            if (!isValidDateRange(startDate, endDate)) {
                System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                return false;
            }
        }

        return true;
    }

    private boolean validateAttendanceInput(int type, String typeVal, int dateType, String start, String end) {
        if (!validateCommonType(type, typeVal)) return false;

        if (dateType < 1 || dateType > 5) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 2 && !isValidDate(start)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 3 && !isValidYearMonth(start)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 4 && !isValidYear(start)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 5) {
            if (!isValidDate(start) || !isValidDate(end)) {
                System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                return false;
            }
            if (!isValidDateRange(start, end)) {
                System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                return false;
            }
        }

        return true;
    }

    private boolean validateSalaryInput(int type, String typeVal, int dateType, String start, String end) {
        if (!validateCommonType(type, typeVal)) return false;

        if (dateType < 1 || dateType > 4) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 2 && !isValidYearMonth(start)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 3 && !isValidYear(start)) {
            System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
            return false;
        }

        if (dateType == 4) {
            if (!isValidDate(start) || !isValidDate(end)) {
                System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                return false;
            }
            if (!isValidDateRange(start, end)) {
                System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                return false;
            }
        }

        return true;
    }

    // =========================================================================
    // [1] 인사 상태 통계
    // =========================================================================
    public void showWorkStatusStats(int type, String typeVal, int dateType, String startDate, String endDate) {
        if (!validateWorkStatusInput(type, typeVal, dateType, startDate, endDate)) {
            return;
        }

        try {
            getConnection();

            StringBuilder sql = new StringBuilder(
                "SELECT u.USER_ID, u.USER_NAME, NVL(d.DEPT_NAME, '-') AS DEPT_NAME, " +
                "       NVL(p.POSITION_NAME, '-') AS POSITION_NAME, u.EMP_STATUS, " +
                "       TO_CHAR(u.JOIN_DATE, 'YYYY-MM-DD') AS JDATE " +
                "FROM USERTEST u " +
                "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                "LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "WHERE 1=1 "
            );

            if (type == 2) {
                sql.append("AND d.DEPT_NAME = ? ");
            } else if (type == 3) {
                sql.append("AND p.POSITION_NAME = ? ");
            }

            if (dateType == 2) {
                sql.append("AND TO_CHAR(u.JOIN_DATE, 'YYYY-MM') <= ? ");
            } else if (dateType == 3) {
                sql.append("AND TO_CHAR(u.JOIN_DATE, 'YYYY') <= ? ");
            } else if (dateType == 4) {
                sql.append("AND u.JOIN_DATE BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') ");
            }

            sql.append("ORDER BY u.USER_ID ASC");

            pstmt = conn.prepareStatement(sql.toString());

            int idx = 1;
            if (type > 1) {
                pstmt.setString(idx++, typeVal.trim());
            }

            if (dateType == 2 || dateType == 3) {
                pstmt.setString(idx++, startDate.trim());
            } else if (dateType == 4) {
                pstmt.setString(idx++, startDate.trim());
                pstmt.setString(idx++, endDate.trim());
            }

            rs = pstmt.executeQuery();

            System.out.println();
            printDivider(70);
            System.out.println("인사 상태 상세 명단");
            printDivider(70);

            System.out.println(
                    pad("사번", 8) +
                    pad("이름", 10) +
                    pad("부서", 14) +
                    pad("직급", 12) +
                    pad("근무상태", 10)
            );

            System.out.println("-".repeat(70));

            int total = 0;
            int work = 0;
            int leave = 0;
            boolean hasData = false;

            while (rs.next()) {
                String status = rs.getString("EMP_STATUS");

                if (status != null && "RESIGNED".equalsIgnoreCase(status)) {
                    continue;
                }

                hasData = true;
                total++;

                if ("WORK".equalsIgnoreCase(status)) {
                    work++;
                } else if ("LEAVE".equalsIgnoreCase(status)) {
                    leave++;
                }

                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 8) +
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(rs.getString("DEPT_NAME"), 14) +
                        pad(rs.getString("POSITION_NAME"), 12) +
                        pad(empStatusToKor(status), 10)
                );
            }

            if (!hasData) {
                System.out.println("조건에 맞는 사원이 없습니다.");
            }

            printDivider(70);
            System.out.println("총 " + total + "명 (재직: " + work + " / 휴직: " + leave + ")");
            printDivider(70);

        } catch (Exception e) {
            System.out.println("❌ 인사 통계 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }
    }

    // =========================================================================
    // [2] 근태 기록 통계
    // =========================================================================
    public void showAttendanceStats(int type, String typeVal, int dateType, String start, String end) {
        if (!validateAttendanceInput(type, typeVal, dateType, start, end)) {
            return;
        }

        try {
            getConnection();

            StringBuilder sql = new StringBuilder(
                "SELECT u.USER_NAME, NVL(d.DEPT_NAME, '-') AS DEPT_NAME, NVL(p.POSITION_NAME, '-') AS POSITION_NAME, " +
                "       TO_CHAR(a.ATT_DATE, 'YYYY-MM-DD') AS ADATE, " +
                "       NVL(a.IN_STATUS, 0) AS IN_STATUS, NVL(a.OUT_STATUS, 0) AS OUT_STATUS, " +
                "       TO_CHAR(a.CHECK_IN, 'HH24:MI') AS CIN, " +
                "       TO_CHAR(a.CHECK_OUT, 'HH24:MI') AS COUT, " +
                "       ROUND((a.CHECK_OUT - a.CHECK_IN) * 24, 1) AS HRS, " +
                "       TO_CHAR(a.ATT_DATE, 'D') AS DNUM " +
                "FROM ATTENDANCE a " +
                "JOIN USERTEST u ON a.USER_ID = u.USER_ID " +
                "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                "LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "WHERE 1=1 "
            );

            if (type == 2) {
                sql.append("AND d.DEPT_NAME = ? ");
            } else if (type == 3) {
                sql.append("AND p.POSITION_NAME = ? ");
            }

            if (dateType == 2) {
                sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY-MM-DD') = ? ");
            } else if (dateType == 3) {
                sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY-MM') = ? ");
            } else if (dateType == 4) {
                sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY') = ? ");
            } else if (dateType == 5) {
                sql.append("AND a.ATT_DATE BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') ");
            }

            sql.append("ORDER BY a.ATT_DATE DESC, u.USER_NAME ASC");

            pstmt = conn.prepareStatement(sql.toString());

            int idx = 1;
            if (type > 1) {
                pstmt.setString(idx++, typeVal.trim());
            }

            if (dateType >= 2 && dateType <= 4) {
                pstmt.setString(idx++, start.trim());
            } else if (dateType == 5) {
                pstmt.setString(idx++, start.trim());
                pstmt.setString(idx++, end.trim());
            }

            rs = pstmt.executeQuery();

            System.out.println();
            printDivider(96);
            System.out.println("근태 기록 상세 현황");
            printDivider(96);

            System.out.println(
                    pad("날짜", 12) +
                    pad("이름", 10) +
                    pad("부서", 14) +
                    pad("출근", 8) +
                    pad("퇴근", 8) +
                    pad("상태", 12) +
                    pad("근무시간", 10)
            );

            System.out.println("-".repeat(96));

            int normalIn = 0;
            int lateIn = 0;
            int normalOut = 0;
            int earlyOut = 0;
            int overtimeOut = 0;
            int holiday = 0;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;

                int inStatus = rs.getInt("IN_STATUS");
                int outStatus = rs.getInt("OUT_STATUS");

                int dNum = 0;
                try {
                    dNum = Integer.parseInt(nvl(rs.getString("DNUM")));
                } catch (Exception ignore) {
                    dNum = 0;
                }

                if (dNum == 1 || dNum == 7) {
                    holiday++;
                } else {
                    if (inStatus == 1) normalIn++;
                    else if (inStatus == 2) lateIn++;

                    if (outStatus == 1) normalOut++;
                    else if (outStatus == 2) earlyOut++;
                    else if (outStatus == 3) overtimeOut++;
                }

                System.out.println(
                        pad(rs.getString("ADATE"), 12) +
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(rs.getString("DEPT_NAME"), 14) +
                        pad(rs.getString("CIN"), 8) +
                        pad(rs.getString("COUT"), 8) +
                        pad(makeAttendanceStatus(inStatus, outStatus), 12) +
                        pad(formatHour(rs, "HRS"), 10)
                );
            }

            if (!hasData) {
                System.out.println("조건에 맞는 근태 기록이 없습니다.");
            }

            printDivider(96);
            System.out.println(
                    "출근(정상 " + normalIn + " / 지각 " + lateIn + ") | " +
                    "퇴근(정상 " + normalOut + " / 조퇴 " + earlyOut + " / 야근 " + overtimeOut + ") | " +
                    "휴일 " + holiday + "건"
            );
            printDivider(96);

        } catch (Exception e) {
            System.out.println("❌ 근태 통계 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }
    }

    // =========================================================================
    // [3] 급여 통계
    // =========================================================================
    public void showSalaryStats(int type, String typeVal, int dateType, String start, String end) {
        if (!validateSalaryInput(type, typeVal, dateType, start, end)) {
            return;
        }

        try {
            getConnection();

            StringBuilder sql = new StringBuilder(
                "SELECT u.USER_NAME, NVL(p.POSITION_NAME, '-') AS POSITION_NAME, NVL(d.DEPT_NAME, '-') AS DEPT_NAME, " +
                "       SUM(NVL(s.SAL_BASE, 0)) AS T_BASE, " +
                "       SUM(NVL(s.SAL_OVERTIME, 0)) AS T_OT, " +
                "       SUM(NVL(s.SAL_HOLIDAY, 0)) AS T_HOL, " +
                "       SUM(NVL(s.SAL_TAX, 0)) AS T_TAX " +
                "FROM SALARY_MANAGEMENT s " +
                "JOIN USERTEST u ON s.USER_ID = u.USER_ID " +
                "LEFT JOIN ATTENDANCE a ON s.ATT_ID = a.ATT_ID " +
                "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                "LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "WHERE 1=1 "
            );

            if (type == 2) {
                sql.append("AND d.DEPT_NAME = ? ");
            } else if (type == 3) {
                sql.append("AND p.POSITION_NAME = ? ");
            }

            if (dateType == 2) {
                sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY-MM') = ? ");
            } else if (dateType == 3) {
                sql.append("AND TO_CHAR(a.ATT_DATE, 'YYYY') = ? ");
            } else if (dateType == 4) {
                sql.append("AND a.ATT_DATE BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') ");
            }

            sql.append("GROUP BY u.USER_ID, u.USER_NAME, p.POSITION_NAME, d.DEPT_NAME ");
            sql.append("ORDER BY T_BASE DESC, u.USER_NAME ASC");

            pstmt = conn.prepareStatement(sql.toString());

            int idx = 1;
            if (type > 1) {
                pstmt.setString(idx++, typeVal.trim());
            }

            if (dateType == 2 || dateType == 3) {
                pstmt.setString(idx++, start.trim());
            } else if (dateType == 4) {
                pstmt.setString(idx++, start.trim());
                pstmt.setString(idx++, end.trim());
            }

            rs = pstmt.executeQuery();

            System.out.println();
            printDivider(118);
            System.out.println("급여 지급 집계 리스트");
            printDivider(118);

            System.out.println(
                    pad("이름", 10) +
                    pad("직급", 10) +
                    pad("부서", 14) +
                    pad("기본급", 14) +
                    pad("야근수당", 14) +
                    pad("휴일수당", 14) +
                    pad("세금", 12) +
                    pad("실지급액", 16)
            );

            System.out.println("-".repeat(118));

            long totalBase = 0;
            long totalOt = 0;
            long totalHol = 0;
            long totalTax = 0;
            long totalNet = 0;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;

                long base = rs.getLong("T_BASE");
                long ot = rs.getLong("T_OT");
                long hol = rs.getLong("T_HOL");
                long tax = rs.getLong("T_TAX");
                long net = base + ot + hol - tax;

                totalBase += base;
                totalOt += ot;
                totalHol += hol;
                totalTax += tax;
                totalNet += net;

                System.out.println(
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(rs.getString("POSITION_NAME"), 10) +
                        pad(rs.getString("DEPT_NAME"), 14) +
                        pad(String.format("%,d", base), 14) +
                        pad(String.format("%,d", ot), 14) +
                        pad(String.format("%,d", hol), 14) +
                        pad(String.format("%,d", tax), 12) +
                        pad(String.format("%,d원", net), 16)
                );
            }

            if (!hasData) {
                System.out.println("조건에 맞는 급여 데이터가 없습니다.");
            }

            printDivider(118);
            System.out.println(
                    "기본급: " + String.format("%,d", totalBase) +
                    " | 야근: " + String.format("%,d", totalOt) +
                    " | 휴일: " + String.format("%,d", totalHol) +
                    " | 세금: " + String.format("%,d", totalTax)
            );
            System.out.println("최종 실지급액 총계: " + String.format("%,d원", totalNet));
            printDivider(118);

        } catch (Exception e) {
            System.out.println("❌ 급여 통계 조회 중 오류가 발생했습니다.");
        } finally {
            closeResources();
        }
    }
}