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

// 관리자 화면에서 공통적으로 사용하는 변수들  
public class AdminScreen {

    private BufferedReader br;//사용자 입력 받는 객체, 관리자가 메뉴 입력할 때 사용
    private int userId; // 현재 로그인한 관리자 ID (로그 기록, 사원승인처리, 행동로그)에 사용
    private int loginLogId; // 현재 로그인 세션의 로그 ID (로그아웃할 때 사용)
    private LogDAO logDao; // 관리자 행동 로그, 로그인 로그 기록 담당
   
//생성자 - 관리자가 로그인 성공하면, ShareScreen에서 이 클래스 실행
    public AdminScreen(BufferedReader br, int userId, Integer loginLogId) {
        this.br = br;// 로그인 화면에서 사용하던 입력 객체를 그대로 사용
        this.userId = userId;// 현재 로그인한 관리자 ID 저장
        this.loginLogId = (loginLogId == null) ? -1 : loginLogId;//로그인 로그 ID 저장(null이면 -1로 바꿈)
        this.logDao = new LogDAO();// 로그 DAO 생성

        try {
            adminScreenMenu();// 관리자 메인 메뉴 실행
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//관리자 메뉴 출력 - 관리자가 할 수 있는 기능들 보여줌
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
                int no = Integer.parseInt(br.readLine());// 관리자가 입력한 숫자 읽는다.

                switch (no) {// 입력한 숫자에 따라 다른 관리자 기능 실행
                    case 1: // 사원관리
                        new UserManage_Admin(br, userId, loginLogId);
                        break;

                    case 2:
                        // 부서관리 메뉴 클래스 연결 (아직 없으면 아래 안내처럼 먼저 생성)
                    	new Dept_Admin(br, userId, loginLogId);
                        break;

                    case 3:
                        // 직급관리
                    	new Position_Admin(br, userId, loginLogId);
                        break;

                    case 4: // 급여관리
                    	new Salary_Admin(br, userId, loginLogId);
                        break;

                    case 5: // 인사발령관리
                    	new HrAppointmentHistory_Admin(br, userId, loginLogId);
                        break;

                    case 6: // 근태조회
                       new Attendance_Admin(br, userId, loginLogId);
                        break;

                    case 7: //게시판 관리
                        new NoticeAdmin(br, userId, loginLogId);
                        break;

                    case 8: //검색
                        new Search_Admin(br, userId, loginLogId);
                        break;

                    case 9: //통계
                        new Stats_Admin(br, userId, loginLogId);
                        break;

                    case 10: // 로그 기록 조회
                        new LogAdminMenu(br, userId, loginLogId);
                        break;

                    case 0: // 로그 아웃
                        if (loginLogId > 0) {
                            logDao.updateLogoutTime(loginLogId); // 로그아웃 시간 기록
                        }
                        System.out.println("로그아웃합니다.");
                        return; //관리자 화면 종료 (AdminScreen 종료)

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
