package com.grand.duke.elliot.jjabkaotalk.sign_in

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseActivity
import com.grand.duke.elliot.jjabkaotalk.databinding.ActivitySignInBinding
import com.grand.duke.elliot.jjabkaotalk.sign_in.SignInHelper.Companion.REQUEST_CODE_GOOGLE_SIGN_IN
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import timber.log.Timber

class SignInActivity: BaseActivity(), SignInHelper.OnSignInListener {

    private lateinit var signInHelper: SignInHelper
    private lateinit var binding: ActivitySignInBinding

    // Facebook.
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        setDisplayHomeAsUpEnabled(binding.toolbar) {
            onBackPressed()
        }

        signInHelper = SignInHelper(this)
        signInHelper.setOnSignInListener(this)

        /** Facebook Login. */
        binding.frameLayoutFacebookLogin.setOnClickListener {
            signInHelper.loginWithFacebook(callbackManager)
        }

        /** Google Sign-In. */
        binding.frameLayoutGoogleSignIn.setOnClickListener {
            signInHelper.signInWithGoogle()
        }

        /** Kakao Login. */
        binding.frameLayoutKakaoLogin.setOnClickListener {
            signInHelper.loginWithKakao(this)
        }

        /** Naver Login. */
        binding.frameLayoutNaverLogin.setOnClickListener {
            signInHelper.loginWithNaver(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_GOOGLE_SIGN_IN -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    signInHelper.firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Timber.e(e, getString(R.string.failed_to_sign_in))
                    showToast("${getString(R.string.failed_to_sign_in)}: ${e.message}")

                    val intent = Intent()
                    setResult(RESULT_CANCELED, intent)
                    finish()
                }
            }
        }
    }

    override fun onFacebookLogin(result: Boolean) {
        finish()
    }

    override fun onGoogleSignIn(result: Boolean) {

        if (result) {

            finish()
        } else {

            // finish()
        }
    }

    override fun onKakaoLogin(result: Boolean) {
        if (result)
            finish()
    }

    override fun onNaverLogin(result: Boolean, lastErrorCode: String?, lastErrorDesc: String?) {
        finish()
    }

    override fun onCanceled() {

    }
}