package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.LogDAO;
import kr.hrsystem.dao.PositionDAO;

public class Position_Admin {

    private BufferedReader br;
    private PositionDAO dao;
    private LogDAO logDao;

    private int adminUserId;
    private int loginLogId;
 
    // ✅ AdminScreen에서 공유 br, 관리자정보 전달받기
    public Position_Admin(BufferedReader br, int adminUserId, int loginLogId) {
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
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│          🏷 직급 관리 (관리자)           │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 직급 조회                           │");
            System.out.println("│  [2] 직급 등록                           │");
            System.out.println("│  [3] 직급 수정                           │");
            System.out.println("│  [4] 직급 삭제                           │");
            System.out.println("│  [5] 직급별 기본급 설정                  │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
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

                    	System.out.print("등록할 직급명(뒤로가기: 0): ");
                    	String positionName = br.readLine().trim();
                    	if ("0".equals(positionName)) {
                    	    System.out.println("직급 등록을 취소했습니다.");
                    	    break;
                    	}
                    	if (positionName.length() == 0) {
                    	    System.out.println("직급명은 비울 수 없습니다.");
                    	    break;
                    	}

                    	System.out.print("기본급(뒤로가기: 0): ");
                    	String salInput = br.readLine().trim();
                    	if ("0".equals(salInput)) {
                    	    System.out.println("직급 등록을 취소했습니다.");
                    	    break;
                    	}
                    	int positionSal = Integer.parseInt(salInput);

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

                    	System.out.print("수정할 직급번호(뒤로가기: 0): ");
                    	String numInput = br.readLine().trim();
                    	if ("0".equals(numInput)) {
                    	    System.out.println("직급 수정을 취소했습니다.");
                    	    break;
                    	}
                    	int updatePositionNum = Integer.parseInt(numInput);

                    	String oldName = dao.getPositionNameByNum(updatePositionNum);
                    	if (oldName == null) {
                    	    System.out.println("❌ 해당 직급번호가 존재하지 않습니다.");
                    	    break;
                    	}

                    	System.out.print("새 직급명(뒤로가기: 0): ");
                    	String newPositionName = br.readLine().trim();
                    	if ("0".equals(newPositionName)) {
                    	    System.out.println("직급 수정을 취소했습니다.");
                    	    break;
                    	}
                    	if (newPositionName.length() == 0) {
                    	    System.out.println("직급명은 비울 수 없습니다.");
                    	    break;
                    	}

                    	System.out.print("새 기본급(뒤로가기: 0): ");
                    	String newSalInput = br.readLine().trim();
                    	if ("0".equals(newSalInput)) {
                    	    System.out.println("직급 수정을 취소했습니다.");
                    	    break;
                    	}
                    	int newPositionSal = Integer.parseInt(newSalInput);

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

                    	System.out.print("삭제할 직급번호(뒤로가기: 0): ");
                    	String delNumInput = br.readLine().trim();
                    	if ("0".equals(delNumInput)) {
                    	    System.out.println("직급 삭제를 취소했습니다.");
                    	    break;
                    	}
                    	int deletePositionNum = Integer.parseInt(delNumInput);

                    	String deleteName = dao.getPositionNameByNum(deletePositionNum);
                    	if (deleteName == null) {
                    	    System.out.println("❌ 해당 직급번호가 존재하지 않습니다.");
                    	    break;
                    	}

                    	System.out.print("정말 삭제하시겠습니까? (Y/N), 뒤로가기: 0): ");
                    	String yn = br.readLine().trim().toUpperCase();
                    	if ("0".equals(yn)) {
                    	    System.out.println("직급 삭제를 취소했습니다.");
                    	    break;
                    	}
                    	if (!"Y".equals(yn)) {
                    	    System.out.println("삭제를 취소했습니다.");
                    	    break;
                    	}

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

                    	System.out.print("기본급 변경할 직급번호(뒤로가기: 0): ");
                    	String salaryNumInput = br.readLine().trim();
                    	if ("0".equals(salaryNumInput)) {
                    	    System.out.println("기본급 변경을 취소했습니다.");
                    	    break;
                    	}
                    	int salaryPositionNum = Integer.parseInt(salaryNumInput);

                    	String salaryName = dao.getPositionNameByNum(salaryPositionNum);
                    	if (salaryName == null) {
                    	    System.out.println("❌ 해당 직급번호가 존재하지 않습니다.");
                    	    break;
                    	}

                    	System.out.print("새 기본급(뒤로가기: 0): ");
                    	String newSalaryInput = br.readLine().trim();
                    	if ("0".equals(newSalaryInput)) {
                    	    System.out.println("기본급 변경을 취소했습니다.");
                    	    break;
                    	}
                    	int newSalary = Integer.parseInt(newSalaryInput);

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

            new Position_Admin(br, 1, -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
