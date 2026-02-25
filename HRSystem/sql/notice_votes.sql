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