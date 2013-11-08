# -*- coding: utf-8 -*-

from PySide.QtGui import *
from PySide.QtCore import *
from gen_ui_settings import Ui_SettingsDialog

class SettingsWindow(QDialog, Ui_SettingsDialog):

	def __init__(self, settings, parent=None):
		'''Mandatory initialisation of a class.'''
		super(SettingsWindow, self).__init__(parent)
		self.settings = settings
		self.setupUi(self)

	def showEvent(self, e):
		if e.type() != QEvent.Show:
			return

		# save settings before reading
		self.settings.sync()
		# load the file...
		conf_file = open(self.settings.fileName(), 'r') 
		self.editArea.setPlainText(conf_file.read())

		
	def accept(self):
		# save the file, then resync
		conf_file = open(self.settings.fileName(), 'w') 
		conf_file.write(self.editArea.toPlainText())
		self.settings.sync()
		self.hide()	

	def reject(self):
		self.hide()	
