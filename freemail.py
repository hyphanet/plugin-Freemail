#!/usr/bin/env python
#@+leo-ver=4
#@+node:@file freemail.py
#@@first #!/usr/bin/env python
#@@language python

# hi there - if you're reading this in a conventional code editor,
# you can save yourself a lot of pain and angst if you download
# and install the 'Leo' metastructural editor.

# http://leo.sourceforge.net

# once you have Leo installed, simply open the 'code.leo' file
# and you'll see this file appearing as a tidy tree of logical
# code groupings.

# After using Leo, you may never want to use vi or emacs for
# significant editing again.

"""
FreeMail - the Freenet Mail Transfer Agent software.

Written from Sep 2003 by David McNab <david@freenet.org.nz>
Copyright (c) 2003 by David McNab

Released under the GNU General Public License, the text
of which can be found at http://www.gnu.org

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
"""
#@+others
#@+node:Suppress old API warnings
try:
    import warnings
    warnings.filterwarnings('ignore',
                            'Python C API version mismatch',
                            RuntimeWarning,
                            )
    warnings.filterwarnings('ignore',
                            'FCNTL',
                            DeprecationWarning,
                            )
                            
except:
    pass

#@-node:Suppress old API warnings
#@+node:imports
import sys

# vet the python version
pyversOk = 1
if hasattr(sys, 'version_info'):
    # sys.version_info is new in python2.0
    major, minor, micro, rlevel, serial = sys.version_info
    if major == 2:
        if minor < 2:
            pyversOk = 0
        elif minor == 2 and micro < 1:
            pyversOk = 0
if not pyversOk:
    print "You are trying to run FreeMail with Python version:"
    print sys.version
    print "FreeMail requires Python version 2.2.1 or later"
    sys.exit(1)

import os, time, base64, sha, re, string, random, fnmatch
import thread, threading
import socket, SocketServer
import md5, traceback, signal
import BaseHTTPServer
import mimetypes
import getopt
import posixpath
import BaseHTTPServer
import select
import urllib
import cgi
import shutil
import mimetypes
import codecs
import encodings
from StringIO import StringIO
import Queue
from code import InteractiveConsole
try:
    import readline
except:
    pass
import pydoc

#try:
#    import cPickle as pickle
#except:
#    import pickle
import pickle

from pdb import set_trace

# import some other modules that should be with this package
from pyweb import *
import freenet

try:
    import SSLCrypto
    canImportSSLCrypto = 1
except:
    print
    print "***********************************************"
    print "Cannot import the SSLCrypto python extension"
    print
    print "Did you type 'make'?"
    print "Did you follow the instructions in INSTALL?"
    print "***********************************************"
    print
    canImportSSLCrypto = 0
    time.sleep(5)

if canImportSSLCrypto:
    try:
        SSLCrypto.key(256, "ElGamal").exportKeyPrivate()
    except:
        print "SSLCrypto module failing, please contact author"
        print "david@freenet.org.nz"
        print
        print "Traceback follows - send this to author:"
        traceback.print_exc(file=sys.stdout)
        sys.exit(1)

# epydoc seems to need this
#import freemail





#@-node:imports
#@+node:globals
days = 86400 # seconds

# build/protocol indicators

version = "0.1-alpha"
build = "020"
protocolVersion = "1"

newTid = 1

oldStdout = sys.stdout

# these hard-coded constants are mainly for testing

socketTimeout = 1800
reportLocks = 0
mailsiteInsertRetryTime = 300

fcpCycleMinTime = 10
fcpAntiThrashTime = 10
fcpThreadLaunchDelay = 1

fcpRunInThreads = 1
fcpRunServerInThread = 1

receiptSendDelay = 0

# slot state constants

SLOT_STATE_EMPTY = 0
SLOT_STATE_USED = 1
SLOT_STATE_BUSY = 2

# message send state constants

TX_STATUS_NOT_SENT = 0
TX_STATUS_AWAITING_RECEIPT = 1
TX_STATUS_CONFIRMED = 2
TX_STATUS_DEAD = 3


# lock object for writing to "freemail.log.crash"
exceptionLogLock = threading.Lock()

interactivePrompt = "FreeMail> "
#@-node:globals
#@+node:exceptions
class FreeMailCannotSend(Exception):
    "Cannot insert outbound message onto peer queue"

class FreeMailNoPeerInfo(Exception):
    "Cannot retrieve peer info"
