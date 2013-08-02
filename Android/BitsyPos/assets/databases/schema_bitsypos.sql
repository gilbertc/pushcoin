
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
	-- ID within this combo
	combo_item_id TEXT NOT NULL,
	-- combo slot name
	slot_name TEXT NULL,
	-- default slot filler
	default_item_id TEXT NULL,
	-- alternatives for the slot
	choice_item_tag TEXT NULL,
	-- quantity of items in slot
	quantity INTEGER,
	-- apply price (to slot) matching given tag
	price_tag TEXT NULL,
	PRIMARY KEY(parent_item_id, combo_item_id)
);

DROP TABLE IF EXISTS price;
CREATE TABLE price
(
  item_id TEXT NOT NULL,
  price_tag TEXT NULL,
  value INTEGER,
  scale INTEGER,
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
	-- tag of related item
  item_tag TEXT NULL,
	-- base item
  item_id TEXT NOT NULL,
  PRIMARY KEY(item_id, item_tag)
);

DROP TABLE IF EXISTS category
CREATE TABLE category
(
	-- category id
  category_id TEXT NOT NULL,
	-- tag which marks items belonging to this category
  tag_id TEXT NOT NULL,
  PRIMARY KEY (category_id)
);

/*
	Sample of items to play with, inspired by Gabby's Kitchen menu.
*/

insert into item (item_id, name, image) values
('PNCKE_PLAIN', 'Plain Pancake', NULL),
('PNCKE_STRBRY', 'Strawberry Pancake', NULL),
('PNCKE_BLUBRY', 'Blueberry Pancake', NULL),
('FRENCH_TOAST', 'French Toast', NULL),
('TOAST_JELLY', 'Toast & Jelly', NULL),
('TURKY_SAUSGE', 'Turkey Sausage', NULL),
('CHOCKLETE_CHIP', 'Chocklete Chips', NULL),
('WHIPCREAM_HV', 'Whip Cream (Heavy)', NULL),
('SODA_CAN_335', 'Soda Can (335 ml)', NULL),
('ORANGE_JUICE_335', 'Orange Juice (335 ml)', NULL),
('PNCKE_STRBRY_ML2', 'Two Strawberry Pancakes w/ Drink', NULL),
('PNCKE_BLUBRY_ML2', 'Two Blueberry Pancakes w/ Drink', NULL),
('THREE_ITEM_BREAKFAST_MEAL', 'Breakfast Special', NULL),
('BREAKFAST_SPECIAL_ITEM_TWO_SAUSAGES', '2-Sausage Links', NULL),
('BREAKFAST_SPECIAL_ITEM_TWO_PANCAKES', '2-Pancakes', NULL)
;

insert into combo_item (parent_item_id, combo_item_id, slot_name, default_item_id, choice_item_tag, quantity, price_tag) values
('THREE_ITEM_BREAKFAST_MEAL', '_1', 'Choice-1', NULL, 'breakfast_special_item', 1, 'combo_priced_in'),
('THREE_ITEM_BREAKFAST_MEAL', '_2', 'Choice-2', NULL, 'breakfast_special_item', 1, 'combo_priced_in'),
('THREE_ITEM_BREAKFAST_MEAL', '_3', 'Choice-3', NULL, 'breakfast_special_item', 1, 'combo_priced_in'),
('BREAKFAST_SPECIAL_ITEM_TWO_SAUSAGES', '_1', NULL, 'TURKY_SAUSGE', NULL, 2, NULL),
('BREAKFAST_SPECIAL_ITEM_TWO_PANCAKES', '_1', NULL, 'PNCKE_PLAIN', NULL, 2, NULL)
;

insert into price (item_id, price_tag, value, scale) values 
('THREE_ITEM_BREAKFAST_MEAL', 'unit', 500, 2)
;

insert into tagged_item ( tag_id, item_id ) values 
('breakfast', 'THREE_ITEM_BREAKFAST_MEAL'),
('breakfast_special_item', 'BREAKFAST_SPECIAL_ITEM_TWO_PANCAKES'),
('breakfast_special_item', 'BREAKFAST_SPECIAL_ITEM_TWO_SAUSAGES'),
('breakfast_special_item', 'TOAST_JELLY'),
('breakfast_addon', 'FRENCH_TOAST'),
('breakfast_addon', 'TOAST_JELLY'),
('breakfast_addon', 'TURKY_SAUSGE'),
('breakfast_addon', 'CHOCKLETE_CHIP'),
('breakfast_addon', 'WHIPCREAM_HV'),
('breakfast_addon', 'ORANGE_JUICE_335')
;

insert into related_item ( item_tag, item_id ) values 
('breakfast_addon', 'THREE_ITEM_BREAKFAST_MEAL')
;

/*
insert into customization (item_id, tagmask, scope, level) values
('PNCKE_STRBRY_ML2', 4, 'addon', 1),
('PNCKE_BLUBRY_ML2', 4, 'addon', 1);


insert into price (item_id, scope, level, value, scale) values
('PNCKE_STRBRY', 'unit', 1, 210, 2),
('PNCKE_BLUBRY', 'unit', 1, 215, 2),
('CHOCKLETE_CHIP', 'addon', 1, 60, 2),
('WHIPCREAM_HV', 'addon', 1, 40, 2),
('TURKY_SAUSGE', 'addon', 1, 100, 2),
('TOAST', 'addon', 1, 75, 2),
('SODA_CAN_335', 'unit', 1, 100, 2),
('SODA_CAN_335', 'addon', 1, 80, 2),
('ORANGE_JUICE_335', 'unit', 1, 195, 2),
('ORANGE_JUICE_335', 'addon', 1, 100, 2),
('TOAST', 'addon', 1, 75, 2),

App interaction:

1. User clicks on cateogry "breakfast", we list items:

select * from tagged_item where tag_id = 'breakfast';

breakfast|THREE_ITEM_BREAKFAST_MEAL

2. User clicks on THREE_ITEM_BREAKFAST_MEAL:

--
	Is it a combo? yes:

select * from combo_item where parent_item_id = 'THREE_ITEM_BREAKFAST_MEAL';

THREE_ITEM_BREAKFAST_MEAL|_1|Choice-1||breakfast_special_item|1|combo_priced_in
THREE_ITEM_BREAKFAST_MEAL|_2|Choice-2||breakfast_special_item|1|combo_priced_in
THREE_ITEM_BREAKFAST_MEAL|_3|Choice-3||breakfast_special_item|1|combo_priced_in

--
	User is expected to configure three slots before adding to cart. This is
	becuase we don't have actual items specified in anywhere. We use tag to
	figure out candidates:

select * from tagged_item where tag_id = 'breakfast_special_item';

breakfast_special_item|BREAKFAST_SPECIAL_ITEM_TWO_PANCAKES
breakfast_special_item|BREAKFAST_SPECIAL_ITEM_TWO_SAUSAGES
breakfast_special_item|TOAST_JELLY
	
--
	Show tags of related items (for checkbox-filter):

select distinct(tag_id) from tagged_item where item_id in
(select item_id from tagged_item where tag_id = 'breakfast_special_item')

breakfast_special_item

	Ok, we found 3 candidates, show each one:
	
*/
