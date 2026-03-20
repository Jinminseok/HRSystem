CREATE TABLE usertest (
    USER_ID             NUMBER PRIMARY KEY,
    LOGIN_ID            VARCHAR2(30)  NOT NULL UNIQUE,
    PASSWORD            VARCHAR2(100) NOT NULL,
    USER_NAME           VARCHAR2(20)  NOT NULL,
    DEPT_NUM            NUMBER(10),            -- 관리자 등록 시 채움
    POSITION_NUM        NUMBER(10),            -- 관리자 등록 시 채움
    EMAIL               VARCHAR2(50) UNIQUE,
    PHONE               VARCHAR2(20),
    APPROVAL_STATUS     VARCHAR2(10) DEFAULT 'PENDING' NOT NULL, -- 가입승인대기
    EMP_STATUS          VARCHAR2(10) DEFAULT 'WAIT' NOT NULL,    -- 재직전/대기
    USER_ROLE           VARCHAR2(10) DEFAULT 'USER' NOT NULL,    -- ROLE 대신 USER_ROLE 권장
    JOIN_DATE           DATE DEFAULT SYSDATE NOT NULL,
    USER_MODIFIED_DATE  DATE DEFAULT SYSDATE
);

CREATE SEQUENCE USER_ACCOUNT_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;

INSERT INTO USERTEST (
    USER_ID,
    LOGIN_ID,
    PASSWORD,
    USER_NAME,
    DEPT_NUM,
    POSITION_NUM,
    EMAIL,
    PHONE,
    APPROVAL_STATUS,
    EMP_STATUS,
    USER_ROLE,
    JOIN_DATE,
    USER_MODIFIED_DATE
) VALUES (
    USER_ACCOUNT_SEQ.NEXTVAL,
    'admin',
    '1234',
    '관리자',
    NULL,      -- 부서 FK 전이면 NULL 권장
    NULL,      -- 직급 FK 전이면 NULL 권장
    'admin@test.com',
    '010-0000-0000',
    'APPROVED',
    'WORK',
    'ADMIN',
    SYSDATE,
    SYSDATE
);

COMMIT;