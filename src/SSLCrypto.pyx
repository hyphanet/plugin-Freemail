#@+leo-ver=4
#@+node:@file SSLCrypto.pyx
#@@language python

"""
SSLCrypto - partial Python wrapper for SSL crypto

Contains an abstract class called <b>key</b>, which provides
an effortlessly simple API for most common crypto operations.<br>

An almost totally-compatible replacement for ezPyCrypto

Also, features an implementation of ElGamal
"""
#@+others
#@+node:imports
#import sys, types

#
# Crypto imports
#

#from pdb import set_trace as trace
#import pickle
import base64
import zlib
import md5
import types
import traceback
import struct
import random
import math
#@-node:imports
#@+node:Exceptions
# Define some exceptions for the various problems that can happen

class CryptoKeyError(Exception):
    "Attempt to import invalid key"

class CryptoCannotDecrypt(Exception):
    "Cannot decrypt - private key not present"

class CryptoNoPrivateKey(Exception):
    "Key object has no private key"

class CryptoInvalidCiphertext(Exception):
    "Ciphertext was not produced with SSLCrypto"

class CryptoInvalidSignature(Exception):
    "Signature text was not produced with SSLCrypto"

class CryptoInvalidElGamalK(Exception):
    "Invalid k parameter for ElGamal operation"
#@-node:Exceptions
#@+node:cdef externs
# Extern declarations

#@+others
#@+node:Python.h
cdef extern from "Python.h":
    object PyString_FromStringAndSize(char *, int)

#@-node:Python.h
#@+node:string.h
cdef extern from "string.h":

    cdef void *memset(void *s, int c, int n)
    cdef void *memcpy(void *dest, void *src, int n)
#@-node:string.h
#@+node:stdio.h
cdef extern from "stdio.h":
    int printf(char *format,...)

#@-node:stdio.h
#@+node:stdlib.h
cdef extern from "stdlib.h":
    void *malloc(int size)
    void free(void *ptr)

#@-node:stdlib.h
#@+node:openssl/crypto.h
cdef extern from "openssl/crypto.h":
    void CRYPTO_free(void *)

    ctypedef struct CRYPTO_EX_DATA:
        void *sk
        int dummy

#@-node:openssl/crypto.h
#@+node:openssl/bn.h
cdef extern from "openssl/bn.h":
    ctypedef struct BIGNUM:
        unsigned long *d
        int top
        int dmax
        int neg
        int flags

    ctypedef unsigned long BN_ULONG

    ctypedef struct BN_MONT_CTX:
        int ri      # number of bits in R
        BIGNUM RR   # used to convert to montgomery form
        BIGNUM N    # The modulus
        BIGNUM Ni   # R*(1/R mod N) - N*Ni = 1
                    # (Ni is only stored for bignum algorithm)
        BN_ULONG n0 # least significant word of Ni
        int flags

    ctypedef struct BN_BLINDING:
        int init
        BIGNUM *A
        BIGNUM *Ai
        BIGNUM *mod

    ctypedef struct BN_CTX:
        int dummy

    cdef BIGNUM *BN_generate_prime(BIGNUM *ret,int bits,int safe,
                                   BIGNUM *add, BIGNUM *rem,
                                   void (*callback)(int,int,void *),void *cb_arg)
    cdef BIGNUM *BN_rand(BIGNUM *rnd, int bits, int top, int bottom)



    cdef int BN_dec2bn(BIGNUM **a, char *str)
    cdef char *BN_bn2dec(BIGNUM *a)
    BIGNUM *BN_new()
    cdef void BN_free(BIGNUM *a)


    cdef BN_CTX *BN_CTX_new()
    cdef void BN_CTX_init(BN_CTX *c)
    cdef void BN_CTX_free(BN_CTX *c)

#@-node:openssl/bn.h
#@+node:openssl/dh.h
cdef extern from "openssl/dh.h":

    ctypedef struct DH:
        int pad
        int version
        BIGNUM *p
        BIGNUM *g
        long length
        BIGNUM *pub_key
        BIGNUM *priv_key
        int flags
        char *method_mont_p
        # Place holders if we want to do X9.42 DH
        BIGNUM *q
        BIGNUM *j
        unsigned char *seed
        int seedlen
        BIGNUM *counter

        int references
        CRYPTO_EX_DATA ex_data
        void *meth
        void *engine
    
    cdef DH *DH_new()
    cdef void DH_free(DH *dh)
    cdef int DH_up_ref(DH *dh)
    cdef int DH_size(DH *dh)
    cdef int DH_get_ex_new_index(long argl, void *argp, void *new_func,
                                 void *dup_func, void *free_func)
    cdef int DH_set_ex_data(DH *d, int idx, void *arg)
    cdef void *DH_get_ex_data(DH *d, int idx)
    cdef DH *DH_generate_parameters(int prime_len,int generator,
                               void (*callback)(int,int,void *),void *cb_arg)
    cdef int DH_check(DH *dh,int *codes)
    cdef int DH_generate_key(DH *dh)
    cdef int DH_compute_key(unsigned char *key, BIGNUM *pub_key,DH *dh)
#@-node:openssl/dh.h
#@+node:openssl/blowfish.h

cdef extern from "openssl/blowfish.h":

    ctypedef enum BF_CONSTANTS:
        BF_ENCRYPT
        BF_DECRYPT
        BF_ROUNDS

    ctypedef long int BF_LONG

    ctypedef struct BF_KEY:
        BF_LONG P[BF_ROUNDS+2] # BF_ROUNDS + 2
        BF_LONG S[4*256]
 
    cdef void BF_set_key(BF_KEY *key, int len, char *data)

    cdef void BF_cfb64_encrypt(char *inbuf, char *outbuf, long length, BF_KEY *schedule,
                               char *ivec, int *num, int enc)

#@-node:openssl/blowfish.h
#@+node:openssl/rsa.h
cdef extern from "openssl/rsa.h":

    #
    # Constants
    #
    ctypedef enum RSA_PAD_MODES:
        RSA_PKCS1_OAEP_PADDING
        RSA_PKCS1_PADDING

    #
    # Structs
    #
    ctypedef struct RSA:
        int pad
        long version
        void *meth
        # functional reference if 'meth' is ENGINE-provided
        void *engine
        BIGNUM *n
        BIGNUM *e
        BIGNUM *d
        BIGNUM *p
        BIGNUM *q
        BIGNUM *dmp1
        BIGNUM *dmq1
        BIGNUM *iqmp

        # be careful using this if the RSA structure is shared
        CRYPTO_EX_DATA ex_data
        int references
        int flags

        # Used to cache montgomery values
        BN_MONT_CTX *_method_mod_n
        BN_MONT_CTX *_method_mod_p
        BN_MONT_CTX *_method_mod_q

        # all BIGNUM values are actually in the
        # following data, if it is not NULL
        char *bignum_data
        BN_BLINDING *blinding

    #
    # Function 'prototypes'
    #
    cdef RSA *RSA_new()
    cdef RSA *RSA_new_method(void *engine)
    cdef int RSA_size(RSA *)
    cdef RSA *RSA_generate_key(int bits, unsigned long e,
                               void (*callback)(int,int,void *),void *cb_arg)
    cdef int RSA_check_key(RSA *key)

    cdef int RSA_public_encrypt(int flen, unsigned char *pfrom,
                                unsigned char *pto, RSA *rsaobj, int padding)
    cdef int RSA_private_encrypt(int flen, unsigned char *pfrom,
                                 unsigned char *pto, RSA *rsaobj, int padding)
    cdef int RSA_public_decrypt(int flen, unsigned char *pfrom, 
                                unsigned char *pto, RSA *rsaobj, int padding)
    cdef int RSA_private_decrypt(int flen, unsigned char *pfrom, 
                                 unsigned char *pto, RSA *rsaobj, int padding)
    cdef void RSA_free (RSA *r)

    cdef int RSA_blinding_on(RSA *rsa, void *bnctx)
    cdef void RSA_blinding_off(RSA *rsa)

    cdef int RSA_sign(int type, unsigned char *m, unsigned int m_len,
                      unsigned char *sigret, unsigned int *siglen, RSA *rsa)

    cdef int RSA_verify(int type, unsigned char *m, unsigned int m_len,
                        unsigned char *sigbuf, unsigned int siglen, RSA *rsa)
#@-node:openssl/rsa.h
#@+node:openssl/rand.h
cdef extern from "openssl/rand.h":

    cdef void RAND_seed(char *buf, int num)
    cdef int RAND_bytes(char *buf, int num)
#@-node:openssl/rand.h
#@+node:openssl/objects.h
cdef extern from "openssl/objects.h":

    #
    # Constants
    #
    ctypedef enum RSA_HASH_ALGOS:
        NID_md5
#@-node:openssl/objects.h
#@-others

#@-node:cdef externs
#@+node:globals
cdef BIGNUM *BN_NULL
BN_NULL = <BIGNUM *>0

_True = 1
_False = 0

version = "0.2.3"

