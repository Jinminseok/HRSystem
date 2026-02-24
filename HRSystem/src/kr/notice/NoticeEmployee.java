package kr.notice;

import java.io.BufferedReader;
import java.io.IOException;

import kr.hrsystem.dao.NoticeDAO;


public class NoticeEmployee {

    private BufferedReader br;
    private NoticeDAO dao;
    private int userId;
    private Integer loginLogId; // ✅ null 허용

    public NoticeEmployee(BufferedReader br, int userId, Integer loginLogId) {
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
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│              📢 게시판 (사원)               ");
            System.out.println("├─────────────────────────────────────────────");
            System.out.println("│  1. 게시글 작성                             ");
            System.out.println("│  2. 전체 게시글 조회                        ");
            System.out.println("│  3. 내 게시글 조회                          ");
            System.out.println("│  4. 게시글 상세 조회                        ");
            System.out.println("│  5. 투표 참여 (찬성/반대)                   ");
            System.out.println("│  6. 내 게시글 수정                          ");
            System.out.println("│  7. 내 게시글 삭제                          ");
            System.out.println("│  0. 뒤로가기                               ");
            System.out.println("└─────────────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        System.out.println("\n[ 게시글 작성 ]");
                        System.out.print("제목 : ");
                        String title = br.readLine();

                        System.out.print("내용 : ");
                        String content = br.readLine();

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
                        System.out.print("조회할 공지번호 : ");
                        dao.selectNoticeDetail(Integer.parseInt(br.readLine()));
                        break;

                    case 5:
                        dao.selectNoticeList();
                        System.out.print("투표할 공지번호 : ");
                        int voteNoticeId = Integer.parseInt(br.readLine());

                        System.out.print("찬성(Y) / 반대(N) 입력 : ");
                        String choice = br.readLine().trim().toUpperCase();

                        if (!"Y".equals(choice) && !"N".equals(choice)) {
                            System.out.println("Y 또는 N만 입력하세요.");
                            break;
                        }

                        dao.voteNotice(voteNoticeId, userId, choice, loginLogId);
                        break;

                    case 6:
                        System.out.println("\n[ 내 게시글 수정 ]");
                        dao.selectMyNoticeList(userId);

                        System.out.print("수정할 게시글 번호 : ");
                        int updateNoticeId = Integer.parseInt(br.readLine());

                        System.out.print("새 제목 : ");
                        String newTitle = br.readLine();

                        System.out.print("새 내용 : ");
                        String newContent = br.readLine();

                        dao.updateMyNotice(updateNoticeId, userId, newTitle, newContent, loginLogId);
                        break;

                    case 7:
                        System.out.println("\n[ 내 게시글 삭제 ]");
                        dao.selectMyNoticeList(userId);

                        System.out.print("삭제할 게시글 번호 : ");
                        int deleteNoticeId = Integer.parseInt(br.readLine());

                        System.out.print("정말 삭제하시겠습니까? (Y/N) : ");
                        String yn = br.readLine().trim().toUpperCase();

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
}
