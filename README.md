# WChat - Frontend Android

Aplicação mobile desenvolvida em Kotlin utilizando Jetpack Compose para gerenciamento de mensagens, grupos, notificações e comunicação em tempo real.

O frontend foi desenvolvido para funcionar integrado ao backend WChat (Spring Boot + MongoDB), utilizando Firebase Authentication, Firebase Cloud Messaging e Retrofit para comunicação com a API.

---

# 📱 Demonstração do projeto

O aplicativo possui:

* Login e cadastro de usuários
* Persistência de sessão
* Conversas em tempo real
* Gerenciamento de grupos
* Segmentação de usuários
* Sistema de notificações
* Edição de perfil
* Seleção de destinatários
* Integração com Firebase
* Comunicação com backend REST API

---

# 🛠️ Tecnologias utilizadas

## Android

* Kotlin
* Jetpack Compose
* Material 3
* Navigation Compose
* ViewModel
* Coroutines
* Retrofit
* OkHttp

## Firebase

* Firebase Authentication
* Firebase Firestore
* Firebase Cloud Messaging (FCM)
* Firebase Analytics

## Backend integrado

O frontend se comunica com um backend desenvolvido utilizando:

* Java
* Spring Boot
* MongoDB
* JWT Authentication
* Firebase Admin SDK
* Docker Compose

---

# 🏗️ Arquitetura do projeto

O projeto segue uma estrutura organizada em camadas para facilitar manutenção, escalabilidade e reutilização de código.

```text
app/src/main/java/com/example/wchat
│
├── components/     # Componentes reutilizáveis
├── data/           # APIs, DTOs e integração remota
├── model/          # Modelos de dados
├── screens/        # Telas da aplicação
├── services/       # Serviços e integrações
├── session/        # Gerenciamento de sessão
├── ui/             # Tema, cores e estilos
├── utils/          # Utilitários auxiliares
└── viewmodel/      # Controle de estado das telas
```

---

# ✨ Funcionalidades

## 🔐 Autenticação

* Login de usuários
* Cadastro de usuários
* Persistência de sessão
* Integração com Firebase Authentication
* Controle de autenticação via JWT

## 💬 Conversas

* Listagem de conversas
* Chat em tempo real
* Envio de mensagens
* Atualização dinâmica da interface

## 👥 Grupos e Segmentos

* Criação e gerenciamento de grupos
* Segmentação de usuários
* Seleção de destinatários

## 🔔 Notificações

* Push notifications com Firebase Cloud Messaging
* Criação de notificações
* Cards personalizados

## 👤 Perfil

* Visualização de perfil
* Atualização de informações do usuário
* Persistência de dados

---

# 📂 Principais telas

| Tela                    | Descrição                  |
| ----------------------- | -------------------------- |
| Login                   | Autenticação do usuário    |
| Cadastro                | Registro de novos usuários |
| Conversas               | Lista de chats disponíveis |
| ChatScreen              | Conversa em tempo real     |
| Grupos                  | Gerenciamento de grupos    |
| Segmentos               | Organização de usuários    |
| Perfil                  | Informações do usuário     |
| EditarPerfil            | Atualização de perfil      |
| CriarNotificacao        | Criação de notificações    |
| SelecionarDestinatarios | Seleção de usuários        |

---

# 📦 Estrutura da entrega

O projeto contém:

* Código fonte completo do frontend Android
* Integração com Firebase
* Firebase Functions
* APK de release para instalação

APK disponível em:

```text
app/release/app-release.apk
```

Arquivos desnecessários como `.idea/`, `.kotlin/` e `node_modules/` não fazem parte da entrega por serem gerados automaticamente pelo ambiente.

---

# ⚙️ Pré-requisitos

Para executar o projeto é necessário possuir:

* Android Studio Hedgehog ou superior
* JDK 11
* Gradle
* Node.js
* Backend WChat em execução
* Conta Firebase configurada

---

# 🔗 Integração com o Backend

O frontend foi desenvolvido para consumir a API do backend WChat.

A comunicação é realizada utilizando Retrofit.

Base URL utilizada no Android Emulator:

```text
http://10.0.2.2:8080/
```

Caso utilize Genymotion:

```text
http://192.168.56.1:8080/
```

Certifique-se de que o backend esteja em execução antes de iniciar o aplicativo Android.

## Executando o backend

Linux/Mac:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

Caso utilize Docker:

```bash
docker-compose up
```

---

# 🚀 Como executar o frontend

## 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/seu-repositorio.git
```

---

## 2. Abra no Android Studio

Abra a pasta do projeto no Android Studio.

---

## 3. Configure o Firebase

O projeto utiliza Firebase Authentication, Firestore e FCM.

Certifique-se de que o arquivo:

```text
app/google-services.json
```

esteja corretamente configurado.

---

## 4. Instale as dependências do Firebase Functions

O projeto utiliza Firebase Functions.

Antes de executar, instale as dependências:

```bash
cd functions
npm install
```

---

## 5. Execute o projeto

Pelo Android Studio:

```text
Run > Run 'app'
```

Ou utilizando Gradle:

```bash
./gradlew installDebug
```

---

# 🔌 Configuração da API

A comunicação com a API é realizada utilizando Retrofit.

Arquivos principais:

```text
data/remote/api/RetrofitProvider.kt
```

```text
data/remote/api/WChatApi.kt
```

---

# 📦 Dependências principais

```kotlin
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
implementation("com.google.firebase:firebase-messaging")
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
```

---

# 🎨 Interface

A interface foi construída com Jetpack Compose seguindo uma abordagem moderna e declarativa.

O projeto possui:

* Componentes reutilizáveis
* Estrutura modular
* Navegação desacoplada
* Tema customizado
* Material Design 3
* Responsividade para diferentes telas

---

# 📌 Melhorias futuras

* Upload de imagens
* Status online/offline
* Indicador de digitação
* Mensagens de áudio
* Dark mode completo
* Testes automatizados
* Paginação de mensagens

---

# 📄 Licença

Projeto desenvolvido para fins acadêmicos e de estudo.