#@+others
#@+node:hardPrimes
hardPrimes = {
    4096: long("FFFFFFFF" "FFFFFFFF" "C90FDAA2" "2168C234" "C4C6628B" "80DC1CD1"
               "29024E08" "8A67CC74" "020BBEA6" "3B139B22" "514A0879" "8E3404DD"
               "EF9519B3" "CD3A431B" "302B0A6D" "F25F1437" "4FE1356D" "6D51C245"
               "E485B576" "625E7EC6" "F44C42E9" "A637ED6B" "0BFF5CB6" "F406B7ED"
               "EE386BFB" "5A899FA5" "AE9F2411" "7C4B1FE6" "49286651" "ECE45B3D"
               "C2007CB8" "A163BF05" "98DA4836" "1C55D39A" "69163FA8" "FD24CF5F"
               "83655D23" "DCA3AD96" "1C62F356" "208552BB" "9ED52907" "7096966D"
               "670C354E" "4ABC9804" "F1746C08" "CA18217C" "32905E46" "2E36CE3B"
               "E39E772C" "180E8603" "9B2783A2" "EC07A28F" "B5C55DF0" "6F4C52C9"
               "DE2BCBF6" "95581718" "3995497C" "EA956AE5" "15D22618" "98FA0510"
               "15728E5A" "8AAAC42D" "AD33170D" "04507A33" "A85521AB" "DF1CBA64"
               "ECFB8504" "58DBEF0A" "8AEA7157" "5D060C7D" "B3970F85" "A6E1E4C7"
               "ABF5AE8C" "DB0933D7" "1E8C94E0" "4A25619D" "CEE3D226" "1AD2EE6B"
               "F12FFA06" "D98A0864" "D8760273" "3EC86A64" "521F2B18" "177B200C"
               "BBE11757" "7A615D6C" "770988C0" "BAD946E2" "08E24FA0" "74E5AB31"
               "43DB5BFC" "E0FD108E" "4B82D120" "A9210801" "1A723C12" "A787E6D7"
               "88719A10" "BDBA5B26" "99C32718" "6AF4E23C" "1A946834" "B6150BDA"
               "2583E9CA" "2AD44CE8" "DBBBC2DB" "04DE8EF9" "2E8EFC14" "1FBECAA6"
               "287C5947" "4E6BC05D" "99B2964F" "A090C3A2" "233BA186" "515BE7ED"
               "1F612970" "CEE2D7AF" "B81BDD76" "2170481C" "D0069127" "D5B05AA9"
               "93B4EA98" "8D8FDDC1" "86FFB7DC" "90A6C08F" "4DF435C9" "34063199"
               "FFFFFFFF" "FFFFFFFF",
               16),

    1024: long("FFFFFFFF" "FFFFFFFF" "C90FDAA2" "2168C234"	"C4C6628B" "80DC1CD1"
               "29024E08" "8A67CC74" "020BBEA6" "3B139B22" "514A0879" "8E3404DD"
               "EF9519B3" "CD3A431B" "302B0A6D" "F25F1437" "4FE1356D" "6D51C245"
               "E485B576" "625E7EC6" "F44C42E9" "A637ED6B" "0BFF5CB6" "F406B7ED" 
               "EE386BFB" "5A899FA5" "AE9F2411" "7C4B1FE6" "49286651" "ECE65381"
               "FFFFFFFF" "FFFFFFFF",
               16),
    
    1536: long("FFFFFFFF" "FFFFFFFF" "C90FDAA2" "2168C234" "C4C6628B" "80DC1CD1"
               "29024E08" "8A67CC74" "020BBEA6" "3B139B22" "514A0879" "8E3404DD"
               "EF9519B3" "CD3A431B" "302B0A6D" "F25F1437" "4FE1356D" "6D51C245"
               "E485B576" "625E7EC6" "F44C42E9" "A637ED6B" "0BFF5CB6" "F406B7ED"
               "EE386BFB" "5A899FA5" "AE9F2411" "7C4B1FE6" "49286651" "ECE45B3D"
               "C2007CB8" "A163BF05" "98DA4836" "1C55D39A" "69163FA8" "FD24CF5F"
               "83655D23" "DCA3AD96" "1C62F356" "208552BB" "9ED52907" "7096966D"
               "670C354E" "4ABC9804" "F1746C08" "CA237327" "FFFFFFFF" "FFFFFFFF",
               16),

    2048: long("FFFFFFFF" "FFFFFFFF" "C90FDAA2" "2168C234" "C4C6628B" "80DC1CD1"
               "29024E08" "8A67CC74" "020BBEA6" "3B139B22" "514A0879" "8E3404DD"
               "EF9519B3" "CD3A431B" "302B0A6D" "F25F1437" "4FE1356D" "6D51C245"
               "E485B576" "625E7EC6" "F44C42E9" "A637ED6B" "0BFF5CB6" "F406B7ED"
               "EE386BFB" "5A899FA5" "AE9F2411" "7C4B1FE6" "49286651" "ECE45B3D"
               "C2007CB8" "A163BF05" "98DA4836" "1C55D39A" "69163FA8" "FD24CF5F"
               "83655D23" "DCA3AD96" "1C62F356" "208552BB" "9ED52907" "7096966D"
               "670C354E" "4ABC9804" "F1746C08" "CA18217C" "32905E46" "2E36CE3B"
               "E39E772C" "180E8603" "9B2783A2" "EC07A28F" "B5C55DF0" "6F4C52C9"
               "DE2BCBF6" "95581718" "3995497C" "EA956AE5" "15D22618" "98FA0510"
               "15728E5A" "8AACAA68" "FFFFFFFF" "FFFFFFFF",
               16),
    3072: long("FFFFFFFF" "FFFFFFFF" "C90FDAA2" "2168C234" "C4C6628B" "80DC1CD1"
               "29024E08" "8A67CC74" "020BBEA6" "3B139B22" "514A0879" "8E3404DD"
               "EF9519B3" "CD3A431B" "302B0A6D" "F25F1437" "4FE1356D" "6D51C245"
               "E485B576" "625E7EC6" "F44C42E9" "A637ED6B" "0BFF5CB6" "F406B7ED"
               "EE386BFB" "5A899FA5" "AE9F2411" "7C4B1FE6" "49286651" "ECE45B3D"
               "C2007CB8" "A163BF05" "98DA4836" "1C55D39A" "69163FA8" "FD24CF5F"
               "83655D23" "DCA3AD96" "1C62F356" "208552BB" "9ED52907" "7096966D"
               "670C354E" "4ABC9804" "F1746C08" "CA18217C" "32905E46" "2E36CE3B"
               "E39E772C" "180E8603" "9B2783A2" "EC07A28F" "B5C55DF0" "6F4C52C9"
               "DE2BCBF6" "95581718" "3995497C" "EA956AE5" "15D22618" "98FA0510"
               "15728E5A" "8AAAC42D" "AD33170D" "04507A33" "A85521AB" "DF1CBA64"
               "ECFB8504" "58DBEF0A" "8AEA7157" "5D060C7D" "B3970F85" "A6E1E4C7"
               "ABF5AE8C" "DB0933D7" "1E8C94E0" "4A25619D" "CEE3D226" "1AD2EE6B"
               "F12FFA06" "D98A0864" "D8760273" "3EC86A64" "521F2B18" "177B200C"
               "BBE11757" "7A615D6C" "770988C0" "BAD946E2" "08E24FA0" "74E5AB31"
               "43DB5BFC" "E0FD108E" "4B82D120" "A93AD2CA" "FFFFFFFF" "FFFFFFFF",
               16),

    4096: long("FFFFFFFF" "FFFFFFFF" "C90FDAA2" "2168C234" "C4C6628B" "80DC1CD1"
               "29024E08" "8A67CC74" "020BBEA6" "3B139B22" "514A0879" "8E3404DD"
               "EF9519B3" "CD3A431B" "302B0A6D" "F25F1437" "4FE1356D" "6D51C245"
               "E485B576" "625E7EC6" "F44C42E9" "A637ED6B" "0BFF5CB6" "F406B7ED"
               "EE386BFB" "5A899FA5" "AE9F2411" "7C4B1FE6" "49286651" "ECE45B3D"
               "C2007CB8" "A163BF05" "98DA4836" "1C55D39A" "69163FA8" "FD24CF5F"
               "83655D23" "DCA3AD96" "1C62F356" "208552BB" "9ED52907" "7096966D"
               "670C354E" "4ABC9804" "F1746C08" "CA18217C" "32905E46" "2E36CE3B"
               "E39E772C" "180E8603" "9B2783A2" "EC07A28F" "B5C55DF0" "6F4C52C9"
               "DE2BCBF6" "95581718" "3995497C" "EA956AE5" "15D22618" "98FA0510"
               "15728E5A" "8AAAC42D" "AD33170D" "04507A33" "A85521AB" "DF1CBA64"
               "ECFB8504" "58DBEF0A" "8AEA7157" "5D060C7D" "B3970F85" "A6E1E4C7"
               "ABF5AE8C" "DB0933D7" "1E8C94E0" "4A25619D" "CEE3D226" "1AD2EE6B"
               "F12FFA06" "D98A0864" "D8760273" "3EC86A64" "521F2B18" "177B200C"
               "BBE11757" "7A615D6C" "770988C0" "BAD946E2" "08E24FA0" "74E5AB31"
               "43DB5BFC" "E0FD108E" "4B82D120" "A9210801" "1A723C12" "A787E6D7"
               "88719A10" "BDBA5B26" "99C32718" "6AF4E23C" "1A946834" "B6150BDA"
               "2583E9CA" "2AD44CE8" "DBBBC2DB" "04DE8EF9" "2E8EFC14" "1FBECAA6"
               "287C5947" "4E6BC05D" "99B2964F" "A090C3A2" "233BA186" "515BE7ED"
               "1F612970" "CEE2D7AF" "B81BDD76" "2170481C" "D0069127" "D5B05AA9"
               "93B4EA98" "8D8FDDC1" "86FFB7DC" "90A6C08F" "4DF435C9" "34063199"
               "FFFFFFFF" "FFFFFFFF",
               16),

    6144: long("FFFFFFFF" "FFFFFFFF" "C90FDAA2" "2168C234" "C4C6628B" "80DC1CD1"
               "29024E08" "8A67CC74" "020BBEA6" "3B139B22" "514A0879" "8E3404DD"
               "EF9519B3" "CD3A431B" "302B0A6D" "F25F1437" "4FE1356D" "6D51C245"
               "E485B576" "625E7EC6" "F44C42E9" "A637ED6B" "0BFF5CB6" "F406B7ED"
               "EE386BFB" "5A899FA5" "AE9F2411" "7C4B1FE6" "49286651" "ECE45B3D"
               "C2007CB8" "A163BF05" "98DA4836" "1C55D39A" "69163FA8" "FD24CF5F"
               "83655D23" "DCA3AD96" "1C62F356" "208552BB" "9ED52907" "7096966D"
               "670C354E" "4ABC9804" "F1746C08" "CA18217C" "32905E46" "2E36CE3B"
               "E39E772C" "180E8603" "9B2783A2" "EC07A28F" "B5C55DF0" "6F4C52C9"
               "DE2BCBF6" "95581718" "3995497C" "EA956AE5" "15D22618" "98FA0510"
               "15728E5A" "8AAAC42D" "AD33170D" "04507A33" "A85521AB" "DF1CBA64"
               "ECFB8504" "58DBEF0A" "8AEA7157" "5D060C7D" "B3970F85" "A6E1E4C7"
               "ABF5AE8C" "DB0933D7" "1E8C94E0" "4A25619D" "CEE3D226" "1AD2EE6B"
               "F12FFA06" "D98A0864" "D8760273" "3EC86A64" "521F2B18" "177B200C"
               "BBE11757" "7A615D6C" "770988C0" "BAD946E2" "08E24FA0" "74E5AB31"
               "43DB5BFC" "E0FD108E" "4B82D120" "A9210801" "1A723C12" "A787E6D7"
               "88719A10" "BDBA5B26" "99C32718" "6AF4E23C" "1A946834" "B6150BDA"
               "2583E9CA" "2AD44CE8" "DBBBC2DB" "04DE8EF9" "2E8EFC14" "1FBECAA6"
               "287C5947" "4E6BC05D" "99B2964F" "A090C3A2" "233BA186" "515BE7ED"
               "1F612970" "CEE2D7AF" "B81BDD76" "2170481C" "D0069127" "D5B05AA9"
               "93B4EA98" "8D8FDDC1" "86FFB7DC" "90A6C08F" "4DF435C9" "34028492"
               "36C3FAB4" "D27C7026" "C1D4DCB2" "602646DE" "C9751E76" "3DBA37BD"
               "F8FF9406" "AD9E530E" "E5DB382F" "413001AE" "B06A53ED" "9027D831"
               "179727B0" "865A8918" "DA3EDBEB" "CF9B14ED" "44CE6CBA" "CED4BB1B"
               "DB7F1447" "E6CC254B" "33205151" "2BD7AF42" "6FB8F401" "378CD2BF"
               "5983CA01" "C64B92EC" "F032EA15" "D1721D03" "F482D7CE" "6E74FEF6"
               "D55E702F" "46980C82" "B5A84031" "900B1C9E" "59E7C97F" "BEC7E8F3"
               "23A97A7E" "36CC88BE" "0F1D45B7" "FF585AC5" "4BD407B2" "2B4154AA"
               "CC8F6D7E" "BF48E1D8" "14CC5ED2" "0F8037E0" "A79715EE" "F29BE328"
               "06A1D58B" "B7C5DA76" "F550AA3D" "8A1FBFF0" "EB19CCB1" "A313D55C"
               "DA56C9EC" "2EF29632" "387FE8D7" "6E3C0468" "043E8F66" "3F4860EE"
               "12BF2D5B" "0B7474D6" "E694F91E" "6DCC4024" "FFFFFFFF" "FFFFFFFF",
               16),
    8192: long("FFFFFFFF" "FFFFFFFF" "C90FDAA2" "2168C234" "C4C6628B" "80DC1CD1"
               "29024E08" "8A67CC74" "020BBEA6" "3B139B22" "514A0879" "8E3404DD"
               "EF9519B3" "CD3A431B" "302B0A6D" "F25F1437" "4FE1356D" "6D51C245"
               "E485B576" "625E7EC6" "F44C42E9" "A637ED6B" "0BFF5CB6" "F406B7ED"
               "EE386BFB" "5A899FA5" "AE9F2411" "7C4B1FE6" "49286651" "ECE45B3D"
               "C2007CB8" "A163BF05" "98DA4836" "1C55D39A" "69163FA8" "FD24CF5F"
               "83655D23" "DCA3AD96" "1C62F356" "208552BB" "9ED52907" "7096966D"
               "670C354E" "4ABC9804" "F1746C08" "CA18217C" "32905E46" "2E36CE3B"
               "E39E772C" "180E8603" "9B2783A2" "EC07A28F" "B5C55DF0" "6F4C52C9"
               "DE2BCBF6" "95581718" "3995497C" "EA956AE5" "15D22618" "98FA0510"
               "15728E5A" "8AAAC42D" "AD33170D" "04507A33" "A85521AB" "DF1CBA64"
               "ECFB8504" "58DBEF0A" "8AEA7157" "5D060C7D" "B3970F85" "A6E1E4C7"
               "ABF5AE8C" "DB0933D7" "1E8C94E0" "4A25619D" "CEE3D226" "1AD2EE6B"
               "F12FFA06" "D98A0864" "D8760273" "3EC86A64" "521F2B18" "177B200C"
               "BBE11757" "7A615D6C" "770988C0" "BAD946E2" "08E24FA0" "74E5AB31"
               "43DB5BFC" "E0FD108E" "4B82D120" "A9210801" "1A723C12" "A787E6D7"
               "88719A10" "BDBA5B26" "99C32718" "6AF4E23C" "1A946834" "B6150BDA"
               "2583E9CA" "2AD44CE8" "DBBBC2DB" "04DE8EF9" "2E8EFC14" "1FBECAA6"
               "287C5947" "4E6BC05D" "99B2964F" "A090C3A2" "233BA186" "515BE7ED"
               "1F612970" "CEE2D7AF" "B81BDD76" "2170481C" "D0069127" "D5B05AA9" + \
               "93B4EA98" "8D8FDDC1" "86FFB7DC" "90A6C08F" "4DF435C9" "34028492"
               "36C3FAB4" "D27C7026" "C1D4DCB2" "602646DE" "C9751E76" "3DBA37BD"
               "F8FF9406" "AD9E530E" "E5DB382F" "413001AE" "B06A53ED" "9027D831"
               "179727B0" "865A8918" "DA3EDBEB" "CF9B14ED" "44CE6CBA" "CED4BB1B"
               "DB7F1447" "E6CC254B" "33205151" "2BD7AF42" "6FB8F401" "378CD2BF"
               "5983CA01" "C64B92EC" "F032EA15" "D1721D03" "F482D7CE" "6E74FEF6"
               "D55E702F" "46980C82" "B5A84031" "900B1C9E" "59E7C97F" "BEC7E8F3"
               "23A97A7E" "36CC88BE" "0F1D45B7" "FF585AC5" "4BD407B2" "2B4154AA"
               "CC8F6D7E" "BF48E1D8" "14CC5ED2" "0F8037E0" "A79715EE" "F29BE328"
               "06A1D58B" "B7C5DA76" "F550AA3D" "8A1FBFF0" "EB19CCB1" "A313D55C"
               "DA56C9EC" "2EF29632" "387FE8D7" "6E3C0468" "043E8F66" "3F4860EE"
               "12BF2D5B" "0B7474D6" "E694F91E" "6DBE1159" "74A3926F" "12FEE5E4"
               "38777CB6" "A932DF8C" "D8BEC4D0" "73B931BA" "3BC832B6" "8D9DD300"
               "741FA7BF" "8AFC47ED" "2576F693" "6BA42466" "3AAB639C" "5AE4F568"
               "3423B474" "2BF1C978" "238F16CB" "E39D652D" "E3FDB8BE" "FC848AD9"
               "22222E04" "A4037C07" "13EB57A8" "1A23F0C7" "3473FC64" "6CEA306B"
               "4BCBC886" "2F8385DD" "FA9D4B7F" "A2C087E8" "79683303" "ED5BDD3A"
               "062B3CF5" "B3A278A6" "6D2A13F8" "3F44F82D" "DF310EE0" "74AB6A36"
               "4597E899" "A0255DC1" "64F31CC5" "0846851D" "F9AB4819" "5DED7EA1"
               "B1D510BD" "7EE74D73" "FAF36BC3" "1ECFA268" "359046F4" "EB879F92"
               "4009438B" "481C6CD7" "889A002E" "D5EE382B" "C9190DA6" "FC026E47"
               "9558E447" "5677E9AA" "9E3050E2" "765694DF" "C81F56E8" "80B96E71"
               "60C980DD" "98EDD3DF" "FFFFFFFF" "FFFFFFFF",
               16),
    }
