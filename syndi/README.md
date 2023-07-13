# Syndi

Syndi reads service status notices from Crossref and DataCite Atom (RSS) feeds, and updates a table in the DB with the results. The DataPortal checks the table and displays any current notices on applicable pages.

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

For running every 5 minutes WITHOUT logging:

*/5 * * * * . ~/miniconda3/etc/profile.d/conda.sh && conda activate syndi && python ~/git/DataPortal/syndi/syndi.py > /dev/null 2>&1

For running every 5 minutes WITH logging:

*/5 * * * * . ~/miniconda3/etc/profile.d/conda.sh && conda activate syndi && python ~/git/DataPortal/syndi/syndi.py > ~/git/DataPortal/syndi/syndi.log
```
