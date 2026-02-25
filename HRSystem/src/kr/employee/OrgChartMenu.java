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
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│              🏢 조직도 조회                 ");
            System.out.println("├─────────────────────────────────────────────");
            System.out.println("│  1. 전체 조직도 조회                        ");
            System.out.println("│  2. 부서별 조직도 조회                      ");
            System.out.println("│  0. 뒤로가기                                ");
            System.out.println("└─────────────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        dao.selectAllOrgChart();
                        break;

                    case 2:
                        System.out.print("조회할 부서명 입력 (예: 개발부) : ");
                        String deptName = br.readLine();
                        dao.selectOrgChartByDeptName(deptName);
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