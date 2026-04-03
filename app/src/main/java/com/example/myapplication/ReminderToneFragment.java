package com.example.myapplication;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class ReminderToneFragment extends Fragment {

    private ReminderViewModel viewModel;

    // Inflates the tone selection layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_tone, container, false);
    }

    // Wires the tone selector and live preview.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(ReminderViewModel.class);

        MaterialButtonToggleGroup toneGroup = view.findViewById(R.id.toneGroup);
        MaterialButton caringBtn = view.findViewById(R.id.caringBtn);
        MaterialButton directBtn = view.findViewById(R.id.directBtn);
        TextView selectedToneText = view.findViewById(R.id.selectedToneText);
        TextView preview = view.findViewById(R.id.previewText);
        Button nextBtn = view.findViewById(R.id.nextBtn);

        syncSelection(toneGroup, caringBtn, directBtn, selectedToneText, preview);

        toneGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }

            if (checkedId == R.id.caringBtn) {
                viewModel.setTone(ReminderScheduler.TONE_CARING);
            } else if (checkedId == R.id.directBtn) {
                viewModel.setTone(ReminderScheduler.TONE_DIRECT);
            }

            applyToneVisualState(caringBtn, directBtn, selectedToneText);
            preview.setText(ReminderMessageFormatter.getPreviewMessage(requireContext(), viewModel.getTone()));
        });

        nextBtn.setOnClickListener(v -> {
            if (viewModel.getTone() == null) {
                Toast.makeText(getContext(), R.string.reminder_tone_required, Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.setReminderScheduled(false);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_tone_to_confirmation);
        });
    }

    // Restores the previously chosen tone or applies the supportive default.
    private void syncSelection(
            MaterialButtonToggleGroup toneGroup,
            MaterialButton caringBtn,
            MaterialButton directBtn,
            TextView selectedToneText,
            TextView preview
    ) {
        if (ReminderScheduler.TONE_DIRECT.equals(viewModel.getTone())) {
            toneGroup.check(R.id.directBtn);
        } else {
            toneGroup.check(R.id.caringBtn);
            viewModel.setTone(ReminderScheduler.TONE_CARING);
        }

        applyToneVisualState(caringBtn, directBtn, selectedToneText);
        preview.setText(ReminderMessageFormatter.getPreviewMessage(requireContext(), viewModel.getTone()));
    }

    // Applies strong visual feedback so the selected tone is obvious.
    private void applyToneVisualState(MaterialButton caringBtn, MaterialButton directBtn, TextView selectedToneText) {
        boolean isCaring = ReminderScheduler.TONE_CARING.equals(viewModel.getTone());

        int selectedBackground = ContextCompat.getColor(requireContext(), R.color.primary);
        int unselectedBackground = ContextCompat.getColor(requireContext(), R.color.surface);
        int selectedText = ContextCompat.getColor(requireContext(), R.color.white);
        int unselectedText = ContextCompat.getColor(requireContext(), R.color.text_primary);
        int selectedStroke = ContextCompat.getColor(requireContext(), R.color.primary_variant);
        int unselectedStroke = ContextCompat.getColor(requireContext(), R.color.border);

        styleToneButton(caringBtn, isCaring, selectedBackground, unselectedBackground, selectedText, unselectedText, selectedStroke, unselectedStroke);
        styleToneButton(directBtn, !isCaring, selectedBackground, unselectedBackground, selectedText, unselectedText, selectedStroke, unselectedStroke);

        // Surface the active mode in plain text for quick scan and accessibility.
        String selectedToneLabel = isCaring
                ? getString(R.string.reminder_tone_caring_label)
                : getString(R.string.reminder_tone_direct_label);
        selectedToneText.setText(getString(R.string.reminder_tone_selected_template, selectedToneLabel));
    }

    // Styles one tone button as selected or unselected.
    private void styleToneButton(
            MaterialButton button,
            boolean selected,
            int selectedBackground,
            int unselectedBackground,
            int selectedText,
            int unselectedText,
            int selectedStroke,
            int unselectedStroke
    ) {
        // Mirror toggle state with color, border, and icon so selection is unmistakable.
        button.setBackgroundTintList(ColorStateList.valueOf(selected ? selectedBackground : unselectedBackground));
        button.setTextColor(selected ? selectedText : unselectedText);
        button.setStrokeColor(ColorStateList.valueOf(selected ? selectedStroke : unselectedStroke));
        button.setStrokeWidth(selected ? 4 : 2);
        button.setIconResource(selected ? android.R.drawable.checkbox_on_background : 0);
        button.setIconTint(ColorStateList.valueOf(selected ? selectedText : unselectedText));
        button.setIconPadding(10);
    }
}
