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
#### 6、onLongClick()
clicker+按键长按时调用。
#### 7、onWeakup()
clicker+发送唤醒指令后调用。
#### 8、onIdeaCapsule()
clicker+闪念胶囊时候调用。
#### 9、onVoicePCM(data: ByteArray, isEnd: Boolean)
语音录音的PCM数据回调，最后一帧PCM数据时，isEnd为true。
#### 10、onIdeaPCM(data: ByteArray, isEnd: Boolean)
闪念胶囊的PCM数据，一个文件的最后一帧时，isEnd为true。
#### 11、onBatteryChange(percent: Int)
电池电量发生变化（clicker+发送电池电量指令）时调用，percent为当前电量的百分比。
## Library调用方法
### 1、初始化
```java
initClicker(context: Context)
```
一般在使用该库时首先运行该方法。可以创建一个继承Application的BaseApplication，在BaseApplication中调用初始化方法，像下面一样。
```java
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
```java
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ClickerPlus.mClickerPlusListener = mListener
    }
    
    val mListener = object: ClickerPlusListener{
        override fun onPair(state: ClickerPlus.ClickerPlusState) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onCancelPair(state: ClickerPlus.ClickerPlusState) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onConnectBack(state: ClickerPlus.ClickerPlusState) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onClick() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDoubleClick() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onLongClick() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onWeakup() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIdeaCapsule() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onVoicePCM(data: ByteArray, isEnd: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIdeaPCM(data: ByteArray, isEnd: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onBatteryChange(percent: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}
```
### 2、连接配对
### 3、设备回连
### 4、取消配对
