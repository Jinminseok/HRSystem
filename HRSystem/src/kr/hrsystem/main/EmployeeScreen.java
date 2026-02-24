package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;

import kr.attendance.UserAttendanceMenu;
import kr.hrsystem.dao.LogDAO;
import kr.notice.NoticeEmployee;


public class EmployeeScreen {

    private BufferedReader br;
    private int userId;
    private int loginLogId;
    private LogDAO logDao;

    public EmployeeScreen(BufferedReader br, int userId, int loginLogId) {
        this.br = br;
        this.userId = userId;
        this.loginLogId = loginLogId;
        this.logDao = new LogDAO();

        try {
            employeeMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void employeeMenu() throws IOException {

        while (true) {

            System.out.println();
            System.out.println("┌────────────────────────────────────");
            System.out.println("│        👨‍💼 사원 전용 화면");
            System.out.println("├────────────────────────────────────");
            System.out.println("│  1. 내 정보 조회");
            System.out.println("│  2. 근태 관리");
            System.out.println("│  3. 조직도 조회");
            System.out.println("│  4. 게시판");
            System.out.println("│  0. 로그아웃");
            System.out.println("└────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        System.out.println("내 정보 조회 기능 준비중");
                        break;

                    case 2:
                        new UserAttendanceMenu(br, userId, loginLogId);
                        break;

                    case 3:
                        System.out.println("조직도 조회 준비중");
                        break;

                    case 4:
                        new NoticeEmployee(br, userId, loginLogId);
                        break;

                    case 0:
                        if (loginLogId > 0) {
                            logDao.updateLogoutTime(loginLogId);
                        }
                        System.out.println("로그아웃합니다.");
                        return;

                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
            }
        }
    }
}
