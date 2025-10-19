package com.pedromoura.laprobqi;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private List<Produto> produtos;
    private Context context;
    private BancoDadosProduto banco;
    private boolean modoCatalogo; // true = catálogo, false = estoque

    public ProdutoAdapter(List<Produto> produtos, Context context, boolean modoCatalogo) {
        this.produtos = produtos;
        this.context = context;
        this.banco = BancoDadosProduto.getInstancia(context);
        this.modoCatalogo = modoCatalogo;
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_produto, parent, false);
        return new ProdutoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        Produto produto = produtos.get(position);
        holder.bind(produto);
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
        private TextView txtNome, txtTipo, txtQuantidade, txtValidade, txtObservacoes;
        private Button btnAdicionar, btnRemover;

        public ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtQuantidade = itemView.findViewById(R.id.txtQuantidade);
            txtValidade = itemView.findViewById(R.id.txtValidade);
            txtObservacoes = itemView.findViewById(R.id.txtObservacoes);
            btnAdicionar = itemView.findViewById(R.id.btnAdicionar);
            btnRemover = itemView.findViewById(R.id.btnRemover);

            // Configurar visibilidade dos botões baseado no modo
            if (modoCatalogo) {
                // Modo catálogo: botões para editar e deletar
                btnAdicionar.setText("Editar");
                btnRemover.setText("Excluir");
                btnAdicionar.setVisibility(View.VISIBLE);
                btnRemover.setVisibility(View.VISIBLE);
                
                btnAdicionar.setOnClickListener(v -> editarProduto());
                btnRemover.setOnClickListener(v -> excluirProduto());
                
            } else {
                // Modo estoque: botões para adicionar e remover quantidade
                btnAdicionar.setText("+");
                btnRemover.setText("-");
                btnAdicionar.setVisibility(View.VISIBLE);
                btnRemover.setVisibility(View.VISIBLE);
                
                btnAdicionar.setOnClickListener(v -> adicionarQuantidade());
                btnRemover.setOnClickListener(v -> removerQuantidade());
            }
        }

        private void editarProduto() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Produto produto = produtos.get(position);
                Toast.makeText(context, "Editar: " + produto.getNome(), Toast.LENGTH_SHORT).show();
                // Aqui você pode abrir uma activity de edição
            }
        }

        private void excluirProduto() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Produto produto = produtos.get(position);
                
                new AlertDialog.Builder(context)
                    .setTitle("Confirmar exclusão")
                    .setMessage("Deseja realmente excluir o produto '" + produto.getNome() + "'?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        banco.deletarProduto(produto.getId());
                        produtos.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Produto excluído: " + produto.getNome(), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Não", null)
                    .show();
            }
        }

        private void adicionarQuantidade() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Produto produto = produtos.get(position);
                mostrarDialogoQuantidade(produto, true);
            }
        }

        private void removerQuantidade() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Produto produto = produtos.get(position);
                mostrarDialogoQuantidade(produto, false);
            }
        }

        private void mostrarDialogoQuantidade(Produto produto, boolean adicionar) {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_quantidade, null);
            EditText editQuantidade = dialogView.findViewById(R.id.editQuantidade);
            TextView txtTitulo = dialogView.findViewById(R.id.txtTitulo);
            
            String acao = adicionar ? "Adicionar" : "Remover";
            txtTitulo.setText(acao + " quantidade - " + produto.getNome());
            editQuantidade.setHint("Quantidade a " + acao.toLowerCase());

            new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(acao, (dialog, which) -> {
                    try {
                        double quantidade = Double.parseDouble(editQuantidade.getText().toString());
                        if (quantidade <= 0) {
                            Toast.makeText(context, "Quantidade deve ser maior que zero", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        double novaQuantidade;
                        if (adicionar) {
                            novaQuantidade = produto.getQuantidade() + quantidade;
                        } else {
                            novaQuantidade = produto.getQuantidade() - quantidade;
                            if (novaQuantidade < 0) {
                                Toast.makeText(context, "Quantidade insuficiente em estoque", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        
                        produto.setQuantidade(novaQuantidade);
                        banco.atualizarProduto(produto);
                        notifyItemChanged(getAdapterPosition());
                        
                        String mensagem = acao + " " + quantidade + " " + produto.getUnidade() + 
                                        " de " + produto.getNome() + ". Novo total: " + novaQuantidade + " " + produto.getUnidade();
                        Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show();
                        
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Digite uma quantidade válida", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
        }

        public void bind(Produto produto) {
            txtNome.setText("Nome: " + produto.getNome());
            txtTipo.setText("Tipo: " + produto.getTipo());
            txtQuantidade.setText("Quantidade: " + produto.getQuantidade() + " " + produto.getUnidade());
            txtValidade.setText("Validade: " + produto.getValidade());
            txtObservacoes.setText("Obs: " + produto.getObservacoes());
        }
    }
} 