#@-node:hardPrimes
#@-others
#@-node:globals
#@+node:class dh
cdef class dh:
    """
    class for Diffie-Hellman secure session key exchange<br>
    <br>
    Constructor arguments:<ul>
    <li>None</li></ul>
    """
    #@    << class dh declarations >>
    #@nl
    #@    @+others
    #@+node:C vars
    # hold the C-level DH structure
    cdef DH *myDH
    
    #@-node:C vars
    #@+node:__new__
    def __new__(self):
        self.myDH = NULL
        pass
    #@-node:__new__
    #@+node:__dealloc__
    def __dealloc__(self):
        "Destructor"
        if self.myDH != NULL:
            DH_free(self.myDH)
        pass
    #@-node:__dealloc__
    #@+node:genParms
    def genParms(self, prime_len=1024, generator=5, myCallback=None):
        """
        <b>generateParms</b><br>
        <br>
        Generate random modulus for DH key generation<br>
        <br>
        Arguments:<ul>
         <li><b>prime_len</b> - length of modulus (n) in bits, default 1024</li>
         <li><b>generator</b> - g, as in pub = g^x mod n. Default 5</li>
         <li><b>myCallback</b> - a python func for status callbacks. Must accept 2 args,
           'type' and 'num'. 'type' is a number from 0 to 3. Default is no callback</li>
        </ul>
        Returns:<ul>
         <li>None</li></ul>
        """
        if self.myDH != NULL:
            DH_free(self.myDH)
        self.myDH = DH_generate_parameters(prime_len,
                                           generator,
                                           callback, <void *>myCallback)
        callback(-1, 0, <void *>myCallback)
        return (bn2pyLong(self.myDH.g), bn2pyLong(self.myDH.p))
    #@-node:genParms
    #@+node:importParms
    def importParms(self, generator, modulus=None):
        """
        <b>importParms</b><br>
        <br>
        Imports DH key generation parameters into this DH object.<br>
        <br>
        Arguments - choice of:<ul>
         <li><b>tuple</b> - (generator, modulus)<br>
             <b>OR:</b><br><li>
         <li><b>generator</b></li>
         <li><b>modulus</b> (as separate arguments)</ul>
    
        Returns:<ul>
         <li>None</li></ul>
        """
    
        self.myDH = DH_new()
    
        if type(generator) is type(()):
            modulus = generator[1]
            generator = generator[0]
        self.myDH.g = pyLong2bn(generator)
        self.myDH.p = pyLong2bn(modulus)
    #@-node:importParms
    #@+node:exportParms
    def exportParms(self):
        """
        <b>exportParms</b><br>
        <br>
        Returns the DH key generation parameters required for a peer to generate pub keys<br>
        <br>
        Arguments:<ul>
         <li><b>None</b></li>
        </ul>
        Returns:<ul>
         <li><b>tuple</b> - (generator, modulus)</ul>
        """
        return (bn2pyLong(self.myDH.g),
                bn2pyLong(self.myDH.p))
    #@-node:exportParms
    #@+node:genPubKey
    def genPubKey(self):
        """
        <b>genPubKey</b><br>
        <br>
        Generates a public key from generated (or imported) parameters<br>
        <br>
        Arguments:<ul>
         <li>None</li>
         </ul>
        Returns:<ul>
         <li><b>public key</b>, as a long int
         </ul>
        """
        if self.myDH == NULL:
            raise Exception("Can't generate public key: DH object has no generation parameters")
    
        DH_generate_key(self.myDH)
        return bn2pyLong(self.myDH.pub_key)
    #@-node:genPubKey
    #@+node:privKey
    def privKey(self):
        """
        <b>privKey</b><br>
        <br>
        Returns the private key as a long int
        """
        if self.myDH == NULL:
            raise Exception("DH object has no public or private keys")
        return bn2pyLong(self.myDH.priv_key)
    #@-node:privKey
    #@+node:genSessKey
    def genSessKey(self, peerKey):
        """
        <br>genSessKey</b><br><br>
    
        Calculates and returns the session key.<br><br>
    
        Arguments:<ul>
          <li><b>peerKey</b> - peer's public key, as returned from <b>genPubKey</b></li>
          </ul>
        Returns:<ul>
          <li>None</li>
          </ul>
        """
        if self.myDH == NULL:
            raise Exception("Can't generate public key: DH object has no generation parameters")
        if self.myDH.pub_key == NULL:
            raise Exception("Can't generate session key: DH object has no public key")
    
        cdef keySize
        cdef BIGNUM *peerKeyBN
        cdef char *key
    
        peerKeyBN = pyLong2bn(peerKey)
        keySize = DH_size(self.myDH)
        key = <char *>malloc(keySize)
    
        DH_compute_key(<unsigned char *>key, peerKeyBN, self.myDH)
    
        BN_free(peerKeyBN)
        s = PyString_FromStringAndSize(key, keySize)
        free(key)
    
        return s
    #@-node:genSessKey
    #@-others
#@-node:class dh
#@+node:class blowfish
cdef class blowfish:
    """
    class for Blowfish stream encryption<br>
    <br>
    Constructor Arguments:<ul>
    <li><b>key</b> - string containing encryption key, <i>default None</i></li>
    <li><b>iv</b> - 8-byte string for initial value (for chaining), default None</li>
    </ul>
    """
    #@    @+others
    #@+node:C vars
    cdef BF_KEY keyStruct
    cdef char iv[8]
    cdef int counter
    cdef char *key
    cdef int keylen
    #@-node:C vars
    #@+node:__new__
    def __new__(self, key=None, iv=None):
    
        # set key and IV
        self.setKey(key)
        self.setIV(iv)
    
        # initialise C attributes
        self.counter = 0
    #@-node:__new__
    #@+node:__dealloc__
    def __dealloc__(self):
        pass
    
    #@-node:__dealloc__
    #@+node:setKey
    def setKey(self, key, iv=None):
        """
        <b>setKey</b><br><br>
    
        Sets the session key for the Blowfish cipher object.<br><br>
    
        Arguments:<ul>
          <li><b>key</b> - blowfish encryption key, as a string. For acceptable
              security, this should be at least 16 characters</li>
          <li><b>iv</b> - cipher <b>initial value</b>, as used in the chaining feedback.<br>
              Not essential, default nothing</li>
          </ul>
        """
        cdef char *keyBuf
        # Validate key
        if key == None:
            self.key = <char *>0
        else:
            self.keylen = len(key)
            if self.keylen == 0:
                raise Exception("Key length is zero")
            else:
                self.key = <char *>malloc(self.keylen)
                keyBuf = key
                memcpy(self.key, keyBuf, self.keylen)
    
        # reset IV - it won't work now
        self.setIV(iv)
    #@-node:setKey
    #@+node:setIV
    def setIV(self, iv=None):
        """
        <b>setIV</b> - sets the cipher chaining <b>initial value</b><br><br>
    
        Arguments:<ul>
          <li><b>iv</b> - 8-byte initial value, as a python string</li>
          </ul>
        Returns:<ul>
          <li>None</li>
          </ul>
        """
        cdef char *ivBuf
        # Validate IV
        if iv == None:
            memset(self.iv, 0, 8)
        elif len(iv) != 8:
            raise Exception(
                "Blowfish IV length=%d, should be 8" % len(iv))
        else:
            ivBuf = iv
            memcpy(self.iv, ivBuf, 8)
    
        self.counter = 0
        BF_set_key(&(self.keyStruct), self.keylen, self.key)
    #@-node:setIV
    #@+node:encrypt
    def encrypt(self, inbuf):
        """
        <b>encrypt</b> - encrypt a block of data<br><br>
    
        Arguments:<ul>
          <li><b>inbuf</b> - plaintext to be encrypted, as a string</li>
          </ul>
    
        Returns:<ul>
          <li>Encrypted ciphertext, as a string</li>
          </ul>
        """
        cdef char *inbufC
        cdef char *outbufC
        cdef int blklen
    
        inbufC = inbuf
        blklen = len(inbuf)
        #printf("blk len=%d\n", blklen)
        outbufC = <char *>malloc(blklen)
        #printf("out=%lx\n", outbufC)
    
        BF_cfb64_encrypt(inbufC,
                         outbufC, blklen, &self.keyStruct,
                         self.iv, &self.counter, BF_ENCRYPT)
        outbuf = PyString_FromStringAndSize(outbufC, blklen)
        free(outbufC)
        return outbuf
    #@-node:encrypt
    #@+node:decrypt
    def decrypt(self, inbuf):
        """
        <b>decrypt</b> - decrypt a block of data<br><br>
    
        Arguments:<ul>
          <li><b>inbuf</b> - ciphertext to be decrypted, as a string</li>
          </ul>
    
        Returns:<ul>
          <li>Decrypted plaintext, as a string</li>
          </ul>
        """
        cdef char *inbufC
        cdef char *outbufC
        cdef int blklen
    
        inbufC = inbuf
        blklen = len(inbuf)
        outbufC = <char *>malloc(blklen)
            
        BF_cfb64_encrypt(inbufC,
                         outbufC, blklen, &self.keyStruct,
                         self.iv, &self.counter, BF_DECRYPT)
        outbuf = PyString_FromStringAndSize(outbufC, blklen)
        free(outbufC)
        return outbuf
    #@-node:decrypt
    #@-others
