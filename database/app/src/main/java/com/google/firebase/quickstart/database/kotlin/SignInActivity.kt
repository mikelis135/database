package com.google.firebase.quickstart.database.kotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : BaseActivity(), View.OnClickListener {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val rcsignin = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        // Click listeners
        buttonSignIn.setOnClickListener(this)
        buttonSignUp.setOnClickListener(this)
    }

    public override fun onStart() {
        super.onStart()

        // Check auth on Activity start
        auth.currentUser?.let {
            onAuthSuccess(it)
        }
    }

    private fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                rcsignin)
        // [END auth_fui_create_intent]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == rcsignin) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private fun signIn() {
        Log.d(TAG, "signIn")
        if (!validateForm()) {
            return
        }

        showProgressDialog()
        val email = fieldEmail.text.toString()
        val password = fieldPassword.text.toString()

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signIn:onComplete:" + task.isSuccessful)
                    hideProgressDialog()

                    if (task.isSuccessful) {
                        onAuthSuccess(task.result?.user!!)
                    } else {
                        Toast.makeText(baseContext, "Sign In Failed",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun signUp() {
        Log.d(TAG, "signUp")
        if (!validateForm()) {
            return
        }

        showProgressDialog()
        val email = fieldEmail.text.toString()
        val password = fieldPassword.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "createUser:onComplete:" + task.isSuccessful)
                    hideProgressDialog()

                    if (task.isSuccessful) {
                        onAuthSuccess(task.result?.user!!)
                    } else {
                        Toast.makeText(baseContext, "Sign Up Failed",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun onAuthSuccess(user: FirebaseUser?) {
        val username = usernameFromEmail(user?.email+"")

        // Write new user
        writeNewUser(user?.uid+"", username, user?.email)

        // Go to MainActivity
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun usernameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            email
        }
    }

    private fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(fieldEmail.text.toString())) {
            fieldEmail.error = "Required"
            result = false
        } else {
            fieldEmail.error = null
        }

        if (TextUtils.isEmpty(fieldPassword.text.toString())) {
            fieldPassword.error = "Required"
            result = false
        } else {
            fieldPassword.error = null
        }

        return result
    }

    // [START basic_write]
    private fun writeNewUser(userId: String, name: String, email: String?) {
        val user = User(name, email)
        database.child("users").child(userId).setValue(user)
    }
    // [END basic_write]

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.buttonSignIn) {
//            signIn()
            createSignInIntent()
        } else if (i == R.id.buttonSignUp) {
            signUp()
        }
    }

    private fun twitterSignIn(){
        val provider = OAuthProvider.newBuilder("twitter.com")

        val pendingResultTask: Task<AuthResult>? = auth.pendingAuthResult
        if (pendingResultTask != null) { // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            OnSuccessListener<AuthResult?> {
                                Toast.makeText(this, "signed in", Toast.LENGTH_LONG).show()
                            })
                    .addOnFailureListener {
                        // Handle failure.
                    }
        }

        auth.startActivityForSignInWithProvider( /* activity= */this, provider.build())
                .addOnSuccessListener {
                    Toast.makeText(this, "signed in again" + it?.user?.displayName, Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Log.d("okh", it?.message+"")

                }

        FirebaseAuth.getInstance().currentUser?.startActivityForLinkWithProvider( /* activity= */this, provider.build())
                ?.addOnSuccessListener {
                    Toast.makeText(this, "signed in", Toast.LENGTH_LONG).show()
                }
                ?.addOnFailureListener {
                    // Handle failure.
                }

    }

    companion object {

        private const val TAG = "SignInActivity"
    }
}
