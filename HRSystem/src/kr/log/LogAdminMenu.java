package kr.log;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.LogDAO;


public class LogAdminMenu {

    private BufferedReader br;
    private int userId;
    private Integer loginLogId;  // ✅ int -> Integer
    private LogDAO dao;

    public LogAdminMenu(BufferedReader br, int userId, Integer loginLogId) { // ✅ int -> Integer
        this.br = br;
        this.userId = userId;
        this.loginLogId = loginLogId;
        this.dao = new LogDAO();

        try {
            menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void menu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("┌──────────────────────────────");
            System.out.println("│        로그 기록 조회         ");
            System.out.println("├──────────────────────────────");
            System.out.println("│ [1] 중요 로그 요약            ");
            System.out.println("│ [2] 로그인 로그 조회          ");
            System.out.println("│ [3] 행동 로그 조회            ");
            System.out.println("│ [0] 뒤로가기                 ");
            System.out.println("└──────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        dao.selectTodayImportantSummary();
                        break;
                    case 2:
                        dao.selectLoginHistory();
                        break;
                    case 3:
                        dao.selectActionLog();
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