#@-node:class blowfish
#@+node:class rsa
cdef class rsa:
    """
    class for RSA public-key encryption<br><br>

    Constructor Arguments:<ul>
    <li>None</li>
    </ul>
    """
    #@    @+others
    #@+node:C attributes
    cdef RSA *rsaObj
    cdef BN_CTX *bnCtx
    #@-node:C attributes
    #@+node:__new__
    def __new__(self):
        cdef RSA *rsaC
        rsaC = RSA_new()
        self.rsaObj = rsaC
        self.bnCtx = BN_CTX_new()
        BN_CTX_init(self.bnCtx)
    #@-node:__new__
    #@+node:__dealloc__
    def __dealloc__(self):
        #print "dealloc'ing rsa object"
        BN_CTX_free(self.bnCtx)
        RSA_free(self.rsaObj)
    #@-node:__dealloc__
    #@+node:generateKey
    def generateKey(self, nbits=1024, **kw):
        """
        Generate a fresh RSA keypair<br><br>
    
        Arguments:
         - bits</b> - length of required key in bits, >=1024 recommended, default 1024
    
        Keywords:
         - e - exponent for key generation, default 5
         - myCallback - a Python callback function which accepts
           2 arguments - level and num. Default None
        """
    
        e = kw.get('e', 5)
        myCallback = kw.get('myCallback', None)
    
        # ditch any old key info
        if self.rsaObj != NULL:
            RSA_free(self.rsaObj)
    
        # cook up a new key
        self.rsaObj = RSA_generate_key(nbits, e, callback, <void *>myCallback)
    
        # and turn on blinding
        RSA_blinding_on(self.rsaObj, self.bnCtx)
    
        callback(-1, 0, <void *>myCallback)
    #@-node:generateKey
    #@+node:pubKey
    def pubKey(self):
        """
        Returns the public key for this object.<br><br>
    
        Arguments:<ul>
         <li>None</li>
         </ul>
    
        Returns:<ul>
         <li><b>Public key</b>, as a tuple (e, n) (refer: Applied Cryptography)</li>
         </ul>
        """
    
        return (bn2pyLong(self.rsaObj.e),
                bn2pyLong(self.rsaObj.n),
                )
    #@-node:pubKey
    #@+node:privKey
    def privKey(self):
        """
        Returns the public key for this object.<br><br>
    
        Arguments:<ul>
        <li>None</li>
        </ul>
    
        Returns:<ul>
        <li><b>Public and Private key</b>, as a tuple (e, n, d) (refer: Applied Cryptography)</li>
        </ul>
        """
    
        return (bn2pyLong(self.rsaObj.e),
                bn2pyLong(self.rsaObj.n),
                bn2pyLong(self.rsaObj.d),
                )
    #@-node:privKey
    #@+node:rawprimes()
    def rawprimes(self):
        """
        Returns the p and q for this RSA key.<br><br>
    
        Arguments:<ul>
        <li>None</li>
        </ul>
    
        Returns:<ul>
        <li><b>p and q as a tuple (refer: Applied Cryptography)</li>
        </ul>
        """
    
        return (bn2pyLong(self.rsaObj.p),
                bn2pyLong(self.rsaObj.q),
                )
    #@-node:rawprimes()
    #@+node:importPubKey
    def importPubKey(self, parms):
        """
        Import someone else's public key
    
        Arguments:
          - (e, n), a tuple (as returned by pubKey()),
        """
    
        if type(parms) not in [type(()), type([])]:
            raise Exception("Invalid public key parameters")
        e = parms[0]
        n = parms[1]
    
        #print "e = %s" % e
        #print "n = %s" % n
    
        self.rsaObj.e = pyLong2bn(e)
        self.rsaObj.n = pyLong2bn(n)
        self.rsaObj.d = <BIGNUM *>0
    
        # and turn on blinding
        RSA_blinding_on(self.rsaObj, self.bnCtx)
    
    #@-node:importPubKey
    #@+node:importPrivKey
    def importPrivKey(self, parms):
        """
        importPubKey - import private key parameters
    
        Arguments:
         - (e, n, d), as a tuple (as returned by privKey())
        """
    
        if type(parms) not in [type(()), type([])]:
            raise Exception("Invalid private key parameters")
        e = parms[0]
        n = parms[1]
        d = parms[2]
    
        #print "e = %s" % e
        #print "n = %s" % n
        #print "d = %s" % d
    
        self.rsaObj.e = pyLong2bn(e)
        self.rsaObj.n = pyLong2bn(n)
        self.rsaObj.d = pyLong2bn(d)
    
        # and turn on blinding
        RSA_blinding_on(self.rsaObj, self.bnCtx)
    #@-node:importPrivKey
    #@+node:hasPrivateKey
    def hasPrivateKey(self):
        """
        Returns <b>True</b> if private key is present in this key object (and therefore the
        key object is capable of decryption), or <b>False</b> if not.
        """
        if self.rsaObj.d == <BIGNUM *>0:
            return _False
        else:
            return _True
    #@-node:hasPrivateKey
    #@+node:encrypt
    def encrypt(self, pPlain):
        """
        Encrypts a block of data.<br><br>
    
        Arguments:<ul>
        <li><b>plain</b> - plaintext to be encrypted, as a string</li>
        </ul>
    
        Returns:<ul>
        <li><b>ciphertext</b> - encrypted data, as a string</b></li>
        </ul>
    
        <b>Note:</b> This method has a strict limit as to the size of the block which can
        be encrypted. To determine the limit, call the <b>maxSize</b> method.
        """
        cdef char *cPlain
        cdef int cPlainLen
        cdef char *cCipher
        cdef int cCipherLen
        cdef int cRsaSize
    
        cRsaSize = RSA_size(self.rsaObj)
    
        cPlain = pPlain
        cPlainLen = len(pPlain)
    
        # spit if plaintext is too long
        #if cPlainLen > cRsaSize - 11:
        if cPlainLen > self.maxSize():
            raise Exception("Plaintext exceeds maximum %d bytes" % self.maxSize())
    
        cCipher = <char *>malloc(cRsaSize)
    
        cCipherLen = RSA_public_encrypt(cPlainLen,
                                        <unsigned char *>cPlain,
                                        <unsigned char *>cCipher,
                                        self.rsaObj,
                                        RSA_PKCS1_PADDING)
        pCipher = PyString_FromStringAndSize(cCipher, cCipherLen)
    
        free(<char *>cCipher)
        return pCipher
    #@-node:encrypt
    #@+node:decrypt
    def decrypt(self, pCipher):
        """
        decrypt a previously encrypted block<br><br>
    
        Arguments:<ul>
        <li><b>cipher</b> - ciphertext to be decrypted, as a string</li>
        </ul>
        
        Returns:<ul>
        <li><b>plaintext</b> - decrypted asa python string</li>
        </ul>
        """
        cdef char *cPlain
        cdef int cPlainLen
        cdef char *cCipher
        cdef int cCipherLen
    
        cCipher = pCipher
        cCipherLen = len(pCipher)
    
        cPlain = <char *>malloc(RSA_size(self.rsaObj))
    
        if self.rsaObj.d == <BIGNUM *>0:
            raise CryptoCannotDecrypt("Private key not present")
    
        cPlainLen = RSA_private_decrypt(cCipherLen,
                                        <unsigned char *>cCipher,
                                        <unsigned char *>cPlain,
                                        self.rsaObj,
                                        RSA_PKCS1_PADDING)
        pPlain = PyString_FromStringAndSize(cPlain, cPlainLen)
    
        free(<char *>cPlain)
        return pPlain
    #@-node:decrypt
    #@+node:maxSize
    def maxSize(self):
        """
        Returns the maximum allowable size of plaintext strings which can
        be encrypted
        """
    
        return RSA_size(self.rsaObj) - 11
    #@-node:maxSize
    #@+node:sign
    def sign(self, digest):
        """
        Signs an MD5 message digest<br><br>
    
        Arguments:<ul>
        <li><b>digest</b> - MD5 digest of data, as a string</li>
        </ul>
    
        Returns:<ul>
        <li><b>signature</b>, as a MIME-friendly, base64-encoded string</li>
        </ul>
        """
        cdef char *cSigBuf
        cdef int cSigLen
        cdef char *cDigestBuf
        cdef int  cDigestLen
    
        cSigBuf = <char *>malloc(RSA_size(self.rsaObj))
        cDigestBuf = digest
        cDigestLen = len(digest)
    
        RSA_sign(NID_md5,
                 <unsigned char *>cDigestBuf, <unsigned int>cDigestLen,
                 <unsigned char *>cSigBuf, <unsigned int *>&cSigLen,
                 self.rsaObj)
    
        pSig = PyString_FromStringAndSize(cSigBuf, cSigLen)
        free(cSigBuf)
        return pSig
    #@-node:sign
    #@+node:verify
    def verify(self, digest, sig):
        """
        Verify a digest against a signature<li><li>
    
        Arguments:<ul>
        <li><b>digest</b> - digest against which signature is to be checked.</b></li>
        <li><b>signature</b> - signature as returned from the <b>sign()</b> method</li>
        </ul>
    
        Returns:<ul>
        <li><b>True</b> if signature is valid, or <b>False</b> if not.</li>
        </ul>
    
        Note:<ul>
        <li>To pass as valid, the key object must contain the same public key as was used
            in creating the original signature</li>
        </ul>
        """
        cdef char *cSigBuf
        cdef int cSigLen
        cdef char *cDigestBuf
        cdef int  cDigestLen
        cdef int result
    
        cSigBuf = sig
        cSigLen = len(sig)
        cDigestBuf = digest
        cDigestLen = len(digest)
    
        result = RSA_verify(NID_md5,
                          <unsigned char *>cDigestBuf, <unsigned int>cDigestLen,
                          <unsigned char *>cSigBuf, <unsigned int>cSigLen,
                          self.rsaObj)
        return result
    #@-node:verify
    #@-others
