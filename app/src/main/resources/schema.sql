DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS participation;
DROP TABLE IF EXISTS chat;

-- chat 테이블
CREATE TABLE chat
(
    id         VARCHAR(64) PRIMARY KEY,
    creator_id VARCHAR(64)  NOT NULL,
    title      VARCHAR(100) NOT NULL
);

-- message 테이블
CREATE TABLE message
(
    id         VARCHAR(64) PRIMARY KEY,
    user_id    VARCHAR(64)  NOT NULL,
    chat_id    VARCHAR(64)  NOT NULL,
    content    VARCHAR(300) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT fk_message_chat FOREIGN KEY (chat_id) REFERENCES chat (id)
);

-- participation 테이블
CREATE TABLE participation
(
    id         VARCHAR(64) PRIMARY KEY,
    user_id    VARCHAR(64) NOT NULL,
    chat_id    VARCHAR(64) NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    CONSTRAINT fk_participation_chat FOREIGN KEY (chat_id) REFERENCES chat (id)
);