# -*- coding: utf-8 -*-

import sys, time, datetime
from decimal import Decimal
from PySide.QtGui import *
from PySide.QtCore import *
from gen_ui_main import Ui_MainWindow
from ui_settings_window import SettingsWindow
import settings as const

# Templates for rendering PTAs
PTA_WITH_TIP_TMPL = '''<center><h2><hr />Payment Authorization<hr /></h2></center>
<table align="center">
<tr><td align="right">Limit: </td><td align="right"><font size="12" color="white"><strong>{limit}</strong></font></td></tr>
<tr><td align="right">Tip: </td><td align="right"><font size="12" color="#9090ff"><strong>{tip}</strong></font></td></tr>
</table>
'''

PTA_NO_TIP_TMPL = '''<center><h2><hr />Payment Authorization<hr /></h2></center>
<table align="center">
<tr><td align="right">Limit: </td><td align="right"><font size="12" color="white"><strong>{limit}</strong></font></td></tr>
</table>
'''

MSG_CERTIFICATE_EXPIRED = '<font color="#ff4433">Certificate expired</font>'
MSG_CERTIFICATE_ALMOST_EXPIRED = '<font color="#ffaa33">Certificate expires...</font>'
MSG_TOTAL_HIGHER_THAN_LIMIT = 'Total exceeds limit'

def format_tip(tip_val, tip_type):
	'''Returns tip value either as absolute dollar value or percentage'''
	if tip_type == 'A':
		return '$' + str(tip_val.quantize(Decimal("0.01")))
	else:
		tip_scaled = tip_val.shift(2)
		return str(tip_scaled.quantize(Decimal("1"))) + '%'
	

def until_expired(expiry_tm):
	'''Returns number of seconds until expiration'''
	now = long( time.time() + 0.5 )
	seconds = expiry_tm - now
	return max(0, seconds)


class Form():
	'''Holds submitted form attributes'''
	pass;

