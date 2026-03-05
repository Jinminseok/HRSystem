package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import kr.appointment.HrAppointmentHistoryDAO;
import kr.hrsystem.dao.LogDAO;
import kr.hrsystem.dao.LoginDAO;
import kr.hrsystem.dao.DeptDAO;
import kr.hrsystem.dao.PositionDAO;
import kr.hrsystem.dao.SearchDAO;
import kr.hrsystem.dao.OrgChartDAO;

public class UserManageAdminMenu {

    private BufferedReader br;
    private int adminUserId;
    private int loginLogId;
    private LoginDAO userDao;
    private LogDAO logDao;
    private HrAppointmentHistoryDAO historyDao;
    private DeptDAO deptDao;
    private PositionDAO positionDao;

    public UserManageAdminMenu(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        this.adminUserId = adminUserId;
        this.loginLogId = loginLogId;
        this.userDao = new LoginDAO();
        this.logDao = new LogDAO();
        this.historyDao = new HrAppointmentHistoryDAO();
        this.deptDao = new DeptDAO();
        this.positionDao = new PositionDAO();

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
            System.out.println("│  4. 사원 목록 조회                     ");
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

                    case 2: {
                        // 사원 승인 처리
                        userDao.selectPendingUsers();

                        System.out.print("승인할 USER_ID(취소: 0) : ");
                        String approveInput = br.readLine();

                        int approveUserId;
                        try {
                            approveUserId = Integer.parseInt(approveInput.trim());
                        } catch (NumberFormatException e) {
                            System.out.println("❌ 숫자만 입력하세요.");
                            break;
                        }

                        if (approveUserId == 0) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
                            break; // case 2 종료 → 사원관리 메뉴로 복귀
                        }

                        // ✅ 부서 목록(예쁜 UI) 출력
                        OrgChartDAO orgDao = new OrgChartDAO();
                        orgDao.printDeptGuide();

                        System.out.print("부서명(DEPT_NAME) 입력 : ");
                        String deptName = br.readLine().trim();

                        Integer deptNum = userDao.getDeptNumByName(deptName);
                        if (deptNum == null) {
                            System.out.println("❌ 입력한 부서명(" + deptName + ")은 존재하지 않습니다.");
                            break;
                        }

                        // ✅ 직급 목록(예쁜 UI) 출력
                        PositionDAO posDao = new PositionDAO();
                        posDao.printPositionGuide();

                        System.out.print("직급명(POSITION_NAME) 입력 : ");
                        String positionName = br.readLine().trim();

                        Integer positionNum = userDao.getPositionNumByName(positionName);
                        if (positionNum == null) {
                            System.out.println("❌ 입력한 직급명(" + positionName + ")은 존재하지 않습니다.");
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
                                "userId=" + approveUserId
                                    + ", dept_num=" + deptNum + "(" + deptName + ")"
                                    + ", pos_num=" + positionNum + "(" + positionName + ")"
                                    + ", role=" + role,
                                "USERTEST",
                                approveUserId,
                                (loginLogId > 0 ? loginLogId : null)
                            );
                        } else {
                            System.out.println("❌ 승인 실패 (대상 확인 필요 / 이미 처리됨)");
                        }
                        break;
                    }

                    case 3:
                        // 승인 거절
                        userDao.selectPendingUsers();

                        System.out.print("거절할 USER_ID(취소: 0) : ");
                        String rejectInput = br.readLine();

                        int rejectUserId;
                        try {
                            rejectUserId = Integer.parseInt(rejectInput.trim());
                        } catch (NumberFormatException e) {
                            System.out.println("❌ 숫자만 입력하세요.");
                            break;
                        }

                        if (rejectUserId == 0) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
                            break; // case 3 종료 → 사원관리 메뉴로 복귀
                        }

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
                        // 사원 목록 조회
                        employeeListMenu();
                        break;

                    case 5:
                        // 사원 정보 변경 (부서/직급/재직상태)
                        userDao.selectAllUsers();

                        System.out.print("변경할 USER_ID(취소: 0) : ");
                        String inputUserId = br.readLine().trim();

                        if ("0".equals(inputUserId)) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
                            break;
                        }

                        int targetUserId;
                        try {
                            targetUserId = Integer.parseInt(inputUserId);
                        } catch (NumberFormatException e) {
                            System.out.println("❌ 숫자만 입력하세요.");
                            break;
                        }

                        // ✅ 변경 전 현재값 조회 (이력 저장용)
                        Map<String, Object> beforeUser = userDao.getUserInfoMapById(targetUserId);
                        if (beforeUser == null) {
                            System.out.println("❌ 해당 USER_ID가 존재하지 않습니다.");
                            break;
                        }

                        DeptDAO deptDao = new DeptDAO();
                        PositionDAO positionDao = new PositionDAO();

                        // =========================
                        // ✅ 새 부서명 입력 (취소: 0)
                        // =========================
                        deptDao.printDeptListUI();
                        System.out.print("새 부서명 입력(취소: 0) : ");
                        String newDeptName = br.readLine().trim();

                        if ("0".equals(newDeptName)) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
                            break;
                        }

                        int newDeptNum = deptDao.getDeptNumByName(newDeptName);
                        if (newDeptNum == -1) {
                            System.out.println("❌ 존재하지 않는 부서명입니다: " + newDeptName);
                            break;
                        }

                        // =========================
                        // ✅ 새 직급명 입력 (취소: 0)
                        // =========================
                        positionDao.printPositionListUI();
                        System.out.print("새 직급명 입력(취소: 0) : ");
                        String newPositionName = br.readLine().trim();

                        if ("0".equals(newPositionName)) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
                            break;
                        }

                        int newPositionNum = positionDao.getPositionNumByName(newPositionName);
                        if (newPositionNum == -1) {
                            System.out.println("❌ 존재하지 않는 직급명입니다: " + newPositionName);
                            break;
                        }

                        // =========================
                        // ✅ 재직상태 입력 (취소: 0)
                        // =========================
                        System.out.print("재직상태(재직/휴직/퇴직 또는 WORK/LEAVE/RESIGNED) (취소: 0) : ");
                        String empStatusInput = br.readLine().trim();

                        if ("0".equals(empStatusInput)) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
                            break;
                        }

                        String empStatus = empStatusToCode(empStatusInput);
                        if (empStatus == null) {
                            System.out.println("❌ 재직상태는 재직 / 휴직 / 퇴직 (또는 WORK / LEAVE / RESIGNED)만 가능합니다.");
                            break;
                        }

                        Integer beforeDeptNum = (Integer) beforeUser.get("DEPT_NUM");
                        Integer beforePositionNum = (Integer) beforeUser.get("POSITION_NUM");
                        String beforeEmpStatus = (String) beforeUser.get("EMP_STATUS");

                        boolean deptSame = Objects.equals(beforeDeptNum, newDeptNum);
                        boolean posSame = Objects.equals(beforePositionNum, newPositionNum);
                        boolean statusSame = (beforeEmpStatus != null && beforeEmpStatus.equalsIgnoreCase(empStatus));

                        if (deptSame && posSame && statusSame) {
                            System.out.println("⚠ 변경된 항목이 없습니다.");
                            break;
                        }

                        int updateCnt = userDao.updateUserInfoByAdmin(targetUserId, newDeptNum, newPositionNum, empStatus);

                        if (updateCnt > 0) {
                            System.out.println("✅ 사원 정보 변경 완료!");

                            saveAppointmentHistoryOnUserUpdate(beforeUser, targetUserId, newDeptNum, newPositionNum, empStatus);

                            logDao.insertActionLog(
                                adminUserId,
                                "사원관리",
                                "USER_UPDATE",
                                "userId=" + targetUserId
                                    + ", dept=" + newDeptName
                                    + ", pos=" + newPositionName
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
    private void employeeListMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│            📋 사원 목록 조회 (관리자)        ");
            System.out.println("├─────────────────────────────────────────────");
            System.out.println("│  1. 부서별 사원 목록 조회                    ");
            System.out.println("│  2. 직급별 사원 목록 조회                    ");
            System.out.println("│  3. 전체 사원 목록 조회                      ");
            System.out.println("│  0. 뒤로가기                                ");
            System.out.println("└─────────────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1: {
                        // ✅ 부서 번호표 먼저 보여주기
                        System.out.println("\n[부서 목록]");
                        deptDao.selectDepartment();

                        System.out.print("조회할 부서번호(DEPT_NUM) (0:취소) : ");
                        int deptNum = Integer.parseInt(br.readLine());

                        if (deptNum == 0) break;

                        // ✅ 존재 체크 (잘못 입력 방지)
                        if (!userDao.existsDeptNum(deptNum)) {
                            System.out.println("❌ 해당 부서번호는 존재하지 않습니다.");
                            break;
                        }

                        userDao.selectUsersByDept(deptNum);
                        break;
                    }

                    case 2: {
                        // ✅ 직급 번호표 먼저 보여주기
                        System.out.println("\n[직급 목록]");
                        positionDao.selectPosition();

                        System.out.print("조회할 직급번호(POSITION_NUM) (0:취소) : ");
                        int posNum = Integer.parseInt(br.readLine());

                        if (posNum == 0) break;

                        // ✅ 존재 체크
                        if (!userDao.existsPositionNum(posNum)) {
                            System.out.println("❌ 해당 직급번호는 존재하지 않습니다.");
                            break;
                        }

                        userDao.selectUsersByPosition(posNum);
                        break;
                    }

                    case 3:
                        userDao.selectAllUsers();
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
    
    private void saveAppointmentHistoryOnUserUpdate(
            Map<String, Object> beforeUser,
            int targetUserId,
            int newDeptNum,
            int newPositionNum,
            String newEmpStatus) {

        Integer loginLogIdValue = (loginLogId > 0 ? loginLogId : null);

        Integer beforeDeptNum = (Integer) beforeUser.get("DEPT_NUM");
        Integer beforePositionNum = (Integer) beforeUser.get("POSITION_NUM");
        String beforeEmpStatus = (String) beforeUser.get("EMP_STATUS");

        // 1) 부서 변경
        if (!Objects.equals(beforeDeptNum, newDeptNum)) {
            String beforeValue = (beforeDeptNum == null) ? null : String.valueOf(beforeDeptNum);
            String afterValue = String.valueOf(newDeptNum);

            String beforeLabel = userDao.getDeptNameByNum(beforeDeptNum);
            String afterLabel = userDao.getDeptNameByNum(newDeptNum);

            historyDao.insertHistory(
                targetUserId,
                "DEPT",
                beforeValue,
                afterValue,
                beforeLabel,
                afterLabel,
                "관리자 사원정보 변경",
                adminUserId,
                "USER_MANAGE",
                loginLogIdValue
            );
        }

        // 2) 직급 변경
        if (!Objects.equals(beforePositionNum, newPositionNum)) {
            String beforeValue = (beforePositionNum == null) ? null : String.valueOf(beforePositionNum);
            String afterValue = String.valueOf(newPositionNum);

            String beforeLabel = userDao.getPositionNameByNum(beforePositionNum);
            String afterLabel = userDao.getPositionNameByNum(newPositionNum);

            historyDao.insertHistory(
                targetUserId,
                "POSITION",
                beforeValue,
                afterValue,
                beforeLabel,
                afterLabel,
                "관리자 사원정보 변경",
                adminUserId,
                "USER_MANAGE",
                loginLogIdValue
            );
        }

        // 3) 재직상태 변경
        if (beforeEmpStatus == null || !beforeEmpStatus.equalsIgnoreCase(newEmpStatus)) {
            historyDao.insertHistory(
                targetUserId,
                "EMP_STATUS",
                beforeEmpStatus,
                newEmpStatus,
                empStatusToKor(beforeEmpStatus),
                empStatusToKor(newEmpStatus),
                "관리자 사원정보 변경",
                adminUserId,
                "USER_MANAGE",
                loginLogIdValue
            );
        }
    }
}