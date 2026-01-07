import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Locale;

public class TesteJavaXXXVIII implements AcaoRotinaJava { //@GuilhermeNetoGig 07/01/2026 17:20
   public void doAction(ContextoAcao contexto) throws Exception {
      Registro[] registros = contexto.getLinhas();
      if (registros != null && registros.length != 0) {
         Registro registroSelecionado = registros[0];
         BigDecimal nroAdiant = (BigDecimal)registroSelecionado.getCampo("NROADIANT");
         if (nroAdiant == null) {
            contexto.mostraErro("Não foi possível identificar o número do adiantamento.");
         } else {
            QueryExecutor query = contexto.getQuery();
            query.setParam("NROADIANT", nroAdiant);
            query.nativeSelect("SELECT COUNT(1) AS TOTAL FROM AD_ACERTOVIAGEM WHERE NROADIANT = {NROADIANT} AND NUFINDESP IS NOT NULL");
            if (query.next() && query.getInt("TOTAL") > 0) {
               contexto.mostraErro("Já existe um título financeiro gerado para o adiantamento nº " + nroAdiant);
            } else {
               query.nativeSelect("SELECT STATUS_REGISTRO FROM AD_ACERTOVIAGEM WHERE NROADIANT = {NROADIANT}");
               if (query.next()) {
                  String status = query.getString("STATUS_REGISTRO");
                  if (!"1".equalsIgnoreCase(status)) {
                     contexto.mostraErro("O adiantamento nº " + nroAdiant + " não está com status 'Aguardando Aprovação'. Ação não permitida. Solicite Aprovação.");
                  } else {
                     query.close();
                     query = contexto.getQuery();
                     query.setParam("NROADIANT", nroAdiant);
                     query.nativeSelect("SELECT A.CODPARC, NOMEPARC, VLRSOLICIT, DATA, DHAPROV, NROADIANT FROM AD_ACERTOVIAGEM A LEFT JOIN TGFPAR P ON P.CODPARC = A.CODPARC WHERE NROADIANT = {NROADIANT}");
                     double vlrDesdob = 0.0D;
                     Integer codParc = 0;
                     String nomeParc = "";
                     Timestamp dhAprov = null;
                     Date dataSol = null;

                     while(query.next()) {
                        double reembolso = query.getDouble("VLRSOLICIT");
                        codParc = query.getInt("CODPARC");
                        nomeParc = query.getString("NOMEPARC");
                        if (dhAprov == null) {
                           dhAprov = query.getTimestamp("DHAPROV");
                        }

                        java.util.Date utilData = query.getDate("DATA");
                        if (utilData != null && dataSol == null) {
                           dataSol = new Date(utilData.getTime());
                        }

                        if (reembolso > 0.0D) {
                           vlrDesdob += reembolso;
                        } else {
                           contexto.mostraErro("O reembolso do lançamento " + query.getInt("NROADIANT") + " não foi calculado ainda.");
                        }
                     }

                     if (vlrDesdob == 0.0D) {
                        contexto.confirmar("Valor do título zerado", "O adiantamento informado não possui lançamentos para reembolso, o título terá valor de desdobramento igual a zero. Deseja continuar?", 1);
                     }

                     query.close();
                     
                     Registro financeiro = contexto.novaLinha("TGFFIN");
                     financeiro.setCampo("VLRDESDOB", vlrDesdob);
                     financeiro.setCampo("RECDESP", -1);
                     financeiro.setCampo("CODEMP", 1);
                     // AQUI: BUSCAR PRÓXIMO NUMNOTA
                     QueryExecutor queryNumNotaFin = contexto.getQuery();
                     queryNumNotaFin.nativeSelect("SELECT ISNULL(MAX(NUMNOTA), 0) + 1 AS PROXIMO FROM TGFFIN WHERE CODTIPOPER = 215");
                     int proximoNumnotaFin = 1;
                     if (queryNumNotaFin.next()) {
                         proximoNumnotaFin = queryNumNotaFin.getInt("PROXIMO");
                     }
                     queryNumNotaFin.close();
                     // AQUI USA O PRÓXIMO
                     financeiro.setCampo("NUMNOTA", proximoNumnotaFin);
                     financeiro.setCampo("DTNEG", dataSol);
                     financeiro.setCampo("CODPARC", codParc);
                     financeiro.setCampo("CODNAT", 4040000);
                     financeiro.setCampo("CODBCO", 0);
                     financeiro.setCampo("CODTIPTIT", 2);
                     financeiro.setCampo("CODTIPOPER", 215);
                     financeiro.setCampo("DTVENC", dataSol);
                     financeiro.setCampo("HISTORICO", "TESTE JAVA/Github PARA REEMBOLSO DE VIAGEM " + nroAdiant);
                     financeiro.save();
                     QueryExecutor updateQuery = contexto.getQuery();
                     updateQuery.setParam("NROADIANT", nroAdiant);
                     updateQuery.update("UPDATE AD_ACERTOVIAGEM SET STATUS_REGISTRO = '77', DHAPROV = GETDATE() , CODUSUAPROV = " + contexto.getUsuarioLogado() + ", " + "NUFINDESP = " + financeiro.getCampo("NUFIN") + ", " + "OBSADIANT = CAST(ISNULL(OBSADIANT, '') AS VARCHAR(MAX)) + CHAR(13) + CHAR(10) + 'Título financeiro gerado: NUFIN = " + financeiro.getCampo("NUFIN") + "' " + "WHERE NROADIANT = {NROADIANT}");
                     updateQuery.close();
                     
                     BigDecimal vlrDesdobra = (BigDecimal) financeiro.getCampo("VLRDESDOB");
                     NumberFormat formatoMoedaBR = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
                     String valorFormatado = formatoMoedaBR.format(vlrDesdobra);                     
                     StringBuffer mensagem = new StringBuffer();
                     
                     mensagem.append("Foi gerado o título ");
                     mensagem.append(financeiro.getCampo("NUFIN"));
                     mensagem.append(" no valor de ");
                     mensagem.append(valorFormatado);
                     //mensagem.append(financeiro.getCampo("VLRDESDOB"));
                     mensagem.append(" como reembolso de Viagem para o registro nº ");
                     mensagem.append(nroAdiant);
                     mensagem.append(" para o parceiro ");
                     mensagem.append(codParc);
                     mensagem.append(" - ");
                     mensagem.append(nomeParc);
                     contexto.setMensagemRetorno(mensagem.toString());
                  }
               } else {
                  contexto.mostraErro("Adiantamento nº " + nroAdiant + " não encontrado.");
               }
            }
         }
      } else {
         contexto.mostraErro("Nenhum registro selecionado.");
      }
   }
}

