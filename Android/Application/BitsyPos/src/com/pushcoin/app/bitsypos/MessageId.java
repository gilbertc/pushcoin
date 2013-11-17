package com.pushcoin.app.bitsypos;

public class MessageId 
{
	final static int CATEGORY_CLICKED = 1;
	final static int ITEM_CLICKED = 2;
	final static int CUSTOMER_DETAILS_AVAILABLE= 3;
	final static int CHECKOUT_CLICKED = 4;
	// Events pertaining to the items within a single cart
	final static int CART_CONTENT_CHANGED = 10;
	// Events pertaining to the collection of carts
	final static int CART_POOL_CHANGED = 20;
}
