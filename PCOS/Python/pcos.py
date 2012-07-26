# Copyright (c) 2012 Minta, Inc.
#
# GNU General Public Licence (GPL)
# 
# This program is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free Software
# Foundation; either version 2 of the License, or (at your option) any later
# version.
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
# details.
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 59 Temple
# Place, Suite 330, Boston, MA  02111-1307  USA
#
__author__  = '''Slawomir Lisznianski <sl@minta.com>'''

import binascii, struct, ctypes

# This is an absolute minimum length (in bytes) for a PCOS serialized object:
#
#   min_size = sizeof_header(16) + sizeof_empty_unbounded_array(2)
#
# Note: the empty unbounded array implies no block enumerations.
MESSAGE_HEADER_LENGTH = 4

# Block-meta record has a max size: size_of_block_id(2) + max_int_varint(5)
MAX_BLOCK_META_LENGTH = 7

# Size of message and block identifier
MESSAGE_ID_LENGTH = 2
BLOCK_ID_LENGTH = 2

# PCOS object mime type
PCOS_MIME_TYPE = 'application/pcos'

# Minimum size of block enumartion segment
MIN_BLOCK_ENUMARTION_SIZE = 1

# Largest unsigned long
MAX_UINT=4294967295
MAX_ULONG=18446744073709551615

# PUSHCOiN protocol identifier (aka "magic")
PROTOCOL_MAGIC = b'PC'
PROTOCOL_MAGIC_LEN = 2

# PCOS parser error codes
ERR_INTERNAL_ERROR = 100
ERR_MALFORMED_MESSAGE = 101
ERR_BAD_MAGIC = 102

# Helper to copy from bytearray to ctypes buffer
def _copy_bytearray_to_ctype_buffer(src, dst, count, dst_offset=0):
	# create storage to hold the data-pointer
	src_ptr_holder = ctypes.c_byte * len(src)
	src_ptr = src_ptr_holder.from_buffer(src)
	# copy the data
	ctypes.memmove(ctypes.byref( dst, dst_offset ), src_ptr, count)
	return count


# Helper to copy from bytes to ctypes buffer
def _copy_bytes_to_ctype_buffer(src, dst, count, dst_offset=0):
	# copy the data
	ctypes.memmove(ctypes.byref( dst, dst_offset ), src, count)
	return count


# Convert integer to a varint, return a bytearray
def _to_varint( val ):
	dat = bytearray()
	while val > 0x7f:
		octet = val & 0x7f
		
		# first one doesn't need continuation bit --
		# after reversing it will be last
		if dat:
			octet |= 0x80

		dat.append(octet)
		val >>= 7

	# deal with last (or only) octet
	if dat:
		val |= 0x80
		dat.append( val )
		# obey network order
		dat.reverse()
	else:
		dat.append( val )

	return dat


class PcosError( Exception ):
	""" Basis for all exceptions thrown from the PCOS codec."""

	def __init__(self, code, what = ''):
		self.code = code
		self.what = what

	def __str__(self):
		return repr("code=%s;what=%s" % (self.code, self. what) )


class BlockMeta:
	"""Stores meta information about a block found in the data-segment"""
	pass


