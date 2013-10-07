package com.pushcoin.bitsypos;

import java.math.BigDecimal;

class Util
{
	static public Cart.Combo toCartCombo(Item item)
	{
		Cart.Combo combo = new Cart.Combo();
		combo.basePrice = new BigDecimal(0); 

		if ( item.isCombo() ) 
		{
			// Combo usually has a bundle name
			combo.name = item.getName();

			// Combo may have base price in addition to its slot item prices
			BigDecimal basePrice = item.basePrice( Conf.FIELD_PRICE_TAG_DEFAULT );
			if ( basePrice != null ) {
				combo.basePrice = basePrice;
			}

			for (Slot slot: item.getSlots()) 
			{
				Cart.Entry entry = new Cart.Entry();
				Item chosenItem = slot.getChosenItem();
				entry.sku = chosenItem.getId();
				entry.name = chosenItem.getName();
				entry.qty = slot.getQuantity();

				String slotPriceTag = slot.getPriceTag();
				if (slotPriceTag == null) {
					slotPriceTag = Conf.FIELD_PRICE_TAG_DEFAULT;
				}
				entry.unitPrice = chosenItem.getPrice( slotPriceTag );

				combo.entries.add( entry );
			}
		}
		else  // not a combo
		{
			Cart.Entry entry = new Cart.Entry();
			entry.sku = item.getId();
			entry.name = item.getName();
			entry.qty = 1;
			entry.unitPrice = item.getPrice( Conf.FIELD_PRICE_TAG_DEFAULT );
			combo.entries.add( entry );
		}
		return combo;
	}
}
