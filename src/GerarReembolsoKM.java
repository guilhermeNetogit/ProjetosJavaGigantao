import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;
import java.text.NumberFormat;
import java.util.Locale;

public class GerarReembolsoKM implements AcaoRotinaJava { // Github ->Eclipse @guilhermeNetogit 06/01/2026 15:41; Duda passou aqui 05/01/26 20:12

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        // Obtém o código do veículo informado como parâmetro da ação
        Object nroAdiantParam = contexto.getParam("NROADIANT");
        if (nroAdiantParam == null) {
            contexto.mostraErro("Parâmetro NROADIANT não foi informado.");
        }

        // Consulta apenas as colunas necessárias na tabela de controle de KM
        QueryExecutor query = contexto.getQuery();
        query.setParam("NROADIANT", nroAdiantParam);

        query.nativeSelect(
            "SELECT NROADIANT, VLRSOLICIT , " +
        	"	av.CODPARC , par.NOMEPARC , CODCENCUSPAD , " +
            "	CONCAT(YEAR(DATA),NROADIANT) AS NUMNOTA " +
            "FROM AD_ACERTOVIAGEM av " +
        	"	JOIN TGFPAR par ON par.CODPARC = av.CODPARC " +
            "	JOIN TSIUSU usu ON usu.CODPARC = par.CODPARC " +
            "WHERE NROADIANT = {NROADIANT} "
        );

        BigDecimal vlrDesdob = BigDecimal.ZERO;
        Integer codParc = null;
        Integer cenCus = null;
        String nomeParc = "Sem Parceiro";
        String numNota = "Sem Nota";

        while (query.next()) {
            BigDecimal reembolso = query.getBigDecimal("VLRSOLICIT");

            // Se o reembolso for nulo ou menor/igual a zero, interrompe com erro
            if (reembolso == null || reembolso.compareTo(BigDecimal.ZERO) <= 0) {
                int nroAdiant = query.getInt("NROADIANT");
                contexto.mostraErro(
                    "O reembolso do lançamento " + nroAdiant + " não foi calculado ainda."
                );
            }

            vlrDesdob = vlrDesdob.add(reembolso);
            codParc = query.getInt("CODPARC");
            nomeParc = query.getString("NOMEPARC");
            cenCus = query.getInt("CODCENCUSPAD");
            numNota = query.getString("NUMNOTA");
        }

        query.close();

        // Se não houver valor a reembolsar, avisa o usuário mas permite continuar
        if (vlrDesdob.compareTo(BigDecimal.ZERO) == 0) {
            contexto.confirmar(
                "Valor do título zerado",
                "O veículo informado não possui lançamentos para reembolso. O título terá valor zero. Deseja continuar?",
                1
            );
        }

        // Data atual do sistema (data de negociação e vencimento)
        Date dataAtual = new Date(Calendar.getInstance().getTimeInMillis());

        // Cria nova linha no financeiro (TGFFIN)
        Registro financeiro = contexto.novaLinha("TGFFIN");

        financeiro.setCampo("VLRDESDOB", vlrDesdob);
        financeiro.setCampo("RECDESP", -1);                  // Despesa (a pagar)
        financeiro.setCampo("CODEMP", 3);                    // Ajuste conforme sua empresa padrão, se necessário
        financeiro.setCampo("NUMNOTA", numNota);
        financeiro.setCampo("DTNEG", dataAtual);
        financeiro.setCampo("CODPARC", codParc);             // Pode ser parametrizado ou vinculado ao motorista
        financeiro.setCampo("CODNAT", 3040600);              // Natureza financeira - verifique se está correta
        financeiro.setCampo("CODCENCUS", cenCus);            // Centro Resultado do Parceiro - puxando do usuário        
        financeiro.setCampo("CODBCO", 341);
        financeiro.setCampo("CODCTABCOINT", 7);
        financeiro.setCampo("CODTIPTIT", 2);                 // Tipo de título (geralmente "Despesa Diversa")
        financeiro.setCampo("CODTIPOPER", 1300);             // Tipo de operação (geralmente "LANÇAMENTO FINANCEIRO")        
        financeiro.setCampo("DTVENC", dataAtual);            // Pode ser alterado para data futura se desejar
        financeiro.setCampo("HISTORICO", 
            "REEMBOLSO DE VIAGEM Nº " + nroAdiantParam + ", PARA O COLABORADOR " + codParc + " - " + nomeParc + ". Lançado via JAVA/Github.");

        // Salva o registro no banco (gera o NUFIN automaticamente)
        financeiro.save();
        
        BigDecimal vlrDesdobra = (BigDecimal) financeiro.getCampo("VLRDESDOB");
        NumberFormat formatoMoedaBR = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
        String valorFormatado = formatoMoedaBR.format(vlrDesdobra);

        // Monta mensagem de retorno amigável
        StringBuffer mensagem = new StringBuffer();
        mensagem.append("Foi gerado o título ");
        mensagem.append(financeiro.getCampo("NUFIN"));
        mensagem.append(" no valor de ");
        mensagem.append(valorFormatado);
        mensagem.append(" como reembolso de viagem para o colaborador ");
        mensagem.append(codParc);
        mensagem.append(" - ");
        mensagem.append(nomeParc);
        mensagem.append(" via Java/Github.");
        contexto.setMensagemRetorno(mensagem.toString());
    }

}

