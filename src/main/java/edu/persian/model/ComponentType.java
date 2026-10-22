package edu.persian.model;

public enum ComponentType {
    OBJECTIVES("Learning objectives · تعلیمی مقاصد"),
    DIALOGUE("Persian dialogue · فارسی مکالمہ"),
    READING("Persian reading · فارسی عبارت"),
    TRANSLATION("Urdu explanation · اردو تشریح"),
    GRAMMAR("Grammar note · قواعد"),
    EXAMPLES("Examples · مثالیں"),
    GLOSSARY("Glossary · فرہنگ");

    private final String label;
    ComponentType(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