#@-node:class rsa
#@+node:class ElGamal
class ElGamal:
    #@    @+others
    #@+node:attribs
    keydata=['p', 'g', 'y', 'x']
    
    #@-node:attribs
    #@+node:__init__
    def __init__(self, bits=None, progressfunc=None):
        if bits:
            self.keysize = bits
            self.generateKey(bits, progressfunc)
        pass
    #@-node:__init__
    #@+node:generateKey
    # Generate an ElGamal key with N bits
    def generateKey(self, bits, progress_func=None):
        """generate(bits:int, randfunc:callable, progress_func:callable)
    
        Generate an ElGamal key of length 'bits', using 'randfunc' to get
        random data and 'progress_func', if present, to display
        the progress of the key generation.
        """
    
        self.keysize = bits
    
        # Generate prime p
        if 0:
            if progress_func:
                progress_func('p\n')
            #obj.p=bignum(getPrime(bits, randfunc))
            self.p = genprime(bits)
        else:
            if hardPrimes.has_key(bits):
                self.p = hardPrimes[bits]
            else:
                self.p = genprime(bits)
    
        # Generate random number g
        if progress_func:
            progress_func('g\n')
    
        size = bits - 1 - (ord(rndfunc(1)) & 63) # g will be from 1--64 bits smaller than p
    
        if size<1:
            size=bits-1
        while (1):
            # self.g = genprime(size, None, 0)
            self.g = genprime(bits/2, progress_func, 0)
            #print "----"
            #print self.g
            #print self.p
            if self.g < self.p:
                break
            size=(size+1) % bits
            if size==0:
                size=4
        # Generate random number x
        if progress_func:
            progress_func('x\n')
    
        #while (1):
        #    size=bits-1-ord(rndfunc(1)) # x will be from 1 to 256 bits smaller than p
        #    if size>2:
        #        break
        
        #while (1):
        #    #self.x = SSLCrypto.genprime(size)
        #    self.x = genprime(size/2, progress_func, 0)
        #    if self.x < self.p:
        #        break
        #    size = (size+1) % bits
        #    if size==0:
        #        size=4
        self.x = genrandom(bits-16)
        if progress_func:
            progress_func('y\n')
        self.y = pow(self.g, self.x, self.p)
    #@-node:generateKey
    #@+node:genk
    def genk(self):
        """
        Generate a 'K' value for signing/encrypting
        """
        while 1:
            K = genrandom(self.keysize - 1 - genrandom(5,1))
            if GCD(K, self.p - 1) == 1:
    
                #print "sign: p = %s" % self.p
                #print "sign: k = %s" % K
    
                return K
    
    #@-node:genk
    #@+node:construct
    def construct(tuple):
        """construct(tuple:(long,long,long,long)|(long,long,long,long,long)))
                 : ElGamalobj
        Construct an ElGamal key from a 3- or 4-tuple of numbers.
        """
    
        obj=ElGamalobj()
        if len(tuple) not in [3,4]:
            raise error, 'argument for construct() wrong length'
        for i in range(len(tuple)):
            field = obj.keydata[i]
            setattr(obj, field, tuple[i])
        return obj
    #@-node:construct
    #@+node:__getstate__
    def __getstate__(self):
        """To keep key objects platform-independent, the key data is
        converted to standard Python long integers before being
        written out.  It will then be reconverted as necessary on
        restoration."""
        d=self.__dict__
        for key in self.keydata:
            if d.has_key(key): d[key]=long(d[key])
        return d
    #@-node:__getstate__
    #@+node:__setstate__
    def __setstate__(self, d):
        """On unpickling a key object, the key data is converted to the big
    number representation being used, whether that is Python long
    integers, MPZ objects, or whatever."""
        for key in self.keydata:
            if d.has_key(key): self.__dict__[key]=bignum(d[key])
    #@-node:__setstate__
    #@+node:encrypt
    def encrypt(self, plaintext, K=None):
        """encrypt(plaintext:string|long, K:string|long) : tuple
        Encrypt the string or integer plaintext.  K is a random
        parameter required by some algorithms.
        """
        wasString=0
    
        if K is None:
            K = genprime(256)
    
        if isinstance(plaintext, types.StringType):
            plaintext = bytes_to_long(plaintext)
            wasString=1
    
        if isinstance(K, types.StringType):
            K = bytes_to_long(K)
    
        a = pow(self.g, K, self.p)
        b = (plaintext * pow(self.y, K, self.p)) % self.p
        ciphertext = (a, b)
    
        if wasString:
            ctext1 = tuple(map(long_to_bytes, ciphertext))
        else:
            ctext1 = ciphertext
    
        return bencode(ctext1)
    #@-node:encrypt
    #@+node:decrypt
    def decrypt(self, ciphertext):
        """decrypt(ciphertext:tuple|string|long): string
        Decrypt 'ciphertext' using this key.
        """
        try:
            ciphertext = bdecode(ciphertext)
        except:
            raise CryptoInvalidCiphertext
    
        wasString = 0
    
        if type(ciphertext) not in [type(()), type([])]:
            ciphertext = (ciphertext,)
    
        if isinstance(ciphertext[0], types.StringType):
            ciphertext = tuple(map(bytes_to_long, ciphertext))
            wasString = 1
    
        if (not hasattr(self, 'x')):
            raise error, 'Private key not available in this object'
    
        ax = pow(ciphertext[0], self.x, self.p)
        plaintext = (ciphertext[1] * inverse(ax, self.p)) % self.p
    
        if wasString:
            return long_to_bytes(plaintext)
        else:
            return plaintext
    #@-node:decrypt
    #@+node:sign
    def sign(self, M, K=None):
        """sign(M : string|long, K:string|long) : tuple
        Return a tuple containing the signature for the message M.
        K is a random parameter required by some algorithms.
        """
        if (not self.hasPrivateKey()):
            raise error, 'Private key not available in this object'
        if isinstance(M, types.StringType): M=bytes_to_long(M)
    
        #if isinstance(K, types.StringType): K=bytes_to_long(K)
        if K is None:
            K = self.genk()
    
        #print "sign: p = %s" % self.p
        #print "sign: k = %s" % K
        
        p1 = self.p - 1
        if (GCD(K, p1) != 1):
            raise CryptoInvalidElGamalK("Bad K value: GCD(K, p-1) != 1")
        a = pow(self.g, K, self.p)
        t = (M - self.x * a) % p1
        while t < 0:
            t=t+p1
        b = (t * inverse(K, p1)) % p1
    
        return bencode((a, b))
    
    
    #@-node:sign
    #@+node:verify
    def verify(self, M, sig):
        """verify(M:string|long, signature:tuple) : bool
        Verify that the signature is valid for the message M;
        returns true if the signature checks out.
        """
        try:
            sig = bdecode(sig)
        except:
            raise CryptoInvalidSignature
    
        if isinstance(M, types.StringType):
            M = bytes_to_long(M)
    
        v1 = pow(self.y, sig[0], self.p)
        v1 = (v1 * pow(sig[0], sig[1], self.p)) % self.p
        v2 = pow(self.g, M, self.p)
        if v1 == v2:
            return 1
        return 0
    #@-node:verify
    #@+node:importPubKey
    def importPubKey(self, parms):
        """
        Import someone else's public key
    
        Arguments:
         - (p, g, y), a tuple (as returned by pubKey())
        """
    
        if (type(parms) not in [type(()), type([])]) or (len(parms) != 3):
            raise Exception("Invalid public key parameters")
        self.p = parms[0]
        self.g = parms[1]
        self.y = parms[2]
    
        self.keysize = size(self.p)
    
        #print "p = %s" % self.p
        #print "g = %s" % self.g
        #print "y = %s" % self.y
    
    
    #@-node:importPubKey
    #@+node:importPrivKey
    def importPrivKey(self, parms):
        """
        importPubKey - import private key parameters
    
        Arguments:
         - (e, n, d), as a tuple (as returned by privKey())
        """
    
        if (type(parms) not in [type(()), type([])]) or (len(parms) != 4):
            raise Exception("Invalid private key parameters")
        self.p = parms[0]
        self.g = parms[1]
        self.y = parms[2]
        self.x = parms[3]
    
        self.keysize = size(self.p)
    
        #print "p = %s" % self.p
        #print "g = %s" % self.g
        #print "y = %s" % self.y
        #print "x = %s" % self.x
    
    
    #@-node:importPrivKey
    #@+node:hasPrivateKey
    def hasPrivateKey(self):
        """
        Returns <b>True</b> if private key is present in this key object (and therefore the
        key object is capable of decryption), or <b>False</b> if not.
        """
        if hasattr(self, 'x'):
            return _True
        else:
            return _False
    #@-node:hasPrivateKey
    #@+node:pubKey
    def pubKey(self):
        """Return tuple containing only the public information."""
        return (self.p, self.g, self.y)
    #@-node:pubKey
    #@+node:privkey
    def privKey(self):
        """Return tuple containing the public and private information."""
        return (self.p, self.g, self.y, self.x)
    #@-node:privkey
    #@+node:maxSize
    def maxSize(self):
        """
        Returns the maximum allowable size of plaintext strings which can
        be encrypted
        """
    
        #print "WARNING: ElGamal.maxSize() not implemented"
        return 128
    #@-node:maxSize
    #@+node:__eq__
    def __eq__ (self, other):
        """__eq__(other): 0, 1
        Compare us to other for equality.
        """
        return self.__getstate__() == other.__getstate__()
    #@-node:__eq__
    #@-others
#@-node:class ElGamal
#@+node:class key
class key:
    """
    key - simple crypto API object.<br><br>
    
    This may well be the only crypto class for Python that you'll ever need.
    Think of this class, and the ezPyCrypto module, as 'cryptography for
    the rest of us'.<br><br>
    
    Designed to strike the optimal balance between ease of use, features
    and performance.<br><br>
    
    Basic High-level methods:
    <ul>
     <li>encString - encrypt a string</li>
     <li>decString - decrypt a string</li>
     <li>encStringToAscii - encrypt a string to a printable, mailable format</li>
     <li>decStringFromAscii - decrypt an ascii-format encrypted string<br><br></li>

     <li>signString - produce ascii-format signature of a string</li>
     <li>verifyString - verify a string against a signature<br><br></li>

     <li>importKey - import public key (and possibly private key too)</li>
     <li>exportKey - export public key only, as printable mailable string</li>
     <li>exportKeyPrivate - same, but export private key as well</li>
     <li>makeNewKeys - generate a new, random private/public key pair</li>
    </ul>

    Middle-level (stream-oriented) methods:
    <ul>
     <li>encStart - start a stream encryption session</li>
     <li>encNext - encrypt another piece of data</li>
     <li>encEnd} - finalise stream encryption session</li>
     <li>decStart} - start a stream decryption session</li>
     <li>decNext} - decrypt the next piece of available data</li>
     <li>decEnd} - finalise stream decryption session</li>
     </ul>

    Low-level methods:<ul>
    <li>refer to the source code</li>
    </ul>
    """

