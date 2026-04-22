import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

const nomesDeExibicao: { [key: string]: string } = {
  CMO: "CMOs",
  CFO: "CFOs",
  CEO: "CEOs",
  CPO: "CPOs",
  CHRO: "CHROs",
  CSO: "CSOs",
  CIO: "CIOs",
  ED: "ED",
  IT: "IT",
  RETAIL_FINANCIAL: "RETAIL & FINANCIAL",
  GRC: "GRC",
  HR: "HR",
  SMART_SPENDS: "SMART SPENDS",
  HEALTH: "HEALTH",
  CSC: "CSC",
  FIELD_MARKETING: "FIELD MARKETING",
  FINANCE: "FINANCE",
  ESG: "ESG",
  CX: "CX",
};

export const enviarNotificacaoPush = functions
  .region("southamerica-east1")
  .firestore.document("{collection}/{chatId}/mensagens/{mensagemId}")
  .onCreate(async (snapshot: functions.firestore.QueryDocumentSnapshot, context: functions.EventContext) => {
    const novaMensagem = snapshot.data();

    if (!novaMensagem) {
      console.log("Dados da mensagem não encontrados.");
      return null;
    }

    const remetenteId = novaMensagem.remetenteId as string;
    const remetenteNome = novaMensagem.remetenteNome as string;
    const remetenteTipo = novaMensagem.remetenteTipo as string;
    const textoMensagem = novaMensagem.texto as string;
    const chatId = context.params.chatId as string;
    const collection = context.params.collection as string;

    if (!remetenteTipo) {
      console.log(`Mensagem ${snapshot.id} ignorada por não conter "remetenteTipo".`);
      return null;
    }

    const colecoesPermitidas = ["grupos", "segmentos", "chats1a1"];
    if (!colecoesPermitidas.includes(collection)) {
      console.log(`Coleção "${collection}" ignorada.`);
      return null;
    }

    console.log(`Nova mensagem de "${remetenteNome}" (${remetenteTipo}) no chat "${chatId}".`);

    let destinatariosFinais: string[] = [];

    if (collection === "chats1a1") {
      const destinatarioId = novaMensagem.destinatarioId as string | undefined;
      if (destinatarioId) {
        destinatariosFinais.push(destinatarioId);
      }
    } else {
      const ids: string[] = [];
      const chatDocRef = db.collection(collection).doc(chatId);
      const chatDoc = await chatDocRef.get();

      if (chatDoc.exists) {
        const participantes = (chatDoc.data()?.participantesIds as string[]) || [];

        if (remetenteTipo === "OPERADOR") {
          ids.push(...participantes);
          console.log("(OPERADOR) Notificando apenas clientes:", participantes);
        } else {
          console.log("(CLIENTE) Iniciando notificação para clientes e operadores.");

          try {
            const operadoresSnapshot = await db.collection("usuarios")
              .where("tipo", "==", "OPERADOR")
              .get();

            if (!operadoresSnapshot.empty) {
              const operadoresIds = operadoresSnapshot.docs.map((doc) => doc.id);
              ids.push(...operadoresIds);
              console.log("--> Adicionados operadores do sistema:", operadoresIds);
            }
          } catch (error) {
            console.error("Erro ao buscar usuários operadores:", error);
          }
        }
      }
      destinatariosFinais = [...new Set(ids)];
    }

    destinatariosFinais = destinatariosFinais.filter((id) => id !== remetenteId);

    if (destinatariosFinais.length === 0) {
      console.log("Nenhum destinatário final para notificar após filtros.");
      return null;
    }

    console.log("Destinatários finais para notificar:", destinatariosFinais);

    const tokensPromises = destinatariosFinais.map((userId) =>
      db.collection("usuarios").doc(userId).get()
    );

    const userSnapshots = await Promise.all(tokensPromises);

    const tokens = userSnapshots
      .map((snap) => snap.data()?.fcmToken as string)
      .filter((token): token is string => !!token && token.length > 0);

    const tokensUnicos = [...new Set(tokens)];

    if (tokensUnicos.length === 0) {
      console.log("Nenhum token FCM válido encontrado para os destinatários.");
      return null;
    }

    console.log(`Enviando notificação para ${tokensUnicos.length} dispositivo(s).`);

    let payloadTitle: string;
    let payloadBody: string;

    let nomeDoChat: string;
    if (collection === "chats1a1") {
      nomeDoChat = remetenteNome;
    } else {
      nomeDoChat = nomesDeExibicao[chatId] || chatId;
      console.log(`ID do chat: "${chatId}", Nome de Exibição Encontrado: "${nomeDoChat}".`);
    }

    if (textoMensagem.startsWith("[NOTIFICATION]")) {
      const data = textoMensagem
        .split("\n")
        .slice(1)
        .reduce((acc, line) => {
          const [key, ...valueParts] = line.split("=");
          if (key && valueParts.length > 0) {
            acc[key.trim()] = valueParts.join("=").trim();
          }
          return acc;
        }, {} as Record<string, string>);

      const tituloCampanha = data.titulo || "Nova Campanha";
      const descricaoCampanha = data.descricao || "Confira os detalhes no app.";
      payloadTitle = nomeDoChat;
      payloadBody = `${tituloCampanha}: ${descricaoCampanha}`;
    } else {
        payloadTitle = nomeDoChat;

        if (collection === "chats1a1") {
            payloadBody = textoMensagem;
        } else {
            payloadBody = `${remetenteNome}: ${textoMensagem}`;
        }
    }

    const message = {
      data: {
        title: payloadTitle,
        body: payloadBody,
        chatId: chatId,
        collection: collection,
        remetenteNome: remetenteNome,
      },
      tokens: tokensUnicos,
    };

    try {
      const response = await messaging.sendEachForMulticast(message);
      console.log(`✅ Notificações enviadas com sucesso: ${response.successCount}`);
      if (response.failureCount > 0) {
        const failedTokens: string[] = [];
        response.responses.forEach((resp, idx) => {
          if (!resp.success) {
            failedTokens.push(tokensUnicos[idx]);
          }
        });
        console.error(`❌ Falha ao enviar para ${response.failureCount} tokens:`, failedTokens);
      }
      return null;
    } catch (error) {
      console.error("🚨 Erro catastrófico ao enviar notificação:", error);
      return null;
    }
  });
