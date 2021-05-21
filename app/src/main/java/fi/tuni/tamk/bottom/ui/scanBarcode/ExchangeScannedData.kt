package fi.tuni.tamk.test2

/**
 * This interface is to used for to send scanned data between classes
 *
 * @author Jennina Färm
 * @since 1.0
 */
interface ExchangeScannedData {
    /**
     * Sends the scanned barcode to receiver as String
     * @param code the barcode scanned
     */
    fun sendScannedCode(code: String?)
}