#Principle of operation:
#     - Data is encrypted with choice of symmetric block-mode session cipher
#       (or default Blowfish if user doesn't care)
#     - CFB block chaining is used for added security - each next block's
#       key is affected by the previous block
#     - The session key and initial value (IV) are encrypted against an RSA
#       or ElGamal public key (user's choice, default RSA)
#     - Each block in the stream is prepended with a 'length' byte, indicating
#       how many bytes in the decrypted block are significant - needed when
#       total data len mod block size is non-zero
#     - Format of encrypted data is:
#         - public key len - 2 bytes, little-endian - size of public key in bytes
#        - public key - public key of recipient
#        - block cipher len - unencrypted length byte - size of block cipher in bytes
#        - block cipher - encrypted against public key, index into array
#          of session algorithms
#        - block key len - unencrypted length byte - size of block key in bytes
#        - block key - encrypted against public key
#        - block IV len - unencrypted length of block cipher IV - IV length in bytes
#        - block cipher IV - encrypted against public key, prefixed 1-byte length
#        - block1 len - 1 byte - number of significant chars in block1 *
#        - block1 data - always 8 bytes, encrypted against session key
#        - ...
#        - blockn len
#        - blockn data
#        - If last data block is of the same size as the session cipher blocksize,
#          a final byte 0x00 is sent.

    #@    << class key declarations >>
    #@+node:<< class key declarations >>
    # Various lookup tables for encryption algorithms
    
    _algosPub = {'RSA' : rsa,
                 'ElGamal' : ElGamal}
    
    _algosPub1 = {rsa : 'RSA',
                  ElGamal : 'ElGamal'}
    
    _algosSes = { "Blowfish":blowfish,
                  }
    _algosSes1 = {'Blowfish':0}
    
    _algosSes2 = [blowfish]
    
    _algosSes3 = {blowfish:'Blowfish'}
    
    # Generate IV for passphrase encryption
    _passIV = "w@8.~Z4("
    
    # Buffer for yet-to-be-encrypted stream data
    _encBuf = ''
    #@-node:<< class key declarations >>
    #@nl
    #@    @+others
    #@+node:__init__
    def __init__(self, something=1024, algoPub=None, algoSess=None, **kw):
        """Constructor. Creates a key object<br><br>
    
        This constructor, when creating the key object, does one of
        two things:
         - Creates a completely new keypair, OR
         - Imports an existing keypair
    
        Arguments:
         - If new keys are desired:
           - key size in bits (int), default 1024 - advise 2048 or more
           - algoPub - only 'RSA' (default) or 'ElGamal' presently supported
           - algoSess - only 'Blowfish' (default) presently supported
    
         - If importing an existing key or keypair:
            - keyobj (string) - result of a prior exportKey() call
        
        Keywords:
         - passphrase - default '':
            - If creating new keypair, this passphrase is used to encrypt privkey when
              exporting.
            - If importing a new keypair, the passphrase is used to authenticate and
              grant/deny access to private key
        """
        #print "key constructor called"
        if type(something) in [types.IntType, types.LongType]:
        #if type(something) == type(0):
            # which public key algorithm did they choose?
            if algoPub == None:
                algoPub = 'RSA'
            algoP = self._algosPub.get(algoPub, None)
            if algoP == None:
                # Whoops - don't know that one
                raise Exception("AlgoPub must be one of: " + ",".join(self._algosPub.keys()))
            self.algoPub = algoP
            self.algoPname = algoPub
            self.callback = kw.get('myCallback', None)
    
            # which session key algorithm?
            if algoSess == None:
                algoSess = 'Blowfish'
            algoS = self._algosSes.get(algoSess, None)
            if algoS == None:
                # Whoops - don't know that session algorithm
                raise Exception(
    	      "AlgoSess must be one of %s" % self._aldosSes.keys()) 
            self.algoSes = algoS
            self.algoSname = algoSess
    
            # organise random data pool
    
            # now create the keypair
            self.makeNewKeys(something, **kw)
    
        elif type(something) is type(""):
            if algoPub != None:
                raise Exception("Don't specify algoPub if importing a key")
            if self.importKey(something, passphrase=kw.get('passphrase', '')) == _False:
                raise CryptoKeyError(
                    "Attempted to import invalid key, or passphrase is bad")
        else:
            raise Exception("Must pass keysize or importable keys")
    #@-node:__init__
    #@+node:makeNewKeys()
    def makeNewKeys(self, keysize=512, **kw):
        """
        Creates a new keypair in cipher object, and a new session key
    
        Arguments:
          - keysize (default 512), advise at least 1536
    
        Returns:
          - None
    
        Keywords:
          - passphrase - used to secure exported private key - default '' (no passphrase)
          - e - e value, in case of RSA
        </ul>
    
        Keypair gets stored within the key object. Refer exportKey()
        exportKeyPrivate() and importKey()
        
        Generally no need to call this yourself, since the constructor
        calls this in cases where you aren't instantiating with an
        importable key.
        """
    
        passphrase = kw.get('passphrase', '')
        if passphrase == None:
            passphrase = ''
        self.passphrase = passphrase
    
        # set up a public key object
        self.k = self.algoPub()
        self.k.generateKey(keysize, **kw)
    
        #self.k = self.algoPub.generate(keysize, self.randfunc)
        self._calcPubBlkSize()
    
        # Generate random session key
        self._genNewSessKey()
    
        # Create a new block cipher object
        self._initBlkCipher()
    #@-node:makeNewKeys()
    #@+node:importKey()
    def importKey(self, keystring, **kwds):
        """
        Imports a public key or private/public key pair.<br><br>
    
        (as previously exported from this object
        with the <b>exportKey</b> or <b>exportKeyPrivate</b> methods.<br><br>
    
        Arguments:<ul>
        <li><b>keystring</b> - a string previously imported with
        <b>exportKey</b> or <b>exportKeyPrivate</b></li>
        </ul>
        
        Keywords:<ul>
        <li><b>passphrase</b> - string (default '', meaning 'try to import without passphrase')
        </li></ul>
        
        Returns:<ul>
        <li><b>True</b> if import successful, <b>False</b> if failed<li>
        </ul>
    
        You don't have to call this if you instantiate your key object
        in 'import' mode - ie, by calling it with a previously exported key.<br><br>
    
        Note - you shouldn't give a 'passphrase' when importing a public key.
        """
    
        passphrase = kwds.get('passphrase', '')
        if passphrase == None:
            passphrase = ''
    
        try:
            keypickle = self._unwrap("Key", keystring)
    
            #keytuple = pickle.loads(keypickle)
            keytuple = bdecode(keypickle)
    
            ispriv, haspass, keyobj = keytuple
    
            if haspass:
                ppCipher = blowfish(passphrase, self._passIV)
                keyobj = ppCipher.decrypt(keyobj)
    
            #self.algoPname, keyparms = pickle.loads(keyobj)
            self.algoPname, keyparms = bdecode(keyobj)
    
            self.algoPub = self._algosPub[self.algoPname]
    
            # Create empty public key object
            self.k = self.algoPub()
            if ispriv:
                self.k.importPrivKey(keyparms)
            else:
                self.k.importPubKey(keyparms)
    
            #raise Exception("Tried to import Invalid Key")
            self._calcPubBlkSize()
            self.passphrase = passphrase
    
            # create new session key
            algoSess = 'Blowfish'
            algoS = self._algosSes.get(algoSess, None)
            if algoS == None:
                # Whoops - don't know that session algorithm
                raise Exception(
    	      "AlgoSess must be one of %s" % self._aldosSes.keys()) 
            self.algoSes = algoS
            self.algoSname = algoSess
    
            
            return _True
        except:
            traceback.print_exc()
            return _False
    #@-node:importKey()
    #@+node:exportKey()
    def exportKey(self):
        """
        Exports the public key as a printable string.<br><br>
    
        Exported keys can be imported elsewhere into MyCipher instances
        with the <b>importKey</b> method.<br><br>
        
        Note that this object contains only the public key. If you want to
        export the private key as well, call <b>exportKeyPrivate</b> instaead.<br><br>
    
        Note also that the exported string is Base64-encoded, and safe for sending
        in email.<br><br>
    
        Arguments:<ul>
        <li>None</li>
        </ul>
        Returns:<ul>
        <li><b>base64-encoded string</b> containing an importable key</li>
        </ul>
        """
        rawpub = self._rawPubKey()
        # tuple format: (isprivate haspassphrase keyparameters)
        expTuple = (_False, _False, rawpub)
    
        #expPickle = pickle.dumps(expTuple, _True)
        expPickle = bencode(expTuple)
    
        return self._wrap("Key", expPickle)
    #@-node:exportKey()
    #@+node:exportKeyPrivate()
    def exportKeyPrivate(self, **kwds):
        """
        Exports public/private key pair as a printable string.<br><br>
    
        This string is a binary string consisting of a pickled key object,
        that can be imported elsewhere into <b>key</b> instances
        with the <b>importKey</b> method.<br><br>
        
        Note that this object contains the public AND PRIVATE keys.
        Don't EVER email any keys you export with this function (unless you
        know what you're doing, and you encrypt the exported keys against
        another key). When in doubt, use L{exportKey} instead.<br><br>
    
        Keep your private keys safe at all times. You have been warned.<br><br>
    
        Note also that the exported string is Base64-encoded, and safe for sending
        in email.<br><br>
    
        Arguments:<ul>
        <li>None</li>
        </ul>
        
        Keywords:<ul>
        <li><b>passphrase</b> - default (None) to using existing passphrase.
        Set to '' to export without passphrase (if this is really what you want to do!)</li>
        </ul>
        Returns:<ul>
        <li>A base64-encoded string containing an importable key, or None if there is no
        private key available.</li>
        </ul>
        """
    
        passphrase = kwds.get('passphrase', None)
        if passphrase == None:
            passphrase = self.passphrase
    
        rawpriv = self._rawPrivKey()
        if not rawpriv:
            return None
    
        # tuple format: (isprivate haspassphrase keyparameters)
        # if using passphrase, 'keypickle' is encrypted against blowfish
    
        # prepare the key tuple, depending on whether we're using passphrases
        if passphrase != '':
            # encrypt this against passphrase
            ppCipher = blowfish(passphrase, self._passIV)
            encpriv = ppCipher.encrypt(rawpriv)
            keytuple = (_True, _True, encpriv)
        else:
            keytuple = (_True, _False, rawpriv)
    
        # prepare final pickle, base64 encode, wrap
    
        #keypickle = pickle.dumps(keytuple, _True)
        keypickle = bencode(keytuple)
    
        return self._wrap("Key", keypickle)
    #@-node:exportKeyPrivate()
    #@+node:hasPrivateKey()
    def hasPrivateKey(self):
        """
        Returns 1 if this key object has a private key, 0 if not
        """
        return self.k.hasPrivateKey()
    #@-node:hasPrivateKey()
    #@+node:encString()
    def encString(self, raw):
        """
        Encrypt a string of data<br><br>
    
        High-level func. encrypts an entire string of data, returning the encrypted
        string as binary.<br><Br>
    
        Arguments:<ul>
        <li>raw string to encrypt</li>
        </ul>
        Returns:<ul>
        <li>encrypted string as binary</li>
        </ul>
    
        Note - the encrypted string can be stored in files, but I'd suggest
        not emailing them - use L{encStringToAscii} instead. The sole advantage
        of this method is that it produces more compact data, and works a bit faster.
        """
    
        # All the work gets done by the stream level
        #print "encString: ok1"
        self.encStart()
        #print "encString: ok2"
        enc = self.encNext(raw)
        return enc
    #@-node:encString()
    #@+node:encStringToAscii()
    def encStringToAscii(self, raw):
        """
        Encrypts a string of data to printable ASCII format<br><br>
    
        Use this method instead of L{encString}, unless size and speed are
        major issues.<br><br>
    
        This method returns encrypted data in bracketed  base64 format,
        safe for sending in email.<br><br>
    
        Arguments:<ul>
        <li><b>raw</b> - string to encrypt</li>
        </ul>
        Returns:<ul>
        <li><b>enc</b> - encrypted string, text-wrapped and Base-64 encoded, safe for
        mailing.</li>
        </ul>
    
        There's an overhead with base64-encoding. It costs size, bandwidth and
        speed. Unless you need ascii-safety, use encString() instead.
        """
        enc = self.encString(raw)
        return self._wrap("Message", enc)
    #@-node:encStringToAscii()
    #@+node:decString()
    def decString(self, enc):
        """
        Decrypts a previously encrypted string.<br><Br>
    
        Arguments:<ul>
        <li><b>enc</b> - string, previously encrypted in binary mode with encString</li>
        </ul>
        
        Returns:<ul>
        <li><b>dec</b> - raw decrypted string</li>
        </ul>
        """
    
        #chunklen = 1024
        #
        #size = len(enc)
        #bits = []
        #pos = 0
    
        self.decStart()
    
        # carve up into small chunks so we don't get any order n^2 on large strings
        #while pos < size:
        #    bits.append(self.decNext(enc[pos:pos+chunklen]))
        #    pos = pos + chunklen
        #
        dec = self.decNext(enc)
        self.decEnd()
    
        #dec = "".join(bits)
        return dec
    
    #@-node:decString()
    #@+node:decStringFromAscii()
    def decStringFromAscii(self, enc):
        """
        Decrypts a previously encrypted string in ASCII (base64)
        format, as created by encryptAscii()<br><br>
    
        Arguments:<ul>
        <li><b>enc</b>ascii-encrypted string, as previously encrypted with
        encStringToAscii()</li>
        </ul>
        
        Returns:<ul>
        <li><b>dec</b> - decrypted string</li>
        </ul>
    
        May generate an exception if the public key of the encrypted string
        doesn't match the public/private keypair in this key object.<br><br>
    
        To work around this problem, either instantiate a key object with
        the saved keypair, or use the <b>importKey()</b> function.<br><br>
    
        Exception will also occur if this object is not holding a private key
        (which can happen if you import a key which was previously exported
        via exportKey(). If you get this problem, use exportKeyPrivate() instead
        to export your keypair.
        """
        #trace()
        wrapped = self._unwrap("Message", enc)
        return self.decString(wrapped)
    #@-node:decStringFromAscii()
    #@+node:signString()
    def signString(self, raw, wrap=_True):
        """
        Sign a string using private key<br><br>
    
        Arguments:<ul>
        <li><b>raw</b> - string to be signed</li>
        <li><b>wrap</b> - wrap in email-friendly ASCII, default True
        </ul>
        
        Returns:<ul>
        <li>wrapped, base-64 encoded string of signature</li>
        </ul>
    
        Note - private key must already be present in the key object.
        Call <b>importKey()</b> for the right private key first if needed.
        """
    
        # hash the key with MD5
        m = md5.new()
        m.update(raw)
        d = m.digest()
        #print "sign: digest"
        #print repr(d)
    
        # sign the hash with our current public key cipher
        s = self.k.sign(d)
    
        # now wrap into a tuple with the public key cipher
        tup = (self.algoPname, s)
    
        # and pickle it
    
        #p = pickle.dumps(tup, _True)
        p = bencode(tup)
    
        # lastly, wrap it into our base64
        if wrap:
            p = self._wrap("Signature", p)
    
        return p
    
    #@-node:signString()
    #@+node:verifyString()
    def verifyString(self, raw, signature, wrap=_True):
        """
        Verifies a string against a signature.<br><br>
    
        Object must first have the correct public key loaded. (see
        <b>importKey</b>. An exception will occur if this is not the case.<br><br>
    
        Arguments:<ul>
        <li><b>raw</b> - string to be verified</li>
        <li><b>signature</b> - as produced when key is signed with <b>signString</b></li>
        <li><b>wrap</b> - take signature as email-friendly wrapped (default True)</b></li>
        </ul>
        
        Returns:<ul>
        <li>True if signature is authentic, or False if not</li>
        </ul>
        """
    
        # unrwap the signature to a pickled tuple
        if wrap:
            signature = self._unwrap("Signature", signature)
    
        # unpickle
    
        #algoname, rawsig = pickle.loads(signature)
        algoname, rawsig = bdecode(signature)
    
        # ensure we've got the right algorithm
        if algoname != self.algoPname:
            return _False # wrong algorithm - automatic fail
    
        # hash the string
        m = md5.new()
        m.update(raw)
        d = m.digest()
        #print "verify: digest"
        #print repr(d)
    
        # now verify the hash against sig
        if self.k.verify(d, rawsig):
            return _True # signature valid, or very clever forgery
        else:
            return _False # sorry
    #@-node:verifyString()
    #@+node:getSessKey()
    def getSessKey(self):
        """
        Returns the current cipher's session key
        """
        return self.sessKey
    #@-node:getSessKey()
    #@+node:getSessIV()
    def getSessIV(self):
        """
        Returns the starting IV for current session cipher
        """
        return self.sessIV
    #@-node:getSessIV()
    #@+node:setSessKey()
    def setSessKey(self, sessKey, IV):
        """
        Resets block cipher to use given session key and IV
        """
        self._setNewSessKey(sessKey, IV)
        self._initBlkCipher()
    #@-node:setSessKey()
    #@+node:genSessKey()
    def genSessKey(self):
        """
        Resets block cipher to use random session key and IV
        """
        self._genNewSessKey()
        self._initBlkCipher()
    #@-node:genSessKey()
    #@+node:encStringSess()
    def encStringSess(self, plain):
        """
        Encrypts a string against the session cipher,
        with none of the high=level packaging
        """
        return self.blkCipher.encrypt(plain)
    #@-node:encStringSess()
    #@+node:decStringSess()
    def decStringSess(self, ciphertext):
        """
        Decrypts a string against the session cipher,
        with none of the high=level packaging
        """
        return self.blkCipher.decrypt(ciphertext)
    #@-node:decStringSess()
    #@+node:test()
    def test(self, raw):
        """
        Encrypts, then decrypts a string. What you get back should
        be the same as what you put in.
    
        This is totally useless - it just gives a way to test if this API
        is doing what it should.
        """
        enc = self.encString(raw)
        dec = self.decString(enc)
        return dec
    #@-node:test()
    #@+node:testAscii()
    def testAscii(self, raw):
        """
        Encrypts, then decrypts a string. What you get back should
        be the same as what you put in.
    
        This is totally useless - it just gives a way to test if this API
        is doing what it should.
        """
        enc = self.encStringToAscii(raw)
        dec = self.decStringFromAscii(enc)
        return dec
    #@-node:testAscii()
    #@+node:Stream Methods
    # ---------------------------------------------
    #
    # These methods provide stream-level encryption
    #
    # ---------------------------------------------
    
    #@-node:Stream Methods
    #@+node:encStart()
    def encStart(self):
        """
        Starts a stream encryption session<br>
        Sets up internal buffers for accepting ad-hoc data.<br><br>
    
        No arguments needed, nothing returned.
        """
    
        # Create a header block of segments, each segment is
        # encrypted against recipient's public key, to enable
        # recipient to decrypt the rest of the stream.
    
        # format of header block is:
        #  - recipient public key
        #  - stream algorithm identifiers
        #  - stream session key
        #  - stream cipher initial value
    
        # stick in pubkey
        pubkey = self._rawPubKey()
    
        #print "encStart: ok1"
        pubkeyLen = len(pubkey)
        len0 = pubkeyLen % 256
        len1 = pubkeyLen / 256
    
        #print "pub key len = %d" % pubkeyLen
    
        self._encHdrs = chr(len0) + chr(len1) + pubkey
    
        # Create algorithms identifiers blk. Structure is:
        #  1byte  - index into session ciphers table
        #  1byte - session key len in bytes
        #  1byte  - session IV len in bytes
    
        # add algorithms index
        algInfo = chr(self._algosSes2.index(self.algoSes))
    
        #print "encStart: ok2"
    
        # Create new session key
        self._genNewSessKey()
    
        # add session key length
        sessKeyLen = len(self.sessKey)
        algInfo = algInfo + chr(sessKeyLen)
    
        # add session IV length
        sessIVLen = len(self.sessIV)
        algInfo = algInfo + chr(sessIVLen)
    
        # Prefix with 2 length bytes for consistency
        # hardwire because we know the length, which won't change anytime soon
        # don't encrypt this - because the range of values is small,
        # encrypting it could weaken the public key encryption
        algInfo = chr(3) + chr(0) + algInfo
        
        # Add to encrypted headers
        self._encHdrs = self._encHdrs + algInfo
    
        # ensure we can encrypt session key in one hit
        if len(self.sessKey) > self.pubBlkSize:
            raise Exception(
               "encStart: you need a bigger public key length")
    
        #print "encStart: ok3"
    
        # encrypt and add session key
        sKeyEnc = self._encRawPub(self.sessKey)
    
        #print "encStart: ok4"
    
        if sKeyEnc == None:
            raise Exception(
                "encStart: encryption of session key failed")
    
        # Add to encrypted headers
        self._encHdrs = self._encHdrs + sKeyEnc
    
        # encrypt and add session cipher initial value
        sCipherInit = self._encRawPub(self.sessIV)
        if sCipherInit == None:
            raise Exception(
                "encStart: encryption of session IV failed")
    
    
        # Add to encrypted headers
        self._encHdrs = self._encHdrs + sCipherInit
    
        # Create a new block cipher object
        self._initBlkCipher()
    
        # ready to go!
        self._encBuf = ''
    #@-node:encStart()
    #@+node:encNext()
    def encNext(self, raw=''):
        """
        Encrypt the next piece of data in a stream.<br><Br>
    
        Arguments:<ul>
        <li><b>raw</b> - raw piece of data to encrypt</li>
        </ul>
        
        Returns - one of:<ul>
        <li>'' - not enough data to encrypt yet - stored for later, OR</li>
        <li><b>encdata</b> - string of encrypted data</li>
        </ul>
        """
    
        if raw == '':
            return ''
    
        # grab any headers
        enc = self._encHdrs
        self._encHdrs = ''
    
        enc = enc + self.blkCipher.encrypt(raw)
        return enc
    
    #@-node:encNext()
    #@+node:encEnd()
    def encEnd(self):
        """
        Called to terminate a stream session.
        Encrypts any remaining data in buffer.<br>
        <br>
        Kinda obsolete now that we're using stream ciphers with full
        stream chaining mode.<br>
        <br>
        Arguments:<ul>
         <li>None</li></ul>
        Returns:<ul><li>None</li></ul>
        """
    
        return ''
    #@-node:encEnd()
    #@+node:decStart()
    def decStart(self):
        """
        Start a stream decryption session.<br><br>
    
        Call this method first, then feed in pieces of stream data into decNext until
        there's no more data to decrypt<br><br>
    
        No arguments, nothing returned
        """
    
        # Start with fresh buffer and initial state
        self._decBuf = ''
        self._decState = 'p'
        self._decEmpty = _False
    
        # states - 'p'->awaiting public key
        #          'c'->awaiting cipher index
        #          'k'->awaiting session key
        #          'i'->awaiting cipher initial data
        #          'd'->awaiting data block
    #@-node:decStart()
    #@+node:decNext()
    def decNext(self, chunk):
        """
        Decrypt the next piece of incoming stream data.<br><Br>
    
        Arguments:<ul>
        <li><b>chunk</b> - some more of the encrypted stream</li>
        </ul>
        
        Returns (depending on state)<ul>
        <li>'' - no more decrypted data available just yet, OR</li>
        <li><b>data</b> - the next available piece of decrypted data, OR</li>
        <li><b>None</b> - session is complete - no more data available</li>
        </ul>
        """
    
        if self._decEmpty:
            return None
    
        # add chunk to our buffer
        self._decBuf = self._decBuf + chunk
    
        # bail out if nothing to do
        chunklen = len(self._decBuf)
    
        #print "decNext: state=%s, chunklen=%d" % (self._decState, chunklen)
        if chunklen < 2 and self._decState != 'd':
            return ''
    
        # start with empty decryption buffer
        decData = ''
    
        # loop around processing as much data as we can
        #print "decNext: started"
        while 1:
            #print "decNext loop: state=%s" % self._decState
    
            if self._decState == 'd':
                # Expecting raw session stream data
                if self._decBuf == '':
                    return ''
                else:
                    decData = self.blkCipher.decrypt(self._decBuf)
                    self._decBuf = ''
                    return decData
    
            if self._decState == 'p':
                # Expecting public key portion
                size = ord(self._decBuf[0]) + 256 * ord(self._decBuf[1])
                if chunklen < size + 2:
                    # don't have full pubkey yet
                    return ''
                else:
                    pubkey = self._decBuf[2:size+2]
                    if not self._testPubKey(pubkey):
                        raise Exception("Can't decrypt - public key mismatch")
    
                    self._decBuf = self._decBuf[size+2:]
                    self._decState = 'c'
                    #print "Got good public key, size %d" % size
                    continue
    
            if len(self._decBuf) < 2:
                return decData
    
            sizeReqd = ord(self._decBuf[0]) + 256 * ord(self._decBuf[1]) + 2
            size = len(self._decBuf)
    
            #print "sizeReqd = %d" % sizeReqd
        
            # bail if we have insufficient data
            if size < sizeReqd:
                return decData
    
            # extract block
            blk = self._decBuf[0:sizeReqd]
            self._decBuf = self._decBuf[sizeReqd:]
    
            # state-dependent processing
            if self._decState == 'c':
                # awaiting cipher info - this is plaintext
    
                #print "Want cipher info block"
    
                # session cipher index
                c = ord(blk[2])
                self.algoSes = self._algosSes2[c]
    
                # session key len
                self._tmpSessKeyLen = ord(blk[3])
    
                # session IV len
                self._tmpSessIVLen = ord(blk[4])
    
                self._decState = 'k'
                continue
    
            elif self._decState == 'k':
                # awaiting session key - encrypted
                #print "decrypting session key"
                blk = self._decRawPub(blk)
                self.sessKey = blk[0:self._tmpSessKeyLen]
                self._decState = 'i'
                continue
    
            elif self._decState == 'i':
                # awaiting cipher start value
                #print "decrypting IV"
                blk = self._decRawPub(blk)
                self.sessIV = blk[0:self._tmpSessIVLen]
    
                # Create cipher object, now we have what we need
                #self.blkCipher = self.algoSes.new(self.sessKey,
                #                                  getattr(self.algoSes, "MODE_CFB"),
                #                                  self.sessIV)
                self.blkCipher = self.algoSes(self.sessKey, self.sessIV)
                self._calcSesBlkSize()
                self._decState = 'd'
                continue
    
            else:
                raise Exception(
                    "decNext: strange state '%s'" % self._decState[0])
    #@-node:decNext()
    #@+node:decEnd()
    def decEnd(self):
        """
        Ends a stream decryption session.
        """
        # nothing really to do here - decNext() has taken care of it
        # just reset internal state
        self._decBuf = ''
        self._decState = 'c'
    #@-node:decEnd()
    #@+node:_wrap()
    def _wrap(self, type, msg):
        """
        Encodes message as base64 and wraps with <StartPyCryptoname>/<EndPycryptoname>
        Args:
         - type - string to use in header/footer - eg 'Key', 'Message'
         - msg - binary string to wrap
        """
        return "<StartPycrypto%s>\n%s<EndPycrypto%s>\n" \
                 % (type, base64.encodestring(msg), type)
    #@-node:_wrap()
    #@+node:_unwrap()
    def _unwrap(self, type, msg):
        """
        Unwraps a previously _wrap()'ed message.
        """
        try:
            #trace()
            k1 = msg.split("<StartPycrypto%s>" % type, 1)
            k2 = k1[1].split("<EndPycrypto%s>" % type)
            k = k2[0]
            #print "raw = "
            #print k
            bin = base64.decodestring(k)
            return bin
        except:
            raise Exception("Tried to import Invalid %s" % type)
            self._calcBlkSize()
    #@-node:_unwrap()
    #@+node:_calcPubBlkSize()
    def _calcPubBlkSize(self):
        """
        Determine size of public key
        """
        #self.pubBlkSize = (self.k.size() - 7) / 8
        self.pubBlkSize = self.k.maxSize()
    #@-node:_calcPubBlkSize()
    #@+node:_encRawPub()
    def _encRawPub(self, raw):
        """
        Encrypt a small raw string using the public key
        algorithm. Input must not exceed the allowable
        block size.<br>
        <br>
        Arguments:<ul>
         <li>raw - small raw bit of string to encrypt</li></ul>
        Returns:<ul>
         <li>binary representation of encrypted chunk, or
             None if verify failed</li></ul>
    
        <b>Note</b> - returned block is prefixed by 2 length bytes, LSB first
        """
    
        if len(raw) > self.pubBlkSize:
            raise Exception(
                "_encraw: max len %d, passed %d bytes" % (self.pubBlkSize, len(raw)))
    
        #print "_encRawPub: ok1"
        enc = self.k.encrypt(raw)
        #print "_encRawPub: ok2"
    
        if self.k.hasPrivateKey():
            dec = self.k.decrypt(enc)
            #print "_encRawPub: ok3"
            if dec != raw:
                raise Exception(
                    "_encRawPub: decrypt verify fail")
    
        enclen = len(enc)
        #print "_encRawPub: hdr bytes are %02x %02x" % (enclen%256, enclen/256)
    
        enc = chr(enclen % 256) + chr(enclen / 256) + enc
        return enc
    #@-node:_encRawPub()
    #@+node:_decRawPub()
    def _decRawPub(self, enc):
        """
        Decrypt a public-key encrypted block, and return the decrypted string<br>
        <br>
        Arguments:<ul>
         <li><b>enc</b> - the encrypted string, in the format as
             created by _encRawPub()</li></ul>
        Returns:<ul>
         <li>decrypted block</li></ul>
    
        <b>Note</b><ul><li>The ciphertext should be prefixed by 2
                           length bytes, LSB first, as created by
                           <b>_encRawPub</b></li></ul>
        """
    
        enclen = len(enc)
        if enclen < 2:
            raise Exception("_decRawPub: ciphertext block too small")
        enclen0 = ord(enc[0]) + 256 * ord(enc[1])
        #print "_decRawPub: hdr bytes are %02x %02x" % (ord(enc[0]), ord(enc[1]))
        if enclen != enclen0 + 2:
            print "_decRawPub: expected %d bytes, got %d bytes" % (
                enclen0+2, enclen)
            raise Exception("_decRawPub: ciphertext block length mismatch")
        
        dec = self.k.decrypt(enc[2:])
        return dec
    #@-node:_decRawPub()
    #@+node:_initBlkCipher()
    def _initBlkCipher(self):
        """
        Create a new block cipher object, set up with a new session key
        and IV
        """
    
        self.blkCipher = self.algoSes(self.sessKey, self.sessIV)
        self._calcSesBlkSize()
    #@-node:_initBlkCipher()
    #@+node:_calcSesBlkSize()
    def _calcSesBlkSize(self):
        """
        Determine size of session blocks
        """
        #self.sesBlkSize = (self.blkCipher.block_size)
        self.sesBlkSize = 8 # TODO - ditch this
    #@-node:_calcSesBlkSize()
    #@+node:_testPubKey()
    def _testPubKey(self, k):
        """
        Checks if binary-encoded key matches this object's pubkey
        """
    
        if k == self._rawPubKey():
            return _True
        else:
            return _False
    #@-node:_testPubKey()
    #@+node:_rawPubKey()
    def _rawPubKey(self):
        """
        Returns a binary-encoded string of public key
        """
        #return pickle.dumps((self.algoPname, self.k.pubKey()), _True)
        return bencode((self.algoPname, self.k.pubKey()))
    
    #@-node:_rawPubKey()
    #@+node:_rawPrivKey()
    def _rawPrivKey(self):
        """
        Returns a binary-encoded string of private key, or None if there is no private key
        """
        #return pickle.dumps((self.algoPname, self.k.privKey()), _True)
        if not self.k.hasPrivateKey():
            raise CryptoNoPrivateKey
        return bencode((self.algoPname, self.k.privKey()))
    #@-node:_rawPrivKey()
    #@+node:_random
    def _random(self, bytes):
        """
        Generate an n-byte random number and return it as a string
        """
        cdef char *buf
        buf = <char *>malloc(bytes)
        cdef int RAND_bytes(buf, bytes)
    
        # convert to python string
        pBuf = PyString_FromStringAndSize(buf, bytes)
        
        # ditch temp buf
        free(buf)
        
        return pBuf
    #@-node:_random
    #@+node:_genNewSessKey()
    def _genNewSessKey(self):
        """
        Generate a new random session key
        """
        self.sessKey = self._random(32)
        self.sessIV = self._random(8)
    #@-node:_genNewSessKey()
    #@+node:_setNewSessKey()
    def _setNewSessKey(self, sessKey, IV):
        """
        Sets the session key to specific values
        """
        self.sessKey = sessKey
        self.sessIV = IV
    #@-node:_setNewSessKey()
    #@-others

