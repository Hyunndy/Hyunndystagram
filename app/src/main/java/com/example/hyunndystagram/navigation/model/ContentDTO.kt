package com.example.hyunndystagram.navigation.model

/*
//--------------------------------------------------------------------------------------------------
작성자: HYEONJIY
작성일: 2020.02.29
클래스명: ContentDTO
설명: Data Transfer Object.
Data를 Object로 변환하여 Data가 포함된 객체를 한 시스템에서 다른 시스템으로 전달하는 작업을 처리하는 객체.
Database record의 data를 mapping 하기 위한 data object를 말한다.

uid란? 리눅스에서 사용하는 개념으로 사용자를 식별하는 유저 아이디로 양의 정수로 16비트까지 표현한다.
//--------------------------------------------------------------------------------------------------
 */

class ContentDTO( var explain : String? = null, //설명
                  var imageUrl : String? = null, // 이미지 주소
                  var uid : String? = null, // 유저의 uid
                  var userEmail : String ? = null, // 유저의 이메일
                  var timestamp : Long? = null, // 몇 시 몇 분에 컨텐츠를 올렸는지 알려주는
                  var favoriteCount : Int = 0, // 좋아요 몇 개
                  var favorites : MutableMap<String,Boolean> = HashMap()){ // 중복 좋아요를 방지하는 좋아하는 유저를 누른 관리
    data class  Comment( var uid : String? = null, // 유저의 uid
                         var userEmail : String? = null, // 이메일
                         var comment : String? = null, // 댓글
                         var timestamp: Long? = null) { // 언제 올렸는지
    }

}