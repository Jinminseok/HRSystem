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

CREATE TABLE notice_votes (
    vote_id        NUMBER PRIMARY KEY,
    notice_id      NUMBER NOT NULL,
    user_id        NUMBER NOT NULL,
    vote_choice    CHAR(1) NOT NULL,
    voted_at       DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT fk_notice_votes_notice
        FOREIGN KEY (notice_id)
        REFERENCES notices(notice_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_notice_votes_user
        FOREIGN KEY (user_id)
        REFERENCES usertest(user_id),

    CONSTRAINT ck_notice_votes_choice
        CHECK (vote_choice IN ('Y','N')),

    CONSTRAINT uk_notice_votes_notice_user
        UNIQUE (notice_id, user_id)
);

CREATE SEQUENCE seq_notice_votes
START WITH 1
INCREMENT BY 1
CACHE 20
NOCYCLE;