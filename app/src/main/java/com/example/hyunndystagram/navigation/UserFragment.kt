package com.example.hyunndystagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hyunndystagram.LoginActivity
import com.example.hyunndystagram.MainActivity
import com.example.hyunndystagram.R
import com.example.hyunndystagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

/*
//--------------------------------------------------------------------------------------------------
작성자: HYEONJIY
작성일: 2020.02.29
클래스명: UserFragment
설명: 계정 상세정보 Fragment.
//--------------------------------------------------------------------------------------------------
 */

class UserFragment : Fragment() {

    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var selectedUseruid: String? = null
    var fragmentView: View? = null

    var currentUseruid: String? = null


    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)

        selectedUseruid = arguments?.getString("destinationUid")
        currentUseruid = auth?.currentUser?.uid
        checkAccountMaster(fragmentView)

        setUI(fragmentView)

        fragmentView?.account_recyclerView?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerView?.layoutManager = GridLayoutManager(activity!!, 3)

        return fragmentView
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        init {

            // Firebas DB에서 images 폴더에 uid가 selectedUseruid인 게시글들 다 가져와!
            firestore?.collection("images")?.whereEqualTo("uid", selectedUseruid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    // 없으면 리턴해
                    if (querySnapshot == null) return@addSnapshotListener

                    // 있으면 거기있는 내용 싹 다 contentDTO에 넣어!
                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }

                    // 게시글의 수 만큼 images 수가 있을 테니까 contentDTO의 수가 올린 게시글의 수.
                    fragmentView?.account_post_count?.text = contentDTOs.size.toString()

                    notifyDataSetChanged()
                }
            }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {}

        // 이미지 크기 3분의 1해서 아이템 뷰 하나 생성.
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            var width = resources.displayMetrics.widthPixels / 3
            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayout.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            // 외부 url로부터 이미지가져와서 이미지 세팅해주기!
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }

    // @HYEONJIY: 내 계정인지 다른 사람 계정 상세페이지인지 구분
    private fun checkAccountMaster(view: View?) {

        // 만약 선택한 계정과 내 계정이 같다면
        if (selectedUseruid == currentUseruid) {

            // LOGOUT 버튼 클릭 시 로그아웃 화면으로 넘어간다.
            view?.account_btn_follow_signout?.text = getString(R.string.signout)
            view?.account_btn_follow_signout?.setOnClickListener {
                // 이 Fragment가 실행되는 Activity를 강제 종료.
                activity?.finish()
                // 로그인 페이지로 돌아간다.
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }

            // 내 계정이라면 계정 프로필 클릭 시 프로필 이미지 선택.
            view?.account_iv_profile?.setOnClickListener {
                var photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
            }
        }
        // 만약 다른이의 계정이라면..!
        else {
            view?.account_btn_follow_signout?.text = getString(R.string.follow)

            // 팔로우 버튼을 눌렀을 때
            //view.account_btn_follow_signout.setOnClickListener {}
        }
    }

    // @HYEONJIY: UI 세팅.
    private fun setUI(view: View?){
        var mainActivity = activity as MainActivity
        mainActivity?.toolbar_username.text = arguments?.getString("userEmail")

        // 백버튼을 누른다면 홈 화면으로 돌아간다.
        mainActivity?.toolbar_btn_back?.setOnClickListener {
            mainActivity.bottom_navigation.selectedItemId = R.id.action_home
        }

        mainActivity?.toolbar_title_image?.visibility = View.GONE
        mainActivity?.toolbar_username?.visibility = View.VISIBLE
        mainActivity?.toolbar_btn_back?.visibility = View.VISIBLE

        // 프로필 이미지 가져오기.
        firestore?.collection("profileImages")?.document(selectedUseruid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener

            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).into(fragmentView?.account_iv_profile!!)
            }
        }
    }
}

