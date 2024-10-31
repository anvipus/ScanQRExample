package com.anvipus.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Base64
import android.util.Base64.encodeToString
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import com.google.firebase.ktx.Firebase
import com.anvipus.core.BuildConfig
import com.anvipus.core.models.months
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.*
import java.lang.reflect.Array
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.net.URL
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


object Helpers {

    private const val NON_THIN = "[^iIl1\\.,']"

    fun getHmacMd5(value: String): String? {
        try {
            val result = hmacDigest(
                encodeToString(value.toByteArray(), Base64.DEFAULT).trim(),
                BuildConfig.KUNCI_GARAM,
                "HmacMD5"
            )
            Timber.d("hmac result: ${result}")
            return result
        }catch (e: Exception){
            e.printStackTrace()
        }
        return ""
    }


    fun getMd5Wms(value: String, msisdn: String): String? {
        try {
            val msisdn_suffix = msisdn.substring(msisdn.length - 6)


            val result = md5(value+msisdn_suffix)
            Timber.d("md5 wms result: ${result}")
            return result
        }catch (e: Exception){
            e.printStackTrace()
        }
        return ""
    }

    fun getSHA256(value: String): String {
        Timber.e("getSHA256: $value")
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val msgDigest = digest.digest(value.toByteArray(charset("US-ASCII")))
            val hexString = StringBuilder()
            msgDigest.forEach {
                var h = Integer.toHexString((0xFF and it.toInt()))
                while (h.length < 2)
                    h = "0$h"
                hexString.append(h)
            }
            Timber.e("sha256 result: $hexString.toString()")
            return hexString.toString()
        } catch (ignore: NoSuchAlgorithmException) {
        } catch (ignore: UnsupportedEncodingException) {
        }

