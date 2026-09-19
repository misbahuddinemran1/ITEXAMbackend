package com.examplatform.modules.written.evaluation.ai.prompt;

import com.examplatform.modules.written.question.entity.WrittenQuestion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TranscriptionPromptBuilder {

    public String buildSystemInstruction() {
        return """
                তুমি একজন OCR বিশেষজ্ঞ। নিচে একজন শিক্ষার্থীর হাতে লেখা উত্তরপত্রের ছবি/পাতা দেওয়া আছে
                (একাধিক পাতা থাকতে পারে, ক্রমানুসারে)। প্রতিটা প্রশ্নের প্রতিটা sub-question (নম্বর ১, ২, ৩...)
                অনুযায়ী শিক্ষার্থী কী লিখেছে তা হুবহু transcribe (হাতের লেখা থেকে টেক্সটে রূপান্তর) করো। বানান
                ভুল থাকলেও যেভাবে লেখা আছে সেভাবেই transcribe করো, নিজে থেকে সংশোধন করবে না। মার্কিং বা
                মূল্যায়ন করবে না, শুধু transcribe করবে।

                নিচের প্রশ্নগুলোর (questionId + sub-question নম্বর) বিপরীতে শিক্ষার্থীর হাতের লেখা খুঁজে বের করে transcribe করো।

                অবশ্যই শুধুমাত্র valid JSON ফরম্যাটে উত্তর দাও, কোনো markdown বা অতিরিক্ত টেক্সট ছাড়া, এই ফরম্যাটে:
                {
                  "transcripts": [
                    { "questionId": "...", "part": "1", "transcribedText": "..." }
                  ]
                }

                "part" ফিল্ডে sub-question নম্বর দাও (যেমন "1", "2", "3" — A/B/C/D অক্ষর না)।
                যদি কোনো প্রশ্ন/sub-question-এর উত্তর ছবিতে খুঁজে না পাও, transcribedText খালি স্ট্রিং ("") দাও।
                """;
    }

    public String buildUserPrompt(List<WrittenQuestion> questions, Map<String, List<Integer>> partsToTranscribeByQuestionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("নিচের প্রশ্নগুলোর উত্তর transcribe করতে হবে:\n\n");

        for (WrittenQuestion question : questions) {
            List<Integer> parts = partsToTranscribeByQuestionId.get(question.getId());
            if (parts == null || parts.isEmpty()) continue;

            sb.append("=== Question ID: ").append(question.getId()).append(" ===\n");
            sb.append("উদ্দীপক: ").append(question.getStimulus()).append("\n");

            for (Integer partOrder : parts) {
                sb.append("Sub-question ").append(partOrder).append(" প্রশ্ন: ")
                        .append(question.getPartQuestionText(partOrder)).append("\n");
            }
            sb.append("\n");
        }

        sb.append("এখন attached ছবি(গুলো) থেকে শিক্ষার্থীর হাতের লেখা প্রতিটা প্রশ্নের সংশ্লিষ্ট sub-question অনুযায়ী transcribe করে JSON আকারে ফেরত দাও।");
        return sb.toString();
    }
}
