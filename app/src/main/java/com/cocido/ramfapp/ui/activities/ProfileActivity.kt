package com.cocido.ramfapp.ui.activities

import android.os.Bundle
import com.cocido.ramfapp.R

class ProfileActivity : BaseActivity() {
    
    override fun requiresAuthentication(): Boolean {
        return true
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }
}