package com.pedromoura.laprobqi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pedromoura.laprobqi.models.Equipamento;

import java.util.List;

public class EquipamentoAdapter extends BaseAdapter {
    
    private Context context;
    private List<Equipamento> equipamentos;
    private LayoutInflater inflater;
    
    public EquipamentoAdapter(Context context, List<Equipamento> equipamentos) {
        this.context = context;
        this.equipamentos = equipamentos;
        this.inflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        return equipamentos.size();
    }
    
    @Override
    public Object getItem(int position) {
        return equipamentos.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            holder = new ViewHolder();
            holder.textNome = convertView.findViewById(android.R.id.text1);
            holder.textDescricao = convertView.findViewById(android.R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Equipamento equipamento = equipamentos.get(position);
        holder.textNome.setText(equipamento.getNome());
        holder.textNome.setTextColor(android.graphics.Color.BLACK); // Forçar cor preta para o nome
        holder.textDescricao.setText(equipamento.getDescricao() + " - " + equipamento.getStatusDisplay());
        
        // Mudar cor do texto baseado no status
        int textColor = context.getResources().getColor(R.color.text_secondary);
        switch (equipamento.getStatus()) {
            case "DISPONIVEL":
                textColor = context.getResources().getColor(R.color.status_disponivel);
                break;
            case "RESERVADO":
                textColor = context.getResources().getColor(R.color.status_reservado);
                break;
            case "EM_USO":
                textColor = context.getResources().getColor(R.color.status_em_uso);
                break;
        }
        holder.textDescricao.setTextColor(textColor);
        
        return convertView;
    }
    
    public void atualizarLista(List<Equipamento> novaLista) {
        this.equipamentos = novaLista;
        notifyDataSetChanged();
    }
    
    private static class ViewHolder {
        TextView textNome;
        TextView textDescricao;
    }
}
