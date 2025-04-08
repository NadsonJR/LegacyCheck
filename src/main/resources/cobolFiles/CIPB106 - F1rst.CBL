IDENTIFICATION  DIVISION.
       PROGRAM-ID.     CIPB106.
       AUTHOR.         NELSON.
       DATE-WRITTEN.   08/2002.
      *----------------------------------------------------------------*
      *  OBJETIVO                                                      *
      *  --------                                                      *
      *     ESTATISTICAS DE UTILIZACAO POR FILIAL CIP X CIC            *
      *----------------------------------------------------------------*
      *  MANUTENCOES                                                   *
      *  ANALISTA    DATA    DESCRICAO                                 *
      *  -------- ---------- ----------------------------------------- *
      *  URANO B  21/01/2003 DAR DISPLAY DE USUARIO NAO ENCONTRADO     *
      *                      TOTALIZAR PARA TELEMARKETING SEMPRE QUE A *
      *                      FILIAL DO USUARIO <> DA FILIAL DO CLIENTE *
      *----------------------------------------------------------------*
      *  -------- ---------- ----------------------------------------- *
AG    *  ADRIANA  18/07/2003 INCLUIR COLUNA CIP-TELEMARKETING NO RELA- *
      *  (AG)                TORIO ESTATISTICA ATUALIZACAO             *
      *----------------------------------------------------------------*
MAR001*  MARCO    08/03/2004 INCLUIR COLUNA CIP-AUTOMATICO NO RELATORIO*
      *                      ESTATISTICA ATUALIZACAO                   *
      *----------------------------------------------------------------*
       INSTALLATION.   ABN AMRO BANK
       DATE-COMPILED.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           DECIMAL-POINT IS COMMA
             C01 IS SALTO.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.

           SELECT  CIPR1061  ASSIGN   TO  UT-S-CIPR1061.

           SELECT  CICA600    ASSIGN UT-S-CICA600
                             FILE STATUS FS-CICA600.

           SELECT  TABA539
             ASSIGN  TO TABA539
             ORGANIZATION IS INDEXED
             ACCESS IS RANDOM
             RECORD KEY IS ST-CHV-ARQ-539
             FILE STATUS FS-TABA539.


      * ----------------------------------------------------------------
       DATA DIVISION.
      * ----------------------------------------------------------------
       FILE SECTION.

       FD  CIPR1061
           LABEL   RECORD   IS   STANDARD
           RECORDING MODE   IS   F
           BLOCK   CONTAINS   0.

       01  RG-R1061.
           03 CANAL-R1061               PIC X(001).
           03 REG-R1061                 PIC X(132).

       FD  CICA600
           RECORDING MODE F
           BLOCK      0.

       01  RG-CICA600-600-FD.
           03  RG-FD-600      PIC X(460).

       FD  TABA539.
           COPY TABB539.
      *
      * ----------------------------------------------------------------
       WORKING-STORAGE SECTION.
      * ----------------------------------------------------------------
      *    AREAS AUXILIARES
      * ----------------------------------------------------------------
       77 CH-FIM-CICA600               PIC X(003)  VALUE 'NAO'.
       77 FS-CICA600                   PIC X(002)  VALUE '00'.
       77 FS-TABA539                   PIC X(002)  VALUE '00'.
       77 WS-ATUALIZA                  PIC X(003)  VALUE SPACES.
       77 WS-ERRO                      PIC X(001)  VALUE SPACES.
       77 WS-TOTAL                     PIC X(001)  VALUE SPACES.
       77 WS-PGM-MAN                   PIC X(008)  VALUE SPACES.
       77 ABEND-ROT                    PIC X(008)  VALUE '#ABEND'.
       77 WS-SERVICO                   PIC X(008)  VALUE '      '.
       77 WS-PAGINA                    PIC 9(004)  VALUE ZEROS.
       77 WS-LINHAS                    PIC 9(002)  VALUE 90.
       77 WS-LINHAS-AUX                PIC 9(002)  VALUE ZEROS.
       77 WS-IND                       PIC 9(004)  VALUE ZEROS.
       77 WS-DATE-DB                   PIC X(010)  VALUE ZEROS.
       77 WS-DATE                      PIC X(010)  VALUE SPACES.
       77 WS-TIMESTAMP                 PIC X(026)  VALUE ZEROS.
       77 WS-TOTAL-VR                  PIC ZZZ.ZZZ.ZZ9.
      *

       01  WS-CONTADORES.
           05 CT-LIDOS                  PIC 9(07) VALUE ZERO.
           05 CT-DESP                   PIC 9(07) VALUE ZERO.
           05 CT-DESP-TP                PIC 9(07) VALUE ZERO.
           05 CT-DESP-FILIAL            PIC 9(07) VALUE ZERO.
           05 CT-DESP-539               PIC 9(07) VALUE ZERO.
