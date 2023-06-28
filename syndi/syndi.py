#!/usr/bin/env python
import contextlib
import datetime
import feedparser
import json
import logging
import pprint
import psycopg2
import sys
import time
import re

log = logging.getLogger(__name__)

SITE_LIST = [
    'https://status.crossref.org/history.atom',
    'https://status.datacite.org/history.atom',
]


def main():
    # clear_db()

    with connect() as conn:
        with conn.cursor() as cur:
            for site_url in SITE_LIST:
                # print('#' * 100)
                # print(site_url)
                d = feedparser.parse(site_url)
                # pprint.pp(d)
                # print(json.dumps(d))
                for entry_dict in d['entries']:
                    # print('-' * 100)
                    # pprint.pp(entry_dict)
                    process_entry(conn, cur, d['feed']['author'], entry_dict)


def process_entry(conn, cur, site_str, entry_dict):
    cur.execute(
        '''
        select exists (
            select *
            from authtoken.rss_feed
            where feed_id = %s
        )
        ''',
        (entry_dict['id'],),
    )

    entry_exists = cur.fetchone()[0]

    if not entry_exists:
        print(f'Creating new: {entry_dict["id"]}')

        cur.execute(
            '''
            insert into authtoken.rss_feed (feed_id, published, updated, resolved, site, title, url)
            values(%s, %s, %s, %s, %s, %s, %s)
            ''',
            (
                entry_dict['id'],
                datetime.datetime.fromtimestamp(
                    time.mktime(entry_dict['published_parsed'])
                ),
                datetime.datetime.fromtimestamp(
                    time.mktime(entry_dict.get('updated_parsed'))
                ),
                is_resolved(entry_dict['summary']),
                site_str,
                entry_dict['title'],
                entry_dict['link'],
            ),
        )
    else:
        print(f'Updating existing: {entry_dict["id"]}')
        print(entry_dict['summary'])
        print(f'RESOLVED: {is_resolved(entry_dict["summary"])}')
        cur.execute(
            '''
            update authtoken.rss_feed
            set updated = %s, resolved = %s
            where feed_id = %s
            ''',
            (
                datetime.datetime.fromtimestamp(
                    time.mktime(entry_dict.get('updated_parsed'))
                ),
                is_resolved(entry_dict['summary']),
                entry_dict['id'],
            ),
        )

    conn.commit()


def clear_db():
    with connect() as conn:
        with conn.cursor() as cur:
            cur.execute('delete from authtoken.rss_feed')


def is_resolved(content_str):
    return bool(re.search(r'<strong>(Completed|Resolved)</strong>', content_str))


@contextlib.contextmanager
def connect():
    with psycopg2.connect(
        dbname='pasta',
        user='pasta',
        password='p@st@',
        port=5432,
        host='localhost',
    ) as conn:
        yield conn


if __name__ == '__main__':
    sys.exit(main())
