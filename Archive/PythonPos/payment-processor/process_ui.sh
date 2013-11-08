#!/bin/bash 
USER_DIR="${PWD}"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $SCRIPT_DIR
set -v
pyside-uic -i 0 ui/main.ui -o gen_ui_main.py
pyside-uic -i 0 ui/settings.ui -o gen_ui_settings.py
set +v

# go back where we came from
cd $USER_DIR
