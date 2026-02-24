package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.hrsystem.user.LoginUser;
import kr.util.DBUtil;

	public class LoginDAO {

	    // 회원가입
	    // 가입 시: APPROVAL_STATUS=PENDING, USER_ROLE=USER, EMP_STATUS=WAIT
	    public int insertUser(String id, String pw, String name, String email, String phone) {
	        Connection conn = null;
	        PreparedStatement pstmt = null;

	        try {
	            conn = DBUtil.getConnection();

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

	        } catch (Exception e) {
	            e.printStackTrace();
	            return 0;
	        } finally {
	            DBUtil.executeClose(null, pstmt, conn);
	        }
	    }

	    // 로그인용: 계정 정보 조회 (승인상태/권한 포함)
	    public LoginUser login(String loginId, String password) {
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
	                return new LoginUser(
	                    rs.getInt("USER_ID"),
	                    rs.getString("LOGIN_ID"),
	                    rs.getString("USER_NAME"),
	                    rs.getString("APPROVAL_STATUS"),
	                    rs.getString("USER_ROLE")
	                );
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
	                       + "       TO_CHAR(JOIN_DATE, 'YYYY-MM-DD HH24:MI') AS JOIN_DATE "
	                       + "FROM USERTEST "
	                       + "WHERE APPROVAL_STATUS = 'PENDING' "
	                       + "ORDER BY USER_ID ASC";

	            pstmt = conn.prepareStatement(sql);
	            rs = pstmt.executeQuery();

	            System.out.println("=".repeat(100));
	            System.out.println("승인대기 사원 목록");
	            System.out.println("=".repeat(100));
	            System.out.println("USER_ID\tLOGIN_ID\t이름\t이메일\t전화번호\t가입일");
	            System.out.println("=".repeat(100));

	            boolean hasData = false;
	            while (rs.next()) {
	                hasData = true;
	                System.out.print(rs.getInt("USER_ID") + "\t");
	                System.out.print(rs.getString("LOGIN_ID") + "\t");
	                System.out.print(rs.getString("USER_NAME") + "\t");
	                System.out.print((rs.getString("EMAIL") == null ? "-" : rs.getString("EMAIL")) + "\t");
	                System.out.print((rs.getString("PHONE") == null ? "-" : rs.getString("PHONE")) + "\t");
	                System.out.print(rs.getString("JOIN_DATE") + "\n");
	            }

	            if (!hasData) {
	                System.out.println("승인대기 사용자가 없습니다.");
	            }

	            System.out.println("=".repeat(100));

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

	            String sql = "SELECT USER_ID, LOGIN_ID, USER_NAME, DEPT_NUM, POSITION_NUM, "
	                       + "       APPROVAL_STATUS, EMP_STATUS, USER_ROLE "
	                       + "FROM USERTEST "
	                       + "ORDER BY USER_ID ASC";

	            pstmt = conn.prepareStatement(sql);
	            rs = pstmt.executeQuery();

	            System.out.println("=".repeat(120));
	            System.out.println("전체 사원 목록");
	            System.out.println("=".repeat(120));
	            System.out.println("ID\tLOGIN_ID\t이름\t부서\t직급\t승인상태\t재직상태\t권한");
	            System.out.println("=".repeat(120));

	            while (rs.next()) {
	                System.out.print(rs.getInt("USER_ID") + "\t");
	                System.out.print(rs.getString("LOGIN_ID") + "\t");
	                System.out.print(rs.getString("USER_NAME") + "\t");
	                System.out.print((rs.getObject("DEPT_NUM") == null ? "-" : rs.getInt("DEPT_NUM")) + "\t");
	                System.out.print((rs.getObject("POSITION_NUM") == null ? "-" : rs.getInt("POSITION_NUM")) + "\t");
	                String approvalStatus = rs.getString("APPROVAL_STATUS");
	                String empStatus = rs.getString("EMP_STATUS");
	                String userRole = rs.getString("USER_ROLE");

	                System.out.print(approvalStatusToKor(approvalStatus) + "\t");
	                System.out.print(empStatusToKor(empStatus) + "\t");
	                System.out.print(userRoleToKor(userRole) + "\n");
	            }

	            System.out.println("=".repeat(120));

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
	                       + "AND APPROVAL_STATUS = 'PENDING'";

	            pstmt = conn.prepareStatement(sql);
	            pstmt.setInt(1, deptNum);
	            pstmt.setInt(2, positionNum);
	            pstmt.setString(3, userRole); // USER or ADMIN
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

	    // ✅ 부서번호 존재 여부 확인
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

	    // ✅ 직급번호 존재 여부 확인
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

	    // ✅ 관리자용 사원 정보 변경 (부서/직급/재직상태)
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
	            pstmt.setString(3, empStatus); // WORK / LEAVE / RESIGNED
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
	            default:         return empStatus; // 혹시 모르는 값은 원본 출력
	        }
	    }

	    // 승인상태 한글 변환 (선택)
	    private String approvalStatusToKor(String approvalStatus) {
	        if (approvalStatus == null) return "-";

	        switch (approvalStatus.toUpperCase()) {
	            case "PENDING":  return "승인대기";
	            case "APPROVED": return "승인완료";
	            case "REJECTED": return "승인거절";
	            default:         return approvalStatus;
	        }
	    }

	    // 권한 한글 변환 (선택)
	    private String userRoleToKor(String userRole) {
	        if (userRole == null) return "-";

	        switch (userRole.toUpperCase()) {
	            case "ADMIN": return "관리자";
	            case "USER":  return "사원";
	            default:      return userRole;
	        }
	    }
	}