AG         05 CT-DESP-CIP10             PIC 9(07) VALUE ZERO.
           05 CT-TT-CIC-AUT             PIC 9(09) VALUE ZERO.
           05 CT-TT-CIC-TLM             PIC 9(09) VALUE ZERO.
           05 CT-TT-CIC-MAN             PIC 9(09) VALUE ZERO.
           05 CT-TT-CIP                 PIC 9(09) VALUE ZERO.
MAR001     05 CT-TT-CIP-AUT             PIC 9(09) VALUE ZERO.
AG         05 CT-TT-CIP-TLM             PIC 9(09) VALUE ZERO.
           05 CT-TT-GERAL               PIC 9(09) VALUE ZERO.
      *
       01  WS-DATA-ED.
           05  WS-DT-DD           PIC X(002)   VALUE SPACES.
           05  FILLER             PIC X(001)   VALUE '/'.
           05  WS-DT-MM           PIC X(002)   VALUE SPACES.
           05  FILLER             PIC X(001)   VALUE '/'.
           05  WS-DT-AAAA         PIC X(004)   VALUE SPACES.
      *
       01  WS-DATA-MV.
           05  WS-DT-DD-MV        PIC X(002)   VALUE SPACES.
           05  FILLER             PIC X(001)   VALUE SPACES.
           05  WS-DT-MM-MV        PIC X(002)   VALUE SPACES.
           05  FILLER             PIC X(001)   VALUE SPACES.
           05  WS-DT-AAAA-MV      PIC X(004)   VALUE SPACES.
      *
       01  WS-TIME-ED.
           05  WS-TIME-HH         PIC X(002)   VALUE SPACES.
           05  FILLER             PIC X(001)   VALUE ':'.
           05  WS-TIME-MM         PIC X(002)   VALUE SPACES.
      *
       01  ABEND-PARAM.
           05  ABEND-NUMLINHA     PIC 9(004)   COMP  VALUE ZEROS.
           05  ABEND-MSG01        PIC X(060)   VALUE SPACES.
           05  ABEND-MSG02        PIC X(060)   VALUE SPACES.
           05  ABEND-MSG03.
               07  ABEND-LITERAL  PIC X(055).
               07  ABEND-VARIA    PIC X(005).
           05  ABEND-MSG04        PIC X(060)   VALUE SPACES.
           05  ABEND-MSG05.
              07 FILLER            PIC X(09)  VALUE 'TABELA : '.
              07 ABEND-TABELA      PIC X(05).
              07 FILLER            PIC X(03)  VALUE ' / '.
              07 FILLER            PIC X(08)  VALUE 'CHAVE : '.
              07 ABEND-CHAVE       PIC X(20)  VALUE SPACES.
              07 FILLER            PIC X(10)  VALUE 'SQLCODE : '.
              07 ABEND-SQLCODE     PIC -999.

       01  ABEND1-VSAM.
           05 ABEND1-ARQUIVO            PIC X(016).
           05 ABEND1-MENSAGEM           PIC X(070).
           05 ABEND1-LIT-STATUS         PIC X(008) VALUE 'STATUS: '.
           05 ABEND1-STATUS             PIC X(004) VALUE ' '.

       01  ABEND2-CHAMADA-SERVICO.
           05 ABEND2-ENVIO.
              10 FILLER                 PIC X(009) VALUE 'SERVICO: '.
              10 ABEND2-SERVICO         PIC X(008).
              10 FILLER                 PIC X(010) VALUE ' ENTRADA: '.
              10 ABEND2-ENTRADA         PIC X(088).
           05 ABEND2-RETORNO.
              10 FILLER                 PIC X(009) VALUE 'RET.COD: '.
              10 ABEND2-CD-RET          PIC 9(002).
              10 FILLER                 PIC X(011) VALUE ' MENSAGEM: '.
              10 ABEND2-MSG             PIC X(070).
              10 FILLER                 PIC X(012) VALUE ' RET.DB2: '.
              10 ABEND2-SQL             PIC -999.

      *---------------------------------------------------------------*
      * TABELA DE ATUALIZACOES POR FILIAL                             *
      * A OCORRENCIA E' O CODIGO DA FILIAL                            *
      *---------------------------------------------------------------*
       01  TAB-FILIAL.
           05  TAB-FILIAL-OC  OCCURS 1000.
               10  TAB-FILIAL-CIP      PIC S9(009) COMP.
               10  TAB-FILIAL-CIC-AUT  PIC S9(009) COMP.
               10  TAB-FILIAL-CIC-TLM  PIC S9(009) COMP.
               10  TAB-FILIAL-CIC-MAN  PIC S9(009) COMP.
