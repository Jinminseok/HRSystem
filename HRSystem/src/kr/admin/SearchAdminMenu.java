package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

import kr.hrsystem.dao.SearchDAO;

public class SearchAdminMenu {

    private BufferedReader br;
    private int userId;
    private int loginLogId;
    private SearchDAO dao;

    public SearchAdminMenu(BufferedReader br, int userId, int loginLogId) {
        this.br = br;
        this.userId = userId;
        this.loginLogId = loginLogId;
        this.dao = new SearchDAO();

        try {
            callMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callMenu() throws IOException {
        while (true) {

            System.out.println();
            System.out.println("┌──────────────────────────────────────────────");
            System.out.println("│               🔎 관리자 검색                ");
            System.out.println("├──────────────────────────────────────────────");
            System.out.println("│  1. 사번으로 사원 검색              ");
            System.out.println("│  2. 이름으로 사원 검색   ");
            System.out.println("│  3. 부서명으로 사원 검색          ");
            System.out.println("│  4. 직급명으로 사원 검색      ");
            System.out.println("│  5. 가입일 기간 검색              ");
            System.out.println("│  0. 뒤로가기                                 ");
            System.out.println("└──────────────────────────────────────────────");
            System.out.print("선택 >> ");

            int no;
            try {
                no = Integer.parseInt(br.readLine());
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
                continue;
            }

            switch (no) {
                case 1:
                    System.out.print("사번(USER_ID) 입력: ");
                    int uid = Integer.parseInt(br.readLine());
                    dao.searchUserByUserId(uid);
                    break;

                case 2:
                    System.out.print("이름 입력(부분검색): ");
                    String name = br.readLine();
                    dao.searchUserByName(name);
                    break;

                case 3: {
                    SearchDAO deptDao = new SearchDAO();

                    // 1) 부서명 목록 출력
                    List<String> deptNames = deptDao.getDeptNameList();
                    if (deptNames.isEmpty()) {
                        System.out.println("등록된 부서가 없습니다.");
                        break;
                    }

                    System.out.println();
                    System.out.println("[현재 등록된 부서 목록]");
                    for (int i = 0; i < deptNames.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, deptNames.get(i));
                    }
                    System.out.println();

                    // 2) 입력 받기
                    System.out.print("부서명 입력: ");
                    String deptName = br.readLine().trim();

                    // 3) 기존 부서명 검색 기능 호출
                    dao.searchUserByDeptName(deptName);  // 
                    break;}
                

                case 4: {
                    SearchDAO dao = new SearchDAO(); // 

                    // 1) 직급명 목록 출력
                    List<String> posNames = dao.getPositionNameList();
                    if (posNames.isEmpty()) {
                        System.out.println("등록된 직급이 없습니다.");
                        break;
                    }

                    System.out.println();
                    System.out.println("[현재 등록된 직급 목록]");
                    for (int i = 0; i < posNames.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, posNames.get(i));
                    }
                    System.out.println();

                    // 2) 입력 받기
                    System.out.print("직급명 입력: ");
                    String positionName = br.readLine().trim();

                    // 3) 기존 직급명 검색 호출
                    dao.searchUserByPositionName(positionName); //
                    break;
                }

                case 5:

                    System.out.println("날짜 형식: YYYY-MM-DD");

                    Date start = null;
                    Date end = null;

                    try {
                        System.out.print("시작일: ");
                        String startStr = br.readLine().trim();
                        start = Date.valueOf(startStr);

                        System.out.print("종료일: ");
                        String endStr = br.readLine().trim();
                        end = Date.valueOf(endStr);

                    } catch (IllegalArgumentException e) {
                        System.out.println("❌ 잘못된 날짜 형식입니다. (예: 2026-02-26)");
                        break;  // case 종료
                    }

                    

                    dao.searchUserByJoinDateRange(start, end);
                    break;

                case 0:
                    return;

                default:
                    System.out.println("잘못 입력했습니다.");
            }
        }
    }
}