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

public interface OutputBlock 
{
	// Parsers for primitives
	void writeByte( int v ) throws PcosError;
	void writeBytes( byte[] v ) throws PcosError;
	void writeChar( char c ) throws PcosError;
	void writeBool( boolean b ) throws PcosError;
	void writeInt(int val) throws PcosError; 
	void writeUint(long val) throws PcosError;
	void writeLong( long v) throws PcosError;
	void writeUlong( long v ) throws PcosError;
	void writeDouble( double v ) throws PcosError;
	void writeVarBytes( byte[] s ) throws PcosError;
	void writeFixBytes( byte[] s, int size) throws PcosError;
	void writeVarString( String s ) throws PcosError;
	void writeFixString( String s, int size) throws PcosError;

	String name();
	int size();
	byte[] toBytes() throws PcosError;
}
