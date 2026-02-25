package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;

import kr.attendance.UserAttendanceMenu;
import kr.employee.MyInfoUpdateMenu;
import kr.hrsystem.dao.LogDAO;
import kr.hrsystem.dao.MyInfoDAO;
import kr.notice.NoticeEmployee;
import kr.employee.OrgChartMenu;


public class EmployeeScreen {

    private BufferedReader br;
    private int userId;
    private int loginLogId;
    private LogDAO logDao;
    private MyInfoDAO myInfoDao;

    public EmployeeScreen(BufferedReader br, int userId, int loginLogId) {
        this.br = br;
        this.userId = userId;
        this.loginLogId = loginLogId;
        this.logDao = new LogDAO();
        this.myInfoDao = new MyInfoDAO();

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
            System.out.println("│  2. 내 정보 수정");
            System.out.println("│  3. 근태 관리");
            System.out.println("│  4. 조직도 조회");
            System.out.println("│  5. 게시판");
            System.out.println("│  0. 로그아웃");
            System.out.println("└────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                    	myInfoDao.selectMyInfo(userId);
                        break;
                        
                    case 2:
                    	new MyInfoUpdateMenu(br, userId, loginLogId);
                        break;
                    case 3:
                        new UserAttendanceMenu(br, userId, loginLogId);
                        break;

                    case 4:
                    	 new OrgChartMenu(br);
                    	 break;

                    case 5:
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
