Third-party dependencies
========================

BitsyPOS uses the following third-party libraries: 

	* TypefaceTextView
		https://github.com/ragunathjawahar/android-typeface-textview

	* Android SQLiteAssetHelper:
		https://github.com/jgilfelt/android-sqlite-asset-helper

	* SwipeToDismissUndoList 
		https://github.com/timroes/SwipeToDismissUndoList

Build Instructions
==================

Third-party dependenies live in Bitsy project in the form of Git Submodules.
When you clone our superproject, you are not automatically downloading its
third-party dependencies. You must visit each submodule directory and call 
`git submodule init` followed by `git submodule update`. 

Example:

$ cd Library/TypefaceTextView
$ git submodule init
$ git submodule update

Repeat above steps for every dependent library.

When done, you should be ready to compile everything with a simple:

$ cd Application/Bitsy
$ ant build


Maintainer Information 
======================

To Test JSON import

lua < conf/breakfast_menu.txt | java -classpath /home/sl/src/JSON-java:bin/classes com.pushcoin.app.bitsypos.JsonImporter

