package com.example.calllogreader

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "call_log"
    private val PERMISSION_REQUEST_CODE = 1

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "getCallLogs") {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), PERMISSION_REQUEST_CODE)
                } else {
                    result.success(getCallLogs())
                }
            } else {
                result.notImplemented()
            }
        }
    }

    private fun getCallLogs(): List<Map<String, String>> {
        val callLogs = mutableListOf<Map<String, String>>()
        val uri: Uri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        )
        val sortOrder = "${CallLog.Calls.DATE} DESC"
        contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            while (cursor.moveToNext()) {
                val callLog = mapOf(
                        "number" to cursor.getString(numberIndex),
                        "type" to cursor.getString(typeIndex),
                        "date" to cursor.getString(dateIndex),
                        "duration" to cursor.getString(durationIndex)
                )
                callLogs.add(callLog)
            }
        }
        return callLogs
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, CHANNEL).invokeMethod("getCallLogs", getCallLogs())
            }
        }
    }
}
