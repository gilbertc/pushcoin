#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys, urllib2, time
import logging as log
import pcos, time, binascii, base64, hashlib, Image
from optparse import OptionParser,OptionError
from pyparsing import *
from M2Crypto import DSA, BIO, RSA

def load_qrcode():
	try:
		import qrcode
		return True
	except ImportError:
		return False

# The transaction key and the key-ID where obtained from:
#   https://pushcoin.com/Pub/SDK/TransactionKeys
#
API_TRANSACTION_KEY_ID = '652fce08'

#
# Please note:
#
#   The PEM format is base64-encoded DER data with additional header and footer lines:
#     -----BEGIN PUBLIC KEY-----
#        <base64-encded DER>
#     -----END PUBLIC KEY-----
# 

API_TRANSACTION_KEY_PEM = '''-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC7BAaOZNk3dNMKQCmom5qem41w
sS8yIWUnOUgYIOT7FE0SVTFj1qXVc5WBpUQuAiYepmyTH8QGUBU4FtNJyQED56LN
Pgm8rTg45kqFjXuJF9IGKb89e7mx8qP0JevT8eVoIpiiwGb3xDuIkjrD5QUpcwes
bYi8AscPo+oDz+jQ5QIDAQAB
-----END PUBLIC KEY-----'''

# Below, the DSA keys were generated as follows:
#   1. generate param file
#      openssl dsaparam 512 < /dev/random > dsa_param.pem
#   
#   2. generating DSA key, 512 bits
#      openssl gendsa dsa_param.pem -out dsa_priv.pem
#   
#   2b. optionally encrypt the key
#       openssl dsa -in dsa_priv.pem -des3 -out dsa_safe_priv.pem
#   
#   3. extract public key out of private, write in DER format
#      openssl dsa -in dsa_priv.pem -outform DER -pubout -out dsa_pub.der

# The public DSA key is sent to the server in the "Register" message.
#
#-----BEGIN PUBLIC KEY-----
TEST_DSA_KEY_PUB_PEM = '''MIHxMIGoBgcqhkjOOAQBMIGcAkEA6DCdaRYmSb4vQUAkaqsR+Ph2aprcMAlDkRGL
Vc1N8Hi3sm97xR+b3IYTHRuYaSEyaWKvuByjbFnJRjyYBpTKqwIVAObFswWoV2wl
LoUs3//+1kRFOHY/AkADEXixNnXLQp3dDapOb57uM+6/TH4mZJizpvCqpVaonIz2
ZGzB+ws/EU7fmitScho04EJg+1xBbLsMbJ1lMxaoA0QAAkEAgnL2PItRT0fn8GJ4
YygfEG1wUMaW9YrkRNWuNtOBtw3WERn8fa+6VeTKujSfDcnnpj6mnyqusPhA4Ek6
iYVpxw=='''
# -----END PUBLIC KEY-----

# The private key is "secretly" kept on the device.
#
TEST_DSA_KEY_PRV_PEM = '''-----BEGIN DSA PRIVATE KEY-----
MIH3AgEAAkEAjfeT35NuNNXa9J6WFRGkbLFPbMjTvfBwBmlIBxkn5C7P7tbrSKX2
v4kkNOxaSoL1IbAcIsRfLAQONhu5OypILwIVAKPptYe+gRwRHTd47lSliZcv6HXx
AkASAkNvUTHAAayp1ozyEa42u/9el+r5ffTGK1VH9VYgCc3dcUHOxGl3gXl2KQfN
Pt6owQKKsZnrpgO1v1N+ciLWAkA9jERRrih0tMqrqBq3iRmpqQXFQhsy+oyPST9v
+KiP+POtARwoOToKJw8Ub8o3EdjoXWobCvDbxTMPP447uJkTAhR5+vvpezohpW2r
WBKhBPOqvJ8X+w==
-----END DSA PRIVATE KEY-----'''

