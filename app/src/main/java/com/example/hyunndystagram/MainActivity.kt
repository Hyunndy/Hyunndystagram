package com.example.hyunndystagram

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hyunndystagram.navigation.AlarmFragment
import com.example.hyunndystagram.navigation.GridFragment
import com.example.hyunndystagram.navigation.HomeFragment
import com.example.hyunndystagram.navigation.UserFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_detail.*

/*
//--------------------------------------------------------------------------------------------------
작성자: HYEONJIY
작성일: 2020.02.29
클래스명: MainActivity
설명: 네비게이션바 리스너를 통해 각 Fragment로의 연결, 실제 내용이 들어가는 Layout이 들어있는 Class.
//--------------------------------------------------------------------------------------------------
 */


// @HYEONJIY: BottomNavigationView의 항목들이 눌렸을 때 해당 Fragment로 이동시킬 리스너를 구한다.
class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {


    //구글로그인 세션 종료를 위한..
    var googleSignInClient : GoogleSignInClient? = null

    var permission_list = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()

        when(item.itemId){
            // 홈 화면.
            R.id.action_home -> {
                var homeFragment = HomeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, homeFragment).commit()
                return true
            }
            // 검색 화면
            R.id.action_search -> {
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, gridFragment).commit()
                return true
            }
            // 갤러리로 부터 사진 가져오기
            R.id.action_add_photo -> {
                if(ContextCompat.checkSelfPermission(this, permission_list[0]) == PERMISSION_GRANTED) {
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                }
                return true
            }
            // 좋아요 알림 화면
            R.id.action_favorite_alarm -> {
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, alarmFragment).commit()
                return true
            }
            // "본인의" 계정 상세 정보 화면
            R.id.action_account -> {
                var userFragment = UserFragment()

                var bundle = Bundle()
                bundle.putString("destinationUid", FirebaseAuth.getInstance().currentUser?.uid)
                bundle.putString("userEmail", FirebaseAuth.getInstance().currentUser?.email)
                userFragment.arguments = bundle

                supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 리스너 세팅.
        bottom_navigation.setOnNavigationItemSelectedListener(this)

        // 권한 요청
        ActivityCompat.requestPermissions(this, permission_list, 1)

        // 기본 화면 설정
        bottom_navigation.selectedItemId = R.id.action_home

        setToolbarDefault()

        // 구글
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    // @HYEONJIY: 툴바 디폴트 ui 설정.
    private fun setToolbarDefault(){
        toolbar_username.visibility = View.GONE
        toolbar_btn_back.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // @HYEONJIY: 계정 페이지에서 프로필 사진 바꾸고 다시 홈으로 돌아오면 프로필사진 초기화해야하니까 DB에 userProfileImages를 넣어버린다..
        if(requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask {

                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener {
                var map = HashMap<String, Any>()
                map["image"] = it.toString()

                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        googleSignInClient?.signOut()
    }
}
