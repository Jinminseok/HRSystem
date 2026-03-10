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
            System.out.println("부서번호    부서명\t   등록일\t\t  수정일");
            System.out.println("=".repeat(90));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.print("   " + rs.getInt("dept_num") + "\t    ");
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
        PreparedStatement pstmtMax = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 1. 현재 DEPT 테이블 기준 다음 부서번호 구하기
            String nextNumSql = "SELECT NVL(MAX(DEPT_NUM), 0) + 10 AS NEXT_DEPT_NUM FROM DEPT";
            pstmtMax = conn.prepareStatement(nextNumSql);
            rs = pstmtMax.executeQuery();

            int nextDeptNum = 10;
            if (rs.next()) {
                nextDeptNum = rs.getInt("NEXT_DEPT_NUM");
            }

            DBUtil.executeClose(rs, pstmtMax, null);

            // 2. insert
            String sql = "INSERT INTO dept (dept_num, dept_name) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, nextDeptNum);
            pstmt.setString(2, deptName);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 부서 등록 완료! (부서번호=" + nextDeptNum + ")");

                logDao.insertActionLog(
                    adminUserId,
                    "부서관리",
                    "DEPT_CREATE",
                    "부서 등록: dept_num=" + nextDeptNum + ", dept_name=" + deptName,
                    "DEPT",
                    nextDeptNum,
                    (loginLogId != null && loginLogId > 0) ? loginLogId : null
                );
            }

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == 1) {
                System.out.println("❌ 부서명이 중복되었거나 부서번호가 중복되었습니다!");
            } else {
                System.out.println("❌ 무결성 제약조건 위배로 부서 등록에 실패했습니다.");
            }

        } catch (java.sql.SQLException e) {
            if (e.getErrorCode() == 1) {
                System.out.println("❌ 부서명이 중복되었거나 부서번호가 중복되었습니다!");
            } else {
                System.out.println("❌ DB 오류로 부서 등록에 실패했습니다.");
            }

        } catch (Exception e) {
            System.out.println("❌ 시스템 오류로 부서 등록에 실패했습니다.");
            e.printStackTrace();

        } finally {
            DBUtil.executeClose(rs, pstmtMax, null);
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
 // ✅ 부서 목록 UI 출력 (조직도 스타일)
    public void printDeptListUI() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT DEPT_NUM, DEPT_NAME FROM DEPT ORDER BY DEPT_NUM";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            System.out.println("=".repeat(60));
            System.out.println("📌 현재 등록된 부서 목록");
            System.out.println("=".repeat(60));
            System.out.println("부서번호\t부서명");
            System.out.println("-".repeat(60));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println(rs.getInt("DEPT_NUM") + "\t\t" + rs.getString("DEPT_NAME"));
            }

            if (!hasData) System.out.println("등록된 부서가 없습니다.");

            System.out.println("=".repeat(60));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.excuteClose(rs, pstmt, conn); // 너 프로젝트에서 DeptDAO는 excuteClose 쓰는 쪽이 많았지
        }
    }

    // ✅ 부서명으로 부서번호 찾기 (없으면 -1)
    public int getDeptNumByName(String deptName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT DEPT_NUM FROM DEPT WHERE DEPT_NAME = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, deptName);

            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("DEPT_NUM");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.excuteClose(rs, pstmt, conn);
        }

        return -1;
    }
}
