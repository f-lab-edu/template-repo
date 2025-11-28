package io.github.jaehyeonhan.project.entity;

public enum ParticipationRole {
    CREATOR(99),
    MANAGER(5),
    USER(1);

    final int level;

    ParticipationRole(int level) {
        this.level = level;
    }

    public boolean canBlock(ParticipationRole other) {
        return this.level > other.level;
    }

    public boolean canUnblock(ParticipationRole other) {
        return this.level > other.level;
    }
}
