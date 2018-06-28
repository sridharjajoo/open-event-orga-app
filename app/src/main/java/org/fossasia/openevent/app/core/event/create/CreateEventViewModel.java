package org.fossasia.openevent.app.core.event.create;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import org.fossasia.openevent.app.common.rx.Logger;
import org.fossasia.openevent.app.data.event.Event;
import org.fossasia.openevent.app.data.event.EventRepository;
import org.fossasia.openevent.app.utils.CurrencyUtils;
import org.fossasia.openevent.app.utils.DateUtils;
import org.fossasia.openevent.app.utils.StringUtils;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeParseException;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

import static org.fossasia.openevent.app.common.rx.ViewTransformers.dispose;
import static org.fossasia.openevent.app.common.rx.ViewTransformers.progressiveErroneous;

public class CreateEventViewModel extends ViewModel {

    private final EventRepository eventRepository;
    private Event event = new Event();
    private final Map<String, String> countryCurrencyMap;
    private final List<String> countryList;
    private final List<String> currencyCodesList;
    private List<String> timeZoneList;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<String> onSuccess = new MutableLiveData<>();
    private MutableLiveData<String> onError = new MutableLiveData<>();
    private MutableLiveData<Boolean> close = new MutableLiveData<>();

    @Inject
    public CreateEventViewModel(EventRepository eventRepository, CurrencyUtils currencyUtils) {
        this.eventRepository = eventRepository;
        LocalDateTime current = LocalDateTime.now();

        String isoDate = DateUtils.formatDateToIso(current);
        event.setStartsAt(isoDate);
        event.setEndsAt(isoDate);

        countryCurrencyMap = currencyUtils.getCountryCurrencyMap();
        countryList = new ArrayList<>(countryCurrencyMap.keySet());
        currencyCodesList = currencyUtils.getCurrencyCodesList();
    }

    public void setTimeZoneList(List<String> timeZoneList) {
        this.timeZoneList = timeZoneList;
    }

    public List<String> getCountryList() {
        return countryList;
    }

    public List<String> getCurrencyCodesList() {
        return currencyCodesList;
    }

    public Event getEvent() {
        return event;
    }

    private boolean verify() {
        try {
            ZonedDateTime start = DateUtils.getDate(event.getStartsAt());
            ZonedDateTime end = DateUtils.getDate(event.getEndsAt());

            if (!end.isAfter(start)) {
                onError.setValue("End time should be after start time");
                return false;
            }
            return true;
        } catch (DateTimeParseException pe) {
                onError.setValue("Please enter date in correct format");
                return false;
        }
    }

    protected void nullifyEmptyFields(Event event) {
        event.setLogoUrl(StringUtils.emptyToNull(event.getLogoUrl()));
        event.setTicketUrl(StringUtils.emptyToNull(event.getTicketUrl()));
        event.setOriginalImageUrl(StringUtils.emptyToNull(event.getOriginalImageUrl()));
        event.setExternalEventUrl(StringUtils.emptyToNull(event.getExternalEventUrl()));
        event.setPaypalEmail(StringUtils.emptyToNull(event.getPaypalEmail()));
    }

    public void createEvent() {
        if (!verify())
            return;

        nullifyEmptyFields(event);

        Log.i("Event Creation: ", "createEvent: " + event.getStartsAt() + " " + event.getEndsAt());
        compositeDisposable.add(eventRepository
            .createEvent(event)
            .subscribe(createdEvent -> {
                onSuccess.setValue("Event Created Successfully");
                close.setValue(true);
            }, Logger::logError));
    }

    public LiveData<String> getSuccessMessage() {
        return onSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return onError;
    }

    public LiveData<Boolean> getCloseState() {
        return close;
    }

//    private void showEvent() {
//        getView().setEvent(event);
//    }

    //Used for loading the event information on start
    public void loadEvents(long eventId) {
        compositeDisposable.add(    eventRepository
            .getEvent(eventId, false)
//            .doFinally(this::showEvent)
            .subscribe(loadedEvent -> this.event = (Event) loadedEvent, Logger::logError));
    }

    //method called for updating an event
    public void updateEvent() {
        if (!verify())
            return;

        nullifyEmptyFields(event);

        compositeDisposable.add(eventRepository
            .updateEvent(event)
            .subscribe(updatedEvent -> {
                onSuccess.setValue("Event Updated Successfully");
                close.setValue(true);
            }, Logger::logError));
     }

    /**
     * Returns the most accurate and searchable address substring, which a user can search for.
     * Also makes sure that the substring doesn't contain any numbers by matching it to the regex,
     * as those are more likely to be house numbers or block numbers.
     * @param address full address string of a location
     * @return searchable address substring
     */
    public String getSearchableLocationName(String address) {
        String primary = address.substring(0, address.indexOf(','));
        if (primary.matches(".*\\d+.*")) { //contains number => not likely to be searchable
            return address.substring(address.indexOf(',') + 2, address.indexOf(",", address.indexOf(',') + 1));
        } else return primary;
    }

    /**
     * auto-selects paymentCurrency when paymentCountry is selected.
     * @param paymentCountry chosen payment country
     */
    public int onPaymentCountrySelected(String paymentCountry) {
        event.setPaymentCountry(paymentCountry);
        String paymentCurrency = countryCurrencyMap.get(paymentCountry);
        event.setPaymentCurrency(paymentCurrency);
        return currencyCodesList.indexOf(paymentCurrency);
    }
}
