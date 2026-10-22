package edu.persian.model;

public record Chapter(long id, String title) {
    @Override public String toString() { return title; }
}
