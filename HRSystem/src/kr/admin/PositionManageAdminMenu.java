package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.LogDAO;
import kr.hrsystem.dao.PositionDAO;

public class PositionManageAdminMenu {

    private BufferedReader br;
    private PositionDAO dao;
    private LogDAO logDao;

    private int adminUserId;
    private int loginLogId;

    // ✅ AdminScreen에서 공유 br, 관리자정보 전달받기
    public PositionManageAdminMenu(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        this.dao = new PositionDAO();
        this.logDao = new LogDAO();
        this.adminUserId = adminUserId;
        this.loginLogId = loginLogId;

        try {
            callMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ❌ br.close() 금지 (공유 스트림)
    }

    private void callMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│              🏷 직급 관리 (관리자)          ");
            System.out.println("├─────────────────────────────────────────────");
            System.out.println("│  1. 직급 조회                               ");
            System.out.println("│  2. 직급 등록                               ");
            System.out.println("│  3. 직급 수정                               ");
            System.out.println("│  4. 직급 삭제                               ");
            System.out.println("│  5. 직급별 기본급 설정                      ");
            System.out.println("│  0. 뒤로가기                                ");
            System.out.println("└─────────────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        dao.selectPosition();

                        // 로그 (선택)
                        writeLog("직급관리", "POSITION_LIST", "직급 목록 조회", "POSITION", null);
                        break;

                    case 2:
                    	 dao.selectPosition();
                        System.out.print("등록할 직급명: ");
                        String positionName = br.readLine().trim();

                        System.out.print("기본급: ");
                        int positionSal = Integer.parseInt(br.readLine());

                        int insertCnt = dao.insertPosition(positionName, positionSal);

                        if (insertCnt > 0) {
                            writeLog(
                                "직급관리",
                                "POSITION_CREATE",
                                "직급 등록: name=" + positionName + ", sal=" + positionSal,
                                "POSITION",
                                null
                            );
                        }
                        break;

                    case 3:
                        dao.selectPosition();

                        System.out.print("수정할 직급번호: ");
                        int updatePositionNum = Integer.parseInt(br.readLine());

                        String oldName = dao.getPositionNameByNum(updatePositionNum);

                        System.out.print("새 직급명: ");
                        String newPositionName = br.readLine().trim();

                        System.out.print("새 기본급: ");
                        int newPositionSal = Integer.parseInt(br.readLine());

                        int updateCnt = dao.updatePosition(updatePositionNum, newPositionName, newPositionSal);

                        if (updateCnt > 0) {
                            writeLog(
                                "직급관리",
                                "POSITION_UPDATE",
                                "직급 수정: num=" + updatePositionNum + ", name=[" + oldName + "] -> [" + newPositionName + "], sal=" + newPositionSal,
                                "POSITION",
                                updatePositionNum
                            );
                        }
                        break;

                    case 4:
                        dao.selectPosition();

                        System.out.print("삭제할 직급번호: ");
                        int deletePositionNum = Integer.parseInt(br.readLine());
                        System.out.print("정말 삭제하시겠습니까? (Y/N): ");
                        String yn = br.readLine().trim().toUpperCase();

                        if (!"Y".equals(yn)) {
                            System.out.println("삭제를 취소했습니다.");
                            break;
                        }
                        String deleteName = dao.getPositionNameByNum(deletePositionNum);

                        int deleteCnt = dao.deletePosition(deletePositionNum);

                        if (deleteCnt > 0) {
                            writeLog(
                                "직급관리",
                                "POSITION_DELETE",
                                "직급 삭제: num=" + deletePositionNum + ", name=" + deleteName,
                                "POSITION",
                                deletePositionNum
                            );
                        }
                        break;

                    case 5:
                        dao.selectPosition();

                        System.out.print("기본급 변경할 직급번호: ");
                        int salaryPositionNum = Integer.parseInt(br.readLine());

                        String salaryName = dao.getPositionNameByNum(salaryPositionNum);

                        System.out.print("새 기본급: ");
                        int newSalary = Integer.parseInt(br.readLine());

                        int salCnt = dao.updateSalary(salaryPositionNum, newSalary);

                        if (salCnt > 0) {
                            writeLog(
                                "직급관리",
                                "POSITION_SALARY_UPDATE",
                                "기본급 변경: num=" + salaryPositionNum + ", name=" + salaryName + ", newSal=" + newSalary,
                                "POSITION",
                                salaryPositionNum
                            );
                        }
                        break;

                    case 0:
                        return; // ✅ AdminScreen으로 복귀

                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력가능");
            }
        }
    }

    // 공통 로그 기록 헬퍼
    private void writeLog(String menuName, String actionType, String actionDesc,
                          String targetTable, Integer targetId) {
        try {
            logDao.insertActionLog(
                adminUserId,
                menuName,
                actionType,
                actionDesc,
                targetTable,
                targetId,
                (loginLogId > 0 ? loginLogId : null)
            );
        } catch (Exception e) {
            System.out.println("⚠ 로그 기록 실패: " + e.getMessage());
        }
    }

    // 단독 테스트용
    public static void main(String[] args) {
        try {
            java.io.BufferedReader br =
                new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

            new PositionManageAdminMenu(br, 1, -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
