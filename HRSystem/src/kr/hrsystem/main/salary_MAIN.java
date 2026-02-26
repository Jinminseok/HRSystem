package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import kr.hrsystem.dao.salary_DAO;

public class salary_MAIN {
    private salary_DAO dao = new salary_DAO();
    private BufferedReader br;
    private int adminUserId;
    private int loginLogId;

    public salary_MAIN(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        this.adminUserId = adminUserId;
        this.loginLogId = loginLogId;
        runSalaryMenu();
    }

    public String align(String text, int length) {
        if (text == null) text = "-";
        int currentLength = 0;
        for (char c : text.toCharArray()) {
            if (c >= '\uAC00' && c <= '\uD7A3') currentLength += 2; 
            else currentLength += 1;
        }
        return text + " ".repeat(Math.max(0, length - currentLength));
    }

    public void runSalaryMenu() {
        while (true) {
            System.out.println("\n  ● 급여 상세 관리 시스템");
            System.out.println("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("  1 · 수당 미등록 내역 조회 및 등록");
            System.out.println("  2 · 월별 총급여 상세 조회 (명세서)");
            System.out.println("  3 · 급여 내역 관리 (수정/삭제)");
            System.out.println("  4 · 총급여 지급 여부 관리 (N/Y 관리)");
            System.out.println("  0 · 프로그램 종료 (관리자 화면으로)");
            System.out.println("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.print("  선택 >> ");
            try {
                String input = br.readLine();
                if (input == null || input.isEmpty()) continue;
                int menu = Integer.parseInt(input);
                if (menu == 0) return;
                switch (menu) {
                    case 1: showUnpaidMenu(); break;
                    case 2: showSummaryMenu(); break;
                    case 3: manageSalaryMenu(); break;
                    case 4: new payment_MAIN(br, adminUserId, loginLogId); break;
                    default: System.out.println("  ❌ 잘못된 번호입니다.");
                }
            } catch (Exception e) { System.out.println("  ⚠️ 숫자만 입력하세요."); }
        }
    }

    // [1] 급여 미등록 내역 조회 및 등록
    private void showUnpaidMenu() throws IOException {
        while (true) {
            System.out.print("\n  ○ 대상 선택 (1.부서별 / 2.전체 / 0.이전) >> ");
            String input = br.readLine();
            if (input.equals("0")) return; // 메인 1~4번 메뉴로 이동
            int type = Integer.parseInt(input);

            int dNum = -1;
            if (type == 1) { 
                dao.showDepartmentList(); 
                System.out.print("  ○ 부서번호 입력 (0.이전 / 99.처음으로) >> "); 
                input = br.readLine();
                if (input.equals("99")) return; // 메인 메뉴로 점프
                if (input.equals("0")) continue; // 대상 선택 단계로
                dNum = Integer.parseInt(input);
            }
            
            while (true) {
                dao.showUnpaidWorkers(dNum);
                System.out.println("\n  [ 1.개별 등록 | 2.일괄 등록 | 0.이전 | 99.처음으로 ]");
                System.out.print("  작업 선택 >> ");
                input = br.readLine();
                if (input.equals("99")) return; // 메인 메뉴로 점프
                if (input.equals("0")) break; // 부서/전체 선택 단계로
                
                int act = Integer.parseInt(input);
                if (act == 1) {
                    System.out.print("  ○ 사번 입력 >> "); int uid = Integer.parseInt(br.readLine());
                    dao.showAttendanceList(uid);
                    System.out.print("  ○ 근태ID 입력 >> "); int aid = Integer.parseInt(br.readLine());
                    if (dao.insertSalary(uid, aid) > 0) System.out.println("  ✅ 등록 완료!");
                } else if (act == 2) {
                    System.out.print("  ⚠️ 미등록 내역을 일괄 등록하시겠습니까? (Y/N) : ");
                    if (br.readLine().equalsIgnoreCase("Y")) {
                        int count = dao.insertSalaryBatch(dNum);
                        System.out.println("  ✅ 총 " + count + "건 등록 완료!");
                    }
                }
            }
        }
    }

    // [2] 월별 급여 합계 조회 (명세서)
    private void showSummaryMenu() throws IOException {
        while (true) {
            System.out.println("\n  ● 월별 급여 요약 조회 (명세서)");
            System.out.print("  ○ 대상 범위 (1.부서별 / 2.전체 / 0.이전) >> ");
            String input = br.readLine();
            if (input.equals("0")) return; // 메인 메뉴로 이동
            int type = Integer.parseInt(input);

            int dNum = -1;
            if (type == 1) {
                dao.showDepartmentList();
                System.out.print("  ○ 부서번호 입력 (0.이전 / 99.처음으로) >> ");
                input = br.readLine();
                if (input.equals("99")) return;
                if (input.equals("0")) continue;
                dNum = Integer.parseInt(input);
            }

            while (true) {
                dao.showUserListByDept(dNum);
                System.out.print("  ○ 사번 선택 (0.이전 / 99.처음으로) >> ");
                input = br.readLine();
                if (input.equals("99")) return;
                int uid = Integer.parseInt(input);
                if (uid == 0) break; // 범위 선택 단계로

                System.out.print("  ○ 조회 월 (YYYY-MM / 0.이전) >> ");
                String month = br.readLine();
                if (month.equals("0")) continue; // 사원 선택 단계로

                Map<String, Object> s = dao.selectMonthlySummary(uid, month);
                if (s != null) {
                    System.out.println("\n  ● [" + month + "] " + s.get("userName") + " 사원 급여 명세");
                    System.out.println("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    System.out.println("  · 기본급   : " + String.format("%12s", String.format("%,d", (int)s.get("salBase"))) + " 원");
                    System.out.println("  · 휴일수당 : " + String.format("%12s", String.format("%,d", (int)s.get("salHoliday"))) + " 원");
                    System.out.println("  · 야근수당 : " + String.format("%12s", String.format("%,d", (int)s.get("salOvertime"))) + " 원");
                    System.out.println("  · 공제세금 : " + String.format("%12s", String.format("%,d", (int)s.get("salTax"))) + " 원");
                    System.out.println("  ──────────────────────────────");
                    System.out.println("  ✨ 실수령액 : " + String.format("%12s", String.format("%,d", (int)s.get("salTotal"))) + " 원");
                    System.out.println("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                } else {
                    System.out.println("  ❌ 해당 월의 급여 정보가 없습니다.");
                }
                System.out.println("\n  (계속 조회: Enter, 뒤로 가기: 0, 처음으로: 99)");
                String next = br.readLine();
                if (next.equals("99")) return;
                if (next.equals("0")) break;
            }
        }
    }

    // [3] 급여 내역 관리 (수정/삭제/초기화)
    private void manageSalaryMenu() throws IOException {
        while (true) {
            System.out.print("\n  ○ 관리 범위 (1.부서별 / 2.전체 / 0.이전) >> ");
            String input = br.readLine();
            if (input.equals("0")) return;
            int type = Integer.parseInt(input);

            int dNum = -1;
            if (type == 1) { 
                dao.showDepartmentList(); 
                System.out.print("  ○ 부서번호 입력 (0.이전 / 99.처음으로) >> "); 
                input = br.readLine();
                if (input.equals("99")) return;
                if (input.equals("0")) continue;
                dNum = Integer.parseInt(input);
            }
            
            while (true) {
                List<Map<String, Object>> list = dao.selectSalaryList(dNum);
                System.out.println("\n  ● 급여 상세 내역 목록 (등록 완료 건)");
                System.out.println("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("  " + align("ID", 6) + align("사번", 8) + align("이름", 10) + align("기본급", 14) + align("야근수당", 14) + align("휴일수당", 14) + "세금");
                System.out.println("  ────────────────────────────────────────────────────────────────────────────────────────────");
                
                for (Map<String, Object> s : list) {
                    System.out.println("  " + align(String.valueOf(s.get("salId")), 6) + 
                                       align(String.valueOf(s.get("userId")), 8) + 
                                       align((String)s.get("userName"), 10) + 
                                       align(String.format("%,d", (int)s.get("salBase")) + "원", 14) + 
                                       align(String.format("%,d", (int)s.get("salOvertime")) + "원", 14) + 
                                       align(String.format("%,d", (int)s.get("salHoliday")) + "원", 14) + 
                                       String.format("%,d", (int)s.get("salTax")) + "원");
                }
                System.out.println("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
                System.out.println("\n  [ 1.수정 | 2.삭제 | 3.일괄 삭제 | 0.이전 | 99.처음으로 ]");
                System.out.print("  작업 선택 >> ");
                input = br.readLine();
                if (input.equals("99")) return;
                if (input.equals("0")) break;

                int act = Integer.parseInt(input);
                if (act == 1) {
                    System.out.print("  ○ SAL_ID >> "); int sid = Integer.parseInt(br.readLine());
                    System.out.print("  ○ 변경 야근수당 >> "); int ot = Integer.parseInt(br.readLine());
                    System.out.print("  ○ 변경 휴일수당 >> "); int ho = Integer.parseInt(br.readLine());
                    if (dao.updateSalary(sid, ot, ho) > 0) System.out.println("  ✅ 수정 완료!");
                } else if (act == 2) {
                    System.out.print("  ○ 삭제할 SAL_ID >> "); int sid = Integer.parseInt(br.readLine());
                    if (dao.deleteSalary(sid) > 0) System.out.println("  ✅ 삭제 완료!");
                } else if (act == 3) {
                    System.out.print("  ⚠️ 전체 내역을 초기화하시겠습니까? (Y/N) : ");
                    if (br.readLine().equalsIgnoreCase("Y")) {
                        int count = dao.deleteSalaryBatch(dNum);
                        System.out.println("  ✅ " + count + "건 삭제 완료!");
                    }
                }
            }
        }
    }
}