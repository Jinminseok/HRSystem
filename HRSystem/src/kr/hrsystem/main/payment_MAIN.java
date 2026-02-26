package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;
import kr.hrsystem.dao.salary_DAO;
import kr.hrsystem.dao.payment_DAO;

public class payment_MAIN {
    private payment_DAO pDao = new payment_DAO();
    private salary_DAO sDao = new salary_DAO();
    private BufferedReader br;

    public payment_MAIN(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        runPaymentMenu();
    }

    public void runPaymentMenu() {
        while (true) {
            System.out.println("\n  ● 급여 지급 관리 시스템");
            System.out.println("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("  1 · 지급 현황 조회 (전체 사원)");
            System.out.println("  2 · 급여 지급 완료 (미지급자 필터)");
            System.out.println("  3 · 급여 지급 취소 (완료자 필터)");
            System.out.println("  0 · 이전 메뉴로 돌아가기");
            System.out.println("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.print("  선택 >> ");
            try {
                String input = br.readLine();
                if (input == null || input.isEmpty()) continue;
                int menu = Integer.parseInt(input);
                if (menu == 0) return;
                
                switch (menu) {
                    case 1: showDeptStatus(); break;
                    case 2: processPayMenu(); break;
                    case 3: cancelPayMenu(); break;
                }
            } catch (Exception e) { System.out.println("  ⚠️ 숫자만 입력하세요."); }
        }
    }

    private void showDeptStatus() throws IOException {
        topLoop:
        while (true) {
            System.out.print("\n  ○ 조회 귀속월 (YYYY-MM / 0.이전) >> ");
            String month = br.readLine();
            if (month.equals("0")) break;

            while (true) {
                System.out.print("  ○ 범위 선택 (1.부서별 / 2.전체 / 0.이전 / 99.처음으로) >> ");
                String input = br.readLine();
                if (input.equals("99")) break topLoop;
                int type = Integer.parseInt(input);
                if (type == 0) break;

                int dNum = -1;
                if (type == 1) {
                    sDao.showDepartmentList();
                    System.out.print("  ○ 부서번호 (0.이전 / 99.처음으로) >> ");
                    input = br.readLine();
                    if (input.equals("99")) break topLoop;
                    dNum = Integer.parseInt(input);
                    if (dNum == 0) continue;
                }
                pDao.showDeptPaymentStatus(dNum, month);
                System.out.println("\n  (계속 조회하려면 엔터, 범위를 다시 고르려면 0 입력, 처음으로 99)");
                String next = br.readLine();
                if (next.equals("99")) break topLoop;
                if (next.equals("0")) break;
            }
        }
    }

    private void processPayMenu() throws IOException {
        topLoop:
        while (true) {
            System.out.print("\n  ○ 완료 귀속월 (YYYY-MM / 0.이전) >> ");
            String month = br.readLine();
            if (month.equals("0")) break;

            while (true) {
                System.out.print("  ○ 대상 범위 (1.부서별 / 2.전체 / 0.이전 / 99.처음으로) >> ");
                String input = br.readLine();
                if (input.equals("99")) break topLoop;
                int type = Integer.parseInt(input);
                if (type == 0) break;

                int dNum = -1;
                if (type == 1) {
                    sDao.showDepartmentList();
                    System.out.print("  ○ 부서번호 (0.이전 / 99.처음으로) >> ");
                    input = br.readLine();
                    if (input.equals("99")) break topLoop;
                    dNum = Integer.parseInt(input);
                    if (dNum == 0) continue;
                }

                while (true) {
                    pDao.showFilteredPaymentList(dNum, month, "N");
                    System.out.println("\n  [ 1.개별 지급 | 2.일괄 지급 | 0.이전 | 99.처음으로 ]");
                    System.out.print("  작업 선택 >> ");
                    input = br.readLine();
                    if (input.equals("99")) break topLoop;
                    int act = Integer.parseInt(input);
                    if (act == 0) break;

                    if (act == 1) {
                        System.out.print("  ○ 지급할 사번 입력 >> "); int uid = Integer.parseInt(br.readLine());
                        if (pDao.processPayment(uid, month) > 0) System.out.println("  ✅ 지급 완료!");
                    } else if (act == 2) {
                        System.out.print("  ⚠️ 일괄 지급하시겠습니까? (Y/N) : ");
                        if (br.readLine().equalsIgnoreCase("Y")) pDao.processPaymentBatch(dNum, month);
                    }
                }
            }
        }
    }

    private void cancelPayMenu() throws IOException {
        topLoop:
        while (true) {
            System.out.print("\n  ○ 취소 귀속월 (YYYY-MM / 0.이전) >> ");
            String month = br.readLine();
            if (month.equals("0")) break;

            while (true) {
                System.out.print("  ○ 대상 범위 (1.부서별 / 2.전체 / 0.이전 / 99.처음으로) >> ");
                String input = br.readLine();
                if (input.equals("99")) break topLoop;
                int type = Integer.parseInt(input);
                if (type == 0) break;

                int dNum = -1;
                if (type == 1) {
                    sDao.showDepartmentList();
                    System.out.print("  ○ 부서번호 (0.이전 / 99.처음으로) >> ");
                    input = br.readLine();
                    if (input.equals("99")) break topLoop;
                    dNum = Integer.parseInt(input);
                    if (dNum == 0) continue; 
                }

                while (true) {
                    pDao.showFilteredPaymentList(dNum, month, "Y");
                    System.out.println("\n  [ 1.개별 취소 | 2.일괄 취소 | 0.이전 | 99.처음으로 ]");
                    System.out.print("  작업 선택 >> ");
                    input = br.readLine();
                    if (input.equals("99")) break topLoop;
                    int act = Integer.parseInt(input);
                    if (act == 0) break;

                    if (act == 1) {
                        System.out.print("  ○ 사번 입력 >> "); int uid = Integer.parseInt(br.readLine());
                        if (pDao.cancelPaymentBatch(uid, month) > 0) System.out.println("  ✅ 취소 완료!");
                    } else if (act == 2) {
                        System.out.print("  ⚠️ 일괄 취소하시겠습니까? (Y/N) : ");
                        if (br.readLine().equalsIgnoreCase("Y")) pDao.cancelPaymentBatch(dNum, month);
                    }
                }
            }
        }
    }
}