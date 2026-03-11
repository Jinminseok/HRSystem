package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;
import kr.hrsystem.dao.salary_DAO;
import kr.hrsystem.dao.payment_DAO;

public class Payment_Admin {
    private payment_DAO pDao = new payment_DAO();
    private salary_DAO sDao = new salary_DAO();
    private BufferedReader br;

    public Payment_Admin(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        runPaymentMenu();
    }
 
    public void runPaymentMenu() {
        while (true) {
            System.out.println("+──────────────────────────────────────────────+");
            System.out.println("│              급여 지급 관리 시스템           │");
            System.out.println("+──────────────────────────────────────────────+");
            System.out.println("│  1. 지급 현황 조회                           │");
            System.out.println("│  2. 급여 지급 완료 처리 (미지급자 조회)      │");
            System.out.println("│  3. 급여 지급 취소 처리 (지급 완료자 조회)   │");
            System.out.println("│  0. 뒤로가기                                 │");
            System.out.println("+──────────────────────────────────────────────+");            
            System.out.print("  선택 >> ");
            try {
                String input = br.readLine();
                if (input == null || input.isEmpty()) continue;
                
                // ★ [FIXED: 메인 숫자 체크]
                if (!input.matches("\\d+")) {
                    System.out.println("  ⚠️ 숫자를 입력하세요."); continue;
                }
                
                int menu = Integer.parseInt(input);
                if (menu == 0) return;
                
                switch (menu) {
                    case 1: showDeptStatus(); break;
                    case 2: processPayMenu(); break;
                    case 3: cancelPayMenu(); break;
                    default: System.out.println("  ❌ 잘못된 번호입니다.");
                }
            } catch (Exception e) { System.out.println("  ⚠️ 숫자를 입력하세요."); }
        }
    }

