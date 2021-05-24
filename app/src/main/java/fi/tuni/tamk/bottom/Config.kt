package fi.tuni.tamk.bottom

import android.util.Base64.*
import android.util.Log
import com.fatsecret.platform.services.Base64.encodeToString
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder.encode
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.thread


object Config {

    // not working
    // curl -u "967514f4b85741989ced62d4210c4947:6d2fabfee4fa41c4b736c9a40fb30621" -d "grant_type=client_credentials&scope=basic" -X POST https://oauth.fatsecret.com/connect/token

    val client_id = "967514f4b85741989ced62d4210c4947"
    val client_secret = "6d2fabfee4fa41c4b736c9a40fb30621"
    //REST API OAuth 1.0 Credentials:
    //Your Consumer Key:
    val consumer_key = "967514f4b85741989ced62d4210c4947"
    //Consumer Secret:
    val consumer_secret = "1e0f176270494b3d9ae62c182183fb5f"
    // Client token for barcode method
    val client_token = "83082053298"

    fun requestAccessTokenToFatSecret() {
        thread() {
            val parameters = "scope=basic&grant_type=client_credentials"
            val url = URL("https://oauth.fatsecret.com/connect/token")
            val authority : String = "${this.client_id}:${this.client_secret}"
            //val encoding: String = Base64.getEncoder().encodeToString(authority.getBytes("utf-8"))

            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", authority)
            conn.setRequestProperty("Content-Type", "application/json")

            var result : String = ""
            try {
                result = conn.inputStream.bufferedReader().use {it.readText()}
                Log.d("Config: TokenResult", result)
            } catch (e: Exception) {
                Log.d("Config: AccessToken", e.toString())
            } finally {
                conn.disconnect()
            }
        }
    }
}

