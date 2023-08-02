# Syndi

Syndi reads service status notices from Crossref and DataCite Atom (RSS) feeds, and updates a table in the DB with the results. The DataPortal checks the table and displays any current notices on applicable pages.

## Ad-hoc messages

Syndi also supports ad-hoc notification messages.

To add a notification message to display during a specific time period:

```text
usage: adhoc.py [-h] start_date start_time end_date end_time message

positional arguments:
  start_date  Start date (YYYY-MM-DD)
  start_time  Start time (HH:MM)
  end_date    End date (YYYY-MM-DD)
  end_time    End time (HH:MM)
  message     Notice to display (up to 256 characters)
```

E.g.:
```shell
$ conda activate syndi
$ ./adhoc.py 2023-05-03 07:30 2023-05-04 15:00 'The message to display'
```

To stop displaying a notification message before the specified end datetime:

```
usage: resolve.py [-h] [id]

positional arguments:
  id          NoticeID

options:
  -h, --help  show this help message and exit
```
E.g.:
```shell
$ conda activate syndi
$ ./resolve.py 123
```

## Install

```shell
sudo apt update
sudo apt install libpq-dev

cd ~/git/DataPortal/syndi
wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh
sh Miniconda3-latest-Linux-x86_64.sh
conda update -n base -c defaults conda
conda env create -f environment-min.yml
conda activate syndi

crontab -e

# For running every 5 minutes WITHOUT logging:

*/5 * * * * . ~/miniconda3/etc/profile.d/conda.sh && conda activate syndi && python ~/git/DataPortal/syndi/syndi.py > /dev/null 2>&1

# For running every 5 minutes WITH logging:

*/5 * * * * . ~/miniconda3/etc/profile.d/conda.sh && conda activate syndi && python ~/git/DataPortal/syndi/syndi.py > ~/git/DataPortal/syndi/syndi.log 2>&1
```