class RmoteCall:

	# CMD: `preauth'
	def preauth(self):
		'''Generates the PTA and submits to server for validation.'''
		pta_encoded = self.payment()

		# package PTA into a block
		pta = pcos.Block( 'Pa', 512, 'O' )
		pta.write_fixed_string(pta_encoded, size=len(pta_encoded))

		# create preauth block
		preauth = pcos.Block( 'Pr', 512, 'O' )
		preauth.write_fixed_string( binascii.unhexlify( self.args['preauth_mat'] ), size=20 )
		preauth.write_int64( long( self.args['preauth_scaled_payment'] ) ) # payment
		preauth.write_int16( int( self.args['preauth_scale'] ) ) # scale
		preauth.write_fixed_string( "USD", size=3 ) # currency
		preauth.write_short_string( '', max=20 ) # user data

		# package everything and ship out
		req = pcos.Doc( name="Pr" )
		req.add( pta )
		req.add( preauth )

		res = self.send( req )
		if res.message_id == "Ok":
			log.info('RETN Preauthorization Success' )

	# CMD: `payment'
	def payment(self):
		'''This command generates the Payment Transaction Authorization, or PTA. It does not communicate with the server, only produces a file.'''

		#------------------
		# PTA public-block
		#------------------
		p1 = pcos.Block( 'P1', 512, 'O' )
		now = long( time.time() + 0.5 )
		p1.write_int64( now ) # certificate create-time
		p1.write_int64( now + 24 * 3600 ) # certificate expiry (in 24 hrs)

		p1.write_int64( long( self.args['scaled_payment'] ) ) # payment
		p1.write_int16( int( self.args['scale'] ) ) # scale

		p1.write_fixed_string( "USD", size=3 ) # currency
		p1.write_fixed_string( binascii.unhexlify( API_TRANSACTION_KEY_ID ), size=4 ) # key-ID

		p1.write_short_string( '', max=127 ) # receiver
		p1.write_short_string( '', max=127 ) # note

		#-------------------
		# PTA private-block
		#-------------------
		priv = pcos.Block( 'S1', 512, 'O' )

		# member authentication token
		mat = self.args['mat'] 
		if len( mat ) != 40:
			raise RuntimeError("MAT must be 40-characters long" % self.cmd)
		priv.write_fixed_string( binascii.unhexlify( self.args['mat'] ), size=20 )
		
		# sign the public-block
		#   * first, produce the checksum
		sha1 = hashlib.sha1()
		sha1.update( str(p1) )
		digest = sha1.digest()
		#   * then sign the checksum
		dsa_priv_key = BIO.MemoryBuffer( TEST_DSA_KEY_PRV_PEM )
		signer = DSA.load_key_bio( dsa_priv_key )
		signature = signer.sign_asn1( digest )
		priv.write_short_string( signature, max=48 )

		priv.write_short_string( '', max=20 ) # empty user data
		priv.write_short_string( '', max=26 ) # empty reserved field

		# encrypt the private-block
		txn_pub_key = BIO.MemoryBuffer( API_TRANSACTION_KEY_PEM )
		encrypter = RSA.load_pub_key_bio( txn_pub_key )
		# RSA Encryption Scheme w/ Optimal Asymmetric Encryption Padding
		encrypted = encrypter.public_encrypt( str(priv), RSA.pkcs1_oaep_padding )

		# At this point we no longer need the 'priv' object. We only attach the
		# encrypted instance.
		s1 = pcos.Block( 'S1', 512, 'O' )
		s1.write_fixed_string( encrypted, size=128 )

		#-------------------
		# PTA envelope
		#-------------------
		env = pcos.Doc( name="Pa" )
		# order in which we add blocks doesn't matter
		env.add( p1 )
		env.add( s1 )

		# write serialized data as binary and qr-code
		encoded = env.encoded()
		reqf = open('pta.pcos', 'w')
		reqf.write( encoded )
		reqf.close()
		print ("Saved PTA object to 'pta.pcos'")

		# optionally generate qr-code
		try:
			import qrcode
			qr = qrcode.QRCode(version=None, error_correction=qrcode.constants.ERROR_CORRECT_L)
			qr.add_data( encoded )
			qr.make(fit=True)
			img = qr.make_image()
			img.save('pta.png')
			print ("PTA-QR: pta.png, version %s" % (qr.version))
		except ImportError:
			log.warn("QR-Code not written -- qrcode module not found")

		return encoded

	
	# CMD: `register'
	def register(self):
		req = pcos.Doc( name="Re" )
		bo = pcos.Block( 'Bo', 512, 'O' )
		bo.write_short_string( self.args['registration_id'], max=64 ) # registration ID
		bo.write_long_string( base64.b64decode(TEST_DSA_KEY_PUB_PEM) )
		bo.write_short_string( ';'.join( ('IceBreaker/1.0', sys.platform, sys.byteorder, sys.version) ), max=128 )
		req.add( bo )

		res = self.send( req )

		# jump to the block of interest

	# CMD: `ping'
	def ping(self):

		req = pcos.Doc( name="Pi" )
		res = self.send( req )
		# jump to the block of interest
		tm = res.block( 'Bo' )

		# read block field(s)
		tm_epoch = tm.read_int64();
		server_time = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(tm_epoch))
		log.info('RETN %s', server_time )


	# CMD: `transaction key'
	def transaction_key(self):

		req = pcos.Doc( name="Tk" )
		res = self.send( req )
		# jump to the block of interest
		body = res.block( 'Bo' )

		# read block field(s)
		keyid = body.read_fixed_string(4)
		key_info = body.read_short_string()
		key_expiry = body.read_int64()
		key_data = body.read_long_string()
		log.info('RETN gotten key %s, len %s bytes, expires on %s', key_info, len(key_data), time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(key_expiry)))

	def __init__(self, options, cmd, args):
		# store the cmd and args for the command-handler
		self.options = options
		self.cmd = cmd
		self.args = args

		# list of commands (PushCoin requests) we are supporting:
		self.lookup = {
			"ping": self.ping,
			"register": self.register,
			"payment": self.payment,
			"preauth": self.preauth,
			"transaction_key": self.transaction_key,
		}		

	# invoked if user asks for an unknown command
	def unknown_command(self):
		raise RuntimeError("'%s' is not a recognized command" % self.cmd)

	# entry point to call out to the server
	def call(self):
		# lookup the command and invoke it
		cmd = self.lookup.get(self.cmd, self.unknown_command)
		cmd();
		
	# sends request to the server, returns result
	def send(self, req):

		# Get encoded PCOS data 	
		encoded = req.encoded()

		# For debugging, we write request and response
		if self.options.is_writing_io:
			reqf = open('request.pcos', 'w')
			reqf.write( encoded )
			reqf.close()

		log.info('CALL %s %s', self.cmd, str(self.args) )
		remote_call = urllib2.urlopen(self.options.url, encoded )
		response = remote_call.read()

		if self.options.is_writing_io:
			reqf = open('response.pcos', 'w')
			reqf.write( response )
			reqf.close()

		res = pcos.Doc( response )
		# check if response is not an error
		if res.message_id == 'Er':
			# jump to the block of interest
			er = res.block( 'Bo' )
			if er:
				code = er.read_int32();
				what = er.read_short_string();
				log.error( '%s (#%s)', what, code )
			else:
				log.error( 'ERROR -- cause unknown' )
			raise RuntimeError('error result') 

		# return a lightweight PCOS document 
		return res

