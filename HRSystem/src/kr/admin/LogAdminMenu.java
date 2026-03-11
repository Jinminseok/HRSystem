package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.LogDAO;

public class LogAdminMenu {

    private BufferedReader br;
    private int userId;
    private Integer loginLogId;
    private LogDAO dao;

    public LogAdminMenu(BufferedReader br, int userId, Integer loginLogId) {
        this.br = br;
        this.userId = userId;
        this.loginLogId = loginLogId;
        this.dao = new LogDAO();

        try {
            menu(); 
        } catch (Exception e) {
            System.out.println("❌ 로그 메뉴 실행 중 오류가 발생했습니다.");
        }
    }

    private void menu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+───────────────────────────────+");
            System.out.println("│       📜 로그 기록 조회       │");
            System.out.println("+───────────────────────────────+");
            System.out.println("│ [1] 중요 로그 요약            │");
            System.out.println("│ [2] 로그인 로그 조회          │");
            System.out.println("│ [3] 행동 로그 조회            │");
            System.out.println("│ [0] 뒤로가기                  │");
            System.out.println("+───────────────────────────────+");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        dao.selectTodayImportantSummary();
                        break;

                    case 2:
                        loginLogMenu();
                        break;

                    case 3:
                        actionLogMenu();
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

    private void loginLogMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────+");
            System.out.println("│        🔐 로그인 로그 조회       │");
            System.out.println("+──────────────────────────────────+");
            System.out.println("│ [1] 전체 로그인 로그             │");
            System.out.println("│ [2] 최근 로그인 로그             │");
            System.out.println("│ [0] 뒤로가기                     │");
            System.out.println("+──────────────────────────────────+");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        dao.selectLoginHistory();
                        break;

                    case 2: {
                        Integer limit = readLimitWithBack("조회할 건수 입력 (예: 10 / 뒤로가기: 0): ");
                        if (limit == null) break;

                        dao.selectImportantLoginHistory(limit);
                        break;
                    }

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

    private void actionLogMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+────────────────────────────────────+");
            System.out.println("│         📋 행동 로그 조회          │");
            System.out.println("+────────────────────────────────────+");
            System.out.println("│ [1] 전체 행동 로그                 │");
            System.out.println("│ [2] 중요 행동 로그                 │");
            System.out.println("│ [3] 최근 근태 로그                 │");
            System.out.println("│ [4] 최근 게시판/투표 로그          │");
            System.out.println("│ [0] 뒤로가기                       │");
            System.out.println("+────────────────────────────────────+");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        dao.selectActionLog();
                        break;

                    case 2: {
                        Integer limit = readLimitWithBack("조회할 건수 입력 (예: 10 / 뒤로가기: 0): ");
                        if (limit == null) break;

                        dao.selectImportantActionLog(limit);
                        break;
                    }

                    case 3: {
                        Integer limit = readLimitWithBack("조회할 건수 입력 (예: 10 / 뒤로가기: 0): ");
                        if (limit == null) break;

                        dao.selectImportantAttendanceActionLog(limit);
                        break;
                    }

                    case 4: {
                        Integer limit = readLimitWithBack("조회할 건수 입력 (예: 10 / 뒤로가기: 0): ");
                        if (limit == null) break;

                        dao.selectImportantBoardActionLog(limit);
                        break;
                    }

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

    private Integer readLimitWithBack(String message) throws IOException {
        System.out.print(message);
        String input = br.readLine();

        if (input == null) return null;

        input = input.trim();

        if ("0".equals(input)) {
            
            return null;
        }

        try {
            int limit = Integer.parseInt(input);

            if (limit <= 0) {
                System.out.println("1 이상의 숫자를 입력하세요.");
                return null;
            }

            return limit;
        } catch (NumberFormatException e) {
            System.out.println("숫자만 입력하세요.");
            return null;
        }
    }
}