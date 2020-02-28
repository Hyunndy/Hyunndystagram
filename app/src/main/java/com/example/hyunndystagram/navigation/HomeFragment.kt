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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class HomeFragment : Fragment() {

    // Firebase DB
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firestore = FirebaseFirestore.getInstance()

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

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }
}