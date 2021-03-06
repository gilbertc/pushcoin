#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys, urllib2, time
import logging as log
import pcos, time, binascii, base64, hashlib, math, Image
from decimal import Decimal
from optparse import OptionParser,OptionError
from pyparsing import *
from M2Crypto import DSA, BIO, RSA
from pprint import pprint

PC_DEFAULT_API_URL="https://api.minta.com/pcos/"

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

def printplus(obj):
    """
    Pretty-prints the object passed in.

    """
    # Dict
    if isinstance(obj, dict):
        for k, v in sorted(obj.items()):
            print u'{0}: {1}'.format(k, v)

    # List or tuple            
    elif isinstance(obj, list) or isinstance(obj, tuple):
        for x in obj:
            print x

    # Other
    else:
        print obj

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


	def error_report(self):
		'''Submits an error report'''

		#------------------------------------
		#      Request Body Block
		#------------------------------------
		out_bo = pcos.create_output_block( 'Bo' )
		# MAT
		out_bo.write_bytestr( binascii.unhexlify( self.args['mat'] ) )
		# mime
		out_bo.write_string( 'text/plain'  )
		# report content
		out_bo.write_bytestr( self.args['report']  )

		req = pcos.Doc( name="Er" )
		req.add( out_bo )

		res = self.send( req )
		self.expect_success( res )


	def history(self):
		'''Returns transaction history'''

		# page number and page-size
		page_num = int(self.args['page'])
		page_size = int(self.args['size'])

		#------------------------------------
		#      Request Body Block
		#------------------------------------
		out_bo = pcos.create_output_block( 'Bo' )
		# MAT
		out_bo.write_bytestr( binascii.unhexlify( self.args['mat'] ) )

		# page size and offset
		out_bo.write_uint( page_num )
		out_bo.write_uint( page_size )

		#------------------------------------
		#    History Query Message
		#------------------------------------
		req = pcos.Doc( name="TxnHistoryQuery" )
		req.add( out_bo )

		res = self.send( req )

		assert res.message_id == 'TxnHistoryReply'

		#------------------------------------
		#      Balanace segment
		#------------------------------------
		balance_seg = res.block( 'Bl' )

		rs={}

		#amount
		value = balance_seg.read_long() # value
		scale = balance_seg.read_int() # scale
		balance_amount = value * math.pow(10, scale)

		# read block field(s)
		tm_epoch = balance_seg.read_ulong();
		balance_as_of_date = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(tm_epoch))

		#------------------------------------
		#   Transaction History segment
		#------------------------------------
		hist_seg = res.block( 'Tr' )

		# total transactions found
		total_count = hist_seg.read_uint()

		# read number of transactions
		count = hist_seg.read_uint()

		for i in xrange(1, count+1):
			print( '-- [ Row %s ] --' % (i+page_num) )
			rs['txn_id'] = hist_seg.read_string()
			rs['device_name'] = hist_seg.read_string()
			rs['txn_as_of_date'] = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(hist_seg.read_ulong()))
			rs['txn_type'] = hist_seg.read_string()
			rs['txn_context'] = hist_seg.read_string()
			rs['currency'] = hist_seg.read_string()

			value = hist_seg.read_long() # value
			scale = hist_seg.read_int() # scale
			rs['txn_amount'] = value * math.pow(10, scale)

			# tax
			if hist_seg.read_bool():
				value = hist_seg.read_long() # value
				scale = hist_seg.read_int() # scale
				rs['txn_tax'] = value * math.pow(10, scale)

			#tip
			if hist_seg.read_bool():
				value = hist_seg.read_long() # value
				scale = hist_seg.read_int() # scale
				rs['txn_tip'] = value * math.pow(10, scale)

			rs['counterparty'] = hist_seg.read_string()
			rs['invoice'] = hist_seg.read_string()
			rs['note'] = hist_seg.read_string()

			# address of the POS station
			if hist_seg.read_bool():
				street = hist_seg.read_string()
				city = hist_seg.read_string()
				state = hist_seg.read_string()
				zipc = hist_seg.read_string()
				country = hist_seg.read_string()
				rs['merchant_address'] = '%s, %s, %s %s, %s' % (street, city, state, zipc, country)

			# contact info
			if hist_seg.read_bool():
				rs['merchant_phone'] = hist_seg.read_string()
				rs['merchant_email'] = hist_seg.read_string()

			# geo location
			if hist_seg.read_bool():
				rs['txn_latitude'] = hist_seg.read_double()
				rs['txn_longitude'] = hist_seg.read_double()

			rs['txn_status'] = hist_seg.read_string()
			
			# rating and score
			rs['user_rating'] = hist_seg.read_uint()
			rs['merchant_score'] = hist_seg.read_double()
 			rs['vote_count'] = hist_seg.read_uint()

			printplus( rs )
			print( '------' )

		page_size = int(self.args['size'])
		log.info('Returned page %s of %s, balance %s as of %s', self.args['page'], int(math.ceil(total_count/page_size)), balance_amount, balance_as_of_date )


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


	def charge_key(self):
		'''Sends a Payment Request with PTK'''
		self.charge( self.payment_key() )


	def charge_pta(self):
		'''Sends a Payment Request with PTA'''
		self.charge( self.payment_pta() )
		

	def charge(self, payment_bytes):
		#------------------------------------
		#      Payment Authorization Block
		#------------------------------------
		payment_auth_block = pcos.create_output_block( 'Py' )
		payment_auth_block.write_bytes(payment_bytes)

		#------------------------------------
		#      Payment Request Block
		#------------------------------------
		paymnt_req_block = pcos.create_output_block( 'R1' ) 

		# mat
		paymnt_req_block.write_bytestr( binascii.unhexlify( self.args['receiver_mat'] ) )

		# ref data
		paymnt_req_block.write_bytestr( '' )

		# create-time
		paymnt_req_block.write_ulong( long( time.time() + 0.5 ) )

		(charge_int, charge_scale) = decimal_to_parts(Decimal(self.args['charge']))
		paymnt_req_block.write_long( charge_int ) # value
		paymnt_req_block.write_int( charge_scale ) # scale

		# tax
		paymnt_req_block.write_bool(True) # optional indicator
		(tax_value, tax_scale) = decimal_to_parts(Decimal(self.args['tax']))
		paymnt_req_block.write_long( tax_value ) # value
		paymnt_req_block.write_int( tax_scale ) # scale

		# tip
		paymnt_req_block.write_bool(True) # optional indicator
		(tip_value, tip_scale) = decimal_to_parts(Decimal(self.args['tip']))
		paymnt_req_block.write_long( tip_value ) # value
		paymnt_req_block.write_int( tip_scale ) # scale

		# passcode
		paymnt_req_block.write_string( '1111' )

		# currency
		paymnt_req_block.write_bytes( b"USD" )

		# invoice ID
		paymnt_req_block.write_string( 'inv-123' )

		# note
		paymnt_req_block.write_string( 'happy meal' )

		# no geo-location available
		paymnt_req_block.write_bool(False)

		# list of purchased goods
		paymnt_req_block.write_int(0)

		#------------------------------------
		#      Payment Request Message
		#------------------------------------
		req = pcos.Doc( name="PaymentReq" )
		req.add( payment_auth_block )
		req.add( paymnt_req_block )

		res = self.send( req )
		self.expect_message(res, 'PaymentAck')
		
		# # read balance
		# balance = res.block( 'Bo' )
		# ref_data = balance.read_varstr() # ref_data
		# transaction_id = balance.read_varstr() # tx-id

		# if res.message_id == 'Np':
		# 	code = er.read_uint()
		# 	what = er.read_varstr()
		# 	
		# # exact amount given?
		# if balance.read_bool():
		# 	exact_balance = 'is'
		# else:
		# 	exact_balance = 'is greater than' 
		# # amount
		# value = balance.read_long() # value
		# scale = balance.read_int() # scale
		# balance_amount = value * math.pow(10, scale)
		# # time of last update
		# tm_epoch = balance.read_ulong();
		# balance_asofdate = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(tm_epoch))

		# if res.message_id == 'Np':
		# 	log.warn('%s (tx_id: %s). Balance %s $%s as of %s', what, base64.b32encode(transaction_id), exact_balance, balance_amount, balance_asofdate)
		# else:
		# 	log.info('Success (tx_id: %s). Balance %s $%s as of %s', base64.b32encode(transaction_id), exact_balance, balance_amount, balance_asofdate)

	def payment_key(self):
		'''Generates the Payment Transaction Key, or PTK. It does not communicate with the server.'''

		#------------------------------------
		#        PTK Payload v1 Block
		#------------------------------------

		p1 = pcos.create_output_block( 'P1' )
		# membership ID
		device_id = self.args['device_id'] 
		p1.write_string( device_id )

		#------------------------------------
		#        PTK M1HW Block
		#------------------------------------

		m1hw = pcos.create_output_block( 'M1' )

		# tag serial number
		m1hw.write_bytestr( binascii.unhexlify(self.args['serial_number']) )

		# app challenge seed
		m1hw.write_bytestr( binascii.unhexlify(self.args['seed']) )

		# transaction key ID
		m1hw.write_bytes( binascii.unhexlify(self.args['keyid']) )

		# PUF response
		m1hw.write_bytestr( binascii.unhexlify(self.args['puf_response']) )

		# fake challenge-response time (ms)
		m1hw.write_uint( 92 )

		#------------------------------------
		# PTK Message
		#  * Payload Block
		#  * M1HW Block
		#------------------------------------
		ptk = pcos.Doc( name="PaymentKey" )
		ptk.add( p1 )
		ptk.add( m1hw )
		ptk_bytes = ptk.as_bytearray()

		# write serialized data as binary and qr-code
		payload_file = open('ptk.pcos', 'w')
		payload_file.write( ptk_bytes )
		payload_file.close()
		print ("Saved PTK object to 'ptk.pcos'")

		return ptk_bytes


	def payment_pta(self):
		'''Generates the Payment Transaction Authorization, or PTA. It does not communicate with the server.'''

		#------------------------------------
		#        PTA Payment Block
		#------------------------------------
		p1 = pcos.create_output_block( 'P1' )

		# member authentication token
		mat = self.args['mat'] 
		if len( mat ) != 40:
			raise RuntimeError("MAT must be 40-characters long" % self.cmd)
		p1.write_varstr( binascii.unhexlify( mat ) )

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

	
	def provision(self):
		'''Provision a device'''

		#------------------------------------
		#           Body Block
		#------------------------------------
		out_bo = pcos.create_output_block( 'Bo' )

		# MAT
		out_bo.write_varstr( binascii.unhexlify( self.args['mat'] ) )
		# Serial number ID
		out_bo.write_varstr( binascii.unhexlify(self.args['serial_number']) )

		#------------------------------------
		#       Provision Message
		#------------------------------------
		req = pcos.Doc( name="Ov" )
		req.add( out_bo )

		# send the message and process the result.
		res = self.send( req )

		self.expect_message(res, 'Cr')

		# jump to the block of interest
		in_bo = res.block( 'Bo' )

		# read MAT returned by the server
		device_id = in_bo.read_varstr()
		registration_id =in_bo.read_varstr()

		log.info('provision-success| device-ID: %s, registration-ID: %s', binascii.hexlify( device_id ), registration_id )


	def register(self):
		'''Register command'''

		#------------------------------------
		#           Body Block
		#------------------------------------
		bo = pcos.create_output_block( 'Bo' )

		# registration ID
		bo.write_string( self.args['registration_id'] )

		# DER encoded public key -- signature verification key
		bo.write_bytestr( base64.b64decode(TEST_DSA_KEY_PUB_PEM) )

		# # user-agent attributes
		# user_agent = [
		# 	('appname', 'IceBreaker'), 
		# 	('appver', '2.0'), 
		# 	('appurl', 'https://pushcoin.com/Pub/SDK/WelcomeDevelopers'), 
		# 	('author', 'PushCoin <ask@pushcoin.com>'), 
		# 	('os', '%s %s' % (sys.platform, sys.version)), 
		# ]
		# bo.write_uint( len(user_agent) )
		# for kv in user_agent:
		# 	bo.write_varstr( kv[0] )
		# 	bo.write_varstr( kv[1] )

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
		mat = out_bo.read_bytestr()

		log.info('Success (MAT: %s)', binascii.hexlify( mat ))


	# CMD: `ping'
	def ping(self):

		req = pcos.Doc( name="Ping" )
		res = self.send( req )

		self.expect_message(res, 'Pong')

		# jump to the block of interest
		tm = res.block( 'Bo' )

		# read block field(s)
		tm_epoch = tm.read_ulong();
		server_time = time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(tm_epoch))
		log.info('RETN %s', server_time )


	# CMD: `transaction key'
	def transaction_key(self):

		req = pcos.Doc( name="TxnKeyQuery" )
		res = self.send( req )
		# jump to the block of interest
		body = res.block( 'Bo' )

		# number of keys
		key_count = body.read_uint()

		for ix in xrange(0,key_count):
			keyid = body.read_bytes(2)
			key_info = body.read_string()
			key_expiry = body.read_ulong()
			key_data = body.read_bytestr()
			log.info('Key ID %s, type %s, len %s bytes, expires on %s', binascii.hexlify(keyid), key_info, len(key_data), time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime(key_expiry)))


	def __init__(self, options, cmd, args):
		# store the cmd and args for the command-handler
		self.options = options
		self.cmd = cmd
		self.args = args

		# list of commands (PushCoin requests) we are supporting:
		self.lookup = {
			"ping": self.ping,
			"register": self.register,
			"payment-pta": self.payment_pta,
			"payment-key": self.payment_key,
			"preauth": self.preauth,
			"transaction-key": self.transaction_key,
			"history": self.history,
			"error-report": self.error_report,
			"balance": self.balance,
			"charge-pta": self.charge_pta,
			"charge-key": self.charge_key,
			"transfer": self.transfer,
			"provision": self.provision,
		}		

	def expect_success( self, res ):
		'''Shows details of Success PCOS message'''
		if res.message_id == "Ok":
			bo = res.block( 'Bo' )
			ref_data = bo.read_bytestr() # ref_data
			transaction_id = bo.read_bytestr() # tx-id
			log.info('Success (tx_id: %s, ref_data: %s)', binascii.hexlify( transaction_id ), binascii.hexlify( ref_data ))
		else:
			raise RuntimeError("'%s' not a Success message" % res.message_id)

	def expect_message( self, res, name):
		'''Checks if message matches given name'''
		if res.message_id != name:
			raise RuntimeError("'%s' not a %s message" % (res.message_id, name))

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
				ref_data = er.read_bytestr()
				transaction_id = er.read_bytestr()
				code = er.read_uint()
				what = er.read_string()
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
