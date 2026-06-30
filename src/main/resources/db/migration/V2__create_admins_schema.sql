create schema if not exists admins;

CREATE TABLE admins.cms_content
(
    content_id UUID NOT NULL,
    title            VARCHAR(255) NOT NULL,
    cms_content      TEXT         NOT NULL,
    author           VARCHAR(100) NOT NULL,
    content_category VARCHAR(100) NOT NULL,
    date_published   TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    picture_url      VARCHAR(255),
    CONSTRAINT pk_cms_content PRIMARY KEY (content_id)
);