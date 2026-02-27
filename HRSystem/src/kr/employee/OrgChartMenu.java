package kr.employee;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.OrgChartDAO;

public class OrgChartMenu {

    private BufferedReader br;
    private OrgChartDAO dao;

    public OrgChartMenu(BufferedReader br) {
        this.br = br;
        this.dao = new OrgChartDAO();

        try {
            menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void menu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│              🏢 조직도 조회              │");
            System.out.println("├──────────────────────────────────────────+");
            System.out.println("│  1. 전체 조직도 조회                     │");
            System.out.println("│  2. 부서별 조직도 조회                   │");
            System.out.println("│  0. 뒤로가기                             │");
            System.out.println("└──────────────────────────────────────────+");
            System.out.print("선택 : ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        dao.selectAllOrgChart();
                        break;

                    case 2:
                    	 dao.printDeptGuide();

                         System.out.print("부서명 입력(취소: 0) : ");
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
                        return;

                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
            }
        }
    }
}