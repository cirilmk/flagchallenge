package com.ciril.flagchallenge.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ciril.flagchallenge.R
import com.ciril.flagchallenge.databinding.FragmentScheduleBinding
import com.ciril.flagchallenge.databinding.HeaderCommonBinding
import com.ciril.flagchallenge.utils.hasInternet
import com.ciril.flagchallenge.utils.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null

    private lateinit var header: HeaderCommonBinding
    private val binding get() = _binding!!

    private val vm: ScheduleViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        header = HeaderCommonBinding.bind(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() = with(binding) {
        binding.btnSave.setOnClickListener {
            val h = two(binding.etHour1.text?.toString(), binding.etHour2.text?.toString())
            val m = two(binding.etMin1.text?.toString(), binding.etMin2.text?.toString())
            val s = two(binding.etSec1.text?.toString(), binding.etSec2.text?.toString())
            if (h == 0 && m == 0 && s == 0) {
                Toast.makeText(requireContext(), "Please set a time > 0", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (!requireContext().hasInternet()) {
                    noInternetNotification()
                    return@setOnClickListener
                }
                vm.saveSchedule(h, m, s)
                Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
                clearInputs()
                hideKeyboard()
            }
        }

    }

    private fun clearInputs() = with(binding) {
        listOf(etHour1, etHour2, etMin1, etMin2, etSec1, etSec2)
            .forEach { it.text?.clear() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.ui.collectLatest { state ->
                    when (state) {
                        is ScheduleUiState.Idle -> {
                            binding.tvSubHeading.text = getString(R.string.challenge_schedule)
                            header.tvTimer.text = "--:--"
                            binding.scheduleInputs.visibility = View.VISIBLE
                        }

                        is ScheduleUiState.Waiting -> {
                            // Before pre-start window
                            binding.tvSubHeading.text = getString(R.string.already_on_schedule)
                            header.tvTimer.text = "--:--"
                            binding.scheduleInputs.visibility = View.VISIBLE
                        }

                        is ScheduleUiState.Prestart -> {
                            binding.scheduleInputs.visibility = View.GONE
                            // Show the required banner text with live 20s countdown
                            val sec = state.secondsLeft.coerceIn(1, 20)
                            binding.tvSubHeading.text = buildString {
                                append(getString(R.string.will_start_in))
                                append("\n")
                                append(
                                    getString(
                                        R.string.time_format,
                                        sec
                                    )
                                ) // Just need to update the format if needed other time
                            }
                        }

                        is ScheduleUiState.StartNow -> {
                            // Navigate to Challenge
                            findNavController().navigate(
                                com.ciril.flagchallenge.ui.schedule.ScheduleFragmentDirections
                                    .actionScheduleToChallenge()
                            )
                        }
                    }
                }
            }
        }
    }


    private fun two(tens: String?, ones: String?): Int {
        val d1 = tens?.trim().orEmpty().firstOrNull()?.digitToIntOrNull() ?: 0
        val d2 = ones?.trim().orEmpty().firstOrNull()?.digitToIntOrNull() ?: 0
        return d1 * 10 + d2
    }

    private fun noInternetNotification() {
        Snackbar.make(
            requireView(),
            getString(R.string.no_internet),
            Snackbar.LENGTH_SHORT
        ).apply {
            setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}