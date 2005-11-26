#!/usr/bin/python -i
#@+leo
#@+node:0::@file hackdb.py
#@+body
#@@first
#@@language python

import editobj
import cPickle
import sys

import freenet
import freemail
from freemail import cell, slotmap

import SSLCrypto

if len(sys.argv) == 1:
    dbfile = "freemail.dat"
else:
    dbfile = sys.argv[1]

def load():
    global m
    global db

    m = freemail.freemailServer(database=dbfile)
    db = m.db
    #db = cPickle.load(open(dbfile))

def edit():
    editobj.edit(db).mainloop()

def save():
    cPickle.dump(db, open(dbfile, "wb"), 1)

if __name__ == '__main__':
    load()
    print "database is in variable 'db'"
    print "type 'save()' to save changes, or 'load()' to reload the database"
    print "or, if you're brave, type 'edit()' to run a gui db editor"
    print

#@-body
#@-node:0::@file hackdb.py
#@-leo
