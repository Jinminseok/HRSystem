-- 1. 부서 테이블
CREATE TABLE dept (
    dept_num            NUMBER PRIMARY KEY,
    dept_name           VARCHAR2(50) NOT NULL UNIQUE,
    dept_day            DATE DEFAULT SYSDATE NOT NULL,
    dept_modified_date  DATE
);

-- 2. 시퀀스
CREATE SEQUENCE seq_dept
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- 3. 수정일 자동 반영 트리거
CREATE OR REPLACE TRIGGER trg_dept_modified
BEFORE UPDATE ON dept
FOR EACH ROW
BEGIN
    :NEW.dept_modified_date := SYSDATE;
END;
/

-- 4. 초기 데이터
INSERT INTO dept (dept_num, dept_name) VALUES (seq_dept.NEXTVAL, '인사부');
INSERT INTO dept (dept_num, dept_name) VALUES (seq_dept.NEXTVAL, '개발부');
INSERT INTO dept (dept_num, dept_name) VALUES (seq_dept.NEXTVAL, '영업부');
INSERT INTO dept (dept_num, dept_name) VALUES (seq_dept.NEXTVAL, '마케팅부');
INSERT INTO dept (dept_num, dept_name) VALUES (seq_dept.NEXTVAL, '재무부');

COMMIT;

ALTER TABLE USERTEST
ADD CONSTRAINT fk_usertest_dept
FOREIGN KEY (dept_num)
REFERENCES dept(dept_num);