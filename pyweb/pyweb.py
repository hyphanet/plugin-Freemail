#!/usr/bin/env python1.5
#@+leo
#@+node:0::@file pyweb/pyweb.py
#@+body
#@@first
#@@language python

"""
pyWeb - your Python DHTML framework

Official website: U{http://www.freenet.org.nz/python/pyweb}

Written by David McNab (david at freenet dot org dot nz)
Copyright (C) 2003, David McNab

Released under the GNU General Public License, a copy of which
is included in the pyWeb distribution, and can also be viewed
online at U{http://www.gnu.org/licenses/gpl.html}.

No warranty, express or implied, applies in any way to this code.
Use it strictly at your own risk.
"""


#@+others
#@+node:1::imports
#@+body
# import standard system modules
import sys, os, string, types, cgi, base64, UserDict, pickle, zlib, sha
import traceback, time

#import random # sourceforge's python crashes with this

from StringIO import StringIO

# import local supporting modules
import hmac
import CookieSmall
#from bencode import bencode, bdecode

#sys.path.append("..")

write = sys.stdout.write # get faster access to stdout

from pdb import set_trace

#@-body
#@-node:1::imports
#@+node:2::GLOBALS
#@+body
# set globals here

# set this to a unique string which others can't guess
pickleSignatureKey = "try to guess this, asshole!"

# Sets the desired compression level for the datastore cookie
# 0 for no compression, 1 is minimal, ..., 9 is maximal
# higher compression is slowest
pickleCompressLevel = 7

# set this to 1 to case debugging info to appear at the top of pages
createDebugText = 0

pyweb_version = "0.1.0"

try:
    tmp = False
except:
    True = 1
    False = 0

#@-body
#@-node:2::GLOBALS
#@+node:3::class attr
#@+body
class attr:
    """
    Convenience class which allows a list of tag attributes to be
    put first during tag construction, instead of at the end as is
    normally required by python syntax.
    
    Example:
    
      - The following two are identical::
      
        table(tr(td("top row")),
              tr(td("bottom row")),
              cellspacing=5, cellpadding=5, border=2)
        
        table(attr(cellspacing=5, cellpadding=5, border=2),
              tr(td("top row")),
              tr(td("bottom row")),
              )
    """
    
    _is_pyweb_attr = 1

    def __init__(self, **kw):
        """
        attr object constructor. Refer class docstring
        """
        self.kw = kw
        return
        self.kw = {}
        attrs = self.kw
        for k, v in kw.items():
            if k[-1] == '_':
                k = k[:-1]
            attrs[k] = v

#@-body
#@-node:3::class attr
#@+node:4::class save
#@+body
class save:
    """
    Convenience class for saving a widget, under a named attrib of another widget
    """
    _is_pyweb_save = 1

    def __init__(self, widget, attr):
        """
        Constructs a 'save' object, which marks a widget to be set as an attribute
        of another widget.
        
        Arguments:
         - widget - parent widget in which to store child widget ref as an attribute
         - attr - name of the attribute to be created in the parent widget
        """
        if hasattr(widget, '_is_pyweb_webwidget') and type(attr) is types.StringType:
            self.widget = widget
            self.attr = attr
        elif hasattr(attr, '_is_pyweb_webwidget') and type(widget) is types.StringType:
            # cope gracefully if caller has parms in wrong order
            self.widget = attr
            self.attr = widget
        else:
            raise Exception

#@-body
#@-node:4::class save
#@+node:5::class tagid
#@+body
class tagid:
    """
    Marker class for setting a tag object's 'id' (which allows later location of
    the object through subscripting the parent object).
    """
    _is_pyweb_tagid = 1
    
    def __init__(self, name):
        self.value = name


