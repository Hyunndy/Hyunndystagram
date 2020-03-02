package com.example.hyunndystagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hyunndystagram.R
import com.example.hyunndystagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)

        view.alarmfragment_recyclerview.adapter = AlarmRecyclerViewAdapter()
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity)


        return view
    }

    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()

        init {
            var currentUid = FirebaseAuth.getInstance().currentUser?.uid

            // @HYEONJIY: 나한테 온 알람만 추려내기 위해.
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid",currentUid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                alarmDTOList.clear()
                if( querySnapshot == null)  return@addSnapshotListener

                for(snapshot in querySnapshot.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }

                // 중요~!
                notifyDataSetChanged()
            }
        }

        inner class CustomVIewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CustomVIewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView

            // @HYEONJIY: 프로필 사진 가져오기.
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener {task ->
                if(task.isSuccessful) {
                    var url = task.result!!["image"]
                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_profile_image)
                }
            }

            when(alarmDTOList[position].kind){
                0 -> {
                    val str_0 = alarmDTOList[position].userEmail + getString(R.string.alarm_favorite)
                    view.commentviewitem_profile_text.text = str_0
                }
                1 -> {
                    val str_1 = alarmDTOList[position].userEmail + getString(R.string.alarm_comment) + " of " + alarmDTOList[position].message
                    view.commentviewitem_profile_text.text = str_1
                }
                2 -> {
                    val str_2 = alarmDTOList[position].userEmail + getString(R.string.alarm_follow)
                    view.commentviewitem_profile_text.text = str_2
                }
            }

            view.commentviewitem_comment_text.visibility = View.INVISIBLE
        }

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }
    }
}