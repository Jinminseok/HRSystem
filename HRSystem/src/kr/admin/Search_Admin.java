package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

import kr.hrsystem.dao.SearchDAO;

public class Search_Admin {

	private BufferedReader br;
	private int userId;
	private int loginLogId;
	private SearchDAO dao;

	public Search_Admin(BufferedReader br, int userId, int loginLogId) {
		this.br = br;
		this.userId = userId;
		this.loginLogId = loginLogId;
		this.dao = new SearchDAO();
 
		try {
			callMenu();
		} catch (Exception e) {
			System.out.println("❌ 관리자 검색 메뉴 실행 중 오류가 발생했습니다.");
		}
	}

	private void callMenu() throws IOException {
		while (true) {

			System.out.println();
			System.out.println("+──────────────────────────────────────────+");
			System.out.println("│              🔎 관리자 검색              │");
			System.out.println("+──────────────────────────────────────────+");
			System.out.println("│  [1]. 사번으로 사원 검색                 │");
			System.out.println("│  [2]. 이름으로 사원 검색                 │");
			System.out.println("│  [3]. 부서명으로 사원 검색               │");
			System.out.println("│  [4]. 직급명으로 사원 검색               │");
			System.out.println("│  [5]. 가입일 기간 검색                   │");
			System.out.println("│  [0]. 뒤로가기                           │");
			System.out.println("+──────────────────────────────────────────+");
			System.out.print("선택 >> ");

			int no;
			try {
				no = Integer.parseInt(br.readLine());
			} catch (NumberFormatException e) {
				System.out.println("숫자만 입력하세요.");
				continue;
			}

			switch (no) {
			case 1: {
				Integer uid = readIntWithBack("사번(USER_ID) 입력 (뒤로가기: 0): ");
				if (uid == null) break;

				dao.searchUserByUserId(uid);
				break;
			}

			case 2: {
				String name = readLineWithBack("이름 입력(부분검색, 뒤로가기: 0): ");
				if (name == null) break;

				dao.searchUserByName(name);
				break;
			}

			case 3: {
				List<String> deptNames = dao.getDeptNameList();
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

				String deptName = readLineWithBack("부서명 입력 (뒤로가기: 0): ");
				if (deptName == null) break;

				dao.searchUserByDeptName(deptName.trim());
				break;
			}

			case 4: {
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

				String positionName = readLineWithBack("직급명 입력 (뒤로가기: 0): ");
				if (positionName == null) break;

				dao.searchUserByPositionName(positionName.trim());
				break;
			}

			case 5: {
				System.out.println("날짜 형식: YYYY-MM-DD");

				Date start = readDateWithBack("시작일 (뒤로가기: 0): ");
				if (start == null) break;

				Date end = readDateWithBack("종료일 (뒤로가기: 0): ");
				if (end == null) break;

				if (start.after(end)) {
					System.out.println("❌ 시작일이 종료일보다 늦을 수 없습니다.");
					break;
				}

				dao.searchUserByJoinDateRange(start, end);
				break;
			}

			case 0:
				return;

			default:
				System.out.println("잘못 입력했습니다.");
			}
		}
	}

	private String readLineWithBack(String message) throws IOException {
		System.out.print(message);
		String input = br.readLine();

		if (input == null) return null;

		if ("0".equals(input.trim())) {

			return null;
		}

		return input;
	}

	private Integer readIntWithBack(String message) throws IOException {
		System.out.print(message);
		String input = br.readLine();

		if (input == null) return null;

		input = input.trim();

		if ("0".equals(input)) {

			return null;
		}

		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			System.out.println("숫자만 입력하세요.");
			return null;
		}
	}

	private Date readDateWithBack(String message) throws IOException {
		System.out.print(message);
		String input = br.readLine();

		if (input == null) return null;

		input = input.trim();

		if ("0".equals(input)) {
			
			return null;
		}

		try {
			return Date.valueOf(input);
		} catch (IllegalArgumentException e) {
			System.out.println("❌ 잘못된 날짜 형식입니다. (예: 2026-02-26)");
			return null;
		}
	}
}