#@-body
#@-node:5::class tagid
#@+node:6::class webwidget
#@+body
class webwidget:
    """
    This is the base class for all web widgets
    """

    #@+others
    #@+node:1::attribs
    #@+body
    delimiter = "\n"
    notagclose = False
    _is_pyweb_webwidget = True
    
    #@-body
    #@-node:1::attribs
    #@+node:2::__init__
    #@+body
    def __init__(self, *content, **kw):
        """
        Generic widget constructor.
        Creates a widget, and optionally stores content.
        
        Arguments:
          - any number of content items - strings or nested widgets
        
        Keywords:
          - delimiter - the delimiter between contents of widget, default is newline
          - savein - ref oa another widget into which to store a ref to this widget
          - saveas - name to save this widget
        
        Example::
    
          table(tr(td("contents of cell",
                      savein=parentwidget,
                      saveas='thiscell')))
    
        More:
          - Instead of the 'savein' and 'saveas' keywords, you can simply use
            'save(widget, name)' as one of the items. For example::
    
              table(tr(td(save(parentwidget, 'thiscell'),
                          "Contents of cell")))
        """
        #self.subtags = {}
    
        # Thanks vincent delft for this touch
        # allows content to be added via the 'content' keyword, which
        # provides an alternative to using the 'attr' class
        if kw.has_key('content'):
            content = kw['content']
            if type(content) != type([]):
                content=[content]
            del kw['content']
    
        # save a ref to this widget into another widget as an attrib, if needed
        for c in content:
            if hasattr(c, '_is_pyweb_attr'):
                kw.update(c.kw)
    
        # get save parms from keywords
        if kw.has_key('savein') and kw.has_key('saveas'):
            setattr(kw['savein'], kw['saveas'], self)
            del kw['savein']
            del kw['saveas']
    
        # ditto, from the 'save' convenience class
        for c in content:
            if hasattr(c, '_is_pyweb_save'):
                setattr(c.widget, c.attr, self)
            elif hasattr(c, '_is_pyweb_tagid'):
                #print "setting tagid=%s" % c.value
                self.__dict__['tagid'] = c.value
    
        # assume remaining keywords to be tag attribs
        self.attr = kw
    
        tagopen = getattr(self, 'tagopen', kw.get('tagopen', self.__class__.__name__))
        tagclose = getattr(self, 'tagclose', kw.get('tagclose', self.__class__.__name__))
     
        #print "taginit: tagopen=%s tagclose=%s" % (tagopen, tagclose)
        self.__dict__['tagopen'] = tagopen
        self.__dict__['tagclose'] = tagclose
    
        # start with no content
        self.content = []
    
        # add any given content
        for c in content:
            if not hasattr(c, '_is_pyweb_attr') \
            and not hasattr(c, '_is_pyweb_save') \
            and not hasattr(c, '_is_pyweb_tagid'):
                self.add(c)
    #@-body
    #@-node:2::__init__
    #@+node:3::__getattr__
    #@+body
    def __getattr__(self, name):
    
        #print "trying to retrieve attribute '%s'" % name
        
        if name in ['__reduce__', '__getstate__', '__module__', '__getinitargs__',
                    'attr', 'content']:
            raise AttributeError
    
        elif self.attr.has_key(name):
            return self.attr[name]
        else:
            for item in self.content:
                if hasattr(item, '_is_pyweb_webwidget'):
                    try:
                        val = getattr(item, 'tagid')
                        if name == val:
                            return item
                    except:
                        pass
        raise Exception("Subscripted for nonexistent item '%s'" % name)
    
    
    #@-body
    #@-node:3::__getattr__
    #@+node:4::__setattr__
    #@+body
    def __setattr__(self, attr, val):
        if self.__dict__.has_key(attr):
            #print attr
            #print self.__dict__[attr]
            if attr == 'content':
                self.__dict__[attr] = list(val)
            elif attr in ['tagopen', 'tagclose', 'parent']:
                #print "adding tagopen/tagclose"
                self.__dict__[attr] = val
            else:
                self.attr[attr] = val
        elif attr in ['tagopen', 'tagclose', 'attr', 'delimiter',
                      'content', 'parent', 'notagclose', 'tagid']:
            #print "setting object attrib '%s' to '%s'" % (attr, val)
            self.__dict__[attr] = val
        else:
            #print "setting tag attrib '%s' to '%s'" % (attr, val)
            self.attr[attr] = val
    
    #@-body
    #@-node:4::__setattr__
    #@+node:5::__getitem__
    #@+body
    def __getitem__(self, name):
        # support numerical subscript
        if type(name) is types.IntType:
            return self.content[name]
    
        # support named subscript, as set up by tagid() calls
        for item in self.content:
            if hasattr(item, '_is_pyweb_webwidget'):
                try:
                    val = getattr(item, 'tagid')
                    if name == val:
                        return item
                except:
                    pass
        raise Exception("Subscripted for nonexistent item '%s'" % name)
    
    #@-body
    #@-node:5::__getitem__
    #@+node:6::__len__
    #@+body
    def __len__(self):
        return len(self.content)
    
    #@-body
    #@-node:6::__len__
    #@+node:7::__nonzero__
    #@+body
    def __nonzero__(self):
        return len(self.content) > 0
    
    #@-body
    #@-node:7::__nonzero__
    #@+node:8::__str__
    #@+body
    def __str__(self):
        return self.render()
    
    #@-body
    #@-node:8::__str__
    #@+node:9::add
    #@+body
    def add(self, *items, **kw):
        """
        Add content to a widget.
        """
        if len(items) == 1 and kw.has_key('_id'):
            self.__setattr__(kw['_id'], items[0])
            self.content.append(self.__getattr__(kw['_id']))
            del kw['_id']
        else:
            for item in items:
                if hasattr(item, '_is_pyweb_webwidget'):
                    item.parent = self
                    self.content.append(item)
                elif hasattr(item, '_is_pyweb_attr'):
                    self.attr.update(item.kw)
                elif hasattr(item, '_is_pyweb_tagid'):
                    self.__dict__['tagid'] = item.value
                else:
                    self.content.append(item)
    
    #@-body
    #@-node:9::add
    #@+node:10::addnamed
    #@+body
    def addnamed(self, name, *item, **kw):
        """
        Adds a named item in the scope of the current tag
        """
        #if len(item) != 1:
        #    raise Exception("Can only add 1 item with addnamed()")
        kw['_id'] = name
        apply(self.add, item, kw)
    
    
    #@-body
    #@-node:10::addnamed
    #@+node:11::addglobal
    #@+body
    def addglobal(self, name, item):
        self.addnamed(name, item)
        if hasattr(item, '_is_pyweb_webwidget'):
            item.parent = self
        widget = self
        while 1:
            #print "addglobal: tag='%s'" % self.tagopen
            if widget.__dict__.has_key('parent'):
                widget = widget.parent
                if hasattr(widget, '_is_pyweb_webwidget'):
                    setattr(widget, name, item)
            else:
                break
    
    #@-body
    #@-node:11::addglobal
    #@+node:12::set
    #@+body
    def set(self, *items):
        """
        Replaces widget's contents with given items
        """
        self.content = list(items)
    
    #@-body
    #@-node:12::set
    #@+node:13::setattr
    #@+body
    def setattr(self, *args, **kw):
        """
        Sets attributes of this widget
        """
        for arg in args:
            if hasattr(arg, '_is_pyweb_attr'):
                self.attr.update(arg.kw)
            elif type(arg) is type({}):
                self.attr.update(arg)
        self.attr.update(kw)
    
    #@-body
    #@-node:13::setattr
    #@+node:14::attrdump
    #@+body
    def attrdump(self):
        """
        Returns a space-separated list of the widget's tag attribs
        """
        attrs = []
        for k in self.attr.keys():
            #print "attrdump: attr['%s'] = '%s'" % (k, self.attr[k])
            attrs.append(k + "=" + '"' + str(self.attr[k]) + '"')
        return string.join(attrs, " ")
    
    
    
    #@-body
    #@-node:14::attrdump
    #@+node:15::attrdumpinto
    #@+body
    def attrdumpinto(self, bucket):
        """
        renders the tag attributes into the bucket
        """
        for k in self.attr.keys():
            #print "attrdump: attr['%s'] = '%s'" % (k, self.attr[k])
            bucket.append(" " + k + "=" + '"' + str(self.attr[k]) + '"')
    
    #@-body
    #@-node:15::attrdumpinto
    #@+node:16::render
    #@+body
    def render(self):
        """
        Recursively colate all the content, and content within the content,
        into one array of string fragments
        """
        tmp = []
        self.renderinto(tmp)
        return string.join(tmp, '')
    
    #@-body
    #@-node:16::render
    #@+node:17::renderinto
    #@+body
    def renderinto(self, bucket):
        """
        Writes string fragments into list 'bucket', for later joining.
    
        If opening and closing tags (eg "<p>" and "</p>") are present,
        encloses all the content within these tags
        """
        #attribs = self.attrdump()
    
        ishtml = hasattr(self, '_is_pyweb_html')
    
        # output the opening tag, if any (there usually is one)
        if ishtml:
            bucket.append("<html>")
        elif self.tagopen:
            bucket.append(self.delimiter)
            bucket.append("<" + self.tagopen)
            self.attrdumpinto(bucket)
            bucket.append(">")
    
        # dump out the tag's content
        bucket.append(self.delimiter)
        self._renderinto(bucket, self.content)
    
        # output the closing tag, if any
        if ishtml:
            bucket.append("</html>")
        elif self.tagclose and not self.notagclose:
            #bucket.append(self.delimiter)
            bucket.append("</" + self.tagclose + ">")
            bucket.append(self.delimiter)
    
    
    
    #@-body
    #@-node:17::renderinto
    #@+node:18::_renderinto
    #@+body
    def _renderinto(self, bucket, *items, **kw):
        for item in items:
            if hasattr(item, '_is_pyweb_webwidget'):
                item.renderinto(bucket)
            elif type(item) in [type(0), type('0')]:
                bucket.append(str(item))
            elif type(item) is types.ListType or type(item) is types.TupleType:
                for element in item:
                    self._renderinto(bucket, element)
    
    #@-body
    #@-node:18::_renderinto
    #@+node:19::dump
    #@+body
    def dump(self):
        """
        Writes string fragments to stdout
    
        If opening and closing tags (eg "<p>" and "</p>") are present,
        encloses all the content within these tags
        """
        #attribs = self.attrdump()
    
        # output the opening tag, if any (there usually is one)
        ishtml = hasattr(self, '_is_pyweb_html')
        if ishtml:
            write("<html>")
        elif self.tagopen:
            write(self.delimiter)
            write("<" + self.tagopen)
            self.attrToStdout()
            write(">")
    
        # dump out the tag's content
        write(self.delimiter)
        self._dump(self.content)
    
        # output the closing tag, if any
        if ishtml:
            write("</html>")
        elif self.tagclose and not self.notagclose:
            #bucket.append(self.delimiter)
            write("</" + self.tagclose + ">")
            write(self.delimiter)
    
    #@-body
    #@-node:19::dump
    #@+node:20::_dump
    #@+body
    def _dump(self, *items, **kw):
        for item in items:
            if hasattr(item, '_is_pyweb_webwidget'):
                item.dump()
            elif type(item) in [type(0), type('0')]:
                write(str(item))
            elif type(item) is types.ListType or type(item) is types.TupleType:
                for element in item:
                    self._dump(element)
    
    #@-body
    #@-node:20::_dump
    #@+node:21::attrToStdout
    #@+body
    def attrToStdout(self):
        """
        Returns a space-separated list of the widget's tag attribs
        """
        attrs = []
        for k in self.attr.keys():
            #print "attrdump: attr['%s'] = '%s'" % (k, self.attr[k])
            write(" " + k + '="' + str(self.attr[k]) + '"')
    
    #@-body
    #@-node:21::attrToStdout
    #@+node:22::join
    #@+body
    def join(self, args):
        """
        What a weird yet consummately pythonic method! Read on...
    
        This method renders out the tag object, and uses that as the
        separator for a join.
    
        For example:
    
         - br().join(['Line 1', 'Line 2', 'Line 3']) produces
    
           Line 1
           <br>
           Line 2
           <br>
           Line 3
        """
        tag = notag()
        nargs = len(args)
        i = 0
        while i < nargs:
            arg = args[i]
            tag.add(arg)
            if i == nargs-1:
                break
            i = i + 1
            tag.add(self)
        return tag
    
    #@-body
    #@-node:22::join
    #@-others