MAR001         10  TAB-FILIAL-CIP-AUT  PIC S9(009) COMP.
AG             10  TAB-FILIAL-CIP-TLM  PIC S9(009) COMP.
      *
       01  WS-RELATORIO.
      *
           05  CAB01.
               10  FILLER         PIC X(001)   VALUE '1'.
               10  FILLER         PIC X(060)   VALUE
                   'CIPR106'.
               10  FILLER         PIC X(059)   VALUE
                   'ABN-AMRO BANK'.
               10  FILLER         PIC X(009)   VALUE
                   'PAGINA : '.
               10  CAB01-PAGINA   PIC ZZZ9.
      *
           05  CAB02.
               10  FILLER         PIC X(001)   VALUE ' '.
               10  FILLER         PIC X(050)   VALUE
                   'CIPJ055D'.
               10  FILLER         PIC X(057)   VALUE
                   'ESTATISTICAS DE UTILIZACAO CIC X CIP'.
      *
           05  CAB03.
               10  FILLER         PIC X(001)   VALUE ' '.
               10  FILLER         PIC X(054)   VALUE SPACES.
               10  FILLER         PIC X(050)   VALUE
                   'RELATORIO DE ATUALIZACOES'.
               10  FILLER         PIC X(010)   VALUE
                   'EMISSAO : '.
               10  CAB03-DATA     PIC X(010).
               10  FILLER         PIC X(003)   VALUE
                   ' - '.
               10  CAB03-HORA      PIC X(005).
      *
           05  CAB04.
               10  FILLER          PIC X(001)   VALUE ' '.
               10  FILLER          PIC X(132)   VALUE ALL '-'.
      *
           05  CAB05.
               10  FILLER          PIC X(001)   VALUE ' '.
               10  FILLER          PIC X(55)   VALUE
           'FILIAL  CIC-AUTOMATICO  CIC-TELEMARKETING  CIC-MANUAL  '.   OMATICO
               10  FILLER          PIC X(006)   VALUE ' '.
AG    ****     10  FILLER          PIC X(71)   VALUE
AG    **** 'CIP'.                                                       OMATICO
AG             10  FILLER          PIC X(71)   VALUE
MAR001     'CIP     CIP-AUTOMATICO  CIP-TELEMARKETING'.                 OMATICO
      *
           05  DET01.
               10  FILLER          PIC X(004)   VALUE ' '.
               10  DET01-FILIAL    PIC ZZ9.
               10  FILLER          PIC X(005)   VALUE SPACES.
               10  DET01-CIC-AUT   PIC ZZZ.ZZZ.ZZ9.
MAR001         10  FILLER          PIC X(008)   VALUE SPACES.
               10  DET01-CIC-TLM   PIC ZZZ.ZZZ.ZZ9.
               10  FILLER          PIC X(001)   VALUE SPACES.
               10  DET01-CIC-MAN   PIC ZZZ.ZZZ.ZZ9.
               10  DET01-CIP       PIC ZZZ.ZZZ.ZZ9.
MAR001         10  FILLER          PIC X(008)   VALUE SPACES.
MAR001         10  DET01-CIP-AUT   PIC ZZZ.ZZZ.ZZ9.
MAR001         10  FILLER          PIC X(008)   VALUE SPACES.
AG             10  DET01-CIP-TLM   PIC ZZZ.ZZZ.ZZ9.
               10  FILLER          PIC X(002)   VALUE ' '.
