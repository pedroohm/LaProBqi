package com.pedromoura.laprobqi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.models.RegistroUso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RegistroUsoAdapter extends RecyclerView.Adapter<RegistroUsoAdapter.RegistroUsoViewHolder> {

    private final List<RegistroUso> registros;
    private final LayoutInflater inflater;
    private final SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat sdfSaida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public RegistroUsoAdapter(Context context, List<RegistroUso> registros) {
        this.inflater = LayoutInflater.from(context);
        this.registros = registros;
    }

    @NonNull
    @Override
    public RegistroUsoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_registro_uso, parent, false);
        return new RegistroUsoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroUsoViewHolder holder, int position) {
        RegistroUso registro = registros.get(position);
        holder.bind(registro);
    }

    @Override
    public int getItemCount() {
        return registros.size();
    }

    class RegistroUsoViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtNomeEquipamento;
        private final TextView txtNomeUsuario;
        private final TextView txtDataHoraInicio;
        private final TextView txtDataHoraFim;
        private final TextView txtDuracao;

        public RegistroUsoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeEquipamento = itemView.findViewById(R.id.txtNomeEquipamento);
            txtNomeUsuario = itemView.findViewById(R.id.txtNomeUsuario);
            txtDataHoraInicio = itemView.findViewById(R.id.txtDataHoraInicio);
            txtDataHoraFim = itemView.findViewById(R.id.txtDataHoraFim);
            txtDuracao = itemView.findViewById(R.id.txtDuracao);
        }

        public void bind(RegistroUso registro) {
            txtNomeEquipamento.setText(registro.getEquipamentoNome());
            txtNomeUsuario.setText("Usuário: " + registro.getUsuarioNome());

            try {
                String dataHoraInicioStr = registro.getDataInicio() + " " + registro.getHoraInicio();
                Date dataInicio = sdfEntrada.parse(dataHoraInicioStr);
                txtDataHoraInicio.setText("Início: " + sdfSaida.format(dataInicio));

                if (registro.getDataFim() != null && !registro.getDataFim().isEmpty() && 
                    registro.getHoraFim() != null && !registro.getHoraFim().isEmpty()) {
                    String dataHoraFimStr = registro.getDataFim() + " " + registro.getHoraFim();
                    Date dataFim = sdfEntrada.parse(dataHoraFimStr);
                    txtDataHoraFim.setText("Fim: " + sdfSaida.format(dataFim));
                    txtDataHoraFim.setVisibility(View.VISIBLE);
                    txtDuracao.setText("Duração: " + calcularDuracao(dataInicio, dataFim));
                    txtDuracao.setVisibility(View.VISIBLE);
                } else {
                    txtDataHoraFim.setVisibility(View.GONE);
                    txtDuracao.setText("Duração: Em uso");
                    txtDuracao.setVisibility(View.VISIBLE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                txtDataHoraInicio.setText("Início: Data inválida");
                txtDataHoraFim.setVisibility(View.GONE);
                txtDuracao.setVisibility(View.GONE);
            }
        }

        private String calcularDuracao(Date inicio, Date fim) {
            long diff = fim.getTime() - inicio.getTime();
            long horas = TimeUnit.MILLISECONDS.toHours(diff);
            long minutos = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
            return String.format(Locale.getDefault(), "%dh %02dm", horas, minutos);
        }
    }
}