#@-body
#@-node:6::class webwidget
#@+node:7::class _fields
#@+body
class _fields:
    """
    Class to simplify access to form and URL fields

    Note that this class wraps the class cgi.FieldStorage,
    adding the ability to create new fields, and set the values
    of existing fields.

    I added this to provide the ability for script front-ends to
    manipulate the http fields which lower levels of scripts will
    see. This allows, for instance, patching in some default values.
    """
    def __init__(self, **kw):
        self.__dict__['_backup'] = {}
        self.__dict__['_deleted'] = []

        env = os.environ
        streamin = kw.get('streamin', sys.stdin)
        #set_trace()
        self.__dict__['_fields'] = cgi.FieldStorage(fp=streamin, environ=env)

    def __getattr__(self, name):
        """
        Allows form/URL fields to be fetched as attributes.
        A bit of magic - if a field doesn't exist, this method returns
        a null string ('') - a bit of PHP-like behaviour for better or worse
        """
        if name in ['__getinitargs__', '__reduce__', '__getstate__', '__module__',
                    '_fields']:
            raise AttributeError

        return self.get(name)

    def __setattr__(self, name, val):
        self[name] = val

    def __getitem__(self, name):
        return self._backup.get(name, self.get(name))

    def __setitem__(self, item, val):
        self._backup[item] = val
        if item in self._deleted:
            self._deleted.remove(item)

    def __delitem__(self, idx):
        if idx in self._backup.keys():
            del self._backup[idx]
        self._deleted.append(idx)
        
    def __repr__(self):
        return repr(self._fields)

    def get(self, name, defval=''):
        """
        """
        #logmsg("_fields.get: name='%s'" % name)

        if name in self._deleted:
            return defval

        if self._fields.has_key(name):
            fld = self._fields[name]
            try:
                return fld.value
            except:
                if type(fld) is type([]):
                    lst = []
                    for v in fld:
                        lst.append(v.value)
                    return lst
                else:
                    return str(self._fields[name])
        elif self._backup.has_key(name):
            return self._backup[name]
        else:
            return defval

    def getint(self, name, defval=-1):
        try:
            val = int(self._fields[name].value)
        except:
            #fd = open("/tmp/blah", "a")
            #traceback.print_exc(file=fd)
            #fd.close()
            val = defval
        return val
            
    def has_key(self, name):
        if name in self._deleted:
            return 0
        else:
            return self._backup.has_key(name) or self._fields.has_key(name)

    def keys(self):
        return self._backup.keys() + self._fields.keys()

fields = _fields




#@-body
#@-node:7::class _fields
#@+node:8::class _datastore
#@+body
class _datastore(UserDict.UserDict):
    """
    Behaves like a dict, but allows values to be set and retrieved
    as attributes.
    """
    def __getattr__(self, name):
        #print "__getattr__: trying to get '%s'" % name
        if name in ['__reduce__', '__getstate__', '__module__', 'data']:
            raise AttributeError
        try:
            return UserDict.UserDict.__getattr__(self, name)
        except:
            if name in ['__reduce__', '__getstate__', '__module__']:
                raise AttributeError
                        
            if name == 'data':
                #print "fetching 'data'"
                return self.data
            return self.data[name]
    
    def __setattr__(self, name, value):
        #print "__setattr__: setting '%s' to '%s'" % (name, value)
        if name not in ['data', '__reduce__', '__getstate__', '__module__']:
            self[name] = value
        else:
            self.__dict__[name] = value

datastore = _datastore

#@-body
#@-node:8::class _datastore
#@+node:9::class cssdef
#@+body
class cssdef:
    content = {}
    
    def __init__(self, **kw):
        self.content.update(kw)
    
    def __setitem__(self, item, val):
        self.content[item] = val

    def __getitem__(self, item):
        return self.content[item]

    def renderinto(self, bucket):
        items = []
        for k, v in self.content.items():
            items.append("%s: %s" % (k, v))
        all = string.join(items, "; ")
        bucket.append(all)

