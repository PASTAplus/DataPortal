CREATE SCHEMA authtoken AUTHORIZATION pasta;


CREATE TABLE authtoken.tokenstore (
  user_id VARCHAR(248) NOT NULL,          -- user id, the primary key
  token VARCHAR(1024) NOT NULL,           -- base64 encoded auth token
  edi_token VARCHAR(1024) NOT NULL,       -- base64 encoded EDI token
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

-- Notification table

drop table if exists authtoken.rss_feed;
drop table if exists authtoken.notification;

create table authtoken.notification
(
    id          serial primary key,
    key         char(256) not null constraint notice_key unique,
    start       timestamp not null,
    stop        timestamp not null,
    resolved    boolean,
    message     text
);

alter table authtoken.notification
    owner to pasta;

create index notification_key_index
    on authtoken.notification (key);

create index notification_start_index
    on authtoken.notification (start);

create index notification_stop_index
    on authtoken.notification (stop);

create index notification_resolved_index
    on authtoken.notification (resolved);
