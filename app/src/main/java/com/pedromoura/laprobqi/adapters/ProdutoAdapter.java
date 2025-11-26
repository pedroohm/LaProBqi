package com.pedromoura.laprobqi.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.repository.ProdutoRepository;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private List<Produto> produtos;
    private Context context;
    private ProdutoRepository produtoRepository;
    private boolean isExitMode; // true = exit mode, false = entry mode
    
    // Formatador para exibir números com ponto e no máximo 3 casas decimais
    private static final DecimalFormat decimalFormat;
    
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        decimalFormat = new DecimalFormat("#,##0.###", symbols);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(0);
    }

    public ProdutoAdapter(List<Produto> produtos, Context context, boolean isExitMode) {
        this.produtos = produtos;
        this.context = context;
        this.produtoRepository = RepositoryProvider.getInstance(context).getProdutoRepository();
        this.isExitMode = isExitMode;
    }
    
    /**
     * Formata um número com ponto como separador decimal e no máximo 3 casas
     */
    private static String formatarNumero(double valor) {
        return decimalFormat.format(valor);
    }
    
    /**
     * Converte string com ponto para double (formato padrão)
     */
    private static double parseNumero(String texto) throws NumberFormatException {
        // Aceita apenas ponto como separador decimal
        String textoNormalizado = texto.trim();
        return Double.parseDouble(textoNormalizado);
    }

    public void setExitMode(boolean exitMode) {
        isExitMode = exitMode;
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

            setupButtons();
        }

        private void setupButtons() {
            // Modo estoque: botões para adicionar e remover quantidade
            if (isExitMode) {
                // Modo EXIT: qualquer usuário pode remover (retirar do estoque)
                btnAdicionar.setVisibility(View.GONE);
                btnRemover.setVisibility(View.VISIBLE);
                btnRemover.setText("-");
                btnRemover.setOnClickListener(v -> removerQuantidade());
            } else {
                btnAdicionar.setVisibility(View.VISIBLE);
                btnRemover.setVisibility(View.GONE);
                btnAdicionar.setText("+");
                btnAdicionar.setOnClickListener(v -> adicionarQuantidade());
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
                        produtoRepository.removerProduto(produto.getId(), new ProdutoRepository.OnCompleteListener() {
                            @Override
                            public void onComplete(boolean sucesso, String mensagem) {
                                if (sucesso) {
                                    produtos.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Produto excluído: " + produto.getNome(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Erro ao excluir: " + mensagem, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
            
            // Configurar EditText para aceitar números decimais
            editQuantidade.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            
            String acao = adicionar ? "Adicionar" : "Remover";
            txtTitulo.setText(acao + " quantidade - " + produto.getNome());
            editQuantidade.setHint("Quantidade a " + acao.toLowerCase() + " (use ponto, ex: 10.5)");

            new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(acao, (dialog, which) -> {
                    try {
                        // Usar o método parseNumero que aceita vírgula
                        double quantidade = parseNumero(editQuantidade.getText().toString());
                        
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
                        
                        // Atualizar no Firebase via Repository
                        produtoRepository.atualizarProduto(produto, new ProdutoRepository.OnCompleteListener() {
                            @Override
                            public void onComplete(boolean sucesso, String mensagem) {
                                if (sucesso) {
                                    notifyItemChanged(getAdapterPosition());
                                    // Exibir mensagem com formatação (ponto como separador decimal)
                                    String msg = acao + " " + formatarNumero(quantidade) + " " + produto.getUnidade() + 
                                                " de " + produto.getNome() + ". Novo total: " + formatarNumero(novaQuantidade) + " " + produto.getUnidade();
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Erro ao atualizar: " + mensagem, Toast.LENGTH_SHORT).show();
                                    // Reverter mudança local em caso de erro
                                    produto.setQuantidade(adicionar ? novaQuantidade - quantidade : novaQuantidade + quantidade);
                                    notifyItemChanged(getAdapterPosition());
                                }
                            }
                        });
                        
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Digite uma quantidade válida (use ponto para decimais, ex: 10.5)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
        }

        public void bind(Produto produto) {
            txtNome.setText("Nome: " + produto.getNome());
            txtTipo.setText("Tipo: " + produto.getTipo());
            // Exibir quantidade com formatação (ponto como separador decimal, máx 3 decimais)
            txtQuantidade.setText("Quantidade: " + formatarNumero(produto.getQuantidade()) + " " + produto.getUnidade());
            txtValidade.setText("Validade: " + produto.getValidade());
            txtObservacoes.setText("Obs: " + produto.getObservacoes());

            setupButtons();
        }
    }
}