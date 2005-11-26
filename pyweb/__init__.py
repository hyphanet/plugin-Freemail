#@+leo
#@+node:0::@file pyweb/__init__.py
#@+body
from pyweb import *
import hmac

if __name__ == '__main__':
    page = http()
    page.title = "Invalid request"
    page.add("Data not available")
    page.send()


#@-body
#@-node:0::@file pyweb/__init__.py
#@-leo
