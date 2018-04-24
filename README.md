# Clicker+ Library 接口文档
## 常数
##### ClickerPlusState.pairing、ClickerPlusState.success、ClickerPlusState.fail
## Clicker+监听
### ClickerPlusListener
#### 1、onPair(state: ClickerPlus.ClickerPlusState)
正在配对时回调，state值为pairing，此时APP可以在界面提示用户按下Clicker+设备上的按键；
配对成功时回调，state值为success；
配对超时失败时回调，state值为fail。
#### 2、onCancelPair(state: ClickerPlus.ClickerPlusState)
取消配对时调用，成功时，state值为success；失败时，state值为fail。
#### 3、onConnectBack(state: ClickerPlus.ClickerPlusState)
回连时调用，成功时，state值为success；失败时，state值为fail；
#### 4、onClick()
clicker+按键单击时调用。
#### 5、onDoubleClick()
clicker+按键双击时调用。
#### 6、onLongPress()
clicker+按键长按时调用。
#### 7、onWeakup()
clicker+发送唤醒指令后调用。
#### 8、onIdeaCapsule()
clicker+闪念胶囊时候调用。
#### 9、onVoicePCMStart()
语音录音的PCM数据开始，表示后面将会收到连续的语音PCM数据。
#### 10、onVoicePCM(data: ByteArray, index: Int)
语音录音的PCM数据，以Index作为标号区分先后。
#### 11、onVoicePCMEnd()
语音录音PCM数据传输结束。
#### 12、onIdeaPCMStart(header: String)
闪念胶囊PCM数据开始传输。
#### 13、onIdeaPCM(data: ByteArray, index: Int)
闪念胶囊的PCM数据，Index表示数据标号，区分先后。
#### 14、onIdeaPCMEnd(info: ByteArray?)
闪念胶囊PCM数据结束传输。
#### 15、onVoiceTmpPCMStart(header: String)
缓存在设备中的语音录音PCM数据开始传输。该种情况出现在传输语音录音PCM数据的时候，BLE连接中断。
#### 16、onVoiceTmpPCM(data: ByteArray, index: Int)
缓存在设备中的语音录音PCM数据
#### 17、onVoiceTmpPCMEnd(info: ByteArray?)
缓存在设备中的语音录音PCM数据结束传输。
#### 18、onBatteryChange(percent: Int)
电池电量发生变化（clicker+发送电池电量指令）时调用，percent为当前电量的百分比。
#### 19、onFindPhone()
查找手机时调用，此时手机可以做震动响铃以提示用户。

