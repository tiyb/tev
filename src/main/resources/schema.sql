DROP TABLE IF EXISTS answer CASCADE;

DROP TABLE IF EXISTS link CASCADE;

DROP TABLE IF EXISTS metadata CASCADE;

DROP TABLE IF EXISTS photo CASCADE;

DROP TABLE IF EXISTS post CASCADE;

DROP TABLE IF EXISTS regular CASCADE;

DROP TABLE IF EXISTS type CASCADE;

DROP TABLE IF EXISTS video CASCADE;

CREATE TABLE answer
(
	post_id BIGINT NOT NULL,
	question LONGVARCHAR NOT NULL,
	answer LONGVARCHAR NOT NULL
);

CREATE TABLE link
(
	post_id BIGINT NOT NULL,
	text LONGVARCHAR NULL,
	url VARCHAR(255) NOT NULL,
	description LONGVARCHAR NULL
);

CREATE TABLE metadata
(
	id INT NOT NULL,
	base_media_path VARCHAR(255) NULL,
	PRIMARY KEY (id)
);

CREATE TABLE photo
(
	id BIGINT NOT NULL IDENTITY,
	post_id BIGINT NOT NULL,
	caption LONGVARCHAR NULL,
	photo_link_url VARCHAR(255) NULL,
	offset VARCHAR(3) NULL,
	width INT NULL,
	height INT NULL,
	url1280 VARCHAR(255) NULL,
	url500 VARCHAR(255) NULL,
	url400 VARCHAR(255) NULL,
	url250 VARCHAR(255) NULL,
	url100 VARCHAR(255) NULL,
	url75 VARCHAR(255) NULL,
	PRIMARY KEY (id)
);

CREATE TABLE type
(
	id BIGINT NOT NULL,
	type VARCHAR(50) NULL,
	PRIMARY KEY (id)
);

CREATE TABLE post
(
	id BIGINT NOT NULL,
	url VARCHAR(255) NULL,
	url_with_slug VARCHAR(255) NULL,
	date_gmt VARCHAR(50) NULL,
	date VARCHAR(50) NULL,
	unixtimestamp BIGINT NULL,
	reblog_key VARCHAR(50) NULL,
	slug VARCHAR(50) NULL,
	is_reblog BOOLEAN NULL,
	tumblelog VARCHAR(50) NULL,
	width INT NULL,
	height INT NULL,
	type BIGINT NOT NULL,
	is_read BOOLEAN DEFAULT FALSE,
	tags LONGVARCHAR NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (type) REFERENCES type(id)
);

CREATE TABLE regular
(
	post_id BIGINT NOT NULL,
	title VARCHAR(255) NULL,
	body LONGVARCHAR NULL
);

CREATE TABLE video
(
	post_id BIGINT NOT NULL,
	content_type VARCHAR(50) NULL,
	extension VARCHAR(4) NULL,
	width INT NULL,
	height INT NULL,
	duration INT NULL,
	revision VARCHAR(50) NULL,
	video_caption LONGVARCHAR NULL
);

CREATE UNIQUE INDEX IXFK_answer_post ON answer (post_id);

CREATE UNIQUE INDEX IXFK_link_post ON link (post_id);

CREATE UNIQUE INDEX IXFK_photo_post ON photo (id);

CREATE UNIQUE INDEX IXFK_regular_post ON regular (post_id);

CREATE UNIQUE INDEX IXFK_video_post ON video (post_id);
