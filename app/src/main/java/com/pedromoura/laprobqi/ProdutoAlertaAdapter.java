package com.pedromoura.laprobqi;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProdutoAlertaAdapter extends RecyclerView.Adapter<ProdutoAlertaAdapter.AlertaViewHolder> {

    private List<Produto> produtos;
    private Context context;
    private SimpleDateFormat dateFormat;

    public ProdutoAlertaAdapter(List<Produto> produtos, Context context) {
        this.produtos = produtos;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    @NonNull
    @Override
    public AlertaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alerta_produto, parent, false);
        return new AlertaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertaViewHolder holder, int position) {
        Produto produto = produtos.get(position);
        holder.bind(produto, position);
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public void atualizarLista(List<Produto> novaLista) {
        this.produtos = novaLista;
        notifyDataSetChanged();
    }

    class AlertaViewHolder extends RecyclerView.ViewHolder {
        private TextView txtNome, txtCodigo, txtQuantidade, txtValidade, txtDiasRestantes;

        public AlertaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtProdutoNome);
            txtCodigo = itemView.findViewById(R.id.txtProdutoCodigo);
            txtQuantidade = itemView.findViewById(R.id.txtProdutoQuantidade);
            txtValidade = itemView.findViewById(R.id.txtProdutoValidade);
            txtDiasRestantes = itemView.findViewById(R.id.txtDiasRestantes);
        }

        public void bind(Produto produto, int position) {
            View contentLayout = itemView.findViewById(R.id.contentLayout);
            
            txtNome.setText(produto.getNome());
            txtCodigo.setText(String.valueOf(produto.getId()));
            
            // Formatar quantidade com unidade
            String quantidadeTexto;
            if (produto.getQuantidade() % 1 == 0) {
                quantidadeTexto = String.format("%d %s", 
                    (int) produto.getQuantidade(), 
                    produto.getUnidade() != null ? produto.getUnidade() : "un");
            } else {
                quantidadeTexto = String.format("%.1f %s", 
                    produto.getQuantidade(), 
                    produto.getUnidade() != null ? produto.getUnidade() : "un");
            }
            txtQuantidade.setText(quantidadeTexto);
            
            // Processar validade e calcular dias restantes
            String validade = produto.getValidade();
            if (validade != null && !validade.isEmpty()) {
                try {
                    Date dataValidade = dateFormat.parse(validade);
                    Date hoje = new Date();
                    
                    long diferenca = dataValidade.getTime() - hoje.getTime();
                    long diasRestantes = TimeUnit.MILLISECONDS.toDays(diferenca);
                    
                    // Formatar data para exibição
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    txtValidade.setText(displayFormat.format(dataValidade));
                    
                    // Exibir dias restantes
                    if (diasRestantes < 0) {
                        txtDiasRestantes.setText("VENCIDO");
                        txtDiasRestantes.setTextColor(Color.RED);
                        contentLayout.setBackgroundColor(Color.parseColor("#FFE0E0"));
                    } else if (diasRestantes == 0) {
                        txtDiasRestantes.setText("VENCE HOJE");
                        txtDiasRestantes.setTextColor(Color.RED);
                        contentLayout.setBackgroundColor(Color.parseColor("#FFE0E0"));
                    } else if (diasRestantes == 1) {
                        txtDiasRestantes.setText("1 dia");
                        txtDiasRestantes.setTextColor(Color.parseColor("#FF6B00"));
                        contentLayout.setBackgroundColor(Color.parseColor("#FFF3E0"));
                    } else if (diasRestantes <= 7) {
                        txtDiasRestantes.setText(diasRestantes + " dias");
                        txtDiasRestantes.setTextColor(Color.parseColor("#FF6B00"));
                        contentLayout.setBackgroundColor(Color.parseColor("#FFF3E0"));
                    } else if (diasRestantes <= 15) {
                        txtDiasRestantes.setText(diasRestantes + " dias");
                        txtDiasRestantes.setTextColor(Color.parseColor("#FFA500"));
                        contentLayout.setBackgroundColor(Color.parseColor("#FFF9E6"));
                    } else {
                        txtDiasRestantes.setText(diasRestantes + " dias");
                        txtDiasRestantes.setTextColor(Color.parseColor("#666666"));
                        if (position % 2 == 0) {
                            contentLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));
                        } else {
                            contentLayout.setBackgroundColor(Color.WHITE);
                        }
                    }
                    
                } catch (ParseException e) {
                    txtValidade.setText(validade);
                    txtDiasRestantes.setText("Erro");
                    txtDiasRestantes.setTextColor(Color.GRAY);
                }
            } else {
                txtValidade.setText("N/A");
                txtDiasRestantes.setText("");
            }
        }
    }
}
