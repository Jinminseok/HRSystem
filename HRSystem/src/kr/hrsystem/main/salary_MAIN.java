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

    // [메인 메뉴]
    public void runSalaryMenu() {
        while (true) {
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│           급여 상세 관리 시스템          │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  1. 수당 미등록 관리                     │");
            System.out.println("│  2. 월별 총급여 조회                     │");
            System.out.println("│  3. 수당 내역 관리                       │");
            System.out.println("│  4. 총급여 지급 여부 관리                │");
            System.out.println("│  0. 뒤로가기                             │");   
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("  선택 >> ");
            try {
                String input = br.readLine();
                if (input == null || input.isEmpty()) continue;
                
                // ★ [FIXED: 메인 메뉴 숫자 체크]
                if (!input.matches("\\d+")) {
                    System.out.println("  ⚠️ 숫자를 입력하세요.");
                    continue;
                }
                
                int menu = Integer.parseInt(input);
                if (menu == 0) return;
                switch (menu) {
                    case 1: showUnpaidMenu(); break;
                    case 2: showSummaryMenu(); break;
                    case 3: manageSalaryMenu(); break;
                    case 4: new payment_MAIN(br, adminUserId, loginLogId); break;
                    default: System.out.println("  ❌ 존재하지 않는 번호입니다.");
                }
            } catch (Exception e) { 
                // ★ [FIXED: 시스템 에러 메시지 은폐]
                System.out.println("  ⚠️ 숫자를 입력하세요."); 
            }
        }
    }

    // [1] 수당 미등록 관리
    private void showUnpaidMenu() throws IOException {
        while (true) {
        	System.out.println("+──────────────────────────────────────────+");
            System.out.println("│             수당 미등록 관리             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  1. 부서 검색                            │");
            System.out.println("│  2. 전체 검색                            │");            
            System.out.println("│  0. 뒤로가기                             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("  선택 >> ");
            String input = br.readLine();
            if (input == null || input.equals("0")) return;
            
            // ★ [FIXED: 숫자 체크]
            if (!input.matches("[1-2]")) {
                System.out.println("  ⚠️ 숫자를 입력하세요. (1 또는 2)"); continue;
            }
            
            int type = Integer.parseInt(input);
            int dNum = -1;

            if (type == 1) { 
                while(true) {
                    dao.showDepartmentList();
                    System.out.println("0. 뒤로 가기");
                    System.out.print("  ○ 부서번호 입력 >> ");
                    String dStr = br.readLine();
                    if (dStr.equals("0")) { dNum = -2; break; }
                    // ★ [FIXED: 부서번호 숫자 체크]
                    if (!dStr.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                    
                    int tempDNum = Integer.parseInt(dStr);
                    if (dao.checkDeptExists(tempDNum)) {
                        dNum = tempDNum; break;
                    } else {
                        System.out.println("  ❌ 존재하지 않는 부서입니다. 다시 입력하세요.");
                    }
                }
                if (dNum == -2) continue;
            }
            
            while (true) {
                dao.showUnpaidWorkers(dNum);
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│             수당 미등록 관리             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  1. 개별 등록                            │");
                System.out.println("│  2. 일괄 등록                            │");            
                System.out.println("│  0. 뒤로가기                             │");
                System.out.println("+──────────────────────────────────────────+");                
                
                System.out.print("  선택 >> ");
                input = br.readLine();
                if (input.equals("0")) break;
                // ★ [FIXED: 작업 선택 숫자 체크]
                if (!input.matches("[1-2]")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                
                if (Integer.parseInt(input) == 1) {
                    while(true) {
                    	System.out.println("0. 뒤로 가기");
                        System.out.print("  ○ 사번 입력 >> "); 
                        String uidStr = br.readLine();
                        if (uidStr.equals("0")) break;
                        // ★ [FIXED: 사번 숫자 체크]
                        if (!uidStr.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                        
                        int uid = Integer.parseInt(uidStr);
                        if (!dao.showAttendanceList(uid)) {
                            System.out.println("  ❌ 존재하지 않는 사번이거나 미등록 내역이 없습니다."); continue;
                        }
                        
                        boolean success = false;
                        while(true) {
                        	System.out.println("0. 뒤로 가기");
                            System.out.print("  ○ 근태ID 입력 >> "); 
                            String aidStr = br.readLine();
                            if (aidStr.equals("0")) break;
                            // ★ [FIXED: 근태ID 숫자 체크]
                            if (!aidStr.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                            
                            if (dao.insertSalary(uid, Integer.parseInt(aidStr)) > 0) {
                                System.out.println("  ✅ 등록 성공!"); success = true; break;
                            } else {
                                System.out.println("  ❌ 존재하지 않는 근태ID입니다.");
                            }
                        }
                        if (success) break;
                    }
                } else {
                    dao.insertSalaryBatch(dNum); System.out.println("  ✅ 완료!");
                }
            }
        }
    }

    // [2] 월별 총급여 조회
    private void showSummaryMenu() throws IOException {
        while (true) {
        	System.out.println("+──────────────────────────────────────────+");
            System.out.println("│             월별 총급여 조회             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  1. 부서 검색                            │");
            System.out.println("│  2. 전체 검색                            │");            
            System.out.println("│  0. 뒤로가기                             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("  선택 >> ");
            String input = br.readLine();
            if (input == null || input.equals("0")) return;
            if (!input.matches("[1-2]")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
            
            int dNum = -1;
            if (input.equals("1")) {
                while (true) {
                    dao.showDepartmentList();
                	System.out.println("0. 뒤로 가기");
                    System.out.print("  ○ 부서번호 입력 >> ");
                    input = br.readLine();
                    if (input.equals("0")) { dNum = -2; break; }
                    if (!input.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                    dNum = Integer.parseInt(input);
                    if (!dao.checkDeptExists(dNum)) { System.out.println("  ❌ 없는 부서입니다."); continue; }
                    break;
                }
                if (dNum == -2) continue;
            }

            while (true) {
                dao.showUserListByDept(dNum);
                int uid = 0;
                while (true) {
                	System.out.println("0. 뒤로 가기");
                    System.out.print("  ○ 사번 선택 >> ");
                    input = br.readLine();
                    if (input.equals("0")) { uid = -1; break; }
                    if (!input.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                    uid = Integer.parseInt(input);
                    if (!dao.checkUserExists(uid)) { System.out.println("  ❌ 없는 사번입니다."); continue; }
                    break;
                }
                if (uid == -1) break;

                while (true) {
                	System.out.println("0. 뒤로 가기");
                    System.out.print("  ○ 조회 월 (YYYY-MM) >> ");
                    String month = br.readLine();
                    if (month.equals("0")) { uid = -2; break; }
                    if (!month.matches("\\d{4}-\\d{2}")) { System.out.println("  ⚠️ 형식 오류! (예: 2026-03)"); continue; }

                    Map<String, Object> s = dao.selectMonthlySummary(uid, month);
                    if (s != null && s.get("userName") != null) {
                        System.out.println("+──────────────────────────────────────────────────────────────────+");
                        System.out.println("  ● [" + month + "] " + s.get("userName") + " 사원 급여 명세");
                        System.out.println("  ✨ 실수령액 : " + String.format("%,d", (int)s.get("salTotal")) + " 원");
                        System.out.println("+──────────────────────────────────────────────────────────────────+");
                        break; 
                    } else {
                        System.out.println("  ❌ 급여 내역이 없습니다.");
                    }
                }
                if (uid == -2) continue; 
            	System.out.println("0. 뒤로 가기");
                System.out.print("Enter 계속 >> ");
                if ("0".equals(br.readLine())) break;
            }
        }
    }

    // [3] 수당 내역 관리
    private void manageSalaryMenu() throws IOException {
        while (true) {            
        	System.out.println("+──────────────────────────────────────────+");
            System.out.println("│              수당 내역 관리              │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  1. 부서 검색                            │");
            System.out.println("│  2. 전체 검색                            │");            
            System.out.println("│  0. 뒤로가기                             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("  선택 >> ");
            String input = br.readLine();
            if (input == null || input.equals("0")) return;
            if (!input.matches("[1-2]")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
            
            int dNum = -1;
            if (input.equals("1")) { 
                while (true) {
                    dao.showDepartmentList(); 
                    System.out.println("0. 뒤로 가기");
                    System.out.print("  ○ 부서번호 입력 >> ");
                    input = br.readLine();
                    if (input.equals("0")) { dNum = -2; break; }
                    if (!input.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                    dNum = Integer.parseInt(input);
                    if (!dao.checkDeptExists(dNum)) { System.out.println("  ❌ 없는 부서입니다."); continue; }
                    break;
                }
                if (dNum == -2) continue;
            }
            
            while (true) {
                List<Map<String, Object>> list = dao.selectSalaryList(dNum);
                if (list.isEmpty()) { System.out.println("  ❌ 데이터가 없습니다."); break; }
                
                // --- 목록 출력 (UI 유지) ---
                System.out.println("+──────────────────────────────────────────────────────────────────────────────────────────────────+");
                System.out.println("  ● 급여 상세 내역 목록 (등록 완료 건)");
                System.out.println("+──────────────────────────────────────────────────────────────────────────────────────────────────+");
                System.out.println("  " + align("ID", 6) + align("사번", 10) + align("이름", 12) + align("기본급", 14) + 
                	    align("야근수당", 14) + 
                	    align("휴일수당", 14) + 
                	    align("세금", 12) + 
                	    "실수령액");
                System.out.println("+──────────────────────────────────────────────────────────────────────────────────────────────────+");
                for (Map<String, Object> s : list) {
                    System.out.println("  " + align(String.valueOf(s.get("salId")), 6) + align(String.valueOf(s.get("userId")), 8) + align((String)s.get("userName"), 10) + align(String.format("%,d", (int)s.get("salBase")), 14) + align(String.format("%,d", (int)s.get("salOvertime")), 14) + align(String.format("%,d", (int)s.get("salHoliday")), 14) + String.format("%,d", (int)s.get("salTax")) + "원");
                }
                System.out.println("+──────────────────────────────────────────────────────────────────────────────────────────────────+");
                
                System.out.println("1.수정");
                System.out.println("2.삭제");
                System.out.println("3.일괄삭제");
                System.out.println("0.뒤로 가기");
                System.out.print("작업 선택 >> ");
                input = br.readLine();
                if (input.equals("0")) break;
                if (!input.matches("[1-3]")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }

                int act = Integer.parseInt(input);
                if (act == 1 || act == 2) {
                    while (true) {
                    	System.out.println("0. 뒤로 가기");
                        System.out.print("  ○ ID 입력 >> ");
                        String sidStr = br.readLine();
                        if (sidStr.equals("0")) break;
                        if (!sidStr.matches("\\d+")) { System.out.println("  ⚠️ 숫자를 입력하세요."); continue; }
                        
                        int sid = Integer.parseInt(sidStr);
                        boolean isExist = list.stream().anyMatch(m -> (int)m.get("salId") == sid);
                        if (!isExist) { System.out.println("  ❌ 목록에 없는 ID입니다."); continue; }

                        if (act == 1) { // 수정
                            int ot = 0, ho = 0;
                            while(true) {
                                System.out.print("  ○ 변경 야근수당 >> "); String s = br.readLine();
                                if(s.matches("\\d+")) { ot = Integer.parseInt(s); break; }
                                System.out.println("  ⚠️ 숫자를 입력하세요.");
                            }
                            while(true) {
                                System.out.print("  ○ 변경 휴일수당 >> "); String s = br.readLine();
                                if(s.matches("\\d+")) { ho = Integer.parseInt(s); break; }
                                System.out.println("  ⚠️ 숫자를 입력하세요.");
                            }
                            dao.updateSalary(sid, ot, ho); System.out.println("  ✅ 수정 완료!");
                        } else {
                            dao.deleteSalary(sid); System.out.println("  ✅ 삭제 완료!");
                        }
                        break; 
                    }
                } else if (act == 3) {
                    while(true) {
                        System.out.print("  ⚠️ 전체 초기화? (Y/N) : ");
                        String confirm = br.readLine();
                        if (confirm.equalsIgnoreCase("Y")) { dao.deleteSalaryBatch(dNum); break; }
                        else if (confirm.equalsIgnoreCase("N")) break;
                        else System.out.println("  ⚠️ Y 또는 N만 가능합니다.");
                    }
                }
            }
        }
    }
}