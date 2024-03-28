CREATE SCHEMA authtoken AUTHORIZATION pasta;


CREATE TABLE authtoken.tokenstore (
  user_id VARCHAR(248) NOT NULL,          -- user id, the primary key
  token VARCHAR(1024) NOT NULL,           -- base64 encoded auth token
  date_created TIMESTAMP DEFAULT now(),   -- insertion/update date/time
  CONSTRAINT token_store_pk PRIMARY KEY (user_id)
);


CREATE TABLE authtoken.saved_data (
  user_id VARCHAR(248) NOT NULL,          -- user id
  scope VARCHAR(100) NOT NULL,            -- the scope
  identifier INT8 NOT NULL,               -- the identifier
  revision INT8 NOT NULL,                 -- the revision
  date_created TIMESTAMP DEFAULT now()    -- insertion date/time
);

-- Syndi RSS feed table

drop table if exists authtoken.rss_feed;

create table authtoken.rss_feed
(
    id serial primary key,
    feed_id char(256) not null constraint feed_id_key unique,
    published timestamp not null,
    updated   timestamp,
    resolved  boolean,
    site      char(32),
    title     char(256),
    url       char(256)
);

alter table authtoken.rss_feed
    owner to pasta;

create index rss_feed_published_index
    on authtoken.rss_feed (published);

create index rss_feed_updated_index
    on authtoken.rss_feed (updated);

create index rss_feed_resolved_index
    on authtoken.rss_feed (resolved);

create index rss_feed_site_index
    on authtoken.rss_feed (site);