#@-node:exceptions
#@+node:class freemailServer
class freemailServer:
    """
    This is the uber-class that runs a freemail server.
    
    It launches the threads for monitoring RTS/CTS queues,
    monitoring inbound message queues, posting outbound
    messages etc.
    
    Note that some people, for some reason, might want to
    run multiple freemail sites on the one machine.
    """
    #@    @+others
    #@+node:__init__
    def __init__(self, **kw):
        """
        Constructs a mailsite object.
        
        Arguments:
         - conf - either a config file, or an instance of a config object.
           defaults to 'freemail.conf'
        
        Keywords:
         - quiet - don't print log msgs to stdout (in addition to logfile)
         - database - database file
         - verbosity - verbosity level for logging, default 2, 0 (quiet) thru 4 (debug)
         - logFile - logging file, default 'freemail.loq'
         - fcpHost - hostname of computer with Freenet node, default localhost
         - fcpPort - port number of FCP port on machine with Freenet node
         - popPort - port number of FreeMail POP3 interface
         - smtpPort - port number of FreeMail SMTP interface
         - httpPort - port number of FreeMail Web (http) interface
         - telnetPort - port number of FreeMail Telnet interface
         - retryInit - initial 
        """
    
        # locate db file
        self.filename = kw.get('database', 'freemail.dat')
    
        # Create the database lock
        self._dbLockObj = threading.RLock()
        self._dbLockLock = threading.Lock()
        self._dbLockStack = []
    
        # set verbosity
        self._verbosity = kw.get('verbosity', 1)
        self._freenetverbosity = kw.get('freenetverbosity', 1)
        freenet.LOGMSG = self.logFreenet
        freenet.verbosity(self._verbosity - 1)
    
        # set up logging
        logfile = kw.get('logfile', "freemail.log")
        #print logfile
        self.logfile = logfile
        self.loglock = threading.Lock()
    
        # set up 'quiet' flag
        self.isQuiet = kw.get('quiet', 0)
    
        # create peer info requests tracker
        self.activePeerRequests = {}
    
        # create or open database
        if not os.path.isfile(self.filename):
            self.log(4, "Creating initial database '%s'" % self.filename)
            self.dbInit(**kw)
    
        self.log(4, "Loading database '%s'" % self.filename)
        try:
            self.dbLoad()
        except:
            self.log(1, exceptionString())
            self.log(1, "*****************\nAAARRRGGGHHH!!!!!\nFreeMail database file '%s' is sorrupted - bailing out" \
                     % self.filename)
            sys.exit(1)
    
        # set socket timeouts
        #socket.setdefaulttimeout(socketTimeout)
    
        self.running = 0
        self.mailServersRunning = 0
    
    #@-node:__init__
    #@+node:runServer
    def runServer(self):
        """
        Runs the server in blocking mode.
    
        In contrast to 'startServer()', this method does not return immediately.
        It blocks until (or unless) Freemail terminates
        """
        self.startServer()
        try:
            while 1:
                time.sleep(3600)
        except KeyboardInterrupt:
            print "FreeMail server terminated by user"
            if os.path.isfile("freemail.pid"):
                os.unlink("freemail.pid")
    #@-node:runServer
    #@+node:startServer
    def startServer(self):
        """
        Launches the mail server
        
        Dispatches all the various threads
    
        If invoked with block=0 (default), returns immediately to the caller,
        so the caller can interact in real-time with the internals of the
        freemailServer object, invoke methods, examine data etc.
        """
    
        self.log(4, "startServer: start")
    
        # we always run the web interface
        if 1:
            self.startWebServer()
        else:
            print "STARTING WEBSERVER IN MAINLINE"
            self.threadHttpServer()
    
        # launch applicable threads here
        
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        state = self.db.config.configState
        httpPort = self.db.config.httpPort
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        if state == 'ready':
            # fully configured - start the rest
            self.startMailServers()
            self.startTelnetServer()
    
            # and fire up the main kernel
            if fcpRunServerInThread:
                self.startFcpEngine()
            else:
                self.threadFcpEngine()
    
        elif state not in ['new', 'security', 'ports']:
            # still configuring - only start the mail servers
            self.startMailServers()
    
        time.sleep(2)
        if state != 'ready':
            print "****************************************************"
            print "Freemail node not (fully) configured"
            print "Please point your browser to http://localhost:%s" % httpPort
            print "to complete the web-based configuration process now"
            print "****************************************************"
            print
            return
    
        print "** Freemail Server version %s build %s now running..." % (version, build)
    
    
    
    #@-node:startServer
    #@+node:startMailServers
    def startMailServers(self):
        """
        Launches the freemail POP/SMTP servers
        """
        if not self.mailServersRunning:
            thread.start_new_thread(self.threadPopServer, ())
            thread.start_new_thread(self.threadSmtpServer, ())
            self.mailServersRunning = 1
    #@-node:startMailServers
    #@+node:startWebServer
    def startWebServer(self):
        """
        Launches the HTTP interface server
        """
        thread.start_new_thread(self.threadHttpServer, ())
        #self.threadHttpServer()
    #@-node:startWebServer
    #@+node:startTelnetServer
    def startTelnetServer(self):
        """
        Launches the Telnet interface server
        """
        thread.start_new_thread(self.threadTelnetServer, ())
    #@-node:startTelnetServer
    #@+node:startFcpEngine
    def startFcpEngine(self):
        """
        Launches the FCP transport thread(s)
        """
        thread.start_new_thread(self.threadFcpEngine, ())
    #@-node:startFcpEngine
    #@+node:stopServer
    def stopServer(self):
        """
        Terminates the mailsite, saves the config
        """
        
        # bring down the threads
        
        # save database
        self.dbSave()
    #@-node:stopServer
    #@+node:threadPopServer
    def threadPopServer(self):
    
        prefix = "popmail"
    
        self.popPrefix = prefix
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        db = self.db
        config = db.config
        popPort = config.popPort
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        try:
            # create dir prefix if nonexistent
            if not os.path.isdir(prefix):
                os.mkdir(prefix)
            for user in db.identities.keys():
                fulldir = os.path.join(prefix, user)
                if not os.path.isdir(fulldir):
                    os.mkdir(fulldir)
    
            # Create a server object
            serv = POPserver(bindaddr='',
                             port=popPort,
                             freemail=self,
                             handlerClass=POPRequestHandler,
                             )
    
        except:
            self.log(1, "Exception:\n%s" % exceptionString())
    
        # and launch it
        try:
            serv.run()
        except:
            self.log(1, "Exception:\n%s" % exceptionString())
    #@-node:threadPopServer
    #@+node:check_user
    def check_user(self, attemptuser):
        """
        Returns 1 if user 'attemptuser' exists, 0 if not
        """
        if len(attemptuser.split("@")) == 1:
            found = ''
            shortid = attemptuser.split("@")[0]
            for id in self.db.identities.keys():
                if id.split("@")[0] == shortid:
                    if found:
                        self.log(3, "Tried to log in with non-unique short-hand id '%s'" % shortid)
                        return ''
                    else:
                        self.log(5, "Shorthand userid '%s' => '%s'" % (shortid, id))
                        found = id
            return found
    
        if self.db.identities.has_key(attemptuser):
            return attemptuser
        else:
            return ''
    
    #@-node:check_user
    #@+node:auth
    def auth(self, attemptuser, passwd):
        """
        Checks if username and password are valid
        """
        attemptuser = self.check_user(attemptuser)
        if not attemptuser:
            self.log(3, "Can't log in as '%s'" % attemptuser)
            return ""
        
        if not self.db.identities.has_key(attemptuser):
            self.log(3, "No identity '%s'" % attemptuser)
            return ""
        userRec = self.db.identities[attemptuser]
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        userPass = userRec.idPopPassword
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
        
        if (not userPass) or (hash(passwd) == userPass):
            # no password on database, no password given
            self.log(4, "User '%s' logged in" % attemptuser)
            return attemptuser
        else:
            self.log(3, "User '%s', bad password '%s'" % (attemptuser, passwd))
            return ""
    #@-node:auth
    #@+node:threadSmtpServer
    def threadSmtpServer(self):
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        db = self.db
        config = db.config
        port = config.smtpPort
        allowedHosts = db.smtpHosts
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        try:
            # Create a server object
            serv = SMTPServer(port=port,
                              allowedHosts=allowedHosts,
                              freemail=self,
                              )
            
            # and launch it
            serv.run()
    
        except:
            self.log(1, "Exception:\n%s" % exceptionString())
    
    #@-node:threadSmtpServer
    #@+node:threadHttpServer
    def threadHttpServer(self):
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        db = self.db
        config = db.config
        httpPort = config.httpPort
        allowedHosts = db.httpHosts
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        self.WebUI = WebUI
    
        try:
            # Create a server object
            serv = HTTPServer(name="Freemail Web Interface",
                              port=httpPort,
                              log=self.log,
                              owner=self,
                              handlerClass=HTTPRequestHandler,
                              allowedHosts=allowedHosts,
                              webUI=WebUI,
                              )
    
            # and launch it
            serv.run()
    
        except:
            self.log(1, "Exception running HTTP Server\n" + exceptionString())
    #@-node:threadHttpServer
    #@+node:threadTelnetServer
    def threadTelnetServer(self):
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        db = self.db
        config = db.config
        telnetPort = config.telnetPort
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        try:
            # Create a server object
            serv = TelnetServer(bindaddr='',
                             port=telnetPort,
                             freemail=self,
                             handlerClass=TelnetRequestHandler,
                             )
    
        except:
            self.log(1, "Exception:\n%s" % exceptionString())
    
        # and launch it
        try:
            serv.run()
        except:
            self.log(1, "Exception:\n%s" % exceptionString())
    #@-node:threadTelnetServer
    #@+node:threadFcpEngine
    def threadFcpEngine(self):
        """
        This thread does the low-level work of the actual Freemail
        mail transport.
    
        Possible situations/events requiring action (in no particular order)::
    
         - an identity's mailsite needs to be refreshed
            - dispatch thread to refresh it
    
         - outbound mail in Unsent queue
            - dispatch thread to:
               - transmit to peer
               - move to 'noreceipt' list
    
         - outbound message in 'noreceipt' list is due for re-send
            - dispatch thread to re-send it
    
         - outbound message in 'noreceipt' list has maxed out retries
            - move to 'dead' list
            - send bounce to user
    
         - found inactive slots within id rx windows
            - dispatch threads to poll these slots
    
        """
        self.running = 1
        print "** Freemail: FCP Engine now running..."
    
        # get/create some handy things
        db = self.db
        config = db.config
        log = self.log
    
        txMaxRetries = config.txMaxRetries
        txBackoffInit = config.txBackoffInit
        txBackoffMult = config.txBackoffMult
    
        # main loop - keep looking for stuff to do, and dispatching tasks
        self._loopcount = 0
        nodefailcount = 0
        while 1:
    
            # ticker message
            if self._loopcount % 1 == 0:
                #log(5, "-------------------------------------------------------")
                log(5, "FCP Engine Thread, top of loop, looking for stuff to do")
            self._loopcount += 1
    
            # make sure node is alive
            nodehandshakefailed = 0
            try:
                node = self.Node()
                try:
                    log(5, "trying node handshake")
                    node._handshake()
                    log(5, "node handshake successful")
                except:
                    if nodefailcount % 90 == 0:
                        loglevel = 2
                    else:
                        loglevel = 5
                    log(loglevel, "Can't talk to freenet node at %s:%s\nRetrying in 10 seconds\n%s" % (
                              config.fcpHost, config.fcpPort, exceptionString()))
                    nodefailcount += 1
                    nodehandshakefailed = 1
    
                if nodehandshakefailed == 0 and nodefailcount > 0:
                    log(2, "Freenet node at %s:%s is back up - now we can get on with it" % (
                                config.fcpHost, config.fcpPort))
                    nodefailcount = 0
            except:
                print "***********************"
                print exceptionString()
                print "***********************"
    
            if nodehandshakefailed:
                log(3, "waiting to retry node handshake")
                time.sleep(10)
                continue
                              
            #set_trace()
            then = time.time()
    
            try:
                self.chugFcpEngine()
            except:
                log(2, "Exception in engine main loop:\n%s" % exceptionString())
    
            now = time.time()
            if now - then < fcpCycleMinTime:
                time.sleep(fcpCycleMinTime - (now - then))
    #@-node:threadFcpEngine
    #@+node:chugFcpEngine
    def chugFcpEngine(self):
        """
        This is the guts of the freemail engine, that gets run inside
        a try/except block by thread FcpEngine.
        
        Performs the checks of things needing to be done, and launches
        threads to perform those tasks.
        """
        db = self.db
        config = db.config
        log = self.log
    
        if fcpRunInThreads:
            # -v-v-v-v-v-v- LOCK DATABASE ----------------
            self._dbLock()
    
        # -----------------------------------------------------
        # refresh identity mailsites that need it
        idRecs = db.identities.values()
        for idRec in idRecs:
            if (not idRec.idRefreshInProgress):
                #if (time.time() - idRec.idLastInserted) >= idRec.idInsertPeriod:
                if (time.time() - idRec.idLastInserted) >= 3600:
                    log(3, "spawning refresh for identity %s" % idRec.idAddr)
                    idRec.idRefreshInProgress = 1
                    if fcpRunInThreads:
                        thread.start_new_thread(self.fcpRefreshMailsite, (idRec.idAddr,))
                    else:
                        self.fcpRefreshMailsite(idRec.idAddr)
    
    
        # -----------------------------------------------------
        # launch threads to poll all the empty (and inactive) rx mail slots
    
        idRecs = db.identities.values()
        for idRec in idRecs:
            # ignore any identities which are currently refreshing
            if idRec.idRefreshInProgress:
                continue
    
            # get slots list
            last = idRec.idUriNext + config.slotLookAhead + 1
            emptySlots = idRec.idSlots.between(idRec.idUriNext, last)
            log(5, "id %s has empty slots %s" % (idRec.idAddr, emptySlots))
            for slot in emptySlots:
                idRec.idSlots[slot] = SLOT_STATE_BUSY
                if fcpRunInThreads:
                    thread.start_new_thread(self.threadFcpPollSlot, (idRec.idAddr, slot))
                else:
                    self.threadFcpPollSlot(idRec.idAddr, slot)
    
        # -----------------------------------------------------
        # launch threads to dispatch the unsent messages
    
        for msg in db.txNotSent:
            now = time.time()
    
            # kludge to squish retryBackoff attribute exceptions
            try:
                retryBackoff = msg.retryBackoff
            except:
                try:
                    retryBackoff = config.txBackoffInit
                except:
                    retryBackoff = 1800
                    config.txBackoffInit = 1800
                msg.retryBackoff = retryBackoff
    
            if now >= msg.lastTry + retryBackoff:
                if not msg.sendInProgress:
                    msg.sendInProgress = 1
                    if fcpRunInThreads:
                        thread.start_new_thread(self.threadFcpDeliverMsg, (msg, db.txNotSent))
                    else:
                        self.threadFcpDeliverMsg(msg, db.txNotSent)
    
        # -----------------------------------------------------
        # launch threads to retry non-receipted messages
    
        for msg in db.txNoReceipt:
            now = time.time()
    
            # kludge to squish retryBackoff attribute exceptions
            try:
                retryBackoff = msg.retryBackoff
            except:
                try:
                    retryBackoff = config.txBackoffInit
                except:
                    retryBackoff = 1800
                    config.txBackoffInit = 1800
                msg.retryBackoff = retryBackoff
    
            if now >= msg.lastTry + retryBackoff:
                # have we maxed out retries
                if msg.numTries >= config.txMaxRetries:
                    # bounce it
                    msgHash = msg.msgHash
                    log(3, "Message retry count exceeded: bouncing:\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                        msg.idAddr, msg.peerAddr, msgHash))
                    db.txDead.append(msg)
                    subj = "Delivery Failure Notification - retries exceeded"
                    body = "\r\n".join([
                        "This is a message from your FreeMail Postmaster,",
                        "A message you previously tried to send through Freemail",
                        "could not be delivered.",
                        "",
                        "We successfully inserted the message into Freenet, but",
                        "never received a receipt, even after %d attempts" % config.txMaxRetries,
                        "",
                        "This may be occuring because the recipient is not running his/her",
                        "FreeMail software.",
                        "",
                        "Sorry, but this is a permanent failure.",
                        "You may be able to contact the recipient by other means,",
                        "and persuade them to keep their FreeMail software running.",
                        "",
                        "The body of the message you tried to send appears below",
                        "-------------------------------------------------------",
                        "",
                        ""]) + self.readFromOutbox(msgHash)
                    self.writeFromPostmaster(msg.idAddr, subj, body)
                    db.txNoReceipt.removeWhen(msgHash=msgHash)
                        
                elif not msg.sendInProgress:
                    log(2, "Re-sending message (retry count=%s):\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                        msg.numTries, msg.idAddr, msg.peerAddr, msg.msgHash))
                    msg.sendInProgress = 1
                    if fcpRunInThreads:
                        thread.start_new_thread(self.threadFcpDeliverMsg, (msg, db.txNotSent))
                    else:
                        self.threadFcpDeliverMsg(msg, db.txNotSent)
    
        if fcpRunInThreads:
            self._dbUnlock()
            # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    #@-node:chugFcpEngine
    #@+node:threadFcpRefreshMailsite
    def threadFcpRefreshMailsite(self, idAddr, **kw):
        """
        Within a thread, (re-)inserts a given identity's mailsite
        """
        try:
            self.fcpRefreshMailsite(idAddr, **kw)
        except:
            self.log(2, "Exception inserting mailsite for peer %s\n%s" % (
                           idAddr, exceptionString()))
    #@-node:threadFcpRefreshMailsite
    #@+node:fcpRefreshMailsite
    def fcpRefreshMailsite(self, idAddr, **kw):
        """
        Re-inserts the identity mailsite.
    
        Recall that a 'mailsite' is just like a normal freesite, but with
        the requirement for certain documents to be available at certain
        paths.
        """
        db = self.db
        config = db.config
        fcpHost = config.fcpHost
        fcpPort = config.fcpPort
        if kw.has_key("htl"):
            htlSend = kw['htl']
        else:
            htlSend = config.htlSend
    
        if not idAddr in db.identities.keys():
            raise Exception("No such identity '%s'" % idAddr)
    
        # set_trace()
    
        db = self.db
        
        rId = db.identities[idAddr]
        key = SSLCrypto.key(rId.idCryptoKey)
        idName = rId.idName
        idUriSite = rId.idUriSite
        idUriSitePriv = rId.idUriSitePriv
        idUriQueue = rId.idUriQueue
        idUriNext = rId.idUriNext
        sskPub = rId.idSskPub
        sskPriv = rId.idSskPriv
    
        offset = 0
        insertPeriod = rId.idInsertPeriod
    
        if rId.idSenderPassword:
            passwordHash = has(rId.idSenderPassword)
        else:
            passwordHash = ''
    
        #- SSK@blahblah/freemail/myidname - holds the identity record, in plaintext:
    
        # construct the mailsite access document
        accessdoc = "\n".join(["FreemailIdentity",
                               "Version=%s" % protocolVersion,
                               "Address=%s" % idAddr,
                               "PasswordRequired=%s" % passwordHash,
                               "QueueUri=%s" % idUriQueue,
                               "NextSlot=%s" % idUriNext,
                               "CryptoKey=%s" % b64enc(key.exportKey()),
                               "End",
                               ])
    
        # insert the freesite
        node = freenet.node(fcpHost, fcpPort, htlSend)
    
        self.log(4, "htlSend=%d" % htlSend)
    
        try:
            # insert the DBR pointer
            future = kw.get('future', 0)
    
            # Create and Insert DBR main pointer key
            metaDbr = freenet.metadata()
            metaDbr.add('', 'DateRedirect',
                        target=idUriSite,
                        increment=insertPeriod,
                        offset=0)
    
            #print metaDbr
            self.log(4, "about to insert dbr")
            #vOld = freenet.verbosity(4)
            node.put('', metaDbr, idUriSitePriv, htl=htlSend, allowSplitfiles=0)
            #freenet.verbosity(vOld)
            self.log(4, "dbr inserted")
    
            for i in [future]:
                # Now insert the manifest du jour as the dbr target - now only
                dbrPrefix = freenet.dbr(i, insertPeriod, offset)
                dujourPubUri = freenet.uri("SSK@%sPAgM/%s-freemail/%s" % (sskPub, dbrPrefix, idName))
                dujourPrivUri = freenet.uri("SSK@%s/%s-freemail/%s" % (
                                  sskPriv, dbrPrefix, idName), sskpriv=True)
        
                self.log(4, "inserting id record du jour for %s, %d periods ahead\n" % (idName, i))
                # print accessdoc
        
                # generate mimetype metadata for benefit of those accessing this page via web proxy
                metaFile = freenet.metadata()
                metaFile.add('', mimetype="text/plain")
    
                try:
                    keyNow = node.put(accessdoc,
                                      metaFile,
                                      dujourPrivUri,
                                      htl=htlSend,
                                      allowSplitfiles=0)
                    self.log(4,
                             "id record du jour for %s inserted\n"
                             "publicURI = '%s'\n"
                             "privateURI = '%s'\n"
                             "future=%s" % (idName, dujourPubUri, dujourPrivUri, i))
                    
                    insertedOk = 1
                    self.log(4, "Successful refresh of mailSite for identity %s" % idAddr)
                except freenet.FreenetKeyCollision:
                    insertedOk = 1
                    self.log(4, "KeyCollision refreshing mailSite %s, take as success" % idAddr)
            
        except:
            self.log(2, "Failed to refresh mailsite for %s\n%s" % (idAddr, exceptionString()))
            insertedOk = 0
        #set_trace()
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        if insertedOk:
            rId.idLastInserted = time.time()
        else:
            rId.idLastInserted = time.time() - rId.idInsertPeriod - mailsiteInsertRetryTime # try again in one hour
        rId.idRefreshInProgress = 0
    
        self.dbSave()
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    
    #@-node:fcpRefreshMailsite
    #@+node:threadFcpRefreshList
    def threadFcpRefreshList(self, listAddr, **kw):
        """
        Within a thread, (re-)inserts a given list's listsite
        """
        try:
            self.fcpRefreshMailsite(listAddr, **kw)
        except:
            self.log(2, "Exception inserting mailsite for peer %s\n%s" % (
                           listAddr, exceptionString()))
    #@-node:threadFcpRefreshList
    #@+node:fcpRefreshList
    def fcpRefreshListsite(self, listAddr, **kw):
        """
        Re-inserts the list's listsite.
        """
        db = self.db
        config = db.config
        fcpHost = config.fcpHost
        fcpPort = config.fcpPort
        if kw.has_key("htl"):
            htlSend = kw['htl']
        else:
            htlSend = config.htlSend
    
        if not listAddr in db.lists.keys():
            raise Exception("No such list '%s'" % listAddr)
    
        # set_trace()
    
        db = self.db
        
        rList = db.lists[listAddr]
        key = SSLCrypto.key(rList.listCryptoKey)
        listName = rList.listName
        listUriSite = rList.listUriSite
        listUriSitePriv = rList.listUriSitePriv
        listUriQueue = rList.listUriQueue
        listUriNext = rList.listUriNext
        sskPub = rList.listSskPub
        sskPriv = rList.listSskPriv
    
        offset = 0
        insertPeriod = rList.listInsertPeriod
    
        if rList.listSenderPassword:
            passwordHash = has(rList.listSenderPassword)
        else:
            passwordHash = ''
    
        #- SSK@blahblah/freemail/mylistname - holds the listentity record, in plaintext:
    
        # construct the mailsite access document
        accessdoc = "\n".join(["FreemailList",
                               "Version=%s" % protocolVersion,
                               "Address=%s" % listAddr,
                               "PasswordRequired=%s" % passwordHash,
                               "QueueUri=%s" % listUriQueue,
                               "NextSlot=%s" % listUriNext,
                               "CryptoKey=%s" % b64enc(key.exportKey()),
                               "End",
                               ])
    
        # insert the freesite
        node = freenet.node(fcpHost, fcpPort, htlSend)
    
        self.log(4, "htlSend=%d" % htlSend)
    
        try:
            # insert the DBR pointer
            future = kw.get('future', 0)
    
            # Create and Insert DBR main pointer key
            metaDbr = freenet.metadata()
            metaDbr.add('', 'DateRedirect',
                        target=listUriSite,
                        increment=insertPeriod,
                        offset=0)
    
            #print metaDbr
            self.log(4, "about to insert dbr")
            #vOld = freenet.verbosity(4)
            node.put('', metaDbr, listUriSitePriv, htl=htlSend, allowSplitfiles=0)
            #freenet.verbosity(vOld)
            self.log(4, "dbr inserted")
    
            for i in [future]:
                # Now insert the manifest du jour as the dbr target - now only
                dbrPrefix = freenet.dbr(i, insertPeriod, offset)
                dujourPubUri = freenet.uri("SSK@%sPAgM/%s-freemail/%s" % (sskPub, dbrPrefix, listName))
                dujourPrivUri = freenet.uri("SSK@%s/%s-freemail/%s" % (
                                  sskPriv, dbrPrefix, listName), sskpriv=True)
        
                self.log(4, "inserting list record du jour for %s, %d periods ahead\n" % (listName, i))
                # print accessdoc
        
                # generate mimetype metadata for benefit of those accessing this page via web proxy
                metaFile = freenet.metadata()
                metaFile.add('', mimetype="text/plain")
    
                try:
                    keyNow = node.put(accessdoc,
                                      metaFile,
                                      dujourPrivUri,
                                      htl=htlSend,
                                      allowSplitfiles=0)
                    self.log(4,
                             "list record du jour for %s inserted\n"
                             "publicURI = '%s'\n"
                             "privateURI = '%s'\n"
                             "future=%s" % (listName, dujourPubUri, dujourPubUri, i))
                    
                    insertedOk = 1
                    self.log(4, "Successful refresh of mailSite for list %s" % listAddr)
                except freenet.FreenetKeyCollision:
                    insertedOk = 1
                    self.log(4, "KeyCollision refreshing mailSite %s, take as success" % listAddr)
            
        except:
            self.log(2, "Failed to refresh listsite for %s\n%s" % (listAddr, exceptionString()))
            insertedOk = 0
        #set_trace()
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        if insertedOk:
            rList.listLastInserted = time.time()
        else:
            rList.listLastInserted = time.time() - rList.listInsertPeriod - mailsiteInsertRetryTime # try again in one hour
        rList.listRefreshInProgress = 0
    
        self.dbSave()
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    
    #@-node:fcpRefreshList
    #@+node:fcpGetPeerInfo
    def fcpGetPeerInfo(self, peerAddr, **kw):
        """
        Given a peer freemail address,
        return a dict of the peer's session queue details,
        or None if we can't get these details
    
        The peer session info dict contains the keys:
         - queueUri - uri object of the peer seq queue to write to
         - nextslot - int - the next sequence number on this queue to try
         - cryptoKey - an SSLCrypto.key object - the peer's public encryption key
        """
        db = self.db
        config = db.config
        log = self.log
    
        # where's the peer mailsite?
        peerUri = str(freemailAddrToUri(peerAddr))
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # get pertinent settings from config
        host = config.fcpHost
        port = config.fcpPort
        maxRetries = config.rxMaxRetries
        maxRegress = config.getMaxRegress
        rxHtl = config.htlReceive
        txHtl = config.htlSend
    
        waitBetweenRetries = kw.get("retryWait", 2)
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        # get peer record, or create if nonexistent
        if db.peers.has_key(peerAddr):
            peerRec = db.peers[peerAddr]
        else:
            # create a new peer record
            peerRec = cell(dateMailsiteLastSeen=0,
                           nextSlot=0,
                           )
            db.peers[peerAddr] = peerRec
            self.dbSave()
    
        log(4, "fcpGetPeerInfo: seeking peer record for %s" % peerAddr)
    
        # calculate how far back we should look.
        # go back as far as the last version of the peer's mailsite, or 
        # config.maxMailsiteRxRegress, whichever is the later
        earliest = dbrStartTime(86400, -config.maxMailsiteRxRegress)
        if peerRec.dateMailsiteLastSeen > earliest:
            earliest = peerRec.dateMailsiteLastSeen
    
    
        if 0:
            # ----------------------------------------------------------
            # Now, for a whole new way of retrieving peer mailsite info.
            # The new technique is to launch several threads, each of which
            # tries to retrieve the version of the peer mailsite n periods back.
            # when all the threads are done, we take the most recent available version.
            #
            # This is now essential, because we've changed the DBR period for
            # mailsites to one day. Refer to fcpRefreshMailsite() for more info
            # on this gratuitous abomination
            
            # 
            # thread func to attempt retrieval of mailsite n periods back
            def thrdGetPeerInfo(dispObj, foundDict, nodeObj, peerUri, past, log=log):
                try:
                    sessobj = nodeObj.get(peerUri, past=past)
                except:
                    log(3, "Failed to get %s, %s periods past" % (peerUri, past))
                else:
                    foundDict[past] = sessobj
                    log(4, "Got %s, %s periods past" % (peerUri, past))
                    if past == 0:
                        log(4, "Got %s, latest version" % peerUri)
                        dispObj.quit()
        
                log(4, "Thread finished for past=%s" % past)
            
            # create the dispatcher to do n simultaneous requests for mailsite
            dispObj = freenet.Dispatcher(thrdGetPeerInfo, maxthreads=config.maxMailsiteRxThreads)
            foundDict = {} # where results (or lack thereof) get reported by threads
        
            #set_trace()
            
            # load the dispatcher, launch it and wait till it completes
            gotWork = 0
            for i in range(0, config.maxMailsiteRxRegress+1):
                if dbrStartTime(86400, -i) <= earliest:
                    break
                dispObj.add(foundDict, freenet.node(host, port, rxHtl), peerUri, i, log)
                gotWork = 1
        
            if gotWork:
                log(4, "starting dispatcher, searching %d insert periods..." % dispObj._jobs.qsize())
                dispObj.start()
                log(4, "waiting for dispatcher to finish...")
                dispObj.wait()
                log(4, "dispatcher complete")
            else:
                if hasattr(peerRec, 'info'):
                    log(4, "No need to retrieve - we already have latest peer record")
        
                    # the peer record contains the exported key, so we'll have to re-instantiate it
                    i = peerRec.info
                    return {'CryptoKey': SSLCrypto.key(i['CryptoKey']),
                                'QueueUri': i['QueueUri'],
                                'Version': i['Version'],
                                'Address': i['Address'],
                                'PasswordRequired': i['PasswordRequired'],
                                'NextSlot': i['NextSlot'],
                                }
        
        
                    return peerRec.info
                else:
                    log(4, "Re-retrieving latest version of peer record")
                    foundDict[0] = freenet.node(host, port, rxHtl).get(peerUri)
        
            res = foundDict.keys()
            res.sort()
        
            log(4, "foundDict = %s" % foundDict)
        
            if not res:
                # got nothing
                log(2, "Can't find mailsite for '%s', not even %d periods back" \
                           % (peerAddr, config.maxMailsiteRxRegress))
                return None
            
            # the first element in the keys list will be the most recent
            latest = res[0]
            sessobj = foundDict[latest]
        
            # update peer record in database
            k = freenet.node(host, port, rxHtl).get(peerUri, rxHtl, raw=True)
            try:
                peerRec.period = int(k.metadata.map['']['args']['DateRedirect.Increment'], 16)
            except:
                peerRec.period = 86400 # default
            peerRec.dateMailsiteLastSeen = dbrStartTime(peerRec.period, -latest)
        
            self.dbSave()
    
        if 1:
            # get a freenet node object
            node = freenet.node(host, port, rxHtl)
    
            # try to retrieve the peer's session info
            numpast = 0
            key = None
            sessobj = None
            searching = 1
            while numpast <= maxRegress and searching:
                numtries = 0
                while numtries <= maxRetries:
                    try:
                        # set_trace()
                        sessobj = node.get(peerUri, past=numpast)
                        searching = 0
                        break
                    except freenet.FreenetFcpError:
                        raise
                    except freenet.FreenetFcpConnectError:
                        raise
                    except:
                        log(3,
                            "failed to get sess info for '%s', waiting to retry\n"
                            "tried to get this under uri %s\n"
                            "numtries=%s/%s, numpast=%s/%s" % (peerAddr, peerUri, numtries, maxRetries, numpast, maxRegress),
                            )
                    numtries = numtries + 1
                    time.sleep(waitBetweenRetries)
                if sessobj:
                    break
                numpast = numpast + 1
    
        # did we get what we want?
        if not sessobj:
            log(2, "giving up on '%s'" % peerAddr)
            return None
    
        # maybe yes - now try to carve up the data
        raw = sessobj.data
        lines = raw.strip().split("\n")
        dat = {}
        if lines[0] != 'FreemailIdentity':
            log(1, "bad header line for sess info from '%s': %s: " % (peerAddr, lines[0]))
            return None
        if lines[-1] != 'End':
            log(1, "bad footer line for sess info from '%s': %s: " % (peerAddr, lines[-1]))
            return None
    
        for line in lines[1:-1]:
            try:
                k, v = line.split("=", 1)
            except:
                log(1, "bad line in sess page for peer %s:\n%s" % (peerAddr, line))
                return None
            dat[k] = v
    
        # instantiate the crypto key
        try:
            dat['CryptoKey'] = SSLCrypto.key(b64dec(dat['CryptoKey']))
        except:
            log(1, "Bad encryption key from peer %s:\n%s" % (peerAddr, exceptionString()))
            return None
    
        try:
            peerRec.nextSlot = int(dat['NextSlot'])
        except:
            peerRec.nextSlot = 0
    
        if not hasattr(peerRec, 'info'):
            peerRec.info = {'CryptoKey': dat['CryptoKey'].exportKey(),
                            'QueueUri': dat['QueueUri'],
                            'Version': dat['Version'],
                            'Address': dat['Address'],
                            'PasswordRequired': dat['PasswordRequired'],
                            'NextSlot': dat['NextSlot'],
                            }
        self.dbSave()
    
        return dat
    #@-node:fcpGetPeerInfo
    #@+node:fcpGetListInfo
    def fcpGetListInfo(self, listAddr, **kw):
        """
        Given a list freemail address,
        return a dict of the list's session queue details,
        or None if we can't get these details
    
        The list session info dict contains the keys:
         - queueUri - uri object of the list seq queue to write to
         - nextslot - int - the next sequence number on this queue to try
         - cryptoKey - an SSLCrypto.key object - the list's public encryption key
        """
        db = self.db
        config = db.config
        log = self.log
    
        # where's the list mailsite?
        listUri = str(freemailAddrToUri(listAddr))
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # get pertinent settings from config
        host = config.fcpHost
        port = config.fcpPort
        maxRetries = config.rxMaxRetries
        maxRegress = config.getMaxRegress
        rxHtl = config.htlReceive
        txHtl = config.htlSend
    
        waitBetweenRetries = kw.get("retryWait", 2)
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        # ----------------------------------------------------------
        # Now, for a whole new way of retrieving list mailsite info.
        # The new technique is to launch several threads, each of which
        # tries to retrieve the version of the list mailsite n periods back.
        # when all the threads are done, we take the most recent available version.
        #
        # This is now essential, because we've changed the DBR period for
        # mailsites to one day. Refer to fcpRefreshMailsite() for more info
        # on this gratuitous abomination
        
        # 
        # thread func to attempt retrieval of mailsite n periods back
        def thrdGetListInfo(dispObj, foundDict, nodeObj, listUri, past, log=log):
            try:
                sessobj = nodeObj.get(listUri, past=past)
            except:
                log(3, "Failed to get %s, %s periods past" % (listUri, past))
            else:
                foundDict[past] = sessobj
                log(4, "Got %s, %s periods past" % (listUri, past))
            log(4, "Thread finished for past=%s" % past)
        
        # create the dispatcher to do n simultaneous requests for mailsite
        dispObj = freenet.Dispatcher(thrdGetListInfo, maxthreads=config.maxMailsiteRxThreads)
        foundDict = {} # where results (or lack thereof) get reported by threads
    
        # get list record, or create if nonexistent
        if db.lists.has_key(listAddr):
            listRec = db.lists[listAddr]
        else:
            # create a new list record
            listRec = cell(dateMailsiteLastSeen=0,
                           nextSlot=0,
                           )
            db.lists[listAddr] = listRec
            self.dbSave()
    
        # calculate how far back we should look.
        # go back as far as the last version of the list's mailsite, or 
        # config.maxMailsiteRxRegress, whichever is the later
        earliest = dbrStartTime(86400, -config.maxMailsiteRxRegress)
        if listRec.dateMailsiteLastSeen > earliest:
            earliest = listRec.dateMailsiteLastSeen
    
        #set_trace()
        
        # load the dispatcher, launch it and wait till it completes
        gotWork = 0
        for i in range(0, config.maxMailsiteRxRegress+1):
            if dbrStartTime(86400, -i) <= earliest:
                break
            dispObj.add(foundDict, freenet.node(host, port, rxHtl), listUri, i, log)
            gotWork = 1
    
        if gotWork:
            log(4, "starting dispatcher, searching %d insert periods..." % dispObj._jobs.qsize())
            dispObj.start()
            log(4, "waiting for dispatcher to finish...")
            dispObj.wait()
            log(4, "dispatcher complete")
        else:
            if hasattr(listRec, 'info'):
                log(4, "No need to retrieve - we already have latest list record")
    
                # the list record contains the exported key, so we'll have to re-instantiate it
                i = listRec.info
                return {'CryptoKey': SSLCrypto.key(i['CryptoKey']),
                            'QueueUri': i['QueueUri'],
                            'Version': i['Version'],
                            'Address': i['Address'],
                            'PasswordRequired': i['PasswordRequired'],
                            'NextSlot': i['NextSlot'],
                            }
    
    
                return listRec.info
            else:
                log(4, "Re-retrieving latest version of list record")
                foundDict[0] = freenet.node(host, port, rxHtl).get(listUri)
    
        res = foundDict.keys()
        res.sort()
    
        log(4, "foundDict = %s" % foundDict)
    
        if not res:
            # got nothing
            log(2, "Can't find mailsite for '%s', not even %d periods back" \
                       % (listAddr, config.maxMailsiteRxRegress))
            return None
        
        # the first element in the keys list will be the most recent
        latest = res[0]
        sessobj = foundDict[latest]
    
        # update list record in database
        k = freenet.node(host, port, rxHtl).get(listUri, rxHtl, raw=True)
        try:
            listRec.period = int(k.metadata.map['']['args']['DateRedirect.Increment'], 16)
        except:
            listRec.period = 86400 # default
        listRec.dateMailsiteLastSeen = dbrStartTime(listRec.period, -latest)
    
        self.dbSave()
    
        if 0:
            # get a freenet node object
            node = freenet.node(host, port, rxHtl)
    
            # try to retrieve the list's session info
            numpast = 0
            key = None
            sessobj = None
            while numpast <= maxRegress:
                numtries = 0
                while numtries <= maxRetries:
                    try:
                        # set_trace()
                        sessobj = node.get(listUri, past=numpast)
                        break
                    except freenet.FreenetFcpError:
                        raise
                    except freenet.FreenetFcpConnectError:
                        raise
                    except:
                        log(3,
                            "failed to get sess info for '%s', waiting to retry\n"
                            "tried to get this under uri %s\n"
                            "numtries=%s/%s, numpast=%s/%s" % (listAddr, listUri, numtries, maxRetries, numpast, maxRegress),
                            )
                    numtries = numtries + 1
                    time.sleep(waitBetweenRetries)
                if sessobj:
                    break
                numpast = numpast + 1
    
        # did we get what we want?
        if not sessobj:
            log(2, "giving up on '%s'" % listAddr)
            return None
    
        # maybe yes - now try to carve up the data
        raw = sessobj.data
        lines = raw.strip().split("\n")
        dat = {}
        if lines[0] != 'FreemailIdentity':
            log(1, "bad header line for sess info from '%s': %s: " % (listAddr, lines[0]))
            return None
        if lines[-1] != 'End':
            log(1, "bad footer line for sess info from '%s': %s: " % (listAddr, lines[-1]))
            return None
    
        for line in lines[1:-1]:
            try:
                k, v = line.split("=", 1)
            except:
                log(1, "bad line in sess page for list %s:\n%s" % (listAddr, line))
                return None
            dat[k] = v
    
        # instantiate the crypto key
        try:
            dat['CryptoKey'] = SSLCrypto.key(b64dec(dat['CryptoKey']))
        except:
            log(1, "Bad encryption key from list %s:\n%s" % (listAddr, exceptionString()))
            return None
    
        try:
            listRec.nextSlot = int(dat['NextSlot'])
        except:
            listRec.nextSlot = 0
    
        if not hasattr(listRec, 'info'):
            listRec.info = {'CryptoKey': dat['CryptoKey'].exportKey(),
                            'QueueUri': dat['QueueUri'],
                            'Version': dat['Version'],
                            'Address': dat['Address'],
                            'PasswordRequired': dat['PasswordRequired'],
                            'NextSlot': dat['NextSlot'],
                            }
        self.dbSave()
    
        return dat
    
    
    
    
    
        # &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
        # old shit
    
        hdr = lines[0]
        kskUri = lines[1]
        nextSlot = lines[2]
        ascKey  = lines[3]
    
        # basic validation
        if len(lines) != 4 or hdr != "Freemail":
            log(1, "bad sess info from '%s': excerpt is: " % (listAddr, repr(raw[:30])))
            return None
    
        # try to get posting URI
        try:
            kskUri = freenet.uri(kskUri)
        except:
            log(1, "bad queue uri from '%s': excerpt is: " % (listAddr, repr(raw[:30])))
            return None
    
        # try to get queue slot
        try:
            nextSlot = int(lines[2])
        except:
            log(1, "bad queue slot from '%s': excerpt is: " % (listAddr, repr(raw[:30])))
            return None
    
        # try to import encryption key
        try:
            listCryptoKey = SSLCrypto.key(lines[3])
        except:
            log(1, "bad crypto key from '%s': excerpt is: " % (listAddr, repr(raw[:30])))
            return None
    
        # seems ok
        info = {'queueUri' : kskUri,
                'nextslot' : nextSlot,
                'cryptoKey' : listCryptoKey,
                }
        return info
    
    #@-node:fcpGetListInfo
    #@+node:threadFcpPollSlot
    def threadFcpPollSlot(self, idAddr, slotno):
        """
        Thread which polls an incoming message slot, and if the data is valid,
        actions it.
    
        Involves sending receipts for messages, sticking received messages on
        inbound queue, and some other shit
        """
        try:
            then = time.time()
            self.chugFcpPollSlot(idAddr, slotno)
            now = time.time()
            if now - then < fcpAntiThrashTime:
                time.sleep(fcpAntiThrashTime - (now - then))
        except:
            self.log(2, "Exception polling slot:\nidAddr=%s\nslotno=%s\n%s" % (
                           idAddr, slotno, exceptionString()))
    #@-node:threadFcpPollSlot
    #@+node:chugFcpPollSlot
    def chugFcpPollSlot(self, idAddr, slotno):
    
        db = self.db
        config = db.config
        log = self.log
    
        log(5, "polling slot %s of id %s" % (slotno, idAddr))
    
        msg = self.fcpGetMsgFromSlot(idAddr, slotno)
    
        if not msg:
            log(5, "no (valid) message at slot %s of id %s" % (slotno, idAddr))
            return
    
        # handle message according to its type
        msgType = msg['Type']
        peerAddr = msg['From']
    
        log(4, "got message on inbound slot:\nType=%s\nidAddr=%s, slot=%s\npeerAddr=%s" % (
            msgType, idAddr, msg['From'], slotno))
    
        if msgType in ['message', 'resend']:
    
            body = msg['Body']
            msgHash = hash(body)
    
            if msgType == 'message':
                # stick on inbound queue
                self.writeToInbox(idAddr, peerAddr, body)
    
            elif msgType == 'resend':
                # look it up - have we received it before??
                pass
    
            if receiptSendDelay > 0:
                print "***********************"
                print "DELIBERATELY STALLING RECEIPT TO CAUSE A SEND RETRY"
                print "***********************"
                time.sleep(receiptSendDelay)
    
            # send receipt
            self.enqueueMessage(idAddr, peerAddr, 'receipt', msgHash)
            log(2, "sent receipt for message:\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                idAddr, peerAddr, msgHash))
            pass
    
        elif msgType == 'receipt':
            # look up message on non-receipted list
    
            msgHash = msg['Hash']
    
            oldMsgs = db.txNoReceipt.select(msgHash=msgHash)
            
            # copy message to 'sent' list
            if not oldMsgs:
                log(2, "got receipt for message we can't remember sending:\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                    idAddr, peerAddr, msgHash))
            else:
                if len(oldMsgs) > 1:
                    log(2, "more than one msg on noreceipt queue:\nidAddr\%s\npeerAddr=%s\nhash=%s" % (
                        idAddr, peerAddr, msgHash))
                else:
                    oldMsg = oldMsgs[0]
                    db.txSent.append(oldMsg)
    
                    # put a receipt on original sender's POP3 queue, if required
                    if config.receiptAll:
                        try:
                            subj = "FreeMail Delivery Receipt"
                            body = "\r\n".join([
                                "This is a message from your FreeMail Postmaster,",
                                "",
                                "A message you previously sent via Freemail",
                                "has been successfully delivered to the recipient.",
                                "",
                                "The body of this message appears below",
                                "-------------------------------------------------------",
                                "",
                                ""]) + self.readFromOutbox(msgHash)
                            self.writeFromPostmaster(oldMsg.idAddr, subj, body)
                        except:
                            log(2, "Failed to put receipt into sender's POP3 mailbox:\n%s" % exceptionString())
    
            # remove message from noreceipt list
            db.txNoReceipt.removeWhen(msgHash=msgHash)
    
        elif msgType == 'bounce':
            pass
    #@-node:chugFcpPollSlot
    #@+node:fcpGetMsgFromSlot
    def fcpGetMsgFromSlot(self, idAddr, slotno):
        """
        Polls a given slot of a given identity's queue.
    
        If a message is present on that slot, marks the id's slot record as 'used',
        retrieves the message, decrypts/verifies it.
    
        Arguments:
         - idAddr - the identity whose queue we should poll for message
         - slotno - the slot number to poll
    
        Returns:
         - a dict of message if successful, None if nothing there
        """
    
        log = self.log
        db = self.db
    
        # get identity record
        idRec = self.db.identities[idAddr]
    
        # determine which actual URI to poll
        pollUri = "%s%s" % (idRec.idUriQueue, slotno)
    
        # try to get the key
        node = self.Node('get')
        try:
            k = node.get(pollUri)
        except:
            k = None
    
        # did we get anything?
        if not k:
            log(5, "No message at slot %s of %s" % (slotno, idAddr))
            idRec.idSlots[slotno] = SLOT_STATE_EMPTY
            return None
    
        # mark slot as used, move id's window if needed
        log(4, "Got message at slot %s of %s" % (slotno, idAddr))
        idRec.idSlots[slotno] = SLOT_STATE_USED
        if idRec.idUriNext <= slotno:
            idRec.idUriNext = slotno + 1
    
        log(5, "About to decrypt message at slot %d, idAddr=%s" % (slotno, idAddr))
    
        # get raw content
        cipherText = k.data
    
        # get our key and decrypt
        idKey = SSLCrypto.key(idRec.idCryptoKey)
        try:
            clearText = idKey.decStringFromAscii(cipherText)
        except:
            log(2, "cannot decrypt msg at slot %d, idAddr=%s" % (slotno, idAddr))
            return None
    
        log(5, "msg decrypted ok at slot %d, idAddr=%s" % (slotno, idAddr))
    
        #set_trace()
    
        # extract message fields
        try:
            msg = self.fcpParseMailMsg(clearText)
        except:
            log(2, "Exception parsing incoming mail message on slot %s of %s:\n%s" % (
                slotno, idAddr, exceptionString()))
            return None
        if not msg:
            log(2, "Parse of message slot %s of %s failed" % (slotno, idAddr))
            return None
    
        log(5, "msg parsed ok at slot %d, idAddr=%s" % (slotno, idAddr))
    
        # basic validation (not already done in fcpParseMailMsg
        if msg['To'] != idAddr:
            log(2, "Message addressed to wrong recipient:\nidAddr=%s, slotno=%s\nAddressed to: %s" % (
                idAddr, slotno, msg['To']))
            return None
    
        # get purported peer info
        try:
            peerAddr = msg['From']
            peer = self.fcpGetPeerInfo(peerAddr)
        except:
            peerAddr = msg['From']
            log(2, "Can't get peer info for received message:\nidAddr=%s\npeerAddr=%s\nslot=%s" % (
                idAddr, peerAddr, slotno))
            return None
    
        # check the signature
        try:
            peerKey = peer['CryptoKey']
            if not peerKey.verifyString(msg['raw'], msg['signature']):
                raise Exception("Invalid signature")
        except:
            self.log(3, "Bad signature in message:\nidAddr=%s, slotno=%s\npeerAddr=%s\n%s" % (
                idAddr, slotno, peerAddr, exceptionString()))
            return None
    
        # don't need raw and sig any longer
        del msg['raw']
        del msg['signature']
    
        # message passes
        log(4, "Got valid message:\nidAddr=%s\npeerAddr=%s\nslot=%s" % (
            idAddr, peerAddr, slotno))
        return msg
    #@-node:fcpGetMsgFromSlot
    #@+node:fcpParseMailMsg
    def fcpParseMailMsg(self, plainText):
        """
        Breaks down an incoming message into a dict of its parts
    
        Also, performs some basic validation of message format
        """
        log = self.log
    
        # extract signature from end
        try:
            plain, sig = plainText.split("<StartPycryptoSignature>")
            sig = "<StartPycryptoSignature>" + sig
        except:
            self.log(2, "message appears not to contain signature")
            return None
    
        lines = plain.strip().split("\n")
        msg = {}
        if lines[0] != 'FreemailMessage':
            log(1, "bad message header line:\nline='%s'" % lines[0])
            return None
        if lines[-1] != 'End':
            log(1, "bad message footer line:\nline='%s'" % lines[-1])
            return None
    
        for line in lines[1:-1]:
            try:
                k, v = line.split("=", 1)
            except:
                log(1, "bad line in message:\nline='%s'" % line)
                return None
            msg[k] = v
    
        # step in and decode body if needed
        if msg.has_key("Body"):
            try:
                msg['Body'] = b64dec(msg['Body'])
            except:
                log(1, "malformed base64-encoded message body")
                return None
    
        # basic validation
        if not msg.has_key('To'):
            log(2, "Message has no 'To' field:\nidAddr=%s, slotno=%s" % (idAddr, slotno))
            return None
        if not msg.has_key('From'):
            log(2, "Message has no 'From' field:\nidAddr=%s, slotno=%s" % (idAddr, slotno))
            return None
        if not msg.has_key('Type'):
            log(2, "Message has no 'Type' field:\nidAddr=%s, slotno=%s" % (idAddr, slotno))
            return None
        msgType = msg['Type']
        if msgType not in ['message', 'bounce', 'resend', 'receipt']:
            log(2, "Invalid 'Type' field in message:\nidAddr=%s, slotno=%s\nType=%s" % (
                idAddr, slotno, msgType))
            return None
    
        # types 'message' and 'resend' must have bodies
        if msgType in ['message', 'resend']:
            if not msg.has_key("Body"):
                log("Message type '%s' missing required body\nFrom=%s\nTo=%s\nType=%s" % (
                    msg['From'], msg['To'], msg['Type']))
                return None
    
        # stick signature and raw into dict
        msg['raw'] = plain
        msg['signature'] = sig
    
        # fine
        return msg
    
    #@-node:fcpParseMailMsg
    #@+node:threadFcpDeliverMsg
    def threadFcpDeliverMsg(self, msgRec, queueObj):
        """
        Attempts to deliver a message to peer,
        and transfers it to another queue depending on outcome
        """
    
        try:
            self.chugFcpDeliverMsg(msgRec, queueObj)
        except:
            self.log(2, "Exception delivering message\nmsgRec=%s\nqueueObj=%s\n%s" % (
                           msgRec, queueObj, exceptionString()))
    #@-node:threadFcpDeliverMsg
    #@+node:chugFcpDeliverMsg
    def chugFcpDeliverMsg(self, msgRec, queueObj):
        """
        Attempts to deliver a message to peer,
        and transfers it to another queue depending on outcome
        """
        log = self.log
        db = self.db
        config = db.config
    
        msgType = msgRec.msgType
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
    
        log(4, "attempting to transmit message:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
            msgType, idAddr, peerAddr, msgHash))
    
        # kludge to squish retryBackoff attribute exceptions
        try:
            retryBackoff = msgRec.retryBackoff
        except:
            try:
                retryBackoff = config.txBackoffInit
            except:
                retryBackoff = 1800
                config.txBackoffInit = 1800
            msgRec.retryBackoff = retryBackoff
    
        try:
            k = self.fcpTransmitMessage(msgRec)
        except:
            log(2, "Exception transmitting message:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s\n%s" % (
                msgType, idAddr, peerAddr, msgHash, exceptionString()))
            k = None
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # update the backoff if this is/was not the first attempt
        if msgRec.lastTry == 0:
            # apply initial backoff
            msgRec.retryBackoff = config.txBackoffInit
        else:
            # expand the backoff
            msgRec.retryBackoff = retryBackoff * config.txBackoffMult
    
        # update retry data
        msgRec.lastTry = time.time()
        msgRec.numTries += 1
    
        # remove from whence it came
        queueObj.removeWhen(msgHash=msgHash)
    
        if k is not None:
            # message delivered successfully
    
            # copy to 'noreceipt' pile if not a receipt, and not there already
            log(2, "Successfully delivered outbound message:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                msgType, idAddr, peerAddr, msgHash))
            if msgType != 'receipt':
                if len(db.txNoReceipt.select(msgHash=msgHash)) == 0:
                    db.txNoReceipt.append(msgRec)
        else:
            # couldn't send message
            log(2, "Failed to enqueue outbound message:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                msgType, idAddr, peerAddr, msgHash))
    
            if msgRec.numTries >= config.txMaxRetries:
                # maxed out retries- move to dead
                if msgType in ['message', 'resend']:
                    try:
                        body = self.readFromOutbox(msgHash)
                    except:
                        body = "<message body deleted from datastore>"
                else:
                    try:   
                        body = self.readFromInbox(msgHash)
                    except:
                        body = "<message body deleted from datastore>"
    
                # send bounce to original sender
                self.pmMaxedRetries(msgRec, body)
    
                db.txDead.append(msgRec) # mark as unsent, schedule for retry
                log(2, "outbound message has exceeded retry limit:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                    msgType, idAddr, peerAddr, msgHash))
            else:
                # reinstate to notsent pile
                db.txNotSent.append(msgRec) # mark as unsent, schedule for retry
                log(2, "scheduling outbound message for retry:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                    msgType, idAddr, peerAddr, msgHash))
    
        msgRec.sendInProgress = 0
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@nonl
    #@-node:chugFcpDeliverMsg
    #@+node:fcpTransmitMessage
    def fcpTransmitMessage(self, msgRec):
        """
        Performs the actual work of sticking a message onto a remote
        recipient's queue
    
        Arguments:
         - msgRec - a freemail message cell object.
    
        Returns:
         - key object if delivery succeeded, None if failed
    
        Format of messages is::
            FreemailMessage
            Version=<protocolversion>
            Type=<msgtype> - one of 'bounce', 'message', 'receipt', 'resend'
            From=<sender-address>
            To=<recipient-address>
            Seq=<message sequence no> - nnn if sent first time, or seq of message being re-sent or bounced
            Body=<one-line ascii-armoured message>
            End
            <message signature>
        """
        log = self.log
        db = self.db
        node = self.Node('put')
    
        msgType = msgRec.msgType
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
        
        # kludge to squish retryBackoff attribute exceptions
        try:
            retryBackoff = int(msgRec.retryBackoff)
        except:
            try:
                retryBackoff = int(config.txBackoffInit)
            except:
                retryBackoff = 1800
                config.txBackoffInit = 1800
            msgRec.retryBackoff = retryBackoff
    
        if msgType in ['message', 'resend']:
            try:
                body = self.readFromOutbox(msgHash)
            except:
                self.pmMsgDeletedFromOutbox(msgRec)
                raise FreeMailCannotSend("Can't get message body from outbound message store")
        else:
            try:   
                body = self.readFromInbox(msgHash)
            except:
                self.pmMsgDeletedFromInbox(msgRec)
                raise FreeMailCannotSend("Can't get message body from inbound message store")
    
        # fetch peer's mailsite
        try:
            peerInfo = self.fcpGetPeerInfo(peerAddr)
        except:
            peerInfo = None
    
        if not peerInfo:
            log(2, "Failed to get peer info:\nidAddr=%s\npeerAddr=%s\n" % (idAddr, peerAddr))
            self.pmNoPeerInfo(msgRec, body)
            return None
    
        # extract peer stuff
        queueUri = peerInfo['QueueUri']
        peerKey = peerInfo['CryptoKey']
    
        # if we've reached here, then there *will* be a peer record
        try:
            nextSlotPeerInfo = peerInfo['NextSlot']
        except:
            print "peerInfo = %s" % peerInfo
            raise
        nextSlotDbPeers = db.peers[peerAddr].nextSlot
        nextSlot = max(nextSlotPeerInfo, nextSlotDbPeers)
    
        # build the message
        msgFlds = ["FreemailMessage",
                   "Version=%s" % protocolVersion,
                   "Type=%s" % msgType,
                   "From=%s" % idAddr,
                   "To=%s" % peerAddr,
                   "Body=%s" % b64enc(body),
                   ]
        if msgType in ['receipt', 'bounce']:
            msgFlds.append("Hash=%s" % hash(body))
        msgFlds.append("End")
        plainText = "\n".join(msgFlds) + "\n"
    
        # sign and encrypt
        idKey = SSLCrypto.key(db.identities[idAddr].idCryptoKey)
        sig = idKey.signString(plainText)
        plainText += sig
    
        try:
            cipherText = peerKey.encStringToAscii(plainText)
        except:
            log(2, "sign/encrypt failed:\nidAddr=%s\npeerAddr=%s" % (idAddr, peerAddr))
            return None
    
        # stick onto peer queue
        try:
            # add 'text/plain' mimetype metadata to ease browsing via web proxy
            metaFile = freenet.metadata()
            metaFile.add('', mimetype="text/plain")
    
            #vOld = freenet.verbosity()
            #freenet.verbosity(4)
            k = node.putseq(queueUri,
                            startnum=nextSlot,
                            numtries=100,
                            keydata=cipherText,
                            keymeta=metaFile,
                            )
            #freenet.verbosity(vOld)
            
            # update nextslot
            db.peers[peerAddr].nextSlot = k.seq
    
            log(2, "Message delivery successful:\nidAddr=%s\npeerAddr=%s" % (idAddr, peerAddr))
            return k
        
        except freenet.FreenetRouteNotFound:
    
            log(2, "got RouteNotFound from node:\nidAddr=%s\npeerAddr=%s" % (
                    idAddr, peerAddr))
    
            # send bounce to sender
            if msgType in ['message', 'resend']:
                self.pmMsgRouteNotFound(msgRec, body)
            elif msgType == 'receipt':
                self.pmReceiptRouteNotFound(msgRec, body)
            raise
            
        except:
            print "*************************"
            print exceptionString()
            print "*************************"
            #freenet.verbosity(vOld)
            log(2,
                "Failed to insert message ciphertext to peer queue:\n"
                "idAddr=%s\n"
                "peerAddr=%s\n"
                "queueUri=%s\n"
                "%s" % (
                idAddr, peerAddr, queueUri, exceptionString()))
    
            if msgType in ['message', 'resend']:
                self.pmMsgFreenetException(msgRec, body)
            elif msgType == 'receipt':
                self.pmReceiptFreenetException(msgRec, body)
            raise
    
    #@-node:fcpTransmitMessage
    #@+node:pmMsgDeletedFromOutbox
    def pmMsgDeletedFromOutbox(self, msgRec, body=''):
    
        config = self.db.config
        log = self.log
    
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
        retryBackoff = msgRec.retryBackoff
        numTries = msgRec.numTries
        
        log(2,
            "failed to read outbox msg:\n"
            "idAddr=%s\n"
            "peerAddr=%s\n"
            "hash=%s\n"
            "dir=%s\n"
            "%s" % (idAddr, peerAddr, msgHash, storeDir, exceptionString()))
    
        # bounce back to sender
        bounce = "\r\n".join([
            "This is a message from your FreeMail Postmaster.",
            "",
            "A message you tried to send could not be delivered.",
            "",
            "Reason: can't get message body from outbound store (did you delete it?)",
            "",
            "idAddr=%s" % idAddr,
            "peerAddr=%s" % peerAddr,
            "hash=%s" % msgHash,
            "",
            "The exception stack trace appears below:",
            "",
            exceptionString(),
            "",
            "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
            "The message is scheduled for another delivery attempt in",
            dhms(retryBackoff),
            ])
    
        self.writeFromPostmaster(idAddr,
                                 "Delivery Failure: Unknown Exception",
                                 bounce)
    
    
    #@-node:pmMsgDeletedFromOutbox
    #@+node:pmMsgDeletedFromInbox
    def pmMsgDeletedFromInbox(self, msgRec, body=''):
    
        config = self.db.config
        log = self.log
        
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
        retryBackoff = msgRec.retryBackoff
        numTries = msgRec.numTries
        
        log(2,
            "failed to read inbox msg:\n"
            "idAddr=%s\n"
            "peerAddr=%s\n"
            "hash=%s\n"
            "dir=%s\n"
            "%s" % (idAddr, peerAddr, msgHash, storeDir, exceptionString()))
    
        # bounce back to sender
        bounce = "\r\n".join([
            "This is a message from your FreeMail Postmaster.",
            "",
            "A message you tried to send could not be delivered.",
            "",
            "idAddr=%s" % idAddr,
            "peerAddr=%s" % peerAddr,
            "hash=%s" % msgHash,
            "",
            "Reason: can't get message body from inbound store (did you delete it?)",
            "",
            "The exception stack trace appears below:",
            "",
            exceptionString(),
            "",
            "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
            "The message is scheduled for another delivery attempt in",
            dhms(retryBackoff),
            ])
    
        self.writeFromPostmaster(idAddr,
                                 "Delivery Failure: Unknown Exception",
                                 bounce)
    
    
    #@-node:pmMsgDeletedFromInbox
    #@+node:pmNoPeerInfo
    def pmNoPeerInfo(self, msgRec, body=''):
    
        config = self.db.config
        log = self.log
    
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
        retryBackoff = msgRec.retryBackoff
        numTries = msgRec.numTries
        
        if 1:
            print "idAddr=%s" % idAddr
            print "peerAddr=%s" % peerAddr
            print "msgHash=%s" % msgHash
            print "retryBackoff=%s" % retryBackoff
        
        log(2, "Exception retrieving peer info:\nidAddr=%s\npeerAddr=%s\n%s" % (
            idAddr, peerAddr, exceptionString()))
    
        # send bounce to sender
        bounce = "\r\n".join([
            "This is a message from your FreeMail Postmaster.",
            "",
            "A message you tried to send could not (yet) be delivered.",
            "",
            "Reason: Cannot (yet) find the recipient's in-freenet freemail record",
            "Recipient: %s" % peerAddr,
            "",
            "In trying to find the recipient record, Freenet (or entropy)",
            "reported the following exception:",
            exceptionString(),
            "",
            "Please check that your Freenet/Entropy node is in",
            "good health.",
            "",
            "You might also try increasing the 'receive HTL' setting via the",
            "web interface",
            "",
            "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
            "The message is scheduled for another delivery attempt in",
            dhms(retryBackoff),
            "",
            "An excerpt of the undeliverable message appears below",
            "-------------------------------------------------",
            "",
            body[:8192],
            ])
        self.writeFromPostmaster(idAddr,
                                 "Delivery Failure: Can't find recipient",
                                 bounce)
    
    
    #@-node:pmNoPeerInfo
    #@+node:pmMsgRouteNotFound
    def pmMsgRouteNotFound(self, msgRec, body=''):
    
        config = self.db.config
    
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
        retryBackoff = msgRec.retryBackoff
        numTries = msgRec.numTries
        
        bounce = "\r\n".join([
            "This is a message from your FreeMail Postmaster.",
            "",
            "A message you tried to send could not (yet) be delivered.",
            "",
            "Reason: got RouteNotFound from Freenet/Entropy node",
            "        when trying to insert the message data.",
            "",
            "Please check that your Freenet/Entropy node is in",
            "good health.",
            "",
            "idAddr=%s" % idAddr,
            "peerAddr=%s" % peerAddr,
            "hash=%s" % msgHash,
            "",
            "Also, you may want to try changing your FreeMail configuration",
            "to set a lower HTL value for inserting.",
            "",
            "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
            "The message is scheduled for another delivery attempt in",
            dhms(retryBackoff),
            "",
            "An excerpt of the undeliverable message appears below",
            "-------------------------------------------------",
            "",
            body[:8192],
            ])
    
        self.writeFromPostmaster(idAddr,
                                 "Delivery Failure: RouteNotFound",
                                 bounce)
    
    
    #@-node:pmMsgRouteNotFound
    #@+node:pmReceiptRouteNotFound
    def pmReceiptRouteNotFound(self, msgRec, body=''):
    
        config = self.db.config
    
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
        retryBackoff = msgRec.retryBackoff
        numTries = msgRec.numTries
        
        bounce = "\r\n".join([
            "This is a message from your FreeMail Postmaster.",
            "",
            "I tried to send off a receipt for an incoming message,",
            "but Freenet complained with a RouteNotFound error.",
            "As a result, the person who sent you this message won't",
            "get their receipt, and will re-send the message.",
            "",
            "This is a non-fatal situation that may right itself.",
            "But you may want to check that your Freenet/Entropy node is in",
            "good health",
            "",
            "idAddr=%s" % idAddr,
            "peerAddr=%s" % peerAddr,
            "hash=%s" % msgHash,
            "",
            "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
            "The message is scheduled for another delivery attempt in",
            dhms(retryBackoff),
            "",
            "Also, you may want to try changing your FreeMail configuration",
            "to set a lower HTL value for inserting.",
            "",
            ])
    
        self.writeFromPostmaster(idAddr,
                                 "Couldn't send receipt: RouteNotFound",
                                 bounce)
    #@-node:pmReceiptRouteNotFound
    #@+node:pmMsgFreenetException
    def pmMsgFreenetException(self, msgRec, body=''):
    
        config = self.db.config
    
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
        retryBackoff = msgRec.retryBackoff
        numTries = msgRec.numTries
        
        # bounce back to sender
        bounce = "\r\n".join([
            "This is a message from your FreeMail Postmaster.",
            "",
            "A message you tried to send could not be delivered.",
            "",
            "Reason: got an exception when trying to insert the message data.",
            "",
            "The exception stack trace appears below:",
            "",
            exceptionString(),
            "",
            "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
            "The message is scheduled for another delivery attempt in",
            dhms(retryBackoff),
            "",
            "Please check that your Freenet/Entropy node is in",
            "good health, and try sending the message again.",
            "",
            "idAddr=%s" % idAddr,
            "peerAddr=%s" % peerAddr,
            "hash=%s" % msgHash,
            "",
            "An excerpt of the undeliverable message appears below",
            "-------------------------------------------------",
            "",
            body[:8192],
            ])
    
        self.writeFromPostmaster(idAddr,
                                 "Delivery Failure: Unknown Exception",
                                 bounce)
    
    #@-node:pmMsgFreenetException
    #@+node:pmReceiptFreenetException
    def pmReceiptFreenetException(self, msgRec, body=''):
    
        config = self.db.config
    
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
        retryBackoff = msgRec.retryBackoff
        numTries = msgRec.numTries
        
        bounce = "\r\n".join([
            "This is a message from your FreeMail Postmaster.",
            "",
            "I tried to send off a reciept for an incoming message,",
            "but an exception occurred within the Freenet interface code.",
            "As a result, the person who sent you this message won't",
            "get their receipt, and will re-send the message.",
            "",
            "This is a non-fatal situation that may right itself.",
            "But you may want to check that your Freenet/Entropy node is in",
            "good health",
            "",
            "idAddr=%s" % idAddr,
            "peerAddr=%s" % peerAddr,
            "hash=%s" % msgHash,
            "",
            "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
            "The message is scheduled for another delivery attempt in",
            dhms(retryBackoff),
            "",
            "The exception stack trace appears below:",
            "",
            exceptionString(),
            "",
            ])
    
        self.writeFromPostmaster(idAddr,
                                 "Couldn't send receipt: Exception",
                                 bounce)
    
    
    #@-node:pmReceiptFreenetException
    #@+node:pmMaxedRetries
    def pmMaxedRetries(self, msgRec, body=''):
    
        config = self.db.config
    
        idAddr = msgRec.idAddr
        peerAddr = msgRec.peerAddr
        msgHash = msgRec.msgHash
        retryBackoff = msgRec.retryBackoff
        numTries = msgRec.numTries
        
        bounce = "\r\n".join([
            "This is a message from your FreeMail Postmaster.",
            "",
            "A message you tried to send could not be delivered.",
            "",
            "The retry limit for sending this message has",
            "been exhausted, so I'm giving up - this is a permanent error.",
            "",
            "I'm sorry things didn't work out in this case.",
            "",
            "You will have received a number of retry notifications, which",
            "hopefully have informed you as to the cause of the problem.",
            "and helped you to track down and rememdy the issue.",
            "",
            "idAddr=%s" % idAddr,
            "peerAddr=%s" % peerAddr,
            "hash=%s" % msgHash,
            "",
            "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
            "",
            "An excerpt of the undeliverable message appears below",
            "-------------------------------------------------",
            "",
            body[:8192],
            "",
            ])
    
        self.writeFromPostmaster(idAddr,
                                 "Message Delivery Failure - retry count exhausted",
                                 bounce)
    
    
    #@-node:pmMaxedRetries
    #@+node:enqueueMessage
    def enqueueMessage(self, idAddr, peerAddr, msgType, bodyOrHash):
        """
        Sticks a message on the queue for later sending
        """
        db = self.db
        config = db.config
    
        if msgType in ['message', 'resend']:
            body = bodyOrHash
            # strip any compromising headers from the message
            hdrs, body = body.split("\r\n\r\n", 1)
            lines = re.split("[\\r\\n]+", hdrs)
            for line in lines:
                # self.log(3, "enqueueMessage: line: " + line)
                if line.startswith("X-Mailer:"):
                    # self.log(4, "trying to suppress X-Mailer header")
                    lines.remove(line)
            hdrs = "\r\n".join(lines)
            body = "\r\n\r\n".join([hdrs, body])
    
        # allow addresses to end with '.freenet'
        if peerAddr.endswith(".freenet"):
            peerAddr = peerAddr[:-8]
        
        # if this is a ping, simply write it back to inbox
        if peerAddr in ['freemail-ping', 'ping'] or peerAddr.startswith("ping@"):
            hdrs, body = body.split("\r\n\r\n", 1)
            body = hdrs + "\r\n\r\n" + "*** MESSAGE SENT TO 'freemail-ping': ***\r\n\r\n" + body
            self.writeToInbox(idAddr, 'freemail-pong', body)
            self.log(4, "got a ping message, ponging it")
            return
        
        # write message into store directory
        if msgType in ['message', 'resend']:
    
            msgHash = hash(body)
    
            # stick in store if this is first time
            if msgType == 'message':
                # stick in message store
                filename = os.path.join(db.config.storeDir, "tx", msgHash)
                fd = open(filename, "wb")
                fd.write(body)
                fd.close()
        else:
            msgHash = bodyOrHash
            body = ''
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # and stick the message onto outbound queue
        db.txNotSent.append(idAddr=idAddr,
                           peerAddr=peerAddr,
                           date=time.time(),
                           msgHash=msgHash,
                           msgLen=len(body),
                           msgChk='',
                           numTries=0,
                           txStatus=TX_STATUS_NOT_SENT,
                           lastTry=0,
                           retryBackoff=config.txBackoffInit,
                           msgType=msgType,
                           sendInProgress=0,
                           )
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        self.dbSave()
    
        self.log(4, "Enqueued message from '%s' to '%s'" % (idAddr, peerAddr))
    
    
    #@-node:enqueueMessage
    #@+node:writeToInbox
    def writeToInbox(self, idAddr, fromAddr, body):
        """
        Takes a received message and adds it to the local inbox
    
        Arguments:
         - inAddr - address of recipient
         - fromAddr - address of peer
         - body - body of message
        """
        msgHash = hash(body)
    
        # write message into store directory    
        filename = os.path.join(self.db.config.storeDir, "rx", msgHash)
        fd = open(filename, "wb")
        fd.write(body)
        fd.close()
    
        # "idAddr:S",        # name of local identity which received the message
        # "peerAddr:S",      # name of remote peer which sent the message
        # "date:D",          # UTC python date/time (seconds since epoch, float)
        # "msgHash:S",       # hash of plaintext message body - filename in store
        # "msgChk:S",        # CHK URI of encrypted message
     
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # enter message into received messages table
        if len(self.db.rxMessages.select(msgHash=msgHash)) == 0:
            self.db.rxMessages.append(idAddr=idAddr,
                                      peerAddr=fromAddr,
                                      date=time.time(),
                                      msgHash=msgHash,
                                      msgChk='',
                                      msgLen=len(body),
                                      isDeleted=0)
        else:
            self.log(4, "Received a duplicate of an existing message:\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                idAddr, fromAddr, msgHash))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        self.dbSave()
    #@-node:writeToInbox
    #@+node:readFromOutbox
    def readFromOutbox(self, msgHash):
        """
        Reads in the text of a message from the outbound store
        
        Arguments:
         - msgHash - the hash of the outbound message
        """
        # write message into store directory    
        filename = os.path.join(self.db.config.storeDir, "tx", msgHash)
        fd = open(filename, "rb")
        body = fd.read()
        fd.close()
        return body
    #@-node:readFromOutbox
    #@+node:readFromInbox
    def readFromInbox(self, msgHash):
        """
        Reads in the text of a message from the inbound store
        
        Arguments:
         - msgHash - the hash of the inbound message
        """
        # write message into store directory    
        filename = os.path.join(self.db.config.storeDir, "rx", msgHash)
        fd = open(filename, "rb")
        body = fd.read()
        fd.close()
        return body
    #@-node:readFromInbox
    #@+node:writeToOutbox
    def writeToOutbox(self, body):
        """
        Writes the text of the message to the local outbound file store
        
        Arguments:
         - body - full string of message
    
        Returns
         - msgHash - the hash of the outbound message
        """
        # hash it
        msgHash = hash(body)
    
        # write message into store directory    
        filename = os.path.join(self.db.config.storeDir, "tx", msgHash)
        fd = open(filename, "wb")
        fd.write(body)
        fd.close()
        return msgHash
    #@-node:writeToOutbox
    #@+node:writeToInboxRaw
    def writeToInboxRaw(self, idAddr, fromAddr, subject, body):
        """
        Composes a raw message to a local identity, and add
        this to the identity's inbox
        
        Headers get added to the message for the benefit of the client
        """
    
        # build up a set of message headers
        nowstr = time.asctime(time.localtime(time.time()))
        
        bodyHash = hash(body)
        hdrs = []
        hdrs.append("Return-path: <%s>" % fromAddr)
        hdrs.append("Envelope-to: %s" % idAddr)
        hdrs.append("Delivery-date: %s" % nowstr)
        hdrs.append("Received: from freemail (helo=freemail) by localhost with")
        hdrs.append("        local-smtp (freemail v%s) id %s; %s" % (version, bodyHash, nowstr))
        hdrs.append("From: %s" % fromAddr)
        hdrs.append("Date: %s" % nowstr)
        hdrs.append("To: %s" % idAddr)
        hdrs.append("Message-ID: <%s>" % bodyHash)
        hdrs.append("Mime-Version: 1.0")
        hdrs.append("Content-Type: text/plain; charset=us-ascii")
        hdrs.append("Content-Disposition: inline")
        hdrs.append("Subject: %s" % subject)
        hdrs.append("Sender: %s" % fromAddr)
        hdrs.append("")
        hdrs.append("")
        
        fullBody = "\r\n".join(hdrs) + body
        self.writeToInbox(idAddr, fromAddr, fullBody)
    
    #@-node:writeToInboxRaw
    #@+node:writeFromPostmaster
    def writeFromPostmaster(self, idAddr, subject, body):
        self.writeToInboxRaw(idAddr,
                             "Freemail Postmaster <postmaster@freemail>", 
                             subject,
                             body)
    #@-node:writeFromPostmaster
    #@+node:_dbLock
    def _dbLock(self):
        """
        acquires the 'lock' on the database
        
        All code should acquire this lock before accessing the database,
        whether reading or writing
        """
    
        caller = traceback.extract_stack()[-2]
        full = "%s:%s:%s()" % (os.path.split(caller[0])[1], caller[1], caller[2])
    
        self._dbLockLock.acquire()
    
        then = time.time()
    
        gotit = self._dbLockObj.acquire(False)
        if gotit:
            self._dbLockStack.append(full)
    
        self._dbLockLock.release()
    
        if gotit:
            if reportLocks:
                self.log(2, "DBLOCK ACQUIRED by %s" % full, 1)
        else:
            try:
                if reportLocks:
                    self.log(2, "DBLOCK WANTED by %s, held by %s" % (full, self._dbLockStack[-1]), 1)
            except:
                if reportLocks:
                    self.log(2, "DBLOCK WANTED by %s, held by ???" % full, 1)
            self._dbLockObj.acquire(True)
            self._dbLockStack.append(full)
    
        now = time.time()
        if reportLocks:
            self.log(2, "DBLOCK ACQUIRED by %s after %f seconds" % (full, now - then), 1)
        self._dbLockStartTime = now
    #@-node:_dbLock
    #@+node:_dbUnlock
    def _dbUnlock(self):
        """
        releases the 'lock' on the database
        """
        #now = time.time()
    
        caller = traceback.extract_stack()[-2]
        full = "%s:%s:%s()" % (os.path.split(caller[0])[1], caller[1], caller[2])
    
        if reportLocks:
            self.log(2, "DBLOCK released by %s" % full)
        self._dbLockObj.release()
        self._dbLockStack.pop()
    
        #self.log(2, "released lock after %f seconds" % (now - self._dbLockStartTime), 1)
    
    
    #@-node:_dbUnlock
    #@+node:dbLoad
    def dbLoad(self):
        """
        Loads the configuration from file
        """
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        self.log(4, "Creating db storage object")
    
        # load database from pickle, incorporate into this object
        fd = open(self.filename, "rb")
        self.db = pickle.load(fd)
        fd.close()
    
        db = self.db
        config = db.config
    
        self.dbUpdate()
    
        self.log(4, "Successfully restored pickled db object")
    
        if not self.dbIntegrityOk():
            newname = "%s-corrupt-%s" % (self.filename, time.strftime("%Y-%m-%d-%H-%M-%S"))
            self.log(1,
                     "*************\n"
                     "\n"
                     "NO! PLEASE! NO! NO!\n"
                     "\n"
                     "I am **TOAST** !!!\n"
                     "\n"
                     "Database file %s is corrupted\n"
                     "\n"
                     "Saved this bad file to %s\n"
                     "\n"
                     "Please send this file to david@freenet.org.nz\n"
                     "The next time you run FreeMail, it will create a whole new database\n"
                     "***************************\n"
                     "\n" % (
                          self.filename, newname))
            os.rename(self.filename, newname)
            sys.exit(1)
    
        # frig identity records to mark refresh not in progress, all busy slots inactive
        for id in db.identities.values():
            id.idRefreshInProgress = 0
            id.idSlots.resetBusy()
    
        # frig tx queues to reset 'in progress' flag
        for msg in db.txNotSent:
            msg.sendInProgress = 0
        for msg in db.txNoReceipt:
            msg.sendInProgress = 0
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        self.log(4, "Database successfully loaded (I think)")
    #@-node:dbLoad
    #@+node:dbUpdate
    def dbUpdate(self):
        """
        Dodgy method which senses changes of FreeMail builds, and
        updates database accordingly
        """
        
        db = self.db
        config = db.config
        log = self.log
        changed = 0
    
        # freemailBuild field added from build 012    
        if not hasattr(config, 'freemailBuild'):
            config.freemailBuild = '011'
    
        dbBuild = config.freemailBuild
        
        if dbBuild == '011':
            # took out slotBackoff and txRetryInterval fields, added txBackoff fields
            log(1, "Updating database from build 011 to build %s" % build)
            del config.slotBackoffInit
            del config.slotBackoffMult
            del db.txNoSite
            del config.txRetryInterval
    
            config.txBackoffInit = 7200
            config.txBackoffMult = 1.6
    
            for msg in db.txNotSent.values():
                msg.retryBackoff = config.txBackoffInit
            for msg in db.txNoReceipt.values():
                msg.retryBackoff = config.txBackoffInit
    
            changed = 1
            log(1, "Database update completed successfully")
    
        if dbBuild < '015':
            # added telnet server
            config.telnetPort = 10021
            db.telnetHosts = ['127.0.0.1']
            changed = 1
    
        if dbBuild < '016' or not config.has_key('maxMailsiteRxRegress'):
            config.maxMailsiteRxRegress = 14
            config.maxMailsiteRxThreads = 10
            changed = 1
    
        if dbBuild < '017' or not db.has_key('lists'):
            # build 17 adds support for mailing lists
            db.lists = cell()
            changed = 1
        
        # save if changed
        if changed:
            config.freemailBuild = build
            self.dbSave()
    
    #@-node:dbUpdate
    #@+node:dbIntegrityOk
    def dbIntegrityOk(self):
        """
        Perform some integrity checks on the database
    
        Arguments:
            - db - metakit database object
    
        Returns 1 if database seems ok, 0 if there's something wrong
        """
    
        minTableSet = ['config', 'smtpHosts', 'popHosts', 'httpHosts',
                       'identities', 'peers',
                       'txNotSent', 'txNoReceipt', 'txSent', 'txDead',
                       'rxMessages',
                       ]
    
        tables = self.db._dict.keys()
    
        #set_trace()
    
        seemsOk = 1    
    
        # only integrity check for now is checking that minimal set of tables is present
        for tabName in minTableSet:
            if tabName not in tables:
                self.log(1, "database %s missing table %s" % (self.filename, tabName))
                seemsOk = 0
    
        return seemsOk
    #@-node:dbIntegrityOk
    #@+node:dbInit
    def dbInit(self, **kw):
        """
        Initialises the config object, to create a new mailsite
        
        Arguments:
         - none
         
        Keywords:
         - name - human-readable text name for this identity - mandatory
         - sskpriv - SSK private key. If none, a new SSK gets created
         - sskpub - SSK public key. IF none, a new SSK gets created
        """
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # create database, populate with given info
        self.db = cell()
        db = self.db
    
        if not os.path.isfile(self.filename):
            # create a basic pickle
            fd = open(self.filename, "wb")
            pickle.dump(db, fd, 1)
            fd.close()
    
        # retrieve password, if any
        try:
            adminPassword = hash(kw['adminPassword'])
        except:
            adminPassword = ''
    
        # determine store directory
        try:
            storeDir = kw['storeDir']
        except:
            storeDir = os.path.join(os.path.split(self.filename)[0], "freemail.store")
    
        # create store directory tree if needed
        if not os.path.isdir(storeDir):
            os.mkdir(storeDir)
    
        storeDirTx = os.path.join(storeDir, "tx")
        storeDirRx = os.path.join(storeDir, "rx")
        if not os.path.isdir(storeDirTx):
            os.mkdir(storeDirTx)
        if not os.path.isdir(storeDirRx):
            os.mkdir(storeDirRx)
    
        # --------------------------------------------------------
        # site-wide data
        db.config = cell(
            freemailBuild=build,
            configState=kw.get('configState', "new"),            # for initial config wizard
            adminLogin=kw.get('adminLogin', 'freemail'),         # login name for HTTP admin sessions
            adminPassword=adminPassword,                         # hashed password for HTTP admin sessions
            fcpHost=kw.get('fcpHost', "127.0.0.1"),              # hostname of freenet/entropy node
            fcpPort=kw.get('fcpPort', 8481),                     # FCP port of freenet/entropy node
            smtpPort=kw.get('smtpPort', 25),                     # listen port for incoming SMTP connections
            popPort=kw.get('popPort', 110),                      # listen port for incoming POP3 connections
            httpPort=kw.get('httpPort', 8889),                   # listen port for incoming HTTP admin connections
            telnetPort=kw.get('telnetPort', 10023),              # listen port for incoming Telnet admin connections
            adminTimeout=kw.get('adminTimeout', 3600),           # stay logged in for one hour, timeout in seconds for HTTP admin sessions
            htlSend=kw.get('htlSend', 25),                       # HTL for sending (inserting) messages
            htlReceive=kw.get('htlReceive', 25),                 # HTL for receiving (retrieving) messages
            receiptAll=kw.get('receiptAll', 1),                  # place receipts for all sent messages into
                                                                 # sender's POP3 mailbox
            storeDir=storeDir,                                   # store directory
            rxMaxRetries=kw.get('rxMaxRetries', 3),              # maximum number of retries when receiving
            txMaxRetries=kw.get('txMaxRetries', 15),             # maximum number of retries when sending
            txBackoffInit=kw.get('txBackoffInit', 600),          # initial tx retry backoff time, in seconds
            txBackoffMult=kw.get('txBackoffMult', 1.5),          # factor to multiply by tx backoff factor
            getMaxRegress=kw.get('getMaxRegress', 14),           # number of periods to regress with mailsite fetches
            cryptoKeySize=kw.get('cryptoKeySize', 2048),         # strength of crypto keys generated for receiving, in bits
            slotLookAhead=kw.get('slotLookAhead', 2),            # number of receive 'slots' to look ahead of 'next slot'
    
            maxMailsiteRxRegress=kw.get('maxMailsiteRxRegress', 14), # max periods to regress when fetching peer mailsites
            maxMailsiteRxThreads=kw.get('maxMailsiteRxThreads', 10), # max threads to use for regressed fetches
    
            )
    
        # --------------------------------------------------------
        # list of hosts from which we accept SMTP connections
        db.smtpHosts = kw.get('smtpHosts', ['127.0.0.1'])
        
        # --------------------------------------------------------
        # list of hosts from which we accept POP3 connections
        db.popHosts = kw.get('popHosts', ['127.0.0.1'])
    
        # --------------------------------------------------------
        # list of hosts from which we accept HTTP connections
        db.httpHosts = kw.get('httpHosts', ['127.0.0.1'])
    
        # --------------------------------------------------------
        # list of hosts from which we accept Telnet connections
        db.telnetHosts = kw.get('telnetHosts', ['127.0.0.1'])
    
        # --------------------------------------------------------
        # all our local identities
        db.identities = cell()
    
        # --------------------------------------------------------
        # all known external peers
        db.peers = {}
        
        # ----------------------------------------------------------
        # all mailing lists - the ones we own, and the ones we don't
        db.lists = cell()
    
        # --------------------------------------------------------
        # store of outbound messages - pending, unconfirmed, sent and dead
        db.txNotSent = cell()
        db.txNoReceipt = cell()
        db.txSent = cell()
        db.txDead = cell()
    
        # --------------------------------------------------------
        # store of messages received
        db.rxMessages = cell()
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        self.dbSave()
    
    
    #@-node:dbInit
    #@+node:dbSave
    def dbSave(self):
        """
        Saves the configuration to file
        """
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # save to temporary file - precaution against this prog being terminated
        # in the middle of a save
        fd = open(self.filename+".new", "wb")
        pickle.dump(self.db, fd)
        fd.close()
    
        # delete old one, rename new one
        os.unlink(self.filename)
        os.rename(self.filename+".new", self.filename)
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:dbSave
    #@+node:dbCleanup
    def dbCleanup(self):
        """
        Cleans up database and instance refs
        
        Called at the end of each processing run.
    
        Performs various checks on the database, and deletes obsolete records.
        Also, deletes local instance refs to these records (eg, slot tables in
        self.rxSlots[]).
        """
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # do the business
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
        
    #@-node:dbCleanup
    #@+node:dbAddIdentity
    def dbAddIdentity(self, name, **kw):
        """
        Creates a whole new identity (alias) and stores in database
    
        Arguments:
         - name - compulsory - name of this identity - short string [a-zA-Z0-9_]+
        
        Keywords:
         - cryptoKey - if provided, this is an SSLCrypto private key
         - password - password under which to access this identity's mail via POP3.
           if provided, gets stored hashed in database. If not provided, no password required
         - cryptoKeySize - length of encryption key - defaults to configured value
         - sskPub, sskPriv - SSK keypair
         - insertPeriod - DBR period in seconds, default 259200 (3 days)
         - refreshInProgress - 1 to mark the id insert record as 'in progress', default 0
    
        Returns:
         - idAddr - the mailing address of the identity
        """
        log = self.log
    
        node = self.Node()
    
        password = kw.get('password', '')
        if password:
            password = hash(password)
    
        mailPassword = kw.get('mailPassword', '')
        if mailPassword:
            mailPassword = hash(mailPassword)
    
        senderPassword = kw.get('senderPassword', '')
    
        # generate an SSK keypair if needed
        if not kw.has_key('sskPub'):
            pub, priv = node.genkeypair()
        else:
            pub, priv = kw['sskPub'], kw['sskPriv']
        
        # now we can build identity mailing address
        idAddr = "%s@%s.freemail" % (name, pub)
    
        db = self.db
        if db.identities.has_key(idAddr):
            raise Exception("identity with address '%s' already exists" % idAddr)
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # generate key
        config = self.db.config
        cryptoKeySize = kw.get('cryptoKeySize', config.cryptoKeySize)
        
        if kw.has_key('cryptoKey'):
            if hasattr(kw['cryptoKey'], 'exportKeyPrivate'):
                cryptoKey = kw['cryptoKey']
            else:
                cryptoKey = SSLCrypto.key(kw['cryptoKey'])
        else:
            cryptoKey = SSLCrypto.key(cryptoKeySize)
        cryptoKeyStr = cryptoKey.exportKeyPrivate()
    
        idUriSite = "SSK@%sPAgM/freemail/%s" % (pub, name)
        idUriSitePriv = "SSK@%s/freemail/%s" % (priv, name)
    
        idUriQueue = kw.get("queueUri", "KSK@%s" % randstring() + "-")
    
        idInsertPeriod = kw.get("insertPeriod", 259200)
    
        # stick into database
        self.db.identities[idAddr] = cell(idAddr=idAddr,
                                          idName=name,
                                          idPopPassword=password,
                                          idMailPassword=mailPassword,
                                          idUriSite=idUriSite,
                                          idUriSitePriv=idUriSitePriv,
                                          idUriQueue=idUriQueue,
                                          idUriNext=0,
                                          idSskPub=pub,
                                          idSskPriv=priv,
                                          idCryptoKey=cryptoKeyStr,
                                          idSlots = slotmap(),
                                          idInsertPeriod=idInsertPeriod,
                                          idLastInserted=0,
                                          idSenderPassword=senderPassword,
                                          idRefreshInProgress=kw.get('refreshInProgress', 0),
                                          )
        self.dbSave()
    
        # send a courtesy greeting to the user
        body = "\r\n".join([
            "Hi and Welcome to FreeMail!",
            "",
            "This is a message from your FreeMail Postmaster.",
            "",
            "If you are reading this, then your new identiy,",
            idAddr,
            "was created successfully and is now ready for use",
            "",
            "You can now send emails from this account, and give",
            "out this freemail address to others.",
            "",
            "Best Regards,",
            "Your FreeMail postmaster",
            "",
            ])
    
        self.writeFromPostmaster(idAddr,
                                 "Welcome to FreeMail, %s" % idAddr,
                                 body)
    
    
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        return idAddr
    #@nonl
    #@-node:dbAddIdentity
    #@+node:dbAddList
    def dbAddList(self, name, **kw):
        """
        Creates a new mailing list record and stores in database
    
        Arguments:
         - name - compulsory - name of this mailing list - short string [a-zA-Z0-9_]+
        
        Keywords:
         - cryptoKey - if provided, this is an SSLCrypto private key
         - cryptoKeySize - length of encryption key - defaults to configured value
         - sskPub, sskPriv - SSK keypair
         - insertPeriod - DBR period in seconds, default 259200 (3 days)
         - refreshInProgress - 1 to mark the id insert record as 'in progress', default 0
    
        Returns:
         - idAddr - the mailing address of the identity
        """
        log = self.log
    
        node = self.Node()
    
        password = kw.get('password', '')
        if password:
            password = hash(password)
    
        mailPassword = kw.get('mailPassword', '')
        if mailPassword:
            mailPassword = hash(mailPassword)
    
        senderPassword = kw.get('senderPassword', '')
    
        # generate an SSK keypair if needed
        if not kw.has_key('sskPub'):
            pub, priv = node.genkeypair()
        else:
            pub, priv = kw['sskPub'], kw['sskPriv']
        
        # now we can build identity mailing address
        idAddr = "%s@%s.freemail" % (name, pub)
    
        db = self.db
        if db.identities.has_key(idAddr):
            raise Exception("identity with address '%s' already exists" % idAddr)
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # generate key
        config = self.db.config
        cryptoKeySize = kw.get('cryptoKeySize', config.cryptoKeySize)
        
        if kw.has_key('cryptoKey'):
            if hasattr(kw['cryptoKey'], 'exportKeyPrivate'):
                cryptoKey = kw['cryptoKey']
            else:
                cryptoKey = SSLCrypto.key(kw['cryptoKey'])
        else:
            cryptoKey = SSLCrypto.key(cryptoKeySize)
        cryptoKeyStr = cryptoKey.exportKeyPrivate()
    
        idUriSite = "SSK@%sPAgM/freemail/%s" % (pub, name)
        idUriSitePriv = "SSK@%s/freemail/%s" % (priv, name)
    
        idUriQueue = kw.get("queueUri", "KSK@%s" % randstring() + "-")
    
        idInsertPeriod = kw.get("insertPeriod", 259200)
    
        # stick into database
        self.db.lists[idAddr] = cell(idAddr=idAddr,
                                     idName=name,
                                     idPopPassword=password,
                                     idMailPassword=mailPassword,
                                     idUriSite=idUriSite,
                                     idUriSitePriv=idUriSitePriv,
                                     idUriQueue=idUriQueue,
                                     idUriNext=0,
                                     idSskPub=pub,
                                     idSskPriv=priv,
                                     idCryptoKey=cryptoKeyStr,
                                     idSlots = slotmap(),
                                     idInsertPeriod=idInsertPeriod,
                                     idLastInserted=0,
                                     idSenderPassword=senderPassword,
                                     idRefreshInProgress=kw.get('refreshInProgress', 0),
                                     )
        self.dbSave()
    
        # send a courtesy greeting to the user
        body = "\r\n".join([
            "Hi and Welcome to FreeMail!",
            "",
            "This is a message from your FreeMail Postmaster.",
            "",
            "If you are reading this, then your new identiy,",
            idAddr,
            "was created successfully and is now ready for use",
            "",
            "You can now send emails from this account, and give",
            "out this freemail address to others.",
            "",
            "Best Regards,",
            "Your FreeMail postmaster",
            "",
            ])
    
        self.writeFromPostmaster(idAddr,
                                 "Welcome to FreeMail, %s" % idAddr,
                                 body)
    
    
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        return idAddr
    #@nonl
    #@-node:dbAddList
    #@+node:dbDelIdentity
    def dbDelIdentity(self, idAddr):
        """
        Purges a mailing identity
    
        This involves:
         - removing the identity from the identities table, and the local dicts
         - removing all mailing relationships involving that identity
         - removing all messages to/from that identity
    
        The database will end up in a form as if the identity never existed.
        """
        db = self.db
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # remove from identities table and local dicts
        del db.identities[idAddr]
    
        # remove all tx and rx messages and message files
        storeDir = db.config.storeDir
    
        for c in [db.txNotSent, db.txNoReceipt, db.txSent, db.txDead]:
            for r in c:
                if r.idAddr == idAddr:
                    msgpath = os.path.join(storeDir, 'tx', r.msgHash)
                    try:
                        os.unlink(msgpath)
                    except:
                        self.log(3, "dbDelIdentity: failed to delete tx msg file %s" % r.msgHash)
            c.removeWhen(idAddr=idAddr)
    
        for r in db.rxMessages:
            if r.idAddr == idAddr:
                msgpath = os.path.join(storeDir, 'rx', r.msgHash)
                try:
                    os.unlink(msgpath)
                except:
                    self.log(3, "dbDelIdentity: failed to delete msg file %s" % r.msgHash)
    
        db.rxMessages.removeWhen(idAddr=idAddr)
    
        # purge is complete - like, who was fred@blahblah.freemail anyway?
    
        self.dbSave()
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:dbDelIdentity
    #@+node:dbAddPeer
    def dbAddPeer(self, peerAddr):
        """
        Adds new peer record
        """
        db = self.db
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        try:
            uri = freemailAddrToUri(peerAddr)
            db.peers[peerAddr] = cell(peerAddr=peerAddr,
                                      peerUriSite=str(uri),
                                      peerNextSloti=0,
                                      )
            self.dbSave()
        
        except:
            self.log("Error adding peer '%s'\n%s" + (peerAddr, exceptionString()))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    #@-node:dbAddPeer
    #@+node:dbDelPeer
    def dbDelPeer(self, peerAddr):
        """
        Deletes a peer from the system.
    
        Radical action - deletes everything to do with that peer
        """
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        del self.peers[peerAddr]
    
        self.dbSave()
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    
    #@-node:dbDelPeer
    #@+node:dbDelTxMsg
    def dbDelTxMsg(self, msgHash):
        """
        Deletes a message from send queue, and from filesystem
        """
        # get handy things
        db = self.db
        config = db.config
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        for c in [db.txNotSent, db.txNoReceipt, db.txSent, db.txDead]:
    
            # retrieve matching database records
            vMsgs = c.select(msgHash=msgHash)
        
            # delete the files one by one
            for rMsg in vMsgs:
                filename = os.path.join(self.db.config.storeDir, "tx", msgHash)
                try:
                    os.unlink(filename)
                except:
                    pass
        
            # and purge from database
            c.removeWhen(msgHash=msgHash)
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@nonl
    #@-node:dbDelTxMsg
    #@+node:dbDelRxMsg
    def dbDelRxMsg(self, msgHash):
        """
        Deletes a message from receive queue, and from filesystem
        """
        # get handy things
        db = self.db
        config = db.config
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # retrieve matching database records
        vMsgs = db.rxMessages.select(msgHash=msgHash)
    
        # delete the files one by one
        for rMsg in vMsgs:
            filename = os.path.join(self.db.config.storeDir, "rx", msgHash)
            try:
                os.unlink(filename)
            except:
                pass
    
        # and purge from database
        db.rxMessages.removeWhen(msgHash=msgHash)
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:dbDelRxMsg
    #@+node:dbGetTxMsgBody
    def dbGetTxMsgBody(self, msgHash):
        """
        Given a message hash, return a string of the full message body
        """
        filename = os.path.join(self.db.config.storeDir, "tx", msgHash)
        fd = open(filename, "rb")
        raw = fd.read()
        fd.close()
        return raw
    #@-node:dbGetTxMsgBody
    #@+node:dbGetRxMsgBody
    def dbGetRxMsgBody(self, msgHash):
        """
        Given a message hash, return a string of the full message body
        """
        filename = os.path.join(self.db.config.storeDir, "rx", msgHash)
        fd = open(filename, "rb")
        raw = fd.read()
        fd.close()
        return raw
    #@-node:dbGetRxMsgBody
    #@+node:log
    def log(self, level, msg, nPrev=0):
        """
        Logs a message if level <= self.verbosity
        """
        if level <= self._verbosity:
    
            self.loglock.acquire()
            try:
                fd = open(self.logfile, "ab")
                caller = traceback.extract_stack()[-(2+nPrev)]
                full = "%s:%s:%s():\n* %s" % (caller[0], caller[1], caller[2], msg.replace("\n", "\n   + "))
                now = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time()))
                msg = "%s %s\n" % (now, full)
                fd.write(msg)
                fd.close()
                if not self.isQuiet:
                    sys.stdout.write(msg)
    
            except:
                traceback.print_exc()
            self.loglock.release()
    
    #@-node:log
    #@+node:logFreenet
    def logFreenet(self, level, msg, nPrev=0):
        """
        Logs a message if level <= self.verbosity
        """
        if level <= self._freenetverbosity:
    
            self.loglock.acquire()
            try:
                fd = open(self.logfile, "ab")
                caller = traceback.extract_stack()[-(2+nPrev)]
                full = "%s:%s:%s():\n* %s" % (caller[0], caller[1], caller[2], msg.replace("\n", "\n   + "))
                now = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time()))
                msg = "%s %s\n" % (now, full)
                fd.write(msg)
                fd.close()
                if not self.isQuiet:
                    sys.stdout.write(msg)
    
            except:
                traceback.print_exc()
            self.loglock.release()
    
    #@-node:logFreenet
    #@+node:verbosity
    def verbosity(self, level):
        """
        Sets logging verbosity
        
        0 - STFU, except for critical messages
        1 - Important messages
        2 - More info
        3 - Even more info
        4 - Debugging only
        """
        self._verbosity = level
        freenet.verbosity(level-1)
    #@-node:verbosity
    #@+node:forceIdRefresh
    def forceIdRefresh(self, idAddr=None):
        """
        Utility method to force the insertion of an identity's
        mailsite.
        
        Arguments:
            - idAddr - address of identity to reinsert. If this argument
              is not given, then all identities will be reinserted
        
        Returns:
            - none
        
        Note - this just schedules the selected id(s) for reinsertion.
        Watch the log output to see the refreshes happening.
        """
        identities = self.db.identities
        idnames = identities.keys()
        log = self.log
        
        if idAddr != None:
            if idAddr in idnames:
                ids = [idAddr]
            else:
                log(1, "Tried to force refresh of nonexistent id\nidAddr=%s" % idAddr)
                return
        else:
            ids = idnames
        
        for id in ids:
            log(3, "Forcing reinsertion of mail id %s" % id)
            identities[id].idLastInserted = 0
    
        self.dbSave()
    
    #@-node:forceIdRefresh
    #@+node:Node
    def Node(self, mode='get', **kw):
        """
        Convenience method which creates a freenet/entropy node object, based on
        the mailserver's current configuration
    
        Arguments:
         - mode - 'put' or 'get', depending on whether this node object will be used
           for inserting or retrieving keys. Determines which htl value in the config
           we use.
        """
    
        db = self.db
        config = db.config
    
        if kw.has_key('htl'):
            htl = kw['htl']
        else:
            if mode == 'get':
                htl = config.htlReceive
            else:
                htl = config.htlSend
    
        return freenet.node(config.fcpHost, config.fcpPort, htl)
    #@-node:Node
    #@+node:decryptAndVerify
    def decryptAndVerify(self, ciphertext, keySender, keyRecipient):
        """
        Decrypts message using a local identity's key,
        then verifies the appended signature against the
        remote peer's public key.
    
        Arguments:
         - msg - raw ciphertext of message. The message is created by
           appending the signature to the end of the plaintext (not even
           a newline), then encrypting it to ascii-armour.
         - keySender - an SSLCrypto key object containing the public key
           of the sender
         - keyRecipient - an SSLCrypto key object containing the public
           and private keys of the recipient
    
        Returns:
         - plaintext of message, if decryption was successful and the signature was verified
         - None if any kind of failure occurred.
    
        Possible causes of failure:
         - keySender or keyRecipient are not valid key objects
         - text cannot be decrypted
         - signature is invalid
        """
    
        # step 1 - check key objects
        if keySender.__class__ != SSLCrypto.key:
            self.log(3, "decryptAndVerify: got invalid sender key")
            return None
        if keyRecipient.__class__ != SSLCrypto.key:
            self.log(3, "decryptAndVerify: got invalid recipient key")
            return None
    
        # step 2 - decrypt ciphertext
        try:
            plainPlusSig = keyRecipient.decStringFromAscii(ciphertext)
        except:
            self.log(3, "decryptAndVerify: failed to decrypt ciphertext")
            return None
    
        # step 3 - split into plaintext plus signature
        try:
            plain, sig = plainPlusSig.split("<StartPycryptoSignature>")
            sig = "<StartPycryptoSignature>" + sig
        except:
            self.log(3, "decryptAndVerify: message appears not to contain signature")
            return None
    
        # step 4 - verify signature against sender key
        try:
            valid = keySender.verifyString(plain, sig)
        except:
            valid = 0
        if not valid:
            self.log(3, "decryptAndVerify: message decrypted, but signature doesn't check out")
            return None
    
        # success
        return plain
    
    #@-node:decryptAndVerify
    #@+node:signAndEncrypt
    def signAndEncrypt(self, plainText, keySender, keyRecipient):
        """
        Signs message using a local identity's key,
        appends signature to message, then encrypts against the
        remote peer's public key.
    
        Arguments:
         - plainText - string - raw plaintext of message.
         - keySender - an SSLCrypto key object containing the public key
           and private keys of the local sender
         - keyRecipient - an SSLCrypto key object containing the public
           key of the remote recipient
    
        Returns:
         - ciphertext, if the keys were valid and the message was successfully
           signed and encypted
         - None if any kind of failure occurred.
    
        Possible causes of failure:
         - keySender or keyRecipient are not valid key objects
        """
    
        # step 1 - check key objects
        if keySender.__class__ != SSLCrypto.key:
            self.log(3, "signAndEncrypt: got invalid sender key")
            return None
        if keyRecipient.__class__ != SSLCrypto.key:
            self.log(3, "signAndEncrypt: got invalid recipient key")
            return None
    
        # step 2 - sign message
        try:
            plainText = str(plainText)
            sig = keySender.signString(plainText)
            signedText = plainText + sig
        except:
            self.log(3, "signAndEncrypt: failed to sign plaintext")
            return None
    
        # step 3 - encrypt message
        try:
            cipherText = keyRecipient.encStringToAscii(signedText)
        except:
            self.log(3, "decryptAndVerify: failed to encrypt signed message")
            return None
    
        # success
        return cipherText
    #@-node:signAndEncrypt
    #@-others
#@-node:class freemailServer
#@+node:class WebUI
class WebUI:
    """
    Handles each incoming HTTP hist
    """
    #@    @+others
    #@+node:__init__
    def __init__(self, req):
        """
        arg 'req' is a handle to an HTTPRequestHandler object
        """
    
        # set up the bare-bones page
        page = http(stream=req.wfile, streamin=req.rfile)
        self.page = page
        self.setCSS()
    
        page.title = "Freemail (build %s) Administration" % build
    
        session = page.session
        self.session = session
    
        fields = session.fields
        self.fields = fields
    
        owner = req.owner
        self.owner = owner
        db = owner.db
        self.db = db
        config = db.config
        self.config = config
        
        self._dbLock = owner._dbLock
        self._dbUnlock = owner._dbUnlock
    
    
        self.req = req
    
        self.log = req.log
    
        # do the interaction
        self.run()
    
        page.send()
        req.wfile.write("\r\n")
    
    #@-node:__init__
    #@+node:run
    def run(self):
    
        #set_trace()
    
        page = self.page
        session = self.session
        fields = self.fields
    
        db = self.db
        config = self.config
        req = self.req
        owner = self.owner
    
        #owner.log(4, "top of method")
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        configState = config.configState
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
        
        #owner.log(4, "ok1")
    
        if configState != 'ready':
            self.wizard(configState)
            return
        
        if not self.isLoggedIn():
            self.showLoginPage()
            return
    
        page.add(center(h3("Freemail Administration (build %s) Web Interface" % build),
                        ))
    
        # decide what page to show
        showpage = fields.get('showpage', 'main')
        #owner.log(4, "ok2")
        method = getattr(self, 'page_'+showpage)
        #owner.log(4, "ok3")
        method()
    
        # temporary - allow HTTP environment dumps
        if session.env['SCRIPT_NAME'].endswith("/env"):
            self.envDump()
    
    #@-node:run
    #@+node:setCSS
    def setCSS(self):
        """
        Adds the CSS stylesheet to the page
        """
        page = self.page
    
        style = self.page.style
    
        cellcolor = "#ffffc0"
        bgcolor = "#ffffc0"
        tablecolor = "#ffffc0"
        textcolor = "#008000"
        font =  'arial, helvetica, sans-serif'
        borderwidth = '0'
        linktextcolor = bgcolor
        linkbgcolor = textcolor
        linkborderwidth = "thick"
        linkbordercolor = textcolor
    
        btntextcolor = bgcolor
        btnbgcolor = textcolor
        btnborderwidth = "thick"
        btnbordercolor = textcolor
        btntextsize = "larger"
    
        fldBgColor = bgcolor
        fldColor = textcolor
        fldFont = font
    
        style['body'] = 'background:'+bgcolor + ';color:'+textcolor + ';font-family:'+font
        style['table.block'] = 'background:'+cellcolor + ';border-width:'+borderwidth
        style['table.main'] = 'background:'+tablecolor + ';border-width:'+borderwidth
        style['table'] = 'background:'+cellcolor + ';border-width:'+borderwidth
        style['.block'] = 'background:'+cellcolor + ';border-width:'+borderwidth
        style['.mainblock'] = 'background:'+cellcolor +';border-width:'+borderwidth + ';height:100%'
        style['a'] = 'text-decoration:none' + ';color:'+linktextcolor + ';background-color:'+linkbgcolor
        style['a:hover'] = 'text-decoration:none' + ';color:'+linkbgcolor + ';background-color:'+linktextcolor
    
        style['.textbutton'] = 'text-decoration:none' \
                               + ';color:'+btntextcolor \
                               + ';background-color:'+btnbgcolor \
                               + ';padding: 2px 2px' \
                               + ':border-width: medium' \
                               + ':border-color:'+btnbordercolor # + ';font-size:'+btntextsize
    
        style['input'] = 'background:'+fldBgColor +';color:'+fldColor + ';font-family:'+fldFont
        style['option'] = 'background:'+fldBgColor +';color:'+fldColor + ';font-family:'+fldFont
        style['select'] = 'background:'+fldBgColor +';color:'+fldColor + ';font-family:'+fldFont
    #@-node:setCSS
    #@+node:page_main
    def page_main(self):
    
        #set_trace()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        fcpHost = config.fcpHost
        fcpPort = config.fcpPort
    
        host = req.headers.getheader("Host").split(":")[0]
    
        # launch engine if required
        if fields['startEngine']:
            #print "startEngine: called"
            owner.running = 1
            owner.startFcpEngine()
    
        if 0 and not fields['shownow']:
            page.add(center("Updating status screen - please wait..."))
            url = "http://%s:%s?showpage=main&shownow=1" % (host, config.httpPort)
            page.head.add('<meta HTTP-EQUIV="refresh" content="1; URL=%s">\n' % url)
            #page.head.add(script('location.href="%s"' % url))
    
            self._dbUnlock()
            # -^-^-^-^-^-^- UNLOCK DATABASE --------------
            return
            
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        # node status
        n = freenet.node(fcpHost, fcpPort)
        try:
            n._handshake()
            systemStatus = 'Online'
        except:
            systemStatus = 'Offline'
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # number of identities
        numIdentities = len(db.identities.keys())
    
        # peers
        numPeers = len(db.peers)
    
        # inbound messages
        numUnreadMessages = len(db.rxMessages.select(isDeleted=0))
        numUnsentMessages = len(db.txNotSent)
        numUnconfirmedMessages = len(db.txNoReceipt)
        numConfirmedMessages = len(db.txSent)
        numDeadMessages = len(db.txDead)
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        if owner.running:
            tdEngine = notag(td("Running"), td())
        else:
            tdEngine = notag(td("Not Running"),
                             td(btnForm("Start Freemail Engine", showpage="main", startEngine=1)))
        
        page.add(center(table(attr(cellspacing=0, cellpadding=3),
                              tr(td(b("Freemail Engine Status: ")),
                                 tdEngine,
                                 ),
                              tr(td(b("Freenet/Entropy Node Status: ")),
                                 td(systemStatus),
                                 td(btnForm("Configure System", showpage="configureSystem")),
                                 ),
                              tr(td(b("Number of Mailing Identities: ")),
                                 td(numIdentities),
                                 td(btnForm("Manage Identities", showpage="manageIdentities")),
                                 ),
                              tr(td(b("Number of Unretrieved Messages: ")),
                                 td(numUnreadMessages),
                                 #td(btnForm("Manage Inbound Messages", showpage="manageInbound")),
                                 ),
                              tr(td(b("Number of Unsent Messages: ")),
                                 td(numUnsentMessages),
                                 #td(btnForm("Manage Outbound Messages", showpage="manageOutbound")),
                                 ),
                              tr(td(b("Number of Unconfirmed Messages: ")),
                                 td(numUnconfirmedMessages),
                                 #td(btnForm("Manage Unconfirmed Messages", showpage="manageOutbound")),
                                 ),
                              tr(td(b("Number of Confirmed Messages: ")),
                                 td(numConfirmedMessages),
                                 #td(btnForm("Manage Confirmed Messages", showpage="manageOutbound")),
                                 ),
                              tr(td(b("Number of Dead Messages: ")),
                                 td(numDeadMessages),
                                 #td(btnForm("Manage Dead Messages", showpage="manageOutbound")),
                                 ),
                              tr(td(attr(colspan=3, align='center'),
                                    table(attr(cellspacing=0, cellpadding=3, border=0, align='center'),
                                          tr(td(btnForm("Refresh", showpage="main")),
                                             td(btnForm("Logout", "/logout")),
                                             ),
                                          ),
                                    ),
                                 ),
                              )))
    
    
    
    #@-node:page_main
    #@+node:page_configureSystem
    def page_configureSystem(self):
    
        #set_trace()
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        savedMsg = ''
        errors = ''
    
        # get values from database
        adminLogin = config.adminLogin
        adminPassword = ''
        adminPassword1 = ''
        fcpHost = config.fcpHost
        fcpPort = config.fcpPort
        httpPort = config.httpPort
        popPort = config.popPort
        telnetPort = config.telnetPort
        smtpPort = config.smtpPort
        adminTimeout = config.adminTimeout
        htlSend = config.htlSend
        htlReceive = config.htlReceive
    
        try:
            receiptAll = config.receiptAll
        except:
            receiptAll = 0
            config.receiptAll = receiptAll
            owner.dbSave()
    
    
        if receiptAll:
            tagReceiptAll = '<input type="checkbox" name="receiptAll" value="yes" CHECKED>'
        else:
            tagReceiptAll = '<input type="checkbox" name="receiptAll" value="yes">'
    
        storeDir = config.storeDir
        rxMaxRetries = config.rxMaxRetries
        txMaxRetries = config.txMaxRetries
        getMaxRegress = config.getMaxRegress
        cryptoKeySize = config.cryptoKeySize
        slotLookAhead = config.slotLookAhead
        txBackoffInit = config.txBackoffInit
        txBackoffMult = config.txBackoffMult
    
        httpHosts = owner.db.httpHosts
        popHosts = owner.db.popHosts
        smtpHosts = owner.db.smtpHosts
        telnetHosts = owner.db.telnetHosts
    
        cryptoKeySel = {1024:'', 2048:'', 3072:'', 4096:''}
        cryptoKeySel[cryptoKeySize] = " selected"
    
        # are we trying to save?
        if fields['command'] == 'save':
            # grab all the values off the form
            adminLogin = fields['adminLogin']
            adminPassword = fields['adminPassword']
            adminPassword1 = fields['adminPassword1']
            fcpHost = fields['fcpHost']
            fcpPort = fields['fcpPort']
            try:
                fcpPort = int(fcpPort)
            except:
                pass
            httpPort = fields['httpPort']
            try:
                httpPort = int(httpPort)
            except:
                pass
            popPort = fields['popPort']
            try:
                popPort = int(popPort)
            except:
                pass
            smtpPort = fields['smtpPort']
            try:
                smtpPort = int(smtpPort)
            except:
                pass
            telnetPort = fields['telnetPort']
            try:
                telnetPort = int(telnetPort)
            except:
                pass
            adminTimeout = fields['adminTimeout']
            try:
                adminTimeout = int(adminTimeout)
            except:
                pass
            htlSend = fields['htlSend']
            try:
                htlSend = int(htlSend)
            except:
                pass
            htlReceive = fields['htlReceive']
            try:
                htlReceive = int(htlReceive)
            except:
                pass
    
            receiptAll = (fields['receiptAll'] == 'yes')
            if receiptAll:
                tagReceiptAll = '<input type="checkbox" name="receiptAll" value="yes" CHECKED>'
            else:
                tagReceiptAll = '<input type="checkbox" name="receiptAll" value="yes">'
            
            storeDir = fields['storeDir']
    
            rxMaxRetries = fields['rxMaxRetries']
            try:
                rxMaxRetries = int(rxMaxRetries)
            except:
                pass
    
            txMaxRetries = fields['txMaxRetries']
            try:
                txMaxRetries = int(txMaxRetries)
            except:
                pass
            getMaxRegress = fields['getMaxRegress']
            try:
                getMaxRegress = int(getMaxRegress)
            except:
                pass
            cryptoKeySel = {1024:'', 2048:'', 3072:'', 4096:''}
            cryptoKeySize = fields['cryptoKeySize']
            try:
                cryptoKeySize = int(cryptoKeySize)
                cryptoKeySel[cryptoKeySize] = " selected"
            except:
                pass
    
            slotLookAhead = fields['slotLookAhead']
            try:
                slotLookAhead = int(slotLookAhead)
            except:
                pass
            txBackoffInit = fields['txBackoffInit']
            try:
                txBackoffInit = int(txBackoffInit)
            except:
                pass
            txBackoffMult = fields['txBackoffMult']
            try:
                txBackoffMult = float(txBackoffMult)
            except:
                pass
    
            httpHosts = [h.strip() for h in fields['httpHosts'].split(",")]
            smtpHosts = [h.strip() for h in fields['smtpHosts'].split(",")]
            popHosts = [h.strip() for h in fields['popHosts'].split(",")]
            telnetHosts = [h.strip() for h in fields['telnetHosts'].split(",")]
    
            # now validate them
            problems = []
            if adminLogin == '':
                problems.append("You must enter a username")
            if adminPassword or adminPassword1:
                if adminPassword == '':
                    problems.append("You must enter a password")
                elif adminPassword != adminPassword1:
                    problems.append("Your passwords do not match")
                elif len(password) < 6:
                    problems.append("Your password is too short")
    
            if fcpHost == '':
                problems.append("You must specify an FCP host")
    
            if not fcpPort:
                problems.append("You must specify an FCP port")
            elif type(fcpPort) is not type(0):
                problems.append("FCP Port must be a number")
            elif fcpPort < 1 or fcpPort > 65535:
                problems.append("Invalid FCP Port '%s' - should be between 1 and 65535" % fcpPort)
    
            if not httpPort:
                problems.append("You must specify an HTTP port")
            elif type(httpPort) is not type(0):
                problems.append("HTTP Port must be a number")
            elif httpPort < 1 or httpPort > 65535:
                problems.append("Invalid HTTP Port '%s' - should be between 1 and 65535" % httpPort)
    
            if not popPort:
                problems.append("You must specify a POP3 port")
            elif type(popPort) is not type(0):
                problems.append("POP3 Port must be a number")
            elif popPort < 1 or popPort > 65535:
                problems.append("Invalid POP3 Port '%s' - should be between 1 and 65535" % popPort)
    
            if not smtpPort:
                problems.append("You must specify an SMTP port")
            elif type(smtpPort) is not type(0):
                problems.append("SMTP Port must be a number")
            elif smtpPort < 1 or smtpPort > 65535:
                problems.append("Invalid SMTP Port '%s' - should be between 1 and 65535" % smtpPort)
    
            if not telnetPort:
                problems.append("You must specify a Telnet port")
            elif type(telnetPort) is not type(0):
                problems.append("Telnet Port must be a number")
            elif telnetPort < 1 or telnetPort > 65535:
                problems.append("Invalid Telnet Port '%s' - should be between 1 and 65535" % telnetPort)
    
            if not adminTimeout:
                problems.append("You must specify an admin timeout")
            elif type(adminTimeout) is not type(0):
                problems.append("Admin timeout must be a number")
            elif adminTimeout < 60:
                problems.append("Dumb value for admin timeout - "
                                "please try at least 60 (recommend 3600)")
            if not htlSend and type(htlSend) != type(0):
                problems.append("You must specify an HTL value for sending")
            elif type(htlSend) is not type(0):
                problems.append("HTL TX value must be a number")
            elif htlSend  > 100:
                problems.append("Dumb HTL TX value '%s' - try something like 20" % htlSend)
            if not htlReceive and type(htlReceive) != type(0):
                problems.append("You must specify an HTL value for receiving")
            elif type(htlReceive) is not type(0):
                problems.append("HTL RX value must be a number")
            elif htlReceive  > 100:
                problems.append("Dumb HTL RX value '%s' - try something like 20" % htlReceive)
            if not storeDir:
                problems.append("You must choose a datastore directory")
            else:
                if os.path.isdir(storeDir):
                    # try to set permissions
                    try:
                        os.chmod(storeDir, 0700)
                    except:
                        problems("Failed to set permissions for store directory '%s' "
                                 "please choose another path" % storeDir)
                else:
                    # try to create store directory now
                    try:
                        os.makedirs(storeDir, 0700)
                    except:
                        problems.append("Failed to create store directory '%s' "
                                        "please try something else" % storeDir)
    
            if not rxMaxRetries:
                problems.append("You must specify a maximum receive retry count")
            elif type(rxMaxRetries) is not type(0):
                problems.append("receive retry count should be a number")
            elif rxMaxRetries not in range(1, 20):
                problems.append("Invalid receive retry limit '%s' - "
                                "please choose something between 1 and 20" % rxMaxRetries)
    
            if not txMaxRetries:
                problems.append("You must specify a maximum transmit retry count")
            elif type(txMaxRetries) is not type(0):
                problems.append("transmit retry count should be a number")
            elif txMaxRetries not in range(1, 29):
                problems.append("Invalid transmit retry limit '%s' - "
                                "please choose something between 1 and 28" % txMaxRetries)
            elif getMaxRegress not in range(1, 20):
                problems.append("Invalid regress limit '%s' - "
                                "please choose something between 1 and 28" % getMaxRegress)
            elif cryptoKeySize not in [1024, 2048, 3072, 4096]:
                problems.append("Bad crypto key size '%s' - choose 1024, 2048, 3072 or 4096" % cryptoKeySize)
    
            if not slotLookAhead:
                problems.append("You must specify an initial rx slot look-ahead count")
            elif type(slotLookAhead) is not type(1):
                problems.append("RX Slot look-ahead should be a number, best 2-10")
            elif slotLookAhead < 1 or slotLookAhead > 10:
                problems.append("Crazy RX slot look-ahead '%s' - "
                                "please choose something between 1 and 10 (recommend 5)" % slotLookAhead)
    
            if not txBackoffInit:
                problems.append("You must specify an initial tx backoff interval")
            elif type(txBackoffInit) is not type(1):
                problems.append("initial send backoff interval should be a number, best 7200 or more")
            elif txBackoffInit < 300 or txBackoffInit > 86400:
                problems.append("Crazy initial send backoff interval '%s' - "
                                "please choose something between 1800 and 86400" % txBackoffInit)
    
            if not txBackoffMult:
                problems.append("You must specify a send backoff multiplier")
            elif type(txBackoffMult) not in [type(1), type(1.1)]:
                problems.append("Send backoff multiplier should be a number from 1.1 to 4.0")
            elif txBackoffMult < 1.1 or txBackoffMult > 4.0:
                problems.append("Crazy send backoff multiplier '%s' - "
                                "please choose something between 1.1 and 4.0" % txBackoffMult)
    
            if not httpHosts or httpHosts == ['']:
                httpHosts = ['127.0.0.1']
            if not smtpHosts or smtpHosts == ['']:
                smtpHosts = ['127.0.0.1']
            if not popHosts or popHosts == ['']:
                popHosts = ['127.0.0.1']
            if not telnetHosts or telnetHosts == ['']:
                telnetHosts = ['127.0.0.1']
    
            if problems:
                errors = notag("Sorry, but there were errors:", ulFromList(problems))
    
            else:
                # write new values to database
                config.adminLogin = adminLogin
                if adminPassword:
                    config.adminPassword = hash(adminPassword)
                config.fcpHost = fcpHost
                config.fcpPort = fcpPort
                config.popPort = popPort
                config.smtpPort = smtpPort
                config.httpPort = httpPort
                config.telnetPort = telnetPort
                config.adminTimeout = adminTimeout
                config.htlSend = htlSend
                config.htlReceive = htlReceive
                config.receiptAll = receiptAll
                config.storeDir = storeDir
                config.rxMaxRetries = rxMaxRetries
                config.txMaxRetries = txMaxRetries
                config.getMaxRegress = getMaxRegress
                config.cryptoKeySize = cryptoKeySize
                config.slotLookAhead = slotLookAhead
                config.txBackoffInit = txBackoffInit
                config.txBackoffMult = txBackoffMult
    
                # now the allowed hosts
                owner.db.httpHosts = httpHosts
                owner.db.smtpHosts = smtpHosts
                owner.db.popHosts = popHosts
                owner.db.telnetHosts = telnetHosts
    
                owner.dbSave()
                savedMsg = center(i("Changes saved successfully"))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        page.add(center(h3("System Configuration")))
        page.add(center(btnForm("Return to Main Page", showpage="main")))
    
        page.add(center(table(attr(align='center', cellspacing=0, cellpadding=2),
                              tr(td(attr(colspan=2),
                                    errors, savedMsg)),
                              form(attr(method="POST"),
                                   input(type="hidden", name="showpage", value="configureSystem"),
                                   input(type="hidden", name="command", value="save"),
    
                                   # ---------------------------------------
                                   tr(td(attr(colspan=2, align='center'),
                                         hr(),
                                         b(big("Security Settings")),
                                         )),
                                   tr(td(b("Admin Interface Login: ")),
                                      td(input(type="text", name="adminLogin", value=adminLogin))),
                                   tr(td(b("Password: "),
                                         br(),
                                         small(i("(Leave blank to keep existing password)")),
                                         ),
                                      td(input(type="text", name="adminPassword", value=""))),
                                   tr(td(b("Confirm Password: ")),
                                      td(input(type="text", name="adminPassword1", value=""))),
    
                                   # ---------------------------------------
                                   tr(td(attr(colspan=2, align='center'),
                                         hr(),
                                         b(big("Server Settings")),
                                         )),
                                   tr(td(b("POP3 Port: ")),
                                      td(input(type="text", name="popPort", size=6, maxlen=5, value=popPort))),
                                   tr(td(b("Allowed POP3 hosts"), br(),
                                         small(i("(comma-separated)")),
                                         ),
                                      td(input(type="text", name="popHosts", size=32, maxlen=80, value=",".join(popHosts))),
                                      ),
    
                                   tr(td(b("SMTP Port: ")),
                                      td(input(type="text", name="smtpPort", size=6, maxlen=5, value=smtpPort))),
                                   tr(td(b("Allowed SMTP hosts"), br(),
                                         small(i("(comma-separated)")),
                                         ),
                                      td(input(type="text", name="smtpHosts", size=32, maxlen=80, value=",".join(smtpHosts))),
                                      ),
    
                                   tr(td(b("Web Interface Port: ")),
                                      td(input(type="text", name="httpPort", size=6, maxlen=5, value=httpPort))),
                                   tr(td(b("Web Interface Timeout: "), br(),
                                         small(i("(seconds)"))),
                                      td(input(type="text", name="adminTimeout", size=8, maxlen=6, value=adminTimeout)),
                                      ),
                                   tr(td(b("Allowed HTTP hosts"), br(),
                                         small(i("(comma-separated)")),
                                         ),
                                      td(input(type="text", name="httpHosts", size=32, maxlen=80, value=",".join(httpHosts))),
                                      ),
    
                                   tr(td(b("Telnet Port: ")),
                                      td(input(type="text", name="telnetPort", size=6, maxlen=5, value=telnetPort))),
                                   tr(td(b("Allowed Telnet hosts"), br(),
                                         small(i("(comma-separated)")),
                                         ),
                                      td(input(type="text", name="telnetHosts",
                                               size=32, maxlen=80, value=",".join(telnetHosts))),
                                      ),
    
                                   # ---------------------------------------
                                   tr(td(attr(colspan=2, align='center'),
                                         hr(),
                                         b(big("Freenet/Entropy Node Settings")),
                                         )),
                                   tr(td(b("FCP Host: ")),
                                      td(input(type="text", name="fcpHost", size=32, maxlen=32, value=fcpHost))),
                                   tr(td(b("FCP Port: ")),
                                      td(input(type="text", name="fcpPort", size=6, maxlen=5, value=fcpPort))),
                                   tr(td(b("Hops To Live (sending): "),
                                         ),
                                      td(input(type="text", name="htlSend", size=3, maxlen=3, value=htlSend)),
                                      ),
                                   tr(td(b("Hops To Live (receiving): "),
                                         ),
                                      td(input(type="text", name="htlReceive", size=3, maxlen=2, value=htlReceive)),
                                      ),
    
                                   # ---------------------------------------
                                   tr(td(attr(colspan=2, align='center'),
                                         hr(),
                                         b(big("Message Transport Settings")),
                                         )),
                                   tr(td(b("Issue receipts for all sent messages?")),
                                      td(tagReceiptAll),
                                      ),
    
                                   tr(td(b("Message Store Directory: "),
                                         ),
                                      td(input(type="text", name="storeDir", size=32, value=storeDir)),
                                      ),
    
                                   tr(td(b("Receive Retry Count: "),
                                         ),
                                      td(input(type='text', name='rxMaxRetries', size=6, value=rxMaxRetries),
                                         )
                                      ),
    
                                   tr(td(b("Maximum Retries for Sending: ")),
                                      td(input(type="text", name="txMaxRetries", size=3, maxlen=2, value=txMaxRetries)),
                                      ),
                                   tr(td(b("Maximum Regress for peer site retrievals: ")),
                                      td(input(type="text", name="getMaxRegress", size=3, maxlen=2, value=getMaxRegress)),
                                      ),
                                   tr(td(attr(align='left'),
                                         b("Encryption Key Size: "),
                                         ),
                                      td('',
                                         #input(type='text', name='cryptoKeySize', size=6, value=cryptoKeySize),
                                         '<select name="cryptoKeySize">',
                                         '<option label="1024 bits (low grade)" value="1024" %s>'
                                           '1024 bits (low grade)</option>' % cryptoKeySel[1024],
                                         '<option label="2048 bits (personal grade)", value="2048" %s>'
                                           '2048 bits (personal grade)</option>' % cryptoKeySel[2048],
                                         '<option label="3072 bits (commercial grade)", value="3072" %s>'
                                           '3072 bits (commercial grade)</option>' % cryptoKeySel[3072],
                                         '<option label="4096 bits (military grade)", value="4096" %s>'
                                           '4096 bits (military grade)</option>' % cryptoKeySel[4096],
                                         "</select>",
                                         ),
                                      ),
                                   tr(td(b("Receive Slot Look-Ahead: "),
                                         ),
                                      td(input(type='text', name='slotLookAhead', size=3, maxlen=2, value=slotLookAhead)),
                                      ),
                                   tr(td(b("Initial Send Retry Interval: "), br(),
                                         small(i("(seconds)"))
                                         ),
                                      td(input(type='text', name='txBackoffInit', size=7, maxlen=6, value=txBackoffInit)),
                                      ),
                                   tr(td(b("Send Retry Multiplier: "), br(),
                                         small(i("(float)"))
                                         ),
                                      td(input(type='text', name='txBackoffMult', size=5, maxlen=4, value=txBackoffMult)),
                                      ),
                                   tr(td(attr(colspan=2, align="center"),
                                         hr(),
                                         btn("Save Changes")),
                                      ),
                                  )
                              )
                        )
                 )
    
    
    #@-node:page_configureSystem
    #@+node:page_manageIdentities
    def page_manageIdentities(self):
    
        #set_trace()
    
        #self.log(4, "top of method")
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        try:
            page = self.page
            session = self.session
            fields = self.fields
            req = self.req
    
            owner = self.owner
            db = owner.db
            config = db.config
        
            page.add(center(h3("Manage Mailing Identities")))
            page.add(center(table(attr(cellspacing=0, cellpadding=0, border=0),
                                  tr(td(btnForm("Return to Main Page", showpage="main")),
                                     td(btnForm("Create New Identity", showpage="createIdentity")),
                                     )
                                  )
                            )
                     )
        
            # pick up any delete or delete confirmation command
            cmd = fields['cmd']
    
            if cmd == 'deleteOk':
                #self.log(2, "ok1")
                idAddr = fields['idAddr']
                #self.log(2, "ok2")
                db = owner.db
                #self.log(2, "ok3")
                if db.identities.has_key(idAddr):
                    #self.log(2, "ok4")
                    owner.dbDelIdentity(idAddr)
                    #self.log(2, "ok5")
                    page.add(br(), br(),
                             center(i("Identity "), br(),
                                    b(idAddr), br(),
                                    i(" was purged completely. Kaput.", br(),
                                      "If there was any sensitive information, however, please be warned "
                                      "that a forensic audit of your PC can easily recover the files. So "
                                      "you may want to run a disk-cleaner program that wipes off all free space.",
                                      )),
                             )
                #self.log(2, "ok6")
        
            elif cmd == 'delete':
                idAddr = fields['idAddr']
                page.add(center(table(attr(width='60%', cellspacing=0, cellpadding=3, border=0),
                                      tr(td(attr(colspan=2, align='center'),
                                            br(),
                                            big(big(b("Warning"))), br(),
                                            "you are about to delete your own mailing identity", br(),
                                            big(b(idAddr)), br(),
                                            "from your Freemail database.", br(),
                                            "This will permanently and irrecoverably delete the identity, "
                                            "as well as all record of any messages "
                                            "involving this identity.", br(), br(),
                                            "Are you really sure you want to do this?")),
                                      tr(td(attr(align='right'),
                                            btnForm("Yes",
                                                    showpage='manageIdentities',
                                                    cmd='deleteOk',
                                                    idAddr=idAddr)),
                                         td(attr(align='left'),
                                            btnForm("No",
                                                    showpage='manageIdentities'))))))
                self._dbUnlock()
                # -^-^-^-^-^-^- UNLOCK DATABASE --------------
                return
        
            # display a listing of all active mailing identities
            db = self.owner.db
            vIds = db.identities.values()
            vPeers = db.peers
            vRxMsgs = db.rxMessages
    
            tIds = notag()
            tMain = table(attr(align='center', cellspacing=0, cellpadding=3, border=1),
                          tr(td(attr(align='center', colspan=1),
                                b("Your Mailing Identities")
                                )),
                          br(), br(),
                          tIds,
                          )
            page.add(tMain)
    
            if not vIds:
                tIds.add(tr(td(i("(No identities currently defined)"))))
    
            # populate with our identities
            for rId in vIds:
                idAddr = rId.idAddr
    
                vTxNotSent = db.txNotSent.select(idAddr=idAddr)
                vTxNoReceipt = db.txNoReceipt.select(idAddr=idAddr)
                vTxSent = db.txSent.select(idAddr=idAddr)
                vTxDead = db.txDead.select(idAddr=idAddr)
    
                nTxNotSent = len(vTxNotSent)
                nTxNoReceipt = len(vTxNoReceipt)
                nTxSent = len(vTxSent)
                nTxDead = len(vTxDead)
                nTxTotal = nTxNotSent + nTxNoReceipt + nTxDead
        
                vRxI = vRxMsgs.select(idAddr=idAddr)
                nRxMsgsTotal = len(vRxI)
                nRxMsgsUnread = len(vRxI.select(isDeleted=0))
    
                tIds.add(tr(td(table(attr(cellspacing=0, cellpadding=0, border=0),
                                     tr(td(b("FreeMail address::&nbsp;")),
                                        td(idAddr)),
                                     tr(td(b("Stored in Freenet as:&nbsp;")),
                                        td(rId.idUriSite+"//")),
                                     tr(td(b("SSK Public Key:")),
                                        td(rId.idSskPub)),
                                     tr(td(b("SSK Private Key:")),
                                        td(rId.idSskPriv)),
                                     tr(td(b("Receive Status::&nbsp;")),
                                        td("&bull;&nbsp;%d unread messages (total %d received)" % (nRxMsgsUnread, nRxMsgsTotal))),
                                     tr(td(b("Send Status::&nbsp;")),
                                        td("&bull;&nbsp;%d messages sent (%d waiting, %d awaiting receipt, "
                                           "%d confirmed, %d dead)" % (nTxTotal,
                                                                       nTxNotSent,
                                                                       nTxNoReceipt,
                                                                       nTxSent,
                                                                       nTxDead,
                                                                       ),
                                           )),
    
                                     tr(# td(btnForm('Manage Identity', showpage='manageIdentity', idAddr=idAddr)),
                                        td(attr(colspan=2, align='center'),
                                           btnForm('Delete Identity', showpage='manageIdentities', cmd='delete', idAddr=idAddr)),
                                        ),
                                     ),
                               )))
        
                #print "idAddr=%s" % idAddr
            #print "----"
    
        except:
            self.log(2, "Some operation failed here:\n%s" % exceptionString())
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:page_manageIdentities
    #@+node:page_createIdentity
    def page_createIdentity(self):
    
        #set_trace()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        log = owner.log
    
        problems = []
        addproblem = problems.append
    
        page.add(center(h3("Create New Mailing Identity"),
                        table(attr(cellspacing=0, cellpadding=3, align='center'),
                              tr(td(btnForm("Return to Main Page", showpage="main")),
                                 td(btnForm("Return to Identities", showpage='manageIdentities')))),
                        br(),
                        ))
    
        # parse the HTTP headers to get the hostname used by the browser
        #print req.headers
        #print type(req.headers)
        #nl = re.compile("[\\r\\n]+")
        #headerlines = nl.split(req.headers)
        host = req.headers.getheader("Host").split(":")[0]
        log(3, "page_createIdentity: you reached this web interface as http://%s:%s" % (host, config.httpPort))
    
        idName = fields['idName']
        sskPub = fields['sskPub']
        sskPriv = fields['sskPriv']
        genssk = fields['genssk']
        keySize = fields['keySize']
        keyAsTxt = fields['keyAsTxt']
        pop3Password = fields['pop3Password']
        pop3Password1 = fields['pop3Password1']
        insertHtl = fields['insertHtl']
    
        try:
            insertPeriod = int(fields['insertPeriod'])
        except:
            insertPeriod = 86400 # set the mailsite DBR period to 24 hours
            
        cryptoKeyLen = 2048
    
        cmd = fields.get('cmd', 'new')
    
        # generate an ssk keypair if needed
        if genssk:
            node = self.owner.Node()
            sskPub, sskPriv = node.genkeypair()
            cmd = 'genssk'
    
        # processing of different stages is here in reverse order, to allow a 'drop-thru'
        # to previous state in case anything goes wrong. Data from earlier states gets
        # stored as form variables in later states
    
        if cmd == 'insertid':
    
            # save in database, and do the insert
    
            # re-constitute key
            log(2, "re-constituting SSLCrypto key")
            key = SSLCrypto.key(b64dec(keyAsTxt))
            log(2, "SSLCrypto key reconstituted fine")
    
            # save all in database
            idAddr = owner.dbAddIdentity(idName,
                                         password=pop3Password,
                                         cryptoKey=key,
                                         sskPub=sskPub,
                                         sskPriv=sskPriv,
                                         insertPeriod=insertPeriod,
                                         refreshInProgress=1,
                                         )
    
            log(2, "new identity for %s added to database" % idName)
    
            # create node object
            try:
                insertHtl = int(insertHtl)
            except:
                addproblem("HTL must be a number from 0 to 40")
                page.add(center(big(b("Sorry")),
                                "There were problems with your request:",
                                ulFromList(problems)))
                problems = []
                cmd = 'generatekey' # drop thru to prev state
    
            if not problems:
                node = self.owner.Node(htl=insertHtl)
    
                # stick the mailsite in
                try:
                    log(2, "inserting new identity %s into freenet (entropy)" % idName)
                    owner.fcpRefreshMailsite(idAddr, htl=insertHtl)
                    log(2, "insert of new identity %s successful" % idName)
                except:
                    addproblem("Failed to insert into Freenet/Entropy the info for your new identity:\n%s\n%s" % (
                        idAddr, exceptionString()))
                    log(2, "New id insert problem:\n%s" % exceptionString())
    
                if problems:
                    page.add(center(big(b("Sorry")),
                                    "There were problems with your request:",
                                    ulFromList(problems)))
                    problems = []
                    cmd = 'generatekey' # drop thru to prev state
                else:
                    # everything went fine
                    page.add(center(b("Success"), br(), br(),
                                    "Your new freemail account was successfully created and inserted).",
                                    br(), br(),
                                    "Your new FreeMail address is:", br(),
                                    b(idAddr), br(), br(),
                                    "and it has been inserted into Freenet (or Entropy) as:", br(),
                                    b(owner.db.identities[idAddr].idUriSite+"//"), br(), br(),
    
                                    b("One last step:"), br(),
                                    "Please now create a new mailing account in your normal "
                                    "email program", br(),
                                    small(i("(Mozilla, Evolution, Mutt, Pine, Kmail, Outlook Express etc)")), br(),
                                    "and set it up with the following details:", br(), br(),
    
                                    table(attr(align='center', cellspacing=0, cellpadding=3, border=1),
                                          tr(td(attr(align='center', colspan=2),
                                                b("General Settings:"))),
                                          tr(td(b("Name: ")),
                                             td("%s at FreeMail" % idName)),
                                          tr(td(b("Email Address: ")),
                                             td(attr(valign='top'), idAddr)),
                                          tr(td(attr(align='center', colspan=2),
                                                b("Receive Settings:"))),
                                          tr(td(attr(valign='top'),
                                                b("POP3 Server:")),
                                             td(attr(valign='top'), host)),
                                          tr(td(b("POP3 Port: ")),
                                             td(attr(valign='top'), config.popPort)),
                                          tr(td(b("POP3 Username: ")),
                                             td(attr(valign='top'), idAddr)),
                                          tr(td(b("POP3 Password: ")),
                                             td(attr(valign='top'), pop3Password)),
                                          tr(td(attr(align='center', colspan=2),
                                                b("Send Settings:"))),
                                          tr(td(b("SMTP Server: ")),
                                             td(attr(valign='top'), host)),
                                          tr(td(b("SMTP Port: ")),
                                             td(attr(valign='top'), config.smtpPort)),
                                          ),
                                    ))
    
        if cmd == 'generatekey':
    
            # Generate a crypto key of required size, notify of insertion
            try:
                keySize = int(keySize)
            except:
                try:
                    log(2,
                        "Strange problem - exception determining int(keySize)\n"
                        "keySize='%s'\n"
                        "defaulting to 3072-bit\n"
                        "%s" % (
                             keySize, exceptionString()))
                except:
                    log(2,
                        "Even stranger - the local var 'keySize' has gone AWOL!!!\n"
                        "Defaulting to 3072-bit\n"
                        "%s" % exceptionString())
                    keySize = 3072
            key = SSLCrypto.key(keySize, 'ElGamal')
            keyAsTxt = b64enc(key.exportKeyPrivate())
    
            page.add(center(form(attr(method="POST", action="/"),
                                 input(type='hidden', name='showpage', value='createIdentity'),
                                 input(type='hidden', name='cmd', value='insertid'),
                                 input(type='hidden', name='idName', value=idName),
                                 input(type='hidden', name='sskPub', value=sskPub),
                                 input(type='hidden', name='sskPriv', value=sskPriv),
                                 input(type='hidden', name='keyAsTxt', value=keyAsTxt),
                                 input(type='hidden', name='pop3Password', value=pop3Password),
    
                                 b("Insert Identity into Freenet"), br(),
                                 "Your new mailing identity is ready to be inserted into Freenet (or Entropy)", br(),
                                 "Please select a HTL (hops-to-live) parameter for this insertion.", br(),
                                 small(i("(Note - the higher this value, the longer it will take now to "
                                         "complete the insertion - but the easier it will be for other "
                                         "FreeMail servers to locate your identity.", br(),
                                         "Feel free to use a low value (even 0) if you are presently testing FreeMail.)",
                                         )), br(), br(),
    
                                 table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                       tr(td(b("Insert HTL")),
                                          td(input(type='text', name='insertHtl', value="15"))),
                                       ),
                                       
                                 btn("Insert into Freenet and Continue"), br(),
                                 small(i("(Please click this only once)")),
    
                                 )))
    
        if cmd == 'promptkeysize':
    
            # validate the POP3 passwords
            if pop3Password == '':
                addproblem("You must choose a password")
            elif pop3Password != pop3Password1:
                addproblem("Passwords do not match")
            if problems:
                page.add(center(big(b("Sorry")),
                                "There were problems with your request:",
                                ulFromList(problems)))
                problems = []
                cmd = 'getpassword' # drop thru to prev state
            else:
                # ssk keys are fine
                page.add(center(form(attr(method="POST", action="/"),
                                     input(type='hidden', name='showpage', value='createIdentity'),
                                     input(type='hidden', name='cmd', value='generatekey'),
                                     input(type='hidden', name='idName', value=idName),
                                     input(type='hidden', name='sskPub', value=sskPub),
                                     input(type='hidden', name='sskPriv', value=sskPriv),
                                     input(type='hidden', name='pop3Password', value=pop3Password),
    
                                     b("Generate encryption key"), br(), br(),
                                     "How strong do you want the encryption for your incoming messages?", br(),
                                     small(i("(Note - if you choose a large key, and/or if you are running "
                                             "on a slow machine, this key could take a while to generate, "
                                             "so please be patient.)")), br(), br(),
                                     
                                     table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                           tr(td(label('<input type="radio", name="keySize", value="512">',
                                                       "512 bits (weak, for testing only)"))),
                                           tr(td(label('<input type="radio", name="keySize", value="1024">',
                                                       "1024 bits (personal grade)"))),
                                           tr(td(label('<input type="radio", name="keySize", value="2048" checked>',
                                                       "2048 bits (commercial grade)"))),
                                           tr(td(label('<input type="radio", name="keySize", value="3072">',
                                                       "3072 bits (military grade)"))),
                                           tr(td(label('<input type="radio", name="keySize", value="4096">',
                                                       "4096 bits (paranoia grade)"))),
                                           ),
    
                                     btn("Generate Encryption Key and Continue"), br(),
                                     small(i("(Please click this only once)")),
    
                                     )))
    
        if cmd == 'getpassword':
    
            # prompt for POP3 password
            # validate the SSK keypair
            node = owner.Node(htl=0)
            try:
                pub = node.put("test", "", "SSK@%s/xxxtest-%s" % (sskPriv, random.randint(0, 1000000))).uri.hash
                if pub != sskPub:
                    addproblem("The public and private keys do not match")
            except:
                addproblem("Cannot contact node to validate your keypair")
                self.log(1, "Failed to contact node:\n%s" % exceptionString())
    
            if problems:
                page.add(center(big(b("Sorry")),
                                "There were problems with your request:",
                                ulFromList(problems)))
                problems = []
                cmd = 'genssk' # drop thru to prev state
            else:
                page.add(center(form(attr(method="POST", action="/"),
                                     input(type='hidden', name='showpage', value='createIdentity'),
                                     input(type='hidden', name='cmd', value='promptkeysize'),
                                     input(type='hidden', name='idName', value=idName),
                                     input(type='hidden', name='sskPub', value=sskPub),
                                     input(type='hidden', name='sskPriv', value=sskPriv),
                                     
                                     b("Secure your POP3 Mailbox"), br(), br(),
                                     "Please choose a password, that your mail program will use "
                                     "for retrieving your FreeMail messages via the POP3 port.", br(),br(),
                                     
                                     table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                           tr(td(b("POP3 Password: ")),
                                              td(input(type="text", name="pop3Password", value=pop3Password, size=16))),
                                           tr(td(b("Confirm POP3 Password: ")),
                                              td(input(type="text", name="pop3Password1", value=pop3Password1, size=16))),
                                           ),
                                     btn("Continue"), br(),
    
                                     ),
                                ),
                         )
            
        if cmd == 'genssk':
    
            # validate chosen name
            lenIdName = len(idName)
            if idName == '':
                addproblem("You did not enter a name")
            elif lenIdName < 3:
                addproblem("Name is too short")
            if lenIdName > 16:
                addproblem("Name is too long")
            if not re.match("^[a-zA-Z0-9\\-]+$", idName):
                addproblem("Name should only consist of letters, digits or hyphens")
    
            if problems:
                page.add(center(big(b("Sorry")),
                                "There were problems with your request:",
                                ulFromList(problems)))
                cmd = 'new' # drop thru to prev state
    
            else:
                # all ok - stick up ssk keys form
                page.add(center(form(attr(method="POST", action="/"),
                                     input(type='hidden', name='showpage', value='createIdentity'),
                                     input(type='hidden', name='cmd', value='getpassword'),
                                     input(type='hidden', name='idName', value=idName),
    
                                     b("Secure your mailing identity"), br(), br(),
                                     "In order to protect your identity against tampering "
                                     "attacks, we will need to secure it with a freenet SSK keypair.",
                                     br(),
                                     small(i("If you have an existing SSK keypair ",
                                             "(eg, one which you are using with an freesite), "
                                             "please enter it here.", br(),
                                             "Otherwise, please select 'Generate New Keys' to "
                                             "generate a whole new keypair.")), br(), br(),
    
                                     table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                           tr(td(attr(colspan=2, align='center'),
                                                 label(input(type='checkbox', name='genssk', value='yes'),
                                                       "Generate new SSK keys"
                                                       ))),
                                           tr(td(b("SSK Public Key: ")),
                                              td(input(type="text", name="sskPub", value=sskPub, size=40))),
                                           tr(td(b("SSK Private Key: ")),
                                              td(input(type="text", name="sskPriv", value=sskPriv, size=40))),
                                           tr(td(attr(colspan=2, align='center'),
                                                 center(btn("Continue")))),
                                           )))),
                                           
    
        if cmd == 'new':
            # first stage - prompt for name, password and keysize
            page.add(center(form(attr(method="POST", action="/"),
                                 input(type='hidden', name='showpage', value='createIdentity'),
                                 input(type='hidden', name='cmd', value='genssk'),
    
                                 b("Please choose a short name"), br(),
                                 small(i("(3-16 chars - letters, digits and '-' only)")),
    
                                 table(attr(cellspacing=0, cellpadding=3, align='center', border=1),
                                       tr(td(input(type="text", name="idName", value=idName, size=18, maxlen=16)),
                                          ),
                                       ),
                                 center(btn("Continue")),
                                 ),
                            ),
                     )
    
        return
    
    #@-node:page_createIdentity
    #@+node:page_createList
    def page_createList(self):
    
        #set_trace()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        log = owner.log
    
        problems = []
        addproblem = problems.append
    
        page.add(center(h3("Create New Mailing List"),
                        table(attr(cellspacing=0, cellpadding=3, align='center'),
                              tr(td(btnForm("Return to Main Page", showpage="main")),
                                 td(btnForm("Return to Lists", showpage='manageLists')))),
                        br(),
                        ))
    
        # parse the HTTP headers to get the hostname used by the browser
        #print req.headers
        #print type(req.headers)
        #nl = re.compile("[\\r\\n]+")
        #headerlines = nl.split(req.headers)
        host = req.headers.getheader("Host").split(":")[0]
        log(3, "page_createList: you reached this web interface as http://%s:%s" % (host, config.httpPort))
    
        listName = fields['listName']
        sskPub = fields['sskPub']
        sskPriv = fields['sskPriv']
        genssk = fields['genssk']
        keySize = fields['keySize']
        keyAsTxt = fields['keyAsTxt']
        pop3Password = fields['pop3Password']
        pop3Password1 = fields['pop3Password1']
        insertHtl = fields['insertHtl']
    
        try:
            insertPeriod = int(fields['insertPeriod'])
        except:
            insertPeriod = 86400 # set the listsite DBR period to 24 hours
            
        cryptoKeyLen = 2048
    
        cmd = fields.get('cmd', 'new')
    
        # generate an ssk keypair if needed
        if genssk:
            node = self.owner.Node()
            sskPub, sskPriv = node.genkeypair()
            cmd = 'genssk'
    
        # processing of different stages is here in reverse order, to allow a 'drop-thru'
        # to previous state in case anything goes wrong. Data from earlier states gets
        # stored as form variables in later states
    
        if cmd == 'insertlist':
    
            # save in database, and do the insert
    
            # re-constitute key
            log(2, "re-constituting SSLCrypto key")
            key = SSLCrypto.key(b64dec(keyAsTxt))
            log(2, "SSLCrypto key reconstituted fine")
    
            # save all in database
            listAddr = owner.dbAddList(listName,
                                       cryptoKey=key,
                                       sskPub=sskPub,
                                       sskPriv=sskPriv,
                                       insertPeriod=insertPeriod,
                                       refreshInProgress=1,
                                       )
    
            log(2, "new list for %s added to database" % listName)
    
            # create node object
            try:
                insertHtl = int(insertHtl)
            except:
                addproblem("HTL must be a number from 0 to 40")
                page.add(center(big(b("Sorry")),
                                "There were problems with your request:",
                                ulFromList(problems)))
                problems = []
                cmd = 'generatekey' # drop thru to prev state
    
            if not problems:
                node = self.owner.Node(htl=insertHtl)
    
                # stick the listsite in
                try:
                    log(2, "inserting new list %s into freenet (entropy)" % listName)
                    owner.fcpRefreshListsite(listAddr, htl=insertHtl)
                    log(2, "insert of new list %s successful" % listName)
                except:
                    addproblem("Failed to insert into Freenet/Entropy the info for your new mailing list:\n%s\n%s" % (
                        listAddr, exceptionString()))
                    log(2, "New list insert problem:\n%s" % exceptionString())
    
                if problems:
                    page.add(center(big(b("Sorry")),
                                    "There were problems with your request:",
                                    ulFromList(problems)))
                    problems = []
                    cmd = 'generatekey' # drop thru to prev state
                else:
                    # everything went fine
                    page.add(center(b("Success"), br(), br(),
                                    "Your new freemail mailing list was successfully created and inserted).",
                                    br(), br(),
                                    "Your new FreeMail List address is:", br(),
                                    b(listAddr), br(), br(),
                                    "and it has been inserted into Freenet (or Entropy) as:", br(),
                                    b(owner.db.lists[listAddr].listUriSite+"//"), br(), br(),
    
                                    b("One last step:"), br(),
                                    "Please now create a new mailing account in your normal "
                                    "email program", br(),
                                    small(i("(Mozilla, Evolution, Mutt, Pine, Kmail, Outlook Express etc)")), br(),
                                    "and set it up with the following details:", br(), br(),
    
                                    table(attr(align='center', cellspacing=0, cellpadding=3, border=1),
                                          tr(td(attr(align='center', colspan=2),
                                                b("General Settings:"))),
                                          tr(td(b("Name: ")),
                                             td("%s at FreeMail" % idName)),
                                          tr(td(b("Email Address: ")),
                                             td(attr(valign='top'), idAddr)),
                                          tr(td(attr(align='center', colspan=2),
                                                b("Receive Settings:"))),
                                          tr(td(attr(valign='top'),
                                                b("POP3 Server:")),
                                             td(attr(valign='top'), host)),
                                          tr(td(b("POP3 Port: ")),
                                             td(attr(valign='top'), config.popPort)),
                                          tr(td(b("POP3 Username: ")),
                                             td(attr(valign='top'), idAddr)),
                                          tr(td(b("POP3 Password: ")),
                                             td(attr(valign='top'), pop3Password)),
                                          tr(td(attr(align='center', colspan=2),
                                                b("Send Settings:"))),
                                          tr(td(b("SMTP Server: ")),
                                             td(attr(valign='top'), host)),
                                          tr(td(b("SMTP Port: ")),
                                             td(attr(valign='top'), config.smtpPort)),
                                          ),
                                    ))
    
        if cmd == 'generatekey':
    
            # Generate a crypto key of required size, notify of insertion
            try:
                keySize = int(keySize)
            except:
                try:
                    log(2,
                        "Strange problem - exception determining int(keySize)\n"
                        "keySize='%s'\n"
                        "defaulting to 3072-bit\n"
                        "%s" % (
                             keySize, exceptionString()))
                except:
                    log(2,
                        "Even stranger - the local var 'keySize' has gone AWOL!!!\n"
                        "Defaulting to 3072-bit\n"
                        "%s" % exceptionString())
                    keySize = 3072
            key = SSLCrypto.key(keySize, 'ElGamal')
            keyAsTxt = b64enc(key.exportKeyPrivate())
    
            page.add(center(form(attr(method="POST", action="/"),
                                 input(type='hidden', name='showpage', value='createList'),
                                 input(type='hidden', name='cmd', value='insertlist'),
                                 input(type='hidden', name='listName', value=listName),
                                 input(type='hidden', name='sskPub', value=sskPub),
                                 input(type='hidden', name='sskPriv', value=sskPriv),
                                 input(type='hidden', name='keyAsTxt', value=keyAsTxt),
    
                                 b("Insert List into Freenet"), br(),
                                 "Your new mailing list is ready to be inserted into Freenet (or Entropy)", br(),
                                 "Please select a HTL (hops-to-live) parameter for this insertion.", br(),
                                 small(i("(Note - the higher this value, the longer it will take now to "
                                         "complete the insertion - but the easier it will be for other "
                                         "FreeMail servers to locate your list.", br(),
                                         "Feel free to use a low value (even 0) if you are presently testing FreeMail.)",
                                         )), br(), br(),
    
                                 table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                       tr(td(b("Insert HTL")),
                                          td(input(type='text', name='insertHtl', value="15"))),
                                       ),
                                       
                                 btn("Insert into Freenet and Continue"), br(),
                                 small(i("(Please click this only once)")),
    
                                 )))
    
        if cmd == 'promptkeysize':
    
            # validate the SSK keypair
            node = owner.Node(htl=0)
            try:
                pub = node.put("test", "", "SSK@%s/xxxtest-%s" % (sskPriv, random.randint(0, 1000000))).uri.hash
                if pub != sskPub:
                    addproblem("The public and private keys do not match")
            except:
                addproblem("Cannot contact node to validate your keypair")
                self.log(1, "Failed to contact node:\n%s" % exceptionString())
    
            if problems:
                page.add(center(big(b("Sorry")),
                                "There were problems with your request:",
                                ulFromList(problems)))
                problems = []
                cmd = 'genssk' # drop thru to prev state
            else:
                page.add(center(form(attr(method="POST", action="/"),
                                     input(type='hidden', name='showpage', value='createIdentity'),
                                     input(type='hidden', name='cmd', value='promptkeysize'),
                                     input(type='hidden', name='idName', value=idName),
                                     input(type='hidden', name='sskPub', value=sskPub),
                                     input(type='hidden', name='sskPriv', value=sskPriv),
                                     
                                     b("Secure your POP3 Mailbox"), br(), br(),
                                     "Please choose a password, that your mail program will use "
                                     "for retrieving your FreeMail messages via the POP3 port.", br(),br(),
                                     
                                     table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                           tr(td(b("POP3 Password: ")),
                                              td(input(type="text", name="pop3Password", value=pop3Password, size=16))),
                                           tr(td(b("Confirm POP3 Password: ")),
                                              td(input(type="text", name="pop3Password1", value=pop3Password1, size=16))),
                                           ),
                                     btn("Continue"), br(),
    
                                     ),
                                ),
                         )
            
        if cmd == 'genssk':
    
            # validate chosen name
            lenListName = len(listName)
            if listName == '':
                addproblem("You did not enter a name")
            elif lenListName < 3:
                addproblem("Name is too short")
            if lenListName > 16:
                addproblem("Name is too long")
            if not re.match("^[a-zA-Z0-9\\-]+$", idName):
                addproblem("Name should only consist of letters, digits or hyphens")
    
            if problems:
                page.add(center(big(b("Sorry")),
                                "There were problems with your request:",
                                ulFromList(problems)))
                cmd = 'new' # drop thru to prev state
    
            else:
                # all ok - stick up ssk keys form
                page.add(center(form(attr(method="POST", action="/"),
                                     input(type='hidden', name='showpage', value='createList'),
                                     input(type='hidden', name='cmd', value='promptkeysize'),
                                     input(type='hidden', name='idName', value=listName),
    
                                     b("Secure your mailing list"), br(), br(),
                                     "In order to protect your mailing list against tampering "
                                     "attacks, we will need to secure it with a freenet SSK keypair.",
                                     br(),
                                     small(i("If you have an existing SSK keypair ",
                                             "(eg, one which you are using with an freesite), "
                                             "please enter it here.", br(),
                                             "Otherwise, please select 'Generate New Keys' to "
                                             "generate a whole new keypair.")), br(), br(),
    
                                     table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                           tr(td(attr(colspan=2, align='center'),
                                                 label(input(type='checkbox', name='genssk', value='yes'),
                                                       "Generate new SSK keys"
                                                       ))),
                                           tr(td(b("SSK Public Key: ")),
                                              td(input(type="text", name="sskPub", value=sskPub, size=40))),
                                           tr(td(b("SSK Private Key: ")),
                                              td(input(type="text", name="sskPriv", value=sskPriv, size=40))),
                                           tr(td(attr(colspan=2, align='center'),
                                                 center(btn("Continue")))),
                                           )))),
                                           
    
        if cmd == 'new':
            # first stage - prompt for name
            page.add(center(form(attr(method="POST", action="/"),
                                 input(type='hidden', name='showpage', value='createList'),
                                 input(type='hidden', name='cmd', value='genssk'),
    
                                 b("Please choose a short name"), br(),
                                 small(i("(3-16 chars - letters, digits and '-' only)")),
    
                                 table(attr(cellspacing=0, cellpadding=3, align='center', border=1),
                                       tr(td(input(type="text", name="listName", value=listName, size=18, maxlen=16)),
                                          ),
                                       ),
                                 center(btn("Continue")),
                                 ),
                            ),
                     )
    
        return
    
    #@-node:page_createList
    #@+node:page_manageInbound
    def page_manageInbound(self):
    
        #set_trace()
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
    
        page.add(center(h3("Manage Inbound Messages"), br(),
                        "not implemented yet",
                        ))
        page.add(center(btnForm("Return to Main Page", showpage="main")))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:page_manageInbound
    #@+node:page_manageOutbound
    def page_manageOutbound(self):
    
        #set_trace()
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        page.add(center(h3("Manage Outbound Messages"), br(),
                        "not implemented yet",
                        ))
        page.add(center(btnForm("Return to Main Page", showpage="main")))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:page_manageOutbound
    #@+node:page_template
    def page_(self):
    
        #set_trace()
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        page.add(center(h3("Page")))
        page.add(btnForm("Return to Main Page", showpage="main"))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:page_template
    #@+node:envDump
    def envDump(self):
        """
        Produces an HTTP environment dump for debugging purposes
        """
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        t = table(attr(cellspacing=0, cellpadding=3, border=1),
                  tr(td(attr(colspan=2, align='left'),
                        b("HTTP Environment"))),
                  tr(td(b("Name")), td(b("Value"))))
        
        for k,v in os.environ.items():
            t.add(tr(td(k), td(v)))
            
        tf = table(attr(cellspacing=0, cellpadding=3, border=1),
                   tr(td(attr(colspan=2, align='center'),
                         b("HTTP fields"))),
                   tr(td(b("Name")), td(b("Value"))))
        for k in page.session.fields.keys():
            v = page.session.fields[k]
            tf.add(tr(td(k), td(v)))
        
        page.add(t)
        page.add(tf)
    #@-node:envDump
    #@+node:isLoggedIn
    def isLoggedIn(self):
    
        """
        Returns 1 if user is logged in (has a non-stale cookie, or has given valid username/password
        """
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        # are we logged in?
        now = time.time()
        logged_in = 0
        if session.data.has_key('last_hit'):
            last_hit = float(session.data['last_hit'])
            elapsed = now - last_hit
            if elapsed <= config.adminTimeout:
                session.data['last_hit'] = now
                logged_in = 1
            else:
                del session.data['last_hit']
    
        # no cookie - look for fields
        if session.env['REQUEST_METHOD'] == 'POST' \
               and fields.has_key('adminLogin') \
               and fields.has_key('adminPassword'):
            login = fields['adminLogin']
            password = fields['adminPassword']
            # print "trying to log in with login '%s', password '%s'" % (login, password)
            if (login == config.adminLogin) \
            and ((config.adminPassword == '') or hash(password) == config.adminPassword):
                session.data['last_hit'] = now
                logged_in = 1
    
        if logged_in and req.path.endswith('/logout'):
            logged_in = 0
            del session.data['last_hit']
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
        return logged_in
    #@-node:isLoggedIn
    #@+node:showLoginPage
    def showLoginPage(self):
    
        """
        Displays a login form
        """
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        page.add(center(h3("Freemail Administration Web Interface"),
                        ))
    
        f = table(attr(align='center', cellspacing=0, cellpadding=2, border=0),
                  form(attr(method="POST", action="/"),
                       tr(td(attr(colspan=2, align='center'),
                             big(b("Please Log In")))),
                       tr(td(attr(align='right'),
                             b("Username:")),
                          td(attr(align='left'),
                             input(type='text', name='adminLogin', value=''))),
                       tr(td(attr(align='right'),
                             b("Password:")),
                          td(attr(align='left'),
                             input(type='password', name='adminPassword', value=''))),
                       tr(td(attr(colspan=2, align='center'),
                             btn("Login")),
                       )))
        f1 = table(attr(align='center', cellspacing=0, cellpadding=0, width='100%', height='50%'),
                   tr(td(attr(align='center', valign='middle'),
                         f)))
        page.add(f1)
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:showLoginPage
    #@+node:wizard
    def wizard(self, state):
    
        method = getattr(self, "wizard_"+state)
        method()
    #@-node:wizard
    #@+node:wizardSetState
    def wizardSetState(self, state):
        """
        Changes the wizard's state to 'state', and displays
        the next pane.
        """
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        # save state in database
        self.config.configState = state
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
        
        # wipe out fields
        for fld in self.fields.keys():
            self.fields[fld] = ''
        
        # display next pane, or main if finished
        if state == 'ready':
            self.page_main()
        else:
            getattr(self, 'wizard_'+state)()
    #@-node:wizardSetState
    #@+node:wizardNextButton
    def wizardNextButton(self, label='Next'):
        return notag(btn(label.capitalize()),
                     input(type='hidden', name='pressednext', value=1))
    #@-node:wizardNextButton
    #@+node:wizard_new
    def wizard_new(self):
        """
        Presents first pane of setup wizard
        """
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        nextstate = 'security'
    
        if fields['pressednext']:
            self.wizardSetState(nextstate)
    
            self._dbUnlock()
            # -^-^-^-^-^-^- UNLOCK DATABASE --------------
            return
                                
        page.add(center(h1("Welcome to Freemail"),
                        "This wizard will walk you through the process of "
                        "configuring your Freemail installation.",
                        br(), br(),
                        "Please click 'Next' to continue", br(), br(),
                        )
                 )
        
        page.add(center(form(attr(method='post', action='/'),
                             self.wizardNextButton(),
                             )
                        )
                 )
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:wizard_new
    #@+node:wizard_security
    def wizard_security(self):
        """
        Prompts for username/password
        """
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        username = fields['username'] or config.adminLogin
        password = fields['password']
        password1 = fields['password1']
    
        nextstate = 'fcp'
    
        errors = ''
    
        if fields['pressednext']:
            # validate username/password
            problems = []
            if username == '':
                problems.append("You must enter a username")
            if password == '':
                problems.append("You must enter a password")
            elif password != password1:
                problems.append("Your passwords do not match")
            elif len(password) < 6:
                problems.append("Your password is too short")
            
            if problems:
                errors = notag("Sorry, but there were errors:", ulFromList(problems))
            else:
                # write new values to database
                config.adminLogin = username
                config.adminPassword = hash(password)
                owner.dbSave()
    
                # and move on
                self.wizardSetState(nextstate)
    
                self._dbUnlock()
                # -^-^-^-^-^-^- UNLOCK DATABASE --------------
                return
    
        page.add(center(h2("Freemail Setup: Web Interface Security"),
                        errors))
    
        # display entry form
        page.add(center(form(attr(action="/", method="POST"),
                             table(attr(cellspacing=0, cellpadding=5),
                                   tr(td(attr(colspan=2, align='center'),
                                         h3("Please choose a username and password"),
                                         "(For logging into this web interface once you're set up)",
                                         )),
                                   tr(td(attr(align='right'),
                                         b("Username: ")),
                                      td(input(type='text', name='username', size=12, value=username))),
                                   tr(td(attr(align='right'),
                                         b("Password: ")),
                                      td(input(type='password', name='password', size=12, value=password))),
                                   tr(td(attr(align='right'),
                                         b("Confirm Password: ")),
                                      td(input(type='password', name='password1', size=12, value=password1))),
                                   tr(td(attr(align='center', colspan=2),
                                         small(i("(password must be at least 6 characters)")))),
                                   ),
                             self.wizardNextButton(),
                             )))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:wizard_security
    #@+node:wizard_fcp
    def wizard_fcp(self):
        """
        Configures the node FCP settings
        """
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
        log = owner.log
    
        errors = ''
    
        nextstate = 'servers'
    
        #print "fields: ", repr(fields)
        fcpHost = fields['fcpHost'] or config.fcpHost
        #print "fcpPort = '%s'" % fields['fcpPort']
    
        fcpPort = int(config.fcpPort)
    
        if fields['pressednext']: # user has submitted form
    
            # validate FCP host/port
            problems = []
    
            fcpPort = fields['fcpPort']
            try:
                fcpPort = int(fcpPort)
            except:
                pass
            #print "fcpPort = '%s'" % fcpPort
    
            # validate fields
            if fcpHost == '':
                problems.append("You must specify an FCP host")
            if not fcpPort:
                problems.append("You must specify an FCP port")
            elif type(fcpPort) is not type(0):
                problems.append("FCP Port must be a number")
            elif fcpPort < 1 or fcpPort > 65535:
                problems.append("Invalid FCP Port '%s' - should be between 1 and 65535" % fcpPort)
    
            #print problems
    
            # try a handshake if ok so far
            if not problems:
                try:
                    n = freenet.node(fcpHost, fcpPort)
                    n._handshake()
                except freenet.FreenetFcpConnectError:
                    problems.append("Can't connect to '%s:%s'" % (fcpHost, fcpPort))
                except freenet.FreenetFcpError:
                    problems.append("Connected to '%s:%s', but it's not talking FCP" % (fcpHost, fcpPort))
                except:
                    log(2, "Exception talking to FCP port at %s:%s\n%s" % (fcpHost, fcpPort, exceptionString()))
                    problems.append("Some weird problem talking to FCP port at '%s:%s'" % (fcpHost, fcpPort))
    
            if problems:
                errors = notag("Sorry, but there were errors:", ulFromList(problems))
            else:
                # write new values to database
                config.fcpHost = fcpHost
                config.fcpPort = fcpPort
                owner.dbSave()
    
                # and move on
                self.wizardSetState(nextstate)
    
                self._dbUnlock()
                # -^-^-^-^-^-^- UNLOCK DATABASE --------------
                return
    
        page.add(center(h2("Freemail Setup: Freenet/Entropy Node Interface Settings"),
                        errors))
    
        # display entry form
        page.add(center(form(attr(action="/", method="POST"),
                             table(attr(cellspacing=0, cellpadding=5),
                                   tr(td(attr(colspan=2, align='center'),
                                         h3("Please choose a Freenet or Entropy FCP interface"))),
                                   tr(td(attr(align='right'),
                                         b("FCP Hostname (or IP): ")),
                                      td(input(type='text', name='fcpHost', size=20, value=fcpHost))),
                                   tr(td(attr(align='right'),
                                         b("FCP Port: ")),
                                      td(input(type='text', name='fcpPort', size=12, value=fcpPort))),
                                   tr(td(attr(align='center', colspan=2),
                                         b("Note - we will test this interface, so please ensure "
                                           "you have a node running"))),
                                   ),
                             input(type='hidden', name='test', value='1'),
                             self.wizardNextButton(),
                             )))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:wizard_fcp
    #@+node:wizard_servers
    def wizard_servers(self):
        """
        Enter listening ports for servers
        """
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
    
        errors = ''
    
        nextstate = 'misc'
    
        httpPort = config.httpPort
        popPort = config.popPort
        smtpPort = config.smtpPort
        telnetPort = config.telnetPort
    
        httpHosts = owner.db.httpHosts
        popHosts = owner.db.popHosts
        smtpHosts = owner.db.smtpHosts
        telnetHosts = owner.db.telnetHosts
    
    
        if fields['pressednext']: # user has submitted form
    
            # validate server ports
            problems = []
    
            httpPort = fields['httpPort']
            try:
                httpPort = int(httpPort)
            except:
                pass
            popPort = fields['popPort']
            try:
                popPort = int(popPort)
            except:
                pass
            smtpPort = fields['smtpPort']
            try:
                smtpPort = int(smtpPort)
            except:
                pass
            telnetPort = fields['telnetPort']
            try:
                telnetPort = int(telnetPort)
            except:
                pass
    
            httpHosts = [h.strip() for h in fields['httpHosts'].split(",")]
            smtpHosts = [h.strip() for h in fields['smtpHosts'].split(",")]
            popHosts = [h.strip() for h in fields['popHosts'].split(",")]
            telnetHosts = [h.strip() for h in fields['telnetHosts'].split(",")]
    
            # validate fields
            if not httpPort:
                problems.append("You must specify an HTTP port")
            elif type(httpPort) is not type(0):
                problems.append("HTTP Port must be a number")
            elif httpPort < 1 or httpPort > 65535:
                problems.append("Invalid HTTP Port '%s' - should be between 1 and 65535" % httpPort)
    
            if not popPort:
                problems.append("You must specify a POP3 port")
            elif type(popPort) is not type(0):
                problems.append("POP3 Port must be a number")
            elif popPort < 1 or popPort > 65535:
                problems.append("Invalid POP3 Port '%s' - should be between 1 and 65535" % popPort)
    
            if not smtpPort:
                problems.append("You must specify an SMTP port")
            elif type(smtpPort) is not type(0):
                problems.append("SMTP Port must be a number")
            elif smtpPort < 1 or smtpPort > 65535:
                problems.append("Invalid SMTP Port '%s' - should be between 1 and 65535" % smtpPort)
    
            if not telnetPort:
                problems.append("You must specify a Telnet port")
            elif type(telnetPort) is not type(0):
                problems.append("Telnet Port must be a number")
            elif telnetPort < 1 or telnetPort > 65535:
                problems.append("Invalid Telnet Port '%s' - should be between 1 and 65535" % telnetPort)
    
            if problems:
                errors = notag("Sorry, but there were errors:", ulFromList(problems))
            else:
                # write new values to database
                config.httpPort = httpPort
                config.popPort = popPort
                config.smtpPort = smtpPort
                config.telnetPort = telnetPort
    
                # now the allowed hosts
                db.httpHosts = httpHosts
                db.smtpHosts = smtpHosts
                db.popHosts = popHosts
                db.telnetHosts = telnetHosts
    
                owner.dbSave()
    
                # launch the mailservers
                owner.startMailServers()
                owner.startTelnetServer()
    
                # and move on
                self.wizardSetState(nextstate)
    
                self._dbUnlock()
                # -^-^-^-^-^-^- UNLOCK DATABASE --------------
                return
    
        page.add(center(h2("Freemail Setup: User Interface Port Settings"),
                        errors))
    
        # display entry form
        page.add(center(form(attr(action="/", method="POST"),
                             table(attr(cellspacing=0, cellpadding=5, border=0, width='70%'),
                                   tr(td(attr(colspan=3, align='center'),
                                         h3("Please choose ports for the Freemail user interface servers")),
                                      ),
    
                                   tr(td(attr(align='left'),
                                         b("HTTP Port: "), br(),
                                         ),
                                      td(input(type='text', name='httpPort', size=12, value=httpPort)),
                                      td(small(i("(For setting up and managing Freemail "
                                                 "via your web browser, "
                                                 "just like you're doing now)")),
                                         )
                                      ),
                                   tr(td(b("Allowed HTTP hosts"), br(),
                                         small(i("(comma-separated)")),
                                         ),
                                      td(input(type="text", name="httpHosts", size=32, maxlen=80, value=",".join(httpHosts))),
                                         td(small(i("(List of hostnames or IP addresses from which connections to the FreeMail "
                                                    "web (HTTP) interface are accepted.)"))),
                                      ),
                                   tr(td(attr(colspan=3), hr())),
    
                                   tr(td(attr(align='left'),
                                         b("POP3 Port: "), br(),
                                         ),
                                      td(input(type='text', name='popPort', size=12, value=popPort)),
                                      td(small(i("(for receiving incoming messages "
                                                 "from Freemail into your email client)")),
                                         )
                                      ),
                                   tr(td(b("Allowed POP3 hosts"), br(),
                                         small(i("(comma-separated)")),
                                         ),
                                      td(input(type="text", name="popHosts", size=32, maxlen=80, value=",".join(popHosts))),
                                      td(small(i("(List of hostnames or IP addresses from which connections to the FreeMail "
                                                 "inbound mail (POP3) interface are accepted.)"))),
                                      ),
                                   tr(td(attr(colspan=3), hr())),
    
                                   tr(td(attr(align='left'),
                                         b("SMTP Port: "), br(),
                                         ),
                                      td(input(type='text', name='smtpPort', size=12, value=smtpPort)),
                                      td(small(i("(for sending messages via Freemail "
                                                 "from your email client)")),
                                         )
                                      ),
                                   tr(td(b("Allowed SMTP hosts"), br(),
                                         small(i("(comma-separated)")),
                                         ),
                                      td(input(type="text", name="smtpHosts", size=32, maxlen=80, value=",".join(smtpHosts))),
                                         td(small(i("(List of hostnames or IP addresses from which connections to the FreeMail "
                                                 "outbound mail (SMTP) interface are accepted.)"))),
                                      ),
                                   tr(td(attr(colspan=3), hr())),
    
                                   tr(td(attr(align='left'),
                                         b("Telnet Port: "), br(),
                                         ),
                                      td(input(type='text', name='telnetPort', size=12, value=telnetPort)),
                                      td(small(i("(Provides a telnet interface to the pythonic console)")),
                                         )
                                      ),
                                   tr(td(b("Allowed Telnet hosts"), br(),
                                         small(i("(comma-separated)")),
                                         ),
                                      td(input(type="text", name="telnetHosts", size=32, maxlen=80, value=",".join(telnetHosts))),
                                      td(small(i("(List of hostnames or IP addresses from which connections to the FreeMail "
                                                 "telnet console interface are accepted.)"))),
                                      ),
                                   tr(td(attr(colspan=3), hr())),
    
                                   tr(td(attr(align='left', colspan=3),
                                         b("Notes:"),
                                         ul(li("Port values less than 1025 will not work, "
                                               "unless you're running Freemail as root"),
                                            li("Changes to the HTTP port won't take effect "
                                               "until you restart Freemail"),
                                            li("If you fill in valid values for all ports, "
                                               "the Freemail POP3, SMTP and Telnet servers will "
                                               "start up when you click 'Next'"),
                                            ),
                                         )),
                                   ),
                             self.wizardNextButton(),
                             )))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@nonl
    #@-node:wizard_servers
    #@+node:wizard_misc
    def wizard_misc(self):
        """
        Enter miscellaneous config info
        """
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        errors = ''
    
        nextstate = 'finish'
    
        adminTimeout = config.adminTimeout
        htlSend = config.htlSend
        htlReceive = config.htlReceive
    
        receiptAll = config.receiptAll
        if receiptAll:
            tagReceiptAll = '<input type="checkbox" name="receiptAll" value="yes" CHECKED>'
        else:
            tagReceiptAll = '<input type="checkbox" name="receiptAll" value="yes">'
    
        storeDir = config.storeDir
        rxMaxRetries = config.rxMaxRetries
        txMaxRetries = config.txMaxRetries
        cryptoKeySize = config.cryptoKeySize
        slotLookAhead = config.slotLookAhead
        txBackoffInit = config.txBackoffInit
        txBackoffMult = config.txBackoffMult
    
        if fields['pressednext']: # user has submitted form
    
            # validate server ports
            problems = []
    
            adminTimeout = fields['adminTimeout']
            try:
                adminTimeout = int(adminTimeout)
            except:
                pass
    
            htlSend = fields['htlSend']
            try:
                htlSend = int(htlSend)
            except:
                pass
            htlReceive = fields['htlReceive']
            try:
                htlReceive = int(htlReceive)
            except:
                pass
    
            receiptAll = (fields['receiptAll'] == 'yes')
            if receiptAll:
                tagReceiptAll = '<input type="checkbox" name="receiptAll" value="yes" CHECKED>'
            else:
                tagReceiptAll = '<input type="checkbox" name="receiptAll" value="yes">'
            
            storeDir = fields['storeDir']
    
            rxMaxRetries = fields['rxMaxRetries']
            try:
                rxMaxRetries = int(rxMaxRetries)
            except:
                pass
    
            txMaxRetries = fields['txMaxRetries']
            try:
                txMaxRetries = int(txMaxRetries)
            except:
                pass
    
            cryptoKeySize = fields['cryptoKeySize']
            try:
                cryptoKeySize = int(cryptoKeySize)
            except:
                pass
    
            maxWorkerThreads = fields['maxWorkerThreads']
            try:
                maxWorkerThreads = int(maxWorkerThreads)
            except:
                pass
    
            slotLookAhead = fields['slotLookAhead']
            try:
                slotLookAhead = int(slotLookAhead)
            except:
                pass
            txBackoffInit = fields['txBackoffInit']
            try:
                txBackoffInit = int(txBackoffInit)
            except:
                pass
            txBackoffMult = fields['txBackoffMult']
            try:
                txBackoffMult = float(txBackoffMult)
            except:
                pass
    
            # validate fields
    
            if not adminTimeout:
                problems.append("You must specify an admin timeout")
            elif type(adminTimeout) is not type(0):
                problems.append("Admin timeout must be a number")
            elif adminTimeout < 60:
                problems.append("Dumb value for admin timeout - "
                                "please try at least 60 (recommend 3600)")
    
            if htlSend != 0 and not htlSend:
                problems.append("You must specify an HTL value for sending")
            elif type(htlSend) is not type(0):
                problems.append("HTL TX value must be a number")
            elif htlSend  > 100:
                problems.append("Dumb HTL TX value '%s' - try something like 20" % htlSend)
    
            if htlReceive != 0 and not htlReceive:
                problems.append("You must specify an HTL value for receiving")
            elif type(htlReceive) is not type(0):
                problems.append("HTL RX value must be a number")
            elif htlReceive  > 100:
                problems.append("Dumb HTL RX value '%s' - try something like 20" % htlReceive)
    
            if not storeDir:
                problems.append("You must choose a datastore directory")
            else:
                if os.path.isdir(storeDir):
                    # try to set permissions
                    try:
                        os.chmod(storeDir, 0700)
                    except:
                        problems("Failed to set permissions for store directory '%s' "
                                 "please choose another path" % storeDir)
                else:
                    # try to create store directory now
                    try:
                        os.makedirs(storeDir, 0700)
                    except:
                        problems.append("Failed to create store directory '%s' "
                                        "please try something else" % storeDir)
    
            if not rxMaxRetries:
                problems.append("You must specify a maximum receive retry count")
            elif type(rxMaxRetries) is not type(0):
                problems.append("receive retry count should be a number")
            elif rxMaxRetries not in range(1, 20):
                problems.append("Invalid receive retry limit '%s' - "
                                "please choose something between 1 and 20" % rxMaxRetries)
    
            if not txMaxRetries:
                problems.append("You must specify a maximum transmit retry count")
            elif type(txMaxRetries) is not type(0):
                problems.append("transmit retry count should be a number")
            elif txMaxRetries not in range(1, 20):
                problems.append("Invalid transmit retry limit '%s' - "
                                "please choose something between 1 and 20" % txMaxRetries)
    
            if not cryptoKeySize:
                problems.append("You must specify an encryption key size")
            elif type(cryptoKeySize) is not type(1):
                problems.append("Encryption key size should be a number")
            elif cryptoKeySize not in [1024, 2048, 3072, 4096]:
                problems.append("Crazy key size '%s' - "
                                "please choose 1024,2048,3072 or 4096 - (recommend 2048 or higher)" % cryptoKeySize)
    
            if not slotLookAhead:
                problems.append("You must specify an initial rx slot look-ahead count")
            elif type(slotLookAhead) is not type(1):
                problems.append("RX Slot look-ahead should be a number, best 2-10")
            elif slotLookAhead < 1 or slotLookAhead > 10:
                problems.append("Crazy RX slot look-ahead '%s' - "
                                "please choose something between 1 and 10 (recommend 5)" % slotLookAhead)
    
            if not txBackoffInit:
                problems.append("You must specify an initial send backoff interval")
            elif type(txBackoffInit) is not type(1):
                problems.append("Initial send backoff interval should be a number, best 7200 or more")
            elif txBackoffInit < 300 or txBackoffInit > 86400:
                problems.append("Crazy initial send backoff interval '%s' - "
                                "please choose something between 1800 and 86400" % txBackoffInit)
    
            if not txBackoffMult:
                problems.append("You must specify an rx slot backoff multiplier")
            elif type(txBackoffMult) not in [type(1), type(1.1)]:
                problems.append("Send backoff multiplier should be a number from 1.1 to 4.0")
            elif txBackoffMult < 1.1 or txBackoffMult > 4.0:
                problems.append("Crazy send backoff multiplier '%s' - "
                                "please choose something between 1.1 and 4.0" % txBackoffMult)
    
            if problems:
                errors = notag("Sorry, but there were errors:", ulFromList(problems))
            else:
                # write new values to database
                config.adminTimeout = adminTimeout
                config.htlSend = htlSend
                config.htlReceive = htlReceive
                config.receiptAll = receiptAll
                config.storeDir = storeDir
                config.rxMaxRetries = rxMaxRetries
                config.txMaxRetries = txMaxRetries
                config.cryptoKeySize = cryptoKeySize
                config.slotLookAhead = slotLookAhead
                config.txBackoffInit = txBackoffInit
                config.txBackoffMult = txBackoffMult
                owner.dbSave()
    
                # and move on
                self.wizardSetState(nextstate)
    
                self._dbUnlock()
                # -^-^-^-^-^-^- UNLOCK DATABASE --------------
                return
    
        page.add(center(h2("Freemail Setup: Miscellaneous Settings"),
                        errors))
    
        page.add(center(small(i("(If there are any of these settings you don't understand, "
                                "just go with the default values)"))))
        # display entry form
        page.add(center(form(attr(action="/", method="POST"),
                             table(attr(cellspacing=0, cellpadding=5),
                                   tr(td(attr(colspan=3, align='center'),
                                         h3("Please now review some miscellaneous settings"))),
    
                                   tr(td(attr(align='right'),
                                         b("Admin Timeout (seconds): "),
                                         ),
                                      td(input(type='text', name='adminTimeout', size=20, value=adminTimeout)),
                                      td(small(i("(Security - if you exceed this time between "
                                                 "accesses to the web interface, you'll have to "
                                                 "log in again.)"
                                                 )),
                                         )
                                      ),
    
                                   tr(td(attr(align='right'),
                                         b("HTL for Sending: "),
                                         ),
                                      td(input(type='text', name='htlSend', size=3, value=htlSend)),
                                      td(small(i("(hops-to-live - specifies how deeply the "
                                                 "messages you send penetrate the network. If too low, "
                                                 "people won't be able to receive your messages. "
                                                 "If too high, your Freemail node will run too slow. "
                                                 "I'd recommend something between 15 and 40.")),
                                         )
                                      ),
    
                                   tr(td(attr(align='right'),
                                         b("HTL for Receiving: "),
                                         ),
                                      td(input(type='text', name='htlReceive', size=3, value=htlReceive)),
                                      td(small(i("(hops-to-live - specifies how deeply Freemail "
                                                 "probes within the network to extract messages "
                                                 "sent to you. "
                                                 "If too low, you won't receive messages sent to you. "
                                                 "If too high, your Freemail node will run too slow. "
                                                 "I'd recommend something between 15 and 40.")),
                                         )
                                      ),
    
                                   tr(td(b("Issue receipts for all sent messages?")),
                                      td(tagReceiptAll),
                                      td(small(i("(Normally, when messages are successfully delivered, "
                                                 "you won't receive anything - only if a message is not "
                                                 "delivered will a message be written to the sending "
                                                 "identity's POP mailbox. But if you enable this option, "
                                                 "the sending identity will receive a yes/no confirmation "
                                                 "for all outbound messages, regardless of outcome.")),
                                         ),
                                      ),
    
                                   tr(td(attr(align='right'),
                                         b("Store Directory: "),
                                         ),
                                      td(input(type='text', name='storeDir', size=20, value=storeDir)),
                                      td(small(i("(Relative or absolute directory path for "
                                                 "storing your inbound and outbound message bodies. "
                                                 "If this path doesn't exist, this wizard will try to "
                                                 "create it and set its permissions.)")),
                                         )
                                      ),
    
                                   tr(td(attr(align='right'),
                                         b("Receive Retry Count: "),
                                         ),
                                      td(input(type='text', name='rxMaxRetries', size=6, value=rxMaxRetries)),
                                      td(small(i("Number of times to retry retrievals. Recommend around 2-8.)")),
                                         )
                                      ),
    
                                   tr(td(attr(align='right'),
                                         b("Transmission Retry Count: "),
                                         ),
                                      td(input(type='text', name='txMaxRetries', size=6, value=txMaxRetries)),
                                      td(small(i("Number of times to retry sends. Recommend around 2-8.)")),
                                         )
                                      ),
    
                                   tr(td(attr(align='right'),
                                         b("Encryption Key Size: "),
                                         ),
                                      td('',
                                         #input(type='text', name='cryptoKeySize', size=6, value=cryptoKeySize),
                                         '<select name="cryptoKeySize">'
                                         '<option label="1024 bits (low grade)" value="1024">1024 bits (low grade)</option>'
                                         '<option label="2048 bits (personal grade)", value="2048" selected>2048 bits (personal grade)</option>'
                                         '<option label="3072 bits (commercial grade)", value="3072">3072 bits (commercial grade)</option>'
                                         '<option label="4096 bits (military grade)", value="4096">4096 bits (military grade)</option>'
                                         "</select>",
                                         ),
                                      td(small(i("Size of encryption key for receiving messages. Recommend 2048 or above.)")),
                                         )
                                      ),
    
                                   tr(td(attr(align='right'),
                                         b("Receive Slot Look-Ahead: "),
                                         ),
                                      td(input(type='text', name='slotLookAhead', size=5, value=slotLookAhead)),
                                      td(small(i("(used for retrieving incoming session and mail messages. "
                                                 "This specifies how many receive slots to 'look ahead' "
                                                 "when polling for incoming messages. "
                                                 "Valid values are between 1 and 10 (suggest 5)")),
                                         )
                                      ),
    
                                   tr(td(attr(align='right'),
                                         b("Initial Send Retry Backoff Interval: "), br(),
                                         small(i("(seconds)"))
                                         ),
                                      td(input(type='text', name='txBackoffInit', size=5, value=txBackoffInit)),
                                      td(small(i("(used for retrying message sends. "
                                                 "This is the <b>initial</b> time in seconds to wait between send retries. "
                                                 "Valid values are between 1800 (30 mins) and 86400 (1 day))")),
                                         )
                                      ),
    
                                   tr(td(attr(align='right'),
                                         b("Send Retry Backoff Multiplier: "), br(),
                                         small(i("(float)"))
                                         ),
                                      td(input(type='text', name='txBackoffMult', size=5, value=txBackoffMult)),
                                      td(small(i("(used for exponential backoff/retry of message re-sends. "
                                                 "This value gets multiplied by the backoff interval each time a message "
                                                 "send fails. "
                                                 "Eg, if 1.5, and init value is 3600, then Freemail will retry the send after "
                                                 "1 hour, then 1.5 hours, then 2.25 hours, 3.375 hours and so on."
                                                 "Valid values are between 1.1 and 4.0, recommend 2.0)")),
                                         )
                                      ),
    
                                   tr(td(attr(colspan=3, align='center'),
                                         self.wizardNextButton())),
                                   )
                             )))
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    #@-node:wizard_misc
    #@+node:wizard_finish
    def wizard_finish(self):
        """
        Completion of setup
        """
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req
    
        owner = self.owner
        db = owner.db
        config = db.config
    
        errors = ''
    
        nextstate = 'ready'
    
        page.add(center(h2("Freemail Setup: Completion")))
    
        page.add(center(h3("Congratulations!"),
                        p("Your Freemail setup is now complete."),
                        p("Freemail is now talking to a live Freenet (or Entropy) node, "
                          "and has launched its POP3 and SMTP mail servers."),
                        p("All you need to do now is log in, and create one or more "
                          "mailing 'identities' - aliases under which you send messages "
                          "to people. When you log in, just click on <b>Manage Identities</b>"),
                        p("Once you have an identity set up, you'll be able to start "
                          "sending and receiving emails with an unprecedented level of privacy."),
    
                        form(attr(action="/", method="POST"),
                             table(attr(align='center', cellspacing=0, cellpadding=4, border=0),
                                   tr(td(self.wizardNextButton())))),
                        ))
    
        # no validation needed here
        config.configState = nextstate
    
        self.owner.dbSave()
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    #@-node:wizard_finish
    #@-others
