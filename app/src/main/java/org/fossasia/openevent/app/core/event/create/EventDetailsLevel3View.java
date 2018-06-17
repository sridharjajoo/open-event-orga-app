package org.fossasia.openevent.app.core.event.create;

import org.fossasia.openevent.app.common.mvp.view.Erroneous;
import org.fossasia.openevent.app.common.mvp.view.Progressive;
import org.fossasia.openevent.app.common.mvp.view.Successful;

import java.util.List;

public interface EventDetailsLevel3View extends Progressive, Erroneous, Successful {

    void attachCountryList(List<String> countryList);

    void attachCurrencyCodesList(List<String> currencyCodesList);

    void setPaymentCurrency(int index);

    void setDefaultCountry(int index);

    void close(boolean bool);

}
