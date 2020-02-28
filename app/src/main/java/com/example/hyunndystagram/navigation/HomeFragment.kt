package com.example.hyunndystagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hyunndystagram.R
import com.example.hyunndystagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

/*
//--------------------------------------------------------------------------------------------------
작성자: HYEONJIY
작성일: 2020.02.29
클래스명: HomeFragment
설명: action_home 버튼을 눌렀을 때 사진-글이 뜨는 Fragment
//--------------------------------------------------------------------------------------------------
 */

class HomeFragment : Fragment() {

    // Firebase DB
    var firestore : FirebaseFirestore? = null
    var currentUseruid : String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firestore = FirebaseFirestore.getInstance()
        currentUseruid = FirebaseAuth.getInstance().currentUser?.uid

        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_home, container, false)
        // 어댑터(아이템 뷰를 생성)
        view.home_recyclerView.adapter = DetailViewRecyclerViewAdapter()
        // 레이아웃 매니저
        view.home_recyclerView.layoutManager = LinearLayoutManager(activity)

        return view
    }

    // 어댑터 중첩 클래스
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        // AddPhotoActivity -> FirebaseDB로 저장된 contentDTO를 가져오는 배열.
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        // AddPhotoActivity -> FirebaseDB로 저장된 uid를 가져오는 배열.
        var contentUidList : ArrayList<String> = arrayListOf()

        init {

            // timestamp순으로 images 폴더에 저장되어있는 가져온다.
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                contentDTOs.clear()
                contentUidList.clear()

                // AddPhotoActivity에서 documents에 contentDTO를 저장했음.
                for(snapshot in querySnapshot!!.documents) {

                    // ContentDTO(Data To Object)
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        // 어댑터에서 생성된 아이템뷰들을 저장하는 뷰 홀더 생성.
        inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view) {}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
           var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)

           return CustomViewHolder(view)
        }

        // 생성된 데이터는 이미 뷰 홀더가 생성되었을 경우 뷰 홀더를 재생성하지 않고 그냥 bind된다.
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            // 뷰 홀더에 어댑터 생성 시 받아온 ContentDTO의 정보들을 넣어준다!
            // 계정 이메일
            viewholder.home_item_profile_text.text = contentDTOs!![position].userEmail

            // 계정 프로필
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.home_item_profile_image)

            // 올린 이미지
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.home_item_image_content)

            // 올린 설명
            viewholder.home_item_comment_explain.text = contentDTOs!![position].explain

            // 좋아요 수
            viewholder.home_item_favorite_counter.text = "Likes +" + contentDTOs[position]!!.favoriteCount

            setFavoriteBtn(viewholder, position)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        private fun setFavoriteBtn(viewHolder : View, position: Int){
            // 좋아요 버튼 클릭이벤트
            viewHolder.home_item_favorite_btn.setOnClickListener {
                favoriteEvent(position)
            }

            // 위에서 좋아요 처리가 되었다면
            if(contentDTOs!![position].favorites.contains(currentUseruid)) {
                viewHolder.home_item_favorite_btn.setImageResource(R.drawable.ic_favorite)
            }
            // 위에서 좋아요 처리가 취소되었다면
            else {
                viewHolder.home_item_favorite_btn.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        private fun favoriteEvent(position: Int){

            // FirebaseDB의 images폴더에 있는 파일들의 document중 선택된 항목의 uid와 일치하는 document를 가져와!
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])

            firestore?.runTransaction { transaction ->

                // firestoreDB에서 위에서 생성한 참조에 해당하는 오브젝트를 가져왔습니다.
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                // 만약 내가 선택한 게시물에 이미 나의 좋아요가 들어있다면? -> 좋아요 취소
                if(contentDTO!!.favorites.containsKey(currentUseruid)){
                    contentDTO?.favoriteCount =  contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(currentUseruid)
                }
                //만약 내가 선택한 게시물에 이미 나의 좋아요가 들어없다면? -> 좋아요!!
                else {
                    contentDTO?.favoriteCount =  contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[currentUseruid!!] = true
                }

                // DB의 tsDoc 참조에 contentDTO를 새로 써넣어!
                transaction.set(tsDoc, contentDTO)
            }
        }
    }
}