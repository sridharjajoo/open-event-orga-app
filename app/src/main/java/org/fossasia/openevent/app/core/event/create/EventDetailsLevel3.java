package org.fossasia.openevent.app.core.event.create;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.mvp.view.BaseBottomSheetFragment;
import org.fossasia.openevent.app.databinding.EventDetailsLevel3Binding;
import org.fossasia.openevent.app.ui.ViewUtils;

import java.util.List;

import javax.inject.Inject;

import br.com.ilhasoft.support.validation.Validator;

import static org.fossasia.openevent.app.ui.ViewUtils.showView;

public class EventDetailsLevel3 extends BaseBottomSheetFragment implements EventDetailsLevel3View {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private EventDetailsLevel3Binding binding;
    private ArrayAdapter<CharSequence> currencyAdapter;
    private ArrayAdapter<CharSequence> paymentCountryAdapter;
    private CreateEventViewModel createEventViewModel;
    private Validator validator;

    public static Fragment newInstance() {
        return new EventDetailsLevel3();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.event_details_level_3, container, false);
        createEventViewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateEventViewModel.class);
        validator = new Validator(binding);
        setupSpinners();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        createEventViewModel.getSuccessMessage().observe(this, this::onSuccess);
        createEventViewModel.getErrorMessage().observe(this, this::showError);
        createEventViewModel.getCloseState().observe(this, this::close);
        attachCountryList(createEventViewModel.getCountryList());
        attachCurrencyCodesList(createEventViewModel.getCurrencyCodesList());

        binding.submit.setOnClickListener(view -> {
            if (validator.validate()) {
                binding.setEvent(createEventViewModel.getEvent());
                createEventViewModel.createEvent();
            }
        });
    }

    private void setupSpinners() {
        currencyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentCountryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        paymentCountryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.paymentCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index = createEventViewModel.onPaymentCountrySelected(adapterView.getItemAtPosition(i).toString());
                setPaymentCurrency(index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }

        });
    }

    @Override
    public void setPaymentCurrency(int index) {
        binding.currencySpinner.setSelection(index);
    }

    @Override
    public void attachCountryList(List<String> countryList) {
        paymentCountryAdapter.addAll(countryList);
        binding.paymentCountrySpinner.setAdapter(paymentCountryAdapter);
    }

    @Override
    public void attachCurrencyCodesList(List<String> currencyCodesList) {
        currencyAdapter.addAll(currencyCodesList);
        binding.currencySpinner.setAdapter(currencyAdapter);
    }

    @Override
    public void setDefaultCountry(int index) {

    }

    @Override
    public void showError(String error) {
        ViewUtils.showSnackbar(binding.getRoot(), error);
    }

    @Override
    public void showProgress(boolean show) {
        showView(binding.progressBar, show);
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void close(boolean bool) {
        getActivity().finish();
    }

}
