package kr.hrsystem.user;

public class LoginUser {

    private int userId;
    private String loginId;
    private String userName;
    private String approvalStatus;
    private String userRole;

    public LoginUser(int userId, String loginId, String userName, String approvalStatus, String userRole) {
        this.userId = userId;
        this.loginId = loginId;
        this.userName = userName;
        this.approvalStatus = approvalStatus;
        this.userRole = userRole;
    }

    public int getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getUserName() {
        return userName;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public String getUserRole() {
        return userRole;
    }

    // 승인 여부
    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(approvalStatus);
    }

    // 관리자 여부
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(userRole);
    }
}
