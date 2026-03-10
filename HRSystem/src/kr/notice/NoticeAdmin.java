package kr.notice;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.NoticeDAO;

public class NoticeAdmin {

    private BufferedReader br;
    private NoticeDAO dao;
    private int adminUserId;
    private Integer loginLogId;

    public NoticeAdmin(BufferedReader br, int adminUserId, Integer loginLogId) {
        this.br = br;
        this.adminUserId = adminUserId;
        this.loginLogId = loginLogId;
        this.dao = new NoticeDAO();

        try {
            noticeAdminMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void noticeAdminMenu() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│          📢  게시판 관리 (관리자)        │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 게시글 등록                         │");
            System.out.println("│  [2] 게시글 수정 (전체)                  │");
            System.out.println("│  [3] 게시글 삭제 (전체)                  │");
            System.out.println("│  [4] 게시글 목록 조회                    │");
            System.out.println("│  [5] 게시글 상세 조회                    │");
            System.out.println("│  [6] 투표 결과 조회                      │");
            System.out.println("│  [7] 특정 사용자 게시글 목록 조회        │");
            System.out.println("│  [8] 특정 사용자 게시글 수정             │");
            System.out.println("│  [9] 특정 사용자 게시글 삭제             │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1: { // 등록
                        System.out.println("\n[ 게시글 등록 ]");

                        String title = readLineWithBack("제목 (뒤로가기: 0) : ");
                        if (title == null) break;

                        String content = readLineWithBack("내용 (뒤로가기: 0) : ");
                        if (content == null) break;

                        String fixedInput = readLineWithBack("상단 고정 여부 (Y/N, 뒤로가기: 0) : ");
                        if (fixedInput == null) break;
                        String fixed = fixedInput.trim().toUpperCase();
                        if (!"Y".equals(fixed)) fixed = "N";

                        String hasVoteInput = readLineWithBack("투표 기능 추가 여부 (Y/N, 뒤로가기: 0) : ");
                        if (hasVoteInput == null) break;
                        String hasVote = hasVoteInput.trim().toUpperCase();
                        if (!"Y".equals(hasVote)) hasVote = "N";

                        String deadline = null;
                        if ("Y".equals(hasVote)) {
                            String deadlineInput = readLineWithBack("투표 마감일시 (예: 2026-02-28 18:00, 없으면 엔터 / 뒤로가기: 0) : ");
                            if (deadlineInput == null) break;
                            deadline = deadlineInput.trim();
                            if (deadline.length() == 0) deadline = null;
                        }

                        dao.insertNotice(title, content, adminUserId, fixed, hasVote, deadline, loginLogId);
                        break;
                    }

                    case 2: { // 전체 수정
                        dao.selectNoticeList();
                        System.out.println("\n[ 게시글 수정 ]");

                        Integer updateId = readIntWithBack("수정할 게시글번호 (뒤로가기: 0) : ");
                        if (updateId == null) break;

                        String newTitle = readLineWithBack("새 제목 (뒤로가기: 0) : ");
                        if (newTitle == null) break;

                        String newContent = readLineWithBack("새 내용 (뒤로가기: 0) : ");
                        if (newContent == null) break;

                        dao.updateNotice(updateId, newTitle, newContent, adminUserId, loginLogId);
                        break;
                    }

                    case 3: { // 전체 삭제
                        dao.selectNoticeList();
                        System.out.println("\n[ 게시글 삭제 ]");

                        Integer deleteId = readIntWithBack("삭제할 게시글번호 (뒤로가기: 0) : ");
                        if (deleteId == null) break;

                        String ynInput = readLineWithBack("정말 삭제하시겠습니까? (Y/N, 뒤로가기: 0) : ");
                        if (ynInput == null) break;

                        String yn = ynInput.trim().toUpperCase();
                        if (!"Y".equals(yn)) {
                            System.out.println("삭제 취소");
                            break;
                        }

                        dao.deleteNotice(deleteId, adminUserId, loginLogId);
                        break;
                    }

                    case 4:
                        dao.selectNoticeList();
                        break;

                    case 5: { // 상세 조회
                        dao.selectNoticeList();

                        Integer noticeId = readIntWithBack("조회할 게시글번호 (뒤로가기: 0) : ");
                        if (noticeId == null) break;

                        dao.selectNoticeDetail(noticeId);
                        break;
                    }

                    case 6: { // 투표 결과 조회
                        dao.selectVoteNoticeList();

                        Integer noticeId = readIntWithBack("투표 결과 볼 게시글번호 (뒤로가기: 0) : ");
                        if (noticeId == null) break;

                        dao.selectVoteResult(noticeId);
                        break;
                    }

                    case 7: { // 특정 사용자 게시글 목록
                        System.out.println("\n[ 특정 사용자 게시글 목록 조회 ]");

                        Integer targetUserIdForList = readIntWithBack("USER_ID 입력 (뒤로가기: 0) : ");
                        if (targetUserIdForList == null) break;

                        dao.selectUserNoticeList(targetUserIdForList);
                        break;
                    }

                    case 8: { // 특정 사용자 게시글 수정
                        System.out.println("\n[ 특정 사용자 게시글 수정 ]");

                        Integer targetUserIdForUpdate = readIntWithBack("USER_ID 입력 (뒤로가기: 0) : ");
                        if (targetUserIdForUpdate == null) break;

                        dao.selectUserNoticeList(targetUserIdForUpdate);

                        Integer targetNoticeIdForUpdate = readIntWithBack("수정할 게시글번호 (뒤로가기: 0) : ");
                        if (targetNoticeIdForUpdate == null) break;

                        String adminNewTitle = readLineWithBack("새 제목 (뒤로가기: 0) : ");
                        if (adminNewTitle == null) break;

                        String adminNewContent = readLineWithBack("새 내용 (뒤로가기: 0) : ");
                        if (adminNewContent == null) break;

                        dao.updateNoticeByAdminForUser(
                                targetNoticeIdForUpdate,
                                targetUserIdForUpdate,
                                adminNewTitle,
                                adminNewContent,
                                adminUserId,
                                loginLogId
                        );
                        break;
                    }

                    case 9: { // 특정 사용자 게시글 삭제
                        System.out.println("\n[ 특정 사용자 게시글 삭제 ]");

                        Integer targetUserIdForDelete = readIntWithBack("USER_ID 입력 (뒤로가기: 0) : ");
                        if (targetUserIdForDelete == null) break;

                        dao.selectUserNoticeList(targetUserIdForDelete);

                        Integer targetNoticeIdForDelete = readIntWithBack("삭제할 게시글번호 (뒤로가기: 0) : ");
                        if (targetNoticeIdForDelete == null) break;

                        String delYnInput = readLineWithBack("정말 삭제하시겠습니까? (Y/N, 뒤로가기: 0) : ");
                        if (delYnInput == null) break;

                        String delYn = delYnInput.trim().toUpperCase();
                        if (!"Y".equals(delYn)) {
                            System.out.println("삭제 취소");
                            break;
                        }

                        dao.deleteNoticeByAdminForUser(
                                targetNoticeIdForDelete,
                                targetUserIdForDelete,
                                adminUserId,
                                loginLogId
                        );
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

    private String readLineWithBack(String message) throws IOException {
        System.out.print(message);
        String input = br.readLine();

        if (input == null) {
            return null;
        }

        if ("0".equals(input.trim())) {
            return null;
        }

        return input;
    }

    private Integer readIntWithBack(String message) throws IOException {
        System.out.print(message);
        String input = br.readLine();

        if (input == null) {
            return null;
        }

        input = input.trim();

        if ("0".equals(input)) {
            return null;
        }

        return Integer.parseInt(input);
    }
}