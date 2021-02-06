package com.grand.duke.elliot.jjabkaotalk.sign_in

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.AuthCodeClient
import com.kakao.sdk.auth.rx
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import io.reactivex.schedulers.Schedulers
import org.koin.java.KoinJavaComponent.get
import retrofit2.Retrofit
import timber.log.Timber

class SignInHelper(private val signInActivity: SignInActivity) {

    private var onSignInListener: OnSignInListener? = null

    fun setOnSignInListener(onSignInListener: OnSignInListener) {
        this.onSignInListener = onSignInListener
    }

    @Suppress("SameParameterValue")
    private fun getString(resId: Int) = signInActivity.getString(resId)

    interface OnSignInListener {
        fun onCanceled()
        fun onGoogleSignIn(result: Boolean)
        @Suppress("SpellCheckingInspection")
        fun onKakaoLogin(result: Boolean)
        fun onFacebookLogin(result: Boolean)
        @Suppress("SpellCheckingInspection")
        fun onNaverLogin(result: Boolean, lastErrorCode: String? = null, lastErrorDesc: String? = null)
    }

    fun loginWithFacebook(callbackManager: CallbackManager) {
        LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK
        LoginManager.getInstance().logInWithReadPermissions(signInActivity, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    firebaseAuthWithFacebook(result)
                }

                override fun onCancel() {
                    Timber.e("Facebook sign in canceled")
                    onSignInListener?.onCanceled()
                }

                override fun onError(error: FacebookException?) {
                    onSignInListener?.onFacebookLogin(false)
                    error?.printStackTrace()
                }
            })
    }

    fun firebaseAuthWithFacebook(result: LoginResult?) {
        result?.accessToken?.token?.let {
            val credential = FacebookAuthProvider.getCredential(it)
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Facebook login successful.")
                    onSignInListener?.onFacebookLogin(true)
                } else {
                    Timber.e(task.exception, "Facebook login failed.")
                    onSignInListener?.onFacebookLogin(false)
                }
            }
        } ?: run {
            onSignInListener?.onFacebookLogin(false)
        }
    }

    /** Google Sign-In. */
    fun signInWithGoogle() {
        try {
            val googleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            val googleSignInClient = GoogleSignIn.getClient(signInActivity, googleSignInOptions)
            val signInIntent = googleSignInClient?.signInIntent
            signInActivity.startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
        } catch (e: Exception) {
            Timber.e(e)
            onSignInListener?.onGoogleSignIn(false)
        }
    }

    fun firebaseAuthWithGoogle(googleSignInAccount: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(googleSignInAccount?.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("Google sign in successful.")
                onSignInListener?.onGoogleSignIn(true)
            } else {
                Timber.d("Google sign in failed.")
                onSignInListener?.onGoogleSignIn(false)
            }
        }
    }

    /** Kakao Login. */
    @Suppress("SpellCheckingInspection")
    @SuppressLint("CheckResult")
    fun loginWithKakao(context: Context) {
        AuthCodeClient.rx
                .authorizeWithKakaoTalk(context, REQUEST_CODE_KAKAO_LOGIN)
                .observeOn(Schedulers.io())
                .flatMap {
                    AuthApiClient.rx.issueAccessToken(it)
                }
                .subscribe({
                    getFirebaseJwt(it.accessToken)
                }) {
                    Timber.e(it)
                }

    }

    @Suppress("SpellCheckingInspection")
    @SuppressLint("CheckResult")
    private fun getFirebaseJwt(accessToken: String) {
        val retrofit = get(Retrofit::class.java)
        try {
            retrofit.create(FirebaseCustomTokenApi.FirebaseCustomTokenService::class.java)
                    .getFirebaseKakaoCustomToken(accessToken)
                    .observeOn(Schedulers.single())
                    .subscribe({
                        MainApplication.getFirebaseAuthInstance()
                                .signInWithCustomToken(it.firebase_token)
                                .addOnSuccessListener {
                                    Timber.d("Kakao login success.")
                                    onSignInListener?.onKakaoLogin(true)
                                }
                                .addOnFailureListener { exception ->
                                    Timber.e(exception, "Kakao login failed.")
                                    onSignInListener?.onKakaoLogin(false)
                                }
                    }) {
                        Timber.e(it, "Kakao login failed.")
                        onSignInListener?.onKakaoLogin(false)
                    }
        } catch (e: Exception) {
            Timber.e(e, "Kakao login failed.")
            onSignInListener?.onKakaoLogin(false)
        }
    }

    /** Naver Login. */
    @Suppress("SpellCheckingInspection")
    private object NaverApi {
        const val ClientId = "BD7oZaRXpIWz0TGGJbXY"
        const val ClientSecret = "jgxIHlqugU"
    }

    @Suppress("SpellCheckingInspection")
    fun loginWithNaver(activity: Activity) {
        val oAuthLogin = OAuthLogin.getInstance()
        oAuthLogin.init(
            activity,
            NaverApi.ClientId,
            NaverApi.ClientSecret,
            NaverApi.ClientId
        )
        oAuthLogin.startOauthLoginActivity(activity, @SuppressLint("HandlerLeak")
        object: OAuthLoginHandler() {
            override fun run(success: Boolean) {
                if (success) {
                    val accessToken: String = oAuthLogin.getAccessToken(activity)
                    val refreshToken: String = oAuthLogin.getRefreshToken(activity)
                    val expiresAt: Long = oAuthLogin.getExpiresAt(activity)
                    val tokenType: String = oAuthLogin.getTokenType(activity)
                } else {
                    val lastErrorCode: String = oAuthLogin.getLastErrorCode(activity).code
                    val lastErrorDesc: String = oAuthLogin.getLastErrorDesc(activity)
                    onSignInListener?.onNaverLogin(false, lastErrorCode, lastErrorCode)
                }
            }
        })
    }

    companion object {
        const val REQUEST_CODE_GOOGLE_SIGN_IN = 1559
        const val REQUEST_CODE_KAKAO_LOGIN = 1560
    }
}