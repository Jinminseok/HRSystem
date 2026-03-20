CREATE TABLE notices (
    notice_id        NUMBER PRIMARY KEY,
    notice_title     VARCHAR2(200) NOT NULL,
    notice_content   CLOB NOT NULL,
    user_id          NUMBER NOT NULL,
    created_at       DATE DEFAULT SYSDATE NOT NULL,
    view_count       NUMBER DEFAULT 0 NOT NULL,
    fixed            CHAR(1) DEFAULT 'N' NOT NULL,
    has_vote         CHAR(1) DEFAULT 'N' NOT NULL,
    vote_status      CHAR(1) DEFAULT 'N' NOT NULL,
    vote_deadline    DATE,

    CONSTRAINT fk_notices_usertest
        FOREIGN KEY (user_id)
        REFERENCES usertest(user_id),

    CONSTRAINT ck_notices_fixed
        CHECK (fixed IN ('Y','N')),

    CONSTRAINT ck_notices_has_vote
        CHECK (has_vote IN ('Y','N')),

    CONSTRAINT ck_notices_vote_status
        CHECK (vote_status IN ('N','O','C'))
);

CREATE SEQUENCE seq_notices
START WITH 1
INCREMENT BY 1
CACHE 20
NOCYCLE;

