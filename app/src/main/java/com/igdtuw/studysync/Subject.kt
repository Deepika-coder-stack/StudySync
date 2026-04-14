package com.igdtuw.studysync

data class Subject(
    var name: String,
    var topics: MutableList<Topic> = mutableListOf()
)