MAR001         10  FILLER          PIC X(020)   VALUE ' '.
      *
           05  DET02.
               10  FILLER          PIC X(003)   VALUE ' '.
               10  DET02-MENSAGEM  PIC X(080)   VALUE ' '.
      *
           05  TOT01.
               10  FILLER          PIC X(001)   VALUE ' '.
               10  FILLER          PIC X(043)   VALUE SPACES.
               10  FILLER          PIC X(089)   VALUE
                   'T O T A I S    D E    C O N T R O L E'.
      *
           05  TOT02.
               10  FILLER          PIC X(001)   VALUE ' '.
               10  FILLER          PIC X(032)   VALUE SPACES.
               10  TOT02-LITERAL   PIC X(032).
               10  TOT02-TOTAL     PIC ZZZ.ZZZ.ZZ9.
               10  FILLER          PIC X(058)   VALUE SPACES.
      *
           05  TOT03.
               10  FILLER          PIC X(001)   VALUE ' '.
               10  FILLER          PIC X(032)   VALUE SPACES.
               10  TOT03-LITERAL   PIC X(048).
               10  FILLER          PIC X(051)   VALUE SPACES.

      *---------------------------------------------------------------*
      *--- AREA DE BOOK ----------------------------------------------*
      *---------------------------------------------------------------*

           COPY CICB600.
      *
      *---------------------------------------------------------------*
      * BOOKS DE SERVICOS                                             *
      *---------------------------------------------------------------*

AG         COPY CIPC109.

      *
       PROCEDURE DIVISION.
      *
      *
       000-PRINCIPAL           SECTION.

           PERFORM 100-INICIO      THRU  100-INICIO-EXIT.

           PERFORM 200-LER-CICA600 THRU  200-LER-CICA600-EXIT.

           PERFORM 500-PROCESSAR   THRU  500-PROCESSAR-EXIT
             UNTIL CH-FIM-CICA600 = 'SIM'.

           PERFORM 600-DESCARGA    THRU  600-DESCARGA-EXIT
                   VARYING WS-IND  FROM  1 BY 1
             UNTIL WS-IND  GREATER 999.

           PERFORM 900-TERMINO     THRU  900-TERMINO-EXIT.

           STOP RUN.

       000-EXIT.
           EXIT.
      * ----------------------------------------------------------------
      *    INICIO
      * ----------------------------------------------------------------
       100-INICIO              SECTION.

           OPEN  INPUT  CICA600 TABA539
                 OUTPUT CIPR1061.

           IF FS-CICA600  NOT = '00'
               MOVE 'CICA600'            TO ABEND1-ARQUIVO
               MOVE 'ERRO NO OPEN'      TO ABEND1-MENSAGEM
               MOVE 'STATUS = '         TO ABEND1-LIT-STATUS
               MOVE FS-CICA600           TO ABEND1-STATUS
               MOVE ABEND1-VSAM         TO DET02-MENSAGEM
               PERFORM 800-IMPRIME
               PERFORM 999-ABEND.

           IF FS-TABA539  NOT = '00'
               MOVE 'TABA539'            TO ABEND1-ARQUIVO
               MOVE 'ERRO NO OPEN'      TO ABEND1-MENSAGEM
               MOVE 'STATUS = '         TO ABEND1-LIT-STATUS
               MOVE FS-TABA539           TO ABEND1-STATUS
               MOVE ABEND1-VSAM         TO DET02-MENSAGEM
               PERFORM 800-IMPRIME
               PERFORM 999-ABEND.

           INITIALIZE  TAB-FILIAL.
      *
           MOVE FUNCTION CURRENT-DATE (01:4) TO WS-DT-AAAA.
           MOVE FUNCTION CURRENT-DATE (05:2) TO WS-DT-MM.
           MOVE FUNCTION CURRENT-DATE (07:2) TO WS-DT-DD.
           MOVE WS-DATA-ED                   TO CAB03-DATA.
      *
           MOVE FUNCTION CURRENT-DATE (09:2) TO WS-TIME-HH
           MOVE FUNCTION CURRENT-DATE (11:2) TO WS-TIME-MM
           MOVE WS-TIME-ED                   TO CAB03-HORA.
      *
       100-INICIO-EXIT.
           EXIT.

      * ----------------------------------------------------------------
      *    LEITURA SEQUENCIAL DO CICA600
      * ----------------------------------------------------------------
       200-LER-CICA600          SECTION.
       200-LER-CICA600-VOLTA.

           READ CICA600 INTO RG-CICA600-02600.

           IF FS-CICA600  EQUAL  '10'
               MOVE 'SIM' TO CH-FIM-CICA600
               GO TO 200-LER-CICA600-EXIT.

           IF  FS-CICA600 NOT = '10' AND FS-CICA600 NOT = '00'
               MOVE 'CICA600'            TO ABEND1-ARQUIVO
               MOVE 'ERRO NO READ'      TO ABEND1-MENSAGEM
               MOVE 'STATUS = '         TO ABEND1-LIT-STATUS
               MOVE FS-CICA600           TO ABEND1-STATUS
               MOVE ABEND1-VSAM         TO DET02-MENSAGEM
               PERFORM 800-IMPRIME
               PERFORM 999-ABEND.

           ADD 1                 TO    CT-LIDOS.

           IF  TP-REG-2600      NOT    EQUAL     2
               ADD  1            TO    CT-DESP-TP
               GO  TO  200-LER-CICA600-VOLTA
           END-IF.

           IF  CD-FIL-CLI-2600  NOT     NUMERIC  OR
               CD-FIL-CLI-2600  EQUAL   ZEROS    OR
               CD-FIL-CLI-2600  GREATER 999
               ADD  1            TO    CT-DESP-FILIAL
               GO  TO  200-LER-CICA600-VOLTA
           END-IF.

       200-LER-CICA600-EXIT.
           EXIT.


       500-PROCESSAR           SECTION.

           IF  (SG-USUA-ULT-ATU-CLI-2600    LESS  SPACES)      OR
               (SG-USUA-ULT-ATU-CLI-2600    EQUAL LOW-VALUES)
               MOVE  SPACES                TO   SG-USUA-ULT-ATU-CLI-2600
           END-IF.

           MOVE CD-FIL-CLI-2600       TO        WS-IND

           IF (SG-USUA-ULT-ATU-CLI-2600    EQUAL   'CIP')
