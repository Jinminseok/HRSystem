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

