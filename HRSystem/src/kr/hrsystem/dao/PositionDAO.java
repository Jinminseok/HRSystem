package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class PositionDAO {

    // 1) 직급 전체 조회
    public void selectPosition() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT POSITION_NUM, POSITION_NAME, POSITION_SAL "
                       + "FROM POSITION "
                       + "ORDER BY POSITION_NUM";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            System.out.println("=".repeat(50));
            System.out.println("직급번호    직급명\t  기본급");
            System.out.println("=".repeat(50));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.print("  " + rs.getInt("POSITION_NUM") + "\t  ");
                System.out.print("   " + rs.getString("POSITION_NAME") + " \t ");
                System.out.print(rs.getInt("POSITION_SAL") + "\n");
            }

            if (!hasData) {
                System.out.println("직급이 존재하지 않습니다.");
            }

            System.out.println("=".repeat(50));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 2) 직급 등록 (번호는 시퀀스로 자동)
    public int insertPosition(String positionName, int positionSal) {
        Connection conn = null;
        PreparedStatement pstmtMax = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 1. 현재 POSITION 테이블 기준 다음 직급번호 구하기
            String maxSql = "SELECT NVL(MAX(POSITION_NUM), 0) + 1 AS NEXT_POSITION_NUM FROM POSITION";
            pstmtMax = conn.prepareStatement(maxSql);
            rs = pstmtMax.executeQuery();

            int nextPositionNum = 10;
            if (rs.next()) {
                nextPositionNum = rs.getInt("NEXT_POSITION_NUM");
            }

            DBUtil.executeClose(rs, pstmtMax, null);
            rs = null;
            pstmtMax = null;

            // 2. insert
            String sql = "INSERT INTO POSITION (POSITION_NUM, POSITION_NAME, POSITION_SAL) "
                       + "VALUES (?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, nextPositionNum);
            pstmt.setString(2, positionName);
            pstmt.setInt(3, positionSal);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 직급 등록이 완료되었습니다. (직급번호=" + nextPositionNum + ")");
            }
            return count;

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == 1) {
                System.out.println("❌ 직급명이 중복되었거나 직급번호가 중복되었습니다!");
            } else {
                System.out.println("❌ 무결성 제약조건 위배로 등록 실패했습니다.");
            }
            return 0;

        } catch (java.sql.SQLException e) {
            if (e.getErrorCode() == 1) {
                System.out.println("❌ 직급명이 중복되었거나 직급번호가 중복되었습니다!");
                return 0;
            }
            System.out.println("❌ DB 오류로 직급 등록에 실패했습니다.");
            return 0;

        } catch (Exception e) {
            System.out.println("❌ 시스템 오류로 직급 등록에 실패했습니다.");
            e.printStackTrace();
            return 0;

        } finally {
            DBUtil.executeClose(rs, pstmtMax, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }
    //직급목록 출력 (ui 예쁘게)
    public void printPositionGuide() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT POSITION_NUM, POSITION_NAME " +
                "FROM POSITION " +
                "ORDER BY POSITION_NUM";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("📌 현재 등록된 직급 목록");
            System.out.println("=".repeat(60));
            System.out.println("직급번호\t직급명");
            System.out.println("-".repeat(60));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println(rs.getInt("POSITION_NUM") + "\t\t" + rs.getString("POSITION_NAME"));
            }

            if (!hasData) {
                System.out.println("등록된 직급이 없습니다.");
            }

            System.out.println("=".repeat(60));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.excuteClose(rs, pstmt, conn);
        }
    }
 // ✅ 직급 목록 UI 출력 (조직도 스타일)
    public void printPositionListUI() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT POSITION_NUM, POSITION_NAME FROM POSITION ORDER BY POSITION_NUM";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            System.out.println("=".repeat(60));
            System.out.println("📌 현재 등록된 직급 목록");
            System.out.println("=".repeat(60));
            System.out.println("직급번호\t직급명");
            System.out.println("-".repeat(60));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println(rs.getInt("POSITION_NUM") + "\t\t" + rs.getString("POSITION_NAME"));
            }

            if (!hasData) System.out.println("등록된 직급이 없습니다.");

            System.out.println("=".repeat(60));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ✅ 직급명으로 직급번호 찾기 (없으면 -1)
    public int getPositionNumByName(String positionName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT POSITION_NUM FROM POSITION WHERE POSITION_NAME = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, positionName);

            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("POSITION_NUM");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return -1;
    }

    // 3) 직급 수정 (직급번호 기준)
    public int updatePosition(int positionNum, String newPositionName, int positionSal) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "UPDATE POSITION "
                       + "SET POSITION_NAME = ?, POSITION_SAL = ? "
                       + "WHERE POSITION_NUM = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPositionName);
            pstmt.setInt(2, positionSal);
            pstmt.setInt(3, positionNum);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ " + count + "개 직급 수정 완료");
            } else {
                System.out.println("❌ 해당 직급번호가 존재하지 않습니다.");
            }

            return count;

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            // UNIQUE 제약조건 위반 (중복)
            if (e.getErrorCode() == 1) { // ORA-00001
                System.out.println("❌ 직급명이 중복되었습니다!");
            } else {
                System.out.println("❌ 무결성 제약조건 위배로 수정 실패");
            }
            return 0;

        } catch (java.sql.SQLException e) {
            if (e.getErrorCode() == 1) {
                System.out.println("❌ 직급명이 중복되었습니다!");
            } else {
                System.out.println("❌ DB 오류로 직급 수정 실패");
            }
            return 0;

        } catch (Exception e) {
            System.out.println("❌ 시스템 오류로 직급 수정 실패");
            return 0;

        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 4) 직급 사용중 인원 수 체크
    // USERTEST 기준 (USER_C 쓰면 USER_C로 바꾸기)
    public int positionCount(int positionNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT COUNT(*) "
                       + "FROM USERTEST "
                       + "WHERE POSITION_NUM = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, positionNum);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return count;
    }

    // 5) 직급 삭제 (번호 기준, 사용 중이면 삭제 불가)
    public int deletePosition(int positionNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            int empCount = positionCount(positionNum);

            if (empCount > 0) {
                System.out.println("❌ 해당 직급 사용 인원이 " + empCount + "명 있어 삭제할 수 없습니다.");
                return 0;
            }

            conn = DBUtil.getConnection();

            String sql = "DELETE FROM POSITION WHERE POSITION_NUM = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, positionNum);

            int count = pstmt.executeUpdate();

            if (count == 1) {
                System.out.println("✅ 직급 삭제가 완료되었습니다.");
            } else {
                System.out.println("❌ 해당 직급번호가 존재하지 않습니다.");
            }

            return count;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 6) 직급별 기본급 수정 (번호 기준)
    public int updateSalary(int positionNum, int newSalary) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "UPDATE POSITION "
                       + "SET POSITION_SAL = ? "
                       + "WHERE POSITION_NUM = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newSalary);
            pstmt.setInt(2, positionNum);

            int count = pstmt.executeUpdate();

            if (count == 1) {
                System.out.println("✅ 기본급 수정 완료");
            } else {
                System.out.println("❌ 해당 직급번호가 존재하지 않습니다.");
            }

            return count;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ 기본급 수정 실패");
            return 0;
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 7) 로그용/검증용: 직급명 조회
    public String getPositionNameByNum(int positionNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT POSITION_NAME FROM POSITION WHERE POSITION_NUM = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, positionNum);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("POSITION_NAME");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return null;
    }
}
