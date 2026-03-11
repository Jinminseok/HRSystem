package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.util.DBUtil;

public class HrAppointmentHistoryDAO {

    // 인사발령 이력 1건 저장
    public int insertHistory(
            int targetUserId,
            String changeType,      
            String beforeValue,
            String afterValue,
            String beforeLabel,
            String afterLabel,
            String changeReason,
            int changedBy,
            String sourceMenu,
            Integer loginLogId
    ) { 
        String sql =
            "INSERT INTO HR_APPOINTMENT_HISTORY ( " +
            "    HISTORY_ID, TARGET_USER_ID, CHANGE_TYPE, BEFORE_VALUE, AFTER_VALUE, " +
            "    BEFORE_LABEL, AFTER_LABEL, CHANGE_REASON, CHANGED_BY, SOURCE_MENU, LOGIN_LOG_ID, CHANGED_AT " +
            ") VALUES ( " +
            "    HR_APPOINTMENT_HISTORY_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE " +
            ")";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstat = conn.prepareStatement(sql)) {

            pstat.setInt(1, targetUserId);
            pstat.setString(2, changeType);
            pstat.setString(3, beforeValue);
            pstat.setString(4, afterValue);
            pstat.setString(5, beforeLabel);
            pstat.setString(6, afterLabel);
            pstat.setString(7, changeReason);
            pstat.setInt(8, changedBy);
            pstat.setString(9, sourceMenu);

            // 로그인 로그 ID가 없으면 null 저장
            if (loginLogId == null) {
                pstat.setNull(10, java.sql.Types.NUMERIC);
            } else {
                pstat.setInt(10, loginLogId);
            }

            return pstat.executeUpdate();

        } catch (Exception e) {
            System.out.println("❌ 인사발령 이력 저장 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        }

        return 0;
    }

    // 전체 인사발령 이력 조회
    public List<Map<String, Object>> selectAllHistory() {
        String sql =
            "SELECT h.HISTORY_ID, h.TARGET_USER_ID, tu.USER_NAME AS TARGET_USER_NAME, " +
            "       h.CHANGE_TYPE, h.BEFORE_VALUE, h.AFTER_VALUE, h.BEFORE_LABEL, h.AFTER_LABEL, " +
            "       h.CHANGE_REASON, h.CHANGED_BY, au.USER_NAME AS CHANGED_BY_NAME, " +
            "       h.SOURCE_MENU, h.LOGIN_LOG_ID, h.CHANGED_AT " +
            "  FROM HR_APPOINTMENT_HISTORY h " +
            "  LEFT JOIN USERTEST tu ON tu.USER_ID = h.TARGET_USER_ID " +
            "  LEFT JOIN USERTEST au ON au.USER_ID = h.CHANGED_BY " +
            " ORDER BY h.HISTORY_ID DESC";

        return selectHistoryBySql(sql, null);
    }

    // 특정 사원의 인사발령 이력 조회
    public List<Map<String, Object>> selectHistoryByUserId(int targetUserId) {
        String sql =
            "SELECT h.HISTORY_ID, h.TARGET_USER_ID, tu.USER_NAME AS TARGET_USER_NAME, " +
            "       h.CHANGE_TYPE, h.BEFORE_VALUE, h.AFTER_VALUE, h.BEFORE_LABEL, h.AFTER_LABEL, " +
            "       h.CHANGE_REASON, h.CHANGED_BY, au.USER_NAME AS CHANGED_BY_NAME, " +
            "       h.SOURCE_MENU, h.LOGIN_LOG_ID, h.CHANGED_AT " +
            "  FROM HR_APPOINTMENT_HISTORY h " +
            "  LEFT JOIN USERTEST tu ON tu.USER_ID = h.TARGET_USER_ID " +
            "  LEFT JOIN USERTEST au ON au.USER_ID = h.CHANGED_BY " +
            " WHERE h.TARGET_USER_ID = ? " +
            " ORDER BY h.HISTORY_ID DESC";

        return selectHistoryBySql(sql, new Object[]{targetUserId});
    }

    // 변경 유형별 인사발령 이력 조회
    public List<Map<String, Object>> selectHistoryByType(String changeType) {
        // 허용된 변경 유형인지 검사
        if (!isValidChangeType(changeType)) {
            System.out.println("❌ 변경 유형 입력이 잘못되었습니다.");
            System.out.println("가능한 값: DEPT / POSITION / EMP_STATUS");
            System.out.println("다시 입력해주세요.");
            return new ArrayList<>();
        }

        String sql =
            "SELECT h.HISTORY_ID, h.TARGET_USER_ID, tu.USER_NAME AS TARGET_USER_NAME, " +
            "       h.CHANGE_TYPE, h.BEFORE_VALUE, h.AFTER_VALUE, h.BEFORE_LABEL, h.AFTER_LABEL, " +
            "       h.CHANGE_REASON, h.CHANGED_BY, au.USER_NAME AS CHANGED_BY_NAME, " +
            "       h.SOURCE_MENU, h.LOGIN_LOG_ID, h.CHANGED_AT " +
            "  FROM HR_APPOINTMENT_HISTORY h " +
            "  LEFT JOIN USERTEST tu ON tu.USER_ID = h.TARGET_USER_ID " +
            "  LEFT JOIN USERTEST au ON au.USER_ID = h.CHANGED_BY " +
            " WHERE h.CHANGE_TYPE = ? " +
            " ORDER BY h.HISTORY_ID DESC";

        return selectHistoryBySql(sql, new Object[]{changeType.toUpperCase()});
    }

    // 기간별 인사발령 이력 조회
    public List<Map<String, Object>> selectHistoryByDateRange(String fromDate, String toDate) {
        try {
            validateDateInput(fromDate);
            validateDateInput(toDate);

            LocalDate from = LocalDate.parse(fromDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate to = LocalDate.parse(toDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 시작일이 종료일보다 늦은 경우 방지
            if (from.isAfter(to)) {
                System.out.println("❌ 시작일이 종료일보다 늦을 수 없습니다.");
                System.out.println("다시 입력해주세요.");
                return new ArrayList<>();
            }

        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
            System.out.println("다시 입력해주세요.");
            return new ArrayList<>();
        }

        String sql =
            "SELECT h.HISTORY_ID, h.TARGET_USER_ID, tu.USER_NAME AS TARGET_USER_NAME, " +
            "       h.CHANGE_TYPE, h.BEFORE_VALUE, h.AFTER_VALUE, h.BEFORE_LABEL, h.AFTER_LABEL, " +
            "       h.CHANGE_REASON, h.CHANGED_BY, au.USER_NAME AS CHANGED_BY_NAME, " +
            "       h.SOURCE_MENU, h.LOGIN_LOG_ID, h.CHANGED_AT " +
            "  FROM HR_APPOINTMENT_HISTORY h " +
            "  LEFT JOIN USERTEST tu ON tu.USER_ID = h.TARGET_USER_ID " +
            "  LEFT JOIN USERTEST au ON au.USER_ID = h.CHANGED_BY " +
            " WHERE TRUNC(h.CHANGED_AT) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') " +
            " ORDER BY h.HISTORY_ID DESC";

        return selectHistoryBySql(sql, new Object[]{fromDate, toDate});
    }

    // 공통 조회 메서드
    private List<Map<String, Object>> selectHistoryBySql(String sql, Object[] params) {
        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstat = conn.prepareStatement(sql)) {

            // 전달받은 파라미터가 있으면 순서대로 바인딩
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstat.setObject(i + 1, params[i]);
                }
            }

            try (ResultSet rs = pstat.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    // 조회 결과를 Map 형태로 저장
                    row.put("HISTORY_ID", rs.getInt("HISTORY_ID"));
                    row.put("TARGET_USER_ID", rs.getInt("TARGET_USER_ID"));
                    row.put("TARGET_USER_NAME", rs.getString("TARGET_USER_NAME"));
                    row.put("CHANGE_TYPE", rs.getString("CHANGE_TYPE"));
                    row.put("BEFORE_VALUE", rs.getString("BEFORE_VALUE"));
                    row.put("AFTER_VALUE", rs.getString("AFTER_VALUE"));
                    row.put("BEFORE_LABEL", rs.getString("BEFORE_LABEL"));
                    row.put("AFTER_LABEL", rs.getString("AFTER_LABEL"));
                    row.put("CHANGE_REASON", rs.getString("CHANGE_REASON"));
                    row.put("CHANGED_BY", rs.getInt("CHANGED_BY"));
                    row.put("CHANGED_BY_NAME", rs.getString("CHANGED_BY_NAME"));
                    row.put("SOURCE_MENU", rs.getString("SOURCE_MENU"));

                    int loginLogId = rs.getInt("LOGIN_LOG_ID");
                    row.put("LOGIN_LOG_ID", rs.wasNull() ? null : loginLogId);

                    Timestamp ts = rs.getTimestamp("CHANGED_AT");
                    row.put("CHANGED_AT", ts);

                    list.add(row);
                }
            }

        } catch (Exception e) {
            System.out.println("❌ 인사발령 이력 조회 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        }

        return list;
    }

    // 인사발령 이력 목록을 콘솔 표 형태로 출력
    public void printHistoryList(List<Map<String, Object>> list) {
        printDivider(180);
        System.out.println("인사발령 이력 조회");
        printDivider(180);

        System.out.println(
                pad("이력ID", 8) +
                pad("대상자ID", 10) +
                pad("대상자명", 10) +
                pad("변경유형", 12) +
                pad("이전값", 14) +
                pad("변경값", 14) +
                pad("이전라벨", 16) +
                pad("변경라벨", 16) +
                pad("변경사유", 24) +
                pad("처리자", 10) +
                pad("메뉴", 14) +
                pad("로그ID", 10) +
                pad("변경일시", 18)
        );

        printDivider(180);

        if (list == null || list.isEmpty()) {
            System.out.println("조회된 인사발령 이력이 없습니다.");
            printDivider(180);
            return;
        }

        // 조회된 이력 한 건씩 출력
        for (Map<String, Object> row : list) {
            System.out.println(
                    pad(String.valueOf(row.get("HISTORY_ID")), 8) +
                    pad(String.valueOf(row.get("TARGET_USER_ID")), 10) +
                    pad((String) row.get("TARGET_USER_NAME"), 10) +
                    pad(changeTypeToKor((String) row.get("CHANGE_TYPE")), 12) +
                    pad((String) row.get("BEFORE_VALUE"), 14) +
                    pad((String) row.get("AFTER_VALUE"), 14) +
                    pad((String) row.get("BEFORE_LABEL"), 16) +
                    pad((String) row.get("AFTER_LABEL"), 16) +
                    pad((String) row.get("CHANGE_REASON"), 24) +
                    pad((String) row.get("CHANGED_BY_NAME"), 10) +
                    pad((String) row.get("SOURCE_MENU"), 14) +
                    pad(row.get("LOGIN_LOG_ID") == null ? "-" : String.valueOf(row.get("LOGIN_LOG_ID")), 10) +
                    pad(tsToMinuteStr((Timestamp) row.get("CHANGED_AT")), 18)
            );
        }

        printDivider(180);
    }

    // 날짜 입력값이 yyyy-MM-dd 형식인지 검사
    private void validateDateInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("날짜를 입력해주세요. 형식: YYYY-MM-DD");
        }

        try {
            LocalDate.parse(input.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 잘못되었습니다. 예: 2026-03-10");
        }
    }

    // 변경 유형이 허용된 값인지 검사
    private boolean isValidChangeType(String changeType) {
        if (changeType == null) return false;

        String type = changeType.trim().toUpperCase();
        return "DEPT".equals(type) || "POSITION".equals(type) || "EMP_STATUS".equals(type);
    }

    // 콘솔 출력 보조 메서드들
    private void printDivider(int length) {
        System.out.println("=".repeat(length));
    }

    private String tsToMinuteStr(Timestamp ts) {
        if (ts == null) return "-";
        String s = ts.toString();
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }

    // 변경 유형 코드를 한글명으로 변환
    private String changeTypeToKor(String changeType) {
        if (changeType == null) return "-";

        switch (changeType.toUpperCase()) {
            case "DEPT":
                return "부서변경";
            case "POSITION":
                return "직급변경";
            case "EMP_STATUS":
                return "재직상태변경";
            default:
                return changeType;
        }
    }

    // 한글/중문처럼 너비 2칸 문자인지 확인
    private boolean isWide(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    // 콘솔 표 정렬을 위해 문자열 길이를 맞춤
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