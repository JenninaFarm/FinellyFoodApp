package fi.tuni.tamk.bottom.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import fi.tuni.tamk.bottom.R

/**
 * Fragment class to have the ui for the home view
 *
 * @since 1.1
 * @author Jennina Färm
 */
class HomeFragment : Fragment() {

    /**
     * Viewmodel to handle live data that can be modified.
     */
    private lateinit var homeViewModel: HomeViewModel

    /**
     * Text view that includes a welcoming
     */
    lateinit var textView: TextView

    /**
     * onCreateView initializes the textView and homeViewModel and inflates the view to the fragment
     */
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        textView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}