package com.example.nutriprontuario.data.local

import android.content.Context

/**
 * Gerenciador de PIN para autenticação local do aplicativo.
 *
 * Esta classe é responsável por armazenar e validar o PIN de acesso local
 * do usuário utilizando SharedPreferences. O PIN é uma camada adicional de
 * segurança além da autenticação Firebase.
 *
 * NOTA: O PIN é armazenado em texto simples no SharedPreferences.
 * Para produção, considere usar criptografia (EncryptedSharedPreferences).
 *
 * @param context Contexto da aplicação para acessar SharedPreferences
 */
class PinManager(context: Context) {

    // SharedPreferences para armazenamento local do PIN
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Verifica se o usuário já configurou um PIN.
     *
     * @return true se existe um PIN salvo, false caso contrário
     */
    fun hasPin(): Boolean = prefs.contains(KEY_PIN)

    /**
     * Salva um novo PIN no armazenamento local.
     *
     * @param pin O PIN a ser salvo (geralmente 4-6 dígitos)
     */
    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    /**
     * Valida se o PIN informado corresponde ao PIN armazenado.
     *
     * @param pin O PIN digitado pelo usuário para validação
     * @return true se o PIN está correto, false se estiver incorreto ou não existir
     */
    fun validate(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN, null) // Recupera PIN armazenado
        return stored != null && stored == pin       // Compara com o informado
    }

    /**
     * Remove o PIN armazenado.
     *
     * Útil quando o usuário deseja redefinir o PIN ou desabilitar
     * a autenticação local por PIN.
     */
    fun clearPin() {
        prefs.edit().remove(KEY_PIN).apply()
    }

    companion object {
        // Nome do arquivo SharedPreferences para o PIN
        private const val PREFS_NAME = "pin_prefs"
        // Chave para armazenar o valor do PIN
        private const val KEY_PIN = "pin_value"
    }
}
