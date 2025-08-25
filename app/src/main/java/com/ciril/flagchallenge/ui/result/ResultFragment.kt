package com.ciril.flagchallenge.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ciril.flagchallenge.R
import com.ciril.flagchallenge.data.repository.ScheduleDataSource
import com.ciril.flagchallenge.databinding.FragmentResultBinding
import com.ciril.flagchallenge.databinding.HeaderCommonBinding
import com.ciril.flagchallenge.utils.ChallengeConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null

    private lateinit var header: HeaderCommonBinding
    private val binding get() = _binding!!

    private var revealJob: Job? = null
    private var revealed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        header = HeaderCommonBinding.bind(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        revealed = savedInstanceState?.getBoolean("revealed") ?: false

        // get score from args (you already navigate with "score")
        val score = arguments?.getInt("score") ?: 0
        val scoreStr = String.format("%02d", score)
        val totalQuestionsStr = String.format("%02d", ChallengeConfig.TOTAL_QUESTIONS)
        binding.tvScoreValue.text = getString(
            R.string.score_out_of,
            scoreStr,
            totalQuestionsStr
        )

        if (revealed) {
            // Show score immediately, don’t animate again
            binding.tvGameOver.visibility = View.GONE
            binding.scoreContainer.apply {
                alpha = 1f
                visibility = View.VISIBLE
            }
        } else {
            binding.tvGameOver.visibility = View.VISIBLE
            binding.scoreContainer.visibility = View.GONE

            revealJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(1000)
                revealed = true
                crossfadeGameOverToScore()
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    revealJob?.cancel() // stop pending reveal
                    findNavController().popBackStack(R.id.scheduleFragment, false)
                }
            }
        )

    }

    private fun crossfadeGameOverToScore() = with(binding) {
        // Fade out GAME OVER
        binding.tvGameOver.animate()
            .alpha(0f)
            .setDuration(250)
            .withEndAction {
                binding.tvGameOver.visibility = View.GONE
                binding.scoreContainer.apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate().alpha(1f).setDuration(250).start()
                }
            }
            .start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}