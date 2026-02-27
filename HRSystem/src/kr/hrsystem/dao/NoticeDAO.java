package kr.hrsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import kr.util.DBUtil;

public class NoticeDAO {

    private LogDAO logDao = new LogDAO();

    // ==========================
    // 공통: 전체 공지 목록 조회
    // ==========================
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

            System.out.println("=".repeat(90));
            System.out.println("번호\t"+"     "+"제목\t\t작성자\t작성일\t"+"     "+"조회수"+"   "+"고정"+"    "+"투표"+"   "+"투표상태");
            System.out.println("=".repeat(90));

            while (rs.next()) {
                System.out.print(rs.getInt("notice_id") + "\t");
                System.out.print(rs.getString("notice_title") + "\t\t");
                System.out.print(rs.getString("user_name") + "\t");
                System.out.print(rs.getString("created_at_str") + "\t");
                System.out.print(rs.getInt("view_count") + "\t");
                System.out.print(rs.getString("fixed") + "\t");
                System.out.print(rs.getString("has_vote") + "\t");
                System.out.print(rs.getString("vote_status") + "\n");
            }

            System.out.println("=".repeat(90));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 특정 사용자 공지 목록 조회 (관리자용)
    // ==========================
    public void selectUserNoticeList(int targetUserId) {
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
                       + "WHERE n.user_id = ? "
                       + "ORDER BY n.notice_id DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, targetUserId);

            rs = pstmt.executeQuery();

            System.out.println("=".repeat(90));
            System.out.println("[ 사원번호 = " + targetUserId + " ] 님의 게시글 목록");
            System.out.println("번호\t"+"     "+"제목\t\t작성자\t작성일\t"+"     "+"조회수"+"   "+"고정"+"    "+"투표"+"   "+"투표상태");
            System.out.println("=".repeat(90));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.print(rs.getInt("notice_id") + "\t");
                System.out.print(rs.getString("notice_title") + "\t\t");
                System.out.print(rs.getString("user_name") + "\t");
                System.out.print(rs.getString("created_at_str") + "\t");
                System.out.print(rs.getInt("view_count") + "\t");
                System.out.print(rs.getString("fixed") + "\t");
                System.out.print(rs.getString("has_vote") + "\t");
                System.out.print(rs.getString("vote_status") + "\n");
            }

            if (!hasData) {
                System.out.println("해당 사용자가 작성한 게시글이 없습니다.");
            }

            System.out.println("=".repeat(90));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 공지 상세 조회 (조회수 증가)
    // ==========================
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

            int count = pstmtUp.executeUpdate();
            if (count == 0) {
                conn.rollback();
                System.out.println("해당 공지가 존재하지 않습니다.");
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
                System.out.println("=".repeat(90));
                System.out.println("번호    : " + rs.getInt("notice_id"));
                System.out.println("제목    : " + rs.getString("notice_title"));
                System.out.println("작성자  : " + rs.getString("user_name"));
                System.out.println("작성일  : " + rs.getString("created_at_str"));
                System.out.println("조회수  : " + rs.getInt("view_count"));
                System.out.println("고정    : " + rs.getString("fixed"));
                System.out.println("투표글  : " + rs.getString("has_vote"));
                System.out.println("투표상태: " + rs.getString("vote_status"));
                System.out.println("투표마감: " + (rs.getString("vote_deadline_str") == null ? "-" : rs.getString("vote_deadline_str")));
                System.out.println("내용    : ");
                System.out.println(rs.getString("notice_content"));
                System.out.println("=".repeat(90));
            }

