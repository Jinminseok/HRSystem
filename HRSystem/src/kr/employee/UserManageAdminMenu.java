package kr.employee;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.LogDAO;
import kr.hrsystem.dao.LoginDAO;



public class UserManageAdminMenu {

    private BufferedReader br;
    private int adminUserId;
    private int loginLogId;

    private LoginDAO userDao;
    private LogDAO logDao;

    public UserManageAdminMenu(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        this.adminUserId = adminUserId;
        this.loginLogId = loginLogId;
        this.userDao = new LoginDAO();
        this.logDao = new LogDAO();

        try {
            menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void menu() throws IOException {

        while (true) {
            System.out.println();
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│              👥 사원 관리 (관리자)          ");
            System.out.println("├─────────────────────────────────────────────");
            System.out.println("│  1. 승인대기 사원 목록 조회                 ");
            System.out.println("│  2. 사원 승인 처리                          ");
            System.out.println("│  3. 사원 승인 거절                          ");
            System.out.println("│  4. 전체 사원 목록 조회                     ");
            System.out.println("│  5. 사원 정보 변경(부서/직급/재직상태)      ");
            System.out.println("│  0. 뒤로가기                               ");
            System.out.println("└─────────────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        // 승인대기 목록
                        userDao.selectPendingUsers();
                        break;

                    case 2:
                        // 사원 승인 처리
                        userDao.selectPendingUsers();

                        System.out.print("승인할 USER_ID : ");
                        int approveUserId = Integer.parseInt(br.readLine());

                        System.out.print("부서번호(DEPT_NUM) : ");
                        int deptNum = Integer.parseInt(br.readLine());

                        // 부서번호 존재 체크
                        if (!userDao.existsDeptNum(deptNum)) {
                            System.out.println("❌ 입력한 부서번호(" + deptNum + ")는 등록된 부서 정보가 없습니다.");
                            break;
                        }

                        System.out.print("직급번호(POSITION_NUM) : ");
                        int positionNum = Integer.parseInt(br.readLine());

                        // 직급번호 존재 체크
                        if (!userDao.existsPositionNum(positionNum)) {
                            System.out.println("❌ 입력한 직급번호(" + positionNum + ")는 등록된 직급 정보가 없습니다.");
                            break;
                        }

                        System.out.print("권한(USER/ADMIN) : ");
                        String role = br.readLine().trim().toUpperCase();
                        if (!"ADMIN".equals(role)) role = "USER";

                        int approveCnt = userDao.approveUser(approveUserId, deptNum, positionNum, role);

                        if (approveCnt > 0) {
                            System.out.println("✅ 사원 승인 완료!");

                            logDao.insertActionLog(
                                adminUserId,
                                "사원관리",
                                "USER_APPROVE",
                                "userId=" + approveUserId + ", dept=" + deptNum + ", pos=" + positionNum + ", role=" + role,
                                "USERTEST",
                                approveUserId,
                                (loginLogId > 0 ? loginLogId : null)
                            );
                        } else {
                            System.out.println("❌ 승인 실패 (대상 확인 필요 / 이미 처리됨)");
                        }
                        break;

                    case 3:
                        // 승인 거절
                        userDao.selectPendingUsers();

                        System.out.print("거절할 USER_ID : ");
                        int rejectUserId = Integer.parseInt(br.readLine());

                        int rejectCnt = userDao.rejectUser(rejectUserId);

                        if (rejectCnt > 0) {
                            System.out.println("✅ 사원 승인 거절 처리 완료");

                            logDao.insertActionLog(
                                adminUserId,
                                "사원관리",
                                "USER_REJECT",
                                "userId=" + rejectUserId,
                                "USERTEST",
                                rejectUserId,
                                (loginLogId > 0 ? loginLogId : null)
                            );
                        } else {
                            System.out.println("❌ 거절 실패 (대상 확인 필요 / 이미 처리됨)");
                        }
                        break;

                    case 4:
                        // 전체 사원 조회
                        userDao.selectAllUsers();
                        break;

                    case 5:
                        // 사원 정보 변경 (부서/직급/재직상태)
                        userDao.selectAllUsers();

                        System.out.print("변경할 USER_ID : ");
                        int targetUserId = Integer.parseInt(br.readLine());

                        System.out.print("새 부서번호(DEPT_NUM) : ");
                        int newDeptNum = Integer.parseInt(br.readLine());

                        if (!userDao.existsDeptNum(newDeptNum)) {
                            System.out.println("❌ 해당 부서번호는 존재하지 않습니다.");
                            break;
                        }

                        System.out.print("새 직급번호(POSITION_NUM) : ");
                        int newPositionNum = Integer.parseInt(br.readLine());

                        if (!userDao.existsPositionNum(newPositionNum)) {
                            System.out.println("❌ 해당 직급번호는 존재하지 않습니다.");
                            break;
                        }

                        System.out.print("재직상태(재직/휴직/퇴직 또는 WORK/LEAVE/RESIGNED) : ");
                        String empStatusInput = br.readLine();
                        String empStatus = empStatusToCode(empStatusInput);

                        if (empStatus == null) {
                            System.out.println("❌ 재직상태는 재직 / 휴직 / 퇴직 (또는 WORK / LEAVE / RESIGNED)만 가능합니다.");
                            break;
                        }

                        int updateCnt = userDao.updateUserInfoByAdmin(targetUserId, newDeptNum, newPositionNum, empStatus);

                        if (updateCnt > 0) {
                            System.out.println("✅ 사원 정보 변경 완료!");

                            logDao.insertActionLog(
                                adminUserId,
                                "사원관리",
                                "USER_UPDATE",
                                "userId=" + targetUserId
                                    + ", dept=" + newDeptNum
                                    + ", pos=" + newPositionNum
                                    + ", empStatus=" + empStatusToKor(empStatus),
                                "USERTEST",
                                targetUserId,
                                (loginLogId > 0 ? loginLogId : null)
                            );
                        } else {
                            System.out.println("❌ 변경 실패 (USER_ID 확인)");
                        }
                        break;

                    case 0:
                        return;

                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
            }
        }
    }

    // ✅ 재직상태 입력값(한글/영문) -> DB 코드값 변환
    private String empStatusToCode(String input) {
        if (input == null) return null;

        String value = input.trim();

        // 한글 입력
        if ("재직".equals(value)) return "WORK";
        if ("휴직".equals(value)) return "LEAVE";
        if ("퇴직".equals(value)) return "RESIGNED";

        // 영문 입력
        value = value.toUpperCase();
        if ("WORK".equals(value)) return "WORK";
        if ("LEAVE".equals(value)) return "LEAVE";
        if ("RESIGNED".equals(value)) return "RESIGNED";

        return null;
    }

    // ✅ 코드값 -> 한글 (로그/출력용)
    private String empStatusToKor(String code) {
        if (code == null) return "-";

        switch (code.toUpperCase()) {
            case "WORK":
                return "재직";
            case "LEAVE":
                return "휴직";
            case "RESIGNED":
                return "퇴직";
            case "WAIT":
                return "대기";
            default:
                return code;
        }
    }
}