#@-node:class key
#@+node:rndfunc
def rndfunc(nbytes):
    bytes = []
    i = 0
    while i < nbytes:
        bytes.append(chr(random.randint(0, 255)))
        i = i + 1
    return "".join(bytes)
#@-node:rndfunc
#@+node:size
#
#   number.py : Number-theoretic functions
#
#  Part of the Python Cryptography Toolkit
#
# Distribute and use freely; there are no restrictions on further
# dissemination and usage except those imposed by the laws of your
# country of residence.  This software is provided "as is" without
# warranty of fitness for use or suitability for any purpose, express
# or implied. Use at your own risk or not at all.
#

__revision__ = "$Id: number.py,v 1.13 2003/04/04 18:21:07 akuchling Exp $"

#bignum = long

#try:
#    from Crypto.PublicKey import _fastmath
#except ImportError:
#    _fastmath = None

#_fastmath = None


def size (N):
    """size(N:long) : int
    Returns the size of the number N in bits.
    """
    bits = 0
    power = long(1)
    while N >= power:
        bits = bits + 1
        power = power << 1
    return bits
#@-node:size
#@+node:GCD
def GCD(a, b):
    """GCD(a:long, b:long): long
    Return the GCD of a and b.
    """
    a = abs(a)
    b = abs(b)

    if a < b:
        a, b = b, a

    while b > 0:
        r = a % b
        a = b
        b = r
    return a
