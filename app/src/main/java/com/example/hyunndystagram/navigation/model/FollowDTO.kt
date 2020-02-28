package com.example.hyunndystagram.navigation.model

/*
//--------------------------------------------------------------------------------------------------
작성자: HYEONJIY
작성일: 2020.02.29
클래스명: FollowDTO
설명: Data Transfer Object.
데이터 클래스.
//--------------------------------------------------------------------------------------------------
 */

data class FollowDTO (
    var followerCount : Int = 0,
    var followers : MutableMap<String, Boolean> = HashMap(),

    var followingCount : Int = 0,
    var followings : MutableMap<String, Boolean> = HashMap()
)