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

CREATE TABLE document(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    state TINYINT NOT NULL DEFAULT 1,
    content_length INTEGER NOT NULL DEFAULT -1,
    term_count INTEGER NOT NULL DEFAULT -1,
    md5sum CHAR(32) NOT NULL DEFAULT '',
    creation_date TIMESTAMP DEFAULT '0000-00-00 00:00:00',
    retrieval_date TIMESTAMP NULL DEFAULT NULL,
    last_modified_date TIMESTAMP NULL DEFAULT NULL,
    type CHAR(1) NOT NULL DEFAULT 'W',
    url VARCHAR(4096) NOT NULL DEFAULT '',
    title TEXT,
    description TEXT,
    etag TEXT NULL DEFAULT NULL,
    version SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    indexed BIT NOT NULL DEFAULT 1
);

CREATE INDEX idx_document_url ON document(url(64)) ;
CREATE INDEX idx_document_state ON document(state);
CREATE INDEX idx_document_creation_date ON document(creation_date);

CREATE TABLE document_content(
    document_id BIGINT NOT NULL PRIMARY KEY,
    text mediumblob NOT NULL,
    FOREIGN KEY(document_id) REFERENCES document(id) ON DELETE CASCADE
);

CREATE TABLE document_named_entity(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    name VARCHAR(255),
    count SMALLINT UNSIGNED,
    FOREIGN KEY(document_id) REFERENCES document(id) ON DELETE CASCADE
);

CREATE TABLE tweet(
    id BIGINT NOT NULL PRIMARY KEY,
    remote_tweet_id BIGINT NOT NULL,
    screen_name VARCHAR(255) BINARY NOT NULL,
    full_name VARCHAR(255) BINARY NOT NULL,
    profile_image_url VARCHAR(4096) NOT NULL,
    is_retweet TINYINT(1) DEFAULT 0,
    retweet_user_name VARCHAR(255) DEFAULT NULL,
    FOREIGN KEY(id) REFERENCES document(id),
    UNIQUE KEY(remote_tweet_id)
);

CREATE TABLE tweet_link(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    tweet_id BIGINT NOT NULL,
    start SMALLINT NOT NULL,
    end SMALLINT NOT NULL,
    display_url VARCHAR(2048),
    expanded_url VARCHAR(2048),
    url VARCHAR(2048),
    FOREIGN KEY(tweet_id) REFERENCES tweet(id)
);

CREATE TABLE tweet_mention(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    tweet_id BIGINT NOT NULL,
    start SMALLINT NOT NULL,
    end SMALLINT NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(128) BINARY NOT NULL,
    screen_name VARCHAR(64) BINARY NOT NULL,
    FOREIGN KEY(tweet_id) REFERENCES tweet(id)
);

CREATE TABLE tweet_hashtag(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    tweet_id BIGINT NOT NULL,
    start SMALLINT NOT NULL,
    end SMALLINT NOT NULL,
    hashtag VARCHAR(140) NOT NULL,
    FOREIGN KEY(tweet_id) REFERENCES tweet(id)
);

CREATE TABLE document_user(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY(document_id) REFERENCES document(id),
    FOREIGN KEY(user_id) REFERENCES user(id),
    UNIQUE KEY(document_id, user_id)
);

CREATE INDEX idx_document_user_all ON document_user(document_id DESC, user_id);

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
   document_id BIGINT NOT NULL,
   score FLOAT DEFAULT NULL,
   creation_date TIMESTAMP NOT NULL DEFAULT NOW(),
   version SMALLINT UNSIGNED NOT NULL DEFAULT 1,
   FOREIGN KEY(topic_id) REFERENCES topic(id) ON DELETE CASCADE,
   FOREIGN KEY(document_id) REFERENCES document(id),
   UNIQUE KEY(topic_id, document_id)
);

CREATE INDEX idx_td_creationdate_score ON topic_document(creation_date, score);

CREATE TABLE term(
    id BIGINT NOT NULL AUTO_INCREMENT,
    part_of_speech TINYINT DEFAULT NULL,
    term_value varchar(184) BINARY DEFAULT NULL,
    term_value_lowercase varchar(184) BINARY DEFAULT NULL,
    version TINYINT UNSIGNED NOT NULL DEFAULT 1,
    PRIMARY KEY(id),
    UNIQUE (part_of_speech, term_value)
);

CREATE INDEX idx_term_term_value_lowercase ON term(term_value_lowercase(8));

CREATE TABLE document_term(
    document_id BIGINT NOT NULL,
    term_id BIGINT NOT NULL,
    term_count INTEGER NOT NULL,
    tf_idf FLOAT,
    PRIMARY KEY(document_id, term_id),
    FOREIGN KEY(document_id) REFERENCES document(id),
    FOREIGN KEY(term_id) REFERENCES term(id)
);

CREATE INDEX idx_dt_tf_idf ON document_term(tf_idf);

CREATE TABLE document_association (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    term_a_id BIGINT NOT NULL,
    term_b_id BIGINT NOT NULL,
    association_type TINYINT NOT NULL DEFAULT -1,
    association_count INTEGER NOT NULL DEFAULT 1,
    association_weight FLOAT NOT NULL DEFAULT -1.0,
    PRIMARY KEY(id),
    UNIQUE KEY(document_id, term_a_id, term_b_id, association_type),
    FOREIGN KEY(document_id) REFERENCES document(id),
    FOREIGN KEY(term_a_id) REFERENCES term(id),
    FOREIGN KEY(term_b_id) REFERENCES term(id)
);

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

