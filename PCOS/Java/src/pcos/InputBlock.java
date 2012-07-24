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

public interface InputBlock 
{
	// Parsers for primitives
	byte readByte() throws PcosError;
	byte[] readBytes(long size) throws PcosError;
	char readChar() throws PcosError;
	boolean readBool() throws PcosError;
	int readInt() throws PcosError;
	long readUint() throws PcosError;
	long readLong() throws PcosError;
	long readUlong() throws PcosError;
	double readDouble() throws PcosError;
	String readVarString() throws PcosError;
	String readFixString(long size) throws PcosError;
	byte[] readVarBytes() throws PcosError;
	byte[] readFixBytes(long size) throws PcosError;

	int readingPosition();
	String name();
	int size();
}