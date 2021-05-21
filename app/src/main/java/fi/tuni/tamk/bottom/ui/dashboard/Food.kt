package fi.tuni.tamk.bottom.ui.dashboard

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Class for handling fetch information
 *
 * @author Jennina Färm
 * @since 1.1
 * @param food the object that is given from fetch
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FoodJson(var food: Food? = null) {
}
/**
 * Class for handling fetch information
 *
 * @author Jennina Färm
 * @since 1.1
 * @param food_name the name that is given from fetch
 * @param servings the object that is given from fetch
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Food(var food_name: String? = null, var servings : Servings? = null) {
}
/**
 * Class for handling fetch information
 * @author Jennina Färm
 * @since 1.1
 * @param serving the list of objects that is given from fetch
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Servings(var serving : MutableList<Serving>? = null) {}
/**
 * Class for handling fetch information
 *
 * @author Jennina Färm
 * @since 1.1
 * @param calories the amount of calories that is given from fetch
 * @param carbohydrate the amount of carbohydrate that is given from fetch
 * @param fat the amount of fat that is given from fetch
 * @param fiber the amount of fiber that is given from fetch
 * @param metric_serving_amount the amount of metric_serving_amount that is given from fetch
 * @param metric_serving_unit the metric_serving_unit that is given from fetch
 * @param protein the amount of protein that is given from fetch
 * @param sugar the amount of sugar that is given from fetch
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Serving(
        var calories: String? = null,
        var carbohydrate : String? = null,
        var fat : String? = null,
        var fiber : String? = null,
        var metric_serving_amount : String? = null,
        var metric_serving_unit : String? = null,
        var protein : String? = null,
        var sugar : String? = null
        ) {
}