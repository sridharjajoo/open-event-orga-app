package org.fossasia.openevent.app.core.event.create;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.mvp.view.BaseBottomSheetFragment;
import org.fossasia.openevent.app.databinding.EventDetailsLevel2Binding;

import javax.inject.Inject;

public class EventDetailsLevel2 extends BaseBottomSheetFragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private EventDetailsLevel2Binding binding;

    public static EventDetailsLevel2 newInstance() {
        return new EventDetailsLevel2();
    }

    private CreateEventViewModel createEventViewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.event_details_level_2, container, false);
        createEventViewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateEventViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("EventDetailsLevel2", "onStart: " + createEventViewModel.getEvent().getStartsAt());
    }
}
