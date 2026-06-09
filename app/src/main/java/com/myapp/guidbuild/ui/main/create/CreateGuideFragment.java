package com.myapp.guidbuild.ui.main.create;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.Game;
import com.myapp.guidbuild.data.database.Guide;
import com.myapp.guidbuild.data.database.GuideBlock;
import com.myapp.guidbuild.utils.SessionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateGuideFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Spinner spinnerGame;
    private EditText etTitle;
    private RecyclerView recyclerViewBlocks;
    private BlocksAdapter blocksAdapter;
    private List<GuideBlock> blocks;
    private List<Game> gamesList;
    private long selectedGameId = -1;
    private int pendingImagePosition = -1;  // ← объявляем ОДИН РАЗ

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_guide, container, false);

        spinnerGame = view.findViewById(R.id.spinnerGame);
        etTitle = view.findViewById(R.id.etTitle);
        recyclerViewBlocks = view.findViewById(R.id.recyclerViewBlocks);

        Button btnAddText = view.findViewById(R.id.btnAddText);
        Button btnAddImage = view.findViewById(R.id.btnAddImage);
        Button btnPublish = view.findViewById(R.id.btnPublish);

        btnAddText.setOnClickListener(v -> addTextBlock());
        btnAddImage.setOnClickListener(v -> addImageBlock());
        btnPublish.setOnClickListener(v -> saveGuide(false));

        recyclerViewBlocks.setLayoutManager(new LinearLayoutManager(getContext()));
        blocks = new ArrayList<>();
        blocksAdapter = new BlocksAdapter(blocks);
        blocksAdapter.setOnBlockDeleteListener(position -> {
            blocks.remove(position);
            blocksAdapter.notifyItemRemoved(position);
        });
        blocksAdapter.setOnImagePickedListener(position -> {
            openImageChooserForPosition(position);
        });
        recyclerViewBlocks.setAdapter(blocksAdapter);

        loadGames();

        return view;
    }

    private void loadGames() {
        new Thread(() -> {
            gamesList = AppDatabase.getInstance(requireContext())
                    .gameDao().getAllGames();

            List<String> gameNames = new ArrayList<>();
            for (Game game : gamesList) {
                gameNames.add(game.name);
            }

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        gameNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGame.setAdapter(adapter);
                spinnerGame.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedGameId = gamesList.get(position).id;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedGameId = -1;
                    }
                });
            });
        }).start();
    }

    private void addTextBlock() {
        blocks.add(new GuideBlock(GuideBlock.TYPE_TEXT, "", null));
        blocksAdapter.notifyItemInserted(blocks.size() - 1);
        recyclerViewBlocks.scrollToPosition(blocks.size() - 1);
    }

    private void addImageBlock() {
        blocks.add(new GuideBlock(GuideBlock.TYPE_IMAGE, "", null));
        blocksAdapter.notifyItemInserted(blocks.size() - 1);
        openImageChooserForPosition(blocks.size() - 1);
    }

    private void openImageChooserForPosition(int position) {
        pendingImagePosition = position;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Выберите картинку"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && pendingImagePosition != -1) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                String imagePath = saveImageToInternalStorage(selectedImageUri);
                if (imagePath != null) {
                    blocks.get(pendingImagePosition).imagePath = imagePath;
                    blocksAdapter.notifyItemChanged(pendingImagePosition);
                }
            }
            pendingImagePosition = -1;
        }
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            android.graphics.Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
            android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 800, 800, true);

            File dir = new File(requireContext().getFilesDir(), "guide_images");
            if (!dir.exists()) dir.mkdirs();

            String filename = "guide_img_" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(dir, filename);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, fos);
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveGuide(boolean isDraft) {
        String title = etTitle.getText().toString().trim();
        long userId = new SessionManager(requireContext()).getCurrentUserId();

        if (selectedGameId == -1) {
            Toast.makeText(getContext(), "Выберите игру", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Введите заголовок", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        String contentJson = gson.toJson(blocks);

        Guide guide = new Guide();
        guide.userId = userId;
        guide.gameId = selectedGameId;
        guide.title = title;
        guide.content = contentJson;
        guide.createdDate = System.currentTimeMillis();
        guide.likesCount = 0;
        guide.commentsCount = 0;
        guide.isDraft = isDraft ? 1 : 0;

        new Thread(() -> {
            AppDatabase.getInstance(requireContext()).guideDao().insert(guide);
            requireActivity().runOnUiThread(() -> {
                String msg = isDraft ? "Черновик сохранён" : "Гайд опубликован!";
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                if (!isDraft) {
                    etTitle.setText("");
                    blocks.clear();
                    blocksAdapter.notifyDataSetChanged();
                }
            });
        }).start();
    }
}