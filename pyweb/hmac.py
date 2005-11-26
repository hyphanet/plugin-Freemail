#@+leo
#@+node:0::@file pyweb/hmac.py
#@+body
#@@language python
#@<< hmac declarations >>
#@+node:1::<< hmac declarations >>
#@+body
"""HMAC (Keyed-Hashing for Message Authentication) Python module.

Implements the HMAC algorithm as described by RFC 2104.
"""

import string


#@-body
#@-node:1::<< hmac declarations >>


#@+others
#@+node:2::_strxor
#@+body
def _strxor(s1, s2):
    """Utility method. XOR the two strings s1 and s2 (must have same length).
    """
    return string.join(map(lambda x, y: chr(ord(x) ^ ord(y)), s1, s2), "")

#@-body
#@-node:2::_strxor
#@+node:3::class HMAC
#@+body

# The size of the digests returned by HMAC depends on the underlying
# hashing module used.
digest_size = None

class HMAC:
	
    #@<< class HMAC declarations >>
    #@+node:1::<< class HMAC declarations >>
    #@+body
    """RFC2104 HMAC class.
    
    This supports the API for Cryptographic Hash Functions (PEP 247).
    """
    
    
    #@-body
    #@-node:1::<< class HMAC declarations >>


    #@+others
    #@+node:2::__init__
    #@+body
    def __init__(self, key, msg = None, digestmod = None):
        """Create a new HMAC object.
    
        key:       key for the keyed hash object.
        msg:       Initial input for the hash, if provided.
        digestmod: A module supporting PEP 247. Defaults to the md5 module.
        """
    
        #print "hmac: digestmod.__name__=", digestmod.__name__
    
        if digestmod == None:
            import md5
            digestmod = md5
            digestmod.digest_size = 16
        
        elif digestmod.__name__ == 'sha':
            digestmod.digest_size = 20
    
        self.digestmod = digestmod
        self.outer = digestmod.new()
        self.inner = digestmod.new()
        self.digest_size = digestmod.digest_size
    
        blocksize = 64
        ipad = "\x36" * blocksize
        opad = "\x5C" * blocksize
    
        if len(key) > blocksize:
            key = digestmod.new(key).digest()
    
        key = key + chr(0) * (blocksize - len(key))
        self.outer.update(_strxor(key, opad))
        self.inner.update(_strxor(key, ipad))
        if (msg):
            self.update(msg)
    
    
    #@-body
    #@-node:2::__init__
    #@+node:3::update
    #@+body
    ##    def clear(self):
    ##        raise NotImplementedError, "clear() method not available in HMAC."
    
    def update(self, msg):
        """Update this hashing object with the string msg.
        """
        self.inner.update(msg)
    
    #@-body
    #@-node:3::update
    #@+node:4::copy
    #@+body
    def copy(self):
        """Return a separate copy of this hashing object.
    
        An update to this copy won't affect the original object.
        """
        other = HMAC("")
        other.digestmod = self.digestmod
        other.inner = self.inner.copy()
        other.outer = self.outer.copy()
        return other
    
    #@-body
    #@-node:4::copy
    #@+node:5::digest
    #@+body
    def digest(self):
        """Return the hash value of this hashing object.
    
        This returns a string containing 8-bit data.  The object is
        not altered in any way by this function; you can continue
        updating the object after calling this function.
        """
        h = self.outer.copy()
        h.update(self.inner.digest())
        return h.digest()
    
    #@-body
    #@-node:5::digest
    #@+node:6::hexdigest
    #@+body
    def hexdigest(self):
        """Like digest(), but returns a string of hexadecimal digits instead.
        """
    
        tmp = []
        for x in tuple(self.digest()):
            tmp.append(string.zfill(hex(ord(x))[2:], 2))
        return string.join(tmp, "")
        #return "".join(
        #    [string.zfill(hex(ord(x))[2:], 2) for x in tuple(self.digest())]
        #    )
    
    #@-body
    #@-node:6::hexdigest
    #@-others


#@-body
#@-node:3::class HMAC
#@+node:4::new
#@+body
def new(key, msg = None, digestmod = None):
    """Create a new hashing object and return it.

    key: The starting key for the hash.
    msg: if available, will immediately be hashed into the object's starting
    state.

    You can now feed arbitrary strings into the object using its update()
    method, and can ask for the hash value at any time by calling its digest()
    method.
    """
    return HMAC(key, msg, digestmod)

#@-body
#@-node:4::new
#@-others


#@-body
#@-node:0::@file pyweb/hmac.py
#@-leo