class Doc:
	"""Parses binary data, presumably PCOS-encoded, and constructs a lightweight document."""

	def __init__( self, data = None, name = None ): 
		"""Constructs PCOS from binary data."""

		if name and len( name ) != MESSAGE_ID_LENGTH:
				raise PcosError( ERR_MALFORMED_MESSAGE, 'malformed message-ID' )
			
		self.message_id = name 

		# map of blocks, such that for a given block-name, we can quickly access its data
		self.blocks = { }

		if data:
			payload = parse_block(data, name, 'Hd')
			if payload.size() < MESSAGE_HEADER_LENGTH + 1:
				raise PcosError( ERR_MALFORMED_MESSAGE, 'payload too small for a valid message' )

			# parse the message header
			self.magic = payload.read_fixstr( PROTOCOL_MAGIC_LEN )

			# check if magic matches our encoding tag
			if self.magic != PROTOCOL_MAGIC:
				raise PcosError( ERR_BAD_MAGIC )

			# message ID
			self.message_id = payload.read_fixstr( MESSAGE_ID_LENGTH )

			# block count
			self.block_count = payload.read_uint()

			# data appears to be "one of ours", store it
			self.data = data
		
			# Enumerating blocks is a two-pass process -- first, we get their names and lengths,
			# then we can arrive at the beginning of the data segment.
			stage_blocks = []

			# Pass One: enumerate blocks
			for i in xrange(0, self.block_count):
				blk = BlockMeta()
				blk.name = payload.read_fixstr( BLOCK_ID_LENGTH )
				blk.length = payload.read_uint()
				stage_blocks.append(blk)

			# at this point remember where data-segment starts,
			# which is the location of the first block
			block_offset = payload.reading_position()

			# Pass Two: populate block positions within payload
			for blk in stage_blocks:
				# mark beginning of this block
				blk.start = block_offset

				# store the block meta-record in the directory
				self.blocks[blk.name] = blk

				# update position for the next block
				block_offset += blk.length
				
			if block_offset > payload.size():
				raise PcosError( ERR_MALFORMED_MESSAGE, "incomplete message or wrong block-meta info -- blocks couldn't fit in the received payload" )


	def block( self, name ):
		"""Returns the block iterator for a given block name."""

		meta = self.blocks.get( name, None )
		if meta:
			return create_input_block(self, meta)
		else:
			return None # Oops, block not found!


	def add( self, block ):
		"""Add a block to the data-segment."""

		self.blocks[ block.name() ] = block
		

	def as_bytearray( self ):
		"""Returns encoded byte-stream."""

		# allocate big enough C-buffer for storing wire data
		max_total_length = MESSAGE_HEADER_LENGTH + MIN_BLOCK_ENUMARTION_SIZE + MAX_BLOCK_META_LENGTH * len(self.blocks) + self._data_segment_size()
		payload = ctypes.create_string_buffer( max_total_length )

		# reset write position
		write_offset = 0

		# protocol magic
		write_offset += _copy_bytes_to_ctype_buffer(PROTOCOL_MAGIC, payload, PROTOCOL_MAGIC_LEN, write_offset)

		# message identifier
		write_offset += _copy_bytes_to_ctype_buffer(self.message_id, payload, MESSAGE_ID_LENGTH, write_offset)

		# number of blocks
		block_count_varint = _to_varint( len(self.blocks) )
		write_offset += _copy_bytearray_to_ctype_buffer(block_count_varint, payload, len(block_count_varint), write_offset)

		# block-metas
		for (name, b) in self.blocks.iteritems():
			# block name
			write_offset += _copy_bytes_to_ctype_buffer(name, payload, BLOCK_ID_LENGTH, write_offset)

			# block size
			block_size_varint = _to_varint( b.size() )
			write_offset += _copy_bytearray_to_ctype_buffer(block_size_varint, payload, len(block_size_varint), write_offset)

		# block data
		for (name, b) in self.blocks.iteritems():
			write_offset += _copy_bytearray_to_ctype_buffer(b.as_bytearray(), payload, b.size(), write_offset)

		buf_ptr_holder = ctypes.c_byte * write_offset
		buf_ptr = buf_ptr_holder.from_buffer(payload)

		return memoryview(buf_ptr).tobytes()


	def _data_segment_size( self ):
		"""(Private) Returns size of all data blocks."""
		size = 0
		for (name, b) in self.blocks.iteritems():
			size += b.size()
		return size


def create_output_block( name ):
	"""Creates and initializes a block in 'output' mode."""

	blk = Block()
	blk.mode = "O"
	blk.data = bytearray()
	blk._name = name
	return blk
	

def create_input_block( doc, meta ):
	"""Creates and initializes a block in 'input' mode."""

	blk = Block()
	blk.mode = "I"
	blk.doc = doc
	blk._name = meta.name
	blk._length = meta.length
	blk._start = meta.start
	blk._offset = 0 # current reading cursor position
	return blk


