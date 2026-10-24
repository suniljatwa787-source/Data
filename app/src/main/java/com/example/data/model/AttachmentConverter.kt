package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object AttachmentConverter {

    fun attachmentsToJson(attachments: List<RecordAttachment>): String {
        val array = JSONArray()
        for (att in attachments) {
            val obj = JSONObject().apply {
                put("id", att.id)
                put("fileName", att.fileName)
                put("mimeType", att.mimeType)
                put("fileSizeBytes", att.fileSizeBytes)
                put("localPath", att.localPath)
                put("uriString", att.uriString)
                put("addedAt", att.addedAt)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToAttachments(json: String?): List<RecordAttachment> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<RecordAttachment>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RecordAttachment(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        fileName = obj.optString("fileName", "Attachment"),
                        mimeType = obj.optString("mimeType", "application/octet-stream"),
                        fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
                        localPath = obj.optString("localPath", ""),
                        uriString = obj.optString("uriString", ""),
                        addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            // fallback gracefully
        }
        return list
    }

    fun customFieldsToJson(fields: Map<String, String>): String {
        val obj = JSONObject()
        for ((k, v) in fields) {
            obj.put(k, v)
        }
        return obj.toString()
    }

    fun jsonToCustomFields(json: String?): Map<String, String> {
        if (json.isNullOrBlank()) return emptyMap()
        val map = mutableMapOf<String, String>()
        try {
            val obj = JSONObject(json)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optString(key, "")
            }
        } catch (_: Exception) {
            // fallback
        }
        return map
    }
}
