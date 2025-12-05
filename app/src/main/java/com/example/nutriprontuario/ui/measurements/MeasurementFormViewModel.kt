package com.example.nutriprontuario.ui.measurements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.MeasurementRepository
import com.example.nutriprontuario.data.model.Measurement
import kotlin.math.pow

/**
 * ViewModel responsável pelo formulário de registro de medidas antropométricas.
 *
 * Gerencia o cálculo do IMC em tempo real, classificação nutricional
 * e salvamento das medidas corporais do paciente.
 *
 * @property repository Repositório de medidas para acesso ao Firestore
 */
class MeasurementFormViewModel(
    private val repository: MeasurementRepository = MeasurementRepository()
) : ViewModel() {

    /**
     * Classe de dados que representa o estado atual do formulário.
     *
     * @property imc Valor do IMC calculado (formato string para exibição)
     * @property classification Classificação do IMC (ex: "Peso normal")
     * @property isLoading Indica se uma operação está em andamento
     * @property error Mensagem de erro a ser exibida
     * @property saved Indica se a medida foi salva com sucesso
     */
    data class FormState(
        val imc: String = "--",           // IMC calculado (ou "--" se inválido)
        val classification: String = "--", // Classificação nutricional
        val isLoading: Boolean = false,   // Carregando (mostra progress)
        val error: String? = null,        // Mensagem de erro
        val saved: Boolean = false        // Salvo com sucesso
    )

    // Estado interno mutável do formulário
    private val _state = MutableLiveData(FormState())
    // Estado público imutável para observação pela UI
    val state: LiveData<FormState> = _state

    /**
     * Calcula o IMC em tempo real conforme o usuário digita.
     *
     * Fórmula: IMC = peso (kg) / altura² (m²)
     * Atualiza o estado com o valor calculado e sua classificação.
     *
     * @param weight Peso em quilogramas (pode ser null)
     * @param heightCm Altura em centímetros (pode ser null)
     */
    fun calculateImc(weight: Double?, heightCm: Double?) {
        // Valida se os valores são válidos para cálculo
        if (weight == null || heightCm == null || weight <= 0 || heightCm <= 0) {
            _state.value = _state.value?.copy(imc = "--", classification = "--")
            return
        }

        // Converte altura para metros e calcula IMC
        val heightM = heightCm / 100.0
        val imcValue = weight / heightM.pow(2)

        // Atualiza estado com IMC formatado e classificação
        _state.value = _state.value?.copy(
            imc = "%.2f".format(imcValue),
            classification = getClassification(imcValue)
        )
    }

    /**
     * Salva uma nova medida antropométrica no Firestore.
     *
     * Valida se data, peso e altura foram preenchidos.
     * Calcula automaticamente o IMC e sua classificação antes de salvar.
     *
     * @param patientId ID do paciente associado à medida
     * @param ownerUid UID do usuário proprietário
     * @param dateMillis Data da medição em timestamp (milissegundos)
     * @param weight Peso em quilogramas
     * @param heightCm Altura em centímetros
     * @param waistCm Circunferência da cintura em centímetros (opcional)
     */
    fun saveMeasurement(
        patientId: Long,
        ownerUid: String,
        dateMillis: Long,
        weight: Double?,
        heightCm: Double?,
        waistCm: Double?
    ) {
        // Valida campos obrigatórios
        if (dateMillis == 0L || weight == null || heightCm == null || weight <= 0 || heightCm <= 0) {
            _state.value = _state.value?.copy(error = "Preencha data, peso e altura.")
            return
        }

        // Calcula IMC para salvar junto com a medida
        val imcValue = weight / (heightCm / 100.0).pow(2)

        // Cria objeto Measurement com todos os dados
        val measurement = Measurement(
            patientId = patientId,
            date = dateMillis,
            weight = weight,
            heightCm = heightCm,
            waistCm = waistCm ?: 0.0,
            imc = imcValue,
            imcClassification = getClassification(imcValue),
            ownerUid = ownerUid
        )

        // Inicia estado de carregamento
        _state.value = _state.value?.copy(isLoading = true, error = null, saved = false)

        // Salva no Firestore
        repository.saveMeasurement(measurement) { success, error ->
            if (success) {
                // Salvo com sucesso
                _state.postValue(FormState(saved = true))
            } else {
                // Erro ao salvar
                _state.postValue(
                    _state.value?.copy(
                        isLoading = false,
                        error = error ?: "Algo deu errado. Tente novamente."
                    )
                )
            }
        }
    }

    /**
     * Retorna a classificação nutricional baseada no valor do IMC.
     *
     * Classificações segundo a OMS (Organização Mundial da Saúde):
     * - Abaixo de 18.5: Abaixo do peso
     * - 18.5 a 24.9: Peso normal
     * - 25.0 a 29.9: Sobrepeso
     * - 30.0 a 34.9: Obesidade grau I
     * - 35.0 a 39.9: Obesidade grau II
     * - 40.0 ou mais: Obesidade grau III (mórbida)
     *
     * @param imc Valor do IMC calculado
     * @return String com a classificação correspondente
     */
    private fun getClassification(imc: Double): String {
        return when {
            imc < 18.5 -> "Abaixo do peso"
            imc < 25.0 -> "Peso normal"
            imc < 30.0 -> "Sobrepeso"
            imc < 35.0 -> "Obesidade grau I"
            imc < 40.0 -> "Obesidade grau II"
            else -> "Obesidade grau III"
        }
    }
}
