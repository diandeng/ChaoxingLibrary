package com.diandeng.chaoxinglibrary.util

import java.security.MessageDigest

object MD5Utils {
    fun toHexMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { byte ->
            String.format("%02x", byte)
        }
    }
}