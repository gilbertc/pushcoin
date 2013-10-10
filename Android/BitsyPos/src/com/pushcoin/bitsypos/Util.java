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
				Item chosenItem = slot.getChosenItem();
				String slotPriceTag = slot.getPriceTag();
				if (slotPriceTag == null) {
					slotPriceTag = Conf.FIELD_PRICE_TAG_DEFAULT;
				}

				Cart.Entry entry = new Cart.Entry(
					chosenItem.getId(), 
					chosenItem.getName(), 
					slot.getQuantity(),
					chosenItem.getPrice( slotPriceTag ));

				combo.entries.add( entry );
			}
		}
		else  // not a combo
		{
			Cart.Entry entry = new Cart.Entry(
				item.getId(), item.getName(), 1, item.getPrice( Conf.FIELD_PRICE_TAG_DEFAULT ));

			combo.entries.add( entry );
		}
		return combo;
	}
}
