const GEMINI_MODEL = "gemini-4-hydrogen";
const GEMINI_ENDPOINT = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`;

const SYSTEM_PROMPT =
  "Sen Pawbyte Studio tarafından geliştirilen Paw Assistant adlı yardımcı yapay zekâ asistanısın. " +
  "Kullanıcılara her zaman anlaşılır, doğal, nazik ve doğru bir Türkçe ile yanıt ver.";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type"
};

function jsonResponse(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      ...CORS_HEADERS
    }
  });
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    if (request.method !== "POST") {
      return jsonResponse({ error: "Yalnızca POST istekleri desteklenmektedir." }, 405);
    }

    if (!env.GEMINI_API_KEY) {
      return jsonResponse(
        { error: "Sunucu yapılandırması eksik. API anahtarı tanımlanmamış." },
        500
      );
    }

    let payload;
    try {
      payload = await request.json();
    } catch {
      return jsonResponse({ error: "Geçersiz JSON isteği gönderildi." }, 400);
    }

    const rawMessages = Array.isArray(payload?.messages) ? payload.messages : [];
    const contents = rawMessages
      .filter((item) => item && typeof item.text === "string" && item.text.trim().length > 0)
      .map((item) => ({
        role: item.role === "model" ? "model" : "user",
        parts: [{ text: item.text.trim() }]
      }));

    if (contents.length === 0) {
      return jsonResponse({ error: "Gönderilecek mesaj bulunamadı." }, 400);
    }

    try {
      const geminiResponse = await fetch(GEMINI_ENDPOINT, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": env.GEMINI_API_KEY
        },
        body: JSON.stringify({
          systemInstruction: {
            parts: [{ text: SYSTEM_PROMPT }]
          },
          contents
        })
      });

      const data = await geminiResponse.json().catch(() => null);

      if (!geminiResponse.ok) {
        const status = geminiResponse.status;
        if (status === 429) {
          return jsonResponse(
            { error: "İstek sınırı aşıldı. Lütfen kısa bir süre sonra tekrar deneyin." },
            429
          );
        }
        return jsonResponse(
          { error: "Yapay zekâ servisinden yanıt alınamadı. Lütfen tekrar deneyin." },
          502
        );
      }

      const replyParts = data?.candidates?.[0]?.content?.parts;
      const replyText = Array.isArray(replyParts)
        ? replyParts
            .map((part) => (typeof part?.text === "string" ? part.text : ""))
            .join("")
            .trim()
        : "";

      if (!replyText) {
        return jsonResponse(
          { error: "Model bu istek için metin yanıtı üretemedi." },
          502
        );
      }

      return jsonResponse({ reply: replyText }, 200);
    } catch {
      return jsonResponse(
        { error: "Yapay zekâ sunucusuna bağlanırken bir hata oluştu." },
        502
      );
    }
  }
};