#@-node:class WebUI
#@+node:class HTTPServer
class HTTPServer(SocketServer.ThreadingMixIn, BaseHTTPServer.HTTPServer):
#class HTTPServer(BaseHTTPServer.HTTPServer):

    #@    @+others
    #@+node:attribs
    request_queue_size = 256
    #@-node:attribs
    #@+node:__init__
    def __init__(self, **kw):
        """
        Creates a HTTP server.
        
        No arguments.
        
        Keywords:
         - owner - an object passed in by whoever owns this server - compulsory;
           note that this is available as the 'owner' attribute within
           request handler objects.
         - WebUI - a callable that handles incoming hits.
         - log - a function for logging which accepts 2 arguments:
             - a log level - 1=critical, 2=important, 3=trivial, 4=debug
         - port - default 8000
         - greeting - a text line to print to console when server fires up
         - handlerClass - a class constructor for a POP Request Handler
         - allowedHosts - list of allowed hosts
        """
    
        allowedHosts = kw.get('allowedHosts', None)
        if allowedHosts:
            bindAddr = ''
        else:
            bindAddr = '127.0.0.1'
            allowedHosts = ['127.0.0.1']
        port = kw.get('port', 8000)
    
        self.name = kw.get('name', "David's hacked-up HTTP server")
        self.log = kw['log']
        self.webUI = kw['webUI']
        self.owner = kw['owner']
        self.bindAddr = bindAddr
        self.port = port
        self.handlerClass = kw.get('handlerClass', HTTPRequestHandler)
        self.allowedHosts = allowedHosts
    
        self.request_queue_size = 256
        self.daemon_thread = False
    
        #set_trace()
    
        BaseHTTPServer.HTTPServer.__init__(self, (bindAddr, port), self.handlerClass)
    #@-node:__init__
    #@+node:run
    def run(self):
    
        print "** "+self.name+" now listening on port %s" % self.port
        self.serve_forever()
    
    
    #@-node:run
    #@-others
