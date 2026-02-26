package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.util.DBUtil;

public class SearchDAO {

    private void printHeader() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-6s %-10s %-10s %-8s %-8s %-12s%n",
                "ID", "LOGIN_ID", "NAME", "DEPT", "POS", "JOIN_DATE");
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void printRow(ResultSet rs) throws Exception {
        System.out.printf("%-6d %-10s %-10s %-8s %-8s %-12s%n",
                rs.getInt("USER_ID"),
                rs.getString("LOGIN_ID"),
                rs.getString("USER_NAME"),
                rs.getString("DEPT_NAME"),
                rs.getString("POSITION_NAME"),
                rs.getDate("JOIN_DATE")); // 가입일 출력
    }

    private void noData() {
        System.out.println("검색 결과가 없습니다.");
    }

    // 1. 사번 검색
    public void searchUserByUserId(int userId) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "NVL(d.dept_name,'-') dept_name, " +
            "NVL(p.position_name,'-') position_name, " +
            "u.join_date " +
            "FROM usertest u " +
            "LEFT JOIN dept d ON u.dept_num = d.dept_num " +
            "LEFT JOIN position p ON u.position_num = p.position_num " +
            "WHERE u.user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }
                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. 이름 부분검색
    public void searchUserByName(String name) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "NVL(d.dept_name,'-') dept_name, " +
            "NVL(p.position_name,'-') position_name, " +
            "u.join_date " +
            "FROM usertest u " +
            "LEFT JOIN dept d ON u.dept_num = d.dept_num " +
            "LEFT JOIN position p ON u.position_num = p.position_num " +
            "WHERE u.user_name LIKE ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }
                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3. 부서 검색
    public void searchUserByDeptName(String deptName) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "d.dept_name, " +
            "NVL(p.position_name,'-') position_name, " +
            "u.join_date " +
            "FROM usertest u " +
            "JOIN dept d ON u.dept_num = d.dept_num " +
            "LEFT JOIN position p ON u.position_num = p.position_num " +
            "WHERE d.dept_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, deptName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }
                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 4. 직급 검색
    public void searchUserByPositionName(String posName) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "NVL(d.dept_name,'-') dept_name, " +
            "p.position_name, " +
            "u.join_date " +
            "FROM usertest u " +
            "LEFT JOIN dept d ON u.dept_num = d.dept_num " +
            "JOIN position p ON u.position_num = p.position_num " +
            "WHERE p.position_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, posName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }
                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 5. 가입일 기간 검색 (시간 포함 문제 해결)
    public void searchUserByJoinDateRange(Date start, Date end) {
        String sql =
            "SELECT u.user_id, u.login_id, u.user_name, " +
            "NVL(d.dept_name,'-') dept_name, " +
            "NVL(p.position_name,'-') position_name, " +
            "u.join_date " +
            "FROM usertest u " +
            "LEFT JOIN dept d ON u.dept_num = d.dept_num " +
            "LEFT JOIN position p ON u.position_num = p.position_num " +
            "WHERE u.join_date >= ? AND u.join_date < (? + 1) " +
            "ORDER BY u.join_date";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, start);
            pstmt.setDate(2, end);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    noData();
                    return;
                }
                printHeader();
                do {
                    printRow(rs);
                } while (rs.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  // 부서명 목록 가져오기
    public List<String> getDeptNameList() {
        List<String> list = new java.util.ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT dept_name FROM dept ORDER BY dept_name";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("dept_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn); // 
        }
        return list;
    }
    
 // 직급명 목록 가져오기
    public List<String> getPositionNameList() {
        List<String> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT position_name FROM position ORDER BY position_num";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("position_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           
            DBUtil.executeClose(rs, pstmt, conn);
            
        }
        return list;
    }
    
}