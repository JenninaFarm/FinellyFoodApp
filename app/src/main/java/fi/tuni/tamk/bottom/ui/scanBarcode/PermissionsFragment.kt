package fi.tuni.tamk.bottom.ui.scanBarcode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import fi.tuni.tamk.bottom.R

/** Permission request code */
private val PERMISSION_REQUEST_CODE = 10
/** the permissions that needs to be requested */
private val PERMISSION_REQUIRED = arrayOf(Manifest.permission.CAMERA/*, Manifest.permission.INTERNET*/)

/**
 *  This fragment requests permissions if needed and then displays camera
 *
 *  @author Jennina Färm
 *  @since 1.1
 */

class PermissionsFragment : Fragment() {
    /**
     *  Checks has permissions been requested and then navigates to camera
     *  @param savedInstanceState saved instance in state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!hasPermissions(requireContext())) {
            requestPermissions(PERMISSION_REQUIRED, PERMISSION_REQUEST_CODE)
        } else {
            navigateToCamera()
        }
    }

    /**
     * requests permissions if needed
     *
     * @param requestCode the request code
     * @param permissions array of permissions
     * @param grantResults array of ints if requests are granted
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_CODE) {
            if(PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
                navigateToCamera()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * navigates to camera fragment
     */
    private fun navigateToCamera() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
                PermissionsFragmentDirections.actionPermissionsFragmentToNavigationScanBarcode())
        }
    }

    /**
     * helper object to check if the permissions are granted
     */
    companion object {
        fun hasPermissions(context: Context) = PERMISSION_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

