package com.arthou.arthousreactions.client;

/** Identity-scoped rendering; a preview never replaces live player reaction state. */
final class PreviewScope<T> {
    private T entity;
    private ActiveReaction reaction;

    boolean contains(Object candidate) {
        return entity != null && entity == candidate;
    }

    ActiveReaction reaction() {
        return reaction;
    }

    void render(T entity, ActiveReaction reaction, Runnable draw) {
        T previousEntity = this.entity;
        ActiveReaction previousReaction = this.reaction;
        this.entity = entity;
        this.reaction = reaction;
        try {
            draw.run();
        } finally {
            this.entity = previousEntity;
            this.reaction = previousReaction;
        }
    }
}
