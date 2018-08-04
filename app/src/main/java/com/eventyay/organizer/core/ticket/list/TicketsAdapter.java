package com.eventyay.organizer.core.ticket.list;

import android.databinding.DataBindingUtil;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import com.eventyay.organizer.R;
import com.eventyay.organizer.core.ticket.list.viewholder.TicketViewHolder;
import com.eventyay.organizer.data.ticket.Ticket;
import com.eventyay.organizer.databinding.HeaderLayoutBinding;
import com.eventyay.organizer.ui.HeaderViewHolder;

import java.util.List;

public class TicketsAdapter extends RecyclerView.Adapter<TicketViewHolder> implements StickyRecyclerHeadersAdapter<HeaderViewHolder> {

    private List<Ticket> tickets;
    private final TicketsViewModel ticketsViewModel;

    public TicketsAdapter(TicketsViewModel ticketsViewModel) {
        this.ticketsViewModel = ticketsViewModel;
    }

    @Override
    public TicketViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        TicketViewHolder ticketViewHolder = new TicketViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
            R.layout.ticket_layout, viewGroup, false));
//        ticketViewHolder.setDeleteAction(ticketsViewModel::deleteTicket);
        ticketViewHolder.setClickAction(ticketsViewModel::singleClick);

        return ticketViewHolder;
    }

    @Override
    public void onBindViewHolder(TicketViewHolder ticketViewHolder, int position) {
        ticketViewHolder.bind(tickets.get(position));
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        return new HeaderViewHolder(HeaderLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder headerViewHolder, int position) {
        headerViewHolder.bindHeader(tickets.get(position).getType());
    }

    @Override
    public long getHeaderId(int position) {
        return tickets.get(position).getType().hashCode();
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis") // Inevitable DU Anomaly
    protected void setTickets(final List<Ticket> newTickets) {
        if (tickets == null) {
            tickets = newTickets;
            notifyItemRangeInserted(0, newTickets.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return tickets.size();
                }

                @Override
                public int getNewListSize() {
                    return newTickets.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return tickets.get(oldItemPosition).getId()
                        .equals(newTickets.get(newItemPosition).getId());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return tickets.get(oldItemPosition).equals(newTickets.get(newItemPosition));
                }
            });
            tickets = newTickets;
            result.dispatchUpdatesTo(this);
        }
    }
}
