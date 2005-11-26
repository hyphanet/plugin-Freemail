#!/bin/bash
#@+leo
#@+node:0::@file stop-freemail.sh
#@+body
#@@first

# terminates FreeMail

if [[ -f freemail.pid ]]
then
 kill -9 `cat freemail.pid`
 rm freemail.pid
 echo "FreeMail terminated"
else
 echo "FreeMail is not running"
fi


#@-body
#@-node:0::@file stop-freemail.sh
#@-leo
