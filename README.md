
# 🥗 NutriProntuario

> **Gestão Inteligente para Nutricionistas**

O **NutriProntuario** é uma aplicação Android nativa desenvolvida em Kotlin, projetada para auxiliar nutricionistas no gerenciamento eficiente dos seus pacientes e prontuários. A aplicação centraliza dados de consultas, planos alimentares e medidas antropométricas, utilizando a nuvem para garantir acesso seguro e em tempo real às informações.

## 📱 Visão Geral do Projeto

Este projeto foi construído seguindo as melhores práticas de desenvolvimento Android moderno, utilizando a arquitetura **MVVM (Model-View-ViewModel)** para garantir um código limpo, testável e de fácil manutenção. O backend é totalmente servido pelo **Firebase**, eliminando a necessidade de um servidor próprio e garantindo escalabilidade.

### ✨ Principais Funcionalidades

-   **🔐 Autenticação Segura**

    -   Login e Registo de nutricionistas.

    -   Gestão de sessão persistente via Firebase Authentication.

-   **👥 Gestão de Pacientes**

    -   Listagem completa de pacientes com atualização em tempo real (Realtime updates).

    -   Pesquisa rápida de pacientes por nome.

    -   Cadastro completo com dados de contato.

    -   Edição e remoção segura de registos.

-   **user_md Perfil Detalhado do Paciente**

    -   Visualização organizada em abas (Tabs) utilizando `ViewPager2`.

    -   **Resumo:** Visão geral do paciente.

    -   **Consultas:** Histórico de atendimentos e agendamentos.

    -   **Medidas:** Acompanhamento da evolução antropométrica (peso, altura, etc.).

    -   **Planos:** Gestão de planos alimentares personalizados.

-   **☁️ Sincronização em Nuvem**

    -   Todos os dados são armazenados no **Cloud Firestore**.

    -   Suporte a funcionamento offline (cache do Firestore).


## 🛠 Tech Stack & Bibliotecas

O projeto utiliza um conjunto robusto de tecnologias modernas do ecossistema Android:

### Core

-   [**Kotlin**](https://kotlinlang.org/ "null"): Linguagem principal de desenvolvimento.

-   **Android SDK**:

    -   Min SDK: 24

    -   Target SDK: 36


### Arquitetura & Jetpack

-   **MVVM**: Padrão de arquitetura para separação de responsabilidades.

-   [**ViewBinding**](https://developer.android.com/topic/libraries/view-binding "null"): Para interação segura e direta com as Views XML.

-   [**Navigation Component**](https://developer.android.com/guide/navigation "null"): Gestão de navegação entre Fragments e passagem de argumentos (SafeArgs).

-   [**ViewModel**](https://developer.android.com/topic/libraries/architecture/viewmodel "null"): Gestão de estado da UI consciente do ciclo de vida.

-   [**LiveData**](https://developer.android.com/topic/libraries/architecture/livedata "null"): Observação reativa de dados.


### UI & Layout

-   **Material Design Components**: Componentes visuais modernos (FAB, TextInputs, Dialogs).

-   **ConstraintLayout**: Criação de layouts responsivos e complexos.

-   **ViewPager2**: Navegação por abas no perfil do paciente.

-   **RecyclerView**: Listas eficientes e dinâmicas.


### Backend (BaaS)

-   [**Firebase Authentication**](https://firebase.google.com/docs/auth "null"): Sistema de login.

-   [**Cloud Firestore**](https://firebase.google.com/docs/firestore "null"): Base de dados NoSQL orientada a documentos.

-   **Firebase BOM**: Gestão de versões das bibliotecas Firebase.


## 📂 Estrutura do Projeto

A estrutura de pastas segue uma organização lógica baseada em funcionalidades e camadas:

```
com.example.nutriprontuario
├── 📂 data                  # Camada de Dados
│   ├── 📂 firebase          # Repositórios (interação com Firestore)
│   │   ├── ConsultationRepository.kt
│   │   ├── PatientRepository.kt
│   │   └── ...
│   └── 📂 model             # Data Classes (Entidades)
│       ├── Patient.kt
│       ├── MealPlan.kt
│       └── ...
├── 📂 ui                    # Camada de Apresentação (UI)
│   ├── 📂 auth              # Login e Registo
│   ├── 📂 patients          # Funcionalidades de Pacientes
│   │   ├── 📂 list          # Lista principal
│   │   ├── 📂 form          # Formulário de cadastro
│   │   └── 📂 profile       # Detalhes e Abas do paciente
│   ├── 📂 consultations     # Gestão de Consultas
│   ├── 📂 plans             # Planos Alimentares
│   └── 📂 settings          # Configurações do App
└── 📄 MainActivity.kt       # Activity única (Single Activity Architecture)

```

## 🚀 Como Configurar e Executar

### Pré-requisitos

1.  **Android Studio**: Versão Ladybug ou mais recente.

2.  **JDK**: Java Development Kit 11 ou superior.

3.  **Conta Google**: Para configuração do Firebase.


### Passo a Passo

1.  **Clone o repositório**

    ```
    git clone [https://github.com/seu-usuario/nutriprontuario.git](https://github.com/seu-usuario/nutriprontuario.git)
    cd nutriprontuario
    
    ```

2.  **Configuração do Firebase**

    -   Aceda à [Consola do Firebase](https://console.firebase.google.com/ "null").

    -   Crie um novo projeto.

    -   Adicione uma app Android com o ID: `com.example.nutriprontuario`.

    -   Faça o download do ficheiro `google-services.json`.

    -   Mova o ficheiro para a pasta: `app/google-services.json`.

3.  **Habilitar Serviços no Firebase**

    -   **Authentication:** Vá em "Sign-in method" e ative o provedor "Email/Password".

    -   **Firestore Database:** Crie uma base de dados em modo de teste ou produção e configure as regras de segurança apropriadas.

4.  **Compilar**

    -   Abra o projeto no Android Studio.

    -   Aguarde a sincronização do Gradle (Sync Project with Gradle Files).

    -   Execute a aplicação (`Shift + F10`) num emulador ou dispositivo físico.

    