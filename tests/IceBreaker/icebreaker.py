#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys, urllib2, time
import logging as log
import pcos, time, binascii, base64, hashlib, math, Image
from decimal import Decimal
from optparse import OptionParser,OptionError
from pyparsing import *
from M2Crypto import DSA, BIO, RSA

PC_DEFAULT_API_URL="https://api.pc-dev.com/pcos/"

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

		bo = pcos.Block( 'Bo', 64, 'O' )
		bo.write_fixed_string( binascii.unhexlify( self.args['mat'] ), size=20 ) # mat
		bo.write_short_string( '', max=127 ) # ref_data

		req = pcos.Doc( name="Bq" )
		req.add( bo )

		res = self.send( req )

		assert res.message_id == 'Br'

		# jump to body
		body = res.block( 'Bo' )
		ref_data = body.read_short_string( ) # ref_data

		value = body.read_int64() # value
		scale = body.read_int16() # scale
		balance_asofepoch = body.read_int64();

		balance = value * math.pow(10, scale)
		balance_asofdate = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(balance_asofepoch))
		log.info('Balance is $%s as of %s', balance, balance_asofdate)


	def history(self):
		'''Returns transaction history'''

		req = pcos.Doc( name="Hq" )
		bo = pcos.Block( 'Bo', 512, 'O' )
		bo.write_fixed_string( binascii.unhexlify( self.args['mat'] ), size=20 ) # mat
		bo.write_short_string( '', max=20 ) # ref-data
		bo.write_short_string( '', max=127 ) # keywords
		bo.write_int16( int(self.args['page']) )
		bo.write_int16( int(self.args['size']) )
		req.add( bo )

		res = self.send( req )

		assert res.message_id == 'Hr'

		# jump to body
		body = res.block( 'Bo' )

		ref_data = body.read_short_string() # ref-data

		# read number of transactions
		count = body.read_int16()
		for i in xrange(1, count+1):
			tx_id = binascii.hexlify( body.read_short_string() ) # transaction ID
			counterparty = binascii.hexlify( body.read_short_string() ) # counterparty ID
			epoch_tx_time = body.read_int64() # transaction time of day
			tx_time = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(epoch_tx_time))
			tx_type = body.read_fixed_string(1) # transaction type
			tx_context = body.read_fixed_string(1) # transaction context: (P)ayment or (T)ransfer

			#amount
			value = body.read_int64() # value
			scale = body.read_int16() # scale
			amount = value * math.pow(10, scale)
			# tax
			if bool(body.read_byte()):
				value = body.read_int64() # value
				scale = body.read_int16() # scale
				tax = value * math.pow(10, scale)
			else:
				tax = 'not provided'
			#tip
			if bool(body.read_byte()):
				value = body.read_int64() # value
				scale = body.read_int16() # scale
				tip = value * math.pow(10, scale)
			else:
				tip = 'not provided'
			
			currency = body.read_fixed_string(3) # currency
			merchant_name = body.read_short_string() # merchant name
			recipient = body.read_short_string() # PTA recipient
			ref_data = binascii.hexlify( body.read_short_string() ) # ref_data
			invoice = body.read_short_string() # invoice
			note = body.read_short_string() # note

			# address of the POS station
			if bool(body.read_byte()):
				street = body.read_short_string()
				city = body.read_short_string()
				state = body.read_short_string()
				zipc = body.read_short_string()
				country = body.read_fixed_string(2)
				address = '%s, %s, %s %s, %s' % (street, city, state, zipc, country)
			else:
				address = 'not provided'

			# contact info at the place of transaction origination
			if bool(body.read_byte()):
				phone = body.read_short_string()
				email = body.read_short_string()
				contact = 'phone: %s, email: %s' % (phone, email)
			else:
				contact = 'not provided'

			# geo-location of the place of transaction origination
			if bool(body.read_byte()):
				latitude = body.read_double()
				longitude = body.read_double()
				geolocation = '%s, %s' % (latitude, longitude)
			else:
				geolocation = 'not provided'

			print "--- %s/%s ---\ntx-id: %s\ncounterparty: %s\ntx_time: %s\naddress: %s\ngeolocation: %s\ncontact: %s\ntx_type: %s\ntx_context: %s\namount: %s\ntip: %s\ntax: %s\ncurrency: %s\nmerchant_name: %s\nrecipient: %s\nref-data: %s\ninvoice: %s\nnote: %s\n" % (i, count, tx_id, counterparty, tx_time, address, geolocation, contact, tx_type, tx_context, amount, tip, tax, currency, merchant_name, recipient, ref_data, invoice, note)

		log.info('Returned %s records', count)


	def preauth(self):
		'''Generates the PTA and submits to server for validation.'''
		pta_encoded = self.payment()

		# package PTA into a block
		pta = pcos.Block( 'Pa', 512, 'O' )
		pta.write_fixed_string(pta_encoded, size=len(pta_encoded))

		# create preauth block
		preauth = pcos.Block( 'Pr', 512, 'O' )
		preauth.write_fixed_string( binascii.unhexlify( self.args['preauth_mat'] ), size=20 ) # mat
		preauth.write_short_string( '', max=20 ) # user data
		# preauth amount
		(charge_int, charge_scale) = decimal_to_parts(Decimal(self.args['charge']))
		preauth.write_int64( charge_int ) # value
		preauth.write_int16( charge_scale ) # scale
		preauth.write_fixed_string( "USD", size=3 ) # currency

		# package everything and ship out
		req = pcos.Doc( name="Pr" )
		req.add( pta )
		req.add( preauth )

		res = self.send( req )
		self.expect_success( res )


	def transfer(self):
		'''Sends a Transfer Request'''
		pta_encoded = self.payment()

		# package PTA into a block
		pta = pcos.Block( 'Pa', 512, 'O' )
		pta.write_fixed_string(pta_encoded, size=len(pta_encoded))

		# create transfer-request block
		r1 = pcos.Block( 'R1', 1024, 'O' )
		r1.write_fixed_string( binascii.unhexlify( self.args['receiver_mat'] ), size=20 ) # mat
		r1.write_short_string( 'transfer-ref', max=127 ) # ref_data
		r1.write_int64( long( time.time() + 0.5 ) ) # request create-time

		# transfer amount
		(charge_value, charge_scale) = decimal_to_parts(Decimal(self.args['transfer']))

		r1.write_int64( charge_value ) # value
		r1.write_int16( charge_scale ) # scale

		r1.write_fixed_string( "USD", size=3 ) # currency
		r1.write_short_string( 'John paid his dept', max=127 ) # note

		# no geo-location available
		r1.write_byte(0)

		# package everything and ship out
		req = pcos.Doc( name="Tt" )
		req.add( pta )
		req.add( r1 )

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
		r1.write_short_string( 'charge-ref', max=127 ) # ref_data
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
		r1.write_short_string( 'inv-123', max=24 ) # invoice ID
		r1.write_short_string( 'happy meal', max=127 ) # note
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

		#------------------
		# PTA public-block
		#------------------
		p1 = pcos.Block( 'P1', 512, 'O' )
		now = long( time.time() + 0.5 )
		p1.write_int64( now ) # certificate create-time
		p1.write_int64( now + 24 * 3600 ) # certificate expiry (in 24 hrs)
		# p1.write_int64( now + 60*2 ) # certificate expiry

		# payment-limit
		(payment_int, payment_scale) = decimal_to_parts(Decimal(self.args['limit']))
		p1.write_int64( payment_int ) # value
		p1.write_int16( payment_scale ) # scale

		# gratuity
		tip = tip_type = None
		tipv = self.args.get('tip_pct', None)
		if tipv:
			tip_type = 'P'
		else:
			tipv = self.args.get('tip_abs', None)
			if tipv:
				tip_type = 'A'

		if tipv:
			(tip_int, tip_scale) = decimal_to_parts(Decimal(tipv))
			p1.write_byte(1) # optional indicator
			p1.write_fixed_string(tip_type, size=1) # tip type (P or A)
			p1.write_int64( tip_int ) # value
			p1.write_int16( tip_scale ) # scale
		else:
			p1.write_byte(0) # optional indicator -- no tip

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
		priv.write_fixed_string( binascii.unhexlify( self.args['mat'] ), size=20 ) # mat
		priv.write_short_string( '', max=20 ) # ref-data
		
		# sign the public-block
		#   * first, produce the checksum
		digest = hashlib.sha1(str(p1)).digest()
		
		#   * then sign the checksum
		dsa_priv_key = BIO.MemoryBuffer( TEST_DSA_KEY_PRV_PEM )
		signer = DSA.load_key_bio( dsa_priv_key )
		signature = signer.sign_asn1( digest )
		priv.write_short_string( signature, max=48 ) # signature of the pub block
		priv.write_short_string( '', max=20 ) # empty user data

		# encrypt the private-block
		txn_pub_key = BIO.MemoryBuffer( API_TRANSACTION_KEY_PEM )
		encrypter = RSA.load_pub_key_bio( txn_pub_key )
		# RSA Encryption Scheme w/ Optimal Asymmetric Encryption Padding
		encrypted = encrypter.public_encrypt( str(priv), RSA.pkcs1_oaep_padding )

		# At this point we no longer need the private object. We only attach the
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
			qr = qrcode.QRCode(version=None, box_size=3, error_correction=qrcode.constants.ERROR_CORRECT_L)
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
		# user-agent attributes
		user_agent = [
			('appname', 'IceBreaker'), 
			('appver', '1.0'), 
			('appurl', 'https://pushcoin.com/Pub/SDK/WelcomeDevelopers'), 
			('author', 'PushCoin <ask@pushcoin.com>'), 
			('os', '%s %s' % (sys.platform, sys.version)), 
		]
		bo.write_byte( len(user_agent) )
		for kv in user_agent:
			bo.write_short_string( kv[0], max=32 )
			bo.write_short_string( kv[1], max=127 )

		req.add( bo )
		res = self.send( req )

		assert res.message_id == 'Ac'
		# jump to the block of interest
		tm = res.block( 'Bo' )
		mat = tm.read_fixed_string(20);
		log.info('Success (mat: %s)', binascii.hexlify( mat ))


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
			"history": self.history,
			"balance": self.balance,
			"charge": self.charge,
			"transfer": self.transfer,
		}		

	def expect_success( self, res ):
		'''Shows details of Success PCOS message'''
		if res.message_id == "Ok":
			bo = res.block( 'Bo' )
			ref_data = bo.read_short_string() # ref_data
			transaction_id = bo.read_short_string() # tx-id
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
		encoded = req.encoded()

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
				ref_data = er.read_short_string();
				transaction_id = er.read_short_string();
				code = er.read_int32();
				what = er.read_short_string();
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
