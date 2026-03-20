CREATE TABLE SALARY_PAYMENT (
    PAY_ID          NUMBER PRIMARY KEY,         -- 고유 번호
    USER_ID         NUMBER NOT NULL,            -- 사원 번호
    PAY_MONTH       VARCHAR2(7) NOT NULL,       -- 귀속월 (YYYY-MM)
    ACTUAL_DATE     DATE,                       -- 실제 지급일
    PAY_STATUS      CHAR(1) DEFAULT 'Y',        -- (기록이 있으면 지급된 것이므로 Y)
    
    CONSTRAINT FK_PAY_USER_ID FOREIGN KEY (USER_ID) REFERENCES USER_C(USER_ID),
    -- 한 사원이 같은 달에 중복 지급 처리되지 않도록 유니크 제약 추가
    CONSTRAINT UQ_USER_MONTH UNIQUE (USER_ID, PAY_MONTH)
);

CREATE SEQUENCE PAY_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;