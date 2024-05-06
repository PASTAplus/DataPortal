"""Connect to DB with parameters from properties file.
"""

import contextlib
import pathlib

import jproperties
import psycopg2

import config


def clear_db():
    with connect() as conn:
        with conn.cursor() as cur:
            cur.execute('delete from authtoken.rss_feed')

@contextlib.contextmanager
def connect():
    prop = load_properties()

    with psycopg2.connect(
        dbname=prop.get('db.Name').data,
        user=prop.get('db.User').data,
        password=prop.get('db.Password').data,
        port=5432,
        host=prop.get('db.ServerName').data,
    ) as conn:
        yield conn


def load_properties():
    prop = jproperties.Properties()
    prop_path = pathlib.Path(config.PROPERTIES_PATH).expanduser()
    with prop_path.open('rb') as prop_file:
        prop.load(prop_file)
    return prop
