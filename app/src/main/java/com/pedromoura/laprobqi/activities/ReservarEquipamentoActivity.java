package com.pedromoura.laprobqi.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.models.Equipamento;
import com.pedromoura.laprobqi.models.LogReserva;
import com.pedromoura.laprobqi.models.Reserva;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.EquipamentoRepository;
import com.pedromoura.laprobqi.repository.ReservaRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;
import com.pedromoura.laprobqi.repositories.LogReservaRepository;
import com.pedromoura.laprobqi.repositories.LogReservaRepositoryFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservarEquipamentoActivity extends AppCompatActivity {

    private ListView listViewEquipamentos;
    private EditText editTextData, editTextDataFim, editTextHoraInicio, editTextHoraFim;
    private Button btnSelecionarData, btnSelecionarDataFim, btnSelecionarHoraInicio, btnSelecionarHoraFim, btnReservar;
    private ProgressBar progressBar;
    
    private EquipamentoRepository equipamentoRepository;
    private ReservaRepository reservaRepository;
    private UsuarioRepository usuarioRepository;
    private LogReservaRepository logReservaRepository;
    private Usuario usuarioAtual;
    private List<Equipamento> equipamentosDisponiveis;
    private ArrayAdapter<Equipamento> adapter;
    private Equipamento equipamentoSelecionado;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private String preSelectedEquipamentoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservar_equipamento);

        // Verificar se há um equipamento pré-selecionado
        if (getIntent().hasExtra("equipamento_id")) {
            preSelectedEquipamentoId = getIntent().getStringExtra("equipamento_id");
        }

        // Inicializar repositories
        equipamentoRepository = RepositoryProvider.getInstance(this).getEquipamentoRepository();
        reservaRepository = RepositoryProvider.getInstance(this).getReservaRepository();
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();
        logReservaRepository = new LogReservaRepositoryFirestore();

        // Inicializar views
        inicializarViews();
        
        // Configurar formatos de data e hora
        configurarFormatos();
        
        // Carregar usuário atual
        carregarUsuarioAtual();
        
        // Carregar equipamentos disponíveis
        carregarEquipamentosDisponiveis();
    }

    private void inicializarViews() {
        listViewEquipamentos = findViewById(R.id.listViewEquipamentos);
        editTextData = findViewById(R.id.editTextData);
        editTextDataFim = findViewById(R.id.editTextDataFim);
        editTextHoraInicio = findViewById(R.id.editTextHoraInicio);
        editTextHoraFim = findViewById(R.id.editTextHoraFim);
        btnSelecionarData = findViewById(R.id.btnSelecionarData);
        btnSelecionarDataFim = findViewById(R.id.btnSelecionarDataFim);
        btnSelecionarHoraInicio = findViewById(R.id.btnSelecionarHoraInicio);
        btnSelecionarHoraFim = findViewById(R.id.btnSelecionarHoraFim);
        btnReservar = findViewById(R.id.btnReservar);
        progressBar = findViewById(R.id.progressBar);

        // Configurar listeners
        btnSelecionarData.setOnClickListener(v -> mostrarDatePicker(true));
        btnSelecionarDataFim.setOnClickListener(v -> mostrarDatePicker(false));
        btnSelecionarHoraInicio.setOnClickListener(v -> mostrarTimePicker(true));
        btnSelecionarHoraFim.setOnClickListener(v -> mostrarTimePicker(false));
        btnReservar.setOnClickListener(v -> fazerReserva());
        
        // Config button
        findViewById(R.id.btnConfig).setOnClickListener(v -> {
            Intent intent = new Intent(ReservarEquipamentoActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        
        listViewEquipamentos.setOnItemClickListener(this::onEquipamentoClick);
    }

    private void configurarFormatos() {
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    private void carregarUsuarioAtual() {
        usuarioRepository.obterUsuarioAtual(usuario -> {
            usuarioAtual = usuario;
        });
    }

    private void carregarEquipamentosDisponiveis() {
        progressBar.setVisibility(View.VISIBLE);
        equipamentoRepository.obterTodosEquipamentos(new EquipamentoRepository.OnEquipamentosListener() {
            @Override
            public void onSuccess(List<Equipamento> equipamentos) {
                progressBar.setVisibility(View.GONE);
                
                // Filtrar equipamentos disponíveis E não em manutenção
                List<Equipamento> equipamentosFiltrados = new ArrayList<>();
                for (Equipamento eq : equipamentos) {
                    if (eq.isDisponivel() && !eq.isEmManutencao()) {
                        equipamentosFiltrados.add(eq);
                    }
                }
                
                equipamentosDisponiveis = equipamentosFiltrados;
                
                // Verificar pré-seleção
                if (preSelectedEquipamentoId != null) {
                    for (Equipamento eq : equipamentosDisponiveis) {
                        if (eq.getId().equals(preSelectedEquipamentoId)) {
                            equipamentoSelecionado = eq;
                            break;
                        }
                    }
                }
                
                atualizarLista();
            }

            @Override
            public void onFailure(String mensagem) {
                progressBar.setVisibility(View.GONE);
                showToast("Erro ao carregar equipamentos: " + mensagem);
            }
        });
    }

    private void atualizarLista() {
        if (equipamentosDisponiveis != null) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_2, 
                android.R.id.text1, equipamentosDisponiveis) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    Equipamento equipamento = getItem(position);
                    
                    android.widget.TextView text1 = view.findViewById(android.R.id.text1);
                    android.widget.TextView text2 = view.findViewById(android.R.id.text2);
                    
                    text1.setText(equipamento.getNome());
                    text1.setTextColor(android.graphics.Color.BLACK); // Nome em preto
                    text2.setText(equipamento.getDescricao());
                    
                    // Marcador de seleção
                    if (equipamentoSelecionado != null && equipamento.getId() != null && equipamento.getId().equals(equipamentoSelecionado.getId())) {
                        view.setBackgroundColor(android.graphics.Color.LTGRAY);
                        text1.setText("✓ " + equipamento.getNome()); // Adiciona marcador visual no texto
                    } else {
                        view.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    }
                    
                    return view;
                }
            };
            
            listViewEquipamentos.setAdapter(adapter);
        }
    }

    private void onEquipamentoClick(AdapterView<?> parent, View view, int position, long id) {
        equipamentoSelecionado = (Equipamento) parent.getItemAtPosition(position);
        showToast("Equipamento selecionado: " + equipamentoSelecionado.getNome());
        if (adapter != null) {
            adapter.notifyDataSetChanged(); // Atualiza a lista para mostrar o marcador
        }
    }

    private void mostrarDatePicker(boolean isDataInicio) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    String dataFormatada = dateFormat.format(calendar.getTime());
                    if (isDataInicio) {
                        editTextData.setText(dataFormatada);
                        // Automaticamente definir data fim igual à data de início
                        editTextDataFim.setText(dataFormatada);
                    } else {
                        editTextDataFim.setText(dataFormatada);
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Não permitir datas passadas
        if (isDataInicio) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        } else {
            // Para data fim, a data mínima é a data de início
            try {
                String dataInicioStr = editTextData.getText().toString();
                if (!dataInicioStr.isEmpty()) {
                    Date dataInicio = dateFormat.parse(dataInicioStr);
                    if (dataInicio != null) {
                        datePickerDialog.getDatePicker().setMinDate(dataInicio.getTime());
                    }
                } else {
                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                }
            } catch (ParseException e) {
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            }
        }
        
        datePickerDialog.show();
    }

    private void mostrarTimePicker(boolean isHoraInicio) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    
                    if (isHoraInicio) {
                        editTextHoraInicio.setText(timeFormat.format(calendar.getTime()));
                    } else {
                        editTextHoraFim.setText(timeFormat.format(calendar.getTime()));
                    }
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }

    private void fazerReserva() {
        // Validações
        if (equipamentoSelecionado == null) {
            showToast("Selecione um equipamento");
            return;
        }
        
        // Verificar se equipamento está em manutenção
        if (equipamentoSelecionado.isEmManutencao()) {
            showToast("Este equipamento está em manutenção e não pode ser reservado");
            return;
        }

        String data = editTextData.getText().toString().trim();
        String dataFim = editTextDataFim.getText().toString().trim();
        String horaInicio = editTextHoraInicio.getText().toString().trim();
        String horaFim = editTextHoraFim.getText().toString().trim();

        if (data.isEmpty()) {
            showToast("Selecione uma data de início");
            return;
        }
        
        if (dataFim.isEmpty()) {
            showToast("Selecione uma data de fim");
            return;
        }

        if (horaInicio.isEmpty()) {
            showToast("Selecione a hora de início");
            return;
        }

        if (horaFim.isEmpty()) {
            showToast("Selecione a hora de fim");
            return;
        }
        
        // Validar se data fim é maior ou igual à data de início
        try {
            Date dataInicioDate = dateFormat.parse(data);
            Date dataFimDate = dateFormat.parse(dataFim);
            if (dataFimDate.before(dataInicioDate)) {
                showToast("Data de fim deve ser igual ou posterior à data de início");
                return;
            }
        } catch (Exception e) {
            showToast("Erro ao validar datas");
            return;
        }

        // Validar se hora fim é maior que hora início (quando for o mesmo dia) (quando for o mesmo dia)
        try {
            Date dataInicioDate = dateFormat.parse(data);
            Date dataFimDate = dateFormat.parse(dataFim);
            
            // Só valida horário se for o mesmo dia
            if (dataInicioDate.equals(dataFimDate)) {
                Date inicio = timeFormat.parse(horaInicio);
                Date fim = timeFormat.parse(horaFim);
                if (fim.before(inicio) || fim.equals(inicio)) {
                    showToast("Hora de fim deve ser maior que hora de início");
                    return;
                }
            }
        } catch (Exception e) {
            showToast("Erro ao validar horários");
            return;
        }

        if (usuarioAtual == null) {
            showToast("Usuário não encontrado");
            return;
        }

        // Verificar conflitos de reserva
        progressBar.setVisibility(View.VISIBLE);
        reservaRepository.verificarConflitoReserva(equipamentoSelecionado.getId(), data, horaInicio, horaFim,
            new ReservaRepository.OnBooleanListener() {
                @Override
                public void onSuccess(boolean temConflito) {
                    if (temConflito) {
                        progressBar.setVisibility(View.GONE);
                        showToast("Já existe uma reserva para este equipamento no horário selecionado");
                    } else {
                        criarReserva();
                    }
                }

                @Override
                public void onFailure(String mensagem) {
                    progressBar.setVisibility(View.GONE);
                    showToast("Erro ao verificar conflitos: " + mensagem);
                }
            });
    }

    private void criarReserva() {
        String data = editTextData.getText().toString().trim();
        String dataFim = editTextDataFim.getText().toString().trim();
        String horaInicio = editTextHoraInicio.getText().toString().trim();
        String horaFim = editTextHoraFim.getText().toString().trim();

        Reserva reserva = new Reserva(
            equipamentoSelecionado.getId(),
            equipamentoSelecionado.getNome(),
            usuarioAtual.getId(),
            usuarioAtual.getNome(),
            data,
            horaInicio,
            horaFim
        );
        
        // Adicionar data fim
        reserva.setDataFim(dataFim);

        reservaRepository.salvarReserva(reserva, new ReservaRepository.OnReservaListener() {
            @Override
            public void onSuccess(Reserva reservaSalva) {
                // Criar log da reserva
                criarLogReserva(reservaSalva);
                
                progressBar.setVisibility(View.GONE);
                showToast("Reserva realizada com sucesso!");
                finish();
            }

            @Override
            public void onFailure(String mensagem) {
                progressBar.setVisibility(View.GONE);
                showToast("Erro ao criar reserva: " + mensagem);
            }
        });
    }

    private void criarLogReserva(Reserva reserva) {
        try {
            // Converter strings de data/hora para Date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dataHoraInicioStr = reserva.getDataReserva() + " " + reserva.getHoraInicio();
            String dataHoraFimStr = reserva.getDataReserva() + " " + reserva.getHoraFim();
            
            Date dataHoraInicio = sdf.parse(dataHoraInicioStr);
            Date dataHoraFim = sdf.parse(dataHoraFimStr);

            // Criar log
            LogReserva log = new LogReserva(
                reserva.getEquipamentoId(),
                reserva.getEquipamentoNome(),
                reserva.getUsuarioId(),
                reserva.getUsuarioNome(),
                usuarioAtual.getEmail(),
                dataHoraInicio,
                dataHoraFim,
                "ATIVA"
            );

            // Salvar log
            logReservaRepository.salvarLog(log, new LogReservaRepository.OnLogSavedListener() {
                @Override
                public void onSuccess(String logId) {
                    // Log criado com sucesso
                }

                @Override
                public void onError(String mensagem) {
                    // Erro ao criar log (não interrompe o fluxo)
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
