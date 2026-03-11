package kr.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import kr.hrsystem.dao.HrAppointmentHistoryDAO;
import kr.hrsystem.dao.LogDAO;

public class HrAppointmentHistory_Admin {

    private BufferedReader br;
    private int adminUserId;
    private int loginLogId;

    private HrAppointmentHistoryDAO historyDao;
    private LogDAO logDao;

    public HrAppointmentHistory_Admin(BufferedReader br, int adminUserId, int loginLogId) {
        this.br = br;
        this.adminUserId = adminUserId;
        this.loginLogId = loginLogId;
        this.historyDao = new HrAppointmentHistoryDAO();
        this.logDao = new LogDAO();

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
            System.out.println("│       🧾 인사발령 이력 관리 (관리자)     │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.println("│  [1] 전체 발령 이력 조회                 │");
            System.out.println("│  [2] 사원별 발령 이력 조회               │");
            System.out.println("│  [3] 유형별 발령 이력 조회               │");
            System.out.println("│  [4] 기간별 발령 이력 조회               │");
            System.out.println("│  [0] 뒤로가기                            │");
            System.out.println("+──────────────────────────────────────────+");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        printHistoryList(historyDao.selectAllHistory());
                        writeViewLog("HR_APPT_HISTORY_ALL", "전체 발령 이력 조회");
                        break;

                    case 2: {
                        System.out.print("조회할 USER_ID (뒤로가기: 0) : ");
                        int userId = Integer.parseInt(br.readLine());

                        if (userId == 0) {
                            
                            break;
                        }

                        printHistoryList(historyDao.selectHistoryByUserId(userId));
                        writeViewLog("HR_APPT_HISTORY_BY_USER", "targetUserId=" + userId);
                        break;
                    }

                    case 3: {
                        System.out.print("유형 입력 (1:부서, 2:직급, 3:재직상태, 뒤로가기:0) : ");
                        int typeNo = Integer.parseInt(br.readLine());

                        if (typeNo == 0) {
                           
                            break;
                        }

                        String changeType = null;
                        if (typeNo == 1) changeType = "DEPT";
                        else if (typeNo == 2) changeType = "POSITION";
                        else if (typeNo == 3) changeType = "EMP_STATUS";

                        if (changeType == null) {
                            System.out.println("❌ 잘못된 유형입니다.");
                            break;
                        }

                        printHistoryList(historyDao.selectHistoryByType(changeType));
                        writeViewLog("HR_APPT_HISTORY_BY_TYPE", "changeType=" + changeType);
                        break;
                    }

                    case 4: {
                        System.out.print("시작일 (YYYY-MM-DD / 뒤로가기: 0) : ");
                        String fromDate = br.readLine().trim();

                        if ("0".equals(fromDate)) {
                            
                            break;
                        }

                        System.out.print("종료일 (YYYY-MM-DD / 뒤로가기: 0) : ");
                        String toDate = br.readLine().trim();

                        if ("0".equals(toDate)) {
                            
                            break;
                        }

                        printHistoryList(historyDao.selectHistoryByDateRange(fromDate, toDate));
                        writeViewLog("HR_APPT_HISTORY_BY_DATE", fromDate + "~" + toDate);
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

    private void printHistoryList(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("조회 결과가 없습니다.");
            return;
        }

        System.out.println();
        printDivider(120);
        System.out.println("인사발령 이력 조회");
        printDivider(120);

        System.out.println(
                pad("번호", 8) +
                pad("대상ID", 10) +
                pad("대상명", 10) +
                pad("유형", 14) +
                pad("변경전", 10) +
                pad("변경후", 10) +
                pad("처리자", 10) +
                pad("변경일시", 18) +
                pad("변경사유", 30) 
                
        );

        System.out.println("-".repeat(120));

        for (Map<String, Object> row : list) {
            int historyId = (Integer) row.get("HISTORY_ID");
            int targetUserId = (Integer) row.get("TARGET_USER_ID");
            String targetUserName = (String) row.get("TARGET_USER_NAME");
            String changeType = (String) row.get("CHANGE_TYPE");

            String beforeLabel = nvl((String) row.get("BEFORE_LABEL"), nvl((String) row.get("BEFORE_VALUE"), "-"));
            String afterLabel = nvl((String) row.get("AFTER_LABEL"), nvl((String) row.get("AFTER_VALUE"), "-"));
            String changedByName = (String) row.get("CHANGED_BY_NAME");
            String changeReason = nvl((String) row.get("CHANGE_REASON"), "-");
            

            Object changedByObj = row.get("CHANGED_BY");
            String changedBy = (changedByName != null && !changedByName.trim().isEmpty())
                    ? changedByName
                    : String.valueOf(changedByObj);

            Timestamp ts = (Timestamp) row.get("CHANGED_AT");
            String changedAt = tsToMinuteStr(ts);

            System.out.println(
                    pad(String.valueOf(historyId), 8) +
                    pad(String.valueOf(targetUserId), 10) +
                    pad(nvl(targetUserName, "-"), 10) +
                    pad(changeTypeToKor(changeType), 14) +
                    pad(beforeLabel, 10) +
                    pad(afterLabel, 10) +
                    pad(changedBy, 10) +
                    pad(changedAt, 18) +
                    pad(changeReason, 30) 
                    
            );
        }

        printDivider(120);
    }

    private void writeViewLog(String actionType, String detail) {
        try {
            logDao.insertActionLog(
                adminUserId,
                "인사발령조회",
                actionType,
                detail,
                "HR_APPOINTMENT_HISTORY",
                null,
                (loginLogId > 0 ? loginLogId : null)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String changeTypeToKor(String type) {
        if (type == null) return "-";

        switch (type) {
            case "DEPT":
                return "부서변경";
            case "POSITION":
                return "직급변경";
            case "EMP_STATUS":
                return "재직상태변경";
            default:
                return type;
        }
    }

    private String nvl(String value, String def) {
        return (value == null || value.trim().isEmpty()) ? def : value;
    }

    private void printDivider(int length) {
        System.out.println("=".repeat(length));
    }

    private String tsToMinuteStr(Timestamp ts) {
        if (ts == null) return "-";
        String s = ts.toString();
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }

    private boolean isWide(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

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
}