            conn.commit();

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, null);
            DBUtil.executeClose(null, pstmtUp, conn);
        }
    }

    // ==========================
    // 내 게시글 조회
    // ==========================
    public void selectMyNoticeList(int userId) {
        selectUserNoticeList(userId);
    }

    // ==========================
    // 공지 등록 (관리자/사원 공용)
    // ==========================
    public void insertNotice(String title, String content, int writerUserId,
                             String fixed, String hasVote, String voteDeadlineInput,
                             Integer loginLogId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmtSeq = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String safeFixed = "Y".equalsIgnoreCase(fixed) ? "Y" : "N";
            String safeHasVote = "Y".equalsIgnoreCase(hasVote) ? "Y" : "N";
            String voteStatus = "Y".equals(safeHasVote) ? "O" : "N";
            Timestamp voteDeadline = parseVoteDeadline(voteDeadlineInput);

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

            pstmtSeq = conn.prepareStatement("SELECT seq_notices.CURRVAL AS notice_id FROM dual");
            rs = pstmtSeq.executeQuery();

            Integer noticeId = null;
            if (rs.next()) {
                noticeId = rs.getInt("notice_id");
            }

            System.out.println(count + "개의 게시글이 등록되었습니다.");

            logDao.insertActionLog(
                writerUserId,
                "게시판",
                "NOTICE_CREATE",
                "공지등록 noticeId=" + noticeId + ", fixed=" + safeFixed + ", hasVote=" + safeHasVote,
                "NOTICES",
                noticeId,
                loginLogId
            );

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSeq, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 관리자: 공지 수정 (전체 대상)
    // ==========================
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

            if (!rs.next()) {
                System.out.println("해당 공지가 존재하지 않습니다.");
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
                System.out.println("공지사항이 수정되었습니다.");

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
                System.out.println("해당 공지가 존재하지 않습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 관리자: 공지 삭제 (전체 대상)
    // ==========================
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

            if (!rs.next()) {
                System.out.println("해당 공지가 존재하지 않습니다.");
                return;
            }
            oldTitle = rs.getString("notice_title");

            DBUtil.executeClose(rs, pstmtSel, null);

            String sql = "DELETE FROM notices WHERE notice_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, noticeId);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("공지사항이 삭제되었습니다.");

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
                System.out.println("해당 공지가 존재하지 않습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 관리자: 특정 사용자 글 수정
    // ==========================
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

            if (!rs.next()) {
                System.out.println("해당 공지가 존재하지 않습니다.");
                return;
            }

            ownerUserId = rs.getInt("user_id");
            oldTitle = rs.getString("notice_title");

            if (ownerUserId != targetUserId) {
                System.out.println("❌ 선택한 USER_ID가 작성한 게시글이 아닙니다.");
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
                System.out.println("수정 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 관리자: 특정 사용자 글 삭제
    // ==========================
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

            if (!rs.next()) {
                System.out.println("해당 공지가 존재하지 않습니다.");
                return;
            }

            ownerUserId = rs.getInt("user_id");
            oldTitle = rs.getString("notice_title");

            if (ownerUserId != targetUserId) {
                System.out.println("❌ 선택한 USER_ID가 작성한 게시글이 아닙니다.");
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
                System.out.println("삭제 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 사원: 내 게시글 수정 (본인만)
    // ==========================
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

            if (!rs.next()) {
                System.out.println("❌ 수정 실패: 게시글이 없거나 본인 글이 아닙니다.");
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
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 사원: 내 게시글 삭제 (본인만)
    // ==========================
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

            if (!rs.next()) {
                System.out.println("❌ 삭제 실패: 게시글이 없거나 본인 글이 아닙니다.");
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
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtSel, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 투표 참여 (찬성/반대)
    // ==========================
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

            if (!rs.next()) {
                System.out.println("해당 공지가 없습니다.");
                return;
            }

            String hasVote = rs.getString("has_vote");
            String voteStatus = rs.getString("vote_status");
            Timestamp deadline = rs.getTimestamp("vote_deadline");

            if (!"Y".equalsIgnoreCase(hasVote)) {
                System.out.println("👉 이 공지는 투표글이 아닙니다.");
                return;
            }

            if (!"O".equalsIgnoreCase(voteStatus)) {
                System.out.println("👉 투표가 진행중이 아닙니다.");
                return;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (deadline != null && deadline.before(now)) {
                System.out.println("👉 투표 마감 시간이 지났습니다.");

                // 선택: 자동 마감 처리
                DBUtil.executeClose(rs, pstmtChk, null);

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

            // ✅ 로그 타입 통일: VOTE_CAST
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
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmtChk, null);
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ==========================
    // 관리자용 투표 결과 조회
    // ==========================
    public void selectVoteResult(int noticeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT NVL(SUM(CASE WHEN vote_choice = 'Y' THEN 1 ELSE 0 END), 0) AS YES_CNT, " +
                "       NVL(SUM(CASE WHEN vote_choice = 'N' THEN 1 ELSE 0 END), 0) AS NO_CNT " +
                "FROM notice_votes " +
                "WHERE notice_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, noticeId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("==== 투표 결과 (공지번호: " + noticeId + ") ====");
                System.out.println("찬성 : " + rs.getInt("YES_CNT"));
                System.out.println("반대 : " + rs.getInt("NO_CNT"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // ==========================
    // 투표 마감일시 파싱
    // ==========================
    private Timestamp parseVoteDeadline(String input) {
        if (input == null) return null;
        input = input.trim();
        if (input.length() == 0) return null;

        // 형식: YYYY-MM-DD HH:MM
        // 초 붙여서 Timestamp 변환
        return Timestamp.valueOf(input + ":00");
    }
}
