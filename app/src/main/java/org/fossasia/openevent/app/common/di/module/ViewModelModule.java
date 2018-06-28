package org.fossasia.openevent.app.common.di.module;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import org.fossasia.openevent.app.common.di.OrgaViewModelFactory;
import org.fossasia.openevent.app.core.auth.login.LoginViewModel;
import org.fossasia.openevent.app.core.event.create.CreateEventViewModel;
import org.fossasia.openevent.app.core.orders.list.OrdersViewModel;
import org.fossasia.openevent.app.core.share.ShareEventViewModel;
import org.fossasia.openevent.app.core.speaker.details.SpeakerDetailsViewModel;
import org.fossasia.openevent.app.core.speakerscall.create.CreateSpeakersCallViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    public abstract ViewModel bindLoginViewModel(LoginViewModel loginViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SpeakerDetailsViewModel.class)
    public abstract ViewModel bindSpeakerDetailsViewModel(SpeakerDetailsViewModel speakerDetailsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(OrdersViewModel.class)
    public abstract ViewModel bindOrdersViewModel(OrdersViewModel ordersViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CreateSpeakersCallViewModel.class)
    public abstract ViewModel bindCreateSpeakersCallViewModel(CreateSpeakersCallViewModel createSpeakersCallViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ShareEventViewModel.class)
    public abstract ViewModel bindShareEventViewModel(ShareEventViewModel shareEventViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CreateEventViewModel.class)
    public abstract ViewModel bindCreateEventViewModel(CreateEventViewModel createEventViewModel);

    @Binds
    public abstract ViewModelProvider.Factory bindViewModelFactory(OrgaViewModelFactory factory);

}