#@-body
#@-node:9::class cssdef
#@+node:10::class httpenv
#@+body
class httpenv:
    """
    Instances of this class hold all pertinent information relating to
    the current http request.

    Info includes:
     - URL variables (from GET req)
     - POSTed variables
     - cookies
    """

    def __init__(self, **kw):
        """
        Creates an http env object, containing form, URL and cookie data
        """
        self.cookies = myCookie()
        try:
            self.cookies.load(os.environ['HTTP_COOKIE'])
        except:
            pass

        streamin = kw.get('streamin', sys.stdin)
        self.fields = _fields(streamin=streamin)

        # bucket for writing in log messages
        # set up 'status' object for debug logging
        self.debugtxt = table(attr(cellspacing=0, cellpadding=4, border=1))
        self.debugtxt.add(tr(td(attr(align='center', bgcolor='#ffe0e0'),
                              b("Debug messages"))))

        # this error string gets output at the top of the page
        self.error = ''
        self.debugEnabled = 1

        # expiry time of datastore cookie(s)
        self.dataExpiry = -1

        # add a dict of data, which gets stored in client browser as 
        # a cookie called '__data'
        try:
            #self.data = _datastore(dictUnpack(self.cookies['__data'].value))
            self.retrieveData()
        except:
            #print "FAIL!!", str(self.cookies)
            self.data = _datastore()

        #cgi.print_environ()

        self.env = {}
        for name in ['DOCUMENT_ROOT', 'HTTP_HOST', 'HTTP_USER_AGENT',
                     'PATH', 'QUERY_STRING', 'REDIRECT_QUERY_STRING',
                     'REMOTE_ADDR', 'REMOTE_PORT', 'REQUEST_METHOD',
                     'REQUEST_URI', 'SCRIPT_FILENAME', 'SCRIPT_NAME',
                     'SERVER_ADDR', 'SERVER_NAME', 'SERVER_PORT',
                     'SERVER_PROTOCOL', 'HTTP_X_FORWARDED_FOR']:
            self.env[name] = os.environ.get(name, '')

    def retrieveData(self):
        """
        Extracts from cookies '__data', '__data_0', '__data_1', ...
        the encoded dictionary that forms the attribute 'data'.
        
        Authenticates this encoded dictionary via hmac/sha1
        
        refer to the saveData method for info of how the data dict
        gets broken into multiple cookies
        """

        # create new datastore object
        self.data = _datastore()

        # barf if user hasn't changed password
        if pickleSignatureKey == 'default':
            self.seterror("keynotset")
            return
                
        # break up into fields
        try:
            hdr = self.cookies['__data'].value
            nchunks = int(hdr[0:3])
            signature = hdr[3:43]
            #signature = "*" * 40
        except:
            self.seterror("nocookie")
            return

        # is all stored in the header?
        try:
            if nchunks == 0:
                encoded = hdr[43:]
            else:
                # build data from the chunks
                chunks = []
                for i in range(nchunks):
                    chunk = self.cookies['__data_%03d' % (i+1)].value
                    #logmsg("rx chunk %d = %s" % (i, chunk))
                    chunks.append(chunk)
                encoded = string.join(chunks, "")
        except:
            self.seterror("badformat")
            return

        #logmsg("rx: data = %s" % encoded)

        # validate against signature
        if hmac.new(pickleSignatureKey, encoded, sha).hexdigest() != signature:
            self.seterror("badsignature")
            return
        
        # seems safe - unpickle it
        try:
            datadict = safeUnpickleDict(encoded)
            for k in datadict.keys():
                self.data[k] = datadict[k]
        except:
            self.seterror("unpackerror")

    def saveData(self):
        """
        Extracts our 'data' attribute, encodes it, signs it, and stores
        it into one or more cookies in the form '__data', '__data_0',
        '__data_1', ...
        
        If there's a lot of data, it will need to be broken up into multiple
        cookies. So here's the encoding scheme:
            - if the pickled data size < 4096 - 40 - 3, then the format is:
              '000sd', where 's' is the hmac/sha1 signature in hex (40 bytes),
              and 'd' is the actual data
            - if the pickled data size is larger, then the format of the
              '__data' cookie is 'nnns', where 'nnn' is the number of cookie
              blocks expressed in decimal (3 chars), s is the 40-char
              hmac/sha1 signature, and the actual data blocks are stored
              in subsequent cookies named '__data_001', ..., '__data_nnn'
        """
        maxCookieSize = 2048
        encodeddata = safePickleDict(self.data)
        encodeddataLen = len(encodeddata)

        # sign it against our secret key
        signature = hmac.new(pickleSignatureKey, encodeddata, sha).hexdigest()
        #signature = '*' * 40 # temporary

        #logmsg("tx Data = %s" % encodeddata)
               
        # Can we fit the data into a single cookie?
        if encodeddataLen < maxCookieSize - 40 - 3:
            # yep - format it into the header cookie
            datahdr = "000" + signature + encodeddata
            self.cookies['__data'] = datahdr
            if self.dataExpiry >= 0:
                self.cookies['__data']['expires'] = self.dataExpiry

        else:
            # no - break it up into subsequent cookies
            nchunks = (encodeddataLen + maxCookieSize - 1) / maxCookieSize # parts thereof
            datahdr = "%03d%s" % (nchunks, signature)
            self.cookies['__data'] = datahdr
            #print "SETTING EXPIRY"
            if self.dataExpiry >= 0:
                self.cookies['__data']['expires'] = self.dataExpiry
            #print "EXPIRY SET"
            for i in range(nchunks):
                chunk = encodeddata[i*maxCookieSize : (i+1)*maxCookieSize]
                #logmsg("tx Chunk %d = %s" % (i, chunk))
                self.cookies['__data_%03d' % (i+1)] = chunk
                if self.dataExpiry >= 0:
                    self.cookies['__data_%03d']['expires'] = self.dataExpiry


    def setDataExpiry(self, seconds):
        """
        Sets the expiry date of the cookie
        """
        self.dataExpiry = seconds

    def seterror(self, errtype):
        """
        Formats a loud error message for insertion at the top of the page
        """
        errors = {'keynotset' : "Please set the variable 'pickleSignatureKey' in pyweb.py to something secure",
                  'nocookie' : "The datastore cookies are not present in the client browser",
                  'badformat' : "The raw datastore cookie came back in an invalid format",
                  'badsignature' : "The cookie signature is invalid - tampering could be involved",
                  'unpackerror' : "The cookie pickle has an invalid format - tampering could be involved",
                  }

        self.error = errtype        
        self.errortext = table(attr(bgcolor='white', border=2, cellspacing=0, cellpadding=5, align='center'),
                               tr(td(attr(colspan=2, align="center"),
                                     h1("Site Datastore Error"))),
                               tr(td(h2(errtype)),
                                  td(errors.get(errtype, "No information available"))))

    def log(self, msg):
        if self.debugEnabled:
            self.debugtxt.add(tr(td(attr(align='left', bgcolor='#ffe0e0'), msg)))

#@-body
#@-node:10::class httpenv
#@+node:11::class notag
#@+body
class notag(webwidget):
    """
    'Tag-less' container for storing content.
    """
    delimiter = ''

    def __init__(self, *content):
        args = [self]
        args.extend(list(content))
        apply(webwidget.__init__, args)
        self.tagopen = None
        self.tagclose = None
        #apply(self.add, content)



#@-body
#@-node:11::class notag
#@+node:12::HTML TAG CLASSES
#@+node:1::A-B
#@+node:1::class a
#@+body
class a(webwidget):
    """Creates an HTML '<A>' tag"""
    delimiter=""


#@-body
#@-node:1::class a
#@+node:2::class abbr
#@+body
class abbr(webwidget):
    """Creates an HTML '<ABBR>' tag"""
    pass


#@-body
#@-node:2::class abbr
#@+node:3::class acronum
#@+body
class acronym(webwidget):
    """Creates an HTML '<ACRONYM>' tag"""
    pass


#@-body
#@-node:3::class acronum
#@+node:4::class address
#@+body
class address(webwidget):
    """Creates an HTML '<ADDRESS>' tag"""
    pass

#@-body
#@-node:4::class address
#@+node:5::class applet
#@+body
class applet(webwidget):
    """Creates an HTML '<APPLET>' tag"""
    pass


#@-body
#@-node:5::class applet
#@+node:6::class area
#@+body
class area(webwidget):
    """Creates an HTML '<AREA>' tag"""
    pass


