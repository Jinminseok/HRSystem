package kr.attendance;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.AttendanceDAO;


public class UserAttendanceMenu {

    private int userId;
    private int loginLogId;
    private AttendanceDAO dao;
    private BufferedReader br;

    // ✅ Shared BufferedReader 사용 (Stream closed 방지)
    public UserAttendanceMenu(BufferedReader br, int userId, int loginLogId) {
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

    private void menu() throws IOException {

        while (true) {
            try {
                System.out.println("\n==== 근태 관리 ====");
                System.out.println("1. 출근하기");
                System.out.println("2. 퇴근하기");
                System.out.println("3. 전체 조회");
                System.out.println("4. 월별 조회");
                System.out.println("5. 월 총 근무시간");
                System.out.println("6. 월 특정 횟수");
                System.out.println("0. 뒤로가기");
                System.out.print("선택 >> ");

                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        dao.checkIn(userId, loginLogId);
                        break;
                    case 2:
                        dao.checkOut(userId, loginLogId);
                        break;
                    case 3:
                        dao.selectAll(userId);
                        break;
                    case 4:
                        System.out.print("조회 월 입력 (예: 2026-02) : ");
                        dao.selectByMonth(userId, br.readLine());
                        break;
                    case 5:
                        System.out.print("조회 월 입력 (예: 2026-02) : ");
                        dao.selectMonthTotal(userId, br.readLine());
                        break;
                    case 6:
                        System.out.print("조회 월 입력 (예: 2026-02) : ");
                        dao.selectMonthStatusCount(userId, br.readLine());
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
