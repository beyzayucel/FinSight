package com.akademi.finsight.notification.repository;

/** Ayni olayin iki kez islenmesini onler (JPA degil, Redis tabanli). */
public interface IdempotencyStore {

    /** @return anahtar bu cagrida alindiysa true, olay daha once islendiyse false */
    boolean tryAcquire(String eventId);

    void release(String eventId);
}