#@-node:GCD
#@+node:inverse
def inverse(u, v):
    """inverse(u:long, u:long):long
    Return the inverse of u mod v.
    """
    u3 = long(u)
    v3 = long(v)

    u1 = long(1)
    v1 = long(0)

    while v3 > 0:
        q = u3 / v3
        u1, v1 = v1, u1 - v1 * q
        u3, v3 = v3, u3 - v3 * q
    while u1 < 0:
        u1 = u1 + v
    return u1
#@-node:inverse
#@+node:long_to_bytes
def long_to_bytes(n):
    """
    convert a number back to a string
    """
    chars = []
    while n > 0:
        rest, chn = divmod(n, 256)
        chars.append(chr(chn))
        n = rest
    chars.reverse()
    return "".join(chars)

#@-node:long_to_bytes
#@+node:bytes_to_long
def bytes_to_long(s):
    """bytes_to_long(string) : long
    Convert a byte string to a long integer.

    This is (essentially) the inverse of long_to_bytes().
    """
    n = long(0)
    for ch in s:
        n = 256 * n + ord(ch)
    return n

#@-node:bytes_to_long
#@+node:genprime
def genprime(bits=256, myCallback=None, safe=1):
    """
    Generate a secure (Sophie-Germain) prime number<br>
    <br>
    Arguments:<ul>
      <li>bits - number of bits length, default 256</li>
      <li>callback - a function which accepts 2 arguents - level, num - for giving
          user some feedback of progress</li>
    </ul>
    """
    cdef BIGNUM *rand
    #cdef char *randStr

    # change the third parameter to 1 for a strong prime
    rand = BN_generate_prime(NULL,
                             bits,
                             safe,
                             NULL,
                             NULL,
                             callback, <void *>myCallback)

    # convert to a python long
    randLong = bn2pyLong(rand)
    BN_free(rand)
    callback(-1, 0, <void *>myCallback)
    return randLong

#@-node:genprime
#@+node:callback()
cdef public void callback(int type, int num, void *arg):
    if arg != <void *>0 and (<object>arg) != None:
        (<object>arg)(type, num)
#@-node:callback()
#@+node:genrandom
def genrandom(bits=256, top=0, bottom=0):
    """
    Generates an n-bit quality random number
    """
    cdef BIGNUM *rand
    #cdef char *randStr

    # change the third parameter to 1 for a strong prime

    rand = BN_new()

    #print "got new rand obje"

    BN_rand(rand,
            bits,
            top,
            bottom)

    #print "Generated rand"

    # convert to a python long
    randLong = bn2pyLong(rand)

    #print "converted to long"

    BN_free(rand)

    #print "freed rand"

    return randLong

#@-node:genrandom
#@+node:bn2pyLong()
cdef public bn2pyLong(BIGNUM *bnum):
    cdef char *numStr
    numStr = BN_bn2dec(bnum)
    numLong = long(numStr)
    CRYPTO_free(numStr)
    return numLong

#@-node:bn2pyLong()
#@+node:pyLong2bn()
cdef public BIGNUM *pyLong2bn(object n):

    cdef BIGNUM *bnum
    cdef int result
    cdef char *cstr
    
    bnum = BN_new()

    s = repr(n)
    cstr = s
    result = BN_dec2bn(&bnum, cstr)
    if result == 0:
        print "pyLong2bn: conversion failed!!"
        print n
        raise Exception("Conversion failed - Invalid number")
    else:
        return bnum
#@-node:pyLong2bn()
#@+node:randomseed
def randomseed(entropy):
    """
    <b>randomseed</b> - feed entropy to the random number generator<br>
    <br>
    Allows client code to pass entropy to OpenSSL's pseudo-random
    number generation.<br>
    <br>
    You <b>must</b> call this function before doing anything with this module.
    If you don't, you'll be totally susceptible to random number analysis
    attacks, and your security could be dead in the water. <i>You have been warned!</i><br>
    <br>
    Arguments:<ul>
     <li><b>entropy</b> - a plain string of data to be fed in as entropy.
     You can pass anything you like here - sampled mouse movements,
     keystrokes etc.</li>
    </ul>
    Returns:<ul>
     <li>None</li>
     </ul>
    """

    cdef int cBufLen
    cdef char *cBuf

    cBuf = entropy
    cBufLen = len(entropy)

    RAND_seed(cBuf, cBufLen)
#@-node:randomseed
#@+node:_die()
cdef extern void _c_die()

def _die():
    _c_die()
#@-node:_die()
#@+node:imports
# Written by Petru Paler
# see LICENSE.txt for license information

from types import IntType, LongType, StringType, ListType, TupleType, DictType
import re
from cStringIO import StringIO
#@-node:imports
#@+node:global data
int_filter = re.compile('(0|-?[1-9][0-9]*)e')
string_filter = re.compile('(0|[1-9][0-9]*):')
#@-node:global data
#@+node:decode_int
def decode_int(x, f):
    m = int_filter.match(x, f)
    if m is None:
        raise ValueError
    return (long(m.group(1)), m.end())
#@-node:decode_int
#@+node:decode_string
def decode_string(x, f):
    m = string_filter.match(x, f)
    if m is None:
        raise ValueError
    l = int(m.group(1))
    s = m.end()
    return (x[s:s+l], s + l)
#@-node:decode_string
#@+node:decode_list
def decode_list(x, f):
    r = []
    while x[f] != 'e':
        v, f = bdecode_rec(x, f)
        r.append(v)
    return (r, f + 1)
#@-node:decode_list
#@+node:decode_dict
def decode_dict(x, f):
    r = {}
    lastkey = None
    while x[f] != 'e':
        k, f = decode_string(x, f)
        if lastkey is not None and lastkey >= k:
            raise ValueError
        lastkey = k
        v, f = bdecode_rec(x, f)
        r[k] = v
    return (r, f + 1)
#@-node:decode_dict
#@+node:bdecode_rec
def bdecode_rec(x, f):
    t = x[f]
    if t == 'i':
        return decode_int(x, f + 1)
    elif t == 'l':
        return decode_list(x, f + 1)
    elif t == 'd':
        return decode_dict(x, f + 1)
    else:
        return decode_string(x, f)
#@-node:bdecode_rec
#@+node:bdecode
def bdecode(x):
    try:
        r, l = bdecode_rec(x, 0)
    except IndexError:
        raise ValueError
    if l != len(x):
        raise ValueError
    return r
#@-node:bdecode
#@+node:bencode_rec
def bencode_rec(x, b):
    t = type(x)
    if t in (IntType, LongType):
        b.write('i%de' % x)
    elif t is StringType:
        b.write('%d:%s' % (len(x), x))
    elif t in (ListType, TupleType):
        b.write('l')
        for e in x:
            bencode_rec(e, b)
        b.write('e')
    elif t is DictType:
        b.write('d')
        keylist = x.keys()
        keylist.sort()
        for k in keylist:
            assert type(k) is StringType
            bencode_rec(k, b)
            bencode_rec(x[k], b)
        b.write('e')
    else:
        assert 0
#@-node:bencode_rec
#@+node:bencode
def bencode(x):
    b = StringIO()
    bencode_rec(x, b)
    return b.getvalue()
#@-node:bencode
#@-others
#@-node:@file SSLCrypto.pyx
#@-leo
