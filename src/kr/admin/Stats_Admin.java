package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.PositionDAO;
import kr.hrsystem.dao.StatsDAO;
import kr.hrsystem.dao.salary_DAO;

public class Stats_Admin {

    private StatsDAO sDao = new StatsDAO();
    private salary_DAO salDao = new salary_DAO();
    private PositionDAO positionDao = new PositionDAO();
    private BufferedReader br;

    public Stats_Admin(BufferedReader br, int userId, int loginLogId) {
        this.br = br;
        runStatsMenu();
    }
 
    public void runStatsMenu() {
        while (true) {
            try {
                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│           📈기업 통합 통계 서비스        │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 인사 통계                           │");
                System.out.println("│  [2] 근태 통계                           │");
                System.out.println("│  [3] 급여 통계                           │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                String input = br.readLine();
                if (input == null || "0".equals(input.trim())) {
                    return;
                }

                switch (input.trim()) {
                    case "1":
                        showWorkStatusMenu();
                        break;
                    case "2":
                        showAttendanceMenu();
                        break;
                    case "3":
                        showSalaryMenu();
                        break;
                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (Exception e) {
                System.out.println("⚠️ 잘못된 입력입니다.");
            }
        }
    }

    // ==========================
    // 인사 통계
    // ==========================
    private void showWorkStatusMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│               👥인사 통계                │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 전체 인사 통계                      │");
            System.out.println("│  [2] 부서별 인사 통계                    │");
            System.out.println("│  [3] 직급별 인사 통계                    │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 >> ");

            String input = br.readLine();
            if (input == null || "0".equals(input.trim())) {
                return;
            }

            int type;
            try {
                type = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
                continue;
            }

            if (type < 1 || type > 3) {
                System.out.println("잘못 입력했습니다.");
                continue;
            }

            String typeVal = "";

            if (type == 2) {
                while (true) {
                    salDao.showDepartmentList();

                    System.out.print("부서명 입력 (뒤로가기: 0) >> ");
                    typeVal = br.readLine().trim();

                    if ("0".equals(typeVal)) {
                        typeVal = "";
                        break;
                    }

                    if (!sDao.existsDeptName(typeVal)) {
                        System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                        continue;
                    }
                    break;
                }

                if ("".equals(typeVal)) {
                    continue;
                }
            } else if (type == 3) {
                while (true) {
                    positionDao.selectPosition();

                    System.out.print("직급명 입력 (뒤로가기: 0) >> ");
                    typeVal = br.readLine().trim();

                    if ("0".equals(typeVal)) {
                        typeVal = "";
                        break;
                    }

                    if (!sDao.existsPositionName(typeVal)) {
                        System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                        continue;
                    }
                    break;
                }

                if ("".equals(typeVal)) {
                    continue;
                }
            }

            while (true) {
                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│             🔎인사 통계 조회             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 전체 조회                           │");
                System.out.println("│  [2] 월별 조회                           │");
                System.out.println("│  [3] 연도별 조회                         │");
                System.out.println("│  [4] 기간별 조회                         │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                input = br.readLine();
                if (input == null) {
                    return;
                }
                if ("0".equals(input.trim())) {
                    break;
                }

                int dateType;
                try {
                    dateType = Integer.parseInt(input.trim());
                } catch (NumberFormatException e) {
                    System.out.println("숫자만 입력하세요.");
                    continue;
                }

                if (dateType < 1 || dateType > 4) {
                    System.out.println("잘못 입력했습니다.");
                    continue;
                }

                String s = "";
                String e = "";

                if (dateType == 2) {
                    System.out.print("월 입력 (YYYY-MM / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;
                } else if (dateType == 3) {
                    System.out.print("연도 입력 (YYYY / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;
                } else if (dateType == 4) {
                    System.out.print("시작일 입력 (YYYY-MM-DD / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;

                    System.out.print("종료일 입력 (YYYY-MM-DD / 뒤로가기: 0) >> ");
                    e = br.readLine().trim();
                    if ("0".equals(e)) continue;
                }

                sDao.showWorkStatusStats(type, typeVal, dateType, s, e);
                // waitBack() 제거
                // 조회 후 바로 현재 목록(인사 통계 조회 메뉴) 다시 출력됨
            }
        }
    }

    // ==========================
    // 근태 통계
    // ==========================
    private void showAttendanceMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│             ⏰근태 통계 검색             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 전체 검색                           │");
            System.out.println("│  [2] 부서 검색                           │");
            System.out.println("│  [3] 직급 검색                           │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 >> ");

            String input = br.readLine();
            if (input == null || "0".equals(input.trim())) {
                return;
            }

            int type;
            try {
                type = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
                continue;
            }

            if (type < 1 || type > 3) {
                System.out.println("잘못 입력했습니다.");
                continue;
            }

            String typeVal = "";

            if (type == 2) {
                while (true) {
                    salDao.showDepartmentList();

                    System.out.print("부서명 입력 (뒤로가기: 0) >> ");
                    typeVal = br.readLine().trim();

                    if ("0".equals(typeVal)) {
                        typeVal = "";
                        break;
                    }

                    if (!sDao.existsDeptName(typeVal)) {
                        System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                        continue;
                    }
                    break;
                }

                if ("".equals(typeVal)) {
                    continue;
                }
            } else if (type == 3) {
                while (true) {
                    positionDao.selectPosition();

                    System.out.print("직급명 입력 (뒤로가기: 0) >> ");
                    typeVal = br.readLine().trim();

                    if ("0".equals(typeVal)) {
                        typeVal = "";
                        break;
                    }

                    if (!sDao.existsPositionName(typeVal)) {
                        System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                        continue;
                    }
                    break;
                }

                if ("".equals(typeVal)) {
                    continue;
                }
            }

            while (true) {
                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│             🔎근태 통계 조회             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 전체 조회                           │");
                System.out.println("│  [2] 일별 조회                           │");
                System.out.println("│  [3] 월별 조회                           │");
                System.out.println("│  [4] 연도별 조회                         │");
                System.out.println("│  [5] 기간별 조회                         │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                input = br.readLine();
                if (input == null) {
                    return;
                }
                if ("0".equals(input.trim())) {
                    break;
                }

                int dateType;
                try {
                    dateType = Integer.parseInt(input.trim());
                } catch (NumberFormatException e) {
                    System.out.println("숫자만 입력하세요.");
                    continue;
                }

                if (dateType < 1 || dateType > 5) {
                    System.out.println("잘못 입력했습니다.");
                    continue;
                }

                String s = "";
                String e = "";

                if (dateType == 2) {
                    System.out.print("날짜 입력 (YYYY-MM-DD / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;
                } else if (dateType == 3) {
                    System.out.print("월 입력 (YYYY-MM / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;
                } else if (dateType == 4) {
                    System.out.print("연도 입력 (YYYY / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;
                } else if (dateType == 5) {
                    System.out.print("시작일 입력 (YYYY-MM-DD / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;

                    System.out.print("종료일 입력 (YYYY-MM-DD / 뒤로가기: 0) >> ");
                    e = br.readLine().trim();
                    if ("0".equals(e)) continue;
                }

                sDao.showAttendanceStats(type, typeVal, dateType, s, e);
                // waitBack() 제거
            }
        }
    }

    // ==========================
    // 급여 통계
    // ==========================
    private void showSalaryMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│             💰급여 통계 검색             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 전체 검색                           │");
            System.out.println("│  [2] 부서 검색                           │");
            System.out.println("│  [3] 직급 검색                           │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 >> ");

            String input = br.readLine();
            if (input == null || "0".equals(input.trim())) {
                return;
            }

            int type;
            try {
                type = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
                continue;
            }

            if (type < 1 || type > 3) {
                System.out.println("잘못 입력했습니다.");
                continue;
            }

            String typeVal = "";

            if (type == 2) {
                while (true) {
                    salDao.showDepartmentList();

                    System.out.print("부서명 입력 (뒤로가기: 0) >> ");
                    typeVal = br.readLine().trim();

                    if ("0".equals(typeVal)) {
                        typeVal = "";
                        break;
                    }

                    if (!sDao.existsDeptName(typeVal)) {
                        System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                        continue;
                    }
                    break;
                }

                if ("".equals(typeVal)) {
                    continue;
                }
            } else if (type == 3) {
                while (true) {
                    positionDao.selectPosition();

                    System.out.print("직급명 입력 (뒤로가기: 0) >> ");
                    typeVal = br.readLine().trim();

                    if ("0".equals(typeVal)) {
                        typeVal = "";
                        break;
                    }

                    if (!sDao.existsPositionName(typeVal)) {
                        System.out.println("❌ 잘못 입력했습니다. 다시 입력해주세요.");
                        continue;
                    }
                    break;
                }

                if ("".equals(typeVal)) {
                    continue;
                }
            }

            while (true) {
                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│            🔎 급여 통계 조회             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 전체 조회                           │");
                System.out.println("│  [2] 월별 조회                           │");
                System.out.println("│  [3] 연도별 조회                         │");
                System.out.println("│  [4] 기간별 조회                         │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                input = br.readLine();
                if (input == null) {
                    return;
                }
                if ("0".equals(input.trim())) {
                    break;
                }

                int dateType;
                try {
                    dateType = Integer.parseInt(input.trim());
                } catch (NumberFormatException e) {
                    System.out.println("숫자만 입력하세요.");
                    continue;
                }

                if (dateType < 1 || dateType > 4) {
                    System.out.println("잘못 입력했습니다.");
                    continue;
                }

                String s = "";
                String e = "";

                if (dateType == 2) {
                    System.out.print("월 입력 (YYYY-MM / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;
                } else if (dateType == 3) {
                    System.out.print("연도 입력 (YYYY / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;
                } else if (dateType == 4) {
                    System.out.print("시작일 입력 (YYYY-MM-DD / 뒤로가기: 0) >> ");
                    s = br.readLine().trim();
                    if ("0".equals(s)) continue;

                    System.out.print("종료일 입력 (YYYY-MM-DD / 뒤로가기: 0) >> ");
                    e = br.readLine().trim();
                    if ("0".equals(e)) continue;
                }

                sDao.showSalaryStats(type, typeVal, dateType, s, e);
                // waitBack() 제거
            }
        }
    }
}