class MainWindow(QMainWindow, Ui_MainWindow):

	def __init__(self, settings, parent=None):
		'''Mandatory initialisation of a class.'''
		super(MainWindow, self).__init__(parent)
		self.settings = settings
		self.settingsDialog = SettingsWindow(settings, self)
		self.setupUi(self)
		self.animationArea.hide()
		self.view_display.setReadOnly(True)

		# prepare busy-animation
		self.busyAnim = QMovie("img/3d_arrows.gif")
		self.animationArea.setMovie(self.busyAnim)

		# status bar
		self.scannerStatus = QLabel()
		self.statusbar.addPermanentWidget(self.scannerStatus)
		self.pta = None

		# when tip checkbox changes state, recalc
		self.chbox_tip.stateChanged.connect(self.__recalculate)

		# when Cost edited, recalc total
		self.input_cost.textChanged.connect(self.__recalculate)

		# prepare certificate expiration watchdog
		self.timer = QTimer(self)
		self.connect(self.timer, SIGNAL("timeout()"), self.__update_expiry)
		self.timer.start(1000)

		# when user wants to change configuration, open default text editor
		self.btn_settings.clicked.connect(self.settingsDialog.show)

		# reset all UI states
		self.__reset();


	def __init_expiry(self):
		if self.pta:
			seconds = until_expired(self.pta.expiry)
			if seconds > 0:
				self.pta.expired = False
				self.countDown.display( seconds )
				self.certificateStatus.clear()
				return
			else:
				self.certificateStatus.setText(MSG_CERTIFICATE_EXPIRED)
				self.pta.expired = True
		else:
			self.certificateStatus.clear()

		self.countDown.display("")


	def __update_expiry(self):
		'''If we have a PTA, it shows how much time until it expires'''
		if self.pta and not self.pta.expired:
			seconds = until_expired(self.pta.expiry)
			self.pta.expired = bool(seconds == 0)
			self.countDown.display( seconds )
			if self.pta.expired:
				self.certificateStatus.setText(MSG_CERTIFICATE_EXPIRED)
			elif seconds < 60:
				# blink warning...
				if seconds % 2 == 0:
					self.certificateStatus.setText(MSG_CERTIFICATE_ALMOST_EXPIRED)
				else:
					self.certificateStatus.setText("")
				
		else:
			self.countDown.display( "" )


	def __recalculate(self):
		'''Takes Cost and Tip and computes the Total'''
		total = 0
		if self.pta:
			# take charge, extract numbers only
			if self.input_cost.text():
				cost = Decimal(self.input_cost.text())
				total = cost
				if self.pta.has_tip:
					self.chbox_tip.setDisabled(False)
					if self.chbox_tip.isChecked():
						if self.pta.tip_type == 'A':
							total += self.pta.tip 
						else:
							total *= 1 + self.pta.tip
				else:
					self.chbox_tip.setDisabled(True)
				self.view_total.setText( str(total.quantize(Decimal("0.01")) ))
			else: # unaccaptable input
				self.view_total.setText( "N/A" )

			if self.pta.has_tip:
				self.view_tip.setText(format_tip(self.pta.tip, self.pta.tip_type))
			else:
				self.view_tip.setText('N/A')
				self.chbox_tip.setChecked(False)

		# decide if to enable Accept button
		if self.pta and total > 0:
			# let merchant know total exceeds payment-limit
			if self.pta.payment_limit >= total:
				self.btn_accept.setEnabled( True )
				self.statusbar.clear()
				return
			else:
				self.statusbar.showMessage(MSG_TOTAL_HIGHER_THAN_LIMIT)

		# by default, Accept is off
		self.btn_accept.setEnabled( False )


	def __reset(self):
		self.pta = None
		self.input_cost.setText("0.00")
		self.view_display.setText( 'Waiting...' )
		self.view_total.setText( "N/A" )
		self.view_tip.setText('N/A')
		self.btn_accept.setEnabled(False)
		self.__init_expiry()
		self.input_cost.setFocus()


	def show_busy(self, is_busy):
		'''Turns animation on/off depending on busy status'''
		
		# only start anim if not already playing
		if is_busy and self.animationArea.isHidden():
			self.busyAnim.start()
			self.view_display.hide()
			self.animationArea.show()

		# only stop anim if actually playing
		elif self.view_display.isHidden():
			# delay the shutoff so the animation won't just flicker
			QTimer.singleShot(250, self, SLOT('animation_off()'))


	@Slot()
	def animation_off(self):
		self.animationArea.hide()
		self.view_display.show()
		self.busyAnim.stop()
		

	def process_data(self, data):
		'''Displays data arriving from the controller'''

		# ignore all but PTA and Error events
		if data.msgid == 'Er':
			self.__reset()
			self.view_display.setText( data.what )
			self.certificateStatus.clear()
			return
		elif data.msgid == 'Ok':
			self.__reset()
			self.view_display.setHtml( data.what )
			self.certificateStatus.setText('Success')
			return
		elif data.msgid != 'PTA':
			self.__reset()
			return


		# make PTA data pretty
		ctime_prt = time.strftime("%x %X", time.localtime(data.ctime))
		payment_limit_prt = '$' + str(data.payment_limit.quantize(Decimal('0.01')))

		# store as PTA
		self.pta = data

		tip_prt = ''
		if data.has_tip:
			tip_prt = format_tip(data.tip, data.tip_type)
			html = PTA_WITH_TIP_TMPL.format(
				ctime = ctime_prt, 
				limit = payment_limit_prt, 
				tip = tip_prt)
		else:
			html = PTA_NO_TIP_TMPL.format(
				ctime = ctime_prt, 
				limit = payment_limit_prt)

		self.view_display.setHtml( html )
		
		# set tip to ON by default on each scan
		self.chbox_tip.setChecked(True)

		# reset progress bar
		self.__init_expiry();

		# recalculate total
		self.__recalculate()
		

	def form_fields( ui ):
		'''Takes UI element and builds a simple form'''
		form = Form()
		form.charge = Decimal(ui.view_total.text())
		if ui.chbox_tip.isChecked():
			form.tip = Decimal(ui.view_total.text()) - Decimal(ui.input_cost.text()) 
		else:
			form.tip = None
		form.order_id = ui.input_order_id.text().encode("utf8","ignore")
		form.note = ui.input_note.text().encode("utf8","ignore")
		return form

