package org.legacyCheck.config;

public class BaseConfig {

    public static final String Path_TO_COBOL_FILES = "src/main/resources/cobolFiles";
    public static final String OPENAI_KEY = System.getenv("OPENAI_API_KEY");
    public static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    public static final String OPENAI_MODEL = "gpt-4.0";
    public static final String TxtPath = "src/main/resources/ReponsePrompts/";
    public static final String PDFPath = "src/main/resources/ReponsePrompts/PDFs";
}