class Block:
	"""Provides facilities for creating a new block or iterating over and parsing block data."""

	def __init__( self ):
		pass


	def as_bytearray( self ):
		'''Returns a Python string from the character array.'''
		if self.mode == 'I':
			return self.doc.data[self._start : self._start + self._length]
		else:
			return self.data


	def size( self ):
		if self.mode == "I":
			return self._length
		else:
			return len(self.data)


	def name( self ):
		return self._name


	def reading_position( self ):
		return self._offset


	def read_byte( self ):
		assert self.mode == 'I'
		if self._length - self._offset > 0:
			octet = ord(self.doc.data[self._start + self._offset])
			self._offset += 1
			return octet

		raise PcosError( ERR_MALFORMED_MESSAGE, 'run out of input bytes to read from - incomplete or corrupted message' )


	def read_bytes( self, size ):
		assert self.mode == 'I'
		if self._length - self._offset >= size:
			begin = self._start + self._offset
			dat = self.doc.data[ begin : begin + size]
			self._offset += size
			return dat

		raise PcosError( ERR_MALFORMED_MESSAGE, 'run out of input bytes to read from - incomplete or corrupted message' )


	def read_char( self ):
		assert self.mode == 'I'
		if self._length - self._offset > 0:
			octet = self.doc.data[self._start + self._offset]
			self._offset += 1
			return octet


	def read_bool( self ):
		assert self.mode == 'I'
		return bool(self.read_byte())


	def read_varint( self, max_octets ):
		val = 0
		seen_end = False
		while max_octets > 0:
			octet = self.read_byte()
			val |= octet & 0x7f 
			# check if there is more...
			seen_end = bool( octet & 0x80 == 0 )
			if seen_end:
				break
			else:
				val <<= 7
				max_octets -= 1

		if not seen_end:
			raise PcosError( ERR_MALFORMED_MESSAGE, 'varint out of range' )

		return val
		

	def read_uint( self ):
		return self.read_varint( 5 ) # 5 => largest int can take on the wire


	def read_int( self ):
		# signed int is encoded as unsigned int
		val = self.read_uint()
		# ..but requires un-ZigZag
		return (val >> 1) ^ (-(val & 1))


	def read_ulong( self ):
		return self.read_varint( 10 ) # 10 => largest long can take up on the wire


	def read_long( self ):
		# signed long is encoded as unsigned long
		val = self.read_ulong()
		# ..but requires un-ZigZag
		return (val >> 1) ^ (-(val & 1))


	def read_double( self ):
		return struct.unpack("!d", self.read_bytes( 8 ))[0]


	def read_varstr( self ):
		length = self.read_uint()
		return self.read_bytes( length )


	def read_fixstr( self, length ):
		return self.read_bytes( length )


	def write_byte( self, val ):
		assert self.mode == 'O'
		self.data.extend( struct.pack("!B", val) )


	def write_bytes( self, val ):
		assert self.mode == 'O'
		self.data.extend( val )


	def write_char( self, val ):
		assert self.mode == 'O'
		self.data.extend( struct.pack("!c", val) )


	def write_bool( self, val ):
		if val:
			self.write_byte(1)
		else:
			self.write_byte(0)


	def write_uint( self, val ):
		if val > MAX_UINT:
			raise PcosError( ERR_INTERNAL_ERROR, '%s does not fit in (unsigned) varint base-32' % val )
		self.write_bytes( _to_varint( val ) )


	def write_int( self, val ):
		# signed numbers are converted to unsigned according to ZigZag
		zz = (val << 1) ^ (val >> 31)
		self.write_uint( zz )


	def write_ulong( self, val ):
		if val > MAX_ULONG:
			raise PcosError( ERR_INTERNAL_ERROR, '%s does not fit in (unsigned) varint base-64' % val )
		self.write_bytes( _to_varint( val ) )


	def write_long( self, val ):
		# signed numbers are converted to unsigned according to ZigZag
		zz = (val << 1) ^ (val >> 63)
		self.write_ulong( zz )


	def write_double( self, val ):
		self.data.extend( struct.pack("!d", val) )


	def write_varstr( self, val ):
		if val == None:
			self.write_uint( 0 )
			return
			
		length = len( val )
		self.write_uint( length )
		if length:
			self.write_bytes(val)


	def write_fixstr( self, val, size ):
		if len( val ) != size:
			raise PcosError( ERR_MALFORMED_MESSAGE, 'fixed array-field size not met: len(str) != %s' % size )
		self.write_bytes(val)


