const GEMINI_MODEL = "gemini-3.1-flash-lite";
const GEMINI_ENDPOINT = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`;

const SYSTEM_PROMPT =
  "Sen PawByte Studio tarafından geliştirilen Paw Assistant adlı yardımcı yapay zekâ asistanısın. " +
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
      return jsonResponse({ error: "Bu işlem desteklenmiyor." }, 405);
    }

    if (!env.GEMINI_API_KEY) {
      return jsonResponse(
        { error: "Asistan servisi şu anda kullanılamıyor." },
        500
      );
    }

    let payload;
    try {
      payload = await request.json();
    } catch {
      return jsonResponse({ error: "Gönderilen mesaj işlenemedi." }, 400);
    }

    const rawMessages = Array.isArray(payload?.messages) ? payload.messages : [];
    const contents = rawMessages
      .filter((item) => item && typeof item.text === "string" && item.text.trim().length > 0)
      .map((item) => ({
        role: item.role === "model" ? "model" : "user",
        parts: [{ text: item.text.trim() }]
      }));

    if (contents.length === 0) {
      return jsonResponse({ error: "Lütfen boş olmayan bir mesaj gönderin." }, 400);
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
        const upstreamMessage =
          typeof data?.error?.message === "string" ? data.error.message.trim() : "";

        if (status === 429) {
          return jsonResponse(
            { error: "Şu anda çok fazla istek var. Lütfen biraz bekleyip tekrar deneyin." },
            429
          );
        }

        if (status === 400 || status === 404) {
          const detail = upstreamMessage ? ` (${upstreamMessage})` : "";
          return jsonResponse(
            { error: `Yapay zekâ modeli isteği kabul etmedi${detail}.` },
            status
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
          { error: "Asistan bu istek için yanıt üretemedi." },
          502
        );
      }

      return jsonResponse({ reply: replyText }, 200);
    } catch {
      return jsonResponse(
        { error: "Asistan servisine bağlanırken bir sorun oluştu." },
        502
      );
    }
  }
};
