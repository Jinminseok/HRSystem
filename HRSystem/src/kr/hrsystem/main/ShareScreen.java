package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import kr.hrsystem.dao.LogDAO;
import kr.hrsystem.dao.LoginDAO;

public class ShareScreen {

	private BufferedReader br;
	private LoginDAO dao;
	private LogDAO logDao;
	private static final String LINE = "───────────────────────────────────────────";

	// 프로그램 시작 시 메뉴 화면 실행
	public ShareScreen() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			dao = new LoginDAO();
			logDao = new LogDAO();
			shareMenu();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ShareScreen();
	}

	// 첫 화면: 로그인 / 회원가입 / 종료 메뉴
	private void shareMenu() throws IOException {

		while (true) {
			System.out.println("+──────────────────────────────────────────+");
			System.out.println("│               HR SYSTEM                  │");
			System.out.println("+──────────────────────────────────────────+");
			System.out.println("│  1. 로그인                               │");
			System.out.println("│  2. 회원가입                             │");
			System.out.println("│  3. 종료                                 │");
			System.out.println("+──────────────────────────────────────────+");
			System.out.print("선택 >> ");
			
			try {
				int no = Integer.parseInt(br.readLine());

				switch (no) {
				case 1:
					loginScreen();
					break;
				case 2:
					signScreen();
					break;
				case 3:
					System.out.println("프로그램이 종료되었습니다.");
					return;
				default:
					System.out.println("잘못 입력했습니다.");
				}

			} catch (NumberFormatException e) {
				System.out.println("숫자만 입력하세요.");
			}
		}
	}

	// 로그인 처리 후 권한에 따라 관리자/사원 화면으로 이동
	private void loginScreen() throws IOException {

		System.out.print("아이디 : ");
		String id = br.readLine().trim();

		System.out.print("비밀번호 : ");
		String pw = br.readLine().trim();

		Map<String, Object> u = dao.loginAsMap(id, pw);

		// 계정 정보가 없으면 로그인 실패
		if (u == null) {
			logDao.insertLoginHistory(null, id, "F", "아이디 또는 비밀번호 불일치");
			System.out.println("❌ 아이디 또는 비밀번호가 틀렸습니다.");
			return;
		}

		int userId = (int) u.get("USER_ID");
		String userName = (String) u.get("USER_NAME");
		String approvalStatus = (String) u.get("APPROVAL_STATUS");
		String userRole = (String) u.get("USER_ROLE");
		String empStatus = (String) u.get("EMP_STATUS");

		// 승인되지 않은 계정은 로그인 불가
		if (!"APPROVED".equalsIgnoreCase(approvalStatus)) {
			logDao.insertLoginHistory(userId, id, "F", "승인대기 또는 승인거절 상태");
			System.out.println("⛔ 관리자 승인 후 로그인 가능합니다. (현재 상태: " + approvalStatus + ")");
			return;
		}

		// 퇴직 상태 계정은 로그인 불가
		if ("RESIGNED".equalsIgnoreCase(empStatus)) {
			logDao.insertLoginHistory(userId, id, "F", "퇴직 상태 계정 로그인 시도");
			System.out.println("⛔ 퇴직 처리된 계정은 로그인할 수 없습니다.");
			return;
		}

		// 로그인 성공 이력 저장
		int loginLogId = logDao.insertLoginHistory(userId, id, "S", null);

		System.out.println();
		System.out.println("✅ 로그인 성공!");
		System.out.println(userName + "님 환영합니다.");
		System.out.println(LINE);

		// 권한별 화면 분기
		if ("ADMIN".equalsIgnoreCase(userRole)) {
			new AdminScreen(br, userId, loginLogId);
		} else {
			new EmployeeScreen(br, userId, loginLogId);
		}
	}

	// 회원가입 처리
	private void signScreen() throws IOException {

		System.out.print("아이디 : ");
		String id = br.readLine().trim();

		if (id.isEmpty()) {
			System.out.println("❌ 아이디를 입력하세요.");
			return;
		}

		// 아이디 중복 체크
		if (dao.existsLoginId(id)) {
			System.out.println("❌ 이미 사용 중인 아이디입니다. 다른 아이디를 입력하세요.");
			return;
		}

		System.out.print("비밀번호 : ");
		String pw = br.readLine().trim();

		System.out.print("이름 : ");
		String name = br.readLine().trim();

		System.out.print("이메일 : ");
		String email = br.readLine().trim();

		System.out.print("전화번호 : ");
		String phone = br.readLine().trim();

		int result = dao.insertUser(id, pw, name, email, phone);

		if (result > 0) {
			System.out.println("✅ 회원가입 완료! (관리자 승인 후 로그인 가능)");
			System.out.println(LINE);
		} else if (result == -1) {
			System.out.println("❌ 이미 사용 중인 아이디입니다. 다른 아이디를 입력하세요.");
		} else {
			System.out.println("❌ 회원가입 실패");
		}
	}
}