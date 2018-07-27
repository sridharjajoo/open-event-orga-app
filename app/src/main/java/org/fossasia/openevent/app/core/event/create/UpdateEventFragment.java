package org.fossasia.openevent.app.core.event.create;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.Function;
import org.fossasia.openevent.app.common.mvp.view.BaseFragment;
import org.fossasia.openevent.app.data.event.Event;
import org.fossasia.openevent.app.databinding.EventCreateLayoutBinding;
import org.fossasia.openevent.app.ui.ViewUtils;
import org.fossasia.openevent.app.utils.Utils;
import org.fossasia.openevent.app.utils.ValidateUtils;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import br.com.ilhasoft.support.validation.Validator;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static org.fossasia.openevent.app.ui.ViewUtils.showView;

public class UpdateEventFragment extends BaseFragment implements CreateEventView {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private EventCreateLayoutBinding binding;
    private Validator validator;
    private ArrayAdapter<CharSequence> currencyAdapter;
    private ArrayAdapter<CharSequence> paymentCountryAdapter;
    private ArrayAdapter<CharSequence> timezoneAdapter;
    private long eventId = -1;
    private int countryIndex = -1;
    private final GooglePlacesDecider googlePlacesDecider = new GooglePlacesDecider();

    private static final int PLACE_PICKER_REQUEST = 1;
    private CreateEventViewModel createEventViewModel;

    public static UpdateEventFragment newInstance() {
        return new UpdateEventFragment();
    }

