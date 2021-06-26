#!/bin/sh

adb shell su -c "echo .dump | sqlite3 /data/data/org.sitebay.android/databases/sitebay"
