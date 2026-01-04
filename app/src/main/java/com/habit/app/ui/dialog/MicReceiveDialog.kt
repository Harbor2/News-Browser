package com.habit.app.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.habit.app.R
import com.habit.app.data.TAG
import com.habit.app.databinding.LayoutDialogMicReceiveBinding
import com.wyz.emlibrary.em.EMManager
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.wyz.emlibrary.util.EMUtil

/**
 * 麦克风声音接收dialog
 */
class MicReceiveDialog : DialogFragment() {
    var _binding: LayoutDialogMicReceiveBinding? = null
    private val binding get() = _binding!!
    var onCloseCallback: ((String?) -> Unit)? = null
    private lateinit var speechRecognizer: SpeechRecognizer

    private var initialY = 0f
    private val dragThreshold = EMUtil.dp2px(100f).toInt()

    val micIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
//        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "准备接收语音")
            binding.editInput.setHint(getString(R.string.hint_say_something))
            binding.editInput.text?.clear()
            binding.tvTitle.text = getString(R.string.text_in_speech_recognition)
            binding.ivVoicePlay.isVisible = false
            binding.lottieVoice.playAnimation()
            binding.lottieVoice.isVisible = true
            binding.ivVoicePause.isVisible = false
        }

        override fun onError(error: Int) {
            Log.d(TAG, "说话错误: $error")
            showNoMatchesUi()
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val firstMatch = matches?.firstOrNull() ?: ""
            Log.d(TAG, "说话结果第一项: $firstMatch")

            if (firstMatch.isEmpty()) {
                showNoMatchesUi()
            } else {
                showFirstMatchesUi(firstMatch)
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBeginningOfSpeech() {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(false)

            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setGravity(Gravity.BOTTOM)

            setOnKeyListener { _, keyCode, event ->
                false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = LayoutDialogMicReceiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateThemeUI()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireActivity())

        initListener()

        startListen()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        speechRecognizer.setRecognitionListener(recognitionListener)

        binding.containerContent.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> initialY = event.rawY
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    if (deltaY > 0) binding.root.translationY = deltaY
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val deltaY = event.rawY - initialY
                    if (deltaY > dragThreshold) dismiss()
                    else binding.root.animate().translationY(0f).setDuration(200).start()
                }
            }
            true
        }
        binding.editInput.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasText = s?.isNotEmpty() ?: false
                binding.btnConfirm.isVisible = hasText
                binding.btnCancel.isVisible = !hasText
            }
        })

        binding.ivVoicePause.setOnClickListener {
            startListen()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }

    private fun showNoMatchesUi() {
        binding.editInput.setHint(getString(R.string.hint_click_to_entry))
        binding.tvTitle.text = getString(R.string.text_no_voice_detected_try_again)
        binding.ivVoicePlay.isVisible = false
        binding.lottieVoice.cancelAnimation()
        binding.lottieVoice.isVisible = false
        binding.ivVoicePause.isVisible = true
    }

    private fun showFirstMatchesUi(firstMatch: String) {
        binding.tvTitle.text = ""
        binding.editInput.setText(firstMatch)
        binding.editInput.setSelection(firstMatch.length)

        binding.ivVoicePlay.isVisible = true
        binding.lottieVoice.cancelAnimation()
        binding.lottieVoice.isVisible = false
        binding.ivVoicePause.isVisible = false
    }

    /**
     * 开始录音
     */
    private fun startListen() {
        binding.root.postDelayed(
            {
                Log.d(TAG, "录音intent执行")
                if (!SpeechRecognizer.isRecognitionAvailable(requireActivity())) {
                    Log.d(TAG, "语音识别不可用")
                    binding.tvTitle.text = ""
                    UtilHelper.showToast(requireContext(), getString(R.string.toast_your_phone_not_support_mic_input))
                    return@postDelayed
                }
                speechRecognizer.startListening(micIntent)
            }, 200
        )
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.editLine)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
        binding.tvTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_70))
        binding.editInput.setHintTextColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        binding.editInput.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.containerContent)
            .setCorner(floatArrayOf(24f, 24f, 0f, 0f))
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.btnCancel)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.create_folder_cancel_color))
        binding.btnCancel.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.btnConfirm)
            .setCorner(12f)
            .setBackGroundColor(R.color.btn_color)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d(TAG, "用户点击 关闭弹窗")
        onCloseCallback?.invoke(binding.editInput.text?.toString()?.trim())
    }

    override fun onDestroyView() {
        _binding = null
        speechRecognizer.stopListening()
        speechRecognizer.destroy()
        super.onDestroyView()
    }

    companion object {
        fun show(
            activity: FragmentActivity,
            onClose: ((String?) -> Unit)? = null
        ): MicReceiveDialog? {
            if (activity.isFinishing || activity.isDestroyed) return null
            val fragment = MicReceiveDialog()
            fragment.onCloseCallback = onClose
            fragment.show(activity.supportFragmentManager, "MicReceiveDialog")
            return fragment
        }
    }
}