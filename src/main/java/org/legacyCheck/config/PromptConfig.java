package org.legacyCheck.config;

public class PromptConfig {
    public static final String PROMPT_01 = "Você é um desenvolvedor senior de Cobol com conhecimento em COBOL VSAM e DB2 Oracle, que acabou de entrar em um novo projeto, agora eu preciso que realize uma análise de um código\n"
            + "para contextualizar o projeto";

    public static final String PROMPT_02 = "Baseado na analise anterior, o contexto é uma migração de VSAM para utilizar DB2 Oracle com SQL\n"
            + "1- Realize um code-review entre [CIPB106 - F1rst.cbl] e [cipb106 - Eccox.CBL]\n"
            + "2- Encontre erros nas linhas onde encontra-se a marcação da Eccox no arquivo [cipb106 - Eccox.CBL] e me indique linha a linha onde acontece\n"
            + "  2.1 Preciso que procure por coisas como redundancia ou erro de lógica\n"
            + "3- Verifique se o código implementado pela Eccox tem coerencia com a linguagem COBOL/VSAM/Db2 Oracle.\n"
            + "4- Não quero boas praticas de código, estou procurando por erros que afetam diretamente o funcionamento da aplicação\n"
            + "5- ignore codigos comentandos, levando somente os que realmente estão atuando na aplicação, outro ponto é, para cada erro encontrado promova uma solução valida pensando em longevidade";
}
