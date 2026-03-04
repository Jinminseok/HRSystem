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
	 //로그인 아이디 중복 체크
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
	            DBUtil.executeClose(rs, pstmt, conn); // 너 프로젝트 close 메서드명에 맞춰 사용
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
	            		   + "       APPROVAL_STATUS AS APPROVAL_STATUS, "
	                       + "       TO_CHAR(JOIN_DATE, 'YYYY-MM-DD HH24:MI') AS JOIN_DATE "
	                       + "FROM USERTEST "
	                       + "WHERE APPROVAL_STATUS IN ('PENDING','REJECTED') "
	                       + "ORDER BY USER_ID ASC";

	            pstmt = conn.prepareStatement(sql);
	            rs = pstmt.executeQuery();

	            System.out.println("=".repeat(100));
	            System.out.println("승인대기 사원 목록");
	            System.out.println("=".repeat(100));
	            System.out.println("USER_ID\tLOGIN_ID\t이름\t승인상태\t이메일\t전화번호\t가입일");
	            System.out.println("=".repeat(100));

	            boolean hasData = false;
	            while (rs.next()) {
	                hasData = true;
	                System.out.print(rs.getInt("USER_ID") + "\t");
	                System.out.print(rs.getString("LOGIN_ID") + "\t");
	                System.out.print(rs.getString("USER_NAME") + "\t");
	                String status = rs.getString("APPROVAL_STATUS");
	                System.out.print(approvalStatusToKor(status) + "\t");
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

	            System.out.println("=".repeat(120));
	            System.out.println("전체 사원 목록");
	            System.out.println("=".repeat(120));
	            System.out.println("ID\tLOGIN_ID\t이름\t부서\t직급\t승인상태\t재직상태\t권한");
	            System.out.println("=".repeat(120));

	            while (rs.next()) {
	                System.out.print(rs.getInt("USER_ID") + "\t");
	                System.out.print(rs.getString("LOGIN_ID") + "\t");
	                System.out.print(rs.getString("USER_NAME") + "\t");
	                System.out.print(rs.getString("DEPT_NAME") + "\t");
	                System.out.print(rs.getString("POSITION_NAME") + "\t");
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

	         System.out.println("=".repeat(120));
	         System.out.println("부서별 사원 목록 (DEPT_NUM = " + deptNum + ")");
	         System.out.println("=".repeat(120));
	         System.out.println("ID\tLOGIN_ID\t이름\t부서\t직급\t승인상태\t재직상태\t권한");
	         System.out.println("=".repeat(120));

	         boolean hasData = false;
	         while (rs.next()) {
	             hasData = true;

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

	         if (!hasData) {
	             System.out.println("해당 부서에 사원이 없습니다.");
	         }

	         System.out.println("=".repeat(120));

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

	         System.out.println("=".repeat(120));
	         System.out.println("직급별 사원 목록 (POSITION_NUM = " + positionNum + ")");
	         System.out.println("=".repeat(120));
	         System.out.println("ID\tLOGIN_ID\t이름\t부서\t직급\t승인상태\t재직상태\t권한");
	         System.out.println("=".repeat(120));

	         boolean hasData = false;
	         while (rs.next()) {
	             hasData = true;

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

	         if (!hasData) {
	             System.out.println("해당 직급에 사원이 없습니다.");
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
	                       + "AND APPROVAL_STATUS IN ('PENDING', 'REJECTED')";

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
	    //사용자 현재값 조회 (수정 전 값)
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
	    //부서명 조회 (테이블명/컬럼명 확인 필요)
	    public String getDeptNameByNum(Integer deptNum) {
	        if (deptNum == null) return null;

	        // 실제 테이블명/컬럼명 기준으로 맞추기
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
	    
	    //직급명 조회 (테이블명/컬럼명 확인 필요)
	    public String getPositionNameByNum(Integer positionNum) {
	        if (positionNum == null) return null;

	        // ✅ 네 실제 테이블명/컬럼명으로 수정
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
	}

