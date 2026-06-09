package com.myapp.guidbuild.ui.main.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.Favorite;
import com.myapp.guidbuild.data.database.Guide;
import com.myapp.guidbuild.data.database.Like;
import com.myapp.guidbuild.ui.main.common.GuidesAdapter;
import com.myapp.guidbuild.ui.main.detail.GuideDetailFragment;
import com.myapp.guidbuild.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class UserGuidesFragment extends Fragment {

    private RecyclerView recyclerView;
    private GuidesAdapter adapter;
    private List<Guide> guides;
    private long userId;
    private long currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_guides, container, false);

        userId = new SessionManager(requireContext()).getCurrentUserId();
        currentUserId = userId;

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        guides = new ArrayList<>();
        adapter = new GuidesAdapter(guides);
        adapter.setShowDelete(true);  // показываем кнопку удаления
        setupAdapterCallbacks();
        recyclerView.setAdapter(adapter);

        loadGuides();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGuides();
    }

    private void setupAdapterCallbacks() {
        adapter.setOnLikeClickListener((guide, position) -> {
            new Thread(() -> {
                int isLiked = AppDatabase.getInstance(requireContext())
                        .likeDao().isLiked(currentUserId, guide.id);
                if (isLiked > 0) {
                    AppDatabase.getInstance(requireContext())
                            .likeDao().deleteByUserAndGuide(currentUserId, guide.id);
                    guide.likesCount--;
                } else {
                    AppDatabase.getInstance(requireContext())
                            .likeDao().insert(new Like(currentUserId, guide.id));
                    guide.likesCount++;
                }
                AppDatabase.getInstance(requireContext())
                        .guideDao().updateLikesCount(guide.id, guide.likesCount);

                requireActivity().runOnUiThread(() -> adapter.notifyItemChanged(position));
            }).start();
        });

        adapter.setOnFavoriteClickListener((guide, position) -> {
            new Thread(() -> {
                int isFavorite = AppDatabase.getInstance(requireContext())
                        .favoriteDao().isFavorite(currentUserId, guide.id);
                if (isFavorite > 0) {
                    AppDatabase.getInstance(requireContext())
                            .favoriteDao().deleteByUserAndGuide(currentUserId, guide.id);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                    });
                } else {
                    AppDatabase.getInstance(requireContext())
                            .favoriteDao().insert(new Favorite(currentUserId, guide.id));
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                    });
                }
            }).start();
        });

        adapter.setOnDeleteClickListener((guide, position) -> {
            showDeleteConfirmDialog(guide, position);
        });

        adapter.setOnItemClickListener(guide -> {
            GuideDetailFragment fragment = GuideDetailFragment.newInstance(guide.id);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadGuides() {
        new Thread(() -> {
            List<Guide> userGuides = AppDatabase.getInstance(requireContext())
                    .guideDao().getGuidesByUser(userId);

            requireActivity().runOnUiThread(() -> {
                guides.clear();
                guides.addAll(userGuides);
                adapter.updateData(guides);
            });
        }).start();
    }

    private void showDeleteConfirmDialog(Guide guide, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление гайда")
                .setMessage("Вы уверены, что хотите удалить гайд \"" + guide.title + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteGuide(guide, position))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteGuide(Guide guide, int position) {
        new Thread(() -> {
            AppDatabase.getInstance(requireContext()).guideDao().delete(guide);
            AppDatabase.getInstance(requireContext()).favoriteDao().deleteByGuideId(guide.id);

            requireActivity().runOnUiThread(() -> {
                guides.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(getContext(), "Гайд удалён", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}