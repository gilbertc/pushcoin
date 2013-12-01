/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pushcoin.icebreaker;

public class MessageId 
{
	final static int ACCOUNT_HISTORY_REQUEST = 1;
	final static int ACCOUNT_HISTORY_REPLY = 2;
	final static int ACCOUNT_HISTORY_PENDING = 3;
	final static int ACCOUNT_HISTORY_STOPPED = 4;

	final static int REGISTER_DEVICE_REQUEST = 11;
	final static int REGISTER_DEVICE_USER_CANCELED = 12;
	final static int REGISTER_DEVICE_PENDING = 13;
	final static int REGISTER_DEVICE_STOPPED = 14;
	final static int REGISTER_DEVICE_SUCCESS = 15;

	final static int MODEL_CHANGED = 21;
	final static int STATUS_CHANGED = 22;
	final static int DEVICE_NOT_ACTIVE = 31;
}
