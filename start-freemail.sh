#!/bin/bash
#@+leo
#@+node:0::@file start-freemail.sh
#@+body
#@@first

# start-freemail.sh

if [[ -f freemail.pid ]]
then
  echo "FreeMail is already running - please run stop-freemail.sh first"
  echo
  echo "(if you're absolutely sure FreeMail is not running, you should"
  echo "delete the freemail.pid file and try again)."
  echo
else
  python freemail.py $*
fi

#@-body
#@-node:0::@file start-freemail.sh
#@-leo
