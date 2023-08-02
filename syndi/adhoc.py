#!/usr/bin/env python

"""Add a notice to display in the Data Portal

This command takes a start and end datetime and a message string. The message is added
to the database and displayed in the Data Portal during the specified time period.

E.g.: ./adhoc.py 2023-05-03 07:30 2023-05-04 15:00 'The message to display'
"""

import argparse
import contextlib
import datetime
import logging
import pathlib
import re
import sys

import jproperties
import psycopg2

log = logging.getLogger(__name__)

PROPERTIES_PATH = '~/git/DataPortal/WebRoot/WEB-INF/conf/dataportal.properties'

DATE_RX = re.compile(r'(\d{4})-(\d{2})-(\d{2})')
TIME_RX = re.compile(r'(\d{2}):(\d{2})')

def main():
    parser = FullHelpArgumentParser()
    parser.add_argument('start_date', help='Start date (YYYY-MM-DD)')
    parser.add_argument('start_time', help='Start time (HH:MM)')
    parser.add_argument('end_date', help='End date (YYYY-MM-DD)')
    parser.add_argument('end_time', help='End time (HH:MM)')
    parser.add_argument('message', help='Notice to display (up to 256 characters)')
    args = parser.parse_args()

    if (not DATE_RX.match(args.start_date)
        or not TIME_RX.match(args.start_time)
        or not DATE_RX.match(args.end_date)
        or not TIME_RX.match(args.end_time)
        or not len(args.message) <= 256
    ):
        parser.print_help()
        return 1

    start_dt = datetime.datetime.fromisoformat(f'{args.start_date}T{args.start_time}:00')
    end_dt = datetime.datetime.fromisoformat(f'{args.end_date}T{args.end_time}:00')

    if start_dt >= end_dt:
        print('Error: Start datetime is at or before end datetime', file=sys.stderr)
        return 1

    print('About to create notice:')
    print()
    print(f'Start:    {start_dt}')
    print(f'End:      {end_dt}')
    print(f'Duration: {end_dt - start_dt}')
    print(f'Message:  {args.message}')
    print()

    answer_str = input('Press Enter to create notice. Press Ctrl-C to cancel: ')

    if answer_str != '':
        print('Cancelled')
        return 0

    with connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                '''
                insert into authtoken.rss_feed (feed_id, published, updated, resolved, site, title, url)
                values (gen_random_uuid(), %s, %s, false, 'adhoc', %s, '')
                returning id
                ''',
                (start_dt, end_dt, args.message.strip()),
            )

            notice_id = cur.fetchone()[0]
            print(f'Inserted notice with ID: {notice_id}')
            print()
            print('To stop displaying this message before the specified end datetime:\n')
            print(f'$ ./resolve.py {notice_id}')

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
