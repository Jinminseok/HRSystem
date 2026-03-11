package kr.employee;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.MyInfoDAO;

public class MyInfoUpdate_employee {

	private BufferedReader br;
	private int userId;
	private int loginLogId; // 지금은 안 써도 나중 로그용으로 받아둠
	private MyInfoDAO myInfoDao;

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
					String newPw = readRequiredOrCancel("새 비밀번호");
					if (newPw == null) {
						System.out.println("↩ 비밀번호 수정이 취소되었습니다.");
						break; // 메뉴로
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
					String newEmail = readRequiredOrCancel("새 이메일");
					if (newEmail == null) {
						System.out.println("↩ 이메일 수정이 취소되었습니다.");
						break; // 메뉴로
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
					String newPhone = readRequiredOrCancel("새 전화번호");
					if (newPhone == null) {
						System.out.println("↩ 전화번호 수정이 취소되었습니다.");
						break; // 메뉴로
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
					return;

				default:
					System.out.println("잘못 입력했습니다.");
				}

			} catch (NumberFormatException e) {
				System.out.println("숫자만 입력하세요.");
			}
		}
	}
	//0누를시 취소
	private String readRequiredOrCancel(String label) throws IOException {
		System.out.print(label + " (뒤로가기 : 0) : ");
		String input = br.readLine();
		if (input == null) return null;

		input = input.trim();
		if ("0".equals(input)) return null;  
		return input; 
	}

	
	private String readOptionalOrCancel(String label) throws IOException {
		System.out.print(label + " (뒤로가기: 0) : ");
		String input = br.readLine();
		if (input == null) return null;

		input = input.trim();
		if ("0".equals(input)) return null;  
		return input; 
	}
}