if __name__ == "__main__":
	# start with basic logger configuration
	log.basicConfig(level=log.INFO, format='%(asctime)s %(levelname)s %(message)s')
	
	# program arguments
	usage = "usage: %prog [options] <command> [args]"
	version = "PushCoin IceBreaker v1.0"
	parser = OptionParser(usage, version = version)
	parser.add_option("-C", "--url", dest="url", action="store", default="https://api.pushcoin.com:20001/pcos/", help="server URL")
	parser.add_option("-S", "--save-io", dest="is_writing_io", action="store_true", default=False, help="save request and response to files")
	
	if len(sys.argv) == 0:
		parser.print_help()
		exit(1)
	
	(options, args) = parser.parse_args()
	
	if len(args) < 1: 
		raise RuntimeError('missing command argument') 

	print version

	cmd = args[0]
	cmd_args = { }
	if len(args) > 1: 
		# define basic elements - use re's for numerics, faster and easier than 
		# composing from pyparsing objects
		integer = Regex(r'[+-]?\d+')
		real = Regex(r'[+-]?\d+\.\d*')
		ident = Regex(r'\w+')
		value = real | integer | quotedString.setParseAction(removeQuotes)

		# define a key-value pair, and a configline as one or more of these
		configline = dictOf(ident + Suppress('='), value + Suppress(Optional(':')))
		cmd_args = configline.parseString(args[1]).asDict()
		print ("Parsed arguments: " + str(cmd_args))
	
	pushCoin = RmoteCall(options, cmd, cmd_args)
	pushCoin.call()
	
	log.info('Bye.')
	exit(0)
