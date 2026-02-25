package kr.employee;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.MyInfoDAO;

public class MyInfoUpdateMenu {

    private BufferedReader br;
    private int userId;
    private int loginLogId; // 지금은 안 써도 나중 로그용으로 받아둠
    private MyInfoDAO myInfoDao;

    public MyInfoUpdateMenu(BufferedReader br, int userId, int loginLogId) {
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
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│              ✏ 내 정보 수정                 ");
            System.out.println("├─────────────────────────────────────────────");
            System.out.println("│  1. 비밀번호 수정                           ");
            System.out.println("│  2. 이메일 수정                             ");
            System.out.println("│  3. 전화번호 수정                           ");
            System.out.println("│  4. 한 번에 수정(비밀번호/이메일/전화번호)  ");
            System.out.println("│  0. 뒤로가기                               ");
            System.out.println("└─────────────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        System.out.print("새 비밀번호 : ");
                        String newPw = br.readLine();

                        if (newPw == null || newPw.trim().isEmpty()) {
                            System.out.println("❌ 비밀번호를 입력하세요.");
                            break;
                        }

                        if (myInfoDao.updatePassword(userId, newPw) > 0) {
                            System.out.println("✅ 비밀번호가 수정되었습니다.");
                        } else {
                            System.out.println("❌ 비밀번호 수정 실패");
                        }
                        break;

                    case 2:
                        System.out.print("새 이메일 : ");
                        String newEmail = br.readLine();

                        if (newEmail == null || newEmail.trim().isEmpty()) {
                            System.out.println("❌ 이메일을 입력하세요.");
                            break;
                        }

                        if (myInfoDao.updateEmail(userId, newEmail) > 0) {
                            System.out.println("✅ 이메일이 수정되었습니다.");
                        } else {
                            System.out.println("❌ 이메일 수정 실패");
                        }
                        break;

                    case 3:
                        System.out.print("새 전화번호 : ");
                        String newPhone = br.readLine();

                        if (newPhone == null || newPhone.trim().isEmpty()) {
                            System.out.println("❌ 전화번호를 입력하세요.");
                            break;
                        }

                        if (myInfoDao.updatePhone(userId, newPhone) > 0) {
                            System.out.println("✅ 전화번호가 수정되었습니다.");
                        } else {
                            System.out.println("❌ 전화번호 수정 실패");
                        }
                        break;

                    case 4:
                        System.out.println("※ 변경하지 않을 항목은 그냥 엔터");
                        System.out.print("새 비밀번호 : ");
                        String pw = br.readLine();

                        System.out.print("새 이메일 : ");
                        String email = br.readLine();

                        System.out.print("새 전화번호 : ");
                        String phone = br.readLine();

                        int result = myInfoDao.updateMyInfo(userId, pw, email, phone);

                        if (result > 0) {
                            System.out.println("✅ 내 정보가 수정되었습니다.");
                        } else {
                            System.out.println("❌ 수정할 항목이 없거나 수정 실패");
                        }
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