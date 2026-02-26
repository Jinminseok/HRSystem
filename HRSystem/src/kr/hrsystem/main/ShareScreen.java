package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.hrsystem.dao.LogDAO;
import kr.hrsystem.dao.LoginDAO;
import kr.hrsystem.user.LoginUser;


public class ShareScreen {

    private BufferedReader br;
    private LoginDAO dao;
    private LogDAO logDao;

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

    private void shareMenu() throws IOException {

        while (true) {
            System.out.println("=".repeat(35));
            System.out.println("🏢 인사관리 시스템");
            System.out.println("=".repeat(35));
            System.out.println("1. 로그인");
            System.out.println("2. 회원가입");
            System.out.println("3. 종료");
            System.out.println("=".repeat(35));
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
                        System.out.println("프로그램 종료");
                        return;
                    default:
                        System.out.println("잘못 입력했습니다.");
                }

            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
            }
        }
    }

    // 로그인
    private void loginScreen() throws IOException {

        System.out.print("아이디 : ");
        String id = br.readLine();

        System.out.print("비밀번호 : ");
        String pw = br.readLine();

        LoginUser loginUser = dao.login(id, pw);

        // 1) 계정 불일치
        if (loginUser == null) {
            logDao.insertLoginHistory(null, id, "F", "아이디 또는 비밀번호 불일치");
            System.out.println("❌ 아이디 또는 비밀번호가 틀렸습니다.");
            return;
        }

        // 2) 승인 안 된 계정
        if (!loginUser.isApproved()) {
            logDao.insertLoginHistory(loginUser.getUserId(), id, "F", "승인대기 또는 승인거절 상태");
            System.out.println("⛔ 관리자 승인 후 로그인 가능합니다. (현재 상태: " + loginUser.getApprovalStatus() + ")");
            return;
        }

        // 3) 로그인 성공
        int loginLogId = logDao.insertLoginHistory(loginUser.getUserId(), id, "S", null);
        System.out.println("✅ 로그인 성공! (" + loginUser.getUserName() + ")");

        // 4) 권한 분기
        if (loginUser.isAdmin()) {
            new AdminScreen(br, loginUser.getUserId(), loginLogId);
        } else {
            new EmployeeScreen(br, loginUser.getUserId(), loginLogId);
        }
    }

 // 회원가입
    private void signScreen() throws IOException {

        System.out.print("아이디 : ");
        String id = br.readLine().trim();

        // ✅ 빈값 체크
        if (id.isEmpty()) {
            System.out.println("❌ 아이디를 입력하세요.");
            return;
        }

        // ✅ 미리 중복 확인 (사용자 경험 향상)
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
        } else if (result == -1) {
            System.out.println("❌ 이미 사용 중인 아이디입니다. 다른 아이디를 입력하세요.");
        } else {
            System.out.println("❌ 회원가입 실패");
        }
    }
}
