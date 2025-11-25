package com.pedromoura.laprobqi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.models.LogReserva;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LogReservaAdapter extends RecyclerView.Adapter<LogReservaAdapter.LogViewHolder> {

    private Context context;
    private List<LogReserva> logs;
    private SimpleDateFormat sdfDateTime;
    private SimpleDateFormat sdfDateTimeShort;

    public LogReservaAdapter(Context context, List<LogReserva> logs) {
        this.context = context;
        this.logs = logs;
        this.sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        this.sdfDateTimeShort = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log_reserva, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogReserva log = logs.get(position);

        holder.txtEquipamentoNome.setText(log.getEquipamentoNome());
        holder.txtStatus.setText(log.getStatusDisplay());
        holder.txtUsuarioNome.setText(log.getUsuarioNome());
        holder.txtUsuarioEmail.setText(log.getUsuarioEmail());

        if (log.getDataHoraInicio() != null) {
            holder.txtDataHoraInicio.setText(sdfDateTime.format(log.getDataHoraInicio()));
        }

        if (log.getDataHoraFim() != null) {
            holder.txtDataHoraFim.setText(sdfDateTime.format(log.getDataHoraFim()));
        }

        if (log.getDataHoraReserva() != null) {
            holder.txtDataHoraReserva.setText(sdfDateTimeShort.format(log.getDataHoraReserva()));
        }

        // Observação
        if (log.getObservacao() != null && !log.getObservacao().isEmpty()) {
            holder.txtObservacao.setVisibility(View.VISIBLE);
            holder.txtObservacao.setText("Obs: " + log.getObservacao());
        } else {
            holder.txtObservacao.setVisibility(View.GONE);
        }

        // Informações de cancelamento
        if (log.isCancelada()) {
            holder.layoutCancelamento.setVisibility(View.VISIBLE);
            
            if (log.getMotivoCancelamento() != null && !log.getMotivoCancelamento().isEmpty()) {
                holder.txtMotivoCancelamento.setText(log.getMotivoCancelamento());
            } else {
                holder.txtMotivoCancelamento.setText("Sem motivo informado");
            }

            if (log.getDataHoraCancelamento() != null) {
                holder.txtDataCancelamento.setText("Cancelado em: " + 
                        sdfDateTime.format(log.getDataHoraCancelamento()));
            }
        } else {
            holder.layoutCancelamento.setVisibility(View.GONE);
        }

        // Colorir card baseado no status
        int backgroundColor;
        switch (log.getStatus()) {
            case "ATIVA":
                backgroundColor = 0xFFE8F5E9; // Verde claro
                break;
            case "CONCLUIDA":
                backgroundColor = 0xFFE3F2FD; // Azul claro
                break;
            case "CANCELADA":
                backgroundColor = 0xFFFFEBEE; // Vermelho claro
                break;
            default:
                backgroundColor = 0xFFFFFFFF; // Branco
                break;
        }
        holder.itemView.setBackgroundColor(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView txtEquipamentoNome;
        TextView txtStatus;
        TextView txtUsuarioNome;
        TextView txtUsuarioEmail;
        TextView txtDataHoraInicio;
        TextView txtDataHoraFim;
        TextView txtDataHoraReserva;
        TextView txtObservacao;
        LinearLayout layoutCancelamento;
        TextView txtMotivoCancelamento;
        TextView txtDataCancelamento;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEquipamentoNome = itemView.findViewById(R.id.txtEquipamentoNome);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtUsuarioNome = itemView.findViewById(R.id.txtUsuarioNome);
            txtUsuarioEmail = itemView.findViewById(R.id.txtUsuarioEmail);
            txtDataHoraInicio = itemView.findViewById(R.id.txtDataHoraInicio);
            txtDataHoraFim = itemView.findViewById(R.id.txtDataHoraFim);
            txtDataHoraReserva = itemView.findViewById(R.id.txtDataHoraReserva);
            txtObservacao = itemView.findViewById(R.id.txtObservacao);
            layoutCancelamento = itemView.findViewById(R.id.layoutCancelamento);
            txtMotivoCancelamento = itemView.findViewById(R.id.txtMotivoCancelamento);
            txtDataCancelamento = itemView.findViewById(R.id.txtDataCancelamento);
        }
    }
}