#@-body
#@-node:6::class area
#@+node:7::class b
#@+body
class b(webwidget):
    """Creates an HTML '<B>' tag"""
    pass



#@-body
#@-node:7::class b
#@+node:8::class base
#@+body
class base(webwidget):
    """Creates an HTML '<BASE>' tag"""
    pass

#@-body
#@-node:8::class base
#@+node:9::class basefont
#@+body
class basefont(webwidget):
    """Creates an HTML '<BASEFONT>' tag"""
    pass


#@-body
#@-node:9::class basefont
#@+node:10::class bdo
#@+body
class bdo(webwidget):
    """Creates an HTML '<BDO>' tag"""
    pass


#@-body
#@-node:10::class bdo
#@+node:11::class big
#@+body
class big(webwidget):
    """Creates an HTML '<BIG>' tag"""
    pass


#@-body
#@-node:11::class big
#@+node:12::class blockquote
#@+body
class blockquote(webwidget):
    """Creates an HTML '<BLOCKQUOTE>' tag"""
    pass

#@-body
#@-node:12::class blockquote
#@+node:13::class body
#@+body
class body(webwidget):
    """
    Creates the BODY section for the HTML document.

    Typically you won't create this yourself, because it
    automatically gets instantiated when you create an http object.
    """
    def __init__(self, **kw):
        webwidget.__init__(self)

    def __setattr__(self, attr, val):
        if attr in ['text', 'link', 'alink', 'vlink', 'bgcolor', 'background',
                   ]:
            self.attr[attr] = val
        else:
            webwidget.__setattr__(self, attr, val)

#@-body
#@-node:13::class body
#@+node:14::class br
#@+body
class br(webwidget):
    """Creates an HTML '<BR>' tag"""
    notagclose = True


#@-body
#@-node:14::class br
#@+node:15::class button
#@+body
class button(webwidget):
    """Creates an HTML '<BUTTON>' tag"""
    pass


#@-body
#@-node:15::class button
#@-node:1::A-B
#@+node:2::C-F
#@+node:1::class caption
#@+body
class caption(webwidget):
    """Creates an HTML '<CAPTION>' tag"""
    pass


#@-body
#@-node:1::class caption
#@+node:2::class center
#@+body
class center(webwidget):
    """Creates an HTML '<CENTER>' tag"""
    delimiter = ""


#@-body
#@-node:2::class center
#@+node:3::class cite
#@+body
class cite(webwidget):
    """Creates an HTML '<CITE>' tag"""
    pass


#@-body
#@-node:3::class cite
#@+node:4::class code
#@+body
class code(webwidget):
    """Creates an HTML '<CODE>' tag"""
    pass


#@-body
#@-node:4::class code
#@+node:5::class col
#@+body
class col(webwidget):
    """Creates an HTML '<COL>' tag"""
    pass


#@-body
#@-node:5::class col
#@+node:6::class colgroup
#@+body
class colgroup(webwidget):
    """Creates an HTML '<COLGROUP>' tag"""
    pass


#@-body
#@-node:6::class colgroup
#@+node:7::class del_
#@+body
class del_(webwidget):
    """Creates an HTML '<DEL>' tag"""
    pass



#@-body
#@-node:7::class del_
#@+node:8::class dfn
#@+body
class dfn(webwidget):
    """Creates an HTML '<DFN>' tag"""
    pass


#@-body
#@-node:8::class dfn
#@+node:9::class div
#@+body
class div(webwidget):
    """Creates an HTML '<DIV>' tag"""
    pass

#@-body
#@-node:9::class div
#@+node:10::class em
#@+body
class em(webwidget):
    """Creates an HTML '<EM>' tag"""
    pass


#@-body
#@-node:10::class em
#@+node:11::class fieldset
#@+body
class fieldset(webwidget):
    """Creates an HTML '<FIELDSET>' tag"""
    pass


#@-body
#@-node:11::class fieldset
#@+node:12::class font
#@+body
class font(webwidget):
    """Creates an HTML '<FONT>' tag"""
    pass


#@-body
#@-node:12::class font
#@+node:13::class form
#@+body
class form(webwidget):
    """Creates an HTML '<FORM>' tag"""
    pass


#@-body
#@-node:13::class form
#@+node:14::class frameset
#@+body
class frameset(webwidget):
    """Creates an HTML '<FRAMESET>' tag"""
    pass


#@-body
#@-node:14::class frameset
#@-node:2::C-F
#@+node:3::H-I
#@+node:1::class h1
#@+body
class h1(webwidget):
    """Creates an HTML '<H1>' tag"""
    delimiter = ''

#@-body
#@-node:1::class h1
#@+node:2::class h2
#@+body
class h2(webwidget):
    """Creates an HTML '<H2>' tag"""
    delimiter = ''

#@-body
#@-node:2::class h2
#@+node:3::class h3
#@+body
class h3(webwidget):
    """Creates an HTML '<H3>' tag"""
    delimiter = ''

#@-body
#@-node:3::class h3
#@+node:4::class h4
#@+body
class h4(webwidget):
    """Creates an HTML '<H4>' tag"""
    delimiter = ''

#@-body
#@-node:4::class h4
#@+node:5::class h5
#@+body
class h5(webwidget):
    """Creates an HTML '<H5>' tag"""
    delimiter = ''

#@-body
#@-node:5::class h5
#@+node:6::class h6
#@+body
class h6(webwidget):
    """Creates an HTML '<H6>' tag"""
    delimiter = ''

#@-body
#@-node:6::class h6
#@+node:7::class head
#@+body
class head(webwidget):
    """
    Creates an HTML '<HEAD>' tag
    
    Typically you won't instantiate this yourself, because it automatically
    gets created as an attribute of http objects.
    """
    def __init__(self, **kw):
        webwidget.__init__(self)
        self.title = "None so far"
        self.style = style()

    def add(self, *items):
        for item in items:
            if hasattr(item, '_is_pyweb_title'):
                self.__dict__['title'] = item.content[0]
            else:
                webwidget.add(self, item)

    def __setattr__(self, attr, val):
        if attr in ['title', 'style']:
            #print "setting title to %s" % value
            self.__dict__[attr] = val
        else:
            webwidget.__setattr__(self, attr, val)

    def renderinto(self, bucket):
        """
        Kludged to automatically insert the <title> tag
        """
        bucket.append("<head")
        self.attrdumpinto(bucket)
        bucket.append(">\n")
        bucket.append("<title>" + self.title + "</title>\n")
        self.style.renderinto(bucket)
        bucket.append("\n")
        webwidget._renderinto(self, bucket, self.content)
        bucket.append("</head>")

    def dump(self):
        """
        Kludged to automatically insert the <title> tag
        """
        write("<head")
        self.attrToStdout()
        write(">\n")
        write("<title>" + self.title + "</title>\n")
        self.style.dump()
        write("\n")
        webwidget._dump(self, self.content)
        write("</head>")

#@-body
#@-node:7::class head
#@+node:8::class hr
#@+body
class hr(webwidget):
    """Creates an HTML '<HR>' tag"""
    notagclose = True



