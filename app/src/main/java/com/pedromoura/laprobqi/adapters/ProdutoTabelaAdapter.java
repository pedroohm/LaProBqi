package com.pedromoura.laprobqi.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProdutoTabelaAdapter extends RecyclerView.Adapter<ProdutoTabelaAdapter.ProdutoViewHolder> {

    private List<Produto> produtos;
    private Context context;
    private SimpleDateFormat dateFormatInput;
    private SimpleDateFormat dateFormatOutput;

    public ProdutoTabelaAdapter(List<Produto> produtos, Context context) {
        this.produtos = produtos;
        this.context = context;
        this.dateFormatInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.dateFormatOutput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_produto_tabela, parent, false);
        return new ProdutoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
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

    class ProdutoViewHolder extends RecyclerView.ViewHolder {
        private TextView txtNome, txtCodigo, txtQuantidade, txtValidade;

        public ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtProdutoNome);
            txtCodigo = itemView.findViewById(R.id.txtProdutoCodigo);
            txtQuantidade = itemView.findViewById(R.id.txtProdutoQuantidade);
            txtValidade = itemView.findViewById(R.id.txtProdutoValidade);
        }

        public void bind(Produto produto, int position) {
            // Alterna cores das linhas para melhor legibilidade
            View contentLayout = itemView.findViewById(R.id.contentLayout);
            if (contentLayout != null) {
                if (position % 2 == 0) {
                    contentLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));
                } else {
                    contentLayout.setBackgroundColor(Color.WHITE);
                }
            }

            txtNome.setText(produto.getNome());
            txtCodigo.setText(String.valueOf(produto.getId()));
            
            // Formatar quantidade com unidade
            String quantidadeTexto;
            if (produto.getQuantidade() % 1 == 0) {
                // Se for número inteiro, mostra sem casas decimais
                quantidadeTexto = String.format("%d %s", 
                    (int) produto.getQuantidade(), 
                    produto.getUnidade() != null ? produto.getUnidade() : "un");
            } else {
                // Se tiver decimais, mostra com 1 casa decimal
                quantidadeTexto = String.format("%.1f %s", 
                    produto.getQuantidade(), 
                    produto.getUnidade() != null ? produto.getUnidade() : "un");
            }
            txtQuantidade.setText(quantidadeTexto);
            
            // Exibir validade formatada
            String validade = produto.getValidade();
            if (validade != null && !validade.isEmpty()) {
                try {
                    Date data = dateFormatInput.parse(validade);
                    txtValidade.setText(dateFormatOutput.format(data));
                } catch (ParseException e) {
                    txtValidade.setText(validade);
                }
            } else {
                txtValidade.setText("N/A");
            }
        }
    }
}