AG            PERFORM 520-TRATA-CIP
AG    ***     COMPUTE  TAB-FILIAL-CIP(WS-IND) =
AG    ***              TAB-FILIAL-CIP(WS-IND) + 1
           ELSE
              IF (SG-USUA-ULT-ATU-CLI-2600   EQUAL   'EP1')    OR
                 (SG-USUA-ULT-ATU-CLI-2600   EQUAL   'AF1')
                 COMPUTE  TAB-FILIAL-CIC-AUT(WS-IND) =
                          TAB-FILIAL-CIC-AUT(WS-IND) + 1
              ELSE
                 INITIALIZE RG-TABA539-539
                 MOVE SG-USUA-ULT-ATU-CLI-2600 TO SG-USUAR-539
                 PERFORM 510-CONSULTA-TABA539
                 IF FS-TABA539 EQUAL '23'
URA001              DISPLAY '--> USUARIO NAO ENCONTR.: '
URA001                      SG-USUA-ULT-ATU-CLI-2600
URA001                      ', CLIENTE= ' NO-SEQ-CLI-2600
                    ADD 1 TO CT-DESP-539
                 ELSE
URA001              IF  CD-FILIAL-TAB-539  NOT EQUAL  CD-FIL-CLI-2600
                        COMPUTE   TAB-FILIAL-CIC-TLM(WS-IND) =
                                  TAB-FILIAL-CIC-TLM(WS-IND) + 1
                    ELSE
                         COMPUTE TAB-FILIAL-CIC-MAN(WS-IND) =
                                 TAB-FILIAL-CIC-MAN(WS-IND) + 1.

           PERFORM 200-LER-CICA600  THRU 200-LER-CICA600-EXIT.

       500-PROCESSAR-EXIT.
           EXIT.

      * ----------------------------------------------------------------
      *                 CONSULTA DO ARQUIVO TABA539
      * ----------------------------------------------------------------
       510-CONSULTA-TABA539      SECTION.

           READ TABA539.

           IF FS-TABA539  EQUAL  '23'
               NEXT SENTENCE
           ELSE
               IF  FS-TABA539 NOT = '00'
                   MOVE 'TABA539'            TO ABEND1-ARQUIVO
                   MOVE 'ERRO NO READ'       TO ABEND1-MENSAGEM
                   MOVE 'STATUS = '          TO ABEND1-LIT-STATUS
                   MOVE FS-TABA539           TO ABEND1-STATUS
                   PERFORM 800-IMPRIME
                   PERFORM 999-ABEND.

       510-CONSULTA-TABA539-EXIT.
           EXIT.

AG    *----------------------------------------------------------------*
AG     520-TRATA-CIP      SECTION.
AG    *----------------------------------------------------------------*

