package com.eventyay.organizer.core.ticket.list;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eventyay.organizer.core.orders.list.OrdersViewModel;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import com.eventyay.organizer.R;
import com.eventyay.organizer.common.mvp.view.BaseFragment;
import com.eventyay.organizer.core.main.MainActivity;
import com.eventyay.organizer.core.ticket.create.CreateTicketFragment;
import com.eventyay.organizer.core.ticket.detail.TicketDetailFragment;
import com.eventyay.organizer.data.ContextUtils;
import com.eventyay.organizer.data.ticket.Ticket;
import com.eventyay.organizer.databinding.TicketsFragmentBinding;
import com.eventyay.organizer.ui.ViewUtils;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;

public class TicketsFragment extends BaseFragment implements TicketsView {

    private Context context;
    private long eventId;

    @Inject
    ContextUtils utilModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private TicketsAdapter ticketsAdapter;
    private RecyclerView.AdapterDataObserver adapterDataObserver;
    private TicketsFragmentBinding binding;
    private SwipeRefreshLayout refreshLayout;
    private TicketsViewModel ticketsViewModel;

    public static TicketsFragment newInstance(long eventId) {
        TicketsFragment fragment = new TicketsFragment();
        Bundle args = new Bundle();
        args.putLong(MainActivity.EVENT_KEY, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();
        if (getArguments() != null)
            eventId = getArguments().getLong(MainActivity.EVENT_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.tickets_fragment, container, false);
        binding.createTicketFab.setOnClickListener(view -> {
//            openCreateTicketFragment();
       });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        setupRecyclerView();
        setupRefreshListener();
        ticketsViewModel = ViewModelProviders.of(this, viewModelFactory).get(TicketsViewModel.class);
        ticketsViewModel.getTicket().observe(this, this::openCreateTicketFragment);
        loadTickets();
    }

    private void loadTickets() {
        ticketsViewModel.getTickets().observe(this, this::showResults);
    }

    @Override
    protected int getTitle() {
        return R.string.tickets;
    }

    @Override
    public void onStop() {
        super.onStop();
        refreshLayout.setOnRefreshListener(null);
        ticketsAdapter.unregisterAdapterDataObserver(adapterDataObserver);
    }

    public void openCreateTicketFragment(Ticket ticket) {
        getFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, CreateTicketFragment.newInstance())
            .addToBackStack(null)
            .commit();
    }

    private void setupRecyclerView() {
        ticketsAdapter = new TicketsAdapter(ticketsViewModel);

        RecyclerView recyclerView = binding.ticketsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(ticketsAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        StickyRecyclerHeadersDecoration decoration = new StickyRecyclerHeadersDecoration(ticketsAdapter);
        recyclerView.addItemDecoration(decoration);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        adapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                decoration.invalidateHeaders();
            }
        };

        ViewUtils.setRecyclerViewScrollAwareFabBehaviour(recyclerView, binding.createTicketFab);

        ticketsAdapter.registerAdapterDataObserver(adapterDataObserver);
    }

    private void setupRefreshListener() {
        refreshLayout = binding.swipeContainer;
        refreshLayout.setColorSchemeColors(utilModel.getResourceColor(R.color.color_accent));
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            ticketsViewModel.loadTickets(true);
        });
    }

    @Override
    public void showError(String error) {
        ViewUtils.showSnackbar(binding.getRoot(), error);
    }

    @Override
    public void showProgress(boolean show) {
        ViewUtils.showView(binding.progressBar, show);
    }

    @Override
    public void onRefreshComplete(boolean success) {
        if (success)
            ViewUtils.showSnackbar(binding.ticketsRecyclerView, R.string.refresh_complete);
    }

    @Override
    public void showResults(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            showEmptyView(true);
            return;
        }

        showEmptyView(false);
        ticketsAdapter.setTickets(tickets);
    }

    @Override
    public void showEmptyView(boolean show) {
        ViewUtils.showView(binding.emptyView, show);
    }

    @Override
    public void showTicketDeleted(String message) {
        ViewUtils.showSnackbar(binding.ticketsRecyclerView, message);
    }

    @Override
    public void openTicketDetailFragment(long ticketId) {
        BottomSheetDialogFragment bottomSheetDialogFragment = TicketDetailFragment.newInstance(ticketId);
        bottomSheetDialogFragment.show(getFragmentManager(), bottomSheetDialogFragment.getTag());
    }
}
