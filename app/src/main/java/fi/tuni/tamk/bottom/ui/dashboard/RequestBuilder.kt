package fi.tuni.tamk.bottom.ui.dashboard

import android.util.Base64
import fi.tuni.tamk.bottom.Config
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * An object to handle and create authority and fetch urls for fatSecret
 *
 * @author Jennina Färm
 * @since 1.1
 */
object RequestBuilder {

    /**
     * app segnature method that is wanted from the api
     */
    private val APP_SIGNATURE_METHOD = "HmacSHA1"

    /**
     * http method that is used
     */
    private val HTTP_METHOD = "GET"

    /**
     * base url that is fetched
     */
    private val APP_URL = "https://platform.fatsecret.com/rest/server.api"

    /**
     * buildAuthParams builds a Array of Strings that has current authorities
     * demanded by fatSecret API
     *
     * @return array of the authorities
     */
    private fun buildAuthParams() : Array<String> {
        return arrayOf(
                "oauth_consumer_key=${Config.consumer_key}",
                "oauth_signature_method=HMAC-SHA1",
                "oauth_timestamp=" + (System.currentTimeMillis() / 1000).toString(),
                "oauth_nonce=" + nonce(),
                "oauth_version=1.0",
                "format=json"
        )
    }

    /**
     * getIdByBarcode creates the url for fetching the id of the product for barcode given
     *
     * @param barcode the barcode that id is based on
     * @return the url with current authorities
     */
    fun getIdByBarcode(barcode : String) : String {
        val params: MutableList<String> = buildAuthParams().toMutableList()
        params.add("method=food.find_id_for_barcode")
        params.add("barcode=$barcode")
        params.add("oauth_token=${Config.client_token}")
        params.add("oauth_signature=" + sign(params.toTypedArray()))

        return this.APP_URL + "?" + paramsToString(params.toTypedArray())
    }
    /**
     * getFoodById creates the url for fetching the food of the product for id given
     *
     * @param id the id that food is based on
     * @return the url with current authorities
     */
    fun getFoodById(id : String) : String {
        val params: MutableList<String> = buildAuthParams().toMutableList()
        params.add("method=food.get.v2")
        params.add("food_id=$id")
        params.add("oauth_signature=" + sign(params.toTypedArray()))

        return this.APP_URL + "?" + paramsToString(params.toTypedArray())
    }


    /**
     * encode encodes the given string and replaces unwanted characters
     *
     * @param encodable the string that is encoded
     * @return the string that is encoded
     */
    private fun encode(encodable: String) : String {
        return try {
            URLEncoder.encode(encodable, "utf-8")
                    .replace("+", "%20")
                    .replace("!", "%21")
                    .replace("*", "%2A")
                    .replace("\\", "%27")
                    .replace("(", "%28")
                    .replace(")", "%29")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e.message, e)
        }
    }

    /**
     * paramsToString gets the array and joins it to a string with & separator
     *
     * @param params the array of params for the fetch url
     * @return the string with & separator
     */
    fun paramsToString(params: Array<String>) : String {
        val p = params
        Arrays.sort(p)
        return p.joinToString(separator = "&")
    }

    /**
     * sign creates a a unique signature for the fetch method that can be
     * used only once.
     *
     * @param params array of params that is used for the sign
     * @return a string with the unique signature
     */
    fun sign(params: Array<String>) : String {
        val encodedURI: String = encode(this.APP_URL)
        val encodedParams = encode(paramsToString(params))

        val p = arrayOf(this.HTTP_METHOD, encodedURI, encodedParams)
        val text = p.joinToString(separator = "&")
        val key = Config.consumer_secret + "&"
        val secretKey = SecretKeySpec(key.toByteArray(), this.APP_SIGNATURE_METHOD)
        var sign = ""
        try {
            val m: Mac = Mac.getInstance(this.APP_SIGNATURE_METHOD)
            m.init(secretKey)
            sign = encode(String(Base64.encode(m.doFinal(text.toByteArray()), Base64.DEFAULT)).trim())
        } catch (e: NoSuchAlgorithmException) {
            println("NoSuchAlgorithmException: " + e.message)
        } catch (e: InvalidKeyException) {
            println("InvalidKeyException: " + e.message)
        }
        return sign
    }


    /**
     * nonce creates randomly generated nonce value for the fetching.
     *
     * @return the randomly generated value for nonce.
     */
    fun nonce(): String {
        val r = Random()
        val n = StringBuffer()
        for (i in 0 until r.nextInt(8) + 2) {
            n.append(r.nextInt(26).toString() + 'a')
        }
        return n.toString()
    }
}