package com.examplatform.modules.doubt.dto;

import lombok.Data;

@Data
public class AdminAnswerRequest {
    private String answerText;      // manual text (nullable)
    private String answerPdfUrl;    // manual/admin-uploaded pdf url (nullable)
    private boolean useAiText;      // true হলে AI generate করা text ব্যবহার হবে
    private boolean useAiPdf;       // true হলে AI text থেকে auto-generated pdf ব্যবহার হবে
    // generate-ai endpoint এ preview হওয়া text/pdf, save এর সময় client পাঠাবে অথবা
    // backend cache/temp রাখবে — এটা design decision, নিচে নোট দেখো
}