#@-body
#@-node:8::class hr
#@+node:9::class html
#@+body
class html(webwidget):
    """
    This is the top-level html page widget.
    
    Subclass off this to create your own pages
    """
    dtd = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">"
    _is_pyweb_html = 1
    
    def __init__(self, *args, **kw):
        webwidget.__init__(self)

        self.head = head();
        webwidget.add(self, self.head)

        self.body = body()
        webwidget.add(self, self.body)
        if args:
            self.body.add(args)

    def __setattr__(self, attr, val):
        if attr in ['title']:
            self.head.__setattr__(attr, val)
        elif attr in ['text', 'link', 'alink', 'vlink', 'bgcolor', 'background',
                      ]:
            self.body.__setattr__(attr, val)
        else:
            webwidget.__setattr__(self, attr, val)

    def __getattr__(self, name):
        if name in ['__reduce__', '__getstate__', '__module__', '__getinitargs__']:
            raise AttributeError
        elif name in ['title', 'style']:
            return getattr(self.head, name)
        else:
            return webwidget.__getattr__(self, name)

    def renderinto(self, bucket):
        bucket.append(self.dtd + self.delimiter)
        webwidget.renderinto(self, bucket)

    def dump(self):
        write(self.dtd + self.delimiter)
        webwidget.dump(self)
        write("\n")

    def add(self, *items, **kw):
        apply(self.body.add, items, kw)

    def addglobal(self, name, item):
        self.body.addglobal(name, item)


#@-body
#@-node:9::class html
#@+node:10::class i
#@+body
class i(webwidget):
    """Creates an HTML '<I>' tag"""
    pass



#@-body
#@-node:10::class i
#@+node:11::class iframe
#@+body
class iframe(webwidget):
    """Creates an HTML '<IFRAME>' tag"""
    pass


#@-body
#@-node:11::class iframe
#@+node:12::class img
#@+body
class img(webwidget):
    """Creates an HTML '<IMG>' tag"""
    pass


#@-body
#@-node:12::class img
#@+node:13::class input
#@+body
class input(webwidget):
    """Creates an HTML '<INPUT>' tag"""
    pass


#@-body
#@-node:13::class input
#@+node:14::class ins
#@+body
class ins(webwidget):
    """Creates an HTML '<INS>' tag"""
    pass


#@-body
#@-node:14::class ins
#@+node:15::class isindex
#@+body
class isindex(webwidget):
    """Creates an HTML '<ISINDEX>' tag"""
    pass

#@-body
#@-node:15::class isindex
#@-node:3::H-I
#@+node:4::K-Q
#@+node:1::class kmb
#@+body
class kbd(webwidget):
    """Creates an HTML '<KBD>' tag"""
    pass


#@-body
#@-node:1::class kmb
#@+node:2::class label
#@+body
class label(webwidget):
    """Creates an HTML '<LABEL>' tag"""
    pass


#@-body
#@-node:2::class label
#@+node:3::class legend
#@+body
class legend(webwidget):
    """Creates an HTML '<LEGEND>' tag"""
    pass


#@-body
#@-node:3::class legend
#@+node:4::class li
#@+body
class li(webwidget):
    """Creates an HTML '<LI>' tag"""
    pass


#@-body
#@-node:4::class li
#@+node:5::class link
#@+body
class link(webwidget):
    """Creates an HTML '<LINK>' tag"""
    pass

#@-body
#@-node:5::class link
#@+node:6::class map
#@+body
class map(webwidget):
    """Creates an HTML '<MAP>' tag"""
    pass


#@-body
#@-node:6::class map
#@+node:7::class meta
#@+body
class meta(webwidget):
    """Creates an HTML '<META>' tag"""
    pass

#@-body
#@-node:7::class meta
#@+node:8::class object_
#@+body
class object_(webwidget):
    """Creates an HTML '<OBJECT>' tag"""
    pass



#@-body
#@-node:8::class object_
#@+node:9::class ol
#@+body
class ol(webwidget):
    """Creates an HTML '<OL>' tag"""
    pass


#@-body
#@-node:9::class ol
#@+node:10::class optgroup
#@+body
class optgroup(webwidget):
    """Creates an HTML '<OPTGROUP>' tag"""
    pass


#@-body
#@-node:10::class optgroup
#@+node:11::class option
#@+body
class option(webwidget):
    """Creates an HTML '<OPTION>' tag"""

    delimiter = '\n'

    # gotta frig this to allow a 'selected' attrib word without value
    def __init__(self, *args, **kw):
        if not kw.has_key('selected'):
            kw['selected'] = 0
        selected = kw['selected']
        del kw['selected']
        if not kw.has_key('disabled'):
            kw['disabled'] = 0
        disabled = kw['disabled']
        del kw['disabled']
        apply(webwidget.__init__, (self,) + args, kw)
        self.__dict__['selected'] = selected
        self.__dict__['disabled'] = disabled

    def renderinto(self, bucket):
        # output the opening tag, if any (there usually is one)
        bucket.append(self.delimiter)
        bucket.append("<option")
        self.attrdumpinto(bucket)
        if self.selected:
            bucket.append(" selected")
        if self.disabled:
            bucket.append(" disabled")
        bucket.append(">")

        # dump out the tag's content
        bucket.append(self.delimiter)
        self._renderinto(bucket, self.content)

        # output the closing tag, if any
        bucket.append("</option>")
        bucket.append(self.delimiter)

    def dump(self):
        # output the opening tag, if any (there usually is one)
        write(self.delimiter)
        write("<option")
        self.attrToStdout()
        if self.selected:
            write(" selected")
        if self.disabled:
            write(" disabled")
        write(">")

        # dump out the tag's content
        write(self.delimiter)
        self._dump(self.content)

        # output the closing tag, if any
        write("</option>")
        write(self.delimiter)

#@-body
#@-node:11::class option
#@+node:12::class p
#@+body
class p(webwidget):
    """Creates an HTML '<P>' tag"""
    delimiter = ""


#@-body
#@-node:12::class p
#@+node:13::class param
#@+body
class param(webwidget):
    """Creates an HTML '<PARAM>' tag"""
    pass


#@-body
#@-node:13::class param
#@+node:14::class pre
#@+body
class pre(webwidget):
    """Creates an HTML '<PRE>' tag"""
    pass


#@-body
#@-node:14::class pre
#@+node:15::class q
#@+body
class q(webwidget):
    """Creates an HTML '<Q>' tag"""
    pass



#@-body
#@-node:15::class q
#@-node:4::K-Q
#@+node:5::S
#@+node:1::class s
#@+body
class s(webwidget):
    """Creates an HTML '<S>' tag"""
    pass



#@-body
#@-node:1::class s
#@+node:2::class samp
#@+body
class samp(webwidget):
    """Creates an HTML '<SAMP>' tag"""
    pass


#@-body
#@-node:2::class samp
#@+node:3::class script
#@+body
class script(webwidget):
    """Creates an HTML '<SCRIPT>' tag"""
    pass

#@-body
#@-node:3::class script
#@+node:4::class select
#@+body
class select(webwidget):
    """Creates an HTML '<SELECT>' tag"""
    pass


#@-body
#@-node:4::class select
#@+node:5::class small
#@+body
class small(webwidget):
    """Creates an HTML '<SMALL>' tag"""
    pass


#@-body
#@-node:5::class small
#@+node:6::class span
#@+body
class span(webwidget):
    """Creates an HTML '<SPAN>' tag"""
    delimiter=""