AG         INITIALIZE CIPC109.
AG         MOVE 'CIPB106'           TO  CIPC109-10-CD-PGM-MAN.
AG         MOVE  NO-SEQ-CLI-2600    TO  CIPC109-CD-PAT-EN.
AG         MOVE  'N'                TO  CIPC109-IC-PSQ-PRF.

AG         MOVE 'CIPS109'           TO     WS-SERVICO.
AG         CALL WS-SERVICO          USING  CIPC109.

AG         IF (CIPC109-CD-RET-COD)      NOT EQUAL      ZEROS
AG             IF CIPC109-CD-SQL-COD  EQUAL 100
AG                DISPLAY '--> PARTE NAO ENCONTRADA CIP10: '
AG                          SG-USUA-ULT-ATU-CLI-2600
AG                          ', CLIENTE= ' NO-SEQ-CLI-2600
AG                ADD  1  TO CT-DESP-CIP10
AG             ELSE
AG                DISPLAY 'COD. RETORNO = ' CIPC109-CD-RET-COD
AG                DISPLAY 'MSG RETORNO SERVICO : ' CIPC109-DS-MSG
AG                DISPLAY 'CLIENTE   : ' NO-SEQ-CLI-2600
AG                MOVE CIPC109-CD-SQL-COD     TO    ABEND-SQLCODE
AG                MOVE 'CIP10'                TO    ABEND-TABELA
AG                PERFORM  999-ABEND
AG             END-IF
AG         END-IF.

MAR001     IF (CIPC109-10-CD-PGM-MAN     EQUAL     'ADPP06A')
MAR001             COMPUTE TAB-FILIAL-CIP-AUT(WS-IND) =
MAR001                    TAB-FILIAL-CIP-AUT(WS-IND) + 1
MAR001     ELSE
AG           IF (CIPC109-10-CD-USU-ULT-ATU(1:3)   EQUAL  'TNW')
AG               IF (CIPC109-10-CD-USU-ULT-ATU(4:1) EQUAL  'S' OR 'T')
AG                   COMPUTE TAB-FILIAL-CIP-TLM(WS-IND) =
AG                          TAB-FILIAL-CIP-TLM(WS-IND) + 1
AG               ELSE
AG                   COMPUTE TAB-FILIAL-CIP(WS-IND) =
AG                           TAB-FILIAL-CIP(WS-IND) + 1
AG               END-IF
AG           ELSE
AG               IF (CIPC109-10-CD-USU-ULT-ATU(1:3)  EQUAL 'TK3')
AG                   COMPUTE TAB-FILIAL-CIP-TLM(WS-IND) =
AG                           TAB-FILIAL-CIP-TLM(WS-IND) + 1
AG               ELSE
AG                    COMPUTE TAB-FILIAL-CIP(WS-IND) =
AG                            TAB-FILIAL-CIP(WS-IND) + 1
AG               END-IF
AG           END-IF
MAR001     END-IF.

AG    *----------------------------------------------------------------*
AG     520-TRATA-CIP-EXIT.  EXIT.
AG    *----------------------------------------------------------------*
     *
       600-DESCARGA            SECTION.

           IF TAB-FILIAL-CIP(WS-IND) GREATER ZEROS      OR
MAR001        TAB-FILIAL-CIP-AUT(WS-IND) GREATER ZEROS  OR
AG            TAB-FILIAL-CIP-TLM(WS-IND) GREATER ZEROS  OR
              TAB-FILIAL-CIC-AUT(WS-IND) GREATER ZEROS  OR
              TAB-FILIAL-CIC-TLM(WS-IND) GREATER ZEROS  OR
              TAB-FILIAL-CIC-MAN(WS-IND) GREATER ZEROS
              COMPUTE CT-TT-CIC-AUT = CT-TT-CIC-AUT
                                  + TAB-FILIAL-CIC-AUT(WS-IND)
              COMPUTE CT-TT-CIC-TLM = CT-TT-CIC-TLM
                                  + TAB-FILIAL-CIC-TLM(WS-IND)
              COMPUTE CT-TT-CIC-MAN = CT-TT-CIC-MAN
                                  + TAB-FILIAL-CIC-MAN(WS-IND)
              COMPUTE CT-TT-CIP = CT-TT-CIP + TAB-FILIAL-CIP(WS-IND)
