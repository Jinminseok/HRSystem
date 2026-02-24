package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class DeptDAO {

    private LogDAO logDao = new LogDAO();

    // ==========================
    // 1. 부서 전체 조회
    // ==========================
    public void selectDepartment() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT dept_num, dept_name, "
                       + "       TO_CHAR(dept_day, 'YYYY-MM-DD HH24:MI') AS dept_day, "
                       + "       TO_CHAR(dept_modified_date, 'YYYY-MM-DD HH24:MI') AS dept_modified_date "
                       + "FROM dept "
                       + "ORDER BY dept_num ASC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            System.out.println("=".repeat(90));
            System.out.println("부서번호\t부서명\t등록일\t\t\t수정일");
            System.out.println("=".repeat(90));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.print(rs.getInt("dept_num") + "\t");
                System.out.print(rs.getString("dept_name") + "\t");
                System.out.print(rs.getString("dept_day") + "\t");
                System.out.print((rs.getString("dept_modified_date") == null ? "-" : rs.getString("dept_modified_date")) + "\n");
            }

            if (!hasData) {
                System.out.println("등록된 부서가 없습니다.");
            }

            System.out.println("=".repeat(90));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 2. 부서 등록 (로그 포함)
    // ==========================
    public void insertDepartment(String deptName, int adminUserId, Integer loginLogId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmtSeq = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "INSERT INTO dept (dept_num, dept_name) "
                       + "VALUES (seq_dept.NEXTVAL, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, deptName);

            int count = pstmt.executeUpdate();

            DBUtil.executeClose(null, pstmt, null);

            Integer deptNum = null;
            pstmtSeq = conn.prepareStatement("SELECT seq_dept.CURRVAL AS dept_num FROM dual");
            rs = pstmtSeq.executeQuery();
            if (rs.next()) {
                deptNum = rs.getInt("dept_num");
            }

            if (count > 0) {
                System.out.println("✅ 부서 등록 완료! (dept_num=" + deptNum + ")");

                logDao.insertActionLog(
                    adminUserId,
                    "부서관리",
                    "DEPT_CREATE",
                    "부서 등록: dept_num=" + deptNum + ", dept_name=" + deptName,
                    "DEPT",
                    deptNum,
                    (loginLogId != null && loginLogId > 0) ? loginLogId : null
                );
            }

        } catch (Exception e) {
            // ORA-00001 (중복) 같은 것도 여기로 옴
            System.out.println("❌ 부서 등록 실패 (중복 부서명인지 확인)");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSeq, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 3. 부서 수정 (로그 포함)
    // ==========================
    public void updateDepartment(int deptNum, String newDeptName, int adminUserId, Integer loginLogId) {
        Connection conn = null;
        PreparedStatement pstmtSel = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 변경 전 조회
            String selSql = "SELECT dept_name FROM dept WHERE dept_num = ?";
            pstmtSel = conn.prepareStatement(selSql);
            pstmtSel.setInt(1, deptNum);
            rs = pstmtSel.executeQuery();

            String oldDeptName = null;
            if (rs.next()) {
                oldDeptName = rs.getString("dept_name");
            } else {
                System.out.println("❌ 해당 부서번호가 존재하지 않습니다.");
                return;
            }

            DBUtil.executeClose(rs, pstmtSel, null);

            String sql = "UPDATE dept SET dept_name = ? WHERE dept_num = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newDeptName);
            pstmt.setInt(2, deptNum);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 부서 수정 완료!");

                logDao.insertActionLog(
                    adminUserId,
                    "부서관리",
                    "DEPT_UPDATE",
                    "dept_num=" + deptNum + " | [" + oldDeptName + "] -> [" + newDeptName + "]",
                    "DEPT",
                    deptNum,
                    (loginLogId != null && loginLogId > 0) ? loginLogId : null
                );
            } else {
                System.out.println("❌ 부서 수정 실패");
            }

        } catch (Exception e) {
            System.out.println("❌ 부서 수정 실패 (중복 부서명인지 확인)");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 4. 부서 삭제 (로그 포함)
    // ==========================
    public void deleteDepartment(int deptNum, int adminUserId, Integer loginLogId) {
        Connection conn = null;
        PreparedStatement pstmtSel = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 삭제 전 부서명 조회
            String selSql = "SELECT dept_name FROM dept WHERE dept_num = ?";
            pstmtSel = conn.prepareStatement(selSql);
            pstmtSel.setInt(1, deptNum);
            rs = pstmtSel.executeQuery();

            String deptName = null;
            if (rs.next()) {
                deptName = rs.getString("dept_name");
            } else {
                System.out.println("❌ 해당 부서번호가 존재하지 않습니다.");
                return;
            }

            DBUtil.executeClose(rs, pstmtSel, null);

            String sql = "DELETE FROM dept WHERE dept_num = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, deptNum);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 부서 삭제 완료!");

                logDao.insertActionLog(
                    adminUserId,
                    "부서관리",
                    "DEPT_DELETE",
                    "부서 삭제: dept_num=" + deptNum + ", dept_name=" + deptName,
                    "DEPT",
                    deptNum,
                    (loginLogId != null && loginLogId > 0) ? loginLogId : null
                );
            } else {
                System.out.println("❌ 부서 삭제 실패");
            }

        } catch (Exception e) {
            // FK 걸려있으면 ORA-02292 가능
            System.out.println("❌ 부서 삭제 실패 (해당 부서를 사용하는 사원이 있을 수 있음)");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }
}