#@-body
#@-node:6::class span
#@+node:7::class strike
#@+body
class strike(webwidget):
    """Creates an HTML '<STRIKE>' tag"""
    pass


#@-body
#@-node:7::class strike
#@+node:8::class strong
#@+body
class strong(webwidget):
    """Creates an HTML '<STRONG>' tag"""
    pass


#@-body
#@-node:8::class strong
#@+node:9::class style
#@+body
class style(notag):
    """Creates an HTML '<STYLE>' tag"""
    
    elements = {}
    raw = []

    def __init__(self, *args):
        webwidget.__init__(self)
        apply(self.add, args)

    def __setitem__(self, item, val):
        self.elements[item] = val

    def add(self, *args):
        for arg in args:
            if type(arg) is types.StringType:
                for part in string.split(arg, '\n'):
                    if part:
                        self.raw.append(part)
            elif type(arg) is types.DictType:
                self.elements.update(arg)
            else:
                raise Exception("Each arg to style init/add must be a string or a dict")

    def renderinto(self, bucket):
        bucket.append("""<style type="text/css" media=screen>\n<!--\n""")
        for item in self.raw:
            bucket.append("  "+item+"\n")
        for k, v in self.elements.items():
            bucket.append("  %s { %s }\n" % (k, v))
        bucket.append("-->\n</style>")
        #webwidget.renderinto(self, bucket)

    def dump(self):
        write("""<style type="text/css" media=screen>\n<!--\n""")
        for item in self.raw:
            write("  "+item+"\n")
        for k, v in self.elements.items():
            write("  %s { %s }\n" % (k, v))
        write("-->\n</style>")

#@-body
#@-node:9::class style
#@+node:10::class sub
#@+body
class sub(webwidget):
    """Creates an HTML '<SUB>' tag"""
    pass


#@-body
#@-node:10::class sub
#@+node:11::class sup
#@+body
class sup(webwidget):
    """Creates an HTML '<SUP>' tag"""
    pass


#@-body
#@-node:11::class sup
#@-node:5::S
#@+node:6::T-V
#@+node:1::class table
#@+body
class table(webwidget):
    """Creates an HTML '<TABLE>' tag"""
    pass


#@-body
#@-node:1::class table
#@+node:2::class tbody
#@+body
class tbody(webwidget):
    """Creates an HTML '<TBODY>' tag"""
    pass


#@-body
#@-node:2::class tbody
#@+node:3::class td
#@+body
class td(webwidget):
    """Creates an HTML '<TD>' tag"""
    delimiter=""


#@-body
#@-node:3::class td
#@+node:4::class textarea
#@+body
class textarea(webwidget):
    """Creates an HTML '<TEXTAREA>' tag"""
    pass


#@-body
#@-node:4::class textarea
#@+node:5::class tfoot
#@+body
class tfoot(webwidget):
    """Creates an HTML '<TFOOT>' tag"""
    pass


#@-body
#@-node:5::class tfoot
#@+node:6::class th
#@+body
class th(webwidget):
    """Creates an HTML '<TH>' tag"""
    pass


#@-body
#@-node:6::class th
#@+node:7::class thead
#@+body
class thead(webwidget):
    """Creates an HTML '<THEAD>' tag"""
    pass


#@-body
#@-node:7::class thead
#@+node:8::class title
#@+body
class title(webwidget):
    """Creates an HTML '<TITLE>' tag"""
    delimiter = ''
    _is_pyweb_title = 1

#@-body
#@-node:8::class title
#@+node:9::class tr
#@+body
class tr(webwidget):
    """Creates an HTML '<TR>' tag"""
    pass


#@-body
#@-node:9::class tr
#@+node:10::class tt
#@+body
class tt(webwidget):
    """Creates an HTML '<TT>' tag"""
    pass


#@-body
#@-node:10::class tt
#@+node:11::class u
#@+body
class u(webwidget):
    """Creates an HTML '<U>' tag"""
    pass



#@-body
#@-node:11::class u
#@+node:12::class ul
#@+body
class ul(webwidget):
    """Creates an HTML '<UL>' tag"""
    pass


#@-body
#@-node:12::class ul
#@+node:13::class var
#@+body
class var(webwidget):
    """Creates an HTML '<VAR>' tag"""
    pass


#@-body
#@-node:13::class var
#@-node:6::T-V
#@-node:12::HTML TAG CLASSES
#@+node:13::class myCookie
#@+body
class myCookie(CookieSmall.SimpleCookie):

    pass

#@-body
#@-node:13::class myCookie
#@+node:14::class http
#@+body
class http(html):
    """
    Subclass of html tag class, which includes HTTP reply headers
    plus cookies in its dump(), as well as parsing cookies upon
    construction

    http objects have a special attribute 'session', of class 'httpenv'.
    Recall that httpenv objects themselves have the following magical
    attributes:

     - cookies - a dict-like object containing cookies. Example:

        page.session.cookies['colour'] = 'red'
        page.session.cookies['colour']['path'] = "/fred"
        page.session.cookies['colour']['expires'] = 1200 (magic - set seconds ahead)

     - fields - another dict-like object containing the fields passed in
       the URL and/or in the form data. This is of type cgi.FieldStorage

     - data - another dict of strings, which gets serialised into a cookie called '__data'
       upon sending to the client. When the client makes another hit, this dict gets
       restored from the '__data' cookie, so this attribute can be used for storing
       any persistent data which can be represented as a string
    """

    def __init__(self, *args, **kw):
        apply(html.__init__, (self,)+args, kw)

        # Add in default header info
        self.contentType = "text/html"
        #self.stream = sys.stdout

        # get file stream settings
        if kw.has_key('stream'):
            self.stream = kw['stream']
            del kw['stream']
        if kw.has_key('streamin'):
            self.streamin = kw['streamin']
            del kw['streamin']
        else:
            self.streamin = sys.stdin

        # Create holder for all session data
        self.session = httpenv(streamin=self.streamin)

                   
    def __setattr__(self, name, val):
        if name in ['stream', 'streamin']:
            self.__dict__[name] = val
        else:
            html.__setattr__(self, name, val)

    def renderinto(self, bucket):
        # create the http headers
        #print "data=", self.session.data

        # shove error at top if any
        #if self.session.error and createDebugText:
        #    self.body.content.insert(0, self.session.errortext)

        # Create a temporary actual dict from self.session.data
        self.session.saveData()
        cookies = str(self.session.cookies)
        if cookies:
            cookies = cookies + self.delimiter
            bucket.append(cookies)

        bucket.append("Content-type: %s\n\n" % self.contentType)
        html.renderinto(self, bucket)

    def dump(self):

        # Create a temporary actual dict from self.session.data
        self.session.saveData()
        cookies = str(self.session.cookies)
        if cookies:
            cookies = cookies + self.delimiter
            write(cookies)

        write("Content-type: %s\n\n" % self.contentType)
        html.dump(self)

    def setstream(self, stream):
        self.__dict__['stream'] = stream

    def send(self):
        stream = self.__dict__.get('stream', sys.stdout)
        stream.write(self.render())
        stream.flush()

    def send1(self):
        global write
        oldwrite = write
        write = self.__dict__.get('stream', sys.stdout.write)
        self.dump()
        write = oldwrite
