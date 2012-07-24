// Copyright (c) 2012 Minta, Inc.
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
// __author__  = '''Slawomir Lisznianski <sl@minta.com>'''

package pcos;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class DocumentReader implements InputDocument 
{
	private String magic_;
	private String documentName_;
	private long blockCount_;
	private Map<String, InputBlock> blocks_ = new TreeMap<String, InputBlock>();

	private final class BlockMeta 
	{
		public long length;
		public String name;
	}

	@Override
	public String getMagic()
	{
		return magic_;
	}

	@Override
	public String getDocumentName()
	{
		return documentName_;
	}

	@Override
	public long getBlockCount()
	{
		return blockCount_;
	}

	@Override
	public Map<String, InputBlock> getBlocks() 
	{
		return Collections.unmodifiableMap(blocks_);
	}

	public DocumentReader(byte[] input) throws PcosError
	{
		InputBlock inblock = new BlockReader( input, 0, input.length, "Hd" );
		// read PCOS magic
		byte[] magic = inblock.readBytes( ProtocolTag.PROTOCOL_MAGIC_LEN );
		if (! Arrays.equals(ProtocolTag.PROTOCOL_MAGIC, magic))
			throw new PcosError( PcosErrorCode.ERR_INCOMPATIBLE_REQUEST, "not a PCOS message" );
		magic_ = new String(magic);
		// message (doc) name
		documentName_ = inblock.readFixString( ProtocolTag.MESSAGE_ID_LEN );
		
		// block count
		blockCount_ = inblock.readUlong();

		// Enumerating blocks is a two-pass process -- first, we get their names and lengths,
		// then we can arrive at the beginning of the data segment.
		AbstractList<BlockMeta> stageBlocks = new ArrayList<BlockMeta>();
		
		// Pass One: enumerate blocks
		for (int i = 0; i < blockCount_; ++i)
		{
			BlockMeta blk = new BlockMeta();
			blk.name = inblock.readFixString( ProtocolTag.BLOCK_ID_LENGTH );
			blk.length = inblock.readUlong();
			stageBlocks.add(blk);
		}

		// at this point remember where data-segment starts,
		// which is the location of the first block
		int block_offset = inblock.readingPosition();

		// Pass Two: populate block positions within payload
		for (BlockMeta blk : stageBlocks)
		{
			// store the block meta-record in the directory
			blocks_.put( blk.name, new BlockReader(input, block_offset, (int)blk.length, blk.name) );

			// update position for the next block
			block_offset += blk.length;
		}
			
		if ( block_offset > input.length ) {
			throw new PcosError( PcosErrorCode.ERR_MALFORMED_MESSAGE, "incomplete message or wrong block-meta info -- blocks couldn't fit in the received payload" );
		}
	}

	@Override
	public InputBlock getBlock(String name) 
	{
		return blocks_.get(name);
	}
}