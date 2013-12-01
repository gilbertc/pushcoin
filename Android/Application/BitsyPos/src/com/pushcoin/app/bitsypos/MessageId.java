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

package com.pushcoin.app.bitsypos;

public class MessageId 
{
	final static int CATEGORY_CLICKED = 1;
	final static int ITEM_CLICKED = 2;
	final static int CUSTOMER_CLICKED = 3;
	final static int CHECKOUT_CLICKED = 4;
	// Events pertaining to the items within a single cart
	final static int CART_CONTENT_CHANGED = 10;
	// Transaction status changed
	final static int TRANSACTION_STATUS_CHANGED = 11;
	// Events pertaining to the collection of carts
	final static int CART_POOL_CHANGED = 20;
	// Query returns user data
	final static int QUERY_USERS_REPLY = 30;
}
