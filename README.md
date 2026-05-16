# WChat - Frontend Android

Aplicação mobile desenvolvida em Kotlin utilizando Jetpack Compose para gerenciamento de mensagens, conversas, grupos e notificações em tempo real.

O projeto utiliza Firebase para autenticação, notificações push e analytics, além de integração com uma API própria via Retrofit.

---

## 📱 Tecnologias utilizadas

### Android

* Kotlin
* Jetpack Compose
* Material 3
* Navigation Compose
* ViewModel
* Coroutines

### Backend & Serviços

* Firebase Authentication
* Firebase Firestore
* Firebase Cloud Messaging (FCM)
* Firebase Analytics
* Retrofit
* OkHttp

---

## 🏗️ Arquitetura do projeto

O projeto segue uma estrutura organizada em camadas para facilitar manutenção, escalabilidade e reutilização de código.

```text
app/src/main/java/com/example/wchat
│
├── components/     # Componentes reutilizáveis da interface
├── data/           # APIs, DTOs e integração remota
├── model/          # Modelos de dados
├── screens/        # Telas da aplicação
├── services/       # Serviços e integrações
├── session/        # Gerenciamento de sessão do usuário
├── ui/             # Tema, cores e estilos
├── utils/          # Utilitários auxiliares
└── viewmodel/      # Lógica de estado das telas
```

---

## ✨ Funcionalidades

### 🔐 Autenticação

* Login de usuários
* Cadastro de usuários
* Persistência de sessão
* Sincronização com Firebase

### 💬 Conversas

* Listagem de conversas
* Tela de chat em tempo real
* Envio de mensagens
* Interface moderna utilizando Compose

### 👥 Grupos e Segmentos

* Gerenciamento de grupos
* Segmentação de usuários
* Seleção de destinatários

### 🔔 Notificações

* Push notifications com Firebase Cloud Messaging
* Notificações internas no app
* Cards personalizados de notificação

### 👤 Perfil

* Visualização e edição de perfil
* Atualização de informações do usuário

---

## 📂 Principais telas

| Tela                    | Descrição                      |
| ----------------------- | ------------------------------ |
| Login                   | Autenticação do usuário        |
| Cadastro                | Registro de novos usuários     |
| Conversas               | Lista de chats disponíveis     |
| ChatScreen              | Conversa em tempo real         |
| Grupos                  | Gerenciamento de grupos        |
| Segmentos               | Organização por segmentos      |
| Perfil                  | Informações do usuário         |
| EditarPerfil            | Atualização de perfil          |
| CriarNotificacao        | Criação de notificações        |
| SelecionarDestinatarios | Seleção de usuários para envio |

---

## ⚙️ Configuração do ambiente

### Pré-requisitos

* Android Studio Hedgehog ou superior
* JDK 11
* Gradle
* Conta Firebase configurada

---

## 🚀 Como executar o projeto

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/seu-repositorio.git
```

### 2. Abra no Android Studio

Abra a pasta do projeto no Android Studio.

---

### 3. Configure o Firebase

O projeto utiliza Firebase Authentication, Firestore e FCM.

Certifique-se de que o arquivo:

```text
app/google-services.json
```

esteja corretamente configurado.

---

### 4. Execute o projeto

Clique em:

```text
Run > Run 'app'
```

ou utilize:

```bash
./gradlew installDebug
```

---

## 🔌 Configuração da API

A comunicação com a API é realizada utilizando Retrofit.

Arquivo principal:

```text
data/remote/api/RetrofitProvider.kt
```

Interface de endpoints:

```text
data/remote/api/WChatApi.kt
```

---

## 📦 Dependências principais

```kotlin
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
implementation("com.google.firebase:firebase-messaging")
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
```

---

## 🎨 Interface

A interface foi construída com Jetpack Compose seguindo uma abordagem moderna e declarativa.

O projeto possui:

* Componentes reutilizáveis
* Estrutura modular
* Navegação desacoplada
* Tema customizado
* Material Design 3

---

## 📌 Melhorias futuras

* Upload de imagens
* Status online/offline
* Indicador de digitação
* Mensagens de áudio
* Tema dark mode completo
* Testes automatizados
* Paginação de mensagens

---

## 👨‍💻 Desenvolvido por

Beatriz Bortolai

---

## 📄 Licença

Este projeto é destinado para fins acadêmicos e de estudo.
