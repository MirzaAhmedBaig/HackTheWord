package com.hacktheword.mirza.hacktheword

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Environment
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import java.util.regex.Pattern
import java.io.File
import java.io.FileWriter
import java.io.IOException
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo


/**
 * Created by Mirza-Ahmed on 15-04-2018.
 */
class MyAccessibilityService : AccessibilityService() {
    private val TAG = MyAccessibilityService::class.java.simpleName
    private var REGEX_FIND_WORD = "(?i).*?\\b%s\\b.*?"
    private val pattern by lazy {
        Pattern.compile("\\bmirza\\b")
    }
    private var isNewNode = false
    private var lastText = ""
    private var hintText = ""
    private var nodeText = ""
    private var isChanged=false
    private val replacingString="ahmed"

    override fun onServiceConnected() {
        /*val m = p.matcher("Print this")
        m.find()
        System.out.println(m.group())*/

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_VIEW_CLICKED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
        info.notificationTimeout = 30
        serviceInfo = info

    }

    override fun onInterrupt() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent?) {
        val interactedNodeInfo = accessibilityEvent!!.source
        val eventType = accessibilityEvent.eventType
        var eventTypeText = ""
        when (eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                eventTypeText = "Text Changed : "
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                if (interactedNodeInfo.text != null) {
                    if (!isNewNode && lastText != interactedNodeInfo.text.toString()) {
                        lastText = interactedNodeInfo.text.toString()
                        hintText = interactedNodeInfo.text.toString()
                        isNewNode = true

                    }
                }

                isNewNode = true
                eventTypeText = "View Clicked : "
                Log.d(TAG, "TYPE_VIEW_CLICKED : ${accessibilityEvent.text}")
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                eventTypeText = "Focused: "

            }
        }
        Log.d(TAG, "TYPE_VIEW_FOCUSED : ${accessibilityEvent.contentDescription}")
        if (interactedNodeInfo.text != null) {
            if (isChanged){
                isChanged=false
                val lastChar=interactedNodeInfo.text.toString().substring(interactedNodeInfo.text.toString().length-1)
                Log.d(TAG,"Last Char : $lastChar")
                if(lastChar!=" "){
                    val text=interactedNodeInfo.text.toString()
                    var undoText=""
                    if(text.length>replacingString.length){
                        undoText=text.substring(0,text.length- replacingString.length-1)
                    }
                    undoText=undoText.plus("mirza").plus(lastChar)
                    Log.d(TAG,"Undo Text : $undoText")
                    pasteText(interactedNodeInfo,undoText)
                    return
                }
            }
            Log.d(TAG, "Is Pattern Matched : ${containsWord(interactedNodeInfo.text.toString(), "mirza")}")

            if (containsWord(interactedNodeInfo.text.toString(), "mirza")) {
                nodeText = interactedNodeInfo.text.toString()
                var replacedText = nodeText
                replacedText=replacedText.replace("mirza", "ahmed", true)
                Log.d(TAG,"Replaced String : $replacedText")
                pasteText(interactedNodeInfo, replacedText)
                isChanged=true
            } else {
//                val lastChar=interactedNodeInfo.text.toString().substring(interactedNodeInfo.text.toString().length-1)
                val undoText = interactedNodeInfo.text.toString()
                undoText.replace("mirza", "(?i).*?\\bahmed\\b.*?")
//                pasteText(interactedNodeInfo, undoText)
            }
        }
    }

    private fun containsWord(text: String, word: String): Boolean {
        val regex = String.format(REGEX_FIND_WORD, Pattern.quote(word))
        return text.matches(regex.toRegex())
    }

    fun generateNoteOnSD(sFileName: String, sBody: String) {
        try {
            val root = File(Environment.getExternalStorageDirectory(), "Notes")
            if (!root.exists()) {
                root.mkdirs()
            }
            val gpxfile = File(root, sFileName)
            val writer = FileWriter(gpxfile)
            writer.append(sBody)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun pasteText(node: AccessibilityNodeInfo, text: String) {
        val arguments = Bundle()
        arguments.putString(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        node.performAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT, arguments)
        Log.d(TAG,"Done paste")
    }

}
