package org.covidwatch.android

import org.covidwatch.android.ble.BluetoothManager
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.domain.TestedRepository
import org.tcncoalition.tcnclient.TcnKeys
import org.tcncoalition.tcnclient.bluetooth.TcnBluetoothServiceCallback
import org.tcncoalition.tcnclient.toUUID

//TODO: Use constants for preference keys
class TcnManager(
    private val tcnKeys: TcnKeys,
    private val bluetoothManager: BluetoothManager,
    private val contactEventDAO: ContactEventDAO,
    private val testedRepository: TestedRepository
) {

    fun start() {
        bluetoothManager.setCallback(object : TcnBluetoothServiceCallback {
            override fun generateTcn() = tcnKeys.generateTcn()

            override fun onTcnFound(tcn: ByteArray, estimatedDistance: Double?) = logTcn(tcn)
        })
        bluetoothManager.startService()
    }

    fun stop() {
        bluetoothManager.stopService()
    }

    private fun logTcn(tcn: ByteArray) {
        CovidWatchDatabase.databaseWriteExecutor.execute {
            val contactEvent = ContactEvent(tcn.toUUID().toString())
            contactEvent.wasPotentiallyInfectious = testedRepository.isUserTestedPositive()
            contactEventDAO.insert(contactEvent)
        }
    }
}