/*CREATE DATABASE alexis CHARACTER SET 'utf8mb4' COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES on alexis.* to alexis@'localhost' identified by 'alexis';
USE alexis;*/

CREATE TABLE user(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    username varchar(32),
    hashed_password char(128),
    salt char(64),
    is_admin tinyint(1) DEFAULT 0,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tos_accept_date DATETIME DEFAULT NULL,
    is_analysis_role tinyint(1) DEFAULT 1
);

ALTER TABLE user ADD UNIQUE KEY(username);

/*
--example of creating an admin-level user
--INSERT INTO user(username, salt, is_admin) VALUES('admin', MD5(rand()), 1);
--UPDATE user set hashed_password = sha2(CONCAT('adm1N!', '{', salt, '}'), 256) where username = 'admin';
*/

CREATE TABLE user_api_token (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(36) NOT NULL, 
    last_used TIMESTAMP NOT NULL,
    FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE
);

/*
--example of creating a persistent system-level credential for encrypted system-to-system-communication
--INSERT INTO user_api_token(user_id, token, last_used) 
--    VALUES( (SELECT id FROM user WHERE username = 'admin'), UUID(), '2037-12-31 23:59:59');
*/

CREATE TABLE social_connection(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    remote_username varchar(255),
    oauth_token varchar(2048),
    oauth_token_secret varchar(2048),
    social_network_type CHAR(1) NOT NULL DEFAULT ' ',
    FOREIGN KEY(user_id) REFERENCES user(id)
);

CREATE TABLE source(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    retrieval_date TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY(user_id) REFERENCES user(id)
);

CREATE TABLE rss_feed(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    url TEXT NOT NULL,
    etag TEXT NULL DEFAULT NULL,
    last_modified_date TIMESTAMP NULL DEFAULT NULL,
    retrieval_date TIMESTAMP NULL DEFAULT NULL
);

CREATE INDEX idx_rss_feed_url ON rss_feed(url(512));

CREATE TABLE rss_source(
    id BIGINT NOT NULL PRIMARY KEY,
    rss_feed_id BIGINT,
    FOREIGN KEY(id) REFERENCES source(id),
    FOREIGN KEY(rss_feed_Id) REFERENCES rss_feed(id)
);

CREATE TABLE twitter_source(
    id BIGINT NOT NULL PRIMARY KEY,
    social_connection_id BIGINT NOT NULL,
    last_tweet_id BIGINT,
    FOREIGN KEY(id) REFERENCES source(id),
    FOREIGN KEY(social_connection_id) REFERENCES social_connection(id)
);

CREATE TABLE facebook_source(
    id BIGINT NOT NULL PRIMARY KEY,
    social_connection_id BIGINT NOT NULL,
    FOREIGN KEY(id) REFERENCES source(id),
    FOREIGN KEY(social_connection_id) REFERENCES social_connection(id)
);

CREATE TABLE topic(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name varchar(32),
    description varchar(255),
    user_id BIGINT NOT NULL,
    last_document_match_date TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01',
    search_expression varchar(255) BINARY DEFAULT NULL,
    version smallint unsigned NOT NULL DEFAULT 1,
    FOREIGN KEY(user_id) REFERENCES user(id),
    UNIQUE KEY(name, user_id)
);

CREATE INDEX idx_topic_user_id ON topic(user_id);

CREATE TABLE topic_document(
   id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   topic_id BIGINT,
   document_id CHAR(32) NOT NULL,
   score FLOAT DEFAULT NULL,
   creation_date TIMESTAMP NOT NULL DEFAULT NOW(),
   version SMALLINT UNSIGNED NOT NULL DEFAULT 1,
   FOREIGN KEY(topic_id) REFERENCES topic(id) ON DELETE CASCADE,
   UNIQUE KEY(topic_id, document_id)
);

CREATE INDEX idx_td_creationdate_score ON topic_document(creation_date, score);

CREATE TABLE data_set_type(
    id BIGINT NOT NULL AUTO_INCREMENT,
    is_system bit,
    user_id BIGINT,
    name VARCHAR(64) NOT NULL,
    aggregation_action CHAR(4) NOT NULL DEFAULT 'SUM',
    PRIMARY KEY(id),
    UNIQUE KEY(user_id, name)
);

INSERT INTO data_set_type(is_system, user_id, name, aggregation_action) VALUES(true, NULL, "Topic Activity", "SUM");
INSERT INTO data_set_type(is_system, user_id, name, aggregation_action) VALUES(true, NULL, "Twitter - Followers", "AVG");
INSERT INTO data_set_type(is_system, user_id, name, aggregation_action) VALUES(true, NULL, "Twitter - Following", "AVG");
INSERT INTO data_set_type(is_system, user_id, name, aggregation_action) VALUES(true, NULL, "Stock Quotes", "LAST");
INSERT INTO data_set_type(is_system, user_id, name, aggregation_action) VALUES(true, NULL, "Model Predictions", "AVG");
INSERT INTO data_set_type(is_system, user_id, name, aggregation_action) values (true, NULL, "Polling Data", "AVG");

CREATE TABLE data_set(
    id BIGINT NOT NULL AUTO_INCREMENT,
    data_set_type_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    is_aggregate BIT NOT NULL DEFAULT 0,
    name VARCHAR(64),
    parent_data_set_id BIGINT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(user_id, name),
    FOREIGN KEY(data_set_type_id) REFERENCES data_set_type(id),
    FOREIGN KEY(parent_data_set_id) REFERENCES data_set(id) ON DELETE CASCADE
);

CREATE TABLE topic_activity_data_set(
    id BIGINT NOT NULL,
    topic_id BIGINT,
    PRIMARY KEY(id),
    FOREIGN KEY(id) REFERENCES data_set(id),
    UNIQUE KEY(topic_id),
    FOREIGN KEY(id) REFERENCES data_set(id) ON DELETE CASCADE,
    FOREIGN KEY(topic_id) REFERENCES topic(id) ON DELETE CASCADE
);

CREATE TABLE data_set_point(
    id BIGINT NOT NULL AUTO_INCREMENT,
    data_set_id BIGINT,
    x TIMESTAMP NOT NULL,
    y DOUBLE NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(data_set_id) REFERENCES data_set(id) ON DELETE CASCADE
);

CREATE INDEX idx_data_set_point_all ON data_set_point(data_set_id, x, y);

CREATE TABLE model(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    training_data_set_id BIGINT NOT NULL,
    prediction_data_set_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type CHAR(10) NOT NULL,
    state CHAR(10) NOT NULL DEFAULT 'PENDING',
    start_date TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01',
    end_date TIMESTAMP NOT NULL DEFAULT '2037-12-31 23:59:59',
    lookahead INTEGER NOT NULL DEFAULT 7,
    time_units CHAR(10) NOT NULL DEFAULT 'HOUR',
    FOREIGN KEY(training_data_set_id) REFERENCES data_set(id) ON DELETE CASCADE,
    FOREIGN KEY(prediction_data_set_id) REFERENCES data_set(id) ON DELETE CASCADE,
    FOREIGN KEY(topic_id) REFERENCES topic(id) ON DELETE CASCADE,
    FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY(user_id, name)
);