#@-body
#@-node:14::class http
#@+node:15::class css
#@+body
class css:
    pass

#@-body
#@-node:15::class css
#@+node:16::class saferUnpickler
#@+body
class saferUnpickler(pickle.Unpickler):
    #
    # Derivative of the standard unpickler.
    #
    # Deliberately hobbled to disable unpickling of everything
    # except numbers, strings, tuples, dicts and lists.
    
    # By overriding several methods of the unpickler, we are hopefully taking
    # away the unpickler's ability to stumble on unsafe code.
    
    # Necessary because a tiny minority of people reading this code will be
    # looking to create malformed pickles in an attempt to compromise
    # the security of pyWeb-based websites. Because at worst case, the standard
    # unpickler can be tricked into executing arbitrary python code (and, via the
    # shell interface, any arbitrary code).

    # DO NOT REMOVE THE OVERRIDES BELOW - I TAKE NO RESPONSIBILITY IF YOU FAIL
    # TO HEED THIS ADVICE - YOU HAVE BEEN WARNED!!

    # The result of this hobbling is that any attempt to send tainted cookies
    # will cause the unpickling to generate an exception, which will cause
    # the data store to come back with a special key - refer function safeUnpickle()

    def load_global(self):
        raise Exception("Refuse to unpickle object")

    def load_inst(self):
        raise Exception("Refuse to unpickle class instance")

    def load_obj(self):
        raise Exception("Refuse to unpickle object")

    def find_class(self, module, name):
        raise Exception("Refuse to unpickle class")


#@-body
#@-node:16::class saferUnpickler
#@+node:17::GLOBAL FUNCTIONS
#@+node:1::parse_css
#@+body
def parse_css(thing):
    """
    Parses a CSS declaration
    """

#@-body
#@-node:1::parse_css
#@+node:2::logmsg
#@+body
def logmsg(s):
    try:
        fd = open("debug.log", "a")
    except:
        fd = open("debug.log", "w")
    fd.write(s)
    fd.write("\n")
    fd.flush()
    fd.close()

#@-body
#@-node:2::logmsg
#@+node:3::tag
#@+body
def tag(name=None, **kw):
    t = apply(webwidget, (), kw)
    t.tagopen = name
    t.tagclose = name
    return t

#@-body
#@-node:3::tag
#@+node:4::safePickleDict
#@+body
def safePickleDict(mydict):
    """
    safe to serialise with standard pickler - gotta watch out
    when unpickling.

    Note - after serialising, the data gets base64 encoded to make
    it safe for storage as a cookie
    """

    tmpdict = {}
    for k in mydict.keys():
        tmpdict[k] = mydict[k]

    s = pickle.dumps(tmpdict, 1)
    #s = bencode(s)

    # compress the thing
    if pickleCompressLevel > 0:
        try:
            sOld = s
            s = zlib.compress(s, pickleCompressLevel)
        except:
            logmsg("compress failed")

    #try:
    #    sNew = zlib.decompress(s)
    #    if sNew != sOld:
    #        logmsg("compress verify failed")
    #    else:
    #        logmsg("compress verify ok")
    #except:
    #    logmsg("compress verify exception")

    # create single-line base64 encoding safe for cookie
    s = base64.encodestring(s)
    s = string.replace(s, "\n", "")
    return s

#@-body
#@-node:4::safePickleDict
#@+node:5::safeUnpickleDict
#@+body
def safeUnpickleDict(s):
    """
    A safer unpickler, that uses our saferUnpickler class and
    therefore won't handle any class objects
    """
    s = base64.decodestring(s)

    # uncompress
    if pickleCompressLevel > 0:
        try:
            s = zlib.decompress(s)
            pass
        except:
            logmsg("uncompress failed")

    try:
        #return saferUnpickler(StringIO(s)).load()
        return pickle.loads(s)
        #return bdecode(s)
    except:
        # Caller expects a dict. Regard an unpickling failure as a
        # deliberate attempt to compromise security - in which case
        # we send back a warning via a specially named key
        return None

#@-body
#@-node:5::safeUnpickleDict
#@+node:6::randStr
#@+body
if 0:
    def randStr(nchars):
        """
        Returns a randome string of nchars length
        """
        chrs = []
        for i in range(nchars):
            chrs.append(chr(random.randint(ord("A"), ord("Z"))))
        return string.join(chrs, "")

#@-body
#@-node:6::randStr
#@+node:7::print_exception
#@+body
def print_exception(filename=None):
    if filename:
        fd = open(filename, "a")
        fd.write("---------------------------------------------\n")
        fd.write("Exception generated on %s\n" % time.ctime(time.time()))
        for k, v in os.environ.items():
            if k in ['DOCUMENT_ROOT', 'HTTP_HOST', 'HTTP_USER_AGENT',
                     'PATH', 'QUERY_STRING', 'REDIRECT_QUERY_STRING',
                     'REMOTE_ADDR', 'REMOTE_PORT', 'REQUEST_METHOD',
                     'REQUEST_URI', 'SCRIPT_FILENAME', 'SCRIPT_NAME',
                     'SERVER_ADDR', 'SERVER_NAME', 'SERVER_PORT',
                     'SERVER_PROTOCOL', 'HTTP_X_FORWARDED_FOR']:
                fd.write("%s = '%s'\n" % (k, v))
        fd.write("\n")
        traceback.print_exc(file=fd)
        fd.close()
    else:
        print "Content-type: text/html\n"
        print "<html><head><title>Exception</title>"
        print "<body><h1>Exception</h1><pre>"
        traceback.print_exc(file=sys.stdout)
        print "</pre></body></html>"

#@-body
#@-node:7::print_exception
#@+node:8::urlGetString
#@+body
def urlGetString(url='', **kw):
    """
    Builds a url with http GET args, eg:
    http://mysites.com/myscript.cgi?arg1=val1&arg2=val2...

    Arguments:
     - url - optional - may be absolute or relative

    Keywords:
     - http arguments.

    Example:
     - urlGetString("myscript.cgi",
                    name='myname',
                    myage=43)

       produces the url: 'myscript.cgi?name=myname&myage=43'
    """
    if kw:
        url = url + '?'
        args = []
        for k,v in kw.items():
            args.append(k+"="+v)
        url = url + string.join(args, "&")
    return url

#@-body
#@-node:8::urlGetString
#@+node:9::selectFromList
#@+body
def selectFromList(name, lst, dflt=''):
    """
    Creates a select tag object.

    Arguments:
     - lst - the list of strings to build the select field from
     - dflt - the option to set as the default (optional)
    """
    seltag = select(name=name)
    for item in lst:
        seltag.add(option(item, selected=(item==dflt)))
    return seltag

#@-body
#@-node:9::selectFromList
#@-node:17::GLOBAL FUNCTIONS
#@+node:18::MAINLINE
#@+body
if __name__ == '__main__':
    page = http()
    page.title = "Invalid request"
    page.add("Data not available")
    page.send()

#@-body
#@-node:18::MAINLINE
#@-others


#@-body
#@-node:0::@file pyweb/pyweb.py
#@-leo
