package kr.appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.util.DBUtil;

public class HrAppointmentHistoryDAO {

    // 이력 1건 저장
    public int insertHistory(
            int targetUserId,
            String changeType,      // DEPT / POSITION / EMP_STATUS
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

            if (loginLogId == null) {
                pstat.setNull(10, java.sql.Types.NUMERIC);
            } else {
                pstat.setInt(10, loginLogId);
            }

            return pstat.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // 전체 조회
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

    // 사원별 조회
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

    // 유형별 조회
    public List<Map<String, Object>> selectHistoryByType(String changeType) {
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

        return selectHistoryBySql(sql, new Object[]{changeType});
    }

    // 기간 조회 (YYYY-MM-DD)
    public List<Map<String, Object>> selectHistoryByDateRange(String fromDate, String toDate) {
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

    private List<Map<String, Object>> selectHistoryBySql(String sql, Object[] params) {
        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstat = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstat.setObject(i + 1, params[i]);
                }
            }

            try (ResultSet rs = pstat.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

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
            e.printStackTrace();
        }

        return list;
    }
}
