#@+leo
#@+node:0::@file setup.py
#@+body
from distutils.core import setup
from distutils.extension import Extension

try:
    from Pyrex.Distutils import build_ext
    gotPyrex = 1
except:
    gotPyrex = 0

import sys

sslLibs = ['crypto']
extra_link_args = []

if gotPyrex:
    setup(
      name = "SSLCrypto",
      version = '0.1',
      ext_modules=[ 
        Extension("SSLCrypto", ["src/SSLCrypto.pyx", 'src/die.c'],
                  libraries=sslLibs,
                  extra_link_args=extra_link_args)
        ],
      cmdclass = {'build_ext': build_ext}
    )
else:
    setup(
      name = "SSLCrypto",
      version = '0.1',
      ext_modules=[ 
        Extension("SSLCrypto", ["src/SSLCrypto.c", 'src/die.c'],
                  libraries=sslLibs,
                  extra_link_args=extra_link_args)
        ],
    )

#@-body
#@-node:0::@file setup.py
#@-leo
