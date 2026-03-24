package com.pedromoura.laprobqi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.models.SolicitacaoCoordenador;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SolicitacaoAdapter extends RecyclerView.Adapter<SolicitacaoAdapter.ViewHolder> {
    
    private Context context;
    private List<SolicitacaoCoordenador> solicitacoes;
    private OnAprovarClickListener onAprovarClick;
    private OnRejeitarClickListener onRejeitarClick;
    
    public interface OnAprovarClickListener {
        void onAprovar(SolicitacaoCoordenador solicitacao);
    }
    
    public interface OnRejeitarClickListener {
        void onRejeitar(SolicitacaoCoordenador solicitacao);
    }
    
    public SolicitacaoAdapter(Context context, List<SolicitacaoCoordenador> solicitacoes,
                             OnAprovarClickListener onAprovarClick,
                             OnRejeitarClickListener onRejeitarClick) {
        this.context = context;
        this.solicitacoes = solicitacoes;
        this.onAprovarClick = onAprovarClick;
        this.onRejeitarClick = onRejeitarClick;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_solicitacao, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SolicitacaoCoordenador solicitacao = solicitacoes.get(position);
        
        holder.txtNome.setText(solicitacao.getNome());
        holder.txtEmail.setText(solicitacao.getEmail());
        holder.txtData.setText(formatarData(solicitacao.getDataSolicitacao()));
        
        holder.btnAprovar.setOnClickListener(v -> {
            if (onAprovarClick != null) {
                onAprovarClick.onAprovar(solicitacao);
            }
        });
        
        holder.btnRejeitar.setOnClickListener(v -> {
            if (onRejeitarClick != null) {
                onRejeitarClick.onRejeitar(solicitacao);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return solicitacoes.size();
    }
    
    private String formatarData(String timestamp) {
        try {
            long time = Long.parseLong(timestamp);
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtEmail, txtData;
        Button btnAprovar, btnRejeitar;
        
        ViewHolder(View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtData = itemView.findViewById(R.id.txtData);
            btnAprovar = itemView.findViewById(R.id.btnAprovar);
            btnRejeitar = itemView.findViewById(R.id.btnRejeitar);
        }
    }
}
