package com.example.hyunndystagram.navigation.model

/*
//--------------------------------------------------------------------------------------------------
작성자: HYEONJIY
작성일: 2020.03.02
클래스명: AlarmDTO
설명: 유저에게 알림메세지가 가게 하기위한 DTO.
//--------------------------------------------------------------------------------------------------
 */


data class AlarmDTO(
    var destinationUid : String? = null, //
    var userEmail : String? = null,
    var uid : String? = null,
    var kind : Int? = null, // 메세지 종류
    var message : String ? = null,
    var timestamp : Long? = null
)