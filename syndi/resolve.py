#!/usr/bin/env python

"""Stop displaying a notice in the Data Portal

This command sets a status notification as "resolved", causing it to no longer
be eligible for displaying in the Data Portal.

To get a list unresolved notices, run without providing an identifier.
"""

import argparse
import contextlib
import datetime
import logging
import pathlib
import sys

import jproperties
import psycopg2

log = logging.getLogger(__name__)

PROPERTIES_PATH = '~/git/DataPortal/WebRoot/WEB-INF/conf/dataportal.properties'


def main():
    parser = FullHelpArgumentParser()
    parser.add_argument('id', nargs='?', type=int, help='NoticeID')
    args = parser.parse_args()

    notice_id = args.id

    if not notice_id:
        print(__doc__, file=sys.stderr)
        parser.print_help()
        print_unresolved()
    else:
        if not is_valid_notice_id(notice_id):
            print(f'Error: Unknown NoticeID: {notice_id}')
            sys.exit(1)
        if is_resolved(notice_id):
            print(f'Error: Notice is already resolved: {notice_id}')
            sys.exit(1)
        set_as_resolved(notice_id)


def print_unresolved():
    with connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                '''
                select id, published, updated, title
                from authtoken.rss_feed
                where resolved = false
                ''',
            )

            row_list = cur.fetchall()
            if row_list:
                print('\nUnresolved notices:\n')
                for notice_id, published, updated, title in row_list:
                    print(f'NoticeID {notice_id}: {published} - {updated}: {title.strip()}')
            else:
                print('There are no unresolved notices')


def is_valid_notice_id(notice_id):
    with connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                '''
                select exists (
                    select * 
                    from authtoken.rss_feed 
                    where id = %s
                )
                ''',
                (notice_id,),
            )
            return bool(cur.fetchone()[0])


def is_resolved(notice_id):
    with connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                '''
                select resolved
                from authtoken.rss_feed
                where id = %s
                ''',
                (notice_id,),
            )
            return bool(cur.fetchone()[0])


def set_as_resolved(notice_id):
    with connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                '''
                update authtoken.rss_feed
                set resolved = true
                where id = %s
                returning 1
                ''',
                (notice_id,),
            )

            if not len(cur.fetchall()):
                print('Error: No rows changed', file=sys.stderr)


@contextlib.contextmanager
def connect():
    prop = jproperties.Properties()
    prop_path = pathlib.Path(PROPERTIES_PATH).expanduser()
    with prop_path.open('rb') as prop_file:
        prop.load(prop_file)

    with psycopg2.connect(
        dbname=prop.get('db.Name').data,
        user=prop.get('db.User').data,
        password=prop.get('db.Password').data,
        port=5432,
        host=prop.get('db.ServerName').data,
    ) as conn:
        yield conn


# Print full help instead of short help on errors.
class FullHelpArgumentParser(argparse.ArgumentParser):
    def error(self, message):
        print(__doc__, file=sys.stderr)
        print(f'\nerror: {message}\n', file=sys.stderr)
        self.print_help()
        sys.exit(1)


if __name__ == '__main__':
    sys.exit(main())
