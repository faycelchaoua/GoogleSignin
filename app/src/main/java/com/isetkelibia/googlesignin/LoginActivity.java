package com.isetkelibia.googlesignin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100; // google sign in request code

    private GoogleSignInClient mGoogleSignInClient; // google sign in client
    private Button customSignInButton;

    private TextView userDetailLabel;
    private ImageView userProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();
        configureGoogleSignIn();
    }

    /**
     * find all views and implement click event over default sign in button
     */
    private void findViews() {

        customSignInButton = findViewById(R.id.custom_sign_in_button);

        userDetailLabel = findViewById(R.id.user_details_label);
        userProfileImageView = findViewById(R.id.user_profile_image_view);

        customSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSignInSignOut();
            }
        });
    }

    /**
     * configure google sign in
     */
    private void configureGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // request email id
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // SILENT SIGN IN
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // Update the UI if user has already sign in with the google for this app
        getProfileInformation(account);
    }

    /**
     * method to do Sign In or Sign Out on the basis of account exist or not
     */
    private void doSignInSignOut() {

        // get the last sign in account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        // if account doesn't exist do login else do sign out
        if (account == null)
            doGoogleSignIn();
        else
            doGoogleSignOut();
    }

    /**
     * do google sign in
     */
    private void doGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN); // pass the declared request code here
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            // Method to handle google sign in result
            handleSignInResult(task);
        }
        /*if (requestCode == RC_MAIN && resultCode == 200) {
            RC_SIGN_OUT = 200;
            doSignInSignOut();
        }*/
    }

    /**
     * Method to handle google sign in result
     *
     * @param completedTask from google onActivityResult
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            getProfileInformation(account);

            //show toast
            Toast.makeText(this, R.string.message_google_signin,
                    Toast.LENGTH_SHORT).show();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());

            // Show toast
            Toast.makeText(this, "Failed to do Sign In : " + e.getStatusCode(),
                    Toast.LENGTH_SHORT).show();

            // Update Ui for this
            getProfileInformation(null);
        }
    }

    /**
     * Method to fetch user profile information from GoogleSignInAccount
     *
     * @param acct googleSignInAccount
     */
    private void getProfileInformation(GoogleSignInAccount acct) {
        // If account is not null fetch the information
        if (acct != null) {

            // User display name
            String personName = acct.getDisplayName();

            // User first name
            String personGivenName = acct.getGivenName();

            // User last name
            String personFamilyName = acct.getFamilyName();

            // User email id
            String personEmail = acct.getEmail();

            // User unique id
            String personId = acct.getId();

            // User profile pic
            Uri personPhoto = acct.getPhotoUrl();

            Bundle user = new Bundle();
            user.putString("personId", personId);
            user.putString("personName", personName);
            user.putString("personEmail", personEmail);
            if (personPhoto != null) {
                user.putString("personPhoto", personPhoto.toString());
            }

            // Show the user details
            String userDetail = getString(R.string.user_id) + " : " + personId + "\n"
                    + getString(R.string.user_display_name) + " : " + personName +
                    "\n" + getString(R.string.user_full_name) + " : " + personGivenName + " " +
                    personFamilyName + "\n" + getString(R.string.user_email) + " : " + personEmail;
            userDetailLabel.setText(userDetail);

            // Show the user profile pic
            Picasso.get()
                    .load(personPhoto)
                    .fit()
                    .error(R.mipmap.ic_launcher)
                    .into(userProfileImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, getString(R.string.message_picasso_success));
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.message_picasso_error) + e.toString(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            // Change the text of Custom Sign in button to sign out
            customSignInButton.setText(getString(R.string.sign_out));

            // Show the label and image view
            userDetailLabel.setVisibility(View.VISIBLE);
            userProfileImageView.setVisibility(View.VISIBLE);

        } else {

            // If account is null change the text back to Sign In and hide the label and image view
            customSignInButton.setText(getString(R.string.sign_in));
            userDetailLabel.setVisibility(View.GONE);
            userProfileImageView.setVisibility(View.GONE);
        }
    }

    /**
     * Method to do google sign out
     * This code clears which account is connected to the app.
     * To sign in again, the user must choose their account again.
     */
    private void doGoogleSignOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(LoginActivity.this, R.string.message_google_signout,
                                Toast.LENGTH_SHORT).show();
                        revokeAccess();
                    }
                });
    }

    /**
     * DISCONNECT ACCOUNTS
     * method to revoke access from this app
     * call this method after successful sign out
     * <p>
     * It is highly recommended that you provide users that signed in with Google the ability
     * to disconnect their Google account from your app. If the user deletes their account,
     * you must delete the information that your app obtained from the Google APIs
     */
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(LoginActivity.this, R.string.message_google_revoke,
                                Toast.LENGTH_SHORT).show();
                        getProfileInformation(null);
                    }
                });
    }

}