package kr.attendance;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.AttendanceDAO;



public class AdminAttendanceMenu {

    private AttendanceDAO dao;
    private BufferedReader br;
    private int adminUserId;
    private int loginLogId;

    public AdminAttendanceMenu(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        this.adminUserId = adminUserId;
        this.loginLogId = loginLogId;
        this.dao = new AttendanceDAO();

        try {
            menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void menu() throws IOException {

        while (true) {
            try {
                System.out.println("\n==== 관리자 근태 관리 ====");
                System.out.println("1. 특정 사원 출근/퇴근 기록 조회");
                System.out.println("2. 특정 사원 월별 근태 조회");
                System.out.println("3. 특정 사원 월 총 근무시간 조회");
                System.out.println("4. 특정 사원 월 상태별 횟수 조회");
                System.out.println("5. 특정 사원 근태 수정");
                System.out.println("0. 뒤로가기");
                System.out.print("선택 >> ");

                int no = Integer.parseInt(br.readLine());
                int userId;
                String yearMonth;

                switch (no) {
                    case 1:
                        System.out.print("조회할 사원 USER_ID 입력: ");
                        userId = Integer.parseInt(br.readLine());
                        dao.selectAll(userId);
                        break;

                    case 2:
                        System.out.print("조회할 사원 USER_ID 입력: ");
                        userId = Integer.parseInt(br.readLine());
                        System.out.print("조회 월 입력 (예: 2026-02): ");
                        yearMonth = br.readLine();
                        dao.selectByMonth(userId, yearMonth);
                        break;

                    case 3:
                        System.out.print("조회할 사원 USER_ID 입력: ");
                        userId = Integer.parseInt(br.readLine());
                        System.out.print("조회 월 입력 (예: 2026-02): ");
                        yearMonth = br.readLine();
                        dao.selectMonthTotal(userId, yearMonth);
                        break;

                    case 4:
                        System.out.print("조회할 사원 USER_ID 입력: ");
                        userId = Integer.parseInt(br.readLine());
                        System.out.print("조회 월 입력 (예: 2026-02): ");
                        yearMonth = br.readLine();
                        dao.selectMonthStatusCount(userId, yearMonth);
                        break;

                    case 5:
                        System.out.print("수정할 사원 USER_ID 입력: ");
                        userId = Integer.parseInt(br.readLine());

                        System.out.print("수정할 날짜 입력 (예:2026-02-20): ");
                        String date = br.readLine();

                        System.out.print("출근 시간 입력 (예:08:50, 없으면 NULL): ");
                        String inTime = br.readLine();

                        System.out.print("퇴근 시간 입력 (예:17:00, 없으면 NULL): ");
                        String outTime = br.readLine();

                        // ✅ 관리자 행위자로 로그 남김
                        dao.updateAttendance(userId, date, inTime, outTime, adminUserId, loginLogId);
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
}
