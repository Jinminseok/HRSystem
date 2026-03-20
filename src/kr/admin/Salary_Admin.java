package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import kr.hrsystem.dao.salary_DAO;

public class Salary_Admin {

    private salary_DAO dao = new salary_DAO();
    private BufferedReader br;
    private int adminUserId;
    private int loginLogId;

    public Salary_Admin(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        this.adminUserId = adminUserId;
        this.loginLogId = loginLogId;
        runSalaryMenu();
    }

    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(String.valueOf(obj));
    }

    // ==========================
    // 공통 출력 유틸
    // ==========================
    private void printDivider(int length) {
        System.out.println("=".repeat(length));
    }

    private boolean isWide(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    private String pad(String text, int width) {
        if (text == null || text.trim().isEmpty()) {
            text = "-";
        }

        StringBuilder sb = new StringBuilder();
        int len = 0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            int charWidth = isWide(ch) ? 2 : 1;

            if (len + charWidth > width) {
                break;
            }

            sb.append(ch);
            len += charWidth;
        }

        while (len < width) {
            sb.append(' ');
            len++;
        }

        return sb.toString();
    }

    private int displayWidth(String s) {
        if (s == null || s.isEmpty()) return 0;

        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            len += isWide(s.charAt(i)) ? 2 : 1;
        }
        return len;
    }

    private void printBoxLine(int width) {
        System.out.println("+" + "─".repeat(width) + "+");
    }

    private void printBoxCenter(String text, int width) {
        int textWidth = displayWidth(text);
        int left = Math.max(0, (width - textWidth) / 2);
        int right = Math.max(0, width - textWidth - left);

        System.out.println("│" + " ".repeat(left) + text + " ".repeat(right) + "│");
    }

    private void printBoxRow(String label, String value, int width) {
        String leftText = "  " + label;
        String rightText = value == null ? "-" : value;

        int leftWidth = displayWidth(leftText);
        int rightWidth = displayWidth(rightText);
        int middleSpaces = Math.max(1, width - leftWidth - rightWidth);

        System.out.println("│" + leftText + " ".repeat(middleSpaces) + rightText + "│");
    }

    // ==========================
    // 메인 메뉴
    // ==========================
    public void runSalaryMenu() {
        while (true) {
            try {
                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│         💰 급여 상세 관리 시스템         │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 수당 미등록 관리                    │");
                System.out.println("│  [2] 월별 총급여 조회                    │");
                System.out.println("│  [3] 수당 내역 관리                      │");
                System.out.println("│  [4] 총급여 지급 여부 관리               │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                String input = br.readLine();
                if (input == null) return;

                input = input.trim();

                if ("0".equals(input)) {
                    return;
                }

                switch (input) {
                    case "1":
                        showUnpaidMenu();
                        break;
                    case "2":
                        showSummaryMenu();
                        break;
                    case "3":
                        manageSalaryMenu();
                        break;
                    case "4":
                        new Payment_Admin(br, adminUserId, loginLogId);
                        break;
                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (Exception e) {
                System.out.println("숫자만 입력하세요.");
            }
        }
    }

    // ==========================
    // [1] 수당 미등록 관리
    // ==========================
    private void showUnpaidMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│          📝 수당 미등록 관리             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 부서별 조회                         │");
            System.out.println("│  [2] 전체 조회                           │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 >> ");

            String input = br.readLine();
            if (input == null) return;

            input = input.trim();

            if ("0".equals(input)) {
                return;
            }

            if (!"1".equals(input) && !"2".equals(input)) {
                System.out.println("잘못 입력했습니다.");
                continue;
            }

            int type = Integer.parseInt(input);
            int dNum = -1;

            if (type == 1) {
                while (true) {
                    dao.showDepartmentList();

                    Integer deptNum = readIntWithBack("부서번호 입력 (뒤로가기: 0): ");
                    if (deptNum == null) {
                        dNum = -2;
                        break;
                    }

                    if (!dao.checkDeptExists(deptNum)) {
                        System.out.println("❌ 존재하지 않는 부서입니다. 다시 입력해주세요.");
                        continue;
                    }

                    dNum = deptNum;
                    break;
                }

                if (dNum == -2) {
                    continue;
                }
            }

            while (true) {
                dao.showUnpaidWorkers(dNum);

                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│          📝 수당 미등록 관리             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 개별 등록                           │");
                System.out.println("│  [2] 일괄 등록                           │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                input = br.readLine();
                if (input == null) return;

                input = input.trim();

                if ("0".equals(input)) {
                    break;
                }

                if (!"1".equals(input) && !"2".equals(input)) {
                    System.out.println("잘못 입력했습니다.");
                    continue;
                }

                if ("1".equals(input)) {
                    while (true) {
                        Integer uid = readIntWithBack("사번 입력 (뒤로가기: 0): ");
                        if (uid == null) {
                            break;
                        }

                        if (!dao.showAttendanceList(uid)) {
                            System.out.println("❌ 존재하지 않는 사번이거나 미등록 내역이 없습니다.");
                            continue;
                        }

                        boolean success = false;

                        while (true) {
                            Integer aid = readIntWithBack("근태ID 입력 (뒤로가기: 0): ");
                            if (aid == null) {
                                break;
                            }

                            if (dao.insertSalary(uid, aid) > 0) {
                                System.out.println("✅ 등록 완료!");
                                success = true;
                                break;
                            } else {
                                System.out.println("❌ 존재하지 않는 근태ID입니다. 다시 입력해주세요.");
                            }
                        }

                        if (success) {
                            break;
                        }
                    }

                } else {
                    dao.insertSalaryBatch(dNum);
                    System.out.println("✅ 일괄 등록 완료!");
                }
            }
        }
    }

    // ==========================
    // [2] 월별 총급여 조회
    // ==========================
    private void showSummaryMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│          📊 월별 총급여 조회             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 부서별 조회                         │");
            System.out.println("│  [2] 전체 조회                           │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 >> ");

            String input = br.readLine();
            if (input == null) return;

            input = input.trim();

            if ("0".equals(input)) {
                return;
            }

            if (!"1".equals(input) && !"2".equals(input)) {
                System.out.println("잘못 입력했습니다.");
                continue;
            }

            int dNum = -1;

            if ("1".equals(input)) {
                while (true) {
                    dao.showDepartmentList();

                    Integer deptNum = readIntWithBack("부서번호 입력 (뒤로가기: 0): ");
                    if (deptNum == null) {
                        dNum = -2;
                        break;
                    }

                    if (!dao.checkDeptExists(deptNum)) {
                        System.out.println("❌ 존재하지 않는 부서입니다. 다시 입력해주세요.");
                        continue;
                    }

                    dNum = deptNum;
                    break;
                }

                if (dNum == -2) {
                    continue;
                }
            }

            while (true) {
                dao.showUserListByDept(dNum);

                Integer uid = readIntWithBack("사번 입력 (뒤로가기: 0): ");
                if (uid == null) {
                    break;
                }

                if (!dao.checkUserExists(uid)) {
                    System.out.println("❌ 존재하지 않는 사번입니다. 다시 입력해주세요.");
                    continue;
                }

                while (true) {
                    String month = readLineWithBack("조회 월 입력 (YYYY-MM / 뒤로가기: 0): ");
                    if (month == null) {
                        uid = -2;
                        break;
                    }

                    if (!month.matches("\\d{4}-\\d{2}")) {
                        System.out.println("❌ 월 형식이 잘못되었습니다. 예: 2026-03");
                        continue;
                    }

                    Map<String, Object> s = dao.selectMonthlySummary(uid, month);

                    if (s != null && s.get("userName") != null) {
                        int salBase = toInt(s.get("salBase"));
                        int salHoliday = toInt(s.get("salHoliday"));
                        int salOvertime = toInt(s.get("salOvertime"));
                        int salTax = toInt(s.get("salTax"));
                        int salTotal = toInt(s.get("salTotal"));

                        int boxWidth = 42;

                        System.out.println();
                        printBoxLine(boxWidth);
                        printBoxCenter("📄 월별 급여 명세 조회", boxWidth);
                        printBoxLine(boxWidth);
                        printBoxRow("조회월", month, boxWidth);
                        printBoxRow("사원명", String.valueOf(s.get("userName")), boxWidth);
                        printBoxLine(boxWidth);
                        printBoxRow("기본급", String.format("%,d원", salBase), boxWidth);
                        printBoxRow("휴일수당", String.format("%,d원", salHoliday), boxWidth);
                        printBoxRow("야근수당", String.format("%,d원", salOvertime), boxWidth);
                        printBoxRow("공제세금", String.format("%,d원", salTax), boxWidth);
                        printBoxLine(boxWidth);
                        printBoxRow("실수령액", String.format("%,d원", salTotal), boxWidth);
                        printBoxLine(boxWidth);
                        break;
                    } else {
                        System.out.println("❌ 급여 내역이 없습니다.");
                    }
                }

                if (uid == -2) {
                    continue;
                }

                String next = readLineWithBack("계속하려면 엔터 / 뒤로가기: 0 >> ");
                if (next == null) {
                    break;
                }
            }
        }
    }

    // ==========================
    // [3] 수당 내역 관리
    // ==========================
    private void manageSalaryMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│            🧾 수당 내역 관리             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 부서별 조회                         │");
            System.out.println("│  [2] 전체 조회                           │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 >> ");

            String input = br.readLine();
            if (input == null) return;

            input = input.trim();

            if ("0".equals(input)) {
                return;
            }

            if (!"1".equals(input) && !"2".equals(input)) {
                System.out.println("잘못 입력했습니다.");
                continue;
            }

            int dNum = -1;

            if ("1".equals(input)) {
                while (true) {
                    dao.showDepartmentList();

                    Integer deptNum = readIntWithBack("부서번호 입력 (뒤로가기: 0): ");
                    if (deptNum == null) {
                        dNum = -2;
                        break;
                    }

                    if (!dao.checkDeptExists(deptNum)) {
                        System.out.println("❌ 존재하지 않는 부서입니다. 다시 입력해주세요.");
                        continue;
                    }

                    dNum = deptNum;
                    break;
                }

                if (dNum == -2) {
                    continue;
                }
            }

            while (true) {
                List<Map<String, Object>> list = dao.selectSalaryList(dNum);

                if (list.isEmpty()) {
                    System.out.println("❌ 데이터가 없습니다.");
                    break;
                }

                System.out.println();
                printDivider(98);
                System.out.println("급여 상세 내역 목록");
                printDivider(98);

                System.out.println(
                        pad("급여ID", 8) +
                        pad("사번", 8) +
                        pad("이름", 12) +
                        pad("기본급", 14) +
                        pad("야근수당", 14) +
                        pad("휴일수당", 14) +
                        pad("세금", 12) +
                        pad("실수령액", 16)
                );

                System.out.println("-".repeat(98));

                for (Map<String, Object> s : list) {
                    int salId = toInt(s.get("salId"));
                    int userId = toInt(s.get("userId"));
                    String userName = String.valueOf(s.get("userName"));
                    int salBase = toInt(s.get("salBase"));
                    int salOvertime = toInt(s.get("salOvertime"));
                    int salHoliday = toInt(s.get("salHoliday"));
                    int salTax = toInt(s.get("salTax"));
                    int net = salBase + salOvertime + salHoliday - salTax;

                    System.out.println(
                            pad(String.valueOf(salId), 8) +
                            pad(String.valueOf(userId), 8) +
                            pad(userName, 12) +
                            pad(String.format("%,d", salBase), 14) +
                            pad(String.format("%,d", salOvertime), 14) +
                            pad(String.format("%,d", salHoliday), 14) +
                            pad(String.format("%,d", salTax), 12) +
                            pad(String.format("%,d원", net), 16)
                    );
                }

                printDivider(98);

                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│            🧾 수당 내역 관리             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 수정                                │");
                System.out.println("│  [2] 삭제                                │");
                System.out.println("│  [3] 일괄삭제                            │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                input = br.readLine();
                if (input == null) return;

                input = input.trim();

                if ("0".equals(input)) {
                    break;
                }

                if (!"1".equals(input) && !"2".equals(input) && !"3".equals(input)) {
                    System.out.println("잘못 입력했습니다.");
                    continue;
                }

                int act = Integer.parseInt(input);

                if (act == 1 || act == 2) {
                    while (true) {
                        Integer sid = readIntWithBack("급여ID 입력 (뒤로가기: 0): ");
                        if (sid == null) {
                            break;
                        }

                        boolean isExist = false;
                        for (Map<String, Object> m : list) {
                            if (toInt(m.get("salId")) == sid) {
                                isExist = true;
                                break;
                            }
                        }

                        if (!isExist) {
                            System.out.println("❌ 목록에 없는 급여ID입니다. 다시 입력해주세요.");
                            continue;
                        }

                        if (act == 1) {
                            Integer ot = readIntWithBack("변경 야근수당 입력 (뒤로가기: 0): ");
                            if (ot == null) break;

                            Integer ho = readIntWithBack("변경 휴일수당 입력 (뒤로가기: 0): ");
                            if (ho == null) break;

                            dao.updateSalary(sid, ot, ho);
                            System.out.println("✅ 수정 완료!");
                        } else {
                            dao.deleteSalary(sid);
                            System.out.println("✅ 삭제 완료!");
                        }

                        break;
                    }

                } else if (act == 3) {
                    String confirm = readLineWithBack("전체 초기화 하시겠습니까? (Y/N, 뒤로가기: 0): ");
                    if (confirm == null) {
                        continue;
                    }

                    if ("Y".equalsIgnoreCase(confirm.trim())) {
                        dao.deleteSalaryBatch(dNum);
                        System.out.println("✅ 일괄 삭제 완료!");
                    } else if ("N".equalsIgnoreCase(confirm.trim())) {
                        System.out.println("취소되었습니다.");
                    } else {
                        System.out.println("Y 또는 N만 입력하세요.");
                    }
                }
            }
        }
    }

    // ==========================
    // 입력 유틸
    // ==========================
    private String readLineWithBack(String message) throws IOException {
        System.out.print(message);
        String input = br.readLine();

        if (input == null) {
            return null;
        }

        if ("0".equals(input.trim())) {
            return null;
        }

        return input;
    }

    private Integer readIntWithBack(String message) throws IOException {
        while (true) {
            System.out.print(message);
            String input = br.readLine();

            if (input == null) {
                return null;
            }

            input = input.trim();

            if ("0".equals(input)) {
                return null;
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
            }
        }
    }
}