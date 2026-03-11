package kr.employee;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.OrgChartDAO;

public class OrgChart_Employee {

    private BufferedReader br;
    private OrgChartDAO dao;

    // 조직도 조회 화면에 필요한 객체를 생성하고 메뉴 실행
    public OrgChart_Employee(BufferedReader br) {
        this.br = br;
        this.dao = new OrgChartDAO();

        try {
            menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

    // 조직도 조회 메뉴 화면
    private void menu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│              🏢 조직도 조회              │");
            System.out.println("├──────────────────────────────────────────+");
            System.out.println("│  [1] 전체 조직도 조회                    │");
            System.out.println("│  [2] 부서별 조직도 조회                  │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("└──────────────────────────────────────────+");
            System.out.print("선택 : ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        // 전체 조직도 조회
                        dao.selectAllOrgChart();
                        break;

                    case 2:
                        // 부서명을 입력받아 해당 부서 조직도 조회
                    	dao.printDeptGuide();

                        System.out.print("부서명 입력(뒤로가기: 0) : ");
                        String keyword = br.readLine().trim();

                        if ("0".equals(keyword)) {
                            System.out.println("↩ 부서별 조회를 취소했습니다.");
                            break;
                        }
                        if (keyword.isEmpty()) {
                            System.out.println("❌ 검색어를 입력하세요.");
                            break;
                        }
                        dao.selectOrgChartByDeptName(keyword);
                        break;

                    case 0:
                        // 이전 화면으로 이동
                        return;

                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (NumberFormatException e) {
                // 숫자가 아닌 값 입력 시 예외 처리
                System.out.println("숫자만 입력하세요.");
            }
        }
    }
}