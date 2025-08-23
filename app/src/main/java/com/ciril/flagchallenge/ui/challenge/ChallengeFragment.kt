package com.ciril.flagchallenge.ui.challenge

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.ciril.flagchallenge.R
import com.ciril.flagchallenge.databinding.FragmentChallengeBinding
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    private val vm: ChallengeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            vm.ui.collectLatest { s ->
                if (s == null) return@collectLatest
                when (s) {
                    is ChallengeState.Question -> {
                        binding.tvQIndex.text = (s.index + 1).toString()
                        binding.tvTimer.text = "00:${s.remainingSec.toString().padStart(2, '0')}"
                        binding.tvPrompt.isGone = false

                        val url =
                            "https://flagcdn.com/w160/${s.question.country_code.lowercase()}.png"
                        binding.ivFlag.load(url) { crossfade(true) }

                        val btns: List<MaterialButton> = listOf(
                            binding.btnOpt1,
                            binding.btnOpt2,
                            binding.btnOpt3,
                            binding.btnOpt4,
                        )
                        val lbls = listOf(
                            binding.tvLbl1,
                            binding.tvLbl2,
                            binding.tvLbl3,
                            binding.tvLbl4,
                        )

                        s.question.countries.take(4).forEachIndexed { i, c ->
                            btns[i].text = c.country_name
                            btns[i].isEnabled = !s.isLocked

                            // if this country was selected, keep it highlighted
                            val state = if (s.selectionId == c.id) {
                                OptionState.SELECTED
                            } else {
                                OptionState.DEFAULT
                            }
                            btns[i].applyOptionState(state, lbls[i], requireContext())

                            lbls[i].text = ""
                            btns[i].setOnClickListener {
                                // Clear all first
                                btns.forEachIndexed { j, b ->
                                    b.applyOptionState(OptionState.DEFAULT, lbls[j], requireContext())
                                }
                                // Mark selected
                                btns[i].applyOptionState(OptionState.SELECTED, lbls[i], requireContext())
                                vm.selectOption(s.index, c.id)
                            }
                        }
                    }

                    is ChallengeState.Interval -> {
                        binding.tvQIndex.text = (s.index + 1).toString()
                        binding.tvTimer.text = "00:${s.remainingSec.toString().padStart(2, '0')}"

                        val url =
                            "https://flagcdn.com/w160/${s.question.country_code.lowercase()}.png"
                        binding.ivFlag.load(url) { crossfade(true) }

                        val btns: List<MaterialButton> = listOf(
                            binding.btnOpt1,
                            binding.btnOpt2,
                            binding.btnOpt3,
                            binding.btnOpt4,
                        )
                        val lbls = listOf(
                            binding.tvLbl1,
                            binding.tvLbl2,
                            binding.tvLbl3,
                            binding.tvLbl4,
                        )


                        s.question.countries.take(4).forEachIndexed { i, c ->
                            val isCorrect = c.id == s.question.answer_id
                            val isSelected = s.selectionId == c.id
                            btns[i].isEnabled = false
                            when {
                                isCorrect -> btns[i].applyOptionState(OptionState.CORRECT, lbls[i], requireContext())
                                isSelected -> btns[i].applyOptionState(OptionState.WRONG, lbls[i], requireContext())
                                else -> btns[i].applyOptionState(OptionState.DEFAULT, lbls[i], requireContext())
                            }
                        }
                    }

                    is ChallengeState.Finished -> {
                        val action = R.id.action_challenge_to_result
                        val args = Bundle().apply { putInt("score", s.score) }
                        findNavController().navigate(action, args)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
