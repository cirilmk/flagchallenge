package com.ciril.flagchallenge.ui.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import coil.load
import com.ciril.flagchallenge.R
import com.ciril.flagchallenge.databinding.FragmentChallengeBinding
import com.ciril.flagchallenge.databinding.HeaderCommonBinding
import com.ciril.flagchallenge.model.FlagQuestion
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null

    private lateinit var header: HeaderCommonBinding
    private val binding get() = _binding!!

    private val vm: ChallengeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        header = HeaderCommonBinding.bind(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.ui.collectLatest { s ->
                    if (s == null) return@collectLatest
                    when (s) {
                        is ChallengeState.Question -> {
                            binding.tvQIndex.text = getString(R.string.question_number, s.index + 1)
                            header.tvTimer.text = getString(R.string.time_format, s.remainingSec)
                            binding.tvPrompt.isGone = false

                            val url =
                                "https://flagcdn.com/w160/${s.question.country_code.lowercase()}.png"
                            binding.ivFlag.load(url) {
                                crossfade(true)
                                placeholder(R.drawable.flag)
                                error(R.drawable.flag)
                                fallback(R.drawable.flag)
                            }


                            renderOptions(
                                q = s.question,
                                qIndex = s.index,
                                selectionId = s.selectionId,
                                isInterval = false
                            )

                        }

                        is ChallengeState.Interval -> {
                            binding.tvQIndex.text = getString(R.string.question_number, s.index + 1)
                            header.tvTimer.text = getString(R.string.time_format, s.remainingSec)

                            val url =
                                "https://flagcdn.com/w160/${s.question.country_code.lowercase()}.png"
                            binding.ivFlag.load(url) { crossfade(true) }

                            renderOptions(
                                q = s.question,
                                qIndex = s.index,
                                selectionId = s.selectionId,
                                isInterval = true
                            )

                        }

                        is ChallengeState.Finished -> {
                            val action = R.id.action_challenge_to_result
                            val args = Bundle().apply { putInt("score", s.score) }
                            findNavController().navigate(action, args,
                                navOptions {
                                    popUpTo(R.id.challengeFragment) {
                                        inclusive = true
                                    } // remove Challenge
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun renderOptions(
        q: FlagQuestion,
        qIndex: Int,
        selectionId: Int?,
        isInterval: Boolean
    ) {
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

        q.countries.take(4).forEachIndexed { i, c ->
            btns[i].text = c.country_name
        }

        if (!isInterval) {
            // Question phase
            q.countries.take(4).forEachIndexed { i, c ->
                btns[i].isEnabled = true
                val state = if (selectionId == c.id) OptionState.SELECTED else OptionState.DEFAULT
                btns[i].applyOptionState(state, lbls[i], requireContext())
                lbls[i].text = ""
                btns[i].setOnClickListener {
                    // Clear visuals then mark selected
                    btns.forEachIndexed { j, b -> b.applyOptionState(OptionState.DEFAULT, lbls[j], requireContext()) }
                    btns[i].applyOptionState(OptionState.SELECTED, lbls[i], requireContext())
                    vm.selectOption(qIndex, countryId = c.id)
                }
            }
        } else {
            // Interval phase
            q.countries.take(4).forEachIndexed { i, c ->
                val isCorrect = c.id == q.answer_id
                val isSelected = selectionId == c.id
                btns[i].isEnabled = false
                when {
                    isCorrect -> btns[i].applyOptionState(
                        OptionState.CORRECT,
                        lbls[i],
                        requireContext()
                    )

                    isSelected -> btns[i].applyOptionState(
                        OptionState.WRONG,
                        lbls[i],
                        requireContext()
                    )

                    else -> btns[i].applyOptionState(OptionState.DEFAULT, lbls[i], requireContext())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshNow()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