MAR001        COMPUTE CT-TT-CIP-AUT = CT-TT-CIP-AUT
MAR001                            + TAB-FILIAL-CIP-AUT(WS-IND)
AG            COMPUTE CT-TT-CIP-TLM = CT-TT-CIP-TLM
AG                                + TAB-FILIAL-CIP-TLM(WS-IND)
              MOVE    WS-IND                 TO DET01-FILIAL
              MOVE    TAB-FILIAL-CIC-AUT(WS-IND) TO DET01-CIC-AUT
              MOVE    TAB-FILIAL-CIC-TLM(WS-IND) TO DET01-CIC-TLM
              MOVE    TAB-FILIAL-CIC-MAN(WS-IND) TO DET01-CIC-MAN
              MOVE    TAB-FILIAL-CIP(WS-IND)     TO DET01-CIP
MAR001        MOVE    TAB-FILIAL-CIP-AUT(WS-IND) TO DET01-CIP-AUT
AG            MOVE    TAB-FILIAL-CIP-TLM(WS-IND) TO DET01-CIP-TLM
              PERFORM 800-IMPRIME
           END-IF.

       600-DESCARGA-EXIT.
           EXIT.

      * ----------------------------------------------------------------
      *    RELATORIO
      * ----------------------------------------------------------------
       800-IMPRIME             SECTION.

           COMPUTE WS-LINHAS = WS-LINHAS + 1.

           IF  WS-LINHAS        GREATER       60
               PERFORM 810-CABECALHO
           END-IF.

           IF  WS-TOTAL    EQUAL  'S' OR
               WS-ERRO     EQUAL  'S'
               PERFORM 820-TOTAL
           ELSE
               WRITE RG-R1061       FROM  DET01
           END-IF.

       800-IMPRIME-EXIT.
           EXIT.

       810-CABECALHO           SECTION.

           ADD      1                  TO  WS-PAGINA
           MOVE     WS-PAGINA          TO  CAB01-PAGINA
           WRITE    RG-R1061         FROM  CAB01
           WRITE    RG-R1061         FROM  CAB02
           WRITE    RG-R1061         FROM  CAB03
           WRITE    RG-R1061         FROM  CAB04
           WRITE    RG-R1061         FROM  CAB05
           WRITE    RG-R1061         FROM  CAB04 AFTER 3

           MOVE  08                    TO  WS-LINHAS.

       810-CABECALHO-EXIT.
           EXIT.

       820-TOTAL               SECTION.

           COMPUTE WS-LINHAS = WS-LINHAS + 60

           IF  WS-LINHAS        GREATER       60
               PERFORM 810-CABECALHO
           END-IF.

           COMPUTE  CT-DESP = CT-DESP-TP + CT-DESP-FILIAL.

           MOVE '* ============================================ *'
                                                   TO TOT03-LITERAL
           WRITE RG-R1061 FROM  TOT03 AFTER 3
           IF    WS-ERRO        EQUAL  'S'
                 MOVE '*  PROGRAMA CIPB106 CANCELADO   '
                                                   TO TOT03-LITERAL
           ELSE
                 MOVE '*  PROGRAMA CIPB106 - TERMINO NORMAL   '
                                                   TO TOT03-LITERAL
           END-IF.

           WRITE RG-R1061 FROM  TOT03
           MOVE '*       LIDOS  CICA600......: '   TO TOT02-LITERAL
           MOVE  CT-LIDOS                          TO TOT02-TOTAL
           WRITE RG-R1061 FROM  TOT02
           MOVE '*ATUAL. CIC-AUTOMATICO......: ' TO TOT02-LITERAL
           MOVE  CT-TT-CIC-AUT                      TO TOT02-TOTAL
           WRITE RG-R1061 FROM  TOT02
           MOVE '*ATUAL. CIC-TELEMARKETING...: ' TO TOT02-LITERAL
           MOVE  CT-TT-CIC-TLM                      TO TOT02-TOTAL
           WRITE RG-R1061 FROM  TOT02
           MOVE '*ATUAL. CIC-MANUAL..........: ' TO TOT02-LITERAL
           MOVE  CT-TT-CIC-MAN                      TO TOT02-TOTAL
           WRITE RG-R1061 FROM  TOT02
           MOVE '*       ATUAL. CIP..........: ' TO TOT02-LITERAL
           MOVE  CT-TT-CIP                         TO TOT02-TOTAL
