package com.example.storyhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.api.UserApiService;
import com.example.storyhub.models.UpdateProfileRequest;
import com.example.storyhub.models.UserResponse;
import com.example.storyhub.utils.TokenManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    ImageView imgProfile, btnBack;
    TextView btnUbahFoto, txtNamaCount, txtUsernameCount, txtBioCount;
    EditText etNama, etUsername, etBio;
    Button btnPerbarui;

    Uri selectedImageUri = null;
    TokenManager tokenManager;
    UserApiService userService;

    final int NAMA_MAX = 40;
    final int USERNAME_MAX = 20;
    final int BIO_MAX = 80;

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).circleCrop().into(imgProfile);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        tokenManager = new TokenManager(this);
        userService = RetrofitClient.getAuthInstance(tokenManager).create(UserApiService.class);

        initViews();
        loadCurrentProfile();
    }

    private void initViews() {
        imgProfile = findViewById(R.id.imgProfile);
        btnBack = findViewById(R.id.btnBack);
        btnUbahFoto = findViewById(R.id.btnUbahFoto);
        etNama = findViewById(R.id.etNama);
        etUsername = findViewById(R.id.etUsername);
        etBio = findViewById(R.id.etBio);
        txtNamaCount = findViewById(R.id.txtNamaCount);
        txtUsernameCount = findViewById(R.id.txtUsernameCount);
        txtBioCount = findViewById(R.id.txtBioCount);
        btnPerbarui = findViewById(R.id.btnPerbarui);

        btnBack.setOnClickListener(v -> finish());

        btnUbahFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        setupCharCounter(etNama, txtNamaCount, NAMA_MAX, "nama");
        setupCharCounter(etUsername, txtUsernameCount, USERNAME_MAX, "username");
        setupCharCounter(etBio, txtBioCount, BIO_MAX, "bio");

        btnPerbarui.setOnClickListener(v -> updateProfile());
    }

    private void setupCharCounter(EditText et, TextView counter, int max, String fieldName) {
        counter.setText("0/" + max);
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int len = s.length();
                counter.setText(len + "/" + max);
                if (len > max) {
                    counter.setTextColor(0xFFE53935);
                    et.removeTextChangedListener(this);
                    et.setText(s.subSequence(0, max));
                    et.setSelection(max);
                    et.addTextChangedListener(this);
                    Toast.makeText(EditProfileActivity.this,
                            fieldName.substring(0,1).toUpperCase() + fieldName.substring(1)
                                    + " tidak boleh lebih dari " + max + " huruf",
                            Toast.LENGTH_SHORT).show();
                } else {
                    counter.setTextColor(0xFF888888);
                }
            }
        });
    }

    private void loadCurrentProfile() {
        userService.getProfile().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().user != null) {
                    var user = response.body().user;
                    etNama.setText(user.name != null ? user.name : "");
                    etUsername.setText(user.username != null ? user.username : "");
                    etBio.setText(user.bio != null ? user.bio : "");

                    Glide.with(EditProfileActivity.this)
                            .load(RetrofitClient.IMAGE_AUTH_URL + user.profileImg)
                            .placeholder(R.drawable.storyhub_logo)
                            .circleCrop()
                            .into(imgProfile);
                }
            }
            @Override public void onFailure(Call<UserResponse> call, Throwable t) {}
        });
    }

    private void updateProfile() {
        String nama = etNama.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (nama.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Nama dan username tidak boleh kosong.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.contains(" ")) {
            Toast.makeText(this, "Pastikan username-mu tanpa spasi.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadWithImage(nama, username, bio);
        } else {
            uploadWithoutImage(nama, username, bio);
        }
    }

    private void uploadWithImage(String nama, String username, String bio) {
        try {
            InputStream is = getContentResolver().openInputStream(selectedImageUri);
            String fileName = "profile_" + tokenManager.getUserId() + ".jpg";
            File tempFile = new File(getCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            fos.close();
            is.close();

            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part imgPart = MultipartBody.Part.createFormData("profile_img", fileName, reqFile);
            RequestBody namaPart = RequestBody.create(MediaType.parse("text/plain"), nama);
            RequestBody usernamePart = RequestBody.create(MediaType.parse("text/plain"), username);
            RequestBody bioPart = RequestBody.create(MediaType.parse("text/plain"), bio);

            userService.updateProfileWithImage(namaPart, usernamePart, bioPart, imgPart)
                    .enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                            handleUpdateResponse(response);
                        }
                        @Override public void onFailure(Call<UserResponse> call, Throwable t) {
                            Toast.makeText(EditProfileActivity.this, "Gagal update.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Gagal proses gambar.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadWithoutImage(String nama, String username, String bio) {
        userService.updateProfile(new UpdateProfileRequest(nama, username, bio))
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        handleUpdateResponse(response);
                    }
                    @Override public void onFailure(Call<UserResponse> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "Gagal update.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleUpdateResponse(Response<UserResponse> response) {
        if (response.isSuccessful()) {
            Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
            finish();
        } else if (response.code() == 409) {
            Toast.makeText(this, "Username sudah dipakai.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal memperbarui profil.", Toast.LENGTH_SHORT).show();
        }
    }
}