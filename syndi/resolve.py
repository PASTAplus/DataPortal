#!/usr/bin/env python

"""Stop displaying a notice in the Data Portal

This command sets a status notification as "resolved", causing it to no longer
be eligible for displaying in the Data Portal.

To get a list unresolved notices, run without providing an identifier.
"""

import argparse
import logging
import sys

import db as db_

log = logging.getLogger(__name__)


def main():
    parser = FullHelpArgumentParser()
    parser.add_argument('id', nargs='?', type=int, help='NoticeID')
    args = parser.parse_args()

    notice_id = args.id

    with db_.Db() as db:
        if not notice_id:
            print(__doc__, file=sys.stderr)
            parser.print_help()
            print()
            db.print_unresolved_adhoc()
            return 1

        if not db.is_valid_adhoc_id(notice_id):
            print(f'Error: Unknown NoticeID: {notice_id}')
            return 1
        if db.is_adhoc_resolved(notice_id):
            print(f'Error: Notice is already resolved: {notice_id}')
            return 1

        db.set_as_resolved(notice_id)




# Print full help instead of short help on errors.
class FullHelpArgumentParser(argparse.ArgumentParser):
    def error(self, message):
        print(__doc__, file=sys.stderr)
        print(f'\nerror: {message}\n', file=sys.stderr)
        self.print_help()
        return 1


if __name__ == '__main__':
    sys.exit(main())
