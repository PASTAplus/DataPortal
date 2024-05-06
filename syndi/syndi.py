#!/usr/bin/env python

import datetime
import logging
import re
import sys
import time

import feedparser

import db
import audit_monitor
import config

log = logging.getLogger(__name__)

# Sample feed record:
# id        | 2
# feed_id   | tag:status.datacite.org,2005:Incident/19658784
# published | 2024-01-10 09:44:07
# updated   | 2024-01-10 09:44:07
# resolved  | t
# site      | DataCite
# title     | ElasticSearch upgrade
# url       | https://status.datacite.org/incidents/t7xqzzs55mdf

def main():
    with db.connect() as conn:
        audit_monitor.update_status(conn)

        with conn.cursor() as cur:
            for feed_url in config.FEED_LIST:
                # print('#' * 100)
                # print(feed_url)
                d = feedparser.parse(feed_url)
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




def is_resolved(content_str):
    return bool(re.search(r'<strong>(Completed|Resolved)</strong>', content_str))


if __name__ == '__main__':
    sys.exit(main())
