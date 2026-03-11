package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;

import kr.employee.MyInfoUpdate_employee;
import kr.employee.Notice_Employee;
import kr.hrsystem.dao.LogDAO;
import kr.hrsystem.dao.MyInfoDAO;
import kr.employee.OrgChart_Employee;
import kr.employee.Attendance_Employee;

public class EmployeeScreen {

    private BufferedReader br;
    private int userId;
    private int loginLogId;
    private LogDAO logDao;
    private MyInfoDAO myInfoDao;
    
    // 사원 화면에 필요한 값들을 전달받아 메뉴 실행
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

    // 사원 전용 메뉴 화면
    private void employeeMenu() throws IOException {

        while (true) {

        	System.out.println();
        	System.out.println("+──────────────────────────────────────────+");
        	System.out.println("│              사원 전용 화면              │");
        	System.out.println("+──────────────────────────────────────────+");
        	System.out.println("│  [1] 내 정보 조회                        │");
        	System.out.println("│  [2] 내 정보 수정                        │");
        	System.out.println("│  [3] 근태 관리                           │");
        	System.out.println("│  [4] 조직도 조회                         │");
        	System.out.println("│  [5] 게시판                              │");
        	System.out.println("│  [0] 로그아웃                            │");
        	System.out.println("+──────────────────────────────────────────+");
        	System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                    	// 로그인한 사용자의 내 정보 조회
                    	myInfoDao.selectMyInfo(userId);
                        break;
                        
                    case 2:
                    	// 내 정보 수정 화면으로 이동
                    	new MyInfoUpdate_employee(br, userId, loginLogId);
                        break;

                    case 3:
                    	// 근태 관리 화면으로 이동
                        new Attendance_Employee(br, userId, loginLogId);
                        break;

                    case 4:
                    	// 조직도 조회 화면으로 이동
                    	new OrgChart_Employee(br);
                    	break;

                    case 5:
                    	// 게시판 화면으로 이동
                        new Notice_Employee(br, userId, loginLogId);
                        break;

                    case 0:
                    	// 로그아웃 시간 저장 후 종료
                        if (loginLogId > 0) {
                            logDao.updateLogoutTime(loginLogId);
                        }
                        System.out.println("로그아웃합니다.");
                        return;

                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (NumberFormatException e) {
            	// 숫자가 아닌 값 입력 시 예외 처리
                System.out.println("숫자만 입력하세요.");
            }
        }
    }
}