package com.example.desafio03_biblioteca

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object AyudanteFirebase {

    private const val URL_BD = "https://desafio03-biblioteca-default-rtdb.firebaseio.com"

    fun bd(): FirebaseDatabase =
        if (URL_BD.isEmpty()) FirebaseDatabase.getInstance()
        else FirebaseDatabase.getInstance(URL_BD)

    fun usuario(): FirebaseUser? = FirebaseAuth.getInstance().currentUser

    fun libros(): DatabaseReference =
        bd().getReference("usuarios").child(usuario()!!.uid).child("libros")

    fun metas(): DatabaseReference =
        bd().getReference("usuarios").child(usuario()!!.uid).child("metas")
}