package com.example.hyunndystagram

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.hyunndystagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

/*
//--------------------------------------------------------------------------------------------------
작성자: HYEONJIY
작성일: 2020.02.29
클래스명: AddPhotoActivity
설명: 3번째 네비게이션 바 - add photo를 누르면 갤러리에서 사진을 가져와 id, desc, db 등록하는 Activity.
//--------------------------------------------------------------------------------------------------
 */

class AddPhotoActivity : AppCompatActivity() {

    // 파이어베이스 인증 라이브러리.
    var auth : FirebaseAuth? = null
    // 파이어베이스 DB
    var firestore : FirebaseFirestore? = null
    // 데이터를 저장하는 버킷.
    var storage : FirebaseStorage? = null

    // 사진을 저장할 URL
    var photoUri : Uri? = null
    val PICK_IMAGE_FROM_ALBUM = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        initFirebase()

        openAlbum()

        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    private fun initFirebase() {

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    private fun openAlbum(){
        // Intent에 type을 지정해서 갤러리를 열게 한다.
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"

        // 갤러리 Activity로 이동한다.
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 갤러리에서 사진을 선택했다면
        if(requestCode == PICK_IMAGE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            photoUri = data?.data
            // ImageView에 사진을 띄운다.
            addphoto_image.setImageURI(photoUri)
        } else {
            finish()
        }
    }

    //{{ @HYEONJIY 선택된 사진을 store 버킷에 넣고, 성공한다면 contentDTO에 넣어서 Firebase DB에 넣어주는 함수. 제일 중요함
    private fun contentUpload(){
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        var imageFilename = "IMAGE" + timestamp + "_.png"

        // Firebasestorage 참조 변수를 선언해서, 파일이름을 포함하여 파일의 전체 경로를 가리키는 참조를 생성한다.
        var storageRef = storage?.reference?.child("images")?.child(imageFilename)

        // putfile() = 카메라의 사진, 동영상과 같은 기기의 로컬 파일을 업로드 한다.
        // File을 취하고 UploadTask를 반환하며 이 반환 객체를 사용하여 업로드를 관리하고 상태를 모니터링할 수 있다.
        var uploadTask = storageRef?.putFile(photoUri!!)

        // 업로드가 성공적이면 Task를 통해 다운로드 URL 가져오기
        var urlTask = uploadTask?.continueWithTask{ task ->
            if(!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef?.downloadUrl
        }?.addOnSuccessListener {

            // 이미지의 url을 가져온다.
            storageRef?.downloadUrl?.addOnSuccessListener { uri ->

                var contentDTO = ContentDTO()

                // 이미지의 다운로드 url을 담는다.
                contentDTO.imageUrl = uri.toString()

                // 유저의 uid를 넣는다.
                contentDTO.uid = auth?.currentUser?.uid

                // 유저 email을 넣는다.
                contentDTO.userEmail = auth?.currentUser?.email

                // 입력한 사진 설명을 넣는다.
                contentDTO.explain = addphoto_explain.text.toString()

                // 타임스탬프를 넣는다.
                contentDTO.timestamp = System.currentTimeMillis()

                // Firebase의 실제 DB(FirebaseFirestore)의 collection("저장폴더")에 ContentDTO(Data Transfer Object)를 넣어준다.
                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()
            }
        }
    }
}