#@-node:class HTTPServer
#@+node:class HTTPRequestHandler
class HTTPRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    #@    @+others
    #@+node:__init__
    def __init__(self, request, client_address, server):
    
        self.server_version = "FreemailHTTP/" + version
    
        self.extensions_map = mimetypes.types_map.copy()
        self.extensions_map.update({
            '': 'application/octet-stream', # Default
            '.py': 'text/plain',
            '.c': 'text/plain',
            '.h': 'text/plain',
            })
        
        # Determine platform specifics
        self.have_fork = hasattr(os, 'fork')
        self.have_popen2 = hasattr(os, 'popen2')
        self.have_popen3 = hasattr(os, 'popen3')
        
        # Make rfile unbuffered -- we need to read one line and then pass
        # the rest to a subprocess, so we can't use buffered input.
        self.rbufsize = 0
        
        self.cgi_directories = ['/cgi-bin', '/htbin']
        
        self.nobody = None
    
    
       
        self.server = server
        self.client_address = client_address
        self.request = request
        self.owner = server.owner
        self.log = server.log
        
        BaseHTTPServer.BaseHTTPRequestHandler.__init__(self, request, client_address, server)
    #@nonl
    #@-node:__init__
    #@+node:do_GET
    def do_GET(self):
        """Serve a GET request."""
        self.run_cgi()
    #@-node:do_GET
    #@+node:do_POST
    def do_POST(self):
        """Serve a POST request.
    
        This is only implemented for CGI scripts.
    
        """
        self.run_cgi()
    #@-node:do_POST
    #@+node:do_HEAD
    def do_HEAD(self):
        """Serve a HEAD request."""
        self.run_cgi()
    #@-node:do_HEAD
    #@+node:run_cgi
    def run_cgi(self):
        """Execute a CGI script."""
    
        splitpath = os.path.split(self.path)
        #print "self.path='%s'" % self.path
        #print "splitted='%s'" % str(splitpath)
        dir, rest = splitpath
    
        i = rest.rfind('?')
        if i >= 0:
            rest, query = rest[:i], rest[i+1:]
        else:
            query = ''
    
        #print "rest='%s' query='%s'" % (rest, query)
    
        i = rest.find('/')
        if i >= 0:
            script, rest = rest[:i], rest[i:]
        else:
            script, rest = rest, ''
        #self.log(4, "self.path='%s'" % self.path)
        scriptname = dir + '/' + script
        #self.log(4, "scriptname='%s'" % scriptname)
    
        # I don't think we need this
        #scriptfile = self.translate_path(scriptname)
        #self.log(4, "scriptfile='%s'" % scriptfile)
    
        #self.log(4, "ATTEMPT: '%s" % self.translate_path(self.path))
        #self.log(4, "ATT: '%s" % urllib.unquote(self.path))
    
        if scriptname.startswith("//"):
            scriptname = scriptname[1:]
        #self.log(4, "scriptname='%s'" % scriptname)
    
        scriptname = urllib.unquote(self.path).split("?", 1)[0]
    
        # Reference: http://hoohoo.ncsa.uiuc.edu/cgi/env.html
        # XXX Much of the following could be prepared ahead of time!
        env = {}
        env['SERVER_SOFTWARE'] = self.version_string()
        env['SERVER_NAME'] = self.server.server_name
        env['GATEWAY_INTERFACE'] = 'CGI/1.1'
        env['SERVER_PROTOCOL'] = self.protocol_version
        env['SERVER_PORT'] = str(self.server.server_port)
        env['REQUEST_METHOD'] = self.command
        uqrest = urllib.unquote(rest)
        env['PATH_INFO'] = uqrest
        env['PATH_TRANSLATED'] = self.translate_path(uqrest)
        env['SCRIPT_NAME'] = scriptname
        if query:
            #print "query='%s'" % query
            env['QUERY_STRING'] = query
        host = self.address_string()
        if host != self.client_address[0]:
            env['REMOTE_HOST'] = host
        env['REMOTE_ADDR'] = self.client_address[0]
        # XXX AUTH_TYPE
        # XXX REMOTE_USER
        # XXX REMOTE_IDENT
        if self.headers.typeheader is None:
            env['CONTENT_TYPE'] = self.headers.type
        else:
            env['CONTENT_TYPE'] = self.headers.typeheader
        length = self.headers.getheader('content-length')
        if length:
            env['CONTENT_LENGTH'] = length
        accept = []
        for line in self.headers.getallmatchingheaders('accept'):
            if line[:1] in "\t\n\r ":
                accept.append(line.strip())
            else:
                accept = accept + line[7:].split(',')
        env['HTTP_ACCEPT'] = ','.join(accept)
        ua = self.headers.getheader('user-agent')
        if ua:
            env['HTTP_USER_AGENT'] = ua
        co = filter(None, self.headers.getheaders('cookie'))
        if co:
            env['HTTP_COOKIE'] = ', '.join(co)
        # XXX Other HTTP_* headers
        if not self.have_fork:
            # Since we're setting the env in the parent, provide empty
            # values to override previously set values
            for k in ('QUERY_STRING', 'REMOTE_HOST', 'CONTENT_LENGTH',
                      'HTTP_USER_AGENT', 'HTTP_COOKIE'):
                env.setdefault(k, "")
    
        # swap to our environment and launch script
        #oldenviron = os.environ
        #os.environ = env
        os.environ.update(env)
    
        # prevent unauthorised remote access
        if env['REMOTE_ADDR'] not in self.server.allowedHosts:
            self.log(1, "run_cgi: Rejecting REMOTE_ADDR '%s'" % env['REMOTE_ADDR'])
            self.send_error(403, "You cannot access this site from your host")
            return
    
        self.send_response(200, "Script output follows")
        try:
            # pass to the http ui generator
            self.server.webUI(self)
        except:
            self.server.handle_error(self.request, self.client_address)
        #os.environ = oldenviron
    
    #@-node:run_cgi
    #@+node:translate_path
    def translate_path(self, path):
        """Translate a /-separated PATH to the local filename syntax.
    
        Components that mean special things to the local file system
        (e.g. drive or directory names) are ignored.  (XXX They should
        probably be diagnosed.)
    
        """
        path = posixpath.normpath(urllib.unquote(path))
        words = path.split('/')
        words = filter(None, words)
        path = os.getcwd()
        for word in words:
            drive, word = os.path.splitdrive(word)
            head, word = os.path.split(word)
            if word in (os.curdir, os.pardir): continue
            path = os.path.join(path, word)
        return path
    
    #@-node:translate_path
    #@+node:log_message
    def log_message(self, format, *args):
        """
        
        """
        self.log(3, format%args)
    
    #@-node:log_message
    #@-others