    // [1] 지급 현황 조회
    private void showDeptStatus() throws IOException {
        while (true) {
            System.out.println("0. 뒤로 가기");
            System.out.print("○ 지급일 조회 (YYYY-MM) >> ");
            String month = br.readLine();
            if (month.equals("0")) break;
            if (!month.matches("\\d{4}-\\d{2}")) {
                System.out.println("  ⚠️ 형식 오류! (예: 2026-03)"); continue;
            }

            while (true) {                
            	System.out.println("+──────────────────────────────────────────+");
                System.out.println("│          급여 지급 관리 시스템           │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  1. 부서 검색                            │");
                System.out.println("│  2. 전체 검색                            │");
                System.out.println("│  0. 뒤로가기                             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("  선택 >> ");
                
                String input = br.readLine();
                if (input.equals("0")) break;
                if (!input.matches("[1-2]")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                
                int type = Integer.parseInt(input);
                int dNum = -1;

                if (type == 1) {
                    while (true) { // ★ [FIXED: 부서 루프 고정]
                        sDao.showDepartmentList();
                        System.out.println("0. 뒤로 가기");
                        System.out.print("○ 부서번호 >> ");
                        input = br.readLine();
                        if (input.equals("0")) { dNum = -2; break; }
                        if (!input.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                        
                        dNum = Integer.parseInt(input);
                        // ★ [FIXED: 동적 부서 존재 체크]
                        if (!sDao.checkDeptExists(dNum)) {
                            System.out.println("  ❌ 존재하지 않는 부서 번호입니다."); continue;
                        }
                        break;
                    }
                    if (dNum == -2) continue;
                }
                
                pDao.showDeptPaymentStatus(dNum, month);
                System.out.print("Enter: 계속 / 0: 뒤로 >> ");
                if ("0".equals(br.readLine())) break;
            }
        }
    }

    // [2] 급여 지급 완료 처리
    private void processPayMenu() throws IOException {
        while (true) {
            System.out.println("0. 뒤로 가기");
            System.out.print("○ 완료 지급월 (YYYY-MM) >> ");
            String month = br.readLine();
            if (month.equals("0")) break;
            if (!month.matches("\\d{4}-\\d{2}")) { System.out.println("  ⚠️ 형식 오류!"); continue; }

            while (true) {
            	System.out.println("+──────────────────────────────────────────+");
                System.out.println("│          급여 지급 관리 시스템           │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  1. 부서 검색                            │");
                System.out.println("│  2. 전체 검색                            │");
                System.out.println("│  0. 뒤로가기                             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("  선택 >> ");
                String input = br.readLine();
                if (input.equals("0")) break;
                if (!input.matches("[1-2]")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }

                int type = Integer.parseInt(input);
                int dNum = -1;
                if (type == 1) {
                    while (true) { // ★ [FIXED: 부서 루프 고정]
                        sDao.showDepartmentList();
                        System.out.println("0. 뒤로 가기");
                        System.out.print("○ 부서번호 입력 >> ");
                        input = br.readLine();
                        if (input.equals("0")) { dNum = -2; break; }
                        if (!input.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                        dNum = Integer.parseInt(input);
                        if (!sDao.checkDeptExists(dNum)) { System.out.println("  ❌ 없는 부서입니다."); continue; }
                        break;
                    }
                    if (dNum == -2) continue;
                }

                while (true) {
                    pDao.showFilteredPaymentList(dNum, month, "N");
                    System.out.println("1. 개별 지급");
                    System.out.println("2. 일괄 지급");
                    System.out.println("0. 뒤로 가기");
                    System.out.print("작업 선택 >> ");
                    input = br.readLine();
                    if (input.equals("0")) break;
                    if (!input.matches("[1-2]")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                    
                    int act = Integer.parseInt(input);
                    if (act == 1) {
                        while (true) { // ★ [FIXED: 사번 입력 단계 고정]
                        	System.out.println("0. 뒤로 가기");
                            System.out.print("○ 사번 입력 >> ");
                            String uidInput = br.readLine();
                            if (uidInput.equals("0")) break;
                            if (!uidInput.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                            
                            int uid = Integer.parseInt(uidInput);
                            
                            // ★ [FIXED: 사번 존재 여부 실시간 검증]
                            if (!sDao.checkUserExists(uid)) {
                                System.out.println("  ❌ 존재하지 않는 사번입니다."); continue;
                            }

                            if (pDao.processPayment(uid, month) > 0) {
                                System.out.println("  ✅ 지급 완료!"); break;
                            } else {
                                // ★ [FIXED: 지급 대상이 아닌 경우]
                                System.out.println("  ❌ 이미 지급되었거나 지급 대상자가 아닙니다.");
                            }
                        }
                    } else if (act == 2) {
                        while (true) {
                            System.out.print("⚠️ 일괄 지급하시겠습니까? (Y/N/0:취소) : ");
                            String confirm = br.readLine();
                            if (confirm.equalsIgnoreCase("Y")) {
                                int count = pDao.processPaymentBatch(dNum, month);
                                System.out.println("  ✅ 총 " + count + "건 지급 완료!"); break;
                            } else if (confirm.equalsIgnoreCase("N") || confirm.equals("0")) {
                                System.out.println("  🚫 취소되었습니다."); break;
                            } else { System.out.println("  ⚠️ Y 또는 N을 입력하세요."); }
                        }
                    }
                }
            }
        }
    }

    // [3] 급여 지급 취소 처리
    private void cancelPayMenu() throws IOException {
        while (true) {
            System.out.println("0. 뒤로 가기");
            System.out.print("○ 취소 지급월 (YYYY-MM) >> ");
            String month = br.readLine();
            if (month.equals("0")) break;
            if (!month.matches("\\d{4}-\\d{2}")) { System.out.println("  ⚠️ 형식 오류!"); continue; }

            while (true) {
            	System.out.println("+──────────────────────────────────────────+");
                System.out.println("│          급여 지급 관리 시스템           │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  1. 부서 검색                            │");
                System.out.println("│  2. 전체 검색                            │");
                System.out.println("│  0. 뒤로가기                             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("  선택 >> ");
                String input = br.readLine();
                if (input.equals("0")) break;
                if (!input.matches("[1-2]")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }

                int type = Integer.parseInt(input);
                int dNum = -1;
                if (type == 1) {
                    while (true) { // ★ [FIXED: 부서 루프 고정]
                        sDao.showDepartmentList();
                        System.out.println("0. 뒤로 가기");
                        System.out.print("○ 부서번호 입력 >> ");
                        input = br.readLine();
                        if (input.equals("0")) { dNum = -2; break; }
                        if (!input.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                        dNum = Integer.parseInt(input);
                        if (!sDao.checkDeptExists(dNum)) { System.out.println("  ❌ 없는 부서입니다."); continue; }
                        break;
                    }
                    if (dNum == -2) continue;
                }

                while (true) {
                    pDao.showFilteredPaymentList(dNum, month, "Y");                    
                    System.out.println("1. 개별 지급 취소");
                    System.out.println("2. 일괄 지급 취소");
                    System.out.println("0. 뒤로 가기");
                    System.out.print("작업 선택 >> ");
                    input = br.readLine();
                    if (input.equals("0")) break;
                    if (!input.matches("[1-2]")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }

                    int act = Integer.parseInt(input);
                    if (act == 1) {
                        while (true) { // ★ [FIXED: 사번 입력 단계 고정]
                        	System.out.println("0. 뒤로 가기");
                            System.out.print("○ 취소할 사번 입력 >> ");
                            String uidInput = br.readLine();
                            if (uidInput.equals("0")) break;
                            if (!uidInput.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                            
                            int uid = Integer.parseInt(uidInput);

                            // ★ [FIXED: 사번 존재 여부 실시간 검증]
                            if (!sDao.checkUserExists(uid)) {
                                System.out.println("  ❌ 존재하지 않는 사번입니다."); continue;
                            }

                            if (pDao.cancelPayment(uid, month) > 0) {
                                System.out.println("  ✅ 취소 완료!"); break;
                            } else {
                                System.out.println("  ❌ 취소할 데이터가 없거나 잘못된 사번입니다.");
                            }
                        }
                    } else if (act == 2) {
                        while (true) {
                            System.out.print("⚠️ 일괄 취소하시겠습니까? (Y/N/0:취소) : ");
                            String confirm = br.readLine();
                            if (confirm.equalsIgnoreCase("Y")) {
                                int count = pDao.cancelPaymentBatch(dNum, month);
                                System.out.println("  ✅ 총 " + count + "건 취소 완료!"); break;
                            } else if (confirm.equalsIgnoreCase("N") || confirm.equals("0")) {
                                System.out.println("  🚫 취소되었습니다."); break;
                            } else { System.out.println("  ⚠️ Y 또는 N을 입력하세요."); }
                        }
                    }
                }
            }
        }
    }
} 