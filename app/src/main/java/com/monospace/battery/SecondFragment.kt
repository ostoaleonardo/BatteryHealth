package com.monospace.battery

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.monospace.battery.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        displayVersion()

        binding.updateCard.setOnClickListener {
            openGooglePlay()
        }

        binding.rateCard.setOnClickListener {
            openGooglePlay()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayVersion() {
        val versionName = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0)
            .versionName

        binding.version.text = getString(R.string.settings_version, versionName)
    }

    private fun openGooglePlay() {
        val appPackageName = requireContext().packageName
        val marketUri =
            Uri.parse("market://details?id=$appPackageName")
        val googlePlayUri =
            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")

        try {
            requireContext().startActivity(
                Intent(Intent.ACTION_VIEW, marketUri)
            )
        } catch (e: ActivityNotFoundException) {
            requireContext().startActivity(
                Intent(Intent.ACTION_VIEW, googlePlayUri)
            )
        }
    }
}