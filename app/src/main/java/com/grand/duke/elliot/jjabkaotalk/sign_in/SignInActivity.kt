package com.grand.duke.elliot.jjabkaotalk.sign_in

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseActivity
import com.grand.duke.elliot.jjabkaotalk.databinding.ActivitySignInBinding
import com.grand.duke.elliot.jjabkaotalk.sign_in.SignInHelper.Companion.REQUEST_CODE_GOOGLE_SIGN_IN
import timber.log.Timber

class SignInActivity: BaseActivity(), SignInHelper.OnSignInListener {

    private lateinit var signInHelper: SignInHelper
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        signInHelper = SignInHelper(this)
        signInHelper.setOnSignInListener(this)

        /** Google Sign-In */
        binding.imageButtonGoogleSignIn.setOnClickListener {
            signInHelper.signInWithGoogle()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_GOOGLE_SIGN_IN -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    signInHelper.firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Timber.e(e, getString(R.string.failed_to_google_sign_in))
                    showToast("${getString(R.string.failed_to_google_sign_in)}: ${e.message}")

                    val intent = Intent()
                    setResult(RESULT_CANCELED, intent)
                    finish()
                }
            }
        }
    }

    override fun onAfterGoogleSignIn(result: Boolean) {
        val intent = Intent()

        if (result) {
            setResult(RESULT_OK, intent)
            finish()
        } else {
            setResult(RESULT_CANCELED, intent)
            finish()
        }
    }

    override fun onBeforeGoogleSignIn() {}
}