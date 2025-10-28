package com.fa25se225.capstone.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class IngestService {

    @Autowired(required = false)
    private VectorStore vectorStore;

    public IngestService() {
    }

    public void upsert(String text) {
        if (vectorStore == null) return;
        Document doc = new Document(text);
        vectorStore.add(List.of(doc));
    }

    public void upsertMany(List<String> texts) {
        if (vectorStore == null) return;
        List<Document> docs = texts.stream()
                .map(Document::new)
                .toList();
        vectorStore.add(docs);
    }
}