        return ""
    }

    fun md5(s: String): String? {
        val MD5 = "MD5"
        try {
            // Create MD5 Hash
            val digest: MessageDigest = MessageDigest
                .getInstance(MD5)
            digest.update(s.toByteArray())
            val messageDigest: ByteArray = digest.digest()

            // Create Hex String
            val hexString = java.lang.StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    fun hmacDigest(msg: String, keyString: String, algo: String?): String? {
        var digest: String? = null
        try {
            val key = SecretKeySpec(keyString.toByteArray(charset("UTF-8")), algo)
            val mac = Mac.getInstance(algo)
            mac.init(key)
            val bytes = mac.doFinal(msg.toByteArray(charset("ASCII")))
            val hash = StringBuffer()
            for (i in bytes.indices) {
                val hex = Integer.toHexString(0xFF and bytes[i].toInt())
                if (hex.length == 1) {
                    hash.append('0')
                }
                hash.append(hex)
            }
            digest = hash.toString()
        } catch (e: UnsupportedEncodingException) {
        } catch (e: InvalidKeyException) {
        } catch (e: NoSuchAlgorithmException) {
        }
        return digest
    }

    fun verifyInstallerId(context: Context): Boolean {
        // A list with valid installers package name
        val validInstallers: List<String> =
            ArrayList(Arrays.asList("com.android.vending", "com.google.android.feedback","dev.firebase.appdistribution"))

        // The package name of the app that has installed your app
        val installer = context.packageManager.getInstallerPackageName(context.packageName)
        Timber.d("verifyInstaller")
        Timber.d("value: ${installer}")
        Timber.d("value: ${installer != null && validInstallers.contains(installer)}")

        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer)
    }

    fun isFirstInstall(context: Context): Boolean {
        return try {
            val firstInstallTime =
                context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            val lastUpdateTime =
                context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            firstInstallTime == lastUpdateTime
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            true
        }
    }

    fun isInstallFromUpdate(context: Context): Boolean {
        return try {
            val firstInstallTime =
                context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            val lastUpdateTime =
                context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            firstInstallTime != lastUpdateTime
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun getJson(bundle: Bundle?): String? {
        if (bundle == null) return null
        val jsonObject = JSONObject()
        for (key in bundle.keySet()) {
            val obj = bundle[key]
            try {
                jsonObject.put(key, wrap(bundle[key]))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return jsonObject.toString()
    }

    fun wrap(o: Any?): Any? {
        if (o == null) {
            return JSONObject.NULL
        }
        if (o is JSONArray || o is JSONObject) {
            return o
        }
        if (o == JSONObject.NULL) {
            return o
        }
        try {
            if (o is Collection<*>) {
                return JSONArray(o as Collection<*>?)
            } else if (o.javaClass.isArray) {
                return toJSONArray(o)
            }
            if (o is Map<*, *>) {
                return JSONObject(o as Map<*, *>?)
            }
            if (o is Boolean ||
                o is Byte ||
                o is Char ||
                o is Double ||
                o is Float ||
                o is Int ||
                o is Long ||
                o is Short ||
                o is String
            ) {
                return o
            }
            if (o.javaClass.getPackage().name.startsWith("java.")) {
                return o.toString()
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    @Throws(JSONException::class)
    fun toJSONArray(array: Any): JSONArray {
        val result = JSONArray()
        if (!array.javaClass.isArray) {
            throw JSONException("Not a primitive array: " + array.javaClass)
        }
        val length = Array.getLength(array)
        for (i in 0 until length) {
            result.put(wrap(Array.get(array, i)))
        }
        return result
    }

    fun getIPAddressLocal(useIPv4: Boolean): String? {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> =
                    Collections.list(intf.getInetAddresses())
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress()) {
                        val sAddr: String = addr.getHostAddress()
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(
                                    0,
                                    delim
                                ).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
        } // for now eat exceptions
        return ""
    }

    suspend fun getPublicIpAddress(): String?{
        return try {
            withContext(Dispatchers.IO) {
                getExternalIpAddress()
            }
        } catch (e: SocketTimeoutException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun getExternalIpAddress(): String? {
        val whatismyip = URL("https://checkip.amazonaws.com")
        var `in`: BufferedReader? = null
        return try {
            `in` = BufferedReader(
                InputStreamReader(
                    whatismyip.openStream()
                )
            )
            `in`.readLine()
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getUserAgent(mContext: Context): String?{
        return WebView(mContext).getSettings().getUserAgentString()
    }

    private fun getNavigationBarHeight(windowManager: WindowManager): Int {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val metrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(metrics)
                val usableHeight = metrics.heightPixels
                windowManager.defaultDisplay.getRealMetrics(metrics)
                val realHeight = metrics.heightPixels
                return if (realHeight > usableHeight) realHeight - usableHeight else 0
            }
            return 0
        }catch (e: Exception){
            return 0
        }

    }

    fun getScreenHeight(windowManager: WindowManager): Int{
        try {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels + getNavigationBarHeight(windowManager)
            return height
        }catch (e: Exception){
            return 0
        }

    }

    fun getScreenWeight(windowManager: WindowManager): Int{
        try {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            return width
        }catch (e: Exception){
            return  0
        }

    }

    fun formatSize(size: Long): String? {
        var size = size
        var suffix: String? = null
        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024
            }
        }
        val resultBuffer =
            StringBuilder(java.lang.Long.toString(size))
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }

    fun getTotalInternalMemorySize(): String? {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return formatSize(totalBlocks * blockSize)
    }

    fun getAvailableInternalMemorySize(): String? {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return formatSize(availableBlocks * blockSize)
    }

    fun getRamSize(mContext: Context): String? {
        val actManager =
            mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalMemory: Long = memInfo.totalMem/ (1024 * 1024)

        return totalMemory.toString()+" MB"
    }

    fun getAvailableRam(mContext: Context): String? {
        val actManager =
            mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val vailMem: Long = memInfo.availMem/ (1024 * 1024)

        return vailMem.toString()+" MB"
    }

    @Throws(IOException::class)
    fun getCPUName(): String? {
        var cpuName: String? = null
        val br = BufferedReader(FileReader("/proc/cpuinfo"))
        var str: String? = null
        while (br.readLine().also { str = it } != null) {
            val data = str!!.split(":".toRegex()).toTypedArray()
            if (data.size > 1) {
                var key = data[0].trim { it <= ' ' }.replace(" ", "_")
                if (key == "model_name") key = "cpu_model"
                cpuName = data[1].trim { it <= ' ' }
            }
        }
        br.close()
        return cpuName
    }

    fun getNext12Month(): MutableList<months>{
        val monthList: MutableList<months> = ArrayList()
        var spf = SimpleDateFormat("MMMM yyyy", Locale("in", "ID"))
        var spf2 = SimpleDateFormat("MM-yyyy", Locale("in", "ID"))
        for(x in 1..12){
            val months = months()
            months.id = x
            var date = getNextMonthDays(x - 1)
            months.date = date
            months.monthYear = spf.format(date)
            months.monthNumber = spf2.format(date)
            monthList.add(months)
        }
        return monthList
    }

    fun getNextXMonth(x: Int): String{
        val monthList: MutableList<months> = ArrayList()
        var spf = SimpleDateFormat("MMMM yyyy", Locale("in", "ID"))
        var spf2 = SimpleDateFormat("MM-yyyy", Locale("in", "ID"))
        val calendar: Calendar = Calendar.getInstance()

        var displayedDate = Date()
        calendar.setTime(displayedDate)
        calendar.add(Calendar.MONTH, x+1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.DATE, -1)

        val lastDayOfMonth = calendar.time

        val sdf= SimpleDateFormat("dd-MM-yyyy")
//        System.out.println("Today            : " + sdf.format(displayedDate))
//        System.out.println("Last Day of Month: " + sdf.format(lastDayOfMonth))

        return sdf.format(lastDayOfMonth)
    }

    fun getNextMonthDays(param: Int): Date? {
        val cl: Calendar = Calendar.getInstance()
        var displayedDate = Date()
        cl.setTime(displayedDate)
        cl.add(Calendar.MONTH, param)
        displayedDate = cl.getTime()
        return displayedDate
    }

    fun hideSoftKeyboard(mActivity: Activity) {
        try {
            val inputMethodManager = mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(mActivity.currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getDateNow(format: String): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(Date())
    }

    fun formatSeconds(timeInSeconds: Int): String? {
        val hours = timeInSeconds / 3600
        val secondsLeft = timeInSeconds - hours * 3600
        val minutes = secondsLeft / 60
        val seconds = secondsLeft - minutes * 60
        var formattedTime = ""
//        if (hours < 10) formattedTime += "0"
//        formattedTime += "$hours:"
        if (minutes < 10) formattedTime += "0"
        formattedTime += "$minutes:"
        if (seconds < 10) formattedTime += "0"
        formattedTime += seconds
        return formattedTime
    }

    fun getDateNow(): Date {
        return Date()
    }

    fun getDateDuration(startDate: Date, endDate: Date): String {
        var different = endDate.time - startDate.time
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different = different % daysInMilli
        val elapsedHours = different / hoursInMilli
        different = different % hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different = different % minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        var result = ""
        if(elapsedDays == 0L){
            result = "$elapsedHours hours, $elapsedMinutes minutes, $elapsedSeconds seconds"
        }else if(elapsedDays == 0L && elapsedHours == 0L){
            result = "$elapsedMinutes minutes, $elapsedSeconds seconds"
        }else if(elapsedDays == 0L && elapsedHours == 0L && elapsedMinutes == 0L){
            result = "$elapsedSeconds seconds"
        }else{
            result = "$elapsedDays days, $elapsedHours hours, $elapsedMinutes minutes, $elapsedSeconds seconds"
        }
        return result
    }

    fun printDifference(startDate: Date, endDate: Date) {
        //milliseconds
        var different = endDate.time - startDate.time
        println("startDate : $startDate")
        println("endDate : $endDate")
        println("different : $different")
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different = different % daysInMilli
        val elapsedHours = different / hoursInMilli
        different = different % hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different = different % minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        System.out.printf(
            "%d days, %d hours, %d minutes, %d seconds%n",
            elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
        )
    }

    fun firebaseAnalyticEventBundle(
        page: String,
        key_event: String?,
        key_activity: String?,
        userId: String?,
        env: String
    ): Bundle {
        val timestamp = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val bundle = Bundle()
        bundle.putString(("page"), page)
        if(key_event != null){
            bundle.putString(("key_event"), key_event)
        }
        if(key_activity != null){
            bundle.putString(("key_activity"), key_activity)
        }
        if(userId != null){
            bundle.putString(("userId"), userId)
        }
        bundle.putString(("environtment"), env)
        bundle.putString(("device_type"), "android")
        bundle.putString(("timestamp"), timestamp)
        bundle.putString(("fingerprint"), "${userId}_android_${timestamp}_${env}_${page}")
        return bundle
    }

    //0: day, 1: hour, 2: minute, 3: second
    fun calculateTime(type: Int, currentTime: Long, lastTime: Long): Long {
        val seconds = (currentTime - lastTime) / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val time: String = when {
            days > 0 -> {
                "$days days ago"
            }
            hours > 0 -> {
                "$hours hours ago"
            }
            minutes > 0 -> {
                "$minutes minutes ago"
            }
            else -> {
                "$seconds seconds ago"
            }
        }
        Timber.d(time)

        return when (type) {
            0 -> days
            1 -> hours
            2 -> minutes
            else -> seconds
        }
    }

    fun encrypt(data: String, publicKey: Key?): String {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA-256andMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val bytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun decrypt(data: String, privateKey: Key?): String {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedData = Base64.decode(data, Base64.DEFAULT)
        val decodedData = cipher.doFinal(encryptedData)
        return String(decodedData)
    }

    fun getPublicKey(keyString: String): PublicKey? {
        val base64Key = Base64.decode(keyString, Base64.DEFAULT)
        val sigBytes: ByteArray = base64Key
        var publicKeyString = String(sigBytes, Charsets.UTF_8)
        publicKeyString = publicKeyString.replace("-----BEGIN PUBLIC KEY-----\n", "")
        publicKeyString = publicKeyString.replace("-----END PUBLIC KEY-----", "")
        Timber.d("publicKeyString: $publicKeyString")
        val sigBytes1: ByteArray = Base64.decode(publicKeyString, Base64.DEFAULT)
        val x509KeySpec = X509EncodedKeySpec(sigBytes1)
        var keyFact: KeyFactory? = null
        try {
            keyFact = KeyFactory.getInstance("RSA")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        }
        try {
            return keyFact!!.generatePublic(x509KeySpec)
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return null
    }

    fun getPrivateKey(keyString: String): PrivateKey? {
        val base64Key = Base64.decode(keyString, Base64.DEFAULT)
        val sigBytes: ByteArray = base64Key
        var privateKeyString = String(sigBytes, Charsets.UTF_8)
        privateKeyString = privateKeyString.replace("-----BEGIN PRIVATE KEY-----\n", "")
        privateKeyString = privateKeyString.replace("-----END PRIVATE KEY-----", "")

        //val sigBytes: ByteArray = keyString.toByteArray()

        val sigBytes1: ByteArray = Base64.decode(privateKeyString, Base64.DEFAULT)

        val x509KeySpec = X509EncodedKeySpec(sigBytes1)
        val pkcS8EncodedKeySpec = PKCS8EncodedKeySpec(sigBytes1)
        var keyFact: KeyFactory? = null
        try {
            keyFact = KeyFactory.getInstance("RSA")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        }
        try {
            return keyFact!!.generatePrivate(pkcS8EncodedKeySpec)
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return null
    }

    fun ellipsize(text: String, max: Int): String {
        if (textWidth(text) <= max) return text

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters...
        var end = text.lastIndexOf(' ', max - 3)

        // Just one long word. Chop it off.
        if (end == -1) return text.substring(0, max - 3) + "..."

        // Step forward as long as textWidth allows.
        var newEnd = end
        do {
            end = newEnd
            newEnd = text.indexOf(' ', end + 1)

            // No more spaces.
            if (newEnd == -1) newEnd = text.length
        } while (textWidth(text.substring(0, newEnd) + "...") < max)
        return text.substring(0, end) + "..."
    }

    fun ellipsize2(text: String, max: Int): String {
        if (text.length <= max) return text

        return text.substring(0, max) + "..."
    }

    private fun textWidth(str: String): Int {
        return (str.length - str.replace(NON_THIN.toRegex(), "").length / 2) as Int
    }

    fun hasInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            if (capabilities != null) {
                return if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) true else if (capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    )
                ) true else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) true else false
            }
        } else {
            return try {
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo != null && networkInfo.isConnected
            } catch (e: NullPointerException) {
                false
            }
        }
        return false
    }
}