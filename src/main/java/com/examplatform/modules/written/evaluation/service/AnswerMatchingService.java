package com.examplatform.modules.written.evaluation.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class AnswerMatchingService {

    // সাধারণ filler words যেগুলো matching-এ বাদ দেওয়া হবে (ইংরেজি + বাংলা)
    private static final Set<String> STOP_WORDS = Set.of(
            // English
            "a", "an", "the", "is", "are", "was", "were", "of", "in", "on", "to", "and", "or",

            // বাংলা — সর্বনাম, অনুসর্গ, সংযোজক, সহায়ক ক্রিয়া
            "এবং", "অথবা", "কিন্তু", "যে", "যা", "যার", "যাকে", "যেটি", "যেটা",
            "এই", "ঐ", "সেই", "এটি", "এটা", "ওটা", "ওই", "তা", "তার", "তাকে", "তাদের",
            "আমি", "আমরা", "আমার", "আমাদের", "তুমি", "তোমরা", "তোমার", "সে", "তিনি", "তারা",
            "হয়", "হয়েছে", "হবে", "হচ্ছে", "ছিল", "করে", "করেছে", "করবে", "করা",
            "একটি", "একটা", "কোনো", "কোন", "সব", "সকল", "প্রতিটি",
            "থেকে", "সাথে", "জন্য", "দ্বারা", "মধ্যে", "পর", "আগে", "উপর", "নিচে",
            "না", "নাই", "নেই", "কি", "কী", "কেন", "কীভাবে", "কোথায়", "কখন",
            "ও", "এ", "এর", "তবে", "যদি", "তাহলে", "কারণ", "যেমন", "অর্থাৎ"
    );

    /**
     * দুইটা answer text-এর মধ্যে TF-IDF weighted Cosine Similarity বের করে (0.0 - 1.0)
     * এই দুইটা টেক্সটকেই একটা mini 2-document corpus হিসেবে ধরে IDF হিসাব করা হয় —
     * যে শব্দ শুধু একটা text-এ আছে (দুইটাতে না), সেটা বেশি distinctive/গুরুত্বপূর্ণ ধরা হয়।
     */
    public BigDecimal calculateSimilarity(String textA, String textB) {
        if (textA == null || textB == null || textA.isBlank() || textB.isBlank()) {
            return BigDecimal.ZERO;
        }

        List<String> tokensA = tokenize(textA);
        List<String> tokensB = tokenize(textB);

        if (tokensA.isEmpty() || tokensB.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Term Frequency (TF) — প্রতিটা document এর জন্য
        Map<String, Integer> tfA = termFrequency(tokensA);
        Map<String, Integer> tfB = termFrequency(tokensB);

        // Vocabulary — দুইটা document মিলিয়ে সব unique শব্দ
        Set<String> vocabulary = new HashSet<>();
        vocabulary.addAll(tfA.keySet());
        vocabulary.addAll(tfB.keySet());

        // Document Frequency (DF) — প্রতিটা শব্দ কয়টা document এ আছে (১ অথবা ২)
        // IDF = log( (totalDocs + 1) / (df + 1) ) + 1   [smoothed IDF, zero হয় না কখনো]
        int totalDocs = 2;
        Map<String, Double> idf = new HashMap<>();
        for (String term : vocabulary) {
            int df = 0;
            if (tfA.containsKey(term)) df++;
            if (tfB.containsKey(term)) df++;
            double idfValue = Math.log((double) (totalDocs + 1) / (df + 1)) + 1;
            idf.put(term, idfValue);
        }

        // TF-IDF weighted vectors বানানো
        double[] vectorA = new double[vocabulary.size()];
        double[] vectorB = new double[vocabulary.size()];
        List<String> vocabList = new ArrayList<>(vocabulary);

        for (int i = 0; i < vocabList.size(); i++) {
            String term = vocabList.get(i);
            double idfValue = idf.get(term);
            vectorA[i] = tfA.getOrDefault(term, 0) * idfValue;
            vectorB[i] = tfB.getOrDefault(term, 0) * idfValue;
        }

        double similarity = cosineSimilarity(vectorA, vectorB);

        return BigDecimal.valueOf(similarity).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * matchScore থেকে predicted mark বের করে: predictedMark = maxMark * matchScore
     */
    public BigDecimal calculatePredictedMark(BigDecimal matchScore, BigDecimal maxMark) {
        if (matchScore == null || maxMark == null) {
            return BigDecimal.ZERO;
        }
        return matchScore.multiply(maxMark).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Text normalize + tokenize করে word list বানায় (order রাখা হয়, TF গণনার জন্য দরকার নেই আসলে কিন্তু simplicity জন্য list)
     */
    private List<String> tokenize(String text) {
        String normalized = text.toLowerCase()
                .replaceAll("[^a-zA-Zঀ-৿0-9\\s]", " ") // punctuation বাদ, বাংলা ইউনিকোড রেঞ্জ রাখা
                .trim();

        if (normalized.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> tokens = new ArrayList<>();
        for (String word : normalized.split("\\s+")) {
            if (!word.isBlank() && !STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    /**
     * একটা token list থেকে term -> frequency count ম্যাপ বানায়
     */
    private Map<String, Integer> termFrequency(List<String> tokens) {
        Map<String, Integer> freq = new HashMap<>();
        for (String token : tokens) {
            freq.merge(token, 1, Integer::sum);
        }
        return freq;
    }

    /**
     * দুইটা vector এর মধ্যে cosine similarity (0.0 - 1.0) হিসাব করে
     */
    private double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
