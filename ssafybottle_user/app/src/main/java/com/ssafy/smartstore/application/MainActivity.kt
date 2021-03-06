package com.ssafy.smartstore.application

import android.Manifest
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.ssafy.smartstore.R
import com.ssafy.smartstore.application.SmartStoreApplication.Companion.isCoupon
import com.ssafy.smartstore.application.SmartStoreApplication.Companion.tableName
import com.ssafy.smartstore.databinding.ActivityMainBinding
import com.ssafy.smartstore.ui.dialog.BeaconNotificationDialog
import com.ssafy.smartstore.ui.login.LoginMainFragment
import com.ssafy.smartstore.ui.root.RootFragment
import com.ssafy.smartstore.utils.BOOTPAY_APPLICATION_ID
import com.ssafy.smartstore.utils.STORE_DISTANCE
import com.ssafy.smartstore.utils.requestPermission
import kr.co.bootpay.BootpayAnalytics
import org.altbeacon.beacon.*

class MainActivity : AppCompatActivity(), BeaconConsumer{

    private lateinit var binding : ActivityMainBinding

    private val mainViewModel : MainViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pIntent: PendingIntent
    private lateinit var filters: Array<IntentFilter>

    private var backPressedTime = 0L

    // Beacon
    private lateinit var beaconManager: BeaconManager
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var needBLERequest = true
    private val region = Region("altbeacon", null, null, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBootPay()
        initNfcAdapter()
        initIntent()
        requestLocationPermission()
        initBeaconManager()
        initBluetoothManager()
        startScan()
    }

    private fun initBootPay() {
        BootpayAnalytics.init(this, BOOTPAY_APPLICATION_ID)
    }

    private fun initNfcAdapter() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            //finish()
            Toast.makeText(this, "????????? nfc ????????? ?????? ????????????.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initIntent() {
        val intent = Intent(this, MainActivity::class.java)
        // SingleTop ??????
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP


        // PendingIntent ??????
        pIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // ?????? ??????
        val tagFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        filters = arrayOf(tagFilter)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent!!.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED
        ) {

            Log.d("MainActivity_??????", "onNewIntent()")
            getNfcMessage(intent)
        }
    }

    private fun getNfcMessage(intent: Intent?) {
        val msgs = getNdefMessages(intent!!)
        if(msgs != null) {
            val storeId = getPayload(msgs!![0]!!.records[0])
            Log.d("MainActivity_??????", storeId) //t,1

            val messages = storeId.split(',')
            when(messages[0]) {
                "t" ->{
                    Toast.makeText(this, "${messages[1]}??? ????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                    tableName = messages[1]
                }
                "c" -> {
                    if(isCoupon) {
                        mainViewModel.coupon.value = messages[1].toInt()
                    }
                }
            }
        }
    }

    private fun getNdefMessages(intent: Intent): Array<NdefMessage?>? {
        var msgs: Array<NdefMessage?>? = null

        val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMsgs != null) {
            msgs = arrayOfNulls(rawMsgs.size)
            for (i in rawMsgs.indices) {
                msgs[i] = rawMsgs[i] as NdefMessage?
            }
        }
        return msgs
    }

    private fun getPayload(recInfo: NdefRecord): String =
        String(recInfo.payload, 3, recInfo.payload.size - 3)

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pIntent, filters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onBackPressed() {
        val curFragment = supportFragmentManager.findFragmentById(R.id.nav_host)!!.childFragmentManager.fragments[0]
        if(curFragment is RootFragment || curFragment is LoginMainFragment) {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - backPressedTime
            backPressedTime = currentTime

            if (elapsedTime in 0..2000) {
                finish()
            } else {
                Toast.makeText(this, "???????????? ????????? ??? ??? ??? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onBackPressed()
        }
    }

    // Beacon
    override fun onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(monitorNotifier)
        beaconManager.addRangeNotifier(rangeNotifier)
        try {
            beaconManager.startMonitoringBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun requestLocationPermission() {
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, object : PermissionListener {
            override fun onPermissionGranted() {}
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(this@MainActivity, "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
    }

    private fun initBluetoothManager() {
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    private fun isEnableBLEService(): Boolean = bluetoothAdapter!!.isEnabled

    private fun requestEnableBLE() {
        requestBLEActivity.launch(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        )
    }

    private val requestBLEActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // ???????????? ???????????? ????????? ???????????? ??????
        if (isEnableBLEService()) {
            needBLERequest = false
            startScan()
        }
    }

    private fun startScan() {
        // ???????????? Enable ??????
        if (!isEnableBLEService()) {
            requestEnableBLE()
            Toast.makeText(this, "??????????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
        }

        // ?????? ?????? ?????? ?????? ??? GPS Enable ?????? ??????
        requestLocationPermission()

        Log.d("RootFragment_??????", "startScan: beacon Scan start")
        // Beacon Service bind
        beaconManager.bind(this)
    }

    private fun stopBeaconScan() {
        beaconManager.stopMonitoringBeaconsInRegion(region)
        beaconManager.stopRangingBeaconsInRegion(region)
        beaconManager.unbind(this)
    }

    private val monitorNotifier = object : MonitorNotifier {
        override fun didEnterRegion(region: Region?) {
            try {
                Log.d("RootFragment_??????", "????????? ?????????????????????.------------${region.toString()}")
                beaconManager.startRangingBeaconsInRegion(region!!)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun didExitRegion(region: Region?) {
            try {
                Log.d("RootFragment_??????", "????????? ?????? ??? ????????????.")
                beaconManager.stopRangingBeaconsInRegion(region!!)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun didDetermineStateForRegion(state: Int, region: Region?) {}
    }

    private val rangeNotifier = RangeNotifier { beacons, _ ->
        for (beacon in beacons) {
            // Major, Minor ??? Beacon ??????, 1?????? ????????? ???????????? ????????? ??????
            if (isYourBeacon(beacon)) {
                // ????????? ????????? ?????? ??????
                Log.d(
                    "RootFragment_??????",
                    "distance: " + beacon.distance + " Major : " + beacon.id2 + ", Minor" + beacon.id3
                )

                // ??????????????? ?????????
                //BeaconNotificationDialog().show(childFragmentManager, "BeaconNotificationDialog")
                BeaconNotificationDialog().show(supportFragmentManager, "BeaconNotificationDialog")

                // ?????? ?????? ??????
                stopBeaconScan()
            }
        }
    }

    // ????????? ?????? Beacon ??? ?????????, ????????? ?????? ???????????? ??????
    private fun isYourBeacon(beacon: Beacon): Boolean {
        return (beacon.distance <= STORE_DISTANCE)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBeaconScan()
    }
}