package com.example.hyunndystagram.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hyunndystagram.LoginActivity
import com.example.hyunndystagram.MainActivity
import com.example.hyunndystagram.R
import com.example.hyunndystagram.navigation.model.ContentDTO
import com.example.hyunndystagram.navigation.model.FollowDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.*
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

        // 팔로우/팔로잉 UI 업데이트.
        updateFollowersAndFollowing()

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
            // 팔로우 버튼을 눌렀을 때
            view?.account_btn_follow_signout?.setOnClickListener {
                requestFollow()
            }
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

    // @HYEONJIY: 팔로우 버튼을 눌렀을 때 팔로우 할지/취소할지.
    private fun requestFollow() {

        // 1. 일단 내 계정의 팔로우 목록을 본다.
        //------------------------------------------------------------------------------------------------------------
        // Firebase DB의 users폴더에 document 중 currentUseruid있는 데이터 다 가져와!
        var tsDocFollowing = firestore?.collection("users")?.document(currentUseruid!!)
        firestore?.runTransaction{ transaction ->

            // 가져온 데이터를 FollowDTO 오브젝트로 바꾼다.
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)

            // 없다 == 나는 지금 팔로우 하고있는 사람이 없다 == 이 계정이 나의 첫번째 팔로우이다!
            if(followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followings[selectedUseruid!!] = true

                // Firebase DB에 이 내용을 넣어주고 돌아가자.
                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            // 내가 얘를 이미 팔로우 중일 때 -> 취소
            if(followDTO.followings.containsKey(selectedUseruid)) {
                followDTO?.followingCount = followDTO?.followingCount -1
                followDTO?.followings?.remove(selectedUseruid)
            }
            // 팔로잉
            else
            {
                followDTO?.followingCount = followDTO?.followingCount +1
                followDTO?.followings[selectedUseruid!!] = true
            }

            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        account_following_count
        //------------------------------------------------------------------------------------------------------------

        // 2. 상대방 계정의 팔로우 처리.
        var tsDocFollower = firestore?.collection("users")?.document(selectedUseruid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)

            // 없다 == 이 계정을 팔로우하는 사람이 없다 == 그렇다면 방금 Follow눌러준 사람이 처음이다!
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUseruid!!] = true

                transaction.set(tsDocFollower, followDTO!!)

                // UI처리
               // fragmentView?.account_follower_count?.text = 1.toString()

                return@runTransaction
            }

            // 내가 지금 Follow버튼 누른사람을 이미 팔로워 목록에 가지고있었을 경우.. 그는 팔로우를 취소한것
            if(followDTO!!.followers.containsKey(currentUseruid)) {
                followDTO!!.followerCount = followDTO!!.followerCount -1
                followDTO!!.followers?.remove(currentUseruid)
            }
            // 팔로우 추가!
            else {
                followDTO!!.followerCount = followDTO!!.followerCount +1
                followDTO!!.followers[currentUseruid!!] = true
            }

            // UI처리
           // fragmentView?.account_follower_count?.text = followDTO!!.followerCount.toString()

            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }

    fun updateFollowersAndFollowing(){
        // 내페이지를 클릭했을땐 ㄴ ㅐ 페이지
        firestore?.collection("users")!!.document(selectedUseruid!!).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener

            // 캐스팅
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null){
                fragmentView?.account_following_count?.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount != null) {
                fragmentView?.account_follower_count?.text = followDTO?.followerCount?.toString()

                //내가 목록에 있는 경우
                if(followDTO?.followers?.containsKey(currentUseruid!!)) {
                    fragmentView?.account_btn_follow_signout?.text = "CANCEL FOLLOW"
                } else {
                    if(selectedUseruid != currentUseruid){
                        fragmentView?.account_btn_follow_signout?.text = "FOLLOW"
                    }
                }
            }
        }
    }
}

