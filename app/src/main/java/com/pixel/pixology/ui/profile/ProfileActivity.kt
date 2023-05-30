package com.pixel.pixology.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.StorageReference
import com.pixel.pixology.databinding.ActivityProfileBinding
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var storageProfilePicRef: StorageReference

    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //        databaseReference = FirebaseDatabase.getInstance().getReference().child("Employees");
        val profileRef: StorageReference = storageProfilePicRef.child("users/Profile/profile.jpg")
        profileRef.downloadUrl.addOnSuccessListener(OnSuccessListener<Uri?> { uri ->
            Picasso.get().load(uri).into(binding.profileImage)
        })



    }
}