AG         WRITE RG-R1061 FROM  TOT02
MAR001     MOVE '*ATUAL. CIP-AUTOMATCIO......: ' TO TOT02-LITERAL
MAR001     MOVE  CT-TT-CIP-AUT                     TO TOT02-TOTAL
MAR001     WRITE RG-R1061 FROM  TOT02
AG         MOVE '*ATUAL. CIP-TELEMARKETING...: ' TO TOT02-LITERAL
AG         MOVE  CT-TT-CIP-TLM                     TO TOT02-TOTAL
           WRITE RG-R1061 FROM  TOT02
           MOVE '*       ATUAL. TOTAL........: '   TO TOT02-LITERAL
           COMPUTE CT-TT-GERAL = CT-TT-CIC-AUT +                        IC-MAN +
MAR001                           CT-TT-CIC-TLM +
MAR001                           CT-TT-CIC-MAN +
MAR001                           CT-TT-CIP     +
MAR001                           CT-TT-CIP-AUT +
MAR001                           CT-TT-CIP-TLM
           MOVE  CT-TT-GERAL                       TO TOT02-TOTAL
           WRITE RG-R1061 FROM  TOT02
           MOVE '* -------------------------------------------- *'
                                                   TO TOT03-LITERAL
           WRITE RG-R1061 FROM  TOT03
           MOVE '*  DESPREZADOS'                   TO TOT03-LITERAL
           WRITE RG-R1061 FROM  TOT03
           MOVE '*       TIPO DE REGISTRO......: ' TO TOT02-LITERAL
           MOVE  CT-DESP-TP                        TO TOT02-TOTAL
           WRITE RG-R1061 FROM  TOT02
           MOVE '*       FILIAL INVALIDA.......: ' TO TOT02-LITERAL
           MOVE  CT-DESP-FILIAL                    TO TOT02-TOTAL
           WRITE RG-R1061 FROM  TOT02
           MOVE '*   USUARIO INVALIDO-TABA539..: ' TO TOT02-LITERAL
           MOVE  CT-DESP-539                       TO TOT02-TOTAL
AG         WRITE RG-R1061 FROM  TOT02
AG         MOVE '*   PARTE NAO ENCONTRADA CIP10: ' TO TOT02-LITERAL
AG         MOVE  CT-DESP-CIP10                     TO TOT02-TOTAL
           WRITE RG-R1061 FROM  TOT02
           MOVE '* ============================================ *'
                                                   TO TOT03-LITERAL
           WRITE RG-R1061 FROM  TOT03.
      *
       820-TOTAL-EXIT.
           EXIT.

      * ----------------------------------------------------------------
      *    TERMINO
      * ----------------------------------------------------------------
       900-TERMINO             SECTION.

           CLOSE CICA600 TABA539.

           IF  FS-CICA600   NOT EQUAL  '00'
               MOVE 'CICA600'            TO ABEND1-ARQUIVO
               MOVE 'ERRO NO CLOSE'      TO ABEND1-MENSAGEM
               MOVE 'STATUS = '          TO ABEND1-LIT-STATUS
               MOVE FS-CICA600           TO ABEND1-STATUS
               MOVE ABEND1-VSAM          TO DET02-MENSAGEM
               PERFORM 800-IMPRIME
               PERFORM 999-ABEND.

           IF  FS-TABA539   NOT EQUAL  '00'
               MOVE 'TABA539'            TO ABEND1-ARQUIVO
               MOVE 'ERRO NO CLOSE'      TO ABEND1-MENSAGEM
               MOVE 'STATUS = '          TO ABEND1-LIT-STATUS
               MOVE FS-TABA539           TO ABEND1-STATUS
               MOVE ABEND1-VSAM          TO DET02-MENSAGEM
               PERFORM 800-IMPRIME
               PERFORM 999-ABEND.

           MOVE 'S'                     TO WS-TOTAL
           PERFORM 800-IMPRIME.

           CLOSE CIPR1061.

       900-TERMINO-EXIT.
           EXIT.

       999-ABEND               SECTION.

           MOVE 'S'                     TO WS-ERRO
           PERFORM 800-IMPRIME.

           MOVE 5                                 TO  ABEND-NUMLINHA.
           MOVE 'CIPB106'                         TO  ABEND-MSG01.
           MOVE 'CONTACTAR ANALISTA RESPONSAVEL ' TO  ABEND-MSG04.

           CALL ABEND-ROT  USING  ABEND-PARAM.

       999-EXIT.
           EXIT.

