package com.example.hyunndystagram

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

/*
//--------------------------------------------------------------------------------------------------
작성자: HYEONJIY
작성일: 2020.02.29
클래스명: LoginActivity
설명: 로그인 화면을 관리하는 액티비티 클래스.
//--------------------------------------------------------------------------------------------------
 */

class LoginActivity : AppCompatActivity() {

    // Firebase 인증 안드로이드 라이브러리.
    var auth : FirebaseAuth? = null

    // 구글 로그인 관리 라이브러리
    var googleSignInClient : GoogleSignInClient? = null
    // 구글 로그인 코드
    val GOOGLE_LOGIN_CODE = 9001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // GoogleSignInApi을 위한 옵션. google-service.json안에 있는 default_web_client_id에서 idToken, emain을 요청한다.
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setButtonListeners()
    }

    private fun setButtonListeners()
    {
        email_login_button.setOnClickListener {
            emailLogin()
        }

        google_sign_in_button.setOnClickListener {
            googleLogin()
        }

    }

    private fun emailLogin(){
        if(email_edittext.text.isNullOrEmpty() || password_edittext.text.isNullOrEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show()
            return
        } else{
            createAndLoginEmail()
        }
    }

    //{{ @HYEONJIY: 신규 사용자 가입(sign in = login) or 기존 사용자 로그인
    private fun createAndLoginEmail() {

        // 이메일 주소와 비밀번호를 가져와 유효성을 검사하는 라이브러리 함수.
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener {task ->

            // 신규 가입 검사가 통과한 경우.
            if(task.isSuccessful){
                moveMainPage(task.result?.user)
            }
            // 입력한 이메일, 패스워드가 이상할 때 메세지 출력.
            else if(task.exception?.message.isNullOrEmpty()){
                Toast.makeText(this, "회원가입 실패!", Toast.LENGTH_LONG).show()
            }
            // 이미 계정이 있는 경우
            else {
                signInEmail()
            }
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 구글에서 로그인
    private fun googleLogin() {

        // 로그인 할 Google 계정을 선택하라는 메세지가 표시된다.
        var signInIntent = googleSignInClient?.signInIntent

        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }
    //}} @HYEONHIY

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 구글로부터 인증 성공이 도착하면, 파이어베이스 로그인 시도.
        if(requestCode == GOOGLE_LOGIN_CODE && resultCode == Activity.RESULT_OK) {

            // API 인터페이스 for Sign in with Google
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    // 파이어베이스 로그인에 성공하면, 로그인 처리.
    private fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                moveMainPage(auth?.currentUser)
            }
            else {
                Toast.makeText(this, "로그인 실패!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun moveMainPage(user:FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    //{{ @HYEONJIY: 기존 사용자 로그인
    private fun signInEmail() {

        // 이메일 주소와 비밀번호의 유효성을 검사하는 함수
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                moveMainPage(task.result?.user)
            }
            else {
                Toast.makeText(this, "로그인 실패!", Toast.LENGTH_LONG).show()
            }
        }
    }
    //}} @HYEONJIY

    override fun onStart() {
        super.onStart()

        moveMainPage(auth?.currentUser)
    }
}