def parse_block(data, message_id, block_name):
	'''Returns a Block instance from raw block data'''
	blk = BlockMeta()
	blk.name = block_name
	blk.length = len(data)
	blk.start = 0

	doc = Doc( data = None, name = message_id )
	doc.data = data
	doc.blocks[block_name] = blk
	doc.magic = PROTOCOL_MAGIC
	doc.length = blk.length
	doc.block_count = 1
	return doc.block(block_name)


def _reading_test_pong():
	"""Tests if parser handles Pong message correctly"""

	# `Pong' message, normally arriving on the wire
	msg = Doc( _writing_test_pong() )

	# jump to the block of interest
	tm = msg.block( 'Tm' )

	# read block field(s)
	tm_epoch = tm.read_ulong();

	assert tm_epoch == 1335795040

	
def _datatype_test():
	"""Test serialization and de-serialization of all primitive types"""

	bo_in = create_output_block( 'Bo' )
	bo_in.write_byte( 44 )
	bo_in.write_bytes( 'Bytes' )
	bo_in.write_char( 'c' )
	bo_in.write_bool( False )
	bo_in.write_bool( True )

	bo_in.write_uint( 127 ) # single octet
	bo_in.write_uint( 128 ) # two octets
	bo_in.write_int( 63 ) # single octet
	bo_in.write_int( 64 ) # two octets

	bo_in.write_ulong( 127 ) # single octet
	bo_in.write_ulong( 128 ) # two octets
	bo_in.write_long( 63 ) # single octet
	bo_in.write_long( 64 ) # two octets
	bo_in.write_double(3.14)

	bo_in.write_varstr('variable string')
	bo_in.write_fixstr('fixed string', 12)

	outgoing = Doc( name="Te" )
	outgoing.add( bo_in )
	
	# Get encoded PCOS data 	
	generated_data = outgoing.encoded()

	reqf = open('data.pcos', 'w')
	reqf.write( generated_data )
	reqf.close()

	# Read back and test values
	incoming = Doc( generated_data )

	# jump to the block of interest
	bo_out = incoming.block( 'Bo' )

	assert bo_out.read_byte() == 44
	assert bo_out.read_bytes(5) == 'Bytes' 
	assert bo_out.read_char() == 'c'
	assert bo_out.read_bool() == False
	assert bo_out.read_bool() == True

	assert bo_out.read_uint() == 127
	assert bo_out.read_uint() == 128
	assert bo_out.read_int() == 63
	assert bo_out.read_int() == 64

	assert bo_out.read_ulong() == 127
	assert bo_out.read_ulong() == 128
	assert bo_out.read_long() == 63
	assert bo_out.read_long() == 64
	assert (abs(bo_out.read_double() - 3.14) < 0.001)

	assert bo_out.read_varstr() == 'variable string'
	assert bo_out.read_fixstr(12) == 'fixed string'


def _writing_test_pong():
	"""Test for PCOS Pong message"""

	tm = create_output_block( 'Tm' )
	tm.write_ulong( 1335795040 )

	msg = Doc( name="Po" )
	msg.add( tm )
	
	# Get encoded PCOS data 	
	generated_data = msg.encoded()

	# Comparison data
	sample_data = binascii.unhexlify( '5043506f01546d0584fcfaba60' )
	
	assert str(generated_data) == str(sample_data)
	return generated_data


def _writing_test_error():
	"""Test for PCOS Error message"""

	bo = create_output_block( 'Bo' )
	bo.write_uint( 100 )
	bo.write_varstr( 'only a test' )

	msg = Doc( name="Er" )
	msg.add( bo )
	
	# Get encoded PCOS data 	
	generated_data = msg.encoded()

	# Comparison data
	sample_data = binascii.unhexlify( '5043457201426f0d640b6f6e6c7920612074657374' )
	assert str(generated_data) == str(sample_data)


if __name__ == "__main__":
	"""Tests basic parser functionality."""

	# datatype serialization test
	_datatype_test()

	# Reading test...
	_reading_test_pong()
	
	# Writing test...
	_writing_test_pong()
	_writing_test_error()

	print "Looks good."

