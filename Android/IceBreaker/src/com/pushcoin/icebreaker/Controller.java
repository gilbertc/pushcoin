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

package com.pushcoin.icebreaker;
import android.os.Handler;
import android.os.Message;

public interface Controller
{
	// Ask for fresh data
	void reload();

	// Model access
	String getBalance();
	String getBalanceTime();
	String getStatus();
	PcosHelper.TransactionInfo getTransaction(int index);
	int getHistorySize();

	void registerHandler( Handler h, int messageId );
	void post( Message m );
}
