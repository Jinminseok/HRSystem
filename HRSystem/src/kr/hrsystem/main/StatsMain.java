package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.PositionDAO;
import kr.hrsystem.dao.StatsDAO;
import kr.hrsystem.dao.salary_DAO;

public class StatsMain {
    private StatsDAO sDao = new StatsDAO();
    private salary_DAO salDao = new salary_DAO();
    private PositionDAO positionDao = new PositionDAO();
    private BufferedReader br;

    public StatsMain(BufferedReader br, int userId, int loginLogId) {
        this.br = br;
        runStatsMenu();
    }

    public void runStatsMenu() {
        while (true) {
        	System.out.println("+──────────────────────────────────────────+");
        	System.out.println("│           기업 통합 통계 서비스          │");
        	System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  1. 인사 통계                            │");
            System.out.println("│  2. 근태 통계                            │");
            System.out.println("│  3. 급여 통계                            │");
            System.out.println("│  0. 뒤로가기                             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 > ");
            try {
                String input = br.readLine();
                if (input == null || input.equals("0")) return;
                if (input.equals("1")) showWorkStatusMenu();
                else if (input.equals("2")) showAttendanceMenu();
                else if (input.equals("3")) showSalaryMenu();
            } catch (Exception e) { System.out.println("  ⚠️ 잘못된 입력입니다."); }
        }
    }

    private void showWorkStatusMenu() throws IOException {
        while (true) {            
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│              인사 통계 검색              │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  1. 전체 검색                            │");
            System.out.println("│  2. 부서 검색                            │");
            System.out.println("│  3. 직급 검색                            │");
            System.out.println("│  0. 뒤로가기                             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 > ");
            String input = br.readLine();
            if (input == null || input.equals("0")) return;
            int type = Integer.parseInt(input);
            String typeVal = "";
            if (type == 2) {
            		salDao.showDepartmentList(); 
            		System.out.println("0. 뒤로가기");
            		System.out.print("○ 부서명 >> "); 
            		typeVal = br.readLine();
            		if ("0".equals(typeVal)) continue;
            		}
            else if (type == 3) {
            	positionDao.selectPosition();
            	System.out.println("0. 뒤로가기");
            	System.out.print("○ 직급명 >> "); 
            	typeVal = br.readLine();
            	if ("0".equals(typeVal)) continue;
            	}
            

            while (true) {
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│              인사 통계 검색              │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  1. 전체 검색                            │");
                System.out.println("│  2. 월별 검색                            │");
                System.out.println("│  3. 년도별 검색                          │");
                System.out.println("│  4. 기간별 검색                          │");
                System.out.println("│  0. 뒤로가기                             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 > ");                
                input = br.readLine();
                if (input == null || input.equals("99")) return;
                if (input.equals("0")) break;
                int dateType = Integer.parseInt(input);
                String s = "", e = "";
                if (dateType == 2) { System.out.println("  ○ 월(YYYY-MM) >> "); System.out.print("0: 뒤로");
                					s = br.readLine(); 
                					if ("0".equals(s)) continue;}
                else if (dateType == 3) { System.out.println("  ○ 년도(YYYY) >> "); System.out.print("0: 뒤로"); 
                					s = br.readLine(); 
                					if ("0".equals(s)) continue;}
                else if (dateType == 4) { System.out.println("  ○ 시작(YYYY-MM-DD) >> "); System.out.println("0: 뒤로");
                					s = br.readLine(); 
                					if ("0".equals(s)) continue;
                					System.out.println("  ○ 종료(YYYY-MM-DD) >> "); System.out.print("0: 뒤로");
                					e = br.readLine(); 
                					if ("0".equals(e)) continue;}
                sDao.showWorkStatusStats(type, typeVal, dateType, s, e);
                System.out.print("\n  0: 뒤로");
                while(true) {
                    String back = br.readLine();
                    if ("0".equals(back)) break; // 안쪽 while문 처음(기간 선택)으로 이동                    
                    System.out.print("  ⚠️ 0번만 입력 가능합니다 > ");
                }
            }
        }
    }