    public static UpdateEventFragment newInstance(long id) {
        Bundle bundle = new Bundle();
        bundle.putLong(CreateEventActivity.EVENT_ID, id);
        UpdateEventFragment updateEventFragment = new UpdateEventFragment();
        updateEventFragment.setArguments(bundle);
        return updateEventFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.event_create_layout, container, false);
        validator = new Validator(binding.form);
        createEventViewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(CreateEventViewModel.class);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            eventId = bundle.getLong(CreateEventActivity.EVENT_ID);
        }

        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        activity.setSupportActionBar(binding.toolbar);

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setHasOptionsMenu(true);

        binding.submit.setOnClickListener(view -> {
            if (validator.validate()) {
                createEventViewModel.updateEvent();
            }
        });

        setupSpinners();
        attachCountryList(createEventViewModel.getCountryList());
        attachCurrencyCodesList(createEventViewModel.getCurrencyCodesList());

        setupPlacePicker();

        return binding.getRoot();
    }

    private void setupSpinners() {
        currencyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentCountryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        paymentCountryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timezoneAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        timezoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timezoneAdapter.addAll(getTimeZoneList());
        binding.form.timezoneSpinner.setAdapter(timezoneAdapter);

        binding.form.paymentCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
    public void onStart() {
        super.onStart();
        createEventViewModel.getProgress().observe(this, this::showProgress);
        createEventViewModel.getErrorMessage().observe(this, this::showError);
        createEventViewModel.getEventLiveData().observe(this, event -> {
            setEvent(event);
            setPaymentBinding(event);
        });
        createEventViewModel.getCloseState().observe(this, isClose -> close());

        validate(binding.form.ticketUrlLayout, ValidateUtils::validateUrl, getResources().getString(R.string.url_validation_error));
        validate(binding.form.logoUrlLayout, ValidateUtils::validateUrl, getResources().getString(R.string.url_validation_error));
        validate(binding.form.externalEventUrlLayout, ValidateUtils::validateUrl, getResources().getString(R.string.url_validation_error));
        validate(binding.form.originalImageUrlLayout, ValidateUtils::validateUrl, getResources().getString(R.string.url_validation_error));
        validate(binding.form.paypalEmailLayout, ValidateUtils::validateEmail, getResources().getString(R.string.email_validation_error));

        createEventViewModel.loadEvents(eventId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_event:
                shareEvent();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.action_share_event);
        Drawable shareIcon = menu.findItem(R.id.action_share_event).getIcon();
        shareIcon.setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);
        menuItem.setVisible(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void shareEvent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, Utils.getShareableInformation(createEventViewModel.getEvent()));
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
    }

    @Override
    public void validate(TextInputLayout textInputLayout, Function<String, Boolean> validationReference, String errorResponse) {
        textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Nothing here
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (validationReference.apply(charSequence.toString())) {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                } else {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(errorResponse);
                }
                if (TextUtils.isEmpty(charSequence)) {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Nothing here
            }
        });
    }

    @Override
    public void setPaymentCurrency(int index) {
        binding.form.currencySpinner.setSelection(index);
    }

    @Override
    public void attachCountryList(List<String> countryList) {
        paymentCountryAdapter.addAll(countryList);
        binding.form.paymentCountrySpinner.setAdapter(paymentCountryAdapter);
        binding.form.paymentCountrySpinner.setSelection(createEventViewModel.getCountryIndex());
    }

    @Override
    public void attachCurrencyCodesList(List<String> currencyCodesList) {
        currencyAdapter.addAll(currencyCodesList);
        binding.form.currencySpinner.setAdapter(currencyAdapter);
    }

    @Override
    protected int getTitle() {
        return R.string.update_event;
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

    public void close() {
        getActivity().finish();
    }

    @Override
    public List<String> getTimeZoneList() {
        return Arrays.asList(getResources().getStringArray(R.array.timezones));
    }

    private void setupPlacePicker() {
        //check if there's an google places API key
        try {
            ApplicationInfo ai = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String placesApiKey = bundle.getString("com.google.android.geo.API_KEY");
            if ("YOUR_API_KEY".equals(placesApiKey)) {
                Timber.d("Add Google Places API key in AndroidManifest.xml file to use Place Picker.");
                binding.form.buttonPlacePicker.setVisibility(View.GONE);
                binding.form.layoutLatitude.setVisibility(View.VISIBLE);
                binding.form.layoutLongitude.setVisibility(View.VISIBLE);
                showLocationLayouts();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Package name not found");
        }

        binding.form.buttonPlacePicker.setOnClickListener(view -> {
            googlePlacesDecider.onSelectingButtonPlacePicker(getActivity());
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            //once place is picked from map, make location fields visible for confirmation by user
            showLocationLayouts();
            //set event attributes

            googlePlacesDecider.setGooglePlaces(getActivity(), data);

            Event event = binding.getEvent();
            event.latitude = googlePlacesDecider.getLatitude();
            event.longitude = googlePlacesDecider.getLongitude();
            //auto-complete location fields for confirmation by user

            binding.form.locationName.setText(googlePlacesDecider.getAddress());
            binding.form.searchableLocationName.setText(
                createEventViewModel.getSearchableLocationName(googlePlacesDecider.getAddress().toString())
            );
        }
    }

    private void showLocationLayouts() {
        binding.form.layoutSearchableLocation.setVisibility(View.VISIBLE);
        binding.form.layoutLocationName.setVisibility(View.VISIBLE);
    }

    @Override
    public void setEvent(Event event) {
        binding.setEvent(event);
        String timezone = createEventViewModel.getEvent().getTimezone();
        if (createEventViewModel.getEvent().getPaymentCountry() != null) {
            String paymentCountry = createEventViewModel.getEvent().getPaymentCountry();
            countryIndex = paymentCountryAdapter.getPosition(paymentCountry);
        } else {
            countryIndex = createEventViewModel.getCountryIndex();
        }

        int timezoneIndex = timezoneAdapter.getPosition(timezone);

        binding.form.paymentCountrySpinner.setSelection(countryIndex);
        binding.form.timezoneSpinner.setSelection(timezoneIndex);
        binding.form.enableSponsor.setChecked(event.isSponsorsEnabled);
        binding.form.enableSession.setChecked(event.isSessionsSpeakersEnabled);
        binding.form.enableTax.setChecked(event.isTaxEnabled);
        binding.form.ticketingDetails.setChecked(event.isTicketingEnabled);
        binding.form.organizerInfo.setChecked(event.hasOrganizerInfo);
    }

    @Override
    public void setPaymentBinding(Event event) {
        binding.form.paypalPayment.setChecked(event.canPayByPaypal);
        binding.form.stripePayment.setChecked(event.canPayByStripe);
        binding.form.bankPayment.setChecked(event.canPayByBank);
        binding.form.chequePayment.setChecked(event.canPayByCheque);
        binding.form.onsitePayment.setChecked(event.canPayOnsite);
        binding.setEvent(event);
    }
}
