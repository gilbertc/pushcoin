// Copyright (c) 2012 PushCoin, Inc.
//
// GNU General Public Licence (GPL)
// 
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
// details.
// You should have received a copy of the GNU General Public License along with
// this program; if not, write to the Free Software Foundation, Inc., 59 Temple
// Place, Suite 330, Boston, MA  02111-1307  USA
//
// __author__  = '''Slawomir Lisznianski <sl@pushcoin.com>'''

package pcos;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.ArrayList;

public class DocumentWriter implements OutputDocument
{
	private	AbstractList<OutputBlock> blocks_ = new ArrayList<OutputBlock>();
	private final String name_;
	
	public DocumentWriter( String name ) throws PcosError
	{
		if ( name.length() != ProtocolTag.MESSAGE_ID_LEN ) {
			throw new PcosError( PcosErrorCode.ERR_MALFORMED_MESSAGE, "invalid message-ID" );
		}
		name_ = name;
	}
		
	@Override
	public void addBlock(OutputBlock b) throws PcosError
	{
		blocks_.add( b );
	}

	/**
	 * Computes total size of all blocks.
	 */
	private int calcDataSegmentSize()
	{
		int size = 0;
		for (OutputBlock b : blocks_) {
			size += b.size();
		}
		return size;
	}

	/**
	 *  Returns an output stream for a ByteBuffer.
	 */
	private static OutputStream asOutputStream(final ByteBuffer buf) 
	{
	    return new OutputStream() 
	    {
	        public void write(int b) throws IOException {
	            buf.put((byte)b);
	        }

	        public void write(byte[] bytes, int off, int len) throws IOException {
	            buf.put(bytes, off, len);
	        }
	    };
	}
	
	/**
	 * 	Returns PCOS byte-array.
	 */
	@Override
	public byte[] toBytes() throws PcosError
	{
		// allocate big enough C-buffer for storing wire data
		int max_total_length = 
			ProtocolTag.MESSAGE_HEADER_LENGTH + 
			ProtocolTag.MIN_BLOCK_ENUMARTION_SIZE + 
			ProtocolTag.MAX_BLOCK_META_LENGTH * blocks_.size() + 
			calcDataSegmentSize();
		
		ByteBuffer payload = ByteBuffer.allocate( max_total_length );
		OutputStream ostream = asOutputStream( payload );
		BlockWriter writer = new BlockWriter("??", ostream);

		// protocol magic
		writer.writeBytes( ProtocolTag.PROTOCOL_MAGIC );
		
		// protocol flags
		writer.writeByte( ProtocolTag.PROTOCOL_FLAGS );

		// message identifier
		writer.writeFixString( name_, ProtocolTag.MESSAGE_ID_LEN );

		// number of blocks
		writer.writeUlong( blocks_.size() );

		// block-metas
		for (OutputBlock blk : blocks_)
		{
			// block name
			writer.writeFixString( blk.name(), ProtocolTag.BLOCK_ID_LENGTH );
			
			// block size
			writer.writeUlong( blk.size() );
		}

		// block data
		for (OutputBlock blk : blocks_) {
			writer.writeBytes( blk.toBytes() );
		}
		
		// byte array with PCOS message
		return payload.array();
	}

}
