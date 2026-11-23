package org.me.gcu.johnston_zoe_s2336647.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.me.gcu.johnston_zoe_s2336647.R;
import org.me.gcu.johnston_zoe_s2336647.viewmodel.TrafficViewModel;

public class SituationListFragment extends Fragment {

    public SituationListFragment() {
        super(R.layout.fragment_situation_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recycler = view.findViewById(R.id.situationRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        recycler.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        TextView status = view.findViewById(R.id.listStatus);
        TextView notice = view.findViewById(R.id.listNotice);

        SituationAdapter adapter = new SituationAdapter();
        recycler.setAdapter(adapter);

        TrafficViewModel viewModel = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);

        // a tap selects a specific situation
        adapter.setOnSituationClickListener(situation -> viewModel.select(situation));

        viewModel.getVisibleSituations().observe(getViewLifecycleOwner(), list -> adapter.setItems(list));

        // highlight row when selected on landscape mode
        viewModel.getSelectedSituation().observe(getViewLifecycleOwner(), situation -> adapter.setSelected(situation));

        viewModel.getStatusText().observe(getViewLifecycleOwner(), text -> status.setText(text));

        // updated/offline data message
        viewModel.getUpdateNotice().observe(getViewLifecycleOwner(), text -> {
            notice.setText(text);
            notice.setVisibility(text == null || text.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
    }
}