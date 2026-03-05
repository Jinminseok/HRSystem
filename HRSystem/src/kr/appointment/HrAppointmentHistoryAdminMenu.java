package kr.appointment;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import kr.hrsystem.dao.LogDAO;

public class HrAppointmentHistoryAdminMenu {

    private BufferedReader br;
    private int adminUserId;
    private int loginLogId;

    private HrAppointmentHistoryDAO historyDao;
    private LogDAO logDao;

    public HrAppointmentHistoryAdminMenu(BufferedReader br, int adminUserId, int loginLogId) {
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
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│          🧾 인사발령 이력 조회 (관리자)     ");
            System.out.println("├─────────────────────────────────────────────");
            System.out.println("│  1. 전체 발령 이력 조회                     ");
            System.out.println("│  2. 사원별 발령 이력 조회                   ");
            System.out.println("│  3. 유형별 발령 이력 조회                   ");
            System.out.println("│  4. 기간별 발령 이력 조회                   ");
            System.out.println("│  0. 뒤로가기                               ");
            System.out.println("└─────────────────────────────────────────────");
            System.out.print("선택 >> ");

            try {
                int no = Integer.parseInt(br.readLine());

                switch (no) {
                    case 1:
                        printHistoryList(historyDao.selectAllHistory());
                        writeViewLog("HR_APPT_HISTORY_ALL", "전체 발령 이력 조회");
                        break;

                    case 2: {
                        System.out.print("조회할 USER_ID (취소: 0) : ");
                        int userId = Integer.parseInt(br.readLine());

                        if (userId == 0) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
                            break;
                        }

                        printHistoryList(historyDao.selectHistoryByUserId(userId));
                        writeViewLog("HR_APPT_HISTORY_BY_USER", "targetUserId=" + userId);
                        break;
                    }

                    case 3: {
                        System.out.print("유형 입력 (1:부서, 2:직급, 3:재직상태, 취소:0) : ");
                        int typeNo = Integer.parseInt(br.readLine());

                        if (typeNo == 0) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
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
                        System.out.print("시작일 (YYYY-MM-DD / 취소: 0) : ");
                        String fromDate = br.readLine().trim();

                        if ("0".equals(fromDate)) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
                            break;
                        }

                        System.out.print("종료일 (YYYY-MM-DD / 취소: 0) : ");
                        String toDate = br.readLine().trim();

                        if ("0".equals(toDate)) {
                            System.out.println("이전 메뉴로 돌아갑니다.");
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println();
        System.out.println("================================================================================================================");
        System.out.printf("%-6s %-8s %-10s %-14s %-20s %-20s %-10s %-19s%n",
                "번호", "대상ID", "대상명", "유형", "변경전", "변경후", "처리자", "변경일시");
        System.out.println("----------------------------------------------------------------------------------------------------------------");

        for (Map<String, Object> row : list) {
            int historyId = (Integer) row.get("HISTORY_ID");
            int targetUserId = (Integer) row.get("TARGET_USER_ID");
            String targetUserName = (String) row.get("TARGET_USER_NAME");
            String changeType = (String) row.get("CHANGE_TYPE");

            String beforeLabel = nvl((String) row.get("BEFORE_LABEL"), nvl((String) row.get("BEFORE_VALUE"), "-"));
            String afterLabel  = nvl((String) row.get("AFTER_LABEL"), nvl((String) row.get("AFTER_VALUE"), "-"));
            String changedByName = (String) row.get("CHANGED_BY_NAME");

            Object changedByObj = row.get("CHANGED_BY");
            String changedBy = (changedByName != null && !changedByName.trim().isEmpty())
                    ? changedByName
                    : String.valueOf(changedByObj);

            Timestamp ts = (Timestamp) row.get("CHANGED_AT");
            String changedAt = (ts == null) ? "-" : sdf.format(ts);

            System.out.printf("%-6d %-8d %-10s %-14s %-20s %-20s %-10s %-19s%n",
                    historyId,
                    targetUserId,
                    cut(nvl(targetUserName, "-"), 10),
                    cut(changeTypeToKor(changeType), 14),
                    cut(beforeLabel, 20),
                    cut(afterLabel, 20),
                    cut(changedBy, 10),
                    changedAt);
        }

        System.out.println("================================================================================================================");
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
            case "DEPT": return "부서변경";
            case "POSITION": return "직급변경";
            case "EMP_STATUS": return "재직상태변경";
            default: return type;
        }
    }

    private String nvl(String value, String def) {
        return (value == null || value.trim().isEmpty()) ? def : value;
    }

    private String cut(String s, int len) {
        if (s == null) return "-";
        return s.length() > len ? s.substring(0, len - 1) + "…" : s;
    }
}
