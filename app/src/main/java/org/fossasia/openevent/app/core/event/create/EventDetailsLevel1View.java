package org.fossasia.openevent.app.core.event.create;

import org.fossasia.openevent.app.common.mvp.view.Erroneous;
import org.fossasia.openevent.app.common.mvp.view.Progressive;
import org.fossasia.openevent.app.common.mvp.view.Successful;

import java.util.List;

public interface EventDetailsLevel1View {

    List<String> getTimeZoneList();

    void setDefaultTimeZone(int index);

}
