"""DB interactions

Strategy:
    - There can be only a single DB entry for a given service being monitored.
    - Check for the entry and create it if it doesn't exist
    - Update the timestamps for the new/existing entry
    - Set its resolved flag to True/False depending on the status of the service

Sample notification record:
    id        | 2
    key       | audit
    start     | 2024-01-10 09:44:07
    stop      | 2024-01-10 10:44:07
    resolved  | f
    message   | HTML, up to 256 characters
"""

import contextlib
import datetime
import logging
import pathlib
import sys

import jproperties
import psycopg2

import config

log = logging.getLogger(__name__)


class Db:
    def __init__(self):
        self._conn = None

    def __enter__(self):
        with self._connect() as conn:
            self._conn = conn
            return self

    def __exit__(self, exc_type, exc_value, traceback):
        self._conn.commit()
        self._conn.close()
        self._conn = None

    # Service

    def update_service_status(self, key_str, is_resolved, msg_str, max_duration_sec):
        if not self._key_exists(key_str):
            self._create_key(key_str)
        self._update_key(key_str, is_resolved, msg_str, max_duration_sec)
        self._conn.commit()

    # Ad-Hoc

    def create_adhoc_notice(self, start_dt, stop_dt, msg_str):
        with self._conn.cursor() as cur:
            cur.execute(
                '''
                insert into authtoken.notification (key, start, stop, resolved, message)
                values(concat('adhoc-', gen_random_uuid()), %(start)s, %(stop)s, False, %(message)s)
                returning id
                ''',
                {
                    'start': start_dt,
                    'stop': stop_dt,
                    'message': msg_str.strip(),
                },
            )
            # Return the new row_id
            return cur.fetchone()[0]

    def is_valid_adhoc_id(self, row_id):
        with self._conn.cursor() as cur:
            cur.execute(
                '''
                select exists (
                    select * 
                    from authtoken.notification 
                    where key like 'adhoc-%%' and id = %(row_id)s
                )
                ''',
                {
                    'row_id': row_id,
                },
            )
            return bool(cur.fetchone()[0])

    def print_unresolved_adhoc(self):
        with self._conn.cursor() as cur:
            cur.execute(
                '''
                select id, start, stop, message
                from authtoken.notification
                where resolved = false
                ''',
            )
            has_unresolved = False
            for id, start, stop, message in cur:
                if not has_unresolved:
                    print('\nUnresolved notices:\n')
                    has_unresolved = True
                print(f'NoticeID {id}: {start} - {stop}: {message}')

            if not has_unresolved:
                print('There are no unresolved notices')

    def is_adhoc_resolved(self, row_id):
        with self._conn.cursor() as cur:
            cur.execute(
                '''
                select resolved
                from authtoken.notification
                where id = %(row_id)s
                ''',
                {
                    'row_id': row_id,
                },
            )
            return bool(cur.fetchone()[0])

    def set_as_resolved(self, row_id):
        with self._conn.cursor() as cur:
            cur.execute(
                '''
                    update authtoken.notification
                    set resolved = true
                    where id = %(row_id)s
                    returning 1
                    ''',
                {
                    'row_id': row_id,
                },
            )

            if not len(cur.fetchall()):
                print('Error: No rows changed', file=sys.stderr)

    # Misc

    def clear_db(self):
        with self._conn.cursor() as cur:
            # noinspection SqlWithoutWhere
            cur.execute('delete from authtoken.notification')

    #
    # Private
    #

    @contextlib.contextmanager
    def _connect(self):
        prop = self._load_properties()

        with psycopg2.connect(
            dbname=prop.get('db.Name').data,
            user=prop.get('db.User').data,
            password=prop.get('db.Password').data,
            port=5432,
            host=prop.get('db.ServerName').data,
        ) as conn:
            yield conn

    def _load_properties(self):
        prop = jproperties.Properties()
        prop_path = pathlib.Path(config.PROPERTIES_PATH).expanduser()
        with prop_path.open('rb') as prop_file:
            prop.load(prop_file)
        return prop

    def _key_exists(self, key_str):
        with self._conn.cursor() as cur:
            cur.execute(
                '''
                select exists (
                    select *
                    from authtoken.notification
                    where key = %(key)s
                )
                ''',
                {
                    'key': key_str,
                },
            )
            return cur.fetchone()[0]

    def _create_key(self, key_str):
        log.debug(f'Creating entry. key_str="{key_str}"')
        with self._conn.cursor() as cur:
            cur.execute(
                '''
                insert into authtoken.notification (key, start, stop, resolved, message)
                values(%(key)s, now(), now(), True, %(message)s)
                ''',
                {
                    'key': key_str,
                    'message': '[uninitialized]',
                },
            )

    def _update_key(self, key_str, is_resolved, msg_str, max_duration_sec):
        log.debug(
            f'Updating entry: '
            f'key_str="{key_str}" '
            f'is_resolved="{is_resolved}" '
            f'msg_str="{msg_str}" '
            f'max_duration_sec="{max_duration_sec}"'
        )
        with self._conn.cursor() as cur:
            cur.execute(
                '''
                update authtoken.notification
                set start = %(start)s, stop = %(stop)s, resolved = %(resolved)s, message = %(message)s
                where key = %(key)s
                ''',
                {
                    'start': datetime.datetime.now(),
                    'stop': datetime.datetime.now()
                    + datetime.timedelta(seconds=max_duration_sec),
                    'resolved': is_resolved,
                    'message': msg_str.strip(),
                    'key': key_str,
                },
            )
