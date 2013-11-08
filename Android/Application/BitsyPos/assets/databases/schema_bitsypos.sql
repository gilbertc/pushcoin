
DROP TABLE IF EXISTS item;
CREATE TABLE item
(
	item_id TEXT NOT NULL,
	name TEXT NOT NULL,
	image TEXT NULL,
	PRIMARY KEY (item_id)
);

DROP TABLE IF EXISTS combo_item;
CREATE TABLE combo_item
(
	-- many combo items can share a single parent
	parent_item_id TEXT NOT NULL,
	-- combo slot name
	slot_name TEXT NOT NULL,
	-- default slot filler
	default_item_id TEXT NULL,
	-- alternatives for the slot
	choice_item_tag TEXT NULL,
	-- quantity of items in slot
	quantity INTEGER NOT NULL,
	-- apply price (to slot) matching given tag
	price_tag TEXT NULL,
	PRIMARY KEY (parent_item_id, slot_name)
);

DROP TABLE IF EXISTS price;
CREATE TABLE price
(
  item_id TEXT NOT NULL,
  price_tag TEXT NULL,
  value NUMERIC,
  PRIMARY KEY (item_id, price_tag)
);

DROP TABLE IF EXISTS tagged_item;
CREATE TABLE tagged_item
(
	-- tag id
  tag_id TEXT NOT NULL,
	-- tagged item
  item_id TEXT NOT NULL,
  PRIMARY KEY (tag_id, item_id)
);

DROP TABLE IF EXISTS related_item;
CREATE TABLE related_item
(
	-- base item
  item_id TEXT NOT NULL,
	-- tag of related item(s)
  tag_id TEXT NOT NULL,
  PRIMARY KEY(item_id, tag_id)
);

DROP TABLE IF EXISTS category;
CREATE TABLE category
(
	-- category id
  category_id TEXT NOT NULL,
	-- tag which marks items belonging to this category
  tag_id TEXT NOT NULL,
  PRIMARY KEY (category_id)
);

DROP TABLE IF EXISTS item_property;
CREATE TABLE item_property
(
	-- base item
  item_id TEXT NOT NULL,
	-- property name
  name TEXT NOT NULL,
	-- property value
  value TEXT NOT NULL,
  PRIMARY KEY(item_id, name)
);

