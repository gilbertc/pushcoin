#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys, urllib2, time
import logging as log
import pcos, time, binascii, base64, hashlib, math, Image
from decimal import Decimal
from optparse import OptionParser,OptionError
from pyparsing import *
from M2Crypto import DSA, BIO, RSA

# PC_DEFAULT_API_URL="https://api.pc-dev.com/pcos/"
PC_DEFAULT_API_URL="https://199.192.203.73/pcos/"

def load_qrcode():
	try:
		import qrcode
		return True
	except ImportError:
		return False

# The transaction key and the key-ID where obtained from:
#   https://pushcoin.com/Pub/SDK/TransactionKeys
#
API_TRANSACTION_KEY_ID = binascii.unhexlify( 'ce08' )

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
TEST_DSA_KEY_PUB_PEM = '''MIHwMIGoBgcqhkjOOAQBMIGcAkEAjfeT35NuNNXa9J6WFRGkbLFPbMjTvfBwBmlI
Bxkn5C7P7tbrSKX2v4kkNOxaSoL1IbAcIsRfLAQONhu5OypILwIVAKPptYe+gRwR
HTd47lSliZcv6HXxAkASAkNvUTHAAayp1ozyEa42u/9el+r5ffTGK1VH9VYgCc3d
cUHOxGl3gXl2KQfNPt6owQKKsZnrpgO1v1N+ciLWA0MAAkA9jERRrih0tMqrqBq3
iRmpqQXFQhsy+oyPST9v+KiP+POtARwoOToKJw8Ub8o3EdjoXWobCvDbxTMPP447
uJkT'''
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

	def balance(self):
		'''Returns account balance'''

		#------------------------------------
		#      Request Body Block
		#------------------------------------
		out_bo = pcos.create_output_block( 'Bo' )
		out_bo.write_varstr( binascii.unhexlify( self.args['mat'] ) ) # mat
		out_bo.write_varstr( '' ) # ref_data

		req = pcos.Doc( name="Bq" )
		req.add( out_bo )

		res = self.send( req )

		assert res.message_id == 'Br'

		#------------------------------------
		#      Response Body Block
		#------------------------------------
		body = res.block( 'Bo' )
		ref_data = body.read_varstr( ) # ref_data

		value = body.read_long() # value
		scale = body.read_int() # scale
		balance_asofepoch = body.read_ulong();

		balance = value * math.pow(10, scale)
		balance_asofdate = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(balance_asofepoch))
		log.info('Balance is $%s as of %s', balance, balance_asofdate)


	def history(self):
		'''Returns transaction history'''

		#------------------------------------
		#      Request Body Block
		#------------------------------------
		out_bo = pcos.create_output_block( 'Bo' )
		# MAT
		out_bo.write_varstr( binascii.unhexlify( self.args['mat'] ) )
		# ref-data
		out_bo.write_varstr( '' )
		# search keywords
		out_bo.write_varstr( '' )

		# page number and page-size
		out_bo.write_uint( int(self.args['page']) )
		out_bo.write_uint( int(self.args['size']) )

		#------------------------------------
		#    History Query Message
		#------------------------------------
		req = pcos.Doc( name="Hq" )
		req.add( out_bo )

		res = self.send( req )

		assert res.message_id == 'Hr'

		#------------------------------------
		#      Response Body Block
		#------------------------------------
		body = res.block( 'Bo' )
		# ref-data
		ref_data = body.read_varstr()

		# read number of transactions
		count = body.read_uint()
		for i in xrange(1, count+1):
			# transaction ID
			tx_id = binascii.hexlify( body.read_varstr() )

			# counterparty ID
			counterparty = binascii.hexlify( body.read_varstr() )

			# transaction time of day
			epoch_tx_time = body.read_ulong()
			tx_time = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(epoch_tx_time))

			# transaction type
			tx_type = body.read_fixstr(1)

			# transaction context: (P)ayment or (T)ransfer
			tx_context = body.read_fixstr(1)

			#amount
			value = body.read_long() # value
			scale = body.read_int() # scale
			amount = value * math.pow(10, scale)

			# tax
			if body.read_bool():
				value = body.read_long() # value
				scale = body.read_int() # scale
				tax = value * math.pow(10, scale)
			else:
				tax = 'not provided'

			#tip
			if body.read_bool():
				value = body.read_long() # value
				scale = body.read_int() # scale
				tip = value * math.pow(10, scale)
			else:
				tip = 'not provided'

			# currency
			currency = body.read_fixstr(3)

			# merchant name
			merchant_name = body.read_varstr()

			# PTA recipient
			recipient = body.read_varstr()

			# ref_data
			ref_data = binascii.hexlify( body.read_varstr() )

			# invoice
			invoice = body.read_varstr()

			# note
			note = body.read_varstr()

			# address of the POS station
			if body.read_bool():
				street = body.read_varstr()
				city = body.read_varstr()
				state = body.read_varstr()
				zipc = body.read_varstr()
				country = body.read_fixstr(2)
				address = '%s, %s, %s %s, %s' % (street, city, state, zipc, country)
			else:
				address = 'not provided'

			# contact info at the place of transaction origination
			if body.read_bool():
				phone = body.read_varstr()
				email = body.read_varstr()
				contact = 'phone: %s, email: %s' % (phone, email)
			else:
				contact = 'not provided'

			# geo-location of the place of transaction origination
			if body.read_bool():
				latitude = body.read_double()
				longitude = body.read_double()
				geolocation = '%s, %s' % (latitude, longitude)
			else:
				geolocation = 'not provided'

			print "--- %s/%s ---\ntx-id: %s\ncounterparty: %s\ntx_time: %s\naddress: %s\ngeolocation: %s\ncontact: %s\ntx_type: %s\ntx_context: %s\namount: %s\ntip: %s\ntax: %s\ncurrency: %s\nmerchant_name: %s\nrecipient: %s\nref-data: %s\ninvoice: %s\nnote: %s\n" % (i, count, tx_id, counterparty, tx_time, address, geolocation, contact, tx_type, tx_context, amount, tip, tax, currency, merchant_name, recipient, ref_data, invoice, note)

		log.info('Returned %s records', count)


	def preauth(self):
		'''Generates the PTA and submits to server for validation.'''
		apta_bytes = self.payment()

		#------------------------------------
		#      Armored-PTA Block
		#------------------------------------
		apta_block = pcos.create_output_block( 'Pa' )

		# we use "fixstr" becase we don't want size-prefix
		apta_block.write_fixstr(apta_bytes, len(apta_bytes))

		#------------------------------------
		#      Preauthorizaton Block
		#------------------------------------
		preauth_block = pcos.create_output_block( 'Pr' ) 
		# mat
		preauth_block.write_varstr( binascii.unhexlify( self.args['preauth_mat'] ) )

		# ref data
		preauth_block.write_varstr( '' )

		# preauth amount
		(charge_int, charge_scale) = decimal_to_parts(Decimal(self.args['charge']))
		preauth_block.write_long( charge_int ) # value
		preauth_block.write_int( charge_scale ) # scale

		# currency
		preauth_block.write_fixstr( "USD", size=3 )

		#------------------------------------
		#      Preauth Message
		#------------------------------------
		req = pcos.Doc( name="Pr" )
		req.add( preauth_block )
		req.add( apta_block )

		res = self.send( req )
		self.expect_success( res )


	def transfer(self):
		'''Sends a Transfer Request'''

		apta_bytes = self.payment()

		#------------------------------------
		#      Armored-PTA Block
		#------------------------------------
		apta_block = pcos.create_output_block( 'Pa' )

		# we use "fixstr" becase we don't want size-prefix
		apta_block.write_fixstr(apta_bytes, len(apta_bytes))

		#------------------------------------
		#      Transfer Request Block
		#------------------------------------
		trnfs_req_block = pcos.create_output_block( 'R1' ) 

		# mat
		trnfs_req_block.write_varstr( binascii.unhexlify( self.args['receiver_mat'] ) )

		# ref data
		trnfs_req_block.write_varstr( '' )

		# create-time
		trnfs_req_block.write_ulong( long( time.time() + 0.5 ) )

		(charge_int, charge_scale) = decimal_to_parts(Decimal(self.args['charge']))
		trnfs_req_block.write_long( charge_int ) # value
		trnfs_req_block.write_int( charge_scale ) # scale

		# currency
		trnfs_req_block.write_fixstr( "USD", size=3 )
 
		# note
		trnfs_req_block.write_varstr( 'John paid his dept' )

		# no geo-location available
		trnfs_req_block.write_bool(False)

		#------------------------------------
		#      Transfer Message
		#------------------------------------
		req = pcos.Doc( name="Tt" )
		req.add( trnfs_req_block )
		req.add( apta_block )

		res = self.send( req )
		self.expect_success( res )


	def charge(self):
		'''Sends a Payment Request'''
		pta_encoded = self.payment()

		# package PTA into a block
		pta = pcos.Block( 'Pa', 512, 'O' )
		pta.write_fixed_string(pta_encoded, size=len(pta_encoded))

		# create payment-request block
		r1 = pcos.Block( 'R1', 1024, 'O' )
		r1.write_fixed_string( binascii.unhexlify( self.args['merchant_mat'] ), size=20 ) # mat
		r1.write_varstr( 'charge-ref', max=127 ) # ref_data
		r1.write_int64( long( time.time() + 0.5 ) ) # request create-time

		# charge amount
		(charge_value, charge_scale) = decimal_to_parts(Decimal(self.args['charge']))
		r1.write_int64( charge_value ) # value
		r1.write_int16( charge_scale ) # scale

		# tax
		r1.write_byte(1) # optional indicator
		(tax_value, tax_scale) = decimal_to_parts(Decimal(self.args['tax']))
		r1.write_int64( tax_value ) # value
		r1.write_int16( tax_scale ) # scale

		# tip
		r1.write_byte(1) # optional indicator
		(tip_value, tip_scale) = decimal_to_parts(Decimal(self.args['tip']))
		r1.write_int64( tip_value ) # value
		r1.write_int16( tip_scale ) # scale

		r1.write_fixed_string( "USD", size=3 ) # currency
		r1.write_varstr( 'inv-123', max=24 ) # invoice ID
		r1.write_varstr( 'happy meal', max=127 ) # note
		r1.write_int16(0) # list of purchased goods

		# no geo-location available
		r1.write_byte(0)

		# package everything and ship out
		req = pcos.Doc( name="Pt" )
		req.add( pta )
		req.add( r1 )

		res = self.send( req )
		self.expect_success( res )


	def payment(self):
		'''This command generates the Payment Transaction Authorization, or PTA. It does not communicate with the server, only produces a file.'''

		#------------------------------------
		#        PTA Payment Block
		#------------------------------------
		p1 = pcos.create_output_block( 'P1' )

		# member authentication token
		mat = self.args['mat'] 
		if len( mat ) != 40:
			raise RuntimeError("MAT must be 40-characters long" % self.cmd)
		p1.write_varstr( binascii.unhexlify( self.args['mat'] ) )

		# cert. create time and expiry
		now = long( time.time() + 0.5 )
		p1.write_ulong( now ) # certificate create-time
		p1.write_ulong( now + 24 * 3600 ) # certificate expires in 24 hrs

		# payment-limit
		(payment_int, payment_scale) = decimal_to_parts(Decimal(self.args['limit']))
		p1.write_long( payment_int ) # value
		p1.write_int( payment_scale ) # scale

		# currency
		p1.write_fixstr( "USD", size=3 )

		# recipient
		p1.write_varstr( "" )

		# ref-data
		p1.write_varstr( "" )

		# note
		p1.write_varstr( "" )

		print (" %s bytes => Payment Block" % p1.size())

		#------------------------------------
		#        PTA Signature Block
		#------------------------------------
		s1 = pcos.create_output_block( 'S1' )

		# checksum Payment Block
		digest = hashlib.sha1(p1.as_bytearray()).digest()
		
		# sign the checksum
		dsa_priv_key = BIO.MemoryBuffer( TEST_DSA_KEY_PRV_PEM )
		signer = DSA.load_key_bio( dsa_priv_key )
		signature = signer.sign_asn1( digest )

		# store the signature of the Payment Block
		s1.write_varstr( signature )
		print (" %s bytes => Signature Block" % s1.size())

		#------------------------------------
		# PTA Message
		#  * Payment Block
		#  * Signature Block
		#------------------------------------
		pta = pcos.Doc( name="Pa" )
		pta.add( p1 )
		pta.add( s1 )

		#------------------------------------
		#    A-PTA Private (PTA) Block
		#------------------------------------
		a1 = pcos.create_output_block( 'A1' )
		
		# encrypt the PTA message
		txn_pub_key = BIO.MemoryBuffer( API_TRANSACTION_KEY_PEM )
		encrypter = RSA.load_pub_key_bio( txn_pub_key )
		# RSA Encryption Scheme w/ Optimal Asymmetric Encryption Padding
		input_data = pta.as_bytearray()
		print (" %s bytes => header+meta" % (len(input_data) - s1.size() - p1.size()))
		print ("-------------\n %s bytes => Total PTA\n---------" % len(input_data))
		encrypted = encrypter.public_encrypt( pta.as_bytearray(), RSA.pkcs1_padding )
		a1.write_fixstr( encrypted, size=len(encrypted) )

		#------------------------------------
		#    A-PTA Public Block
		#------------------------------------
		k1 = pcos.create_output_block( 'K1' )
		# encryption key identifier
		k1.write_fixstr( API_TRANSACTION_KEY_ID, size=len(API_TRANSACTION_KEY_ID) )

		#------------------------------------
		#    A-PTA Message
		#------------------------------------
		apta = pcos.Doc( name="Ap" )
		apta.add( a1 )
		apta.add( k1 )
		apta_bytes = apta.as_bytearray()

		# write serialized data as binary and qr-code
		payload_file = open('apta.pcos', 'w')
		payload_file.write( apta_bytes )
		payload_file.close()
		print ("Saved APTA object to 'apta.pcos'")

		# optionally generate qr-code
		try:
			import qrcode
			qr = qrcode.QRCode(version=None, box_size=3, error_correction=qrcode.constants.ERROR_CORRECT_L)
			qr.add_data( apta_bytes )
			qr.make(fit=True)
			img = qr.make_image()
			img.save('apta.png')
			print ("APTA-QR: apta.png, version %s" % (qr.version))
		except ImportError:
			log.warn("QR-Code not written -- qrcode module not found")

		return apta_bytes

	
	def register(self):
		'''Register command'''

		#------------------------------------
		#           Body Block
		#------------------------------------
		bo = pcos.create_output_block( 'Bo' )

		# registration ID
		bo.write_varstr( self.args['registration_id'] )

		# DER encoded public key -- signature verification key
		bo.write_varstr( base64.b64decode(TEST_DSA_KEY_PUB_PEM) )

		# user-agent attributes
		user_agent = [
			('appname', 'IceBreaker'), 
			('appver', '2.0'), 
			('appurl', 'https://pushcoin.com/Pub/SDK/WelcomeDevelopers'), 
			('author', 'PushCoin <ask@pushcoin.com>'), 
			('os', '%s %s' % (sys.platform, sys.version)), 
		]
		bo.write_uint( len(user_agent) )
		for kv in user_agent:
			bo.write_varstr( kv[0] )
			bo.write_varstr( kv[1] )

		#------------------------------------
		#       Register Message
		#------------------------------------
		req = pcos.Doc( name="Re" )
		req.add( bo )

		# send the message and process the result.
		res = self.send( req )

		assert res.message_id == 'Ac'
		# jump to the block of interest
		out_bo = res.block( 'Bo' )

		# read MAT returned by the server
		mat = out_bo.read_varstr()

		log.info('Success (MAT: %s)', binascii.hexlify( mat ))


	# CMD: `ping'
	def ping(self):

		req = pcos.Doc( name="Pi" )
		res = self.send( req )
		# jump to the block of interest
		tm = res.block( 'Bo' )

		# read block field(s)
		tm_epoch = tm.read_ulong();
		server_time = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(tm_epoch))
		log.info('RETN %s', server_time )


	# CMD: `transaction key'
	def transaction_key(self):

		req = pcos.Doc( name="Tk" )
		res = self.send( req )
		# jump to the block of interest
		body = res.block( 'Bo' )

		# number of keys
		key_count = body.read_uint()

		for ix in xrange(0,key_count):
			keyid = body.read_fixstr(2)
			key_info = body.read_varstr()
			key_expiry = body.read_ulong()
			key_data = body.read_varstr()
			log.info('key %s, len %s bytes, expires on %s', key_info, len(key_data), time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(key_expiry)))

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
			"history": self.history,
			"balance": self.balance,
			"charge": self.charge,
			"transfer": self.transfer,
		}		

	def expect_success( self, res ):
		'''Shows details of Success PCOS message'''
		if res.message_id == "Ok":
			bo = res.block( 'Bo' )
			ref_data = bo.read_varstr() # ref_data
			transaction_id = bo.read_varstr() # tx-id
			log.info('Success (tx_id: %s, ref_data: %s)', binascii.hexlify( transaction_id ), binascii.hexlify( ref_data ))
		else:
			raise RuntimeError("'%s' not a Success message" % res.message_id)

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
		encoded = req.as_bytearray()

		# For debugging, we write request and response
		if self.options.is_writing_io:
			reqf = open('request.pcos', 'w')
			reqf.write( encoded )
			reqf.close()

		log.info('CALL (%s) %s %s', self.options.url, self.cmd, str(self.args) )
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
				ref_data = er.read_varstr()
				transaction_id = er.read_varstr()
				code = er.read_uint()
				what = er.read_varstr()
				raise RuntimeError('tx-id=%s;ref_data=%s;what=%s;code=%s' % (binascii.hexlify( transaction_id ), binascii.hexlify( ref_data ), what, code )) 
			else:
				raise RuntimeError('ERROR -- cause unknown') 

		# return a lightweight PCOS document 
		return res


def decimal_to_parts(value):
	'''Breaks down the decimal into a tuple of value and scale'''
	value = value.normalize()
	exp = int( value.as_tuple()[2] )
	# if scale is negative, we have to shift to preserve precision
	if exp < 0:
		return (long(value.shift(-(exp))), exp)
	else:
		return (long(value), 0)


if __name__ == "__main__":
	# start with basic logger configuration
	log.basicConfig(level=log.INFO, format='%(asctime)s %(levelname)s %(message)s')
	
	# program arguments
	usage = "usage: %prog [options] <command> [args]"
	version = "PushCoin IceBreaker v1.0"
	parser = OptionParser(usage, version = version)
	parser.add_option("-C", "--url", dest="url", action="store", default=PC_DEFAULT_API_URL, help="server URL")
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
