package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import kr.util.DBUtil;

public class LoginDAO {

    // 회원가입
    // 가입 시: APPROVAL_STATUS=PENDING, USER_ROLE=USER, EMP_STATUS=WAIT
    // 반환값: 1 이상 = 성공, -1 = 아이디중복, 0 = 기타실패
    public int insertUser(String id, String pw, String name, String email, String phone) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            if (existsLoginId(id)) {
                return -1;
            }

            String sql = "INSERT INTO USERTEST ("
                       + "    USER_ID, LOGIN_ID, PASSWORD, USER_NAME, EMAIL, PHONE, "
                       + "    APPROVAL_STATUS, EMP_STATUS, USER_ROLE, JOIN_DATE, USER_MODIFIED_DATE"
                       + ") VALUES ("
                       + "    USER_ACCOUNT_SEQ.NEXTVAL, ?, ?, ?, ?, ?, "
                       + "    'PENDING', 'WAIT', 'USER', SYSDATE, SYSDATE"
                       + ")";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, pw);
            pstmt.setString(3, name);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                return -1;
            }
            e.printStackTrace();
            return 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;

        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 로그인 아이디 중복 체크
    public boolean existsLoginId(String loginId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT COUNT(*) FROM USERTEST WHERE LOGIN_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, loginId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return false;
    }

    // 로그인용: 계정 정보 조회 (승인상태/권한 포함)
    public Map<String, Object> loginAsMap(String loginId, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT USER_ID, LOGIN_ID, USER_NAME, APPROVAL_STATUS, USER_ROLE "
                       + "FROM USERTEST "
                       + "WHERE LOGIN_ID = ? AND PASSWORD = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, loginId);
            pstmt.setString(2, password);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("USER_ID", rs.getInt("USER_ID"));
                m.put("LOGIN_ID", rs.getString("LOGIN_ID"));
                m.put("USER_NAME", rs.getString("USER_NAME"));
                m.put("APPROVAL_STATUS", rs.getString("APPROVAL_STATUS"));
                m.put("USER_ROLE", rs.getString("USER_ROLE"));
                return m;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return null;
    }

    // 기존 코드 호환용
    public int getUserIdByLoginId(String loginId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT USER_ID FROM USERTEST WHERE LOGIN_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, loginId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("USER_ID");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return -1;
    }

    // ==============================
    // 관리자 사원관리용 (승인대기 목록 조회)
    // ==============================
    public void selectPendingUsers() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT USER_ID, LOGIN_ID, USER_NAME, EMAIL, PHONE, "
                       + "       APPROVAL_STATUS, "
                       + "       TO_CHAR(JOIN_DATE, 'YYYY-MM-DD HH24:MI') AS JOIN_DATE "
                       + "FROM USERTEST "
                       + "WHERE APPROVAL_STATUS IN ('PENDING','REJECTED') "
                       + "ORDER BY USER_ID ASC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            printDivider(120);
            System.out.println("승인대기 사원 목록");
            printDivider(120);

            System.out.println(
                    pad("USER_ID", 8) +
                    pad("LOGIN_ID", 18) +
                    pad("이름", 10) +
                    pad("승인상태", 10) +
                    pad("이메일", 30) +
                    pad("전화번호", 16) +
                    pad("가입일", 18)
            );

            printDivider(120);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;

                String statusKor = approvalStatusToKor(rs.getString("APPROVAL_STATUS"));

                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 8) +
                        pad(rs.getString("LOGIN_ID"), 18) +
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(statusKor, 10) +
                        pad(rs.getString("EMAIL"), 30) +
                        pad(rs.getString("PHONE"), 16) +
                        pad(rs.getString("JOIN_DATE"), 18)
                );
            }

            if (!hasData) {
                System.out.println("승인대기 사용자가 없습니다.");
            }

            printDivider(120);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 전체 사원 조회
    public void selectAllUsers() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                    "SELECT u.USER_ID, u.LOGIN_ID, u.USER_NAME, " +
                    "       NVL(d.DEPT_NAME, '-') AS DEPT_NAME, " +
                    "       NVL(p.POSITION_NAME, '-') AS POSITION_NAME, " +
                    "       u.APPROVAL_STATUS, u.EMP_STATUS, u.USER_ROLE " +
                    "FROM USERTEST u " +
                    "LEFT JOIN DEPT d ON u.DEPT_NUM = d.DEPT_NUM " +
                    "LEFT JOIN POSITION p ON u.POSITION_NUM = p.POSITION_NUM " +
                    "ORDER BY u.USER_ID ASC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            printDivider(120);
            System.out.println("전체 사원 목록");
            printDivider(120);

            System.out.println(
                    pad("ID", 6) +
                    pad("LOGIN_ID", 18) +
                    pad("이름", 10) +
                    pad("부서", 12) +
                    pad("직급", 12) +
                    pad("승인상태", 10) +
                    pad("재직상태", 10) +
                    pad("권한", 8)
            );

            printDivider(120);

            while (rs.next()) {
                String approvalStatus = approvalStatusToKor(rs.getString("APPROVAL_STATUS"));
                String empStatus = empStatusToKor(rs.getString("EMP_STATUS"));
                String userRole = userRoleToKor(rs.getString("USER_ROLE"));

                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 6) +
                        pad(rs.getString("LOGIN_ID"), 18) +
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(rs.getString("DEPT_NAME"), 12) +
                        pad(rs.getString("POSITION_NAME"), 12) +
                        pad(approvalStatus, 10) +
                        pad(empStatus, 10) +
                        pad(userRole, 8)
                );
            }

            printDivider(120);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==============================
    // 부서별 사원 목록 조회
    // ==============================
    public void selectUsersByDept(int deptNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                    "SELECT USER_ID, LOGIN_ID, USER_NAME, DEPT_NUM, POSITION_NUM, " +
                    "       APPROVAL_STATUS, EMP_STATUS, USER_ROLE " +
                    "FROM USERTEST " +
                    "WHERE DEPT_NUM = ? " +
                    "  AND APPROVAL_STATUS <> 'REJECTED' " +
                    "ORDER BY USER_ID ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, deptNum);

            rs = pstmt.executeQuery();

            printDivider(120);
            System.out.println("부서별 사원 목록 (DEPT_NUM = " + deptNum + ")");
            printDivider(120);

            System.out.println(
                    pad("ID", 6) +
                    pad("LOGIN_ID", 18) +
                    pad("이름", 10) +
                    pad("부서번호", 10) +
                    pad("직급번호", 10) +
                    pad("승인상태", 10) +
                    pad("재직상태", 10) +
                    pad("권한", 8)
            );

            printDivider(120);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;

                String deptNumStr = (rs.getObject("DEPT_NUM") == null) ? "-" : String.valueOf(rs.getInt("DEPT_NUM"));
                String positionNumStr = (rs.getObject("POSITION_NUM") == null) ? "-" : String.valueOf(rs.getInt("POSITION_NUM"));

                String approvalStatus = approvalStatusToKor(rs.getString("APPROVAL_STATUS"));
                String empStatus = empStatusToKor(rs.getString("EMP_STATUS"));
                String userRole = userRoleToKor(rs.getString("USER_ROLE"));

                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 6) +
                        pad(rs.getString("LOGIN_ID"), 18) +
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(deptNumStr, 10) +
                        pad(positionNumStr, 10) +
                        pad(approvalStatus, 10) +
                        pad(empStatus, 10) +
                        pad(userRole, 8)
                );
            }

            if (!hasData) {
                System.out.println("해당 부서에 사원이 없습니다.");
            }

            printDivider(120);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==============================
    // 직급별 사원 목록 조회
    // ==============================
    public void selectUsersByPosition(int positionNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                    "SELECT USER_ID, LOGIN_ID, USER_NAME, DEPT_NUM, POSITION_NUM, " +
                    "       APPROVAL_STATUS, EMP_STATUS, USER_ROLE " +
                    "FROM USERTEST " +
                    "WHERE POSITION_NUM = ? " +
                    "  AND APPROVAL_STATUS <> 'REJECTED' " +
                    "ORDER BY USER_ID ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, positionNum);

            rs = pstmt.executeQuery();

            printDivider(120);
            System.out.println("직급별 사원 목록 (POSITION_NUM = " + positionNum + ")");
            printDivider(120);

            System.out.println(
                    pad("ID", 6) +
                    pad("LOGIN_ID", 18) +
                    pad("이름", 10) +
                    pad("부서번호", 10) +
                    pad("직급번호", 10) +
                    pad("승인상태", 10) +
                    pad("재직상태", 10) +
                    pad("권한", 8)
            );

            printDivider(120);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;

                String deptNumStr = (rs.getObject("DEPT_NUM") == null) ? "-" : String.valueOf(rs.getInt("DEPT_NUM"));
                String positionNumStr = (rs.getObject("POSITION_NUM") == null) ? "-" : String.valueOf(rs.getInt("POSITION_NUM"));

                String approvalStatus = approvalStatusToKor(rs.getString("APPROVAL_STATUS"));
                String empStatus = empStatusToKor(rs.getString("EMP_STATUS"));
                String userRole = userRoleToKor(rs.getString("USER_ROLE"));

                System.out.println(
                        pad(String.valueOf(rs.getInt("USER_ID")), 6) +
                        pad(rs.getString("LOGIN_ID"), 18) +
                        pad(rs.getString("USER_NAME"), 10) +
                        pad(deptNumStr, 10) +
                        pad(positionNumStr, 10) +
                        pad(approvalStatus, 10) +
                        pad(empStatus, 10) +
                        pad(userRole, 8)
                );
            }

            if (!hasData) {
                System.out.println("해당 직급에 사원이 없습니다.");
            }

            printDivider(120);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 사원 승인 처리 (부서/직급/권한 지정)
    public int approveUser(int userId, int deptNum, int positionNum, String userRole) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "UPDATE USERTEST "
                       + "SET DEPT_NUM = ?, "
                       + "    POSITION_NUM = ?, "
                       + "    APPROVAL_STATUS = 'APPROVED', "
                       + "    EMP_STATUS = 'WORK', "
                       + "    USER_ROLE = ?, "
                       + "    USER_MODIFIED_DATE = SYSDATE "
                       + "WHERE USER_ID = ? "
                       + "AND APPROVAL_STATUS IN ('PENDING', 'REJECTED')";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, deptNum);
            pstmt.setInt(2, positionNum);
            pstmt.setString(3, userRole);
            pstmt.setInt(4, userId);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 승인 거절 처리
    public int rejectUser(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "UPDATE USERTEST "
                       + "SET APPROVAL_STATUS = 'REJECTED', "
                       + "    USER_MODIFIED_DATE = SYSDATE "
                       + "WHERE USER_ID = ? "
                       + "AND APPROVAL_STATUS = 'PENDING'";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 부서번호 존재 여부 확인
    public boolean existsDeptNum(int deptNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT COUNT(*) FROM DEPT WHERE DEPT_NUM = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, deptNum);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return false;
    }

    // 직급번호 존재 여부 확인
    public boolean existsPositionNum(int positionNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT COUNT(*) FROM POSITION WHERE POSITION_NUM = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, positionNum);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return false;
    }

    // 부서명 이름으로 입력받기(이름 -> 번호 변환 메서드)
    public Integer getDeptNumByName(String deptName) {
        String sql = "SELECT DEPT_NUM FROM DEPT WHERE DEPT_NAME = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, deptName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 직급명 이름으로 입력받기(이름 -> 번호 변환 메서드)
    public Integer getPositionNumByName(String positionName) {
        String sql = "SELECT POSITION_NUM FROM POSITION WHERE POSITION_NAME = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, positionName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 관리자용 사원 정보 변경 (부서/직급/재직상태)
    public int updateUserInfoByAdmin(int userId, int deptNum, int positionNum, String empStatus) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "UPDATE USERTEST "
                       + "SET DEPT_NUM = ?, "
                       + "    POSITION_NUM = ?, "
                       + "    EMP_STATUS = ?, "
                       + "    USER_MODIFIED_DATE = SYSDATE "
                       + "WHERE USER_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, deptNum);
            pstmt.setInt(2, positionNum);
            pstmt.setString(3, empStatus);
            pstmt.setInt(4, userId);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 재직상태 한글 변환
    private String empStatusToKor(String empStatus) {
        if (empStatus == null) return "-";

        switch (empStatus.toUpperCase()) {
            case "WORK":     return "재직";
            case "LEAVE":    return "휴직";
            case "RESIGNED": return "퇴직";
            case "WAIT":     return "대기";
            default:         return empStatus;
        }
    }

    // 승인상태 한글 변환
    private String approvalStatusToKor(String approvalStatus) {
        if (approvalStatus == null) return "-";

        switch (approvalStatus.toUpperCase()) {
            case "PENDING":  return "승인대기";
            case "APPROVED": return "승인완료";
            case "REJECTED": return "승인거절";
            default:         return approvalStatus;
        }
    }

    // 권한 한글 변환
    private String userRoleToKor(String userRole) {
        if (userRole == null) return "-";

        switch (userRole.toUpperCase()) {
            case "ADMIN": return "관리자";
            case "USER":  return "사원";
            default:      return userRole;
        }
    }

    // 사용자 현재값 조회 (수정 전 값)
    public Map<String, Object> getUserInfoMapById(int userId) {
        String sql =
                "SELECT USER_ID, USER_NAME, DEPT_NUM, POSITION_NUM, EMP_STATUS " +
                "  FROM USERTEST " +
                " WHERE USER_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstat = conn.prepareStatement(sql)) {

            pstat.setInt(1, userId);

            try (ResultSet rs = pstat.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> map = new HashMap<>();

                    map.put("USER_ID", rs.getInt("USER_ID"));
                    map.put("USER_NAME", rs.getString("USER_NAME"));

                    int deptNum = rs.getInt("DEPT_NUM");
                    map.put("DEPT_NUM", rs.wasNull() ? null : deptNum);

                    int posNum = rs.getInt("POSITION_NUM");
                    map.put("POSITION_NUM", rs.wasNull() ? null : posNum);

                    map.put("EMP_STATUS", rs.getString("EMP_STATUS"));

                    return map;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // 부서명 조회
    public String getDeptNameByNum(Integer deptNum) {
        if (deptNum == null) return null;

        String sql = "SELECT DEPT_NAME FROM DEPT WHERE DEPT_NUM = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstat = conn.prepareStatement(sql)) {

            pstat.setInt(1, deptNum);

            try (ResultSet rs = pstat.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "부서#" + deptNum;
    }

    // 직급명 조회
    public String getPositionNameByNum(Integer positionNum) {
        if (positionNum == null) return null;

        String sql = "SELECT POSITION_NAME FROM POSITION WHERE POSITION_NUM = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstat = conn.prepareStatement(sql)) {

            pstat.setInt(1, positionNum);

            try (ResultSet rs = pstat.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "직급#" + positionNum;
    }

    // ==============================
    // 콘솔 출력 정렬용 유틸
    // ==============================

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