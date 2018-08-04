package com.eventyay.organizer.core.ticket.list;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.raizlabs.android.dbflow.structure.BaseModel;

import com.eventyay.organizer.common.rx.Logger;
import com.eventyay.organizer.data.db.DatabaseChangeListener;
import com.eventyay.organizer.data.db.DbFlowDatabaseChangeListener;
import com.eventyay.organizer.data.ticket.Ticket;
import com.eventyay.organizer.data.ticket.TicketRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.eventyay.organizer.common.rx.ViewTransformers.dispose;

public class TicketsViewModel extends ViewModel {

    private final MutableLiveData<List<Ticket>> tickets = new MutableLiveData<>();
    private final TicketRepository ticketRepository;
    private final DatabaseChangeListener<Ticket> ticketChangeListener;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final MutableLiveData<Boolean> progress = new MutableLiveData<>();
    private final MutableLiveData<Ticket> ticket = new MutableLiveData<>();

    private long eventId = -1;

    @Inject
    public TicketsViewModel(TicketRepository ticketRepository, DatabaseChangeListener<Ticket> ticketChangeListener) {
        this.ticketRepository = ticketRepository;
        this.ticketChangeListener = ticketChangeListener;
    }

    public void start(long eventId) {
        listenChanges();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
        ticketChangeListener.stopListening();
    }

    private void listenChanges() {
        ticketChangeListener.startListening();
        compositeDisposable.add(
            ticketChangeListener.getNotifier()
                .map(DbFlowDatabaseChangeListener.ModelChange::getAction)
                .filter(action -> action.equals(BaseModel.Action.INSERT))
                .subscribeOn(Schedulers.io())
                .subscribe(ticketModelChange -> loadTickets(eventId, false), Logger::logError));
    }

    @SuppressLint("CheckResult")
    public LiveData<List<Ticket>> loadTickets(long eventId, boolean forceReload) {
        getTicketSource(eventId, forceReload)
            .compose(dispose(compositeDisposable))
            .doOnSubscribe(disposable -> progress.setValue(true))
            .doFinally(() -> progress.setValue(false))
            .toSortedList()
            .subscribe(tickets::setValue, Logger::logError);

        return tickets;
    }

    private Observable<Ticket> getTicketSource(long eventId, boolean forceReload) {
        if (!forceReload && !tickets.getValue().isEmpty())
            return Observable.fromIterable(tickets.getValue());
        else
            return ticketRepository.getTickets(eventId, forceReload);
    }

//    @SuppressLint("CheckResult")
//    public void deleteTicket(Ticket ticket) {
//        ticketRepository
//            .deleteTicket(ticket.getId())
//            .compose(disposeCompletable(getDisposable()))
//            .compose(progressiveErroneousCompletable(getView()))
//            .subscribe(() -> {
//                getView().showTicketDeleted("Ticket Deleted. Refreshing Items");
//                loadTickets(true);
//            }, Logger::logError);
//    }

    public void singleClick(Ticket ticketDetail) {
        ticket.setValue(ticketDetail);
    }

    public LiveData<Ticket> getTicket() {
        return ticket;
    }

    public LiveData<List<Ticket>> getTickets() {
        return tickets;
    }

}
