package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import kr.util.DBUtil;

public class NoticeDAO {

    private LogDAO logDao = new LogDAO();

    // 전체 게시글 목록 조회
    public void selectNoticeList() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT n.notice_id, n.notice_title, u.user_name, "
                       + "       TO_CHAR(n.created_at, 'YYYY-MM-DD') AS created_at_str, "
                       + "       n.view_count, n.fixed, n.has_vote, n.vote_status "
                       + "FROM notices n "
                       + "JOIN usertest u ON n.user_id = u.user_id "
                       + "ORDER BY n.fixed DESC, n.notice_id DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            printDivider(120);
            System.out.println("전체 게시글 목록");
            printDivider(120);

            System.out.println(
                    pad("번호", 8) +
                    pad("제목", 36) +
                    pad("작성자", 12) +
                    pad("작성일", 14) +
                    pad("조회수", 8) +
                    pad("상단고정", 10) +
                    pad("투표", 8) +
                    pad("투표상태", 12)
            );

            printDivider(120);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;

                // 게시글 목록 한 줄씩 출력
                System.out.println(
                        pad(String.valueOf(rs.getInt("notice_id")), 8) +
                        pad(rs.getString("notice_title"), 36) +
                        pad(rs.getString("user_name"), 12) +
                        pad(rs.getString("created_at_str"), 14) +
                        pad(String.valueOf(rs.getInt("view_count")), 8) +
                        pad(fixedToKor(rs.getString("fixed")), 10) +
                        pad(hasVoteToKor(rs.getString("has_vote")), 8) +
                        pad(voteStatusToKor(rs.getString("vote_status")), 12)
                );
            }

            if (!hasData) {
                System.out.println("등록된 게시글이 없습니다.");
            }

            printDivider(120);

        } catch (Exception e) {
            System.out.println("❌ 게시글 목록 조회 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 투표 게시글만 조회
    public void selectVoteNoticeList() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT n.notice_id, n.notice_title, u.user_name, "
                       + "       TO_CHAR(n.created_at, 'YYYY-MM-DD') AS created_at_str, "
                       + "       n.view_count, n.fixed, n.has_vote, n.vote_status "
                       + "FROM notices n "
                       + "JOIN usertest u ON n.user_id = u.user_id "
                       + "WHERE n.has_vote = 'Y' "
                       + "ORDER BY n.fixed DESC, n.notice_id DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            printDivider(120);
            System.out.println("투표 게시글 목록");
            printDivider(120);

            System.out.println(
                    pad("번호", 8) +
                    pad("제목", 36) +
                    pad("작성자", 12) +
                    pad("작성일", 14) +
                    pad("조회수", 8) +
                    pad("상단고정", 10) +
                    pad("투표", 8) +
                    pad("투표상태", 12)
            );

            printDivider(120);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;

                // 투표 게시글 목록 한 줄씩 출력
                System.out.println(
                        pad(String.valueOf(rs.getInt("notice_id")), 8) +
                        pad(rs.getString("notice_title"), 36) +
                        pad(rs.getString("user_name"), 12) +
                        pad(rs.getString("created_at_str"), 14) +
                        pad(String.valueOf(rs.getInt("view_count")), 8) +
                        pad(fixedToKor(rs.getString("fixed")), 10) +
                        pad(hasVoteToKor(rs.getString("has_vote")), 8) +
                        pad(voteStatusToKor(rs.getString("vote_status")), 12)
                );
            }

            if (!hasData) {
                System.out.println("등록된 투표 게시글이 없습니다.");
            }

            printDivider(120);

        } catch (Exception e) {
            System.out.println("❌ 투표 게시글 목록 조회 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 특정 사용자가 작성한 게시글 목록 조회 (관리자용)
    public void selectUserNoticeList(int targetUserId) {
        Connection conn = null;
        PreparedStatement pstmtName = null;
        PreparedStatement pstmt = null;
        ResultSet rsName = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String userName = null;
            String nameSql = "SELECT user_name FROM usertest WHERE user_id = ?";
            pstmtName = conn.prepareStatement(nameSql);
            pstmtName.setInt(1, targetUserId);
            rsName = pstmtName.executeQuery();

            // 대상 사용자 존재 여부 확인
            if (rsName.next()) {
                userName = rsName.getString("user_name");
            } else {
                System.out.println("❌ 해당 USER_ID(" + targetUserId + ") 사용자가 존재하지 않습니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

            String sql = "SELECT n.notice_id, n.notice_title, u.user_name, "
                       + "       TO_CHAR(n.created_at, 'YYYY-MM-DD') AS created_at_str, "
                       + "       n.view_count, n.fixed, n.has_vote, n.vote_status "
                       + "FROM notices n "
                       + "JOIN usertest u ON n.user_id = u.user_id "
                       + "WHERE n.user_id = ? "
                       + "ORDER BY n.notice_id DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, targetUserId);
            rs = pstmt.executeQuery();

            printDivider(120);
            System.out.println("[ " + userName + " ] 님의 게시글 목록");
            printDivider(120);

            System.out.println(
                    pad("번호", 8) +
                    pad("제목", 36) +
                    pad("작성자", 12) +
                    pad("작성일", 14) +
                    pad("조회수", 8) +
                    pad("상단고정", 10) +
                    pad("투표", 8) +
                    pad("투표상태", 12)
            );

            printDivider(120);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;

                // 특정 사용자의 게시글 목록 출력
                System.out.println(
                        pad(String.valueOf(rs.getInt("notice_id")), 8) +
                        pad(rs.getString("notice_title"), 36) +
                        pad(rs.getString("user_name"), 12) +
                        pad(rs.getString("created_at_str"), 14) +
                        pad(String.valueOf(rs.getInt("view_count")), 8) +
                        pad(fixedToKor(rs.getString("fixed")), 10) +
                        pad(hasVoteToKor(rs.getString("has_vote")), 8) +
                        pad(voteStatusToKor(rs.getString("vote_status")), 12)
                );
            }

            if (!hasData) {
                System.out.println("해당 사용자가 작성한 게시글이 없습니다.");
            }

            printDivider(120);

        } catch (Exception e) {
            System.out.println("❌ 특정 사용자 게시글 목록 조회 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(rsName, pstmtName, null);
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 게시글 상세 조회 + 조회수 1 증가
    public void selectNoticeDetail(int noticeId) {
        Connection conn = null;
        PreparedStatement pstmtUp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String upSql = "UPDATE notices SET view_count = view_count + 1 WHERE notice_id = ?";
            pstmtUp = conn.prepareStatement(upSql);
            pstmtUp.setInt(1, noticeId);

            // 조회수 증가, 게시글이 없으면 롤백
            int count = pstmtUp.executeUpdate();
            if (count == 0) {
                conn.rollback();
                System.out.println("❌ 해당 게시글이 존재하지 않습니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

            String sql = "SELECT n.notice_id, n.notice_title, n.notice_content, u.user_name, "
                       + "       TO_CHAR(n.created_at, 'YYYY-MM-DD HH24:MI') AS created_at_str, "
                       + "       n.view_count, n.fixed, n.has_vote, n.vote_status, "
                       + "       TO_CHAR(n.vote_deadline, 'YYYY-MM-DD HH24:MI') AS vote_deadline_str "
                       + "FROM notices n "
                       + "JOIN usertest u ON n.user_id = u.user_id "
                       + "WHERE n.notice_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, noticeId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 게시글 상세 정보 출력
                printDivider(90);
                System.out.println("번호      : " + rs.getInt("notice_id"));
                System.out.println("제목      : " + nvl(rs.getString("notice_title")));
                System.out.println("작성자    : " + nvl(rs.getString("user_name")));
                System.out.println("작성일    : " + nvl(rs.getString("created_at_str")));
                System.out.println("조회수    : " + rs.getInt("view_count"));
                System.out.println("상단고정  : " + fixedToKor(rs.getString("fixed")));
                System.out.println("투표글    : " + hasVoteToKor(rs.getString("has_vote")));
                System.out.println("투표상태  : " + voteStatusToKor(rs.getString("vote_status")));
                System.out.println("투표마감  : " + nvl(rs.getString("vote_deadline_str")));
                System.out.println("내용      : ");
                System.out.println(nvl(rs.getString("notice_content")));
                printDivider(90);
            }

            conn.commit();

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            System.out.println("❌ 게시글 상세 조회 중 오류가 발생했습니다.");
        } finally {
            DBUtil.executeClose(rs, pstmt, null);
            DBUtil.executeClose(null, pstmtUp, conn);
        }
    }

    // 내 게시글 조회
    public void selectMyNoticeList(int userId) {
        selectUserNoticeList(userId);
    }

    // 게시글 등록 (관리자/사원 공용)
    public void insertNotice(String title, String content, int writerUserId,
                             String fixed, String hasVote, String voteDeadlineInput,
                             Integer loginLogId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmtSeq = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 고정글/투표글 입력값 정리
            String safeFixed = "Y".equalsIgnoreCase(fixed) ? "Y" : "N";
            String safeHasVote = "Y".equalsIgnoreCase(hasVote) ? "Y" : "N";
            String voteStatus = "Y".equals(safeHasVote) ? "O" : "N";

            Timestamp voteDeadline = null;
            try {
                // 투표 마감일시 파싱
                voteDeadline = parseVoteDeadline(voteDeadlineInput);
            } catch (IllegalArgumentException e) {
                System.out.println("❌ " + e.getMessage());
                System.out.println("다시 입력해주세요.");
                return;
            }

            String sql =
                "INSERT INTO notices "
              + "(notice_id, notice_title, notice_content, user_id, fixed, has_vote, vote_status, vote_deadline) "
              + "VALUES (seq_notices.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setInt(3, writerUserId);
            pstmt.setString(4, safeFixed);
            pstmt.setString(5, safeHasVote);
            pstmt.setString(6, voteStatus);

            if (voteDeadline == null) pstmt.setNull(7, java.sql.Types.TIMESTAMP);
            else pstmt.setTimestamp(7, voteDeadline);

            int count = pstmt.executeUpdate();

            DBUtil.executeClose(null, pstmt, null);

            // 방금 등록된 게시글 번호 조회
            pstmtSeq = conn.prepareStatement("SELECT seq_notices.CURRVAL AS notice_id FROM dual");
            rs = pstmtSeq.executeQuery();

            Integer noticeId = null;
            if (rs.next()) {
                noticeId = rs.getInt("notice_id");
            }

            System.out.println(count + "개의 게시글이 등록되었습니다.");

            // 게시글 등록 로그 저장
            logDao.insertActionLog(
                writerUserId,
                "게시판",
                "NOTICE_CREATE",
                "게시글등록 noticeId=" + noticeId + ", fixed=" + safeFixed + ", hasVote=" + safeHasVote,
                "NOTICES",
                noticeId,
                loginLogId
            );

        } catch (Exception e) {
            System.out.println("❌ 게시글 등록 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmtSeq, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 관리자: 모든 게시글 수정
    public void updateNotice(int noticeId, String newTitle, String newContent,
                             int actorUserId, Integer loginLogId) {

        Connection conn = null;
        PreparedStatement pstmtSel = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String oldTitle = null;

            String selSql = "SELECT notice_title FROM notices WHERE notice_id = ?";
            pstmtSel = conn.prepareStatement(selSql);
            pstmtSel.setInt(1, noticeId);
            rs = pstmtSel.executeQuery();

            // 수정 전 기존 제목 조회
            if (!rs.next()) {
                System.out.println("❌ 해당 게시글이 존재하지 않습니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }
            oldTitle = rs.getString("notice_title");

            DBUtil.executeClose(rs, pstmtSel, null);

            String sql = "UPDATE notices SET notice_title = ?, notice_content = ? WHERE notice_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newContent);
            pstmt.setInt(3, noticeId);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("게시글이 수정되었습니다.");

                // 관리자 수정 로그 저장
                logDao.insertActionLog(
                    actorUserId,
                    "게시판",
                    "NOTICE_UPDATE",
                    "관리자 수정 noticeId=" + noticeId + " | [" + oldTitle + "] -> [" + newTitle + "]",
                    "NOTICES",
                    noticeId,
                    loginLogId
                );
            } else {
                System.out.println("❌ 해당 게시글이 존재하지 않습니다.");
                System.out.println("다시 입력해주세요.");
            }

        } catch (Exception e) {
            System.out.println("❌ 게시글 수정 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 관리자: 모든 게시글 삭제
    public void deleteNotice(int noticeId, int actorUserId, Integer loginLogId) {
        Connection conn = null;
        PreparedStatement pstmtSel = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String oldTitle = null;

            String selSql = "SELECT notice_title FROM notices WHERE notice_id = ?";
            pstmtSel = conn.prepareStatement(selSql);
            pstmtSel.setInt(1, noticeId);
            rs = pstmtSel.executeQuery();

            // 삭제 전 기존 제목 조회
            if (!rs.next()) {
                System.out.println("❌ 해당 게시글이 존재하지 않습니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }
            oldTitle = rs.getString("notice_title");

            DBUtil.executeClose(rs, pstmtSel, null);

            String sql = "DELETE FROM notices WHERE notice_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, noticeId);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("게시글이 삭제되었습니다.");

                // 관리자 삭제 로그 저장
                logDao.insertActionLog(
                    actorUserId,
                    "게시판",
                    "NOTICE_DELETE",
                    "관리자 삭제 noticeId=" + noticeId + ", title=" + oldTitle,
                    "NOTICES",
                    noticeId,
                    loginLogId
                );
            } else {
                System.out.println("❌ 해당 게시글이 존재하지 않습니다.");
                System.out.println("다시 입력해주세요.");
            }

        } catch (Exception e) {
            System.out.println("❌ 게시글 삭제 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 관리자: 특정 사용자의 게시글 수정
    public void updateNoticeByAdminForUser(int noticeId, int targetUserId, String newTitle, String newContent,
                                           int adminUserId, Integer loginLogId) {

        Connection conn = null;
        PreparedStatement pstmtSel = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String oldTitle = null;
            int ownerUserId = -1;

            String selSql = "SELECT user_id, notice_title FROM notices WHERE notice_id = ?";
            pstmtSel = conn.prepareStatement(selSql);
            pstmtSel.setInt(1, noticeId);
            rs = pstmtSel.executeQuery();

            // 게시글 존재 여부와 작성자 확인
            if (!rs.next()) {
                System.out.println("❌ 해당 게시글이 존재하지 않습니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

            ownerUserId = rs.getInt("user_id");
            oldTitle = rs.getString("notice_title");

            if (ownerUserId != targetUserId) {
                System.out.println("❌ 선택한 USER_ID가 작성한 게시글이 아닙니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

            DBUtil.executeClose(rs, pstmtSel, null);

            String sql = "UPDATE notices SET notice_title = ?, notice_content = ? WHERE notice_id = ? AND user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newContent);
            pstmt.setInt(3, noticeId);
            pstmt.setInt(4, targetUserId);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 특정 사용자 게시글 수정 완료");

                // 관리자(사용자별) 수정 로그 저장
                logDao.insertActionLog(
                    adminUserId,
                    "게시판",
                    "NOTICE_UPDATE",
                    "관리자(사용자별) 수정 targetUserId=" + targetUserId
                    + ", noticeId=" + noticeId
                    + " | [" + oldTitle + "] -> [" + newTitle + "]",
                    "NOTICES",
                    noticeId,
                    loginLogId
                );
            } else {
                System.out.println("❌ 수정 실패");
                System.out.println("다시 입력해주세요.");
            }

        } catch (Exception e) {
            System.out.println("❌ 특정 사용자 게시글 수정 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 관리자: 특정 사용자의 게시글 삭제
    public void deleteNoticeByAdminForUser(int noticeId, int targetUserId,
                                           int adminUserId, Integer loginLogId) {

        Connection conn = null;
        PreparedStatement pstmtSel = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String oldTitle = null;
            int ownerUserId = -1;

            String selSql = "SELECT user_id, notice_title FROM notices WHERE notice_id = ?";
            pstmtSel = conn.prepareStatement(selSql);
            pstmtSel.setInt(1, noticeId);
            rs = pstmtSel.executeQuery();

            // 게시글 존재 여부와 작성자 확인
            if (!rs.next()) {
                System.out.println("❌ 해당 게시글이 존재하지 않습니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

            ownerUserId = rs.getInt("user_id");
            oldTitle = rs.getString("notice_title");

            if (ownerUserId != targetUserId) {
                System.out.println("❌ 선택한 USER_ID가 작성한 게시글이 아닙니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

            DBUtil.executeClose(rs, pstmtSel, null);

            String sql = "DELETE FROM notices WHERE notice_id = ? AND user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, noticeId);
            pstmt.setInt(2, targetUserId);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 특정 사용자 게시글 삭제 완료");

                // 관리자(사용자별) 삭제 로그 저장
                logDao.insertActionLog(
                    adminUserId,
                    "게시판",
                    "NOTICE_DELETE",
                    "관리자(사용자별) 삭제 targetUserId=" + targetUserId
                    + ", noticeId=" + noticeId
                    + ", title=" + oldTitle,
                    "NOTICES",
                    noticeId,
                    loginLogId
                );
            } else {
                System.out.println("❌ 삭제 실패");
                System.out.println("다시 입력해주세요.");
            }

        } catch (Exception e) {
            System.out.println("❌ 특정 사용자 게시글 삭제 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 사원: 본인 게시글만 수정
    public void updateMyNotice(int noticeId, int userId, String newTitle, String newContent, Integer loginLogId) {
        Connection conn = null;
        PreparedStatement pstmtSel = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String oldTitle = null;

            String selSql = "SELECT notice_title FROM notices WHERE notice_id = ? AND user_id = ?";
            pstmtSel = conn.prepareStatement(selSql);
            pstmtSel.setInt(1, noticeId);
            pstmtSel.setInt(2, userId);
            rs = pstmtSel.executeQuery();

            // 본인 글인지 확인
            if (!rs.next()) {
                System.out.println("❌ 수정 실패: 게시글이 없거나 본인 글이 아닙니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }
            oldTitle = rs.getString("notice_title");

            DBUtil.executeClose(rs, pstmtSel, null);

            String sql = "UPDATE notices SET notice_title = ?, notice_content = ? WHERE notice_id = ? AND user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newContent);
            pstmt.setInt(3, noticeId);
            pstmt.setInt(4, userId);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 내 게시글이 수정되었습니다.");

                // 내 게시글 수정 로그 저장
                logDao.insertActionLog(
                    userId,
                    "게시판",
                    "NOTICE_UPDATE",
                    "내 게시글 수정 noticeId=" + noticeId + " | [" + oldTitle + "] -> [" + newTitle + "]",
                    "NOTICES",
                    noticeId,
                    loginLogId
                );
            } else {
                System.out.println("❌ 수정 실패");
                System.out.println("다시 입력해주세요.");
            }

        } catch (Exception e) {
            System.out.println("❌ 내 게시글 수정 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 사원: 본인 게시글만 삭제
    public void deleteMyNotice(int noticeId, int userId, Integer loginLogId) {
        Connection conn = null;
        PreparedStatement pstmtSel = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String oldTitle = null;

            String selSql = "SELECT notice_title FROM notices WHERE notice_id = ? AND user_id = ?";
            pstmtSel = conn.prepareStatement(selSql);
            pstmtSel.setInt(1, noticeId);
            pstmtSel.setInt(2, userId);
            rs = pstmtSel.executeQuery();

            // 본인 글인지 확인
            if (!rs.next()) {
                System.out.println("❌ 삭제 실패: 게시글이 없거나 본인 글이 아닙니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }
            oldTitle = rs.getString("notice_title");

            DBUtil.executeClose(rs, pstmtSel, null);

            String sql = "DELETE FROM notices WHERE notice_id = ? AND user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, noticeId);
            pstmt.setInt(2, userId);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("✅ 내 게시글이 삭제되었습니다.");

                // 내 게시글 삭제 로그 저장
                logDao.insertActionLog(
                    userId,
                    "게시판",
                    "NOTICE_DELETE",
                    "내 게시글 삭제 noticeId=" + noticeId + ", title=" + oldTitle,
                    "NOTICES",
                    noticeId,
                    loginLogId
                );
            } else {
                System.out.println("❌ 삭제 실패");
                System.out.println("다시 입력해주세요.");
            }

        } catch (Exception e) {
            System.out.println("❌ 내 게시글 삭제 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 투표 참여 또는 기존 투표 수정
    public void voteNotice(int noticeId, int userId, String choice, Integer loginLogId) {

        Connection conn = null;
        PreparedStatement pstmtChk = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String chkSql =
                "SELECT has_vote, vote_status, vote_deadline " +
                "FROM notices " +
                "WHERE notice_id = ?";

            pstmtChk = conn.prepareStatement(chkSql);
            pstmtChk.setInt(1, noticeId);
            rs = pstmtChk.executeQuery();

            // 게시글 존재 여부와 투표 가능 여부 확인
            if (!rs.next()) {
                System.out.println("❌ 해당 게시글이 없습니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

            String hasVote = rs.getString("has_vote");
            String voteStatus = rs.getString("vote_status");
            Timestamp deadline = rs.getTimestamp("vote_deadline");

            if (!"Y".equalsIgnoreCase(hasVote)) {
                System.out.println("👉 이 게시글은 투표글이 아닙니다.");
                return;
            }

            if (!"O".equalsIgnoreCase(voteStatus)) {
                System.out.println("👉 투표가 진행중이 아닙니다.");
                return;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (deadline != null && deadline.before(now)) {
                System.out.println("👉 투표 마감 시간이 지났습니다.");

                DBUtil.executeClose(rs, pstmtChk, null);

                // 마감 시간이 지났으면 상태를 마감(C)으로 변경
                PreparedStatement pstmtClose = null;
                try {
                    pstmtClose = conn.prepareStatement("UPDATE notices SET vote_status='C' WHERE notice_id=?");
                    pstmtClose.setInt(1, noticeId);
                    pstmtClose.executeUpdate();
                } finally {
                    DBUtil.executeClose(null, pstmtClose, null);
                }
                return;
            }

            DBUtil.executeClose(rs, pstmtChk, null);

            String mergeSql =
                "MERGE INTO notice_votes v " +
                "USING (SELECT ? AS notice_id, ? AS user_id, ? AS vote_choice FROM dual) src " +
                "ON (v.notice_id = src.notice_id AND v.user_id = src.user_id) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET v.vote_choice = src.vote_choice, v.voted_at = SYSDATE " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (vote_id, notice_id, user_id, vote_choice, voted_at) " +
                "  VALUES (seq_notice_votes.NEXTVAL, src.notice_id, src.user_id, src.vote_choice, SYSDATE)";

            pstmt = conn.prepareStatement(mergeSql);
            pstmt.setInt(1, noticeId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, choice.toUpperCase());

            pstmt.executeUpdate();

            String label = "Y".equalsIgnoreCase(choice) ? "찬성" : "반대";
            System.out.println("✅ 투표 완료! (" + label + ")");

            // 투표 로그 저장
            logDao.insertActionLog(
                userId,
                "게시판",
                "VOTE_CAST",
                "noticeId=" + noticeId + ", choice=" + label,
                "NOTICE_VOTES",
                noticeId,
                loginLogId
            );

        } catch (Exception e) {
            System.out.println("❌ 투표 처리 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rs, pstmtChk, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 관리자용 투표 결과 조회
    public void selectVoteResult(int noticeId) {
        Connection conn = null;
        PreparedStatement pstmtChk = null;
        PreparedStatement pstmt = null;
        ResultSet rsChk = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 게시글 존재 여부 + 투표글 여부 확인
            String chkSql = "SELECT notice_id, has_vote FROM notices WHERE notice_id = ?";
            pstmtChk = conn.prepareStatement(chkSql);
            pstmtChk.setInt(1, noticeId);
            rsChk = pstmtChk.executeQuery();

            if (!rsChk.next()) {
                System.out.println("❌ 해당 게시글번호가 없습니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

            String hasVote = rsChk.getString("has_vote");
            if (!"Y".equalsIgnoreCase(hasVote)) {
                System.out.println("❌ 해당 게시글은 투표 게시글이 아닙니다.");
                System.out.println("다시 입력해주세요.");
                return;
            }

            // 찬성/반대 수 집계 조회
            String sql =
                "SELECT NVL(SUM(CASE WHEN vote_choice = 'Y' THEN 1 ELSE 0 END), 0) AS YES_CNT, " +
                "       NVL(SUM(CASE WHEN vote_choice = 'N' THEN 1 ELSE 0 END), 0) AS NO_CNT " +
                "FROM notice_votes " +
                "WHERE notice_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, noticeId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                printDivider(40);
                System.out.println("투표 결과 (게시글번호: " + noticeId + ")");
                printDivider(40);
                System.out.println("찬성 : " + rs.getInt("YES_CNT"));
                System.out.println("반대 : " + rs.getInt("NO_CNT"));
                printDivider(40);
            }

        } catch (Exception e) {
            System.out.println("❌ 투표 결과 조회 중 오류가 발생했습니다.");
            System.out.println("다시 입력해주세요.");
        } finally {
            DBUtil.executeClose(rsChk, pstmtChk, null);
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 투표 마감일시 문자열을 Timestamp로 변환
    private Timestamp parseVoteDeadline(String input) {
        if (input == null) return null;

        input = input.trim();
        if (input.length() == 0) return null;

        try {
            // 날짜만 입력하면 23:59:59로 처리
            if (input.matches("\\d{4}-\\d{2}-\\d{2}$")) {
                LocalDate date = LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return Timestamp.valueOf(date.atTime(23, 59, 59));
            }

            // yyyy-MM-dd HH:mm 형식 처리
            if (input.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
                LocalDateTime dateTime = LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return Timestamp.valueOf(dateTime);
            }

            // yyyy-MM-dd HH:mm:ss 형식 처리
            if (input.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {
                LocalDateTime dateTime = LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return Timestamp.valueOf(dateTime);
            }

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("투표 마감일시 형식이 잘못되었습니다. 예: 2026-03-11 또는 2026-03-11 18:00");
        }

        throw new IllegalArgumentException("투표 마감일시 형식이 잘못되었습니다. 예: 2026-03-11 또는 2026-03-11 18:00");
    }

    // ==========================
    // 콘솔 출력 정렬용 유틸
    // ==========================

    // 구분선 출력
    private void printDivider(int length) {
        System.out.println("=".repeat(length));
    }

    // 한글/중문처럼 폭이 2칸인 문자 판별
    private boolean isWide(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    // 콘솔 정렬을 위해 문자열 길이 맞춤
    private String pad(String s, int width) {
        if (s == null || s.trim().isEmpty()) {
            s = "-";
        }

        StringBuilder sb = new StringBuilder();
        int len = 0;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int charWidth = isWide(ch) ? 2 : 1;

            if (len + charWidth > width) {
                break;
            }

            sb.append(ch);
            len += charWidth;
        }

        while (len < width) {
            sb.append(' ');
            len++;
        }

        return sb.toString();
    }

    // null 또는 공백 문자열이면 "-" 반환
    private String nvl(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    // 고정 여부를 한글로 변환
    private String fixedToKor(String fixed) {
        if (fixed == null) return "-";
        return "Y".equalsIgnoreCase(fixed) ? "고정" : "일반";
    }

    // 투표글 여부를 한글로 변환
    private String hasVoteToKor(String hasVote) {
        if (hasVote == null) return "-";
        return "Y".equalsIgnoreCase(hasVote) ? "투표" : "일반";
    }

    // 투표 상태 코드를 한글로 변환
    private String voteStatusToKor(String status) {
        if (status == null) return "-";

        switch (status.toUpperCase()) {
            case "O":
                return "진행중";
            case "C":
                return "마감";
            case "N":
                return "없음";
            default:
                return status;
        }
    }
}