## Library调用方法
### 1、初始化
```kotlin
fun initClicker(context: Context)
```
一般在使用该库时首先运行该方法。可以创建一个继承Application的BaseApplication，在BaseApplication中调用初始化方法，像下面一样。
```kotlin
class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        ClickerPlus.initClicker(this)
    }
}
```
### 2、设置ClickerPlusListener
在使用该库时，可以设置监听对象，用于响应Clicker+设备的状态变化。
在需要的地方新建一个Listener对象，并赋予ClickerPlus。
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ClickerPlus.mClickerPlusListener = mListener
    }
    
    val mListener = object: ClickerPlusListener{
        // 语音指令缓存上传
                override fun onVoiceTmpPCMStart(header: String) {
        
                    Log.e(TAG, "onVoiceTmpPCMStart " + header)
                }
        
                override fun onVoiceTmpPCM(data: ByteArray, index: Int) {
                    Log.e(TAG, "onVoiceTmpPCM " + index)
                }
        
                override fun onVoiceTmpPCMEnd(info: ByteArray?) {
                    Log.e(TAG, "onVoiceTmpPCMEnd ")
                }
        
                override fun onFindPhone() {
                    Toast.makeText(this@LibraryDemoActivity, "查找手机，手机进行震动响铃", Toast.LENGTH_SHORT).show()
                }

                override fun onConnect(deviceMac: String) {
                    Log.e(TAG, deviceMac + " OnConnect")
                    
                }
        
                override fun onDisconnect(deviceMac: String) {
                    Log.e(TAG, deviceMac + " onDisconnect")
                    
                }
        
                override fun onPair(state: ClickerPlus.ClickerPlusState) {
                    Log.e(TAG, "onPair " + state.name)
                    
                    when(state){
                        ClickerPlus.ClickerPlusState.success ->{
                            Toast.makeText(this@LibraryDemoActivity, "配对成功", Toast.LENGTH_SHORT).show()
                        }
                        ClickerPlus.ClickerPlusState.pairing ->{
                            Toast.makeText(this@LibraryDemoActivity, "正在配对，请按一下设备上的按键。", Toast.LENGTH_SHORT).show()
                        }
                        ClickerPlus.ClickerPlusState.fail -> {
                            Toast.makeText(this@LibraryDemoActivity, "配对超时", Toast.LENGTH_SHORT).show()
                        }
                    }
        
                }
        
                override fun onCancelPair(state: ClickerPlus.ClickerPlusState) {
                    Log.e(TAG, "onCancelPair " + state.name)
                    when(state){
                        ClickerPlus.ClickerPlusState.success ->{
                            Toast.makeText(this@LibraryDemoActivity, "取消配对成功", Toast.LENGTH_SHORT).show()
                        }
                        ClickerPlus.ClickerPlusState.fail -> {
                            Toast.makeText(this@LibraryDemoActivity, "取消配对失败", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                        }
                    }
                }
        
                override fun onConnectBack(state: ClickerPlus.ClickerPlusState) {
                    Log.e(TAG, "onConnectBack " + state.name)
                    title = "已回连"
                    when(state){
                        ClickerPlus.ClickerPlusState.success ->{
                            Toast.makeText(this@LibraryDemoActivity, "回连成功", Toast.LENGTH_SHORT).show()
                        }
                        ClickerPlus.ClickerPlusState.fail -> {
                            Toast.makeText(this@LibraryDemoActivity, "回连失败", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                        }
                    }
                }
        
                override fun onClick() {
                    Log.e(TAG, "onClick")
                    Toast.makeText(this@LibraryDemoActivity, "单击", Toast.LENGTH_SHORT).show()
                }
        
                override fun onDoubleClick() {
                    Log.e(TAG, "onDoubleClick")
                    Toast.makeText(this@LibraryDemoActivity, "双击", Toast.LENGTH_SHORT).show()
                }
        
                override fun onLongPress() {
                    Log.e(TAG, "onLongPress")
                    Toast.makeText(this@LibraryDemoActivity, "长按", Toast.LENGTH_SHORT).show()
                }
        
                override fun onWeakup() {
                    Log.e(TAG, "onWeakup")
                    Toast.makeText(this@LibraryDemoActivity, "音箱唤醒", Toast.LENGTH_SHORT).show()
                }
        
                override fun onIdeaCapsule() {
                    Log.e(TAG, "onIdeaCapsule")
                    Toast.makeText(this@LibraryDemoActivity, "闪念胶囊", Toast.LENGTH_SHORT).show()
                }
                override fun onVoicePCMStart() {
                    Log.e(TAG, "onVoicePCMStart")
                    
                }
        
                override fun onVoicePCMEnd() {
                    Log.e(TAG, "onVoicePCMEnd")
                    Toast.makeText(this@LibraryDemoActivity, "PCM数据结束", Toast.LENGTH_SHORT).show()
                    
                }
                override fun onVoicePCM(data: ByteArray, index: Int) {
                    Log.e(TAG, String.format("onVoicePCM  current Index = %d, Voice PCM Data: %s", index, ByteUtils.byteToString(data)))  
                }
        
                override fun onIdeaPCMStart(header: String) {
                    Log.e(TAG, "onIdeaPCMStart " + header)
                    Toast.makeText(this@LibraryDemoActivity, "闪念胶囊PCM数据开始", Toast.LENGTH_SHORT).show()
                }
        
                override fun onIdeaPCMEnd(info: ByteArray?) {
                    Log.e(TAG, "onIdeaPCMEnd " + ByteUtils.byteToString(info))
                    Toast.makeText(this@LibraryDemoActivity, "闪念胶囊PCM数据结束", Toast.LENGTH_SHORT).show()
                }
                override fun onIdeaPCM(data: ByteArray, index: Int) {
                    Log.e(TAG, String.format("onIdeaPCM  current Index = %d, Voice PCM Data: %s", index, ByteUtils.byteToString(data)))
                }
        
                override fun onBatteryChange(percent: Int) {
                    Log.e(TAG, "onBatteryChange percent = " + percent)
                }
        
                override fun onOTAStart(deviceMac: String) {
                    Log.e(TAG, deviceMac + " onOTAStart")
                }
        
                override fun onOTAProgressChanged(deviceMac: String, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
                    Log.e(TAG, "onProgressChanged " + deviceMac + " progress:" + percent + " speed:" + speed +
                            " avgSpeed:" + avgSpeed + " currentPart:" + currentPart + " partsTotal:" + partsTotal)
                }
        
                override fun onOTACompleted(deviceMac: String) {
                    Log.e(TAG, deviceMac + " onOTACompleted")
                }
        
                override fun onOTAError(deviceMac: String, error: Int, errorType: Int, message: String?) {
                    Log.e(TAG, "onError " + deviceMac + " error:" + error + " errorType:" + errorType + " message:" + message)
                }
            }
}
```
### 3、搜索查找Clicker
使用Library提供的搜索方法搜索Clicker，该方法将过滤掉其他非clicker设备，已便让搜索列表更加简洁。
~~~kotlin
ClickerPlus.scanDevice(object : SearchResponse {
    override fun onSearchStarted() {
        // 搜索BLE设备开始
    }

    override fun onDeviceFounded(device: SearchResult) {
        // 搜索到Clicker设备，这里可以将信息展示在ListView上。
        val beacon = Beacon(device.scanRecord)
        if (!isHaveDevice(device.address)) {
            val data = HashMap<String, Any>()
            data.put("name", device.name)
            data.put("mac", device.address)
            data.put("btdevice", device.device)
            deviceData.add(data)
            deviceAdapter.notifyDataSetChanged()
            BluetoothLog.e(String.format("beacon for %s\n%s", device.address, beacon.toString()))
        }
    }

    override fun onSearchStopped() {
        refreshLayout.isRefreshing = false
    }

    override fun onSearchCanceled() {
        refreshLayout.isRefreshing = false
    }
})
~~~

### 2、连接配对
配对连接使用ClickerPlus.pairDevice(deviceMac: String, flagID: String)方法，其中传入需要连接配对设备的MAC地址以及用户唯一的字符串（不超过7个字符）。
~~~kotlin
fun pairClick(v: View){
    val result = ClickerPlus.pairDevice(mDeviceMAC!!, "123456")
    if (!result){
        Toast.makeText(this, "当前已经连接设备，无需配对", Toast.LENGTH_SHORT).show()
    }
}
~~~
### 3、设备回连
Library能够根据历史连接信息在启动时可自动进行连接。在没有设备连接的时候，还会不断搜索BLE设备，以便自动回连。
若需要手动连接设备，可使用上面**"连接配对"**的方法进行连接。
### 4、取消配对
取消配对的设备。
~~~kotlin
/**
 * @param isAll 是否解除设备的所有绑定
 */
fun unPairDevice(isAll: Boolean): Boolean
~~~
### 5、查找设备
~~~kotlin
fun findDevice(): Boolean
~~~
### 6、获取电池电量
~~~kotlin
fun getBattery(): Boolean
~~~
### 7、MIC增益
~~~kotlin
/**
 * @param value 增益的值
 * @return 没有连接或配对返回false。
 */
fun micIncrease(value: Int): Boolean
~~~
### 8、获取版本号
~~~kotlin
fun getVersion(): Boolean
~~~
### 9、获取信号强度RSSI
~~~kotlin
ClickerPlus.readRss(object : BleReadRssiResponse {
    override fun onResponse(code: Int, data: Int?) {
        if (data != null) {
            val h = Handler()
            h.post {

                Toast.makeText(this@LibraryDemoActivity, "RSSI = " + data, Toast.LENGTH_SHORT).show()
            }


        }
    }

})
~~~
### 10、OTA升级
~~~kotlin
fun otaUpdate(context: Context, filePath: String): Boolean
~~~