package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.payment_DAO;
import kr.hrsystem.dao.salary_DAO;

public class Payment_Admin {

    private payment_DAO pDao = new payment_DAO();
    private salary_DAO sDao = new salary_DAO();
    private BufferedReader br;

    public Payment_Admin(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        runPaymentMenu();
    }

    // ==========================
    // 메인 메뉴
    // ==========================
    public void runPaymentMenu() {
        while (true) {
            try {
                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│          💸 급여 지급 관리 시스템        │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 지급 현황 조회                      │");
                System.out.println("│  [2] 급여 지급 완료 처리                 │");
                System.out.println("│  [3] 급여 지급 취소 처리                 │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                String input = br.readLine();
                if (input == null) {
                    return;
                }

                input = input.trim();

                if ("0".equals(input)) {
                    return;
                }

                switch (input) {
                    case "1":
                        showDeptStatus();
                        break;
                    case "2":
                        processPayMenu();
                        break;
                    case "3":
                        cancelPayMenu();
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
    // [1] 지급 현황 조회
    // ==========================
    private void showDeptStatus() throws IOException {
        while (true) {
            String month = readYearMonthWithBack("조회 지급월 입력 (YYYY-MM / 뒤로가기: 0): ");
            if (month == null) {
                return;
            }

            while (true) {
                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│            📊 지급 현황 조회             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 부서별 조회                         │");
                System.out.println("│  [2] 전체 조회                           │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                String input = br.readLine();
                if (input == null) {
                    return;
                }

                input = input.trim();

                if ("0".equals(input)) {
                    break;
                }

                if (!"1".equals(input) && !"2".equals(input)) {
                    System.out.println("잘못 입력했습니다.");
                    continue;
                }

                int dNum = -1;

                if ("1".equals(input)) {
                    while (true) {
                        sDao.showDepartmentList();

                        Integer deptNum = readIntWithBack("부서번호 입력 (뒤로가기: 0): ");
                        if (deptNum == null) {
                            dNum = -2;
                            break;
                        }

                        if (!sDao.checkDeptExists(deptNum)) {
                            System.out.println("❌ 존재하지 않는 부서번호입니다. 다시 입력해주세요.");
                            continue;
                        }

                        dNum = deptNum;
                        break;
                    }

                    if (dNum == -2) {
                        continue;
                    }
                }

                pDao.showDeptPaymentStatus(dNum, month);

                String back = readContinueOrBack("뒤로가기: 0 / 계속 조회: 엔터 >> ");
                if (back == null) {
                    break;
                }
            }
        }
    }

    // ==========================
    // [2] 급여 지급 완료 처리
    // ==========================
    private void processPayMenu() throws IOException {
        while (true) {
            String month = readYearMonthWithBack("지급 완료 처리월 입력 (YYYY-MM / 뒤로가기: 0): ");
            if (month == null) {
                return;
            }

            while (true) {
                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│          ✅ 급여 지급 완료 처리          │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 부서별 조회                         │");
                System.out.println("│  [2] 전체 조회                           │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                String input = br.readLine();
                if (input == null) {
                    return;
                }

                input = input.trim();

                if ("0".equals(input)) {
                    break;
                }

                if (!"1".equals(input) && !"2".equals(input)) {
                    System.out.println("잘못 입력했습니다.");
                    continue;
                }

                int dNum = -1;

                if ("1".equals(input)) {
                    while (true) {
                        sDao.showDepartmentList();

                        Integer deptNum = readIntWithBack("부서번호 입력 (뒤로가기: 0): ");
                        if (deptNum == null) {
                            dNum = -2;
                            break;
                        }

                        if (!sDao.checkDeptExists(deptNum)) {
                            System.out.println("❌ 존재하지 않는 부서번호입니다. 다시 입력해주세요.");
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
                    pDao.showFilteredPaymentList(dNum, month, "N");

                    System.out.println();
                    System.out.println("+──────────────────────────────────────────+");
                    System.out.println("│          ✅ 급여 지급 완료 처리          │");
                    System.out.println("+──────────────────────────────────────────+");
                    System.out.println("│  [1] 개별 지급                           │");
                    System.out.println("│  [2] 일괄 지급                           │");
                    System.out.println("│  [0] 뒤로가기                            │");
                    System.out.println("+──────────────────────────────────────────+");
                    System.out.print("선택 >> ");

                    input = br.readLine();
                    if (input == null) {
                        return;
                    }

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
                            Integer uid = readIntWithBack("지급할 사번 입력 (뒤로가기: 0): ");
                            if (uid == null) {
                                break;
                            }

                            if (!sDao.checkUserExists(uid)) {
                                System.out.println("❌ 존재하지 않는 사번입니다. 다시 입력해주세요.");
                                continue;
                            }

                            if (pDao.processPayment(uid, month) > 0) {
                                System.out.println("✅ 급여 지급 완료!");
                                break;
                            } else {
                                System.out.println("❌ 이미 지급되었거나 지급 대상자가 아닙니다.");
                            }
                        }

                    } else {
                        String confirm = readLineWithBack("일괄 지급하시겠습니까? (Y/N, 뒤로가기: 0): ");
                        if (confirm == null) {
                            continue;
                        }

                        if ("Y".equalsIgnoreCase(confirm.trim())) {
                            int count = pDao.processPaymentBatch(dNum, month);
                            System.out.println("✅ 총 " + count + "건 지급 완료!");
                        } else if ("N".equalsIgnoreCase(confirm.trim())) {
                            System.out.println("취소되었습니다.");
                        } else {
                            System.out.println("Y 또는 N만 입력하세요.");
                        }
                    }
                }
            }
        }
    }

    // ==========================
    // [3] 급여 지급 취소 처리
    // ==========================
    private void cancelPayMenu() throws IOException {
        while (true) {
            String month = readYearMonthWithBack("지급 취소 처리월 입력 (YYYY-MM / 뒤로가기: 0): ");
            if (month == null) {
                return;
            }

            while (true) {
                System.out.println();
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│          ↩️ 급여 지급 취소 처리           │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  [1] 부서별 조회                         │");
                System.out.println("│  [2] 전체 조회                           │");
                System.out.println("│  [0] 뒤로가기                            │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 >> ");

                String input = br.readLine();
                if (input == null) {
                    return;
                }

                input = input.trim();

                if ("0".equals(input)) {
                    break;
                }

                if (!"1".equals(input) && !"2".equals(input)) {
                    System.out.println("잘못 입력했습니다.");
                    continue;
                }

                int dNum = -1;

                if ("1".equals(input)) {
                    while (true) {
                        sDao.showDepartmentList();

                        Integer deptNum = readIntWithBack("부서번호 입력 (뒤로가기: 0): ");
                        if (deptNum == null) {
                            dNum = -2;
                            break;
                        }

                        if (!sDao.checkDeptExists(deptNum)) {
                            System.out.println("❌ 존재하지 않는 부서번호입니다. 다시 입력해주세요.");
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
                    pDao.showFilteredPaymentList(dNum, month, "Y");

                    System.out.println();
                    System.out.println("+──────────────────────────────────────────+");
                    System.out.println("│          ↩️ 급여 지급 취소 처리           │");
                    System.out.println("+──────────────────────────────────────────+");
                    System.out.println("│  [1] 개별 지급 취소                      │");
                    System.out.println("│  [2] 일괄 지급 취소                      │");
                    System.out.println("│  [0] 뒤로가기                            │");
                    System.out.println("+──────────────────────────────────────────+");
                    System.out.print("선택 >> ");

                    input = br.readLine();
                    if (input == null) {
                        return;
                    }

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
                            Integer uid = readIntWithBack("취소할 사번 입력 (뒤로가기: 0): ");
                            if (uid == null) {
                                break;
                            }

                            if (!sDao.checkUserExists(uid)) {
                                System.out.println("❌ 존재하지 않는 사번입니다. 다시 입력해주세요.");
                                continue;
                            }

                            if (pDao.cancelPayment(uid, month) > 0) {
                                System.out.println("✅ 지급 취소 완료!");
                                break;
                            } else {
                                System.out.println("❌ 취소할 데이터가 없거나 잘못된 사번입니다.");
                            }
                        }

                    } else {
                        String confirm = readLineWithBack("일괄 지급 취소하시겠습니까? (Y/N, 뒤로가기: 0): ");
                        if (confirm == null) {
                            continue;
                        }

                        if ("Y".equalsIgnoreCase(confirm.trim())) {
                            int count = pDao.cancelPaymentBatch(dNum, month);
                            System.out.println("✅ 총 " + count + "건 취소 완료!");
                        } else if ("N".equalsIgnoreCase(confirm.trim())) {
                            System.out.println("취소되었습니다.");
                        } else {
                            System.out.println("Y 또는 N만 입력하세요.");
                        }
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

    private String readYearMonthWithBack(String message) throws IOException {
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

            if (!input.matches("\\d{4}-\\d{2}")) {
                System.out.println("❌ 월 형식이 잘못되었습니다. 예: 2026-03");
                continue;
            }

            return input;
        }
    }

    private String readContinueOrBack(String message) throws IOException {
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
}