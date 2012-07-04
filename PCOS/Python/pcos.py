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

# Largest unsigned long
MAX_ULONG=18446744073709551615

# PUSHCOiN protocol identifier (aka "magic")
PROTOCOL_MAGIC = b'PC'
PROTOCOL_MAGIC_LEN = len(PROTOCOL_MAGIC)

# PCOS parser error codes
ERR_INTERNAL_ERROR = 100
ERR_MALFORMED_MESSAGE = 101
ERR_INCOMPATIBLE_REQUEST = 102


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
	while True:
		octet = val & 0x7f
		
		# requires more bytes?
		if val > 0x7f:
			octet |= 0x80
			dat.append(octet)
			val >>= 7
		else:
			dat.append(octet)
			break
	sz = len(dat)
	# reverse for network-order
	dat.reverse()
	return dat


class PcosError( Exception ):
	""" Basis for all exceptions thrown from the PCOS codec."""

	def __init__(self, code, what = '', ref_data = ''):
		self.code = code
		self.what = what
		self.ref_data = ref_data

	def __str__(self):
		return repr("code=%s;what=%s" % (self.code, self. what) )


class BlockMeta:
	"""Stores meta information about a block found in the data-segment"""
	pass


class Doc:
	"""Parses binary data, presumably PCOS-encoded, and constructs a lightweight document."""

	def __init__( self, data = None, name = None ): 
		"""Constructs PCOS from binary data."""

		if name and len( name ) != 2:
				raise PcosError( ERR_MALFORMED_MESSAGE, 'malformed message-ID' )
			
		self.message_id = name 

		# map of blocks, such that for a given block-name, we can quickly access its data
		self.blocks = { }

		if data:
			payload_length = len( data )
			if payload_length < MIN_MESSAGE_LENGTH:
				raise PcosError( ERR_MALFORMED_MESSAGE, 'payload too small for a valid message' )

			# parse the message header
			self.magic, self.length, self.message_id, reserved, self.block_count = Doc._HEADER_PARSER.unpack_from( data )

			# check if magic matches our encoding tag
			if self.magic != PROTOCOL_MAGIC:
				raise PcosError( ERR_INCOMPATIBLE_REQUEST )

			# check if payload is big enough to even hold block_count meta records
			# -- we could be lied!
			block_offset = MIN_MESSAGE_LENGTH + self.block_count * BLOCK_META_LENGTH

			if payload_length < block_offset:
				raise PcosError( ERR_MALFORMED_MESSAGE, 'payload too small for meta data' )

			# data appears to be "one of ours", store it
			self.data = data
		
			# parse block-meta segment
			meta_offset = MIN_MESSAGE_LENGTH
			total_claimed_length = MIN_MESSAGE_LENGTH

			for i in range(0, self.block_count):
				blk = BlockMeta()
				blk.name, blk.length = Doc._BLOCK_META.unpack_from( data, meta_offset )
				blk.start = block_offset

				# store block meta-record
				self.blocks[blk.name] = blk

				# update running totals
				block_offset += blk.length
				meta_offset += BLOCK_META_LENGTH
				total_claimed_length += blk.length + BLOCK_META_LENGTH

			# all the block meta information collected -- check if payload's large enough
			# to hold all the "claimed" block-data
			if total_claimed_length < payload_length:
				raise PcosError( ERR_MALFORMED_MESSAGE, "actual block sizes don't match provided meta data" )


	def block( self, name ):
		"""Returns the block iterator for a given block name."""

		meta = self.blocks.get( name, None )
		if not meta:
			return None # Oops, block not found!

		return Block(self, meta, 'I')


	def add( self, block ):
		"""Add a block to the data-segment."""

		self.blocks[ block.name() ] = block
		

	def encoded( self ):
		"""Returns encoded byte-stream."""

		# allocate big enough C-buffer for storing wire data
		max_total_length = MESSAGE_HEADER_LENGTH + MAX_BLOCK_META_LENGTH * len(self.blocks) + self._data_segment_size()
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
			write_offset += _copy_bytearray_to_ctype_buffer(b.data, payload, b.size(), write_offset)

		return payload.raw


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
			return self.doc.data[self.meta.start : self.meta.start + self.meta.length]
		else:
			return self.data


	def size( self ):
		if self.mode == "I":
			return self.meta.length
		else:
			return len(self.data)


	def name( self ):
		return self._name


	def read_byte( self ):
		assert self.mode == 'I'
		if self._length - self._offset > 0:
			octet = ord(self.doc.data[self._start + self._offset])
			self._offset += 1
			return octet

		raise PcosError( ERR_MALFORMED_MESSAGE, 'run out of input bytes to read from - accessing data out of bounds' )


	def read_bytes( self, size ):
		assert self.mode == 'I'
		if self._length - self._offset >= size:
			begin = self._start + self._offset
			dat = self.doc.data[ begin : begin + size]
			self._offset += size
			return dat

		raise PcosError( ERR_MALFORMED_MESSAGE, 'run out of input bytes to read from - accessing data out of bounds' )


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
			seen_end = bool(octet & 0x80)
			if seen_end:
				break
			else:
				val <<= 7
				max_octets -= 1

		if not seen_end:
			raise PcosError( ERR_MALFORMED_MESSAGE, 'varint out of range' )
		

	def read_uint( self ):
		return self.read_varint( 5 ) # 5 => largest int (base32) can take up to 5 bytes on the wire


	def read_int( self ):
		# signed int is encoded as unsigned int
		val = self.read_uint()
		# ..but requires un-ZigZag
		return (val >> 1) ^ (-(val & 1))


	def read_ulong( self ):
		return self.read_varint( 11 ) # 11 => largest long (base64) can take up to 11 bytes on the wire


	def read_long( self ):
		# signed long is encoded as unsigned long
		val = self.read_ulong()
		# ..but requires un-ZigZag
		return (val >> 1) ^ (-(val & 1))


	def read_double( self ):
		return struct.unpack("!d", self.read_bytes( 8 ))


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
		if val > sys.maxint:
			raise PcosError( ERR_INTERNAL_ERROR, '%s does not fit in (unsigned) varint base-32' % val )
		self.write_bytes( _to_varint( val ) )


	def write_int( self, val ):
		# signed numbers are converted to unsigned according to ZigZag
		zz = (val << 1) ^ (val >> 31)
		write_uint( self, zz )


	def write_ulong( self, val ):
		if val > MAX_ULONG:
			raise PcosError( ERR_INTERNAL_ERROR, '%s does not fit in (unsigned) varint base-64' % val )
		self.write_bytes( _to_varint( val ) )


	def write_long( self, val ):
		# signed numbers are converted to unsigned according to ZigZag
		zz = (val << 1) ^ (val >> 63)
		write_ulong( self, zz )


	def write_double( self, val ):
		self.data.extend( struct.pack("!d", val) )


	def write_varstr( self, val ):
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
	data = binascii.unhexlify( '50434f53506f16000100546d0800609d9e4f00000000' )

	# read message preamble, create a lightweight PCOS document 
	msg = Doc( data )

	# jump to the block of interest
	tm = msg.block( 'Tm' )

	# read block field(s)
	tm_epoch = tm.read_int64();

	assert tm_epoch == 1335795040

	
def _writing_test_pong():
	"""Tests if parser produces correct PCOS Pong message"""

	tm = create_output_block( 'Tm' )
	tm.write_ulong( 1335795040 )

	msg = Doc( name="Po" )
	msg.add( tm )
	
	# Get encoded PCOS data 	
	generated_data = msg.encoded()

	reqf = open('data.pcos', 'w')
	reqf.write( generated_data )
	reqf.close()

	# Comparison data
	sample_data = binascii.unhexlify( '50434f53506f16000100546d0800609d9e4f00000000' )
	assert str(generated_data) == str(sample_data)


def _writing_test_error():
	"""Tests if parser produces correct PCOS Error message"""

	bo = create_output_block( 'Bo' )
	bo.write_int32( 100 )
	bo.write_fixed_string( 'miss' )

	msg = Doc( name="Er" )
	msg.add( bo )
	
	# Get encoded PCOS data 	
	generated_data = msg.encoded()

	reqf = open('data.pcos', 'w')
	reqf.write( generated_data )
	reqf.close()

	# Comparison data
	sample_data = binascii.unhexlify( '50434f53457216000100426f0800640000006d697373' )
	assert str(generated_data) == str(sample_data)


if __name__ == "__main__":
	"""Tests basic parser functionality."""

	# Reading test...
#_reading_test_pong()
	
	# Writing test...
	_writing_test_pong()
	_writing_test_error()

	print "Looks good."

