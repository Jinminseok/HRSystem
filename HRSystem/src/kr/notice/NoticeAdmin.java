package kr.notice;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.NoticeDAO;


public class NoticeAdmin {

    private BufferedReader br;
    private NoticeDAO dao;
    private int adminUserId;
    private Integer loginLogId; // ✅ null 허용

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
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│              📢 공지사항 관리 (관리자)      ");
            System.out.println("├─────────────────────────────────────────────");
            System.out.println("│  1. 공지사항 등록                           ");
            System.out.println("│  2. 공지사항 수정 (전체)                    ");
            System.out.println("│  3. 공지사항 삭제 (전체)                    ");
            System.out.println("│  4. 공지사항 목록 조회                      ");
            System.out.println("│  5. 공지사항 상세 조회                      ");
            System.out.println("│  6. 투표 결과 조회                          ");
            System.out.println("│  7. 특정 사용자 게시글 목록 조회            ");
            System.out.println("│  8. 특정 사용자 게시글 수정                 ");
            System.out.println("│  9. 특정 사용자 게시글 삭제                 ");
            System.out.println("│  0. 뒤로가기                               ");
            System.out.println("└─────────────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1: // 등록
                        System.out.println("\n[ 공지사항 등록 ]");

                        System.out.print("제목 : ");
                        String title = br.readLine();

                        System.out.print("내용 : ");
                        String content = br.readLine();

                        System.out.print("상단 고정 여부 (Y/N) : ");
                        String fixed = br.readLine().trim().toUpperCase();
                        if (!"Y".equals(fixed)) fixed = "N";

                        System.out.print("투표 기능 추가 여부 (Y/N) : ");
                        String hasVote = br.readLine().trim().toUpperCase();
                        if (!"Y".equals(hasVote)) hasVote = "N";

                        String deadline = null;
                        if ("Y".equals(hasVote)) {
                            System.out.print("투표 마감일시 (예: 2026-02-28 18:00, 없으면 엔터) : ");
                            deadline = br.readLine().trim();
                            if (deadline.length() == 0) deadline = null;
                        }

                        dao.insertNotice(title, content, adminUserId, fixed, hasVote, deadline, loginLogId);
                        break;

                    case 2: // 전체 수정
                        dao.selectNoticeList();
                        System.out.println("\n[ 공지사항 수정 ]");

                        System.out.print("수정할 공지번호 : ");
                        int updateId = Integer.parseInt(br.readLine());

                        System.out.print("새 제목 : ");
                        String newTitle = br.readLine();

                        System.out.print("새 내용 : ");
                        String newContent = br.readLine();

                        dao.updateNotice(updateId, newTitle, newContent, adminUserId, loginLogId);
                        break;

                    case 3: // 전체 삭제
                        dao.selectNoticeList();
                        System.out.println("\n[ 공지사항 삭제 ]");

                        System.out.print("삭제할 공지번호 : ");
                        int deleteId = Integer.parseInt(br.readLine());

                        System.out.print("정말 삭제하시겠습니까? (Y/N) : ");
                        String yn = br.readLine().trim().toUpperCase();
                        if (!"Y".equals(yn)) {
                            System.out.println("삭제 취소");
                            break;
                        }

                        dao.deleteNotice(deleteId, adminUserId, loginLogId);
                        break;

                    case 4:
                        dao.selectNoticeList();
                        break;

                    case 5:
                        dao.selectNoticeList();
                        System.out.print("조회할 공지번호 : ");
                        dao.selectNoticeDetail(Integer.parseInt(br.readLine()));
                        break;

                    case 6:
                        dao.selectNoticeList();
                        System.out.print("투표 결과 볼 공지번호 : ");
                        dao.selectVoteResult(Integer.parseInt(br.readLine()));
                        break;

                    case 7: // 특정 사용자 게시글 목록
                        System.out.println("\n[ 특정 사용자 게시글 목록 조회 ]");
                        System.out.print("USER_ID 입력 : ");
                        int targetUserIdForList = Integer.parseInt(br.readLine());
                        dao.selectUserNoticeList(targetUserIdForList);
                        break;

                    case 8: // 특정 사용자 게시글 수정
                        System.out.println("\n[ 특정 사용자 게시글 수정 ]");
                        System.out.print("USER_ID 입력 : ");
                        int targetUserIdForUpdate = Integer.parseInt(br.readLine());

                        dao.selectUserNoticeList(targetUserIdForUpdate);

                        System.out.print("수정할 공지번호 : ");
                        int targetNoticeIdForUpdate = Integer.parseInt(br.readLine());

                        System.out.print("새 제목 : ");
                        String adminNewTitle = br.readLine();

                        System.out.print("새 내용 : ");
                        String adminNewContent = br.readLine();

                        dao.updateNoticeByAdminForUser(
                            targetNoticeIdForUpdate,
                            targetUserIdForUpdate,
                            adminNewTitle,
                            adminNewContent,
                            adminUserId,
                            loginLogId
                        );
                        break;

                    case 9: // 특정 사용자 게시글 삭제
                        System.out.println("\n[ 특정 사용자 게시글 삭제 ]");
                        System.out.print("USER_ID 입력 : ");
                        int targetUserIdForDelete = Integer.parseInt(br.readLine());

                        dao.selectUserNoticeList(targetUserIdForDelete);

                        System.out.print("삭제할 공지번호 : ");
                        int targetNoticeIdForDelete = Integer.parseInt(br.readLine());

                        System.out.print("정말 삭제하시겠습니까? (Y/N) : ");
                        String delYn = br.readLine().trim().toUpperCase();
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