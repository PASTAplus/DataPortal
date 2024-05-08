#!/usr/bin/env python

"""Add a notice to display in the Data Portal

This command takes a start and stop datetime and a message string. The message is added
to the database and displayed in the Data Portal during the specified time period.

E.g.: ./adhoc.py 2023-05-03 07:30 2023-05-04 15:00 'The message to display'
"""

import argparse
import datetime
import logging
import re
import sys

import db as db_

log = logging.getLogger(__name__)

DATE_RX = re.compile(r'(\d{4})-(\d{2})-(\d{2})')
TIME_RX = re.compile(r'(\d{2}):(\d{2})')


def main():
    parser = FullHelpArgumentParser()
    parser.add_argument('start_date', help='Start date (YYYY-MM-DD)')
    parser.add_argument('start_time', help='Start time (HH:MM)')
    parser.add_argument('stop_date', help='Stop date (YYYY-MM-DD)')
    parser.add_argument('stop_time', help='Stop time (HH:MM)')
    parser.add_argument('message', help='Notice to display (HTML supported)')
    args = parser.parse_args()

    if (
        not DATE_RX.match(args.start_date or '')
        or not TIME_RX.match(args.start_time or '')
        or not DATE_RX.match(args.stop_date or '')
        or not TIME_RX.match(args.stop_time or '')
        or not args.message
    ):
        parser.print_help()
        return 1

    start_dt = datetime.datetime.fromisoformat(f'{args.start_date}T{args.start_time}:00')
    stop_dt = datetime.datetime.fromisoformat(f'{args.stop_date}T{args.stop_time}:00')

    if start_dt >= stop_dt:
        print('Error: Start datetime is at or before stop datetime', file=sys.stderr)
        return 1

    print('About to create notice:')
    print()
    print(f'Start:    {start_dt}')
    print(f'Stop:     {stop_dt}')
    print(f'Duration: {stop_dt - start_dt}')
    print(f'Message:  {args.message}')
    print()

    answer_str = input('Press Enter to create notice. Press Ctrl-C to cancel: ')

    if answer_str != '':
        print('Cancelled')
        return 0

    with db_.Db() as db:
        notice_id = db.create_adhoc_notice(
            start_dt,
            stop_dt,
            args.message,
        )

    print(f'Inserted notice with ID: {notice_id}')
    print()
    print('To stop displaying this message before the specified stop datetime:\n')
    print(f'$ ./resolve.py {notice_id}')


# Print full help instead of short help on errors.
class FullHelpArgumentParser(argparse.ArgumentParser):
    def error(self, message):
        print(__doc__, file=sys.stderr)
        print(f'\nerror: {message}\n', file=sys.stderr)
        self.print_help()
        return 1


if __name__ == '__main__':
    sys.exit(main())
