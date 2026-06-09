package com.myapp.guidbuild.ui.main.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.myapp.guidbuild.ui.main.detail.GuideDetailFragment;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.Game;
import com.myapp.guidbuild.data.database.Guide;
import com.myapp.guidbuild.ui.main.common.GuidesAdapter;
import com.myapp.guidbuild.utils.SessionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FavoriteGuidesFragment extends Fragment {

    private RecyclerView recyclerView;
    private GuidesAdapter adapter;
    private List<Guide> allGuides;
    private List<Guide> filteredGuides;
    private long userId;
    private EditText etSearch;
    private ChipGroup chipGroup;
    private TextView tvAddFilter;

    private String currentSort = "newest";
    private Long currentGameId = null;
    private String currentGameName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_guides, container, false);

        userId = new SessionManager(requireContext()).getCurrentUserId();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearch = view.findViewById(R.id.etSearch);
        chipGroup = view.findViewById(R.id.chipGroup);
        tvAddFilter = view.findViewById(R.id.tvAddFilter);

        filteredGuides = new ArrayList<>();
        adapter = new GuidesAdapter(filteredGuides);
        setupAdapterCallbacks();
        recyclerView.setAdapter(adapter);

        loadFavorites();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvAddFilter.setOnClickListener(v -> showFilterDialog());

        return view;
    }

    private void loadFavorites() {
        new Thread(() -> {
            allGuides = AppDatabase.getInstance(requireContext())
                    .favoriteDao().getFavoriteGuides(userId);

            requireActivity().runOnUiThread(() -> {
                filteredGuides.clear();
                filteredGuides.addAll(allGuides);
                applyFilters();
            });
        }).start();
    }

    private void setupAdapterCallbacks() {
        adapter.setOnItemClickListener(guide -> {
            GuideDetailFragment fragment = GuideDetailFragment.newInstance(guide.id);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filters, null);
        builder.setView(dialogView);

        RadioGroup radioGroupSort = dialogView.findViewById(R.id.radioGroupSort);
        Spinner spinnerGame = dialogView.findViewById(R.id.spinnerGame);
        Button btnApply = dialogView.findViewById(R.id.btnApplyFilters);

        if (currentSort.equals("newest")) {
            radioGroupSort.check(R.id.radioNewest);
        } else if (currentSort.equals("oldest")) {
            radioGroupSort.check(R.id.radioOldest);
        } else {
            radioGroupSort.check(R.id.radioAlphabet);
        }

        loadGamesToSpinner(spinnerGame);

        AlertDialog dialog = builder.create();

        btnApply.setOnClickListener(v -> {
            int checkedId = radioGroupSort.getCheckedRadioButtonId();
            if (checkedId == R.id.radioNewest) {
                currentSort = "newest";
            } else if (checkedId == R.id.radioOldest) {
                currentSort = "oldest";
            } else {
                currentSort = "alphabet";
            }

            Game selectedGame = (Game) spinnerGame.getSelectedItem();
            if (selectedGame != null && selectedGame.id != -1) {
                currentGameId = selectedGame.id;
                currentGameName = selectedGame.name;
                addFilterChip("игра: " + selectedGame.name);
            } else {
                currentGameId = null;
                currentGameName = null;
                removeFilterChipByPrefix("игра:");
            }

            updateFilterDisplay();
            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateFilterDisplay() {
        String filterText = "сортировка: ";
        switch (currentSort) {
            case "newest":
                filterText += "новые";
                break;
            case "oldest":
                filterText += "старые";
                break;
            case "alphabet":
                filterText += "по алфавиту";
                break;
        }
        if (currentGameId != null && currentGameName != null) {
            filterText += " | игра: " + currentGameName;
        }
        tvAddFilter.setText(filterText);
    }

    private void loadGamesToSpinner(Spinner spinner) {
        new Thread(() -> {
            List<Game> games = AppDatabase.getInstance(requireContext())
                    .gameDao().getAllGames();

            Game allGames = new Game("все игры");
            allGames.id = -1;
            games.add(0, allGames);

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<Game> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        games
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                if (currentGameId != null) {
                    for (int i = 0; i < games.size(); i++) {
                        if (games.get(i).id == currentGameId) {
                            spinner.setSelection(i);
                            break;
                        }
                    }
                }
            });
        }).start();
    }

    private void addFilterChip(String text) {
        if (text.startsWith("игра:")) {
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip existingChip = (Chip) child;
                    if (existingChip.getText().toString().startsWith("игра:")) {
                        chipGroup.removeView(existingChip);
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip existingChip = (Chip) child;
                if (existingChip.getText().toString().equals(text)) {
                    return;
                }
            }
        }

        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);

        if (text.startsWith("игра:")) {
            chip.setOnCloseIconClickListener(v -> {
                chipGroup.removeView(chip);
                currentGameId = null;
                currentGameName = null;
                updateFilterDisplay();
                applyFilters();
            });
        }

        chipGroup.addView(chip);
    }

    private void removeFilterChipByPrefix(String prefix) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().startsWith(prefix)) {
                    chipGroup.removeView(chip);
                    break;
                }
            }
        }
    }

    private void updateSortChip() {
        removeFilterChipByPrefix("сортировка:");

        String sortText = "сортировка: ";
        switch (currentSort) {
            case "newest":
                sortText += "новые";
                break;
            case "oldest":
                sortText += "старые";
                break;
            case "alphabet":
                sortText += "по алфавиту";
                break;
        }

        Chip chip = new Chip(requireContext());
        chip.setText(sortText);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            currentSort = "newest";
            updateFilterDisplay();
            applyFilters();
        });
        chipGroup.addView(chip, 0);
    }

    private void applyFilters() {
        String searchText = etSearch.getText().toString().toLowerCase(Locale.getDefault());

        new Thread(() -> {
            List<Guide> result = new ArrayList<>();

            for (Guide guide : allGuides) {
                if (!searchText.isEmpty() && !guide.title.toLowerCase().contains(searchText)) {
                    continue;
                }
                if (currentGameId != null && guide.gameId != currentGameId) {
                    continue;
                }
                result.add(guide);
            }

            if (currentSort.equals("newest")) {
                Collections.sort(result, (a, b) -> Long.compare(b.createdDate, a.createdDate));
            } else if (currentSort.equals("oldest")) {
                Collections.sort(result, (a, b) -> Long.compare(a.createdDate, b.createdDate));
            } else if (currentSort.equals("alphabet")) {
                Collections.sort(result, (a, b) -> a.title.compareToIgnoreCase(b.title));
            }

            requireActivity().runOnUiThread(() -> {
                filteredGuides.clear();
                filteredGuides.addAll(result);
                adapter.notifyDataSetChanged();
                updateSortChip();
            });
        }).start();
    }
}