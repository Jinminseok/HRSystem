
CREATE TABLE SALARY_MANAGEMENT (
    -- 1. 급여 내역 고유 번호 (PK)
    SAL_ID           NUMBER PRIMARY KEY,
    
    -- 2. 외래키 및 식별 정보
    USER_ID          NUMBER NOT NULL,
    POSITION_NUM     NUMBER(10) NOT NULL,
    ATT_ID           NUMBER, -- 근태 기록과 연결 (FK)
    
    -- 3. 급여 항목
    SAL_BASE         NUMBER(10) DEFAULT 0,
    SAL_OVERTIME     NUMBER(10) DEFAULT 0, -- 17시 이후 시간당 150,000원 (절삭 로직 적용 예정)
    SAL_HOLIDAY      NUMBER(10) DEFAULT 0, -- 주말 시간당 250,000원 (절삭 로직 적용 예정)
    SAL_TAX          NUMBER(10) DEFAULT 0, -- 합계의 10%
    
    -- 제약 조건
    CONSTRAINT FK_SAL_USER_C FOREIGN KEY (USER_ID) REFERENCES USERTEST(USER_ID),
    CONSTRAINT FK_SAL_ATT_ID FOREIGN KEY (ATT_ID) REFERENCES ATTENDANCE(ATT_ID)
);

-- 급여 상세용 시퀀스 생성
CREATE SEQUENCE SAL_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;



