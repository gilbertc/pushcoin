#!/bin/bash
set -e # exit on error
set -x # verbose
sqlite3 assets/databases/bitsypos < assets/databases/schema_bitsypos.sql 
rm -f assets/databases/bitsypos.zip
zip assets/databases/bitsypos.zip assets/databases/bitsypos
lua < conf/breakfast_menu.txt > assets/databases/example.json