#@-node:class HTTPRequestHandler
#@+node:class POPserver
class POPserver:
    """
    Don't run more than one instance of this in a single process.
    """
    #@    @+others
    #@+node:__init__
    def __init__(self, **kw):
        """
        Creates a POP server.
        
        No arguments.
        
        Keywords:
         - freemail - a freemailServer object - compulsory
         - bindaddr - TCP bind address, default '127.0.0.1'
         - port - default 110
         - handlerClass - a class constructor for a POP Request Handler
        """
        self.freemail = kw['freemail']
        self.bindaddr = kw.get('bindaddr', '')
        self.port = kw.get('port', 110)
        self.handlerClass = kw['handlerClass']
    
    #@-node:__init__
    #@+node:exitNormal
    def exitNormal(self, signum, frame):
        sys.exit(0)
    #@-node:exitNormal
    #@+node:run
    def run(self):
    
        #try:
        #    signal.signal(signal.SIGTERM, self.exitNormal)
        #    signal.signal(signal.SIGABRT, self.exitNormal)
        #except:
        #    print "Can't hook signals on this platform"
    
        while 1:
            try:
                self.server = SocketServer.ThreadingTCPServer(
                    (self.bindaddr, self.port),
                    self.handlerClass,
                    )
                break
            except:
                print "Can't bind to POP3 socket %s - waiting to retry..." % self.port
                time.sleep(10)
    
        self.server.socket.setsockopt(
            socket.SOL_SOCKET,
            socket.SO_REUSEADDR,
            1)
    
        self.server.freemail = self.freemail
    
        print "** Freemail POP3 server now listening on port %d" % self.port
    
        while 1:
            self.server.handle_request()
    
    #@-node:run
    #@-others
