package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.admin.Search_Admin;
import kr.admin.Stats_Admin;
import kr.admin.UserManage_Admin;
import kr.admin.Attendance_Admin;
import kr.admin.Dept_Admin;
import kr.admin.HrAppointmentHistory_Admin;
import kr.admin.LogAdminMenu;
import kr.admin.NoticeAdmin;
import kr.admin.Position_Admin;
import kr.admin.Salary_Admin;
import kr.hrsystem.dao.LogDAO;

 
public class AdminScreen {

    private BufferedReader br;
    private int userId;
    private int loginLogId;
    private LogDAO logDao;
   

    public AdminScreen(BufferedReader br, int userId, Integer loginLogId) {
        this.br = br;
        this.userId = userId;
        this.loginLogId = (loginLogId == null) ? -1 : loginLogId;
        this.logDao = new LogDAO();

        try {
            adminScreenMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adminScreenMenu() throws IOException {

        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│           👑 관리자 전용 화면            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 사원관리                            │");
            System.out.println("│  [2] 부서관리                            │");
            System.out.println("│  [3] 직급관리                            │");
            System.out.println("│  [4] 급여관리                            │");
            System.out.println("│  [5] 인사발령관리                        │");
            System.out.println("│  [6] 근태조회                            │");
            System.out.println("│  [7] 게시판 관리                         │");
            System.out.println("│  [8] 검색                                │");
            System.out.println("│  [9] 통계                                │");
            System.out.println("│ [10] 로그 기록 조회                      │");
            System.out.println("│  [0] 로그아웃                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 > ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        new UserManage_Admin(br, userId, loginLogId);
                        break;

                    case 2:
                        // ✅ 부서관리 메뉴 클래스 연결 (아직 없으면 아래 안내처럼 먼저 생성)
                    	new Dept_Admin(br, userId, loginLogId);
                        break;

                    case 3:
                        // ✅ 직급관리 메뉴는 여기
                    	new Position_Admin(br, userId, loginLogId);
                        break;

                    case 4:
                    	new Salary_Admin(br, userId, loginLogId);
                        break;

                    case 5:
                    	new HrAppointmentHistory_Admin(br, userId, loginLogId);
                        break;

                    case 6:
                       new Attendance_Admin(br, userId, loginLogId);
                        break;

                    case 7:
                        new NoticeAdmin(br, userId, loginLogId);
                        break;

                    case 8:
                        new Search_Admin(br, userId, loginLogId);
                        break;

                    case 9:
                        new Stats_Admin(br, userId, loginLogId);
                        break;

                    case 10:
                        new LogAdminMenu(br, userId, loginLogId);
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

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        new AdminScreen(br, 1, null);
    }
}
