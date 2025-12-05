package com.example.nutriprontuario.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * ViewModel responsável pela autenticação do usuário com Firebase Auth.
 *
 * Gerencia o estado de login e cadastro, incluindo validação de campos,
 * comunicação com o Firebase Authentication e tratamento de erros.
 * Segue o padrão MVVM expondo estados observáveis via LiveData.
 */
class AuthViewModel : ViewModel() {

    /**
     * Classe de dados que representa o estado atual da autenticação.
     *
     * @property isLoading Indica se uma operação de autenticação está em andamento
     * @property error Mensagem de erro a ser exibida (null se não houver erro)
     * @property success Indica se a autenticação foi bem-sucedida
     */
    data class AuthState(
        val isLoading: Boolean = false,  // Carregando (mostra progress)
        val error: String? = null,        // Mensagem de erro (se houver)
        val success: Boolean = false      // Login/cadastro bem-sucedido
    )

    // Instância do Firebase Authentication
    private val auth = Firebase.auth

    // Estado interno mutável da autenticação
    private val _state = MutableLiveData(AuthState())
    // Estado público imutável para observação pela UI
    val state: LiveData<AuthState> = _state

    /**
     * Realiza o login do usuário com email e senha.
     *
     * Valida se os campos estão preenchidos antes de tentar autenticar.
     * Atualiza o estado conforme o resultado da operação.
     *
     * @param email Email do usuário
     * @param password Senha do usuário
     */
    fun signIn(email: String, password: String) {
        // Valida campos obrigatórios
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value?.copy(
                error = "Preencha e-mail e senha."
            )
            return
        }

        // Inicia estado de carregamento
        _state.value = _state.value?.copy(isLoading = true, error = null, success = false)

        // Tenta fazer login no Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido
                    _state.value = AuthState(success = true)
                } else {
                    // Falha no login - exibe erro tratado
                    _state.value = AuthState(
                        error = task.exception.asMessage()
                    )
                }
            }
    }

    /**
     * Cadastra um novo usuário com email e senha.
     *
     * Valida se os campos estão preenchidos antes de criar a conta.
     * Atualiza o estado conforme o resultado da operação.
     *
     * @param email Email para o novo cadastro
     * @param password Senha para o novo cadastro
     */
    fun signUp(email: String, password: String) {
        // Valida campos obrigatórios
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value?.copy(
                error = "Preencha e-mail e senha."
            )
            return
        }

        // Inicia estado de carregamento
        _state.value = _state.value?.copy(isLoading = true, error = null, success = false)

        // Tenta criar conta no Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Cadastro bem-sucedido
                    _state.value = AuthState(success = true)
                } else {
                    // Falha no cadastro - exibe erro tratado
                    _state.value = AuthState(
                        error = task.exception.asMessage()
                    )
                }
            }
    }

    /**
     * Limpa a mensagem de erro atual.
     *
     * Útil após o usuário visualizar o erro e querer tentar novamente.
     */
    fun clearError() {
        _state.value = _state.value?.copy(error = null)
    }

    /**
     * Converte exceções do Firebase em mensagens amigáveis ao usuário.
     *
     * @return Mensagem de erro traduzida e compreensível
     */
    private fun Exception?.asMessage(): String {
        return when (this) {
            // Credenciais inválidas (email/senha incorretos ou usuário não existe)
            is FirebaseAuthInvalidCredentialsException,
            is FirebaseAuthInvalidUserException -> "Credenciais inválidas. Verifique e tente novamente."
            // Outros erros - usa mensagem localizada ou genérica
            else -> this?.localizedMessage ?: "Algo deu errado. Tente novamente."
        }
    }
}
