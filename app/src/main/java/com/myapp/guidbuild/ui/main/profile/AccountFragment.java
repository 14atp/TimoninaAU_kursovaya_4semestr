package com.myapp.guidbuild.ui.main.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.User;
import com.myapp.guidbuild.utils.AvatarHelper;
import com.myapp.guidbuild.utils.SessionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AccountFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ShapeableImageView ivAvatar;
    private TextView tvNickname, tvGuidesCount, tvFollowersCount, tvLikesCount, tvFollowingCount;
    private TextView tvChangeAvatar;
    private SessionManager sessionManager;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        sessionManager = new SessionManager(requireContext());

        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvNickname = view.findViewById(R.id.tvNickname);
        tvGuidesCount = view.findViewById(R.id.tvGuidesCount);
        tvLikesCount = view.findViewById(R.id.tvLikesCount);
        tvChangeAvatar = view.findViewById(R.id.tvChangeAvatar);
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        // обработчики для выбора аватарки
        ivAvatar.setOnClickListener(v -> openImageChooser());
        tvChangeAvatar.setOnClickListener(v -> openImageChooser());

        loadUserInfo();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager();
    }

    private void setupViewPager() {
        viewPager.setAdapter(new ProfilePagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) tab.setText("мои работы");
                    else tab.setText("избранные");
                }
        ).attach();
    }

    private void loadUserInfo() {
        long userId = sessionManager.getCurrentUserId();

        new Thread(() -> {
            User user = AppDatabase.getInstance(requireContext())
                    .userDao().getUserById(userId);

            int guidesCount = AppDatabase.getInstance(requireContext())
                    .guideDao().getGuidesCountByUser(userId);

            int likesCount = AppDatabase.getInstance(requireContext())
                    .guideDao().getTotalLikesByUser(userId);

            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    tvNickname.setText(user.login);
                    tvGuidesCount.setText(String.valueOf(guidesCount));
                    tvLikesCount.setText(String.valueOf(likesCount));
                    AvatarHelper.loadAvatar(requireContext(), user.avatarPath, ivAvatar);
                }
            });
        }).start();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "выберите аватар"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                saveAvatarToInternalStorage(selectedImageUri);
            }
        }
    }

    private void saveAvatarToInternalStorage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);

            File dir = new File(requireContext().getFilesDir(), "avatars");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = "avatar_" + sessionManager.getCurrentUserId() + ".jpg";
            File file = new File(dir, filename);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            }

            new Thread(() -> {
                AppDatabase.getInstance(requireContext())
                        .userDao().updateAvatarPath(sessionManager.getCurrentUserId(), file.getAbsolutePath());

                requireActivity().runOnUiThread(() -> {
                    // очищаем кэш Glide
                    Glide.get(requireContext()).clearMemory();
                    new Thread(() -> Glide.get(requireContext()).clearDiskCache()).start();

                    // обновляем аватарку во фрагменте
                    AvatarHelper.loadAvatar(requireContext(), file.getAbsolutePath(), ivAvatar);

                    // обновляем аватарку в тулбаре
                    if (getActivity() != null) {
                        ShapeableImageView toolbarAvatar = getActivity().findViewById(R.id.ivAvatar);
                        if (toolbarAvatar != null) {
                            AvatarHelper.loadAvatar(requireContext(), file.getAbsolutePath(), toolbarAvatar);
                        }
                    }

                    Toast.makeText(getContext(), "Аватар обновлён", Toast.LENGTH_SHORT).show();
                });
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка сохранения аватарки", Toast.LENGTH_SHORT).show();
        }
    }

    private static class ProfilePagerAdapter extends FragmentStateAdapter {
        public ProfilePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new UserGuidesFragment();
            } else {
                return new FavoriteGuidesFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}