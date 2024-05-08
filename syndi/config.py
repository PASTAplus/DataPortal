import logging
import daiquiri.formatter
import sys

# Set up logging with Daiquiri

daiquiri.setup(
    level=logging.DEBUG,
    outputs=(
        daiquiri.output.Stream(
            sys.stdout,
            formatter=daiquiri.formatter.ColorFormatter(
                fmt='%(asctime)s %(process)d %(levelname)8s %(message)s',
                datefmt='%Y-%m-%d %H:%M:%S',
            ),
        ),
    ),
)

# Increase logging level on loggers that are noisy at debug level
logging.getLogger('requests').setLevel(logging.INFO)
logging.getLogger('urllib3').setLevel(logging.INFO)
logging.getLogger('findfont').setLevel(logging.INFO)


# We pull DB connection details from the DataPortal properties file
PROPERTIES_PATH = '~/git/DataPortal/WebRoot/WEB-INF/conf/dataportal.properties'

# List of RSS feeds to monitor
FEED_LIST = [
    # 'https://status.crossref.org/history.atom',
    'https://status.datacite.org/history.atom',
]

# Services to monitor

SERVICE_LIST = [
    # PASTA Audit
    {
        # This key is used to identify the service in the DB
        'key': 'audit',
        # A simple GET request is made to this URL to check if the service is up.
        'check_url': 'https://pasta-d.lternet.edu/audit/',
        'down_message': (
            "The EDI data repository audit service is currently unavailable. The Data "
            "Portal web pages Data Package Summary (incorrect download statistics) and "
            "Data Package Access Report (failure to load report) may be affected."
        ),
        # Max time to show the audit status message
        'display_period_sec': 8 * 60 * 60,  # 8 hours
    },
    # Crossref
    {
        'key': 'crossref',
        # 10.88888 is a Crossref test DOI
        'check_url': 'https://dx.doi.org/10.88888',
        'down_message': (
            "The Crossref 3rd party service is currently unavailable. This may affect the "
            "Journal Citations page."
        ),
        'display_period_sec': 8 * 60 * 60,
    },
    # DataCite
    {
        'key': 'datacite',
        'check_url': 'https://api.datacite.org/dois/10.14454/FXWS-0523',
        'down_message': (
            "The DataCite 3rd party service is currently unavailable. "
        ),
        'display_period_sec': 8 * 60 * 60,  # 8 hours
    },
]