#@-node:class POPserver
#@+node:class POPRequestHandler
class POPRequestHandler(SocketServer.StreamRequestHandler):
    #@    @+others
    #@+node:__init__
    def __init__(self, sock, addr, server, **kw):
        
        #self.freemail = kw['freemail']
        #del kw['freemail']
        self.freemail = server.freemail
        self.log = self.freemail.log
        self.remotePeer = addr[0]
    
        #print "POPRequestHandler: args='%s' kw='%s'" % (str(args), str(kw))
    
        self.log(3, "POPRequestHandler: addr=%s" % str(addr))
        SocketServer.StreamRequestHandler.__init__(self, sock, addr, server, **kw)
    
    
    
    #@-node:__init__
    #@+node:attributes
    COMMANDS = ("QUIT", "STAT", "LIST", "RETR", "DELE", "NOOP",
                "RSET", "TOP", "UIDL", "USER", "PASS")
    #@-node:attributes
    #@+node:handle
    def handle(self):
    
        if self.remotePeer not in self.freemail.db.popHosts:
            self.server.close_request(self.request)
            return
    
        wf = self.request.makefile('wb')
        session = POPSession(wf, self.freemail)
        session.write_ok("POP3 server ready")
        wf.flush()
    
        #print "POPRequestHandler: self='%s'" % str(self)
        #print "POPRequestHandler: freemail='%s'" % str(self.freemail)
    
        while 1:
            try:
                line = self.request.recv(2048)
                if line == '':
                    wf.close()
                    self.server.close_request(self.request)
                    return
    
                self.freemail.log(5, "POPRequestHandler: line='%s'" % line.strip())
                comm = line.split()
                if not comm:
                    continue
                (cmd, args) = (comm[0].upper(), comm[1:])
                if cmd not in self.COMMANDS:
                    raise Error, "Unknown command"
                method = getattr(session, cmd, None)
                if method is None:
                    raise Error, "Unsupported command"
                method(*args)
            except (TypeError, Error), msg:
                wf.write("-ERR %s\r\n" % str(msg))
                if not isinstance(msg, Error):
                    self.log(2, exceptionString())
                #self.log(2, "-ERR " + str(msg))
            wf.flush()
            if cmd == "QUIT":
                wf.close()
                self.server.close_request(self.request)
                return
    
    
    #@-node:handle
    #@-others