    private void showAttendanceMenu() throws IOException {
        while (true) {
        	System.out.println("+──────────────────────────────────────────+");
            System.out.println("│              근태 통계 검색              │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  1. 전체 검색                            │");
            System.out.println("│  2. 부서 검색                            │");
            System.out.println("│  3. 직급 검색                            │");
            System.out.println("│  0. 뒤로가기                             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 > ");
            String input = br.readLine();
            if (input == null || input.equals("0")) return;
            int type = Integer.parseInt(input);
            String typeVal = "";
            if (type == 2) { 
            	salDao.showDepartmentList(); 
            	System.out.println("0. 뒤로가기");
            	System.out.print("○ 부서명 >> "); 
            	typeVal = br.readLine(); 
            	if ("0".equals(typeVal)) continue;
            	}
            else if (type == 3) { 
            	positionDao.selectPosition(); 
            	System.out.println("0. 뒤로가기");
            	System.out.print("○ 직급명 >> "); 
            	typeVal = br.readLine(); 
            	if ("0".equals(typeVal)) continue;
            	}

            while (true) {
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│              근태 통계 검색              │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  1. 전체 검색                            │");
                System.out.println("│  2. 일별 검색                            │");
                System.out.println("│  3. 월별 검색                            │");
                System.out.println("│  4. 년도별 검색                          │");
                System.out.println("│  5. 기간별 검색                          │");
                System.out.println("│  0. 뒤로가기                             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 > "); 
                input = br.readLine();
                if (input == null || input.equals("99")) return;
                if (input.equals("0")) break;
                int dateType = Integer.parseInt(input);
                String s = "", e = "";
                if (dateType == 2) { System.out.println("  ○ 날짜(YYYY-MM-DD) >> "); System.out.print("0: 뒤로");
                						s = br.readLine(); 
                						if ("0".equals(s)) continue;}
                else if (dateType == 3) { System.out.println("  ○ 월(YYYY-MM) >> "); System.out.print("0: 뒤로");
                						s = br.readLine(); 
                						if ("0".equals(s)) continue;}
                else if (dateType == 4) { System.out.println("  ○ 년도(YYYY) >> "); System.out.print("0: 뒤로");
                						s = br.readLine(); 
                						if ("0".equals(s)) continue;}
                else if (dateType == 5) { System.out.println("  ○ 시작(YYYY-MM-DD) >> "); System.out.println("0: 뒤로"); 
                						s = br.readLine();
                						if ("0".equals(s)) continue;
                						System.out.println("  ○ 종료(YYYY-MM-DD) >> "); System.out.print("0: 뒤로");
                						e = br.readLine(); 
                						if ("0".equals(e)) continue;}
                sDao.showAttendanceStats(type, typeVal, dateType, s, e);
                System.out.print("\n  0: 뒤로");
                while(true) {
                    String back = br.readLine();
                    if ("0".equals(back)) break; // 안쪽 while문 처음(기간 선택)으로 이동                    
                    System.out.print("  ⚠️ 0번만 입력 가능합니다 > ");
                }
            }
        }
    }

    private void showSalaryMenu() throws IOException {
        while (true) {
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│              급여 통계 검색              │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  1. 전체 검색                            │");
            System.out.println("│  2. 부서 검색                            │");
            System.out.println("│  3. 직급 검색                            │");
            System.out.println("│  0. 뒤로가기                             │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 > ");
            String input = br.readLine();
            if (input == null || input.equals("0")) return;
            int type = Integer.parseInt(input);
            String typeVal = "";
            if (type == 2) { 
            	salDao.showDepartmentList(); 
            	System.out.println("0. 뒤로가기");
            	System.out.print("○ 부서명 >> "); 
            	typeVal = br.readLine(); 
            	if ("0".equals(typeVal)) continue;
            	}
            else if (type == 3) { 
            	positionDao.selectPosition(); 
            	System.out.println("0. 뒤로가기");
            	System.out.print("○ 직급명 >> "); 
            	typeVal = br.readLine(); 
            	if ("0".equals(typeVal)) continue;
            	}

            while (true) {                
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│              인사 통계 검색              │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.println("│  1. 전체 검색                            │");
                System.out.println("│  2. 월별 검색                            │");
                System.out.println("│  3. 년도별 검색                          │");
                System.out.println("│  4. 기간별 검색                          │");
                System.out.println("│  0. 뒤로가기                             │");
                System.out.println("+──────────────────────────────────────────+");
                System.out.print("선택 > ");  
                input = br.readLine();
                if (input == null || input.equals("99")) return;
                if (input.equals("0")) break;
                int dateType = Integer.parseInt(input);
                String s = "", e = "";
                if (dateType == 2) { System.out.println("  ○ 월(YYYY-MM) >> "); System.out.print("0: 뒤로");
                					s = br.readLine(); 
                					if ("0".equals(s)) continue;}
                else if (dateType == 3) { System.out.println("  ○ 년도(YYYY) >> "); System.out.print("0: 뒤로");
                					s = br.readLine(); 
                					if ("0".equals(s)) continue;}
                else if (dateType == 4) { System.out.println("  ○ 시작(YYYY-MM-DD) >> "); System.out.println("0: 뒤로"); 
                					s = br.readLine(); 
                					if ("0".equals(s)) continue;
                					System.out.println("  ○ 종료 >> "); System.out.print("0: 뒤로");
                					e = br.readLine(); 
                					if ("0".equals(e)) continue;}
                sDao.showSalaryStats(type, typeVal, dateType, s, e);
                System.out.print("\n  0: 뒤로");
                while(true) {
                    String back = br.readLine();
                    if ("0".equals(back)) break; // 안쪽 while문 처음(기간 선택)으로 이동                    
                    System.out.print("  ⚠️ 0번만 입력 가능합니다 > ");
                }
            }
        }
    }
}