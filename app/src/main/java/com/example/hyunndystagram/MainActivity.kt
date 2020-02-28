package com.example.hyunndystagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.hyunndystagram.navigation.AlarmFragment
import com.example.hyunndystagram.navigation.GridFragment
import com.example.hyunndystagram.navigation.HomeFragment
import com.example.hyunndystagram.navigation.UserFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

// @HYEONJIY: BottomNavigationView의 항목들이 눌렸을 때 해당 Fragment로 이동시킬 리스너를 구한다.
class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
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
                return true
            }
            // 좋아요 알림 화면
            R.id.action_favorite_alarm -> {
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, alarmFragment).commit()
                return true
            }
            // 계정 상세 정보 화면
            R.id.action_account -> {
                var userFragment = UserFragment()
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
    }
}