#@-node:class POPRequestHandler
#@+node:class POPSession

class POPSession:
    #@    @+others
    #@+node:attributes
    # states
    Q_AUTHORIZATION_USER = "AUTHORIZATION (USER)"
    Q_AUTHORIZATION_PASS = "AUTHORIZATION (PASS)"
    Q_TRANSACTION = "TRANSACTION"
    
    #@-node:attributes
    #@+node:__init__
    def __init__(self, wf, freemail):
        self.write = wf.write
        self.state = POPSession.Q_AUTHORIZATION_USER
        self.username = None
        self.freemail = freemail
        self.log = freemail.log
    
    #@-node:__init__
    #@+node:write_ok
    def write_ok(self, msg):            # sends an OK response.
        self.log(5, "+OK " + str(msg))
        self.write("+OK %s\r\n" % msg)
    
    #@-node:write_ok
    #@+node:write_multi
    def write_multi(self, lines):       # write "byte-stuffed" lines.
        for s in lines:
            if s[:1] == ".":            # termination octet?
                s = "." + s
            self.write(s + "\r\n")
    #@-node:write_multi
    #@+node:write_raw
    def write_raw(self,str):
        #print str
        self.write(str)
    #@-node:write_raw
    #@+node:__check_state
    def __check_state(self, STATE):
        if self.state is not STATE:
            raise Error, "Wrong state " + self.state
    #@-node:__check_state
    #@+node:CAPA
    def CAPA(self):
        self.write_ok("Good bye...")
    #@-node:CAPA
    #@+node:QUIT
    def QUIT(self):
        if self.state is POPSession.Q_TRANSACTION:
            self.maildrop.update()
        self.write_ok("Good bye...")
    #@-node:QUIT
    #@+node:USER
    def USER(self, username):
        self.__check_state(POPSession.Q_AUTHORIZATION_USER)
        if not self.freemail.check_user(username):
            raise Error, "Unknown user %s" % username
        self.username = username
        self.state = POPSession.Q_AUTHORIZATION_PASS
        self.write_ok("User %s accepted" % username)
    #@-node:USER
    #@+node:PASS
    def PASS(self, password):
        self.__check_state(POPSession.Q_AUTHORIZATION_PASS)
    
        if self.freemail.auth(self.username, password) == "":
            self.state = POPSession.Q_AUTHORIZATION_USER
            POP3Logger("Failure to login as " + self.username, loglevel=1)
            raise Error, "Invalid password"
    
        self.username = self.freemail.check_user(self.username)
    
        self.maildrop = POPMaildrop(self)
        self.state = POPSession.Q_TRANSACTION
        POP3Logger("Successful login as " + self.username, loglevel=1)
        self.write_ok("Password accepted")
    #@-node:PASS
    #@+node:STAT
    def STAT(self):
    
        self.__check_state(POPSession.Q_TRANSACTION)
    
        self.write_ok("%d %d" % (self.maildrop.msgcount,
                                 self.maildrop.mailboxsize)
                      )
    #@-node:STAT
    #@+node:LIST
    def LIST(self, msgno=None):
    
        self.__check_state(POPSession.Q_TRANSACTION)
    
        self.maildrop.update()
        mails = self.maildrop.mails
    
        self.log(5, "msgno = '%s'" % msgno)
    
        #print "MAILS:"
        #print mails
    
        self.log(5, "got LIST for user %s" % self.username)
            
        if msgno is None:
            self.write_ok("scan listing begins")
            count = 0
            for (status, size, msg) in mails:
                count += 1
                line = "%d %d" % (count, size)
                self.write(line + "\r\n")
                #mydebug( line)
            self.write(".\r\n")
        else:
            try:
                msg = mails[_get_no(msgno)][2]
                self.write_ok("%s %s" % (msgno, msg.msgHash))
            except:
                self.log(2, "Exception retrieving mail list:\nmsgno=%s\nmsg=%s\n%s" % (msgno, msg, exceptionString()))
                raise
    
    #@-node:LIST
    #@+node:RETR
    def RETR(self, msgno):
    
        self.__check_state(POPSession.Q_TRANSACTION)
    
        msg = self.maildrop.get_msg(_get_no(msgno))
        #print "STATUS:*%s*" % self.maildrop.get_msg_status(_get_no(msgno))
        #print "MSG:\r\n",msg
        #lines = msg.split("\r\n")
        self.write_ok("Sending message")
        self.write_raw(msg)
        self.write_raw("\r\n.\r\n")
    #@-node:RETR
    #@+node:DELE
    def DELE(self, msgno):
    
        self.__check_state(POPSession.Q_TRANSACTION)
        self.maildrop.delete_msg(_get_no(msgno))
        self.write_ok("Message deleted")
    #@-node:DELE
    #@+node:NOOP
    def NOOP(self):
    
        self.__check_state(POPSession.Q_TRANSACTION)
        self.write_ok("Still here...")
    #@-node:NOOP
    #@+node:RSET
    def RSET(self):
    
        self.__check_state(POPSession.Q_TRANSACTION)
        self.maildrop.reset()
        self.write_ok("Messages unmarked")
    #@nonl
    #@-node:RSET
    #@+node:TOP
    def TOP(self, msgno, n):
    
        self.__check_state(POPSession.Q_TRANSACTION)
        try:
            n = int(n)
        except ValueError:
            raise Error, "Not a number"
        msg = self.maildrop.get_msg(_get_no(msgno))
        [head, body] = msg.split("\r\n\r\n", 1)
        self.write_ok("Top of message follows")
        self.write_multi(head.split("\r\n"))
        self.write("\r\n")
        self.write_multi(body.split("\r\n")[:n])
        self.write(".\r\n")
    #@-node:TOP
    #@+node:UIDL
    def UIDL(self, msgno=None):
    
        self.__check_state(POPSession.Q_TRANSACTION)
        if msgno is None:
            self.write_ok("UIDL listing begins") 
            count = 0           
            for (status, size, msg) in self.maildrop.mails:
                count += 1
                line = "%d %s" % (count, _digest(str(msg)))
                self.write(line + "\r\n")
                mydebug( line)
            self.write(".\r\n")
        else:
            msg = self.maildrop.get_msg(_get_no(msgno))
            self.write_ok("%s %s" % (msgno, _digest(msg)))
    #@-node:UIDL
    #@-others
#@-node:class POPSession
#@+node:class POPMaildrop
class POPMaildrop:
    #@    @+others
    #@+node:__init__
    def __init__(self, popsess):
    
        freemail = popsess.freemail
        self.username = popsess.username
        self.freemail = freemail
        #self.maildrop = self
        self.db = freemail.db
        db = self.db
        self.log = freemail.log
    
        self._dbLock = freemail._dbLock
        self._dbUnlock = freemail._dbUnlock
    
        self.update()
    
        return
    #@-node:__init__
    #@+node:get_stat
    def get_stat(self):
        return self.msgcount, self.mailboxsize
    #@-node:get_stat
    #@+node:get_msg
    def get_msg(self,msgno):
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        freemail = self.freemail
        db = freemail.db
        config = db.config
    
        msgpath = os.path.join(config.storeDir, "rx", self.mails[msgno][2].msgHash)
        fd = open(msgpath, "rb")
        msg = fd.read()
        fd.close()
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
        return msg
    
        # old stuff from pop3 maildrop
        msgfile = open(self.maildir+"/"+self.mails[msgno][2], "r")
        msg = msgfile.read()
        msgfile.close()
        
        freemail.log(4, "*** Body of message follows...")
        freemail.log(4, msg)
    
        return msg
    
    #@-node:get_msg
    #@+node:delete_msg
    def delete_msg(self, msgno):
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        try:
            db = self.db
    
            self.log(5, "Want to delete msg:\nmsgno=%s\nidAddr=%s\nself.mails=%s" % (
                            msgno, self.username, self.mails))
    
            msg = self.mails[msgno][2]
            msg.isDeleted = 1
        
            self.log(4, "Marking message %d as deleted" % msgno)
        
            #del self.mails[msgno]
        
        except:
            self.log(2, "Error deleting msg:\nmsgno=%s\nidAddr=%s\nself.mails=%s\n%s" % (
                            msgno, self.username, self.mails, exceptionString()))
    
        self.freemail.dbSave()
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
        return
    #@nonl
    #@-node:delete_msg
    #@+node:update
    def update(self):
    
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()
    
        freemail = self.freemail
        db = self.db
        config = db.config
    
        # get a view of messages targetted at user
        vMsgs = db.rxMessages.select(idAddr=self.username, isDeleted=0)
    
        self.mails = []
        msgcount = 0
        totalsize = 0
        for rMsg in vMsgs:
            if not rMsg.isDeleted:
                itemsize = rMsg.msgLen
                totalsize += itemsize
                msgcount += 1
                self.mails.append( (0, itemsize, rMsg) )
        self.msgcount = msgcount 
        self.mailboxsize = totalsize
    
        freemail.log(5, "POPMaildrop: storeDir='%s'" % config.storeDir)
        self.maildir = os.path.join(config.storeDir, "rx")
    
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    
    #@-node:update
    #@+node:reset
    def reset(self):
        pass
    #@-node:reset
    #@-others
#@-node:class POPMaildrop
#@+node:POP3Logger
def POP3Logger(message, loglevel=3):
    return
    print "pop3: %s" % message
#@-node:POP3Logger
#@+node:mydebug
def mydebug(msg):
    print msg
#@-node:mydebug
#@+node:class Error
class Error (Exception):
    pass
#@-node:class Error
#@+node:_digest
def _digest(msg):
    i = msg.find("\r\nMessage-")
    if i >= 0:
        if msg[i+10: i+14].upper() == "ID: ":
            j = msg.find("\r\n", i+14)
            if j >= 0:
                msgid = msg[i+14: j]
                if len(msgid) > 10:
                    return md5.new(msgid).hexdigest()
    return md5.new(msg).hexdigest()
#@-node:_digest
#@+node:_get_no
def _get_no(msgno):
    try:
        return int(msgno) - 1
    except ValueError:
        raise Error, "Not a number"
#@-node:_get_no
#@+node:class SMTPServer
class SMTPServer:
    """
    A single threaded SMTP Server connection manager. Listens for
    incoming SMTP connections on a given port. For each connection,
    the SMTPSession is chugged, passing the given instance of
    SMTPServerInterface. 
    """
    #@    @+others
    #@+node:__init__
    def __init__(self, **kw):
        
        allowedHosts = kw.get('allowedHosts', None)
        if allowedHosts:
            bindAddr = ''
        else:
            bindAddr = '127.0.0.1'
            allowedHosts = ['127.0.0.1']
        self.allowedHosts = allowedHosts
    
        port = kw.get('port', 25)
        self.port = port
    
        self.freemail = kw['freemail']
    
        self._socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        #self._socket.bind((socket.INADDR_ANY, port))
    
        self._socket.bind((bindAddr, port))
        self._socket.listen(5)
    
        # do a lookup on all the local domains
        localIPs = [socket.gethostbyname(domain) for domain in allowedHosts]
        if '127.0.0.1' not in localIPs:
            localIPs.append('127.0.0.1')
    
        self.localIPs = localIPs
    
    #@-node:__init__
    #@+node:run
    def run(self):
    
        print "** Freemail SMTP server now listening on port %d" % self.port
    
        while 1:
            try:
                nsd = self._socket.accept()
            except socket.timeout:
                continue
    
            # check if host is on 'allowd hosts' list
            remaddr = nsd[1][0]
            if remaddr not in self.freemail.db.smtpHosts:
                self.freemail.log(1, "Terminating connection from unauthorised host at %s" % remaddr)
                nsd[0].close()
                continue
    
            session = SMTPSession(nsd[0], nsd[1], self.freemail, self.allowedHosts)
            thread.start_new_thread(session.chug, ())
            #session.chug()
    #@-node:run
    #@-others
#@-node:class SMTPServer
#@+node:class SMTPSession



#
# This drives the state for a single RFC821 message.
#
class SMTPSession:
    #@	<< class SMTPSession declarations >>
    #@+node:<< class SMTPSession declarations >>
    """
    Server engine that calls methods on the SMTPServerInterface object
    passed at construction time. It is constructed with a bound socket
    connection to a client. The 'chug' method drives the state,
    returning when the client RFC821 transaction is complete. 
    """
    
    ST_INIT = 0
    ST_HELO = 1
    ST_MAIL = 2
    ST_RCPT = 3
    ST_DATA = 4
    ST_QUIT = 5
    
    #@-node:<< class SMTPSession declarations >>
    #@nl
    #@    @+others
    #@+node:__init__
    def __init__(self, socket, addr, freemail, allowedIPs):
        # initialisations from old engine class
        self.tid = getNewTid()
        self.socket = socket;
        self.addr = addr[0]
        self.freemail = freemail
        self.db = freemail.db
        self._dbLock = freemail._dbLock
        self._dbUnlock = freemail._dbUnlock
        self.log = freemail.log
        self.state = SMTPSession.ST_INIT
        self.allowedIPs = allowedIPs
    
        # initialisations from 'implementation' class
        self.remaddr = addr[0]
        self.sock = socket
        self.savedTo = []
        self.toAddrs = []
        self.savedHelo = ''
        self.savedMailFrom = ''
        #self.savedData = ''
        self.shutdown = 0
        self.isrbl = 0
        
        self.log(3, "SMTPSession.__init__: pid=%s, remaddr=%s" % (os.getpid(), self.addr))
    
        # determine if remote MUA/MTA is running on local machine
        self.senderIsLocal = (1 in [fnmatch.fnmatch(self.addr, patt) for patt in allowedIPs])
    
        if self.senderIsLocal:
            self.log(4, "Sender at %s is local" % self.addr)
        else:
            self.log(4, "Sender at %s is REMOTE" % self.addr)
    #@-node:__init__
    #@+node:chug
    def chug(self):
        """
        Chug the engine, till QUIT is received from the client. As
        each RFC821 message is received, calls are made on the
        SMTPServerInterface methods on the object passed at
        construction time.
        """
        self.logStatus(4, "Got connection from '%s'" % self.addr)
    
        self.socket.send("220 Welcome to the Freemail SMTP Server\r\n")
        try:
            while 1:
                self.socket.setblocking(0)
                try:
                    data = self.socket.recv(1024);
                    self.socket.setblocking(1)
                except:
                    self.logStatus(5, "waiting for data on socket")
                    time.sleep(1)
                    continue
    
                #print "GOT DATA: '%s'" % data
    
                if len(data):
                    if self.state != SMTPSession.ST_DATA:
                        rsp, keep = self.doCommand(data)
                        self.socket.send(rsp + "\r\n")
                    else:
                        rsp = self.doData(data)
                        if rsp == None:
                            continue
        
                        self.logStatus(5, "message is not spam")
                        self.socket.send("250 OK - Data and terminator. found\r\n")
        
                    if keep == 0:
                        self.socket.close()
                        break
                else:
                    break;
    
            self.logStatus(4, "Session ended")
            #raise Exception("Testing exception logging")
            return
        except:
            self.logException(self.addr)
            self.logStatus(4, "Session ended after crash")
    
    #@-node:chug
    #@+node:doCommand
    def doCommand(self, data):
    
        """Process a single SMTP Command"""
        cmd = data[0:4]
        cmd = string.upper(cmd)
        self.keep = 1
        rv = None
        if cmd == "HELO":
            self.state = SMTPSession.ST_HELO
            rv = self.helo(data)
        elif cmd == "RSET":
            rv = self.reset(data)
            self.dataAccum = ""
            if self.state != SMTPSession.ST_HELO:
                self.state = SMTPSession.ST_INIT
        elif cmd == "NOOP":
            pass
        elif cmd == "QUIT":
            rv = self.quit(data)
            self.keep = 0
        elif cmd == "MAIL":
            if self.state != SMTPSession.ST_HELO:
                return ("503 Bad command sequence", 1)
            self.state = SMTPSession.ST_MAIL
            rv = self.mailFrom(data)
        elif cmd == "RCPT":
            if (self.state != SMTPSession.ST_MAIL) and (self.state != SMTPSession.ST_RCPT):
                return ("503 Bad command sequence", 1)
            self.state = SMTPSession.ST_RCPT
            rv = self.rcptTo(data)
        elif cmd == "DATA":
            if self.state != SMTPSession.ST_RCPT:
                return ("503 Bad command sequence", 1)
            self.state = SMTPSession.ST_DATA
            self.dataAccum = ""
            return ("354 OK, Enter data, terminated with a \\r\\n.\\r\\n", 1)
        else:
            return ("505 Eh? WTF was that?", 1)
    
        if rv:
            return (rv, self.keep)
        else:
            return("250 OK", self.keep)
    #@-node:doCommand
    #@+node:doData
    def doData(self, data):
        """
        Process SMTP Data. Accumulates client DATA until the
        terminator is found.
        """
        self.dataAccum = self.dataAccum + data
        if len(self.dataAccum) > 4 and self.dataAccum[-5:] == '\r\n.\r\n':
            self.dataAccum = self.dataAccum[:-5]
            rv = self.data(self.dataAccum)
            self.state = SMTPSession.ST_HELO
            if rv:
                return rv
            else:
                return "250 OK - Data and terminator. found"
        else:
            return None
    #@-node:doData
    #@+node:logStatus
    def logStatus(self, level, msg):
        msg = "%s: %s" % (self.tid, msg)
        self.freemail.log(level, msg)
    #@-node:logStatus
    #@+node:logException
    def logException(self, addr):
        fd = StringIO("%s:" % self.tid)
        fd.write("Thread crash!!\n")
        fd.write("Addr = '%s'\n" % addr)
        traceback.print_exc(file=fd)
        self.freemail.log(1, fd.getvalue())
    #@-node:logException
    #@+node:helo
    def helo(self, args):
        self.savedHelo = args
        self.logStatus(3, "SMTPService.helo: got '%s'" % args.strip())
    
        addr = self.remaddr
    
    #@-node:helo
    #@+node:mailFrom
    def mailFrom(self, args):
        # Stash who its from for later
        self.logStatus(3, "SMTPService.mailFrom: got '%s'" % args.strip())
        self.MAIL = args
        try:
            self.savedMailFrom = stripAddress(args)
            self.log(4, "from: '%s'" % self.savedMailFrom)
        except:
            self.savedMailFrom = args
            self.log(4, "stripAddress failed with '%s'" % args)
    
        # check the FROM user is known to us
        if self.freemail.db.identities.has_key(self.savedMailFrom):
            return "250 OK got valid MAIL FROM address"
        else:
            self.keep = 0
            return "553 Bad 'FROM:' address '%s'" % self.savedMailFrom
    
    #@-node:mailFrom
    #@+node:rcptTo
    def rcptTo(self, args):
    
        # Stashes multiple RCPT TO: addresses
        self.logStatus(3, "SMTPService.rcptTo: got '%s'" % args.strip())
    
        # save the command verbatim for later passing to MTA
        self.savedTo.append(args)
    
        # extract the target email address
        try:
            toAddr = stripAddress(args)
        except:
            self.logStatus(2, "failed to get 'To' address from '%s'" % args.strip())
            self.keep = 0
            return "553 Invalid RCPT syntax: '%s'" % args
    
        # parse recipient address
        addrBits = toAddr.split("@", 1)
        if len(addrBits) < 2:
            addrBits.append("127.0.0.1")
        toUser, toDomain = addrBits[0], addrBits[1]
    
        # barf on invalid address format
        if not toUser:
            self.logStatus(4, "Invalid recipient address '%s'" % toAddr)
            self.keep = 0
            return "553 Invalid recipient address '%s'" % toAddr
        
        # attempt lookup on recipient domain
        try:
            toIP = socket.gethostbyname(toDomain)
        except:
            toIP = ''
    
        # barf if lookup failed
        if 0 and not toIP:
            self.logStatus(4, "DNS lookup on domain of '%s' failed" % toAddr)
            self.keep = 0
            return "553 Name lookup on domain '%s' failed" % toDomain
    
        # check that either recipient or sender is local
        if not (self.senderIsLocal or (toIP in localIPs)):
            # someone is trying to relay through us
            self.logStatus(4, "Peer at %s is trying to relay to %s" % (self.addr, toAddr))
            self.keep = 0
            return "553 Server not available for relaying"
    
        # all is ok
        self.logStatus(4, "Got valid recipient address: %s" % toAddr)
        self.toAddrs.append(toAddr)
        return "250 Recipient address OK"
    
    #@-node:rcptTo
    #@+node:data
    def data(self, args):
        # Process client mail data. It inserts a silly X-Header, then
        # does a MX DNS lookup for each TO address stashed by the
        # rcptTo method above. Messages are logged to the console as
        # things proceed. 
        self.logStatus(6, "SMTPService.data: got '%s'" % args)
    
        if args[-1] != '\n':
            self.logStatus(4, "Appending newline to data")
            args = args + "\n"
        self.rawData = args
    
        self.dispatchMessage()
    
        return "250 OK - Data and terminator. found"
        #return None
    #@-node:data
    #@+node:quit
    def quit(self, args):
        self.logStatus(5, "HELO: '%s'" % self.savedHelo)
        self.logStatus(5, "MAIL: '%s'" % self.savedMailFrom)
        self.logStatus(5, "RCPT: '%s'" % self.savedTo)
        try:
            self.logStatus(5, "DATA: '%s'" % self.savedData)
        except:
            self.logStatus(3, "QUIT: no saved data")
        self.logStatus(4, "Session with '%s' complete" % str(self.remaddr))
        self.savedTo = []
    #@-node:quit
    #@+node:reset
    def reset(self, args):
        self.logStatus(5, 'Received "RSET": "%s"' % args)
        pass
    
    #@-node:reset
    #@+node:dispatchMessage
    def dispatchMessage(self):
    
        self.logStatus(4, "GOT MESSAGE TO SEND!")
        self.logStatus(4, "From: '%s'" % self.savedMailFrom)
        self.logStatus(4, "To: '%s'" % ",".join(self.toAddrs))
        self.logStatus(4, "RAW DATA:\n%s" % self.rawData)
    
        for toAddr in self.toAddrs:
            self.freemail.enqueueMessage(self.savedMailFrom,
                                         toAddr,
                                         'message',
                                         self.rawData)
    #@-node:dispatchMessage
    #@-others
#@-node:class SMTPSession
#@+node:class TelnetServer
class TelnetServer:
    """
    Implements the FreeMail telnet interface
    """
    #@    @+others
    #@+node:__init__
    def __init__(self, **kw):
        """
        Creates a POP server.
        
        No arguments.
        
        Keywords:
         - freemail - a freemailServer object - compulsory
         - bindaddr - TCP bind address, default '127.0.0.1'
         - port - default 110
         - handlerClass - a class constructor for a Telnet Request Handler
        """
        self.freemail = kw['freemail']
        self.bindaddr = kw.get('bindaddr', '')
        self.port = kw.get('port', 10021)
        self.handlerClass = TelnetRequestHandler
    #@-node:__init__
    #@+node:exitNormal
    def exitNormal(self, signum, frame):
        sys.exit(0)
    #@-node:exitNormal
    #@+node:run
    def run(self):
    
        #try:
        #    signal.signal(signal.SIGTERM, self.exitNormal)
        #    signal.signal(signal.SIGABRT, self.exitNormal)
        #except:
        #    print "Can't hook signals on this platform"
    
        while 1:
            try:
                self.server = SocketServer.ThreadingTCPServer(
                    (self.bindaddr, self.port),
                    self.handlerClass,
                    )
                break
            except:
                print "Can't bind to telnet socket - port %s - waiting to retry..." % self.port
                time.sleep(10)
    
        self.server.socket.setsockopt(
            socket.SOL_SOCKET,
            socket.SO_REUSEADDR,
            1)
    
        self.server.freemail = self.freemail
        self.server.gotClient = 0
    
        print "** Freemail Telnet Server now listening on port %d" % self.port
    
        while 1:
            self.server.handle_request()
    #@nonl
    #@-node:run
    #@-others
