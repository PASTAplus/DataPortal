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

# Audit service monitor

# A simple GET request is made to this URL to check if the audit service is up.
AUDIT_URL = 'https://pasta-d.lternet.edu/audit/'
AUDIT_UP_MESSAGE = """The EDI data repository audit service is up and running."""
AUDIT_DOWN_MESSAGE = """
The EDI data repository audit service is currently unavailable. The Data Portal web pages Data
Package Summary (incorrect download statistics) and Data Package Access Report (failure to load
report) may be affected.
"""
# Max time to show the audit status message
AUDIT_PERIOD_SEC = 8 * 60 * 60  # 8 hours
