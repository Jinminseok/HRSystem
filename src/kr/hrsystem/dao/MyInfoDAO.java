package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kr.util.DBUtil;

public class MyInfoDAO {

    private static final int BOX_WIDTH = 53; // 내 정보 출력 박스 내부 폭

    // 로그인한 사용자의 내 정보 조회 (오늘 출근/퇴근시간 포함)
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
                printBoxLine();
                printBoxCenter("🙋 내 정보 조회");
                printBoxLine();

                // 조회한 사용자 정보를 박스 형태로 출력
                printInfoRow("아이디", rs.getString("LOGIN_ID"));
                printInfoRow("이름", rs.getString("USER_NAME"));
                printInfoRow("부서", rs.getString("DEPT_NAME"));
                printInfoRow("직급", rs.getString("POSITION_NAME"));
                printInfoRow("이메일", rs.getString("EMAIL"));
                printInfoRow("전화번호", rs.getString("PHONE"));
                printInfoRow("오늘 출근시간", nvlTime(rs.getString("CHECK_IN")));
                printInfoRow("오늘 퇴근시간", nvlTime(rs.getString("CHECK_OUT")));

                printBoxLine();
            } else {
                System.out.println("❌ 사용자 정보를 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // DB 자원 정리
            DBUtil.excuteClose(rs, pstmt, conn);
        }
    }

    // 비밀번호 수정
    public int updatePassword(int userId, String newPassword) {
        // 빈값이면 수정하지 않음
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

    // 이메일 수정
    public int updateEmail(int userId, String newEmail) {
        // 빈값이면 수정하지 않음
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
            // 이메일 중복 예외 처리
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

    // 전화번호 수정
    public int updatePhone(int userId, String newPhone) {
        // 빈값이면 수정하지 않음
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

    // 내 정보 일괄 수정 (비밀번호/이메일/전화번호)
    // 빈값은 수정하지 않음
    public int updateMyInfo(int userId, String newPassword, String newEmail, String newPhone) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder("UPDATE USERTEST SET ");

            boolean hasField = false;

            // 입력된 값만 동적으로 UPDATE 항목에 추가
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

            // 수정할 항목이 없으면 종료
            if (!hasField) return 0;

            sql.append("USER_MODIFIED_DATE = SYSDATE ");
            sql.append("WHERE USER_ID = ?");
            params.add(userId);

            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            // 파라미터를 순서대로 바인딩
            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof String) {
                    pstmt.setString(i + 1, (String) v);
                } else if (v instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) v);
                } else {
                    pstmt.setObject(i + 1, v);
                }
            }

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            // 이메일 중복 예외 처리
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

    // ==========================
    // 출력 유틸
    // ==========================

    // 박스 윗줄/아랫줄 출력
    private void printBoxLine() {
        System.out.println("+" + "─".repeat(BOX_WIDTH) + "+");
    }

    // 제목을 박스 가운데 정렬해서 출력
    private void printBoxCenter(String text) {
        int textWidth = displayWidth(text);
        int left = Math.max(0, (BOX_WIDTH - textWidth) / 2);
        int right = Math.max(0, BOX_WIDTH - textWidth - left);

        System.out.println("│" + " ".repeat(left) + text + " ".repeat(right) + "│");
    }

    // 항목명과 값을 한 줄로 출력
    private void printInfoRow(String label, String value) {
        String safeValue = nvl(value);
        String prefix = "  " + label + " : ";
        int remainWidth = BOX_WIDTH - displayWidth(prefix);

        String fittedValue = fitToWidth(safeValue, remainWidth);
        int spaces = BOX_WIDTH - displayWidth(prefix) - displayWidth(fittedValue);

        if (spaces < 0) spaces = 0;

        System.out.println("│" + prefix + fittedValue + " ".repeat(spaces) + "│");
    }

    // 박스 너비를 넘는 문자열은 잘라서 ... 처리
    private String fitToWidth(String text, int width) {
        if (text == null) text = "-";

        if (displayWidth(text) <= width) {
            return text;
        }

        String suffix = "...";
        int suffixWidth = displayWidth(suffix);

        StringBuilder sb = new StringBuilder();
        int len = 0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            int chWidth = isWide(ch) ? 2 : 1;

            if (len + chWidth + suffixWidth > width) {
                break;
            }

            sb.append(ch);
            len += chWidth;
        }

        sb.append(suffix);
        return sb.toString();
    }

    // 문자열의 실제 출력 길이 계산
    private int displayWidth(String text) {
        if (text == null || text.isEmpty()) return 0;

        int len = 0;
        for (int i = 0; i < text.length(); i++) {
            len += isWide(text.charAt(i)) ? 2 : 1;
        }
        return len;
    }

    // 한글/중문처럼 폭이 2칸인 문자 판별
    private boolean isWide(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    // ==========================
    // 공통 유틸
    // ==========================

    // null 또는 빈 문자열이면 "-" 반환
    private String nvl(String value) {
        return (value == null || value.trim().isEmpty()) ? "-" : value;
    }

    // 시간값이 없으면 "기록 없음" 반환
    private String nvlTime(String value) {
        return (value == null || value.trim().isEmpty()) ? "기록 없음" : value;
    }

    // null 또는 공백 여부 확인
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}