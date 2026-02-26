package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.DeptDAO;

public class DepartmentManageAdminMenu {

    private BufferedReader br;
    private DeptDAO dao;
    private int adminUserId;
    private int loginLogId;

    public DepartmentManageAdminMenu(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        this.adminUserId = adminUserId;
        this.loginLogId = loginLogId;
        this.dao = new DeptDAO();

        try {
            callMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│              🏢 부서 관리 (관리자)          ");
            System.out.println("├─────────────────────────────────────────────");
            System.out.println("│  1. 부서 조회                               ");
            System.out.println("│  2. 부서 등록                               ");
            System.out.println("│  3. 부서 수정                               ");
            System.out.println("│  4. 부서 삭제                               ");
            System.out.println("│  0. 뒤로가기                                ");
            System.out.println("└─────────────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        dao.selectDepartment();
                        break;

                    case 2:
                        System.out.println("\n[ 부서 등록 ]");
                        dao.selectDepartment();
                        System.out.print("등록할 부서명: ");
                        String deptName = br.readLine().trim();

                        if (deptName.length() == 0) {
                            System.out.println("부서명은 비울 수 없습니다.");
                            break;
                        }

                        dao.insertDepartment(deptName, adminUserId, (loginLogId > 0 ? loginLogId : null));
                        break;

                    case 3:
                        System.out.println("\n[ 부서 수정 ]");
                        dao.selectDepartment();

                        System.out.print("수정할 부서번호: ");
                        int deptNum = Integer.parseInt(br.readLine());

                        System.out.print("새 부서명: ");
                        String newDeptName = br.readLine().trim();

                        if (newDeptName.length() == 0) {
                            System.out.println("부서명은 비울 수 없습니다.");
                            break;
                        }

                        dao.updateDepartment(deptNum, newDeptName, adminUserId, (loginLogId > 0 ? loginLogId : null));
                        break;

                    case 4:
                        System.out.println("\n[ 부서 삭제 ]");
                        dao.selectDepartment();

                        System.out.print("삭제할 부서번호: ");
                        int deleteDeptNum = Integer.parseInt(br.readLine());

                        System.out.print("정말 삭제하시겠습니까? (Y/N): ");
                        String yn = br.readLine().trim().toUpperCase();

                        if (!"Y".equals(yn)) {
                            System.out.println("삭제를 취소했습니다.");
                            break;
                        }

                        dao.deleteDepartment(deleteDeptNum, adminUserId, (loginLogId > 0 ? loginLogId : null));
                        break;

                    case 0:
                        return; // ✅ AdminScreen으로 복귀

                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
            }
        }
    }

    // 단독 실행 테스트용 (선택)
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(System.in));
            new DepartmentManageAdminMenu(br, 1, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
