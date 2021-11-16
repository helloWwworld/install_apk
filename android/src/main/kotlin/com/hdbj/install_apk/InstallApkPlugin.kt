package com.hdbj.install_apk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.NonNull
import androidx.core.content.FileProvider

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.util.GeneratedPluginRegister
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.io.FileNotFoundException

/** InstallApkPlugin */
class InstallApkPlugin : FlutterPlugin, MethodCallHandler,ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var activity: Activity

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "install_apk")
        channel.setMethodCallHandler(this)

    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "install" -> {
                val filePath = call.argument<String>("filePath")
                val appId = call.argument<String>("appId")
                try {
                    installApk(filePath, appId)
                    result.success("Success")
                } catch (e: Throwable) {
                    result.error(e.javaClass.simpleName, e.message, null)
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun installApk(filePath: String?, currentAppId: String?) {
        if (filePath == null) throw NullPointerException("fillPath is null!")

        val file = File(filePath)
        if (!file.exists()) throw FileNotFoundException("$filePath is not exist! or check permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (canRequestPackageInstalls(activity)) install24(activity, file, currentAppId)
            else
                showSettingPackageInstall(activity)
        } else {
            installBelow24(activity, file)
        }
    }

    private fun showSettingPackageInstall(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivityForResult(intent, 1234)
        } else {
            throw RuntimeException("VERSION.SDK_INT < O")
        }

    }

    private fun canRequestPackageInstalls(activity: Activity): Boolean {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || activity.packageManager.canRequestPackageInstalls()
    }

    private fun installBelow24(context: Context, file: File?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromFile(file)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }
    private fun install24(context: Context?, file: File?, appId: String?) {
        if (context == null) throw NullPointerException("context is null!")
        if (file == null) throw NullPointerException("file is null!")
        if (appId == null) throw NullPointerException("appId is null!")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val uri: Uri = FileProvider.getUriForFile(context, "$appId.fileProvider.install", file)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

    override fun onAttachedToActivity(p0: ActivityPluginBinding) {
        this.activity = p0.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(p0: ActivityPluginBinding) {
        TODO("Not yet implemented")
//        activity = p0.activity;
    }

    override fun onDetachedFromActivity() {
        TODO("Not yet implemented")
    }

}
