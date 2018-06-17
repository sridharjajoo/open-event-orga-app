package org.fossasia.openevent.app.common.di.module.android;

import org.fossasia.openevent.app.core.auth.AuthActivity;
import org.fossasia.openevent.app.core.event.about.AboutEventActivity;
import org.fossasia.openevent.app.core.event.chart.ChartActivity;
import org.fossasia.openevent.app.core.event.create.CreateEventActivity;
import org.fossasia.openevent.app.core.event.create.CreateEventFragment;
import org.fossasia.openevent.app.core.event.create.EventDetailsLevel1;
import org.fossasia.openevent.app.core.event.create.EventDetailsLevel2;
import org.fossasia.openevent.app.core.event.create.EventDetailsLevel3;
import org.fossasia.openevent.app.core.main.MainActivity;
import org.fossasia.openevent.app.core.organizer.detail.OrganizerDetailActivity;
import org.fossasia.openevent.app.core.speaker.details.SpeakerDetailsActivity;
import org.fossasia.openevent.app.core.speaker.details.SpeakerDetailsFragment;
import org.fossasia.openevent.app.core.speakerscall.create.CreateSpeakersCallFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector(modules = {MainFragmentBuildersModule.class, TracksFragmentBuildersModule.class})
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(modules = AuthFragmentBuildersModule.class)
    abstract AuthActivity contributeAuthActivity();

    @ContributesAndroidInjector(modules = AboutFragmentBuildersModule.class)
    abstract AboutEventActivity contributeEventActivity();

    @ContributesAndroidInjector(modules = OrganizerFragmentBuildersModule.class)
    abstract OrganizerDetailActivity contributeOrganizerDetailActivity();

    @ContributesAndroidInjector
    abstract CreateEventActivity contributeCreateEventActivity();

    @ContributesAndroidInjector
    abstract CreateEventFragment contributeCreateEventFragment();

    @ContributesAndroidInjector
    abstract EventDetailsLevel1 contributesEventDetailsLevel1();

    @ContributesAndroidInjector
    abstract EventDetailsLevel2 contributesEventDetailsLevel2();

    @ContributesAndroidInjector
    abstract EventDetailsLevel3 contributesEventDetailsLevel3();

    @ContributesAndroidInjector
    abstract ChartActivity contributeChartActivity();

    @ContributesAndroidInjector
    abstract SpeakerDetailsActivity contributeSpeakerDetailsActivity();

    @ContributesAndroidInjector
    abstract SpeakerDetailsFragment contributeSpeakerDetailsFragment();

    @ContributesAndroidInjector
    abstract CreateSpeakersCallFragment contributeCreateSpeakersCallFragment();

}
