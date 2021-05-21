package fi.tuni.tamk.bottom.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.fasterxml.jackson.databind.ObjectMapper
import fi.tuni.tamk.bottom.R
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * A Fragment class to have and handle the logic of dashboard
 * which includes fetching and placing the nutrient content
 * to the UI based on barcode.
 *
 * At this point product is not fetched based on the barcode, since
 * the app hasn't got the license to use the get_id_for_barcode fetching method.
 * That function is added as soon as the license has been permitted.
 *
 * @author Jennina Färm
 * @since 1.1
 */
class DashboardFragment : Fragment() {

    /**
     * Has the arguments which are given from navigation actions
     */
    private val args: DashboardFragmentArgs by navArgs()

    /**
     * Text view for calories
     */
    private lateinit var calories : TextView

    /**
     * Text view for name of the product
     */
    private lateinit var item : TextView

    /**
     * Text view for carbohydrates
     */
    private lateinit var carbs : TextView

    /**
     * Text view for fat
     */
    private lateinit var fat : TextView

    /**
     * Text view for protein
     */
    private lateinit var protein : TextView

    /**
     * OnCreateView get inflates teh view and fragment together
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_dashboard, container, false)

    /**
     * OnViewCreated initializes the TextViews and finds the barcode from args
     * If barcode is found fetchInfo for the barcode is called.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val barcode = args.barcode
        calories = view.findViewById(R.id.calories_text)
        item = view.findViewById(R.id.food_name_text)
        carbs = view.findViewById(R.id.carbs_text)
        fat = view.findViewById(R.id.fat_text)
        protein = view.findViewById(R.id.protein_text)
        var textView : TextView = view.findViewById(R.id.barcode_text)
        textView.text = barcode
        if(barcode != "") {
            fetchInfo(barcode)
        }
    }

    /**
     * fetchInfo (async) fetches id of the product based on barcode given and
     * then nutrient content based on the id found from the barcode.
     *
     * After fetching UI is updated.
     *
     * At this point there is no fetching from barcode due to lack of
     * license needed to call get_id_for_barcode in fatSecrect API.
     *
     * @param barcode the barcode which id is wanted to find
     */
    private fun fetchInfo( barcode : String) {
        thread() {
            //var url = URL(RequestBuilder.getIdByBarcode(barcode))
            var url = URL(RequestBuilder.getFoodById("4384"))
            //RequestBuilder.getFoodById("33691")
            var result = connect(url)
            updateUi(result)
        }
    }

    /**
     * UpdateUi updates the ui based on the result given
     *
     * @param result the result that has the info for updates ui
     */
    private fun updateUi(result : String) {
        val mp = ObjectMapper()
        val foodJson : FoodJson = mp.readValue(result, FoodJson::class.java)
        activity?.runOnUiThread() {
            item.text = foodJson.food!!.food_name!!
            val serving100g = foodJson.food!!.servings!!.serving!!.last()
            calories.text = serving100g.calories + " kcal"
            carbs.text = serving100g.carbohydrate + " g"
            fat.text = serving100g.fat + " g"
            protein.text = serving100g.protein + " g"
        }
    }

    /**
     * logLargeString is a helper method to see all the info given from fetch methods.
     *
     * @param str the string that is wanted to print
     */
    fun logLargeString(str : String) {

        if(str.length > 3000) {
            Log.i("TAG", str.substring(0, 3000));
            logLargeString(str.substring(3000));
        } else
            Log.i("TAG", str);
    }

    /**
     * Connect method is used to create httpUrlConnection to given url
     *
     * @param url the url that is wanted to make a connection to
     */
   private fun connect(url : URL) : String{
        val conn = url.openConnection() as HttpURLConnection
        var result: String = ""
        try {
            result = conn.inputStream.bufferedReader().use { it.readText() }
            //Log.d("Dash: Get food result", result)
        } catch (e: Exception) {
            Log.d("Dash: Get food error", e.toString())
        } finally {
            conn.disconnect()
        }
       return result
    }
}