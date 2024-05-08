#!/usr/bin/env python

import logging
import sys

import requests

import config
import db as db_

log = logging.getLogger(__name__)


def main():
    with db_.Db() as db:
        for service_dict in config.SERVICE_LIST:
            is_up = is_service_up(service_dict['check_url'])
            db.update_service_status(
                service_dict['key'],
                is_up,
                'Service is up' if is_up else service_dict['down_message'],
                service_dict['display_period_sec'],
            )


def is_service_up(service_url):
    try:
        response = requests.get(
            service_url,
            timeout=5,
            headers={
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3'
            },
        )
        # print(response.text)
        # print(response.status_code)
        return response.ok or response.status_code in (302, 303)
    except requests.HTTPError:
        return False


if __name__ == '__main__':
    sys.exit(main())
