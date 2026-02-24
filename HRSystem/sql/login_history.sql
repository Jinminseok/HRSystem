--로그인 로그 테이블
CREATE TABLE login_history (
    login_log_id     NUMBER PRIMARY KEY,
    user_id          NUMBER,                       -- 로그인 실패 시 null 가능
    login_id         VARCHAR2(30),                -- 입력한 아이디 (실패해도 기록 가능)
    login_time       DATE DEFAULT SYSDATE NOT NULL,
    logout_time      DATE,                        -- 로그아웃 시 업데이트
    login_result     CHAR(1) NOT NULL,            -- S:성공, F:실패
    fail_reason      VARCHAR2(200),               -- 실패 사유(선택)
    program_name     VARCHAR2(50) DEFAULT 'HR_CONSOLE',

    CONSTRAINT fk_login_history_user
        FOREIGN KEY (user_id) REFERENCES usertest(user_id),

    CONSTRAINT ck_login_history_result
        CHECK (login_result IN ('S','F'))
);

CREATE SEQUENCE seq_login_history
START WITH 1
INCREMENT BY 1
NOCYCLE;

--사용자 행동 로그 테이블
CREATE TABLE user_action_log (
    action_log_id      NUMBER PRIMARY KEY,
    user_id            NUMBER NOT NULL,
    action_time        DATE DEFAULT SYSDATE NOT NULL,
    menu_name          VARCHAR2(100) NOT NULL,   -- 예: "공지사항관리"
    action_type        VARCHAR2(50) NOT NULL,    -- 예: "INSERT", "UPDATE", "DELETE", "VIEW", "LOGIN"
    action_desc        VARCHAR2(500),            -- 예: "공지 등록: 워크샵 참석 조사"
    target_table       VARCHAR2(50),             -- 예: "NOTICES"
    target_id          NUMBER,                   -- 예: notice_id
    login_log_id       NUMBER,                   -- 어떤 로그인 세션에서 발생했는지 연결 (선택)

    CONSTRAINT fk_action_user
        FOREIGN KEY (user_id) REFERENCES usertest(user_id),

    CONSTRAINT fk_action_login_log
        FOREIGN KEY (login_log_id) REFERENCES login_history(login_log_id)
);

CREATE SEQUENCE seq_user_action_log
START WITH 1
INCREMENT BY 1
NOCYCLE;