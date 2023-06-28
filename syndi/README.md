# Syndi

Syndi reads service status notices from Crossref and DataCite Atom (RSS) feeds, and updates a table in the DB with the results. The DataPortal checks the table and displays any current notices on applicable pages.

## Install

```shell
sudo apt update
sudo apt install libpq-dev
pip install psycopg2
```
