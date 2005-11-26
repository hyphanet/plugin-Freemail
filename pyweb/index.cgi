#!/usr/bin/env python
#@+leo
#@+node:0::@file pyweb/index.cgi
#@+body
#@@first
#@@language python1.5

# This file exists to stop shady users from trying to poke their noses into the pyweb directory
# Important, since that's where we're storing the cookie verification key

import sys
import traceback

from pyweb import *

page = http()
page.title = "Access denied"
page.add("Invalid request")
page.send()

#@-body
#@-node:0::@file pyweb/index.cgi
#@-leo
