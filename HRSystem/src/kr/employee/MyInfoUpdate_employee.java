package kr.employee;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.MyInfoDAO;

public class MyInfoUpdate_employee {

	private BufferedReader br;
	private int userId;
	private int loginLogId; // 로그인 정보(추후 로그 기록 등에 사용 가능)
	private MyInfoDAO myInfoDao;

	// 내 정보 수정 화면에 필요한 값들을 전달받아 메뉴 실행
	public MyInfoUpdate_employee(BufferedReader br, int userId, int loginLogId) {
		this.br = br;
		this.userId = userId;
		this.loginLogId = loginLogId;
		this.myInfoDao = new MyInfoDAO();
 
		try {
			menu();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 내 정보 수정 메뉴 화면
	private void menu() throws IOException {
		while (true) {
			System.out.println();
			System.out.println("+──────────────────────────────────────────+");
			System.out.println("│              ✏ 내 정보 수정              │");
			System.out.println("├──────────────────────────────────────────│");
			System.out.println("│  [1] 비밀번호 수정                       │");
			System.out.println("│  [2] 이메일 수정                         │ ");
			System.out.println("│  [3] 전화번호 수정                       │ ");
			System.out.println("│  [4] 한 번에 수정                        │");
			System.out.println("│  [0] 뒤로가기                            │");
			System.out.println("└──────────────────────────────────────────│");
			System.out.print("선택 : ");

			try {
				int no = Integer.parseInt(br.readLine());

				switch (no) {
				case 1: {
					// 비밀번호 수정
					String newPw = readRequiredOrCancel("새 비밀번호");
					if (newPw == null) {
						System.out.println("↩ 비밀번호 수정이 취소되었습니다.");
						break;
					}
					if (newPw.isEmpty()) {
						System.out.println("❌ 비밀번호를 입력하세요.");
						break;
					}

					if (myInfoDao.updatePassword(userId, newPw) > 0) {
						System.out.println("✅ 비밀번호가 수정되었습니다.");
					} else {
						System.out.println("❌ 비밀번호 수정 실패");
					}
					break;
				}

				case 2: {
					// 이메일 수정
					String newEmail = readRequiredOrCancel("새 이메일");
					if (newEmail == null) {
						System.out.println("↩ 이메일 수정이 취소되었습니다.");
						break;
					}
					if (newEmail.isEmpty()) {
						System.out.println("❌ 이메일을 입력하세요.");
						break;
					}

					if (myInfoDao.updateEmail(userId, newEmail) > 0) {
						System.out.println("✅ 이메일이 수정되었습니다.");
					} else {
						System.out.println("❌ 이메일 수정 실패");
					}
					break;
				}

				case 3: {
					// 전화번호 수정
					String newPhone = readRequiredOrCancel("새 전화번호");
					if (newPhone == null) {
						System.out.println("↩ 전화번호 수정이 취소되었습니다.");
						break;
					}
					if (newPhone.isEmpty()) {
						System.out.println("❌ 전화번호를 입력하세요.");
						break;
					}

					if (myInfoDao.updatePhone(userId, newPhone) > 0) {
						System.out.println("✅ 전화번호가 수정되었습니다.");
					} else {
						System.out.println("❌ 전화번호 수정 실패");
					}
					break;
				}

				case 4: {
					// 비밀번호, 이메일, 전화번호를 한 번에 수정
					String pw = readOptionalOrCancel("새 비밀번호");
					if (pw == null) {
						System.out.println("↩ 수정이 취소되었습니다.");
						break;
					}

					String email = readOptionalOrCancel("새 이메일");
					if (email == null) {
						System.out.println("↩ 수정이 취소되었습니다.");
						break;
					}

					String phone = readOptionalOrCancel("새 전화번호");
					if (phone == null) {
						System.out.println("↩ 수정이 취소되었습니다.");
						break;
					}

					int result = myInfoDao.updateMyInfo(userId, pw, email, phone);

					if (result > 0) {
						System.out.println("✅ 내 정보가 수정되었습니다.");
					} else {
						System.out.println("❌ 수정할 항목이 없거나 수정 실패");
					}
					break;
				}

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

	// 필수 입력값을 받되, 0 입력 시 취소
	private String readRequiredOrCancel(String label) throws IOException {
		System.out.print(label + " (뒤로가기 : 0) : ");
		String input = br.readLine();
		if (input == null) return null;

		input = input.trim();
		if ("0".equals(input)) return null;
		return input;
	}

	// 선택 입력값을 받되, 0 입력 시 취소
	private String readOptionalOrCancel(String label) throws IOException {
		System.out.print(label + " (뒤로가기: 0) : ");
		String input = br.readLine();
		if (input == null) return null;

		input = input.trim();
		if ("0".equals(input)) return null;
		return input;
	}
}