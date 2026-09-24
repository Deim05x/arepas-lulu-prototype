package com.arepaslulu.catalog.event;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class CatalogEventPublisher {

    private static final long TIMEOUT_MS = 30L * 60L * 1000L;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("catalog-stream-ready"));
        } catch (IOException ex) {
            emitters.remove(emitter);
            emitter.completeWithError(ex);
        }
        return emitter;
    }

    public void catalogChanged() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    broadcast();
                }
            });
            return;
        }
        broadcast();
    }

    private void broadcast() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("catalog-changed").data("refresh"));
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
                emitter.complete();
            }
        }
    }
}
