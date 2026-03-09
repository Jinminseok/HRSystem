package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kr.util.DBUtil;

public class MyInfoDAO {
	
	private static final String LINE = "───────────────────────────────────────────";
	
    // 1) 로그인한 사용자 내 정보 조회 (오늘 출근/퇴근시간 포함)
    public void selectMyInfo(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT u.USER_ID, u.LOGIN_ID, u.USER_NAME, " +
                "       NVL(d.DEPT_NAME, '-') AS DEPT_NAME, " +
                "       NVL(p.POSITION_NAME, '-') AS POSITION_NAME, " +
                "       NVL(u.EMAIL, '-') AS EMAIL, " +
                "       NVL(u.PHONE, '-') AS PHONE, " +
                "       TO_CHAR(a.CHECK_IN,  'YYYY-MM-DD HH24:MI') AS CHECK_IN, " +
                "       TO_CHAR(a.CHECK_OUT, 'YYYY-MM-DD HH24:MI') AS CHECK_OUT " +
                "FROM USERTEST u " +
                "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                "LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                "LEFT JOIN ATTENDANCE a " +
                "       ON a.USER_ID = u.USER_ID " +
                "      AND a.ATT_DATE = TRUNC(SYSDATE) " +
                "WHERE u.USER_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println();
                System.out.println("+───────────────────────────────────────────────+");
                System.out.println("│              🙋 내 정보 조회                  │");
                System.out.println("+───────────────────────────────────────────────+");
                System.out.println("│  아이디        :  " + rs.getString("LOGIN_ID") +"\t\t\t│");
                System.out.println("│  이름          :  " + rs.getString("USER_NAME")+"\t\t\t│");
                System.out.println("│  부서          :  " + rs.getString("DEPT_NAME")+"\t\t\t│");
                System.out.println("│  직급          :  " + rs.getString("POSITION_NAME")+"\t\t\t│");
                System.out.println("│  이메일        :  " + rs.getString("EMAIL")+"\t│");
                System.out.println("│  전화번호      :  " + rs.getString("PHONE")+"\t\t│");
                System.out.println("│  오늘 출근시간 :  " + nvlTime(rs.getString("CHECK_IN"))+"\t\t│");
                System.out.println("│  오늘 퇴근시간 :  " + nvlTime(rs.getString("CHECK_OUT"))+"\t\t│");
                System.out.println("+───────────────────────────────────────────────+");
            } else {
                System.out.println("❌ 사용자 정보를 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 네 DBUtil 메서드명이 excuteClose 라서 그대로 사용
            DBUtil.excuteClose(rs, pstmt, conn);
        }
    }

    // 2) 비밀번호 수정
    public int updatePassword(int userId, String newPassword) {
        if (isBlank(newPassword)) return 0;

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "UPDATE USERTEST " +
                "SET PASSWORD = ?, USER_MODIFIED_DATE = SYSDATE " +
                "WHERE USER_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword.trim());
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.excuteClose(null, pstmt, conn);
        }
    }

    // 3) 이메일 수정
    public int updateEmail(int userId, String newEmail) {
        if (isBlank(newEmail)) return 0;

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "UPDATE USERTEST " +
                "SET EMAIL = ?, USER_MODIFIED_DATE = SYSDATE " +
                "WHERE USER_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newEmail.trim());
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                System.out.println("❌ 이미 사용 중인 이메일입니다.");
            } else {
                e.printStackTrace();
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.excuteClose(null, pstmt, conn);
        }
    }

    // 4) 전화번호 수정
    public int updatePhone(int userId, String newPhone) {
        if (isBlank(newPhone)) return 0;

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "UPDATE USERTEST " +
                "SET PHONE = ?, USER_MODIFIED_DATE = SYSDATE " +
                "WHERE USER_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPhone.trim());
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.excuteClose(null, pstmt, conn);
        }
    }

    // 5) 내 정보 일괄 수정 (비밀번호/이메일/전화번호)
    // 빈값은 수정 안함
    public int updateMyInfo(int userId, String newPassword, String newEmail, String newPhone) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder("UPDATE USERTEST SET ");

            boolean hasField = false;

            if (!isBlank(newPassword)) {
                sql.append("PASSWORD = ?, ");
                params.add(newPassword.trim());
                hasField = true;
            }
            if (!isBlank(newEmail)) {
                sql.append("EMAIL = ?, ");
                params.add(newEmail.trim());
                hasField = true;
            }
            if (!isBlank(newPhone)) {
                sql.append("PHONE = ?, ");
                params.add(newPhone.trim());
                hasField = true;
            }

            if (!hasField) return 0;

            sql.append("USER_MODIFIED_DATE = SYSDATE ");
            sql.append("WHERE USER_ID = ?");
            params.add(userId);

            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof String) pstmt.setString(i + 1, (String) v);
                else if (v instanceof Integer) pstmt.setInt(i + 1, (Integer) v);
                else pstmt.setObject(i + 1, v);
            }

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                System.out.println("❌ 이미 사용 중인 이메일입니다.");
            } else {
                e.printStackTrace();
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.excuteClose(null, pstmt, conn);
        }
    }

    private String nvlTime(String value) {
        return (value == null || value.trim().isEmpty()) ? "기록 없음" : value;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}