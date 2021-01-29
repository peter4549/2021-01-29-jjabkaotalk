package com.grand.duke.elliot.jjabkaotalk.authentication

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentPhoneAutenticationBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class PhoneAuthenticationFragment: BaseFragment() {

    private lateinit var viewModel: PhoneAuthenticationViewModel
    private lateinit var binding: FragmentPhoneAutenticationBinding

    interface OnClickListener {
        //fun onPositiveButtonClick(folder: Folder)
    }

    interface FragmentContainer {
        fun onRequestOnClickListener(): OnClickListener?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(viewModelStore, PhoneAuthenticationViewModelFactory())[PhoneAuthenticationViewModel::class.java]
        binding = FragmentPhoneAutenticationBinding.inflate(inflater, container, false)

        binding.buttonSendCode.setOnClickListener {
            val phoneNumber = binding.textInputEditTextPhoneNumber.text.toString()

            if (phoneNumber.isNotBlank())
                sendCode(phoneNumber)
            else
                showToast("번호 구리다!") // TODO change here. ui update.
        }

        binding.buttonComplete.setOnClickListener {
            val code = binding.textInputEditTextCode.text.toString()

            if (code.isNotBlank())
                verifyCode(code)
            else
                showToast("번호 xxx!") // TODO change here. ui update.
        }

        return binding.root
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            showToast("VVVVVV???: ${phoneAuthCredential.smsCode}")
            phoneAuthCredential.smsCode?.let { smsCode ->
                viewModel.smsCode = smsCode
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Timber.e(e)
            showToast("인증실패. 콜백내부.") // TODO set error failed message.
        }

        override fun onCodeSent(
            verificationId: String,
            forceResendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, forceResendingToken)
            //showToast(getString(R.string.code_sent))
            viewModel.forceResendingToken = forceResendingToken
        }
    }

    private fun sendCode(phoneNumber: String) {
        try {
            viewModel.firebaseAuth.useAppLanguage()

            val builder = PhoneAuthOptions.newBuilder(viewModel.firebaseAuth)
                .setPhoneNumber(phoneNumber.toE164Format(requireContext())) // TODO test.
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)

            viewModel.forceResendingToken?.let {
                builder.setForceResendingToken(it)
            }

            val phoneAuthOptions = builder.build()
            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Invalid phone number.")
            showToast(getString(R.string.invalid_phone_number))
        }
    }

    private fun verifyCode(code: String) {
        if (viewModel.smsCode == code) {
            // TODO 인증성공
            showToast("인증성공 in verifyCode: smsCode:${viewModel.smsCode},, code: $code")
        } else {
            // 인증실패
            showToast("인증실패.. in verifyCode: smsCode:${viewModel.smsCode},, code: $code")
        }
    }

    /** Phone number. */
    private fun String.toE164Format(context: Context): String {
        val telephonyManager = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
        val countryCode = telephonyManager.networkCountryIso.toUpperCase(Locale.ROOT)

        return PhoneNumberUtils.formatNumberToE164(
            this,
            countryCode
        )
    }
}