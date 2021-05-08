package fi.tuni.tamk.test2

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.nio.ByteBuffer

class BarcodeScannerProcessor(context: Context, exchangeScannedData: ExchangeScannedData) : VisionProcessor<List<Barcode>>(context) {
    private val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A)
            .build()
    private val barcodeScanner : BarcodeScanner = BarcodeScanning.getClient(options)
    private val exchangeScannedData = exchangeScannedData
    override fun stop() {
        super.stop()
        barcodeScanner.close()
    }

    override fun detectInImage(image: InputImage?): Task<List<Barcode>> {
        return barcodeScanner.process(image)
    }

    override fun onFailure(e: Exception) {
        Log.e("BarcodeProsessor", "Barcode detection failed " + e);
    }

    override fun onSuccess(results: List<Barcode>) {
        Log.d("BarcodeProsessor", "Barcode detection succeeded ");

        for (i in results.indices) {
            val barcode = results[i]
            if (barcode != null && barcode.rawValue != null && !barcode.rawValue.isEmpty()) {
                exchangeScannedData.sendScannedCode(barcode.rawValue)
                println(barcode.rawValue)
            }
        }
    }
}