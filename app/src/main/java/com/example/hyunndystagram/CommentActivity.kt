package com.example.hyunndystagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hyunndystagram.navigation.HomeFragment
import com.example.hyunndystagram.navigation.model.AlarmDTO
import com.example.hyunndystagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

/*
//--------------------------------------------------------------------------------------------------
작성자: HYEONJIY
작성일: 2020.03.02
클래스명: CommentActivity
설명:
- HomeFragment 에서 게시글의 댓글 버튼을 누르면 실행되는 Activity.
- HomeFragmnet에서 Intent로 보낸 uid를 이용한다. (댓글을 단 사람의 uid)
- ContentDTO의 DATA CLASS인 Comment를 이용한다.
//--------------------------------------------------------------------------------------------------
 */

class CommentActivity : AppCompatActivity() {

    var contentUid: String? = null
    var destinationUid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        comment_recyclerView.adapter = commentRecyclerViewAdapter()
        comment_recyclerView.layoutManager = LinearLayoutManager(this)

        comment_btn_send?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userEmail = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()
            comment.timestamp = System.currentTimeMillis()


            // FirebaseDB의 images폴더의 document가 contentUid인곳에 comments 폴더를 생성하여 comment를 넣으면 comment가 쌓이게된다.
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").document().set(comment)

            commentAlarm(destinationUid!!, comment_edit_message.text.toString())

            comment_edit_message.setText("")
        }
    }

    inner class commentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            // FireBaseDB에 있는걸 읽어오기
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                comments.clear() // 중복 방지

                if(querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot){
                    // 캐스팅해서 넣어준다.
                    comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view:View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.commentviewitem_comment_text.text = comments[position].comment
            view.commentviewitem_profile_text.text = comments[position].userEmail

            // @HYEONJIY: 프로필 사진 가져오기.
            FirebaseFirestore.getInstance().collection("profileImages").document(comments[position].uid!!).get().addOnCompleteListener {task ->
                if(task.isSuccessful) {
                    var url = task.result!!["image"]
                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_profile_image)
                }
            }
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }

    private fun commentAlarm(destinationUid : String, message : String){
        var alarmDTO = AlarmDTO()

        alarmDTO.destinationUid = destinationUid
        alarmDTO.userEmail = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.kind = 1
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }
}
