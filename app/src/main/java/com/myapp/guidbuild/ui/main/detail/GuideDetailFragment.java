package com.myapp.guidbuild.ui.main.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.Guide;
import com.myapp.guidbuild.data.database.GuideBlock;
import java.io.File;
import java.util.List;

public class GuideDetailFragment extends Fragment {

    private static final String ARG_GUIDE_ID = "guide_id";
    private LinearLayout containerBlocks;
    private long guideId;

    public static GuideDetailFragment newInstance(long guideId) {
        GuideDetailFragment fragment = new GuideDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_GUIDE_ID, guideId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide_detail, container, false);
        containerBlocks = view.findViewById(R.id.containerBlocks);

        if (getArguments() != null) {
            guideId = getArguments().getLong(ARG_GUIDE_ID);
        }

        loadGuide();
        return view;
    }

    private void loadGuide() {
        new Thread(() -> {
            Guide guide = AppDatabase.getInstance(requireContext())
                    .guideDao().getGuideById(guideId);

            String contentJson = guide.content;
            Gson gson = new Gson();
            GuideBlock[] blocks = gson.fromJson(contentJson, GuideBlock[].class);

            requireActivity().runOnUiThread(() -> displayBlocks(blocks));
        }).start();
    }

    private void displayBlocks(GuideBlock[] blocks) {
        containerBlocks.removeAllViews();

        for (GuideBlock block : blocks) {
            if (block.type == GuideBlock.TYPE_TEXT) {
                addTextBlock(block.text);
            } else if (block.type == GuideBlock.TYPE_IMAGE && block.imagePath != null) {
                addImageBlock(block.imagePath);
            }
        }
    }

    private void addTextBlock(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(16f);
        textView.setPadding(0, 0, 0, 32);
        containerBlocks.addView(textView);
    }

    private void addImageBlock(String imagePath) {
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        imageView.setPadding(0, 0, 0, 32);
        imageView.setAdjustViewBounds(true);

        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Glide.with(this).load(imgFile).into(imageView);
        }

        containerBlocks.addView(imageView);
    }
}