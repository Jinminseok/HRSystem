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
            System.out.println("HR SYSTEM");
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

    // 로그인 (DTO 없이 Map 사용)
    private void loginScreen() throws IOException {

        System.out.print("아이디 : ");
        String id = br.readLine().trim();

        System.out.print("비밀번호 : ");
        String pw = br.readLine().trim();

        // ✅ Map으로 받기
        Map<String, Object> u = dao.loginAsMap(id, pw);

        // 1) 계정 불일치
        if (u == null) {
            logDao.insertLoginHistory(null, id, "F", "아이디 또는 비밀번호 불일치");
            System.out.println("❌ 아이디 또는 비밀번호가 틀렸습니다.");
            return;
        }

        int userId = (int) u.get("USER_ID");
        String userName = (String) u.get("USER_NAME");
        String approvalStatus = (String) u.get("APPROVAL_STATUS");
        String userRole = (String) u.get("USER_ROLE");

        // 2) 승인 안 된 계정
        if (!"APPROVED".equalsIgnoreCase(approvalStatus)) {
            logDao.insertLoginHistory(userId, id, "F", "승인대기 또는 승인거절 상태");
            System.out.println("⛔ 관리자 승인 후 로그인 가능합니다. (현재 상태: " + approvalStatus + ")");
            return;
        }

        // 3) 로그인 성공
        int loginLogId = logDao.insertLoginHistory(userId, id, "S", null);
        System.out.println("✅ 로그인 성공! (" + userName + ")");

        // 4) 권한 분기
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            new AdminScreen(br, userId, loginLogId);
        } else {
            new EmployeeScreen(br, userId, loginLogId);
        }
    }

    // 회원가입
    private void signScreen() throws IOException {

        System.out.print("아이디 : ");
        String id = br.readLine().trim();

        if (id.isEmpty()) {
            System.out.println("❌ 아이디를 입력하세요.");
            return;
        }

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