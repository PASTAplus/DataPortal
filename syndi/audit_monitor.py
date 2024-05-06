"""Monitor the status of the audit service

Strategy:
    - There can be only a single DB entry for the audit status.
    - Check for the entry and create it if it doesn't exist
    - Update the timestamps for the new/existing entry
    - Set its resolved flag to True/False depending on the status of the audit service
"""
import datetime
import logging

import requests

import config

log = logging.getLogger(__name__)


# Sample audit record:
# id        | 2
# feed_id   | audit
# published | 2024-01-10 09:44:07
# updated   | 2024-01-10 10:44:07
# resolved  | f
# site      | adhoc
# title     | config.AUDIT_UP_MESSAGE or config.AUDIT_DOWN_MESSAGE
# url       | config.AUDIT_URL


def update_status(conn):
    if not is_audit_entry_exists(conn):
        create_audit_entry(conn)
    update_audit_entry(conn, is_audit_service_up())
    conn.commit()


def is_audit_service_up():
    try:
        return requests.get(config.AUDIT_URL).ok
    except requests.HTTPError:
        return False


def is_audit_entry_exists(conn):
    with conn.cursor() as cur:
        cur.execute(
            '''
            select exists (
                select *
                from authtoken.rss_feed
                where feed_id = 'audit'
            )
            ''',
        )
        return cur.fetchone()[0]


def create_audit_entry(conn):
    log.debug(f'Creating audit entry')
    with conn.cursor() as cur:
        cur.execute(
            '''
            insert into authtoken.rss_feed (feed_id, published, updated, resolved, site, title, url)
            values('audit', now(), now(), True, 'adhoc', %s, %s)
            ''',
            (
                config.AUDIT_UP_MESSAGE,
                config.AUDIT_URL,
            ),
        )


def update_audit_entry(conn, resolved):
    log.debug(f'Updating audit entry: resolved={resolved}')
    with conn.cursor() as cur:
        cur.execute(
            '''
            update authtoken.rss_feed
            set published = %s, updated = %s, resolved = %s, title = %s
            where feed_id = 'audit'
            ''',
            (
                datetime.datetime.now(),
                datetime.datetime.now() + datetime.timedelta(seconds=config.AUDIT_PERIOD_SEC),
                resolved,
                config.AUDIT_UP_MESSAGE if resolved else config.AUDIT_DOWN_MESSAGE,
            ),
        )
