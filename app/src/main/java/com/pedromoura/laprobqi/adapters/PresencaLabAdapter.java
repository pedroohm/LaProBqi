package com.pedromoura.laprobqi.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.models.PresencaLab;

import java.util.List;

public class PresencaLabAdapter extends RecyclerView.Adapter<PresencaLabAdapter.PresencaViewHolder> {
    
    private List<PresencaLab> presencas;
    private Context context;
    
    public PresencaLabAdapter(Context context, List<PresencaLab> presencas) {
        this.context = context;
        this.presencas = presencas;
    }
    
    @NonNull
    @Override
    public PresencaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_presenca_lab, parent, false);
        return new PresencaViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PresencaViewHolder holder, int position) {
        PresencaLab presenca = presencas.get(position);
        holder.bind(presenca);
    }
    
    @Override
    public int getItemCount() {
        return presencas.size();
    }
    
    public void atualizarLista(List<PresencaLab> novaLista) {
        this.presencas = novaLista;
        notifyDataSetChanged();
    }
    
    static class PresencaViewHolder extends RecyclerView.ViewHolder {
        private TextView txtStatusIcon, txtUsuarioNome, txtDataEntrada, txtHorarios, txtStatus;
        
        public PresencaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStatusIcon = itemView.findViewById(R.id.txtStatusIcon);
            txtUsuarioNome = itemView.findViewById(R.id.txtUsuarioNome);
            txtDataEntrada = itemView.findViewById(R.id.txtDataEntrada);
            txtHorarios = itemView.findViewById(R.id.txtHorarios);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
        
        public void bind(PresencaLab presenca) {
            // Nome do usuário
            String nome = presenca.getUsuarioNome();
            txtUsuarioNome.setText(nome != null && !nome.isEmpty() ? nome : "Nome não disponível");
            
            // Formatar data (yyyy-MM-dd -> dd/MM/yyyy)
            String dataFormatada = formatarData(presenca.getDataEntrada());
            txtDataEntrada.setText(dataFormatada);
            
            // Ícone de status
            if (presenca.isPresente()) {
                txtStatusIcon.setText("🟢");
                txtStatus.setText("PRESENTE");
                txtStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
            } else {
                txtStatusIcon.setText("🔴");
                txtStatus.setText("SAIU");
                txtStatus.setBackgroundColor(Color.parseColor("#880E4F")); // Vinho
            }
            
            // Horários
            String horarios = "Entrada: " + presenca.getHoraEntrada();
            if (presenca.getHoraSaida() != null) {
                horarios += " • Saída: " + presenca.getHoraSaida();
            }
            txtHorarios.setText(horarios);
        }
        
        private String formatarData(String data) {
            if (data == null || data.isEmpty()) return "";
            try {
                String[] partes = data.split("-");
                if (partes.length == 3) {
                    return partes[2] + "/" + partes[1] + "/" + partes[0];
                }
            } catch (Exception e) {
                // Retorna data original em caso de erro
            }
            return data;
        }
    }
}