#@-node:class TelnetServer
#@+node:class TelnetRequestHandler
class TelnetRequestHandler(SocketServer.StreamRequestHandler):
    #@    @+others
    #@+node:__init__
    def __init__(self, sock, addr, server, **kw):
    
        if server.gotClient:
            sock.send("You are logged in elsewhere!\r\n")
            server.close_request(server.request)
            return
        server.gotClient = 1
    
        self.freemail = server.freemail
        self.config = self.freemail.db.config
        self.log = self.freemail.log
        self.remotePeer = addr[0]
    
        #print "POPRequestHandler: args='%s' kw='%s'" % (str(args), str(kw))
    
        self.log(3, "POPRequestHandler: addr=%s" % str(addr))
        SocketServer.StreamRequestHandler.__init__(self, sock, addr, server, **kw)
    
    #@-node:__init__
    #@+node:handle
    def handle(self):
    
        wf = self.request.makefile('wb')
    
        if self.remotePeer not in self.freemail.db.telnetHosts:
            wf.write("You are not allowed to connect to FreeMail from this machine\n")
            self.server.close_request(self.request)
            self.server.gotClient = 0
            return
    
        wf.write("FreeMail Telnet Server ready\n")
        wf.flush()
    
        # wf.write
    
        config = self.config
    
        badlogins = 0
        loginState = 'login'
        while badlogins < 5:
            while 1:
                wf.write("Login: ")
                wf.flush()
                login = self.getline()
                if login != '':
                    break
            wf.write("Password: ")
            wf.flush()
            password = self.getline()
            if login.strip() != config.adminLogin or hash(password.strip()) != config.adminPassword:
                wf.write("Invalid login or password\n")
                badlogins += 1
            else:
                wf.write("You are now logged in\n")
                break
    
        if badlogins == 5:
            self.server.close_request(self.request)
    
        self.server.gotClient = 1
        sys.stdout = wf
    
        # get a console up
        console = freemailConsole(globals(), outfile=wf)
        
        while 1:
            try:
                wf.write(console.prmt)
                wf.flush()
                
                line = self.getline()
                if line == '':
                    wf.close()
                    break
    
                self.freemail.log(5, "TelnetRequestHandler: line='%s'" % line.strip())
                
                console.handle_line(line)
            except:
                self.freemail.log(5, "TelnetRequestHandler: exception:\n%s'" % exceptionString())
                break
    
        self.server.close_request(self.request)
        self.server.gotClient = 0
        sys.stdout = oldStdout
    #@-node:handle
    #@+node:getline
    def getline(self):
        """
        Reads one line of text from remote peer
        """
        chars = []
    
        while 1:
            char = self.request.recv(1)
            #print "char = '%s'" % repr(char)
            if char == '':
                break
            if char == '\n':
                break
            chars.append(char)
        line = "".join(chars)
        return line
    #@-node:getline
    #@-others
#@-node:class TelnetRequestHandler
#@+node:class freemailConsole
class freemailConsole(InteractiveConsole):

    helpDocs = {
'': """
FreeMail interactive console help.

Type 'help topic' to display detailed help on topic

Available topics:
  help commands verbosity identities reinsert objects
""",

'help': """
help

Synopsis:
    help [arg]
    
Displays help on one of the available help topics.
If arg is not one of these topics, the python help is invoked.
""",

'commands': """
FreeMail console commands summary:
    
    help - display help on topics
    commands - display a command summary
    verbosity - set/change logging verbosity level
    identities - display mailing identities and status
    reinsert - re-insert one or all mailing identities into freenet
    
""",

'verbosity': """
verbosity

Synopsis:
    verbosity [n]

Displays or sets the current logging verbosity level.
If no argument is given, displays the current verbosity.
If an arg is given, this becomes the new verbosity level.

Valid values for n are 0,...,5
""",

'identities': """
identities

Synopsis:
    identities

Displays a list of your currently active freemail identities, and
their reference numbers. (You need the reference number to operate
on any identity).
""",

'reinsert': """
reinsert

Synopsis:
    reinsert id-num
    reinsert all

If arg is a number, reinsert that identity into freenet. To see the
numbers corresponding to your identities, type 'identities'.

If arg is 'all', then schedule all current identities for reinsertion.
""",

'objects': """
Python objects available within this console:
    
    m - the currently running freemail server object
    db - the database
    conf - the configuration object (equiv to db.conf)
    
""",
}
                   
    def __init__(self, mylocals, **kw):

        self.hideSymbols(mylocals)
        InteractiveConsole.__init__(self, mylocals)

        self.outfile = kw.get('outfile', sys.stdout)
        self.setup()

    def hideSymbols(self, mylocals):
        for sym in mylocals.keys():
            if sym in ['help']:
                del mylocals[sym]
        
    def setup(self):

        self.ps1 = interactivePrompt
        self.ps2 = "   <cont> "
        self.prmt = self.ps1
        self.iscont = 0
        self.isrunning = 1
        
    def help(self, arg):
        
        if arg in self.helpDocs.keys():
            pydoc.pager(self.helpDocs[arg])
        else:
            self.push("help(%s)" % arg)

    def run(self):

        print
        print "************************************************************************"
        print "FreeMail (freenet mailserver) build %s" % build
        print "Interactive Python Console"
        print "FreeMail server object is 'm', database object is 'db', config object is 'conf'"
        print
        print "type 'dir(object) or help(object) to get help on object"
        print "type 'v(n) to set verbosity level (0 <= n <= 5)"
        print "************************************************************************"
        print

        while self.isrunning:
            try:
                line = self.raw_input(self.prmt)
            except EOFError:
                print
                break
            if self.handle_line(line) == 'quit':
                break

        if os.path.isfile("freemail.pid"):
            os.unlink("freemail.pid")
        print "Terminating FreeMail server..."

    def handle_line(self, line):
        #print "handle_line: line='%s'" % line
        if not self.iscont:
            res = self.handle_command(line)
            if res == 'quit':
                return 'quit'
            if res:
                cont = 1
                self.prmt = self.ps2
            else:
                cont = 0
                self.prmt = self.ps1
        else:
            if not self.push(line):
                cont = 0
                self.prmt = self.ps1
            else:
                cont = 1
                self.prmt = self.ps2
        
    def handle_command(self, line):

        #line = line.strip()
        try:
            if line[-1] == '\r':
                line = line[:-1]
        except:
            pass
        try:
            cmd, args = re.split("\\s+", line, 1)
        except:
            cmd = line
            args = ""

        if cmd == 'help':
            self.help(args)

        elif cmd == 'quit':
            return quit

        elif cmd == 'verbosity':
            if args:
                self.push("m.verbosity(%s)" % args)
            else:
                print "Current verbosity is: %s" % m._verbosity

        elif cmd == 'identities':
            idnames = m.db.identities.keys()
            idnames.sort()
            i = 0
            for id in idnames:
                print "%4s %s" % (i, id)
                rec = m.db.identities[id]
                if rec.idRefreshInProgress:
                    print "       Refresh currently in progress"
                else:
                    print "       Last refreshed: %s" % time.asctime(time.localtime(rec.idLastInserted))
                i += 1

        elif cmd == 'reinsert':
            if args == 'all':
                m.forceIdRefresh()
                print "Scheduled all identities for reinsertion"
            elif args == '':
                print "You must specify an identity to reinsert, or 'all'"
            else:
                idAddr = self.idByNum(args)
                if idAddr == None:
                    print "Invalid id '%s'" % args
                    print "Type 'identities' to see valid id numbers"
                else:
                    m.forceIdRefresh(idAddr)
                    print "Scheduled identity '%s' for reinsertion" % idAddr

        else:
            # execute anything else as a python command
            return self.push(line)

    def idByNum(self, n):
        try:
            idnames = m.db.identities.keys()
            idnames.sort()
            return idnames[int(n)]
        except:
            return None

    def write(self, buf):
        self.outfile.write(buf)
        self.outfile.flush()
#@-node:class freemailConsole
#@+node:class cell
class cell:
    
    """
    Generic storage object, a kind-of cross between an attributed object and a sequence,
    with some metakit-style methods
    """

    #@    @+others
    #@+node:__init__
    def __init__(self, *args, **kw):
        """
        Creates a cell object.
        
        The keyword 'defaults' is a dict of default values
        """
        self._val = None
        self._seq = []
        self._dict = {}
    
        if len(args) == 1:
            arg = args[0]
            if type(arg) in [type([]), type(())]:
                self._seq = list(arg)
            elif type(arg) is type({}):
                self._dict = arg
            else:
                self._val = arg
        else:
            self._seq = list(args)
    
        if kw.has_key('defaults'):
            dflts = kw['defaults']
            if type(dflts) is not type({}):
                raise Exception("Constructor keywords 'defaults' must be a dict")
            del kw['defaults']
            self.__dict__['_defaults'] = dflts
        else:
            self.__dict__['_defaults'] = {}
                        
        self._dict.update(kw)
    #@-node:__init__
    #@+node:__getattr__
    def __getattr__(self, attr):
    
        try:
            if not self.__dict__.has_key('_dict') or attr not in self._dict.keys():
                if attr not in ['__getinitargs__', '__getstate__', '__setstate__', '__iter__',
                                '__eq__', '__coerce__', '__cmp__']:
                    #print "can't find attribute '%s'" % attr
                    #print "Cell attribs: %s" % self._dict.keys()
                    pass
                raise AttributeError("Cell object has no attribute '%s'" % attr)
        except AttributeError:
            if attr not in ['__getinitargs__', '__getstate__', '__setstate__', '__iter__',
                            '__eq__', '__coerce__', '__cmp__']:
                fd = open("freemail.log.crash", "ab")
                traceback.print_exc(file=fd)
                fd.close()
            raise
    
        return self._dict[attr]
    #@-node:__getattr__
    #@+node:__setattr__
    def __setattr__(self, attr, val):
    
        self.__dict__[attr] = val
        if attr not in ['_val', '_seq', '_dict', '_defaults']:
            self._dict[attr] = val
    #@-node:__setattr__
    #@+node:__delattr__
    def __delattr__(self, name):
        
        self._dict.__delattr__(name)
    #@-node:__delattr__
    #@+node:__iter__
    def __iter__(self):
    
        #return self._seq.__iter__
        return iter(self._seq)
    #@-node:__iter__
    #@+node:__delattr__
    def __delattr__(self, attr):
    
        if not self._dict.has_key(attr):
            raise AttributeError("cell object has no attribute '%s'" % attr)
    
        del self._dict[attr]
    #@-node:__delattr__
    #@+node:__getitem__
    def __getitem__(self, idx):
    
        try:
            return self._seq[idx]
        except:
            return self.__getattr__(idx)
    #@-node:__getitem__
    #@+node:__setitem__
    def __setitem__(self, idx, val):
    
        try:
            self._seq[idx] = val
        except:
            self.__setattr__(idx, val)
    #@-node:__setitem__
    #@+node:__delitem__
    def __delitem__(self, idx):
        try:
            self._seq.__delitem__(idx)
        except TypeError:
            del self._dict[idx]
    #@-node:__delitem__
    #@+node:__getslice__
    def __getslice__(self, fromidx, toidx):
        
        return self._seq[fromidx:toidx]
    #@-node:__getslice__
    #@+node:__setslice__
    def __setslice__(self, fromidx, toidx, newslice):
        
        self._seq[fromidx:toidx] = newslice
    #@-node:__setslice__
    #@+node:__nonzero__
    def __nonzero__(self):
        return (len(self._seq) > 0)
    #@-node:__nonzero__
    #@+node:__len__
    def __len__(self):
        
        return len(self._seq)
    #@-node:__len__
    #@+node:__str__
    def __str__(self):
    
        return self.dump()
    #@-node:__str__
    #@+node:__repr__
    def __repr__(self):
        return self.dump()
    #@-node:__repr__
    #@+node:append
    def append(self, *items, **kw):
    
        for item in items:
            if item is not None:
                if item.__class__ is not cell:
                    pass
                    #raise Exception("Args to append must be cell objects")
                else:
                    item.setDefaults(self._defaults)
                self._seq.append(item)
    
        if kw != {}:
            c = cell(**kw)
            c.setDefaults(self._defaults)
            self._seq.append(c)
    #@-node:append
    #@+node:dump
    def dump(self):
        """
        Dumps out attribs
        """
        rep = "{"
        items = []
        for k,v in self._dict.items():
            items.append("%s: %s" % (repr(k), repr(v)))
        rep += ", ".join(items)
        rep += "}"
        rep += repr(self._seq)
        return rep
    #@-node:dump
    #@+node:dumpseq
    def dumpseq(self):
        """
        Dumps out sequence
        """
    #@-node:dumpseq
    #@+node:extend
    def extend(self, lst):
        
        self._seq.extend(lst)
    #@-node:extend
    #@+node:getColAsList
    def getColAsList(self, colname):
    
        lst = []
        for item in self._seq:
            lst.append(getattr(item, colname))
        return lst
    #@-node:getColAsList
    #@+node:has_key
    def has_key(self, name):
        return self._dict.has_key(name)
    #@-node:has_key
    #@+node:index
    def index(self, item):
        
        return self._seq.index(item)
    #@-node:index
    #@+node:insert
    def insert(self, idx, obj):
        
        return self._seq.insert(idx, obj)
    #@-node:insert
    #@+node:items
    def items(self):
        return self._dict.items()
    #@-node:items
    #@+node:join
    def join(self, other, *attribs):
        """
        Returns a cell containing a sequence of joined rows of this and other cell
        """
        if len(attribs) == 1:
            if type(attribs[0]) in [type([]), type(())]:
                attribs = attribs[0]
        newcell = cell()
        for item in self._seq:
            itemdict = item._dict
            itemdictkeys = itemdict.keys()
            for item1 in other._seq:
                item1dict = item1._dict
                item1dictkeys = item1dict.keys()
                matches = 1
                for attr in attribs:
                    if not (attr in itemdictkeys \
                            and attr in item1dictkeys \
                            and itemdict[attr] == item1dict[attr]):
                        matches = 0
                        break
                if matches:
                    # create cell with attribs of both items
                    newitem = cell()
                    for attr in itemdictkeys:
                        setattr(newitem, attr, itemdict[attr])
                    for attr in item1dictkeys:
                        setattr(newitem, attr, item1dict[attr])
                    newcell.append(newitem)
        return newcell
    #@-node:join
    #@+node:keys
    def keys(self):
        return self._dict.keys()
    #@-node:keys
    #@+node:onlyWhen
    def onlyWhen(self, func):
        newcell = cell()
        newcell._seq = filter(func, self._seq)
        return newcell
    #@-node:onlyWhen
    #@+node:pop
    def pop(self, idx=None):
        
        if idx is None:
            return self._seq.pop()
        else:
            return self._seq.pop(idx)
    #@-node:pop
    #@+node:project
    def project(self, *attribs):
        """
        Returns a cell object, whose list members contain only the attributes
        in attribs
        """
        if len(attribs) == 1:
            if type(attribs[0]) in [type([]), type(())]:
                attribs = attribs[0]
        newcell = cell()
        for item in self._seq:
            newitem = cell()
            for attr in attribs:
                setattr(newitem, attr, getattr(item, attr))
            newcell.append(newitem)
        return newcell
    #@-node:project
    #@+node:remove
    def remove(self, value):
        
        self._seq.remove(value)
    #@-node:remove
    #@+node:removeWhen
    def removeWhen(self, func=None, **kw):
        if func:
            def _shouldKeep(item, func=func):
                return not func(item)
            self._seq = filter(_shouldKeep, self._seq)
        else:
            def _shouldKeep(item, kw=kw):
                #print kw
                for attr, val in kw.items():
                    if not (hasattr(item, attr) and getattr(item, attr) == val):
                        return 1
                return 0
            self._seq = filter(_shouldKeep, self._seq)
    #@-node:removeWhen
    #@+node:reverse
    def reverse(self):
        
        self._seq.reverse()
    #@-node:reverse
    #@+node:select
    def select(self, **kw):
    
        def _select(item, kw=kw):
            #print kw
            for attr, val in kw.items():
                if not (hasattr(item, attr) and getattr(item, attr) == val):
                    return 0
            return 1
    
        return cell(filter(_select, self._seq))
    #@-node:select
    #@+node:setDefaults
    def setDefaults(self, defaults):
        """
        Create a new cell, containing all the attribs of the old one,
        with the defaults filled in
        """
        d = self._dict
        for k, v in defaults.items():
            if not d.has_key(k):
                d[k] = v
    #@-node:setDefaults
    #@+node:sort
    def sort(self, *attribs):
        """
        Sorts a cell's sequence
        """
        if len(attribs) == 1:
            if type(attribs[0]) in [type([]), type(())]:
                attribs = attribs[0]
    
        def sortfunc(item1, item2, attribs=attribs):
            for attr in attribs:
                has1 = hasattr(item1, attr)
                has2 = hasattr(item1, attr)
    
                # judge first on presence of attribs
                if has1 and not has2:
                    return 1
                elif has2 and not has1:
                    return -1
                
                # now judge on values
                if (has1 and has2):
                    val1 = getattr(item1, attr)
                    val2 = getattr(item2, attr)
                    if val1 < val2:
                        return -1
                    elif val1 > val2:
                        return 1
            
            # exhausted sort attribs - judge as identical
            return 0
    
        self._seq.sort(sortfunc)
    #@-node:sort
    #@+node:unique
    def unique(self):
        """
        Returns a copy of this cell, with duplicate sequence members removed
        """
        newcell = cell()
        seq = self._seq
        seqlen = len(seq)
        i = 0
        while i < seqlen:
            isunique = 1
            j = i
            while j < seqlen:
                if seq[i]._dict == seq[j]._dict:
                    isunique = 0
                    break
                j = j + 1
            if isunique:
                newcell.append(seq[i])
            i = i + 1
        newcell._seq.reverse()
        return newcell
    #@-node:unique
    #@+node:values
    def values(self):
        return self._dict.values()
    #@-node:values
    #@-others
#@-node:class cell
#@+node:class slotmap
class slotmap:
    """
    Implements a kind of sparse list for queue slots
    """

    def __init__(self):

        self.slots = {}

    def __getitem__(self, idx):

        # will always work - creates an item if one doesn't already exist
        if not self.slots.has_key(idx):
            self.slots[idx] = SLOT_STATE_EMPTY
        return self.slots[idx]

    def __repr__(self):
        return repr(self.slots)
    
    def __str__(self):
        return str(self.slots)

    def __setitem__(self, idx, val):
        self.slots[idx] = val

    def keys(self):
        return self.slots.keys()

    def values(self):
        return self.slots.values()

    def items(self):
        return self.slots.items()

    def between(self, fromidx, toidx):
        keys = range(fromidx, toidx)
        return filter(lambda r,myself=self: myself[r] == SLOT_STATE_EMPTY,
                      keys)

    def resetBusy(self):
        s = self.slots
        for k in s:
            if s[k] == SLOT_STATE_BUSY:
                s[k] = SLOT_STATE_EMPTY

    def purgeOld(self, idx):

        for item in self.slots.keys():
            if item < idx:
                del self.slots[item]
#@-node:class slotmap
#@+node:DynamicSemaphore
class DynamicSemaphore(threading._Semaphore):

    """
    Extension of Semaphore that provides for dynamic resizing of
    the semaphore.
    """

    def __init__(self, value=1, verbose=None):
        """
        store an 'initvalue' attribute, so we can accurately
        frig the '__value' field.
        """
        threading._Semaphore.__init__(self, value, verbose)
        self.maxvalue = value

    def resize(self, newvalue=1):
        """
        Sets a new limit on the number of unreleased 'acquire()'s
        """
        nOut = self.maxvalue - self._Semaphore__value
        self.maxvalue = newvalue
        self._Semaphore__value = newvalue - nOut

    def acquire(self, blocking=1):
        rc = 0
        self._Semaphore__cond.acquire()
        while self._Semaphore__value <= 0:
            if not blocking:
                break
            self._Semaphore__cond.wait()
        else:
            self._Semaphore__value = self._Semaphore__value - 1
            rc = 1
        self._Semaphore__cond.release()
        return rc

    def isempty(self):
        return (self._Semaphore__value == self.maxvalue)

    def release(self):
        self._Semaphore__cond.acquire()
        self._Semaphore__value = self._Semaphore__value + 1
        self._Semaphore__cond.notify()
        self._Semaphore__cond.release()

    def _dump(self):
        print "DynamicSemaphore: __value='%d', maxvalue='%d'" % (self._Semaphore__value,
                                                                  self.maxvalue)
#@-node:DynamicSemaphore
#@+node:b64enc
def b64enc(s):
    """
    Returns a base-64 encoding of a string, all one one line
    """
    return base64.encodestring(s).replace("\n", "")

def b64dec(s):
    """
    Decodes a single-line base64 encoded string
    """
    return base64.decodestring(s)
#@-node:b64enc
#@+node:dhms
def dhms(secs):
    """
    Formats a number of seconds into a human readable string,
    'n days, n hours, n minutes, n seconds
    """
    days, rest = divmod(secs, 86400)
    hours, rest = divmod(rest, 3600)
    minutes, rest = divmod(rest, 60)
    seconds = rest
    return "%d days, %d hours, %d minutes, %d seconds" % (days, hours, minutes, seconds)
#@-node:dhms
#@+node:dbrStartTime
def dbrStartTime(interval=86400, nperiods=0):
    """
    Returns the seconds since epoch at which a dbr period began.
    
    Arguments:
        - interval - the length of the dbr interval in seconds, default 1 day
        - nperiods - the number of periods to go *forward*
    """
    now = time.time()
    then = now + nperiods * interval
    then = then - (then % interval)
    return then


#@-node:dbrStartTime
#@+node:exceptionString
def exceptionString():
    
    """
    Gets a traceback of the last exception and returns it as a string
    Also writes the stacktrace to 'freemail.log.crash'
    """
    
    s = StringIO()
    traceback.print_exc(file=s)
    dump = s.getvalue()

    exceptionLogLock.acquire()
    try:
        fd = open("freemail.log.crash", "a")
        fd.write("%s\n" % dump)
        fd.close()
    except:
        pass
    exceptionLogLock.release()

    return dump
 
#@-node:exceptionString
#@+node:stackString
def stackString():
    
    """
    Gets a traceback of the stack
    """
    
    s = StringIO()
    traceback.print_stack(file=s)
    return s.getvalue()
 
#@-node:stackString
#@+node:stripAddress
def stripAddress(address):
    """
    Strip the leading & trailing <> from an address.  Handy for
    getting FROM: addresses.
    """
    #start = string.index(address, '<') + 1
    #end = string.index(address, '>')
    #return address[start:end]

    patt = "(mail|Mail|MAIL|rcpt|Rcpt|RCPT)(\\s+)(from|From|FROM|to|To|TO):(\\s*)(.+)"
    address = string.strip(address)
    address = re.findall(patt, address)
    #print address
    address = address[0][4]
    address = address.strip()
    if address[0] == '<':
        address = address[1:]
    if address[-1] == '>':
        address = address[:-1]
    addrbits = address.split("@")
    if len(addrbits) == 2:
        name, domain = addrbits
        if name[0] == '"' and name[-1] == '"':
            name = name[1:-1]
        address = name.strip() + "@" + domain.strip()
    return address
#@-node:stripAddress
#@+node:hash
def hash(raw):
    """
    Hashes a string into a hex string
    """
    #return re.sub("[=]*\s*", "", base64.encodestring(sha.new(raw).digest()))
    return sha.new(raw).hexdigest()
#@-node:hash
#@+node:randstring
def randstring():
    """
    Outputs a 16-char random string
    """
    s = []
    random.seed(time.time())
    for i in range(32):
        s.append(chr(random.randint(ord("A"), ord("Z"))))
    s = "".join(s)
    return s
    return hash(s)
#@-node:randstring
#@+node:getNewTid
def getNewTid():
    global newTid
    tid = newTid
    newTid = newTid + 1
    return tid
#@-node:getNewTid
#@+node:ulFromList
def ulFromList(lst):
    u = ul()
    for item in lst:
        u.add(li(item))
    return u
#@-node:ulFromList
#@+node:freemailAddrToUri
def freemailAddrToUri(addr):
    """
    Converts a freemail address to the URI of the corresponding mailsite

    For example:
     - fred@blahblah.freemail => SSK@blahblahPAgM/freemail/fred

    Returns a freenet.uri object
    """
    try:
        name, rest = addr.split("@")
        hash, domain = rest.split(".")
        valid = 1
    except:
        valid = 0

    if not valid:
        raise Exception("Invalid freemail address syntax '%s'" % addr)

    uri = freenet.uri("SSK@%sPAgM/freemail/%s" % (hash, name))
    return uri
#@-node:freemailAddrToUri
#@+node:uriToFreemailAddr
def uriToFreemailAddr(uri):
    """
    Converts a mailsite URI to a freemail address

    For example:
     - SSK@blahblahblahPAgM/freemail/alice[//] => alice@blahblahblah.freemail

    The uri can be expressed as a string, or a freenet.uri object
    """
    olduri = uri
    uri = freenet.uri(str(uri))
    hash = uri.hash
    if hash.endswith("PAgM") or hash.endswith("BCMA"):
        hash = hash[:-4]
    mskpath = uri.mskpath
    sskpath = uri.sskpath.split("/")
    
    if mskpath:
        raise Exception("Bad mailsite URI '%s' - msk path non-empty" % olduri)

    if uri.type != 'SSK':
        raise Exception("Bad mailsite URI '%s' - it's not an SSK" % olduri)

    if len(sskpath) != 2:
        raise Exception("Bad mailsite URI '%s' - should be 2 elements in SSK path" % olduri)
    if sskpath[0] != 'freemail':
        raise Exception("Bad mailsite URI '%s' - SSK path should begin with '/freemail'" % olduri)

    name = sskpath[1]
    
    return "%s@%s.freemail" % (name, hash)
#@-node:uriToFreemailAddr
#@+node:btnForm
def btnForm(label, action="", **kw):
    """
    Returns a styled button in its own form
    """
    butform = form(attr(method="POST", action=action),
                   btn(label),
                   )
    for k,v in kw.items():
        butform.add(input(type='hidden', name=k, value=v))

    return butform
#@-node:btnForm
#@+node:btn
def btn(label):
    """
    Creates and returns a styled text button

    Arguments:
     - label - text of button label
     - action - relative/absolute URL for action
    """

    return input(type="submit", class_="textbutton", alt=label, value=label)
#@-node:btn
#@+node:usage
def usage(msg='', exitcode=0):

    iswindows = (sys.platform == 'win32')
    if msg:
        print msg
    print "Options:"
    print "  -h, -?, --help       Display this help"
    print "  -v, --version        Display Freemail program version number"
    print "  -l, --logfile        File to write logging messages to"
    print "  -q, --quiet          Don't display log messages to stdout, only write to"
    print "                       logfile (default is both stdout and logfile)"
    if not iswindows:
        print "  -f, --foreground Run in foreground (default is to run in background)"
    print "  -i, --interactive    Run server in an interactive console - very handy"
    print "      --nostart        Used with '-i'. Don't launch the server yet"
    print "      --prompt         Sets prompt when running interactively"
    print "  -n, --node-address=  Hostname/IP addr of Freenet node, default localhost"
    print "  -p, --node-port=     FCP port number on this node, default 8481"
    print "  -P, --pop-port=      Port for POP3 server to listen on, default 10110"
    print "  -S, --smtp-port=     Port for SMTP server to listen on, default 10025"
    print "  -H, --http-port=     Port for HTTP server to listen on, default 8889"
    print "  -T, --telnet-port=   Port for Telnet server to listen on, default 10023"
    print "  -D, --database=      Path of database file, defaults to 'freemail.dat'"
    print "      --store-dir=     Path of store directory, ddefaults to 'freemail.store'"
    print "  -V, --verbosity=     Sets the verbosity of log output. Valid values are:"
    print "                       0=shutup, 1=critical, 2=normal, 3=trivial, 4=debug, 5=noisy"
    print "   --freenetverbosity  sets verbosity of freenet log messages - same scale"
    print "  -L, --listen-hosts=  comma-separated list of hosts from which to accept"
    print "                       POP3, SMTP and HTTP connections. Connecting from"
    print "                       any host not in this list results in immediate"
    print "                       disconnect. Defaults to '127.0.0.1'"
    print "  -t, --max-threads=   Sets a limit on the number of 'worker threads', the"
    print "                       threads that perform actual mail transfer. More"
    print "                       threads mean your email gets transferred faster, but"
    print "                       consumes more system resources. Recommend 3-10"
    if exitcode != None:
        sys.exit(exitcode)




#@-node:usage
#@+node:help
def help():
    usage("", None)

#@-node:help
#@+node:main
def main(*args):

    import getopt

    global iswindows, m, db, conf
    iswindows = (sys.platform == 'win32')

    #print "sys.argv: %s" % str(sys.argv)
    try:
        opts, args = getopt.getopt(args,
                                   "h?vqifl:n:p:P:S:H:D:V:L:T:",
                                   ['help', 'version',
                                    'logfile=', 'foreground', 'interactive', 'nostart', 'prompt=',
                                    'node-address=', 'node-port=',
                                    'pop3-port=', 'smtp-port=', 'http-port=', 'telnet-port=',
                                    'database=', 'store-dir=',
                                    'verbosity=', 'freenetverbosity=', 'quiet',
                                    'listen-hosts=', 'max-threads='])
    except:
        import freemail
        import traceback
        traceback.print_exc(file=sys.stdout)
        freemail.usage("You entered an invalid option", 1)

    # set defaults
    showHelp = False
    showVersion = False
    nodeAddress = "127.0.0.1"
    nodePortStr = '8481'
    pop3PortStr = '10110'
    smtpPortStr = '10025'
    httpPortStr = '8889'
    telnetPortStr = '10023'
    database = "freemail.dat"
    storeDir = 'freemail.store'
    if iswindows:
        verbosityStr = '4'
        freenetVerbosityStr = '2'
    else:
        verbosityStr = '4'
        freenetVerbosityStr = '2'
    listenHostsStr = "127.0.0.1"
    maxThreadsStr = '5'
    runInForeground = iswindows
    logFile = "freemail.log"
    isQuiet = 0
    isInteractive = 0
    noStart = 0
    global interactivePrompt

    # grab raw options
    for opt, val in opts:
        if opt in ['-h', '-?', '--help']:
            showHelp = True
        elif opt in ['-v', '--version']:
            showVersion = True
        elif opt in ['-f', '--foreground'] and not iswindows:
            runInForeground = True
        elif opt in ['-l', '--logfile']:
            logFile = val
        elif opt in ['-q', '--quiet']:
            isQuiet = 1
        elif opt in ['-n', '--node-address']:
            nodeAddress = val
        elif opt in ['-p', '--node-port']:
            nodePortStr = val
        elif opt in ['-P', '--pop-port']:
            pop3PortStr = val
        elif opt in ['-H', '--http-port']:
            httpPortStr = val
        elif opt in ['-T', '--telnet-port']:
            telnetPortStr = val
        elif opt in ['-D', '--database']:
            database = val
        elif opt in ['--store-dir']:
            storeDir = val
        elif opt in ['-V', '--verbosity']:
            verbosityStr = val
        elif opt in ['--freenetverbosity']:
            freenetVerbosityStr = val
        elif opt in ['-L', '--listen-hosts']:
            listenHostsStr = val
        elif opt in ['-t', '--max-threads']:
            maxThreadsStr = val
        elif opt in ['-i', '--interactive']:
            isInteractive = 1
            runInForeground = 1
        elif opt in ['--nostart']:
            isInteractive = 1
            runInForeground = 1
            noStart = 1
        elif opt in ['--prompt']:
            interactivePrompt = val

    # intercept terminal options
    if showHelp:
        usage("Freemail version %s" % version, 0)
    if showVersion:
        print "Freemail version %s" % version
        sys.exit(0)

    # convert numerical arguments
    try:
        nodePort = int(nodePortStr)
    except:
        usage("Bad Freenet node port '%s'" % nodePortStr, 1)
    try:
        pop3Port = int(pop3PortStr)
    except:
        usage("Bad POP3 server port '%s'" % pop3PortStr, 1)
    try:
        smtpPort = int(smtpPortStr)
    except:
        usage("Bad SMTP server port '%s'" % smtpPortStr, 1)
    try:
        httpPort = int(httpPortStr)
    except:
        usage("Bad HTTP server port '%s'" % httpPortStr, 1)
    try:
        telnetPort = int(telnetPortStr)
    except:
        usage("Bad Telnet server port '%s'" % telnetPortStr, 1)
    try:
        verbosityLevel = int(verbosityStr)
    except:
        usage("Bad verbosity value '%s'" % verbosityStr, 1)
    try:
        freenetVerbosityLevel = int(freenetVerbosityStr)
    except:
        usage("Bad freenet verbosity value '%s'" % freenetVerbosityStr, 1)
    try:
        maxThreads = int(maxThreadsStr)
    except:
        usage("Bad maximum thread count '%s'" % maxThreadsStr, 1)

    #print opts

    # get listen hosts list
    try:
        listenHosts = [s.strip() for s in listenHostsStr.split(",")]
    except:
        usage("Bad listen hosts list '%s'" % listenHostsStr, 1)

    if 0:
        print "nodeAddress =     %s" % nodeAddress
        print "nodePort =        %s" % nodePort
        print "popPort =         %s" % pop3Port
        print "smtpPort =        %s" % smtpPort
        print "httpPort =        %s" % httpPort
        print "database =        %s" % database
        print "verbosity =       %s" % verbosityLevel
        print "listenHosts =     %s" % str(listenHosts)
        print "maxThreads =      %s" % maxThreads
        print "logFile =         %s" % logFile
        print "isQuiet =         %s" % isQuiet
        if not iswindows:
            print "runInForeground = %s" % runInForeground

    if not runInForeground:
        args = " ".join(sys.argv[1:])
        os.system("./freemail.py -f %s &" % args)
        sys.exit(0)

    # running in foreground - write out a pid
    fd = open("freemail.pid", "w")
    fd.write("%s" % os.getpid())
    fd.close()

    # Create a freemail server object
    m = freemailServer(
        popHosts=listenHosts,
        smtpHosts=listenHosts,
        httpHosts=listenHosts,
        telnetHosts=listenHosts,
        fcpHost=nodeAddress,
        fcpPort=nodePort,
        popPort=pop3Port,
        smtpPort=smtpPort,
        httpPort=httpPort,
        telnetPort=telnetPort,
        database=database,
        storeDir=storeDir,
        maxWorkerThreads=maxThreads,
        logfile=logFile,
        verbosity=verbosityLevel,
        freenetverbosity=freenetVerbosityLevel,
        quiet=isQuiet,
        )

    # set logging verbosity
    m.verbosity(verbosityLevel)

    # launch the shell if interactive
    if isInteractive:
        db = m.db
        conf = db.config
        if not noStart:
            m.startServer()
        con = freemailConsole(globals())
        con.run()
    else:
        # and run it
        m.runServer()
#@-node:main
#@+node:MAINLINE
if __name__ == '__main__':
    main(*(sys.argv[1:]))
#@-node:MAINLINE
#@-others
#@-node:@file freemail.py
#@-leo
