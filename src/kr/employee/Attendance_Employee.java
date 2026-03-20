package kr.employee;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.AttendanceDAO;

public class Attendance_Employee {

    private int userId;
    private int loginLogId;
    private AttendanceDAO dao;
    private BufferedReader br;

    // 사원 근태관리 화면에 필요한 값들을 전달받아 메뉴 실행
    public Attendance_Employee(BufferedReader br, int userId, int loginLogId) {
        this.br = br;
        this.userId = userId;
        this.loginLogId = loginLogId;
        this.dao = new AttendanceDAO();
 
        try {
            menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 근태 관리 메뉴 화면
    private void menu() throws IOException {

        while (true) {
            try {
            	System.out.println("+──────────────────────────────────────────+"); 	
            	System.out.println("│               근태 관리                  │");
    			System.out.println("+──────────────────────────────────────────+");
    			System.out.println("│  [1] 출근하기                            │");
    			System.out.println("│  [2] 퇴근하기                            │");
    			System.out.println("│  [3] 전체조회                            │");
    			System.out.println("│  [4] 월별 조회                           │");
    			System.out.println("│  [5] 월 총 근무시간                      │");
    			System.out.println("│  [6] 월 근무 횟수                        │");
    			System.out.println("│  [0] 뒤로가기                            │");
    			System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 : ");

                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                    	// 출근 처리
                        dao.checkIn(userId, loginLogId);
                        break;
                    case 2:
                    	// 퇴근 처리
                        dao.checkOut(userId, loginLogId);
                        break;
                    case 3:
                    	// 전체 근태 조회
                        dao.selectAll(userId);
                        break;
                    case 4:
                    	// 입력한 월의 근태 조회
                        System.out.print("조회 월 입력 (예: 2026-02) : ");
                        String ym4 = br.readLine().trim();

                        if (!isValidYearMonth(ym4)) {
                            System.out.println("👉 해당 월이 없습니다. (예: 2026-02)");
                            System.out.println();
                            break;
                        }
                        dao.selectByMonth(userId, ym4);
                        break;

                    case 5:
                    	// 입력한 월의 총 근무시간 조회
                        System.out.print("조회 월 입력 (예: 2026-02) : ");
                        String ym5 = br.readLine().trim();

                        if (!isValidYearMonth(ym5)) {
                            System.out.println("👉 해당 월이 없습니다. (예: 2026-02)");
                            System.out.println();
                            break;
                        }
                        dao.selectMonthTotal(userId, ym5);
                        break;

                    case 6:
                    	// 입력한 월의 근무 횟수 조회
                        System.out.print("조회 월 입력 (예: 2026-02) : ");
                        String ym6 = br.readLine().trim();

                        if (!isValidYearMonth(ym6)) {
                            System.out.println("👉 해당 월이 없습니다. (예: 2026-02)");
                            System.out.println();
                            break;
                        }
                        dao.selectMonthStatusCount(userId, ym6);
                        break;

                    case 0:
                    	// 이전 화면으로 이동
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

    // YYYY-MM 형식의 월 입력값인지 확인
    private boolean isValidYearMonth(String ym) {
        if (ym == null) return false;
        ym = ym.trim();
        return ym.matches("^(\\d{4})-(0[1-9]|1[0-2])$");
    }
}