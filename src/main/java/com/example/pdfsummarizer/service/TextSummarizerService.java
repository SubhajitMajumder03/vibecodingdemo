package com.example.pdfsummarizer.service;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TextSummarizerService {

    private static final Logger logger = LoggerFactory.getLogger(TextSummarizerService.class);

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    public String summarizeText(String text, int maxWords) {
        logger.debug("Starting text summarization, input length: {} characters, max words: {}", text.length(), maxWords);

        if (text == null || text.trim().isEmpty()) {
            return "No content to summarize.";
        }

        // Try OpenAI summarization first if API key is available
        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            try {
                return summarizeWithOpenAI(text, maxWords);
            } catch (Exception e) {
                logger.warn("OpenAI summarization failed, falling back to simple summarization: {}", e.getMessage());
            }
        }

        // Fallback to simple extractive summarization
        return simpleSummarization(text, maxWords);
    }

    private String summarizeWithOpenAI(String text, int maxWords) {
        logger.debug("Using OpenAI for text summarization");
        
        OpenAiService service = new OpenAiService(openAiApiKey);
        
        String prompt = String.format(
            "Please provide a comprehensive summary of the following text in approximately %d words. " +
            "Focus on the main points, key concepts, and important details:\n\n%s",
            maxWords, text
        );

        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("gpt-3.5-turbo-instruct")
                .prompt(prompt)
                .maxTokens(maxWords * 2) // Rough estimate: 1 word ≈ 0.5-2 tokens
                .temperature(0.3)
                .build();

        String summary = service.createCompletion(completionRequest)
                .getChoices()
                .get(0)
                .getText()
                .trim();

        logger.debug("OpenAI summarization completed, output length: {} characters", summary.length());
        return summary;
    }

    private String simpleSummarization(String text, int maxWords) {
        logger.debug("Using simple extractive summarization");

        // Split into sentences
        String[] sentences = text.split("[.!?]+");
        if (sentences.length == 0) {
            return text.substring(0, Math.min(text.length(), maxWords * 5)); // Rough word estimate
        }

        // Score sentences based on word frequency and position
        Map<String, Integer> wordFreq = calculateWordFrequency(text);
        List<ScoredSentence> scoredSentences = new ArrayList<>();

        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i].trim();
            if (sentence.length() > 10) { // Filter out very short sentences
                double score = scoreSentence(sentence, wordFreq, i, sentences.length);
                scoredSentences.add(new ScoredSentence(sentence, score, i));
            }
        }

        // Sort by score and select top sentences
        scoredSentences.sort((a, b) -> Double.compare(b.score, a.score));

        StringBuilder summary = new StringBuilder();
        int wordCount = 0;
        Set<Integer> selectedIndices = new HashSet<>();

        for (ScoredSentence scored : scoredSentences) {
            String[] words = scored.sentence.split("\\s+");
            if (wordCount + words.length <= maxWords) {
                selectedIndices.add(scored.originalIndex);
                wordCount += words.length;
            }
        }

        // Reconstruct summary in original order
        List<Integer> sortedIndices = selectedIndices.stream().sorted().collect(Collectors.toList());
        for (Integer index : sortedIndices) {
            if (summary.length() > 0) {
                summary.append(" ");
            }
            summary.append(sentences[index].trim()).append(".");
        }

        String result = summary.toString().trim();
        logger.debug("Simple summarization completed, output length: {} characters", result.length());
        return result.isEmpty() ? text.substring(0, Math.min(text.length(), maxWords * 5)) : result;
    }

    private Map<String, Integer> calculateWordFrequency(String text) {
        Map<String, Integer> frequency = new HashMap<>();
        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .split("\\s+");

        Set<String> stopWords = Set.of("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "must", "this", "that", "these", "those");

        for (String word : words) {
            if (word.length() > 2 && !stopWords.contains(word)) {
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }
        }

        return frequency;
    }

    private double scoreSentence(String sentence, Map<String, Integer> wordFreq, int position, int totalSentences) {
        String[] words = sentence.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .split("\\s+");

        double score = 0;
        int validWords = 0;

        for (String word : words) {
            if (wordFreq.containsKey(word)) {
                score += wordFreq.get(word);
                validWords++;
            }
        }

        // Normalize by sentence length
        if (validWords > 0) {
            score = score / validWords;
        }

        // Boost score for sentences at the beginning and end
        double positionMultiplier = 1.0;
        if (position < totalSentences * 0.1) {
            positionMultiplier = 1.5; // Beginning sentences
        } else if (position > totalSentences * 0.9) {
            positionMultiplier = 1.2; // Ending sentences
        }

        return score * positionMultiplier;
    }

    private static class ScoredSentence {
        String sentence;
        double score;
        int originalIndex;

        ScoredSentence(String sentence, double score, int originalIndex) {
            this.sentence = sentence;
            this.score = score;
            this.originalIndex = originalIndex;
        }
    }
}