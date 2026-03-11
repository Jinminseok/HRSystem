package kr.employee;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.NoticeDAO;


public class Notice_Employee {

    private BufferedReader br;
    private NoticeDAO dao;
    private int userId;
    private Integer loginLogId; // ✅ null 허용

    public Notice_Employee(BufferedReader br, int userId, Integer loginLogId) {
        this.br = br;
        this.userId = userId;
        this.loginLogId = loginLogId;
        this.dao = new NoticeDAO();
 
        try {
            noticeEmployeeMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void noticeEmployeeMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│              📢 게시판 (사원)            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 게시글 작성                         │ ");
            System.out.println("│  [2] 전체 게시글 조회                    │");
            System.out.println("│  [3] 내 게시글 조회                      │");
            System.out.println("│  [4] 게시글 상세 조회                    │");
            System.out.println("│  [5] 투표 참여 (찬성/반대)               │");
            System.out.println("│  [6] 내 게시글 수정                      │");
            System.out.println("│  [7] 내 게시글 삭제                      │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 : ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                    	System.out.println("\n[ 게시글 작성 ]");
                        String title = readRequiredOrCancel("제목");
                        if (title == null) {
                            System.out.println("↩ 게시글 작성이 취소되었습니다.");
                            break;
                        }
                        if (title.isEmpty()) {
                            System.out.println("❌ 제목을 입력하세요.");
                            break;
                        }
                        String content = readRequiredOrCancel("내용");
                        if (content == null) {
                            System.out.println("↩ 게시글 작성이 취소되었습니다.");
                            break;
                        }
                        if (content.isEmpty()) {
                            System.out.println("❌ 내용을 입력하세요.");
                            break;
                        }

                        // 사원 작성글은 기본: 고정X, 투표X
                        dao.insertNotice(title, content, userId, "N", "N", null, loginLogId);
                        break;

                    case 2:
                        dao.selectNoticeList();
                        break;

                    case 3:
                        dao.selectMyNoticeList(userId);
                        break;

                    case 4:
                    	dao.selectNoticeList();
                        Integer noticeId = readIntOrCancel("조회할 공지번호");
                        if (noticeId == null) {
                            System.out.println("↩ 상세 조회가 취소되었습니다.");
                            break;
                        }
                        dao.selectNoticeDetail(noticeId);
                        break;

                    case 5:
                    	dao.selectNoticeList();
                        Integer voteNoticeId = readIntOrCancel("투표할 공지번호");
                        if (voteNoticeId == null) {
                            System.out.println("↩ 투표가 취소되었습니다.");
                            break;
                        }
                        String choice = readVoteChoiceOrCancel();
                        if (choice == null) {
                            System.out.println("↩ 투표가 취소되었습니다.");
                            break;
                        }
                        dao.voteNotice(voteNoticeId, userId, choice, loginLogId);
                        break;

                    case 6:
                    	System.out.println("\n[ 내 게시글 수정 ]");
                        dao.selectMyNoticeList(userId);

                        Integer updateNoticeId = readIntOrCancel("수정할 게시글 번호");
                        if (updateNoticeId == null) {
                            System.out.println("↩ 게시글 수정이 취소되었습니다.");
                            break;
                        }
                        String newTitle = readRequiredOrCancel("새 제목");
                        if (newTitle == null) {
                            System.out.println("↩ 게시글 수정이 취소되었습니다.");
                            break;
                        }
                        if (newTitle.isEmpty()) {
                            System.out.println("❌ 새 제목을 입력하세요.");
                            break;
                        }
                        String newContent = readRequiredOrCancel("새 내용");
                        if (newContent == null) {
                            System.out.println("↩ 게시글 수정이 취소되었습니다.");
                            break;
                        }
                        if (newContent.isEmpty()) {
                            System.out.println("❌ 새 내용을 입력하세요.");
                            break;
                        }
                        dao.updateMyNotice(updateNoticeId, userId, newTitle, newContent, loginLogId);
                        break;

                    case 7:
                    	System.out.println("\n[ 내 게시글 삭제 ]");
                        dao.selectMyNoticeList(userId);

                        Integer deleteNoticeId = readIntOrCancel("삭제할 게시글 번호");
                        if (deleteNoticeId == null) {
                            System.out.println("↩ 게시글 삭제가 취소되었습니다.");
                            break;
                        }
                        String yn = readYNOrCancel("정말 삭제하시겠습니까?");
                        if (yn == null) {
                            System.out.println("↩ 게시글 삭제가 취소되었습니다.");
                            break;
                        }
                        if (!"Y".equals(yn)) {
                            System.out.println("삭제를 취소했습니다.");
                            break;
                        }
                        dao.deleteMyNotice(deleteNoticeId, userId, loginLogId);
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
 //(취소: 0)
   
    private String readRequiredOrCancel(String label) throws IOException {
        System.out.print(label + " (뒤로가기: 0) : ");
        String input = br.readLine();
        if (input == null) return null;

        input = input.trim();
        if ("0".equals(input)) return null; // 취소
        return input; // 엔터면 ""
    }

    private Integer readIntOrCancel(String label) throws IOException {
        while (true) {
            System.out.print(label + " (뒤로가기: 0) : ");
            String input = br.readLine();
            if (input == null) return null;

            input = input.trim();
            if ("0".equals(input)) return null;

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ 숫자만 입력하세요.");
            }
        }
    }

    private String readYNOrCancel(String label) throws IOException {
        while (true) {
            System.out.print(label + " (Y/N, 취소: 0) : ");
            String input = br.readLine();
            if (input == null) return null;

            input = input.trim().toUpperCase();
            if ("0".equals(input)) return null;

            if ("Y".equals(input) || "N".equals(input)) return input;

            System.out.println("❌ Y 또는 N만 입력하세요.");
        }
    }

    private String readVoteChoiceOrCancel() throws IOException {
        while (true) {
            System.out.print("찬성(Y) / 반대(N) 입력 (뒤로가기: 0) : ");
            String choice = br.readLine();
            if (choice == null) return null;

            choice = choice.trim().toUpperCase();
            if ("0".equals(choice)) return null;

            if ("Y".equals(choice) || "N".equals(choice)) return choice;

            System.out.println("❌ Y 또는 N만 입력하세요.");
        }
    }
}
