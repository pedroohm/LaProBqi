package com.pedromoura.laprobqi.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.pedromoura.laprobqi.models.Reserva;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.EquipamentoRepository;
import com.pedromoura.laprobqi.repository.ReservaRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservarEquipamentoActivity extends AppCompatActivity {

    private ListView listViewEquipamentos;
    private EditText editTextData, editTextHoraInicio, editTextHoraFim;
    private Button btnSelecionarData, btnSelecionarHoraInicio, btnSelecionarHoraFim, btnReservar;
    private ProgressBar progressBar;
    
    private EquipamentoRepository equipamentoRepository;
    private ReservaRepository reservaRepository;
    private UsuarioRepository usuarioRepository;
    private Usuario usuarioAtual;
    private List<Equipamento> equipamentosDisponiveis;
    private ArrayAdapter<Equipamento> adapter;
    private Equipamento equipamentoSelecionado;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservar_equipamento);

        // Inicializar repositories
        equipamentoRepository = RepositoryProvider.getInstance(this).getEquipamentoRepository();
        reservaRepository = RepositoryProvider.getInstance(this).getReservaRepository();
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();

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
        editTextHoraInicio = findViewById(R.id.editTextHoraInicio);
        editTextHoraFim = findViewById(R.id.editTextHoraFim);
        btnSelecionarData = findViewById(R.id.btnSelecionarData);
        btnSelecionarHoraInicio = findViewById(R.id.btnSelecionarHoraInicio);
        btnSelecionarHoraFim = findViewById(R.id.btnSelecionarHoraFim);
        btnReservar = findViewById(R.id.btnReservar);
        progressBar = findViewById(R.id.progressBar);

        // Configurar listeners
        btnSelecionarData.setOnClickListener(v -> mostrarDatePicker());
        btnSelecionarHoraInicio.setOnClickListener(v -> mostrarTimePicker(true));
        btnSelecionarHoraFim.setOnClickListener(v -> mostrarTimePicker(false));
        btnReservar.setOnClickListener(v -> fazerReserva());
        
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
        equipamentoRepository.obterEquipamentosDisponiveis(new EquipamentoRepository.OnEquipamentosListener() {
            @Override
            public void onSuccess(List<Equipamento> equipamentos) {
                progressBar.setVisibility(View.GONE);
                equipamentosDisponiveis = equipamentos;
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
                    text2.setText(equipamento.getDescricao());
                    
                    return view;
                }
            };
            
            listViewEquipamentos.setAdapter(adapter);
        }
    }

    private void onEquipamentoClick(AdapterView<?> parent, View view, int position, long id) {
        equipamentoSelecionado = (Equipamento) parent.getItemAtPosition(position);
        showToast("Equipamento selecionado: " + equipamentoSelecionado.getNome());
    }

    private void mostrarDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    editTextData.setText(dateFormat.format(calendar.getTime()));
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Não permitir datas passadas
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
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

        String data = editTextData.getText().toString().trim();
        String horaInicio = editTextHoraInicio.getText().toString().trim();
        String horaFim = editTextHoraFim.getText().toString().trim();

        if (data.isEmpty()) {
            showToast("Selecione uma data");
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

        // Validar se hora fim é maior que hora início
        try {
            Date inicio = timeFormat.parse(horaInicio);
            Date fim = timeFormat.parse(horaFim);
            if (fim.before(inicio) || fim.equals(inicio)) {
                showToast("Hora de fim deve ser maior que hora de início");
                return;
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

        reservaRepository.salvarReserva(reserva, new ReservaRepository.OnReservaListener() {
            @Override
            public void onSuccess(Reserva reservaSalva) {
                // Atualizar status do equipamento para RESERVADO
                equipamentoRepository.atualizarStatusEquipamento(equipamentoSelecionado.getId(), "RESERVADO",
                    new EquipamentoRepository.OnBooleanListener() {
                        @Override
                        public void onSuccess(boolean success) {
                            progressBar.setVisibility(View.GONE);
                            showToast("Reserva realizada com sucesso!");
                            finish();
                        }

                        @Override
                        public void onFailure(String mensagem) {
                            progressBar.setVisibility(View.GONE);
                            showToast("Reserva criada, mas erro ao atualizar status do equipamento: " + mensagem);
                        }
                    });
            }

            @Override
            public void onFailure(String mensagem) {
                progressBar.setVisibility(View.GONE);
                showToast("Erro ao criar reserva: